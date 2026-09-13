package matteroverdrive.quest;

import matteroverdrive.event.StructureLoreEvents;
import matteroverdrive.item.RecoveredArtifactItem;
import matteroverdrive.registry.ModItems;
import matteroverdrive.android.AndroidLoadout;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Locale;

/**
 * Repeatable, event-driven field assignments that bridge exploration and the
 * research campaign. State lives on the player and only changes when relevant
 * gameplay events occur; there is deliberately no global/ticking mission manager.
 */
public final class FieldOperations {
    public static final String ROOT_KEY = "MatterOverdriveFieldOperations";
    private static final String ACTIVE_KEY = "Active";
    private static final String PROGRESS_KEY = "Progress";
    private static final String COMPLETIONS_KEY = "Completions";
    private static final int MAX_COMPLETIONS = 10_000;

    public enum Doctrine {
        RECOVERY("Recovery", "Recover lost technology and intact research from abandoned sites."),
        SYSTEMS("Systems", "Restore or validate automation, power and logistics infrastructure."),
        ANOMALY("Anomaly", "Collect field evidence from unstable containment and high-energy sites.");
        public final String title;
        public final String description;
        Doctrine(String title, String description) { this.title = title; this.description = description; }
    }

    public enum Operation {
        RELAY_SALVAGE(Doctrine.RECOVERY, ResearchProgression.Stage.AUTOMATION_DRONES,
                "Relay Salvage", "android_relay_outpost", 2,
                "Recover evidence from Android relay infrastructure."),
        LAB_RECOVERY(Doctrine.RECOVERY, ResearchProgression.Stage.MATTER_TECHNOLOGY,
                "Laboratory Recovery", "abandoned_matter_lab", 2,
                "Recover intact matter-technology archives from abandoned laboratories."),
        LOGISTICS_RECOMMISSION(Doctrine.SYSTEMS, ResearchProgression.Stage.AUTOMATION_DRONES,
                "Depot Recommission", "field_logistics_depot", 2,
                "Audit logistics depots and restore field-network readiness."),
        OBSERVATORY_SURVEY(Doctrine.SYSTEMS, ResearchProgression.Stage.ADVANCED_POWER,
                "Observatory Survey", "matter_observatory", 2,
                "Validate instrumentation and power readiness at matter observatories."),
        CONTAINMENT_AUDIT(Doctrine.ANOMALY, ResearchProgression.Stage.FUSION_RESEARCH,
                "Containment Audit", "anomaly_research_site", 2,
                "Acquire repeatable containment evidence without forcing an anomaly breach."),
        HORIZON_EXPOSURE(Doctrine.ANOMALY, ResearchProgression.Stage.ANOMALY_ENGINEERING,
                "Horizon Exposure", "event_horizon", 1,
                "Cross a controlled event horizon and return with telemetry.");

        public final Doctrine doctrine;
        public final ResearchProgression.Stage minimumStage;
        public final String title;
        public final String target;
        public final int goal;
        public final String description;

        Operation(Doctrine doctrine, ResearchProgression.Stage minimumStage, String title,
                  String target, int goal, String description) {
            this.doctrine = doctrine;
            this.minimumStage = minimumStage;
            this.title = title;
            this.target = target;
            this.goal = goal;
            this.description = description;
        }
    }

    private FieldOperations() {}

    public static Operation active(ServerPlayer player) {
        CompoundTag root = root(player);
        int ordinal = root.getInt(ACTIVE_KEY) - 1;
        return ordinal >= 0 && ordinal < Operation.values().length ? Operation.values()[ordinal] : null;
    }

    public static int progress(ServerPlayer player) { return Math.max(0, root(player).getInt(PROGRESS_KEY)); }
    public static int completions(ServerPlayer player) { return Math.max(0, root(player).getInt(COMPLETIONS_KEY)); }

    public static Operation assignNext(ServerPlayer player, Doctrine preferred) {
        Operation[] eligible = Arrays.stream(Operation.values())
                .filter(op -> ResearchProgression.atLeast(player, op.minimumStage))
                .filter(op -> preferred == null || op.doctrine == preferred)
                .toArray(Operation[]::new);
        if (eligible.length == 0 && preferred != null) return assignNext(player, null);
        if (eligible.length == 0) return null;

        int seed = Math.floorMod(player.getUUID().hashCode() + completions(player), eligible.length);
        Operation chosen = eligible[seed];
        CompoundTag root = root(player);
        root.putInt(ACTIVE_KEY, chosen.ordinal() + 1);
        root.putInt(PROGRESS_KEY, 0);
        save(player, root);
        return chosen;
    }

    public static boolean recordSite(ServerPlayer player, String site) {
        Operation operation = active(player);
        if (operation == null || site == null || !operation.target.equals(site)) return false;
        return advance(player, operation, 1);
    }

    public static boolean recordAnomalyHorizon(ServerPlayer player) {
        Operation operation = active(player);
        if (operation == null || operation != Operation.HORIZON_EXPOSURE) return false;
        return advance(player, operation, 1);
    }

    private static boolean advance(ServerPlayer player, Operation operation, int amount) {
        CompoundTag root = root(player);
        int next = Math.min(operation.goal, Math.max(0, root.getInt(PROGRESS_KEY)) + Math.max(1, amount));
        root.putInt(PROGRESS_KEY, next);
        save(player, root);
        player.sendSystemMessage(Component.literal("FIELD OPERATION: " + operation.title + " " + next + "/" + operation.goal));
        if (next < operation.goal) return true;
        complete(player, operation);
        return true;
    }

    private static void complete(ServerPlayer player, Operation operation) {
        CompoundTag root = root(player);
        int completed = Math.min(MAX_COMPLETIONS, Math.max(0, root.getInt(COMPLETIONS_KEY)) + 1);
        root.putInt(COMPLETIONS_KEY, completed);
        root.remove(ACTIVE_KEY);
        root.remove(PROGRESS_KEY);
        save(player, root);

        StructureLoreEvents.discoverFromFieldOperation(player, operation.target);

        player.giveExperiencePoints(50 + Math.min(200, completed * 5));
        ItemStack reward = reward(operation, completed);
        if (!reward.isEmpty() && !player.getInventory().add(reward)) player.drop(reward, false);
        player.sendSystemMessage(Component.literal("FIELD OPERATION COMPLETE: " + operation.title
                + " | doctrine " + operation.doctrine.title + " | reward secured."));
        assignNext(player, operation.doctrine);
    }

    private static ItemStack reward(Operation operation, int completed) {
        return switch (operation.doctrine) {
            case RECOVERY -> RecoveredArtifactItem.recovered(AndroidLoadout.Artifact.values()[
                    1 + Math.floorMod(operation.ordinal() + completed, AndroidLoadout.Artifact.values().length - 1)]);
            case SYSTEMS -> new ItemStack(ModItems.get(completed % 3 == 0 ? "upgrade_parallel_processing" : "upgrade_speed").get());
            case ANOMALY -> new ItemStack(ModItems.get(completed % 3 == 0 ? "upgrade_failsafe" : "dilithium_crystal").get(), completed % 3 == 0 ? 1 : 2);
        };
    }

    public static String status(ServerPlayer player) {
        Operation operation = active(player);
        if (operation == null) return "No active field operation. Use /matteroverdrive research field assign <doctrine>.";
        return operation.title + " [" + operation.doctrine.title + "] " + progress(player) + "/" + operation.goal
                + " - " + operation.description;
    }

    public static Doctrine parseDoctrine(String raw) {
        if (raw == null) return null;
        try { return Doctrine.valueOf(raw.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ignored) { return null; }
    }

    private static CompoundTag root(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(ROOT_KEY);
    }

    private static void save(ServerPlayer player, CompoundTag root) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(ROOT_KEY, root);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }
}
