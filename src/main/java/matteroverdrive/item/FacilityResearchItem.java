package matteroverdrive.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import matteroverdrive.blockentity.MatterAnalyzerBlockEntity;
import matteroverdrive.quest.ResearchProgression;
import javax.annotation.Nullable;
import java.util.List;

/** Field research complements the scientist campaign without bypassing its objectives. */
public final class FacilityResearchItem extends Item {
    public enum Archive {
        SYNTHETIC_MANUFACTURING_PLANT("Synthetic assembly records", "Separate fabrication, charging and shipping lines with buffered storage.", 100),
        MATTER_REFINERY("Matter recovery survey", "Analyze recovered materials before archiving replication patterns.", 100),
        QUANTUM_RELAY_STATION("Quantum relay calibration", "Buffer relay endpoints and inspect network demand before adding load.", 150),
        ANDROID_COMMAND_BUNKER("Android command doctrine", "Guard squads defend their deployment station; clear security before salvaging it.", 150),
        FUSION_RESEARCH_COMPLEX("Fusion containment study", "Establish powered stabilizers and controlled matter input before reactor operation.", 200),
        BLACK_SITE("Restricted anomaly dossier", "Containment equipment is not proof that an abandoned experiment is safe.", 250),
        ABANDONED_MATTER_LAB("Field matter laboratory notes", "The excavator and storage matrix were paired to process samples without exposing the main facility.", 75),
        ANDROID_RELAY_OUTPOST("Relay outpost command log", "The induction relay called a persistent security drone because the outpost was never truly abandoned.", 75),
        ANOMALY_RESEARCH_SITE("Anomaly site containment log", "Containment hardware must be powered and observed before an anomaly site can be safely reclaimed.", 100),
        MATTER_OBSERVATORY("Observatory survey record", "The analyzer and sensor mast isolate field measurements from the facility network.", 100),
        FIELD_LOGISTICS_DEPOT("Logistics depot manifest", "Charging, switching and protected storage form the minimum viable field supply chain.", 100);
        final String title, finding;
        final int xp;
        Archive(String title, String finding, int xp) { this.title=title; this.finding=finding; this.xp=xp; }
    }
    public FacilityResearchItem(Properties properties) { super(properties); }
    @Nullable private static Archive archive(ItemStack stack) {
        if (!stack.hasTag()) return null;
        try { return Archive.valueOf(stack.getTag().getString("FacilityArchive")); }
        catch (IllegalArgumentException ex) { return null; }
    }
    @Override public Component getName(ItemStack stack) {
        Archive archive = archive(stack);
        return Component.literal(archive == null ? "Recovered Research Dossier" : archive.title);
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (archive(stack) == null) return InteractionResultHolder.pass(stack);
        if (!level.isClientSide) player.displayClientMessage(Component.literal(
                "Research dossier requires a powered Matter Analyzer. Use it on the Analyzer to queue a secure analysis."), false);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override public net.minecraft.world.InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (archive(stack) == null) return net.minecraft.world.InteractionResult.PASS;
        if (context.getLevel().isClientSide) return net.minecraft.world.InteractionResult.SUCCESS;
        Player player = context.getPlayer();
        if (!(player instanceof ServerPlayer server)
                || !(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MatterAnalyzerBlockEntity analyzer)) {
            if (player != null) player.displayClientMessage(Component.literal("Research dossiers can only be queued at a Matter Analyzer."), true);
            return net.minecraft.world.InteractionResult.CONSUME;
        }
        return analyzer.queueResearch(server, stack)
                ? net.minecraft.world.InteractionResult.CONSUME : net.minecraft.world.InteractionResult.FAIL;
    }

    public static boolean isArchived(ServerPlayer server, ItemStack stack) {
        Archive archive = archive(stack);
        if (archive == null) return false;
        CompoundTag discoveries = server.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG)
                .getCompound("MatterOverdriveFacilityResearch");
        return discoveries.getBoolean(archive.name());
    }

    /** Called only by the server-side Matter Analyzer after its paid analysis completes. */
    public static boolean archiveAtAnalyzer(ServerPlayer server, ItemStack stack) {
        Archive archive = archive(stack);
        if (archive == null || isArchived(server, stack)) return false;
        CompoundTag persisted = server.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag discoveries = persisted.getCompound("MatterOverdriveFacilityResearch");
        discoveries.putBoolean(archive.name(), true);
        persisted.put("MatterOverdriveFacilityResearch", discoveries);
        server.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        server.giveExperiencePoints(archive.xp);
        String rewardId = switch (archive) {
                    case SYNTHETIC_MANUFACTURING_PLANT -> "upgrade_speed";
                    case MATTER_REFINERY -> "upgrade_matter_storage";
                    case QUANTUM_RELAY_STATION -> "upgrade_power_storage";
                    case ANDROID_COMMAND_BUNKER -> "weapon_module_holo_sights";
                    case FUSION_RESEARCH_COMPLEX -> "upgrade_failsafe";
                    case BLACK_SITE -> "chassis_optics_precision";
                    case ABANDONED_MATTER_LAB -> "upgrade_matter_storage";
                    case ANDROID_RELAY_OUTPOST -> "upgrade_power_storage";
                    case ANOMALY_RESEARCH_SITE -> "upgrade_failsafe";
                    case MATTER_OBSERVATORY -> "upgrade_range";
                    case FIELD_LOGISTICS_DEPOT -> "upgrade_speed";
        };
        ItemStack reward = new ItemStack(matteroverdrive.registry.ModItems.get(rewardId).get());
        if (!server.getInventory().add(reward)) server.drop(reward, false);
        ResearchProgression.Stage unlocked = researchStage(archive);
        boolean stageAdvanced = ResearchProgression.unlockEvidence(server, unlocked);
        String progression = stageAdvanced ? "; clearance advanced to " + unlocked.title : "";
        server.displayClientMessage(Component.literal("Research archived: " + archive.title + " (+" + archive.xp + " XP; research equipment delivered" + progression + ")"), false);
        server.displayClientMessage(Component.literal(archive.finding), false);
        return true;
    }

    /** Facility evidence advances the shared research spine without allowing a dossier to regress it. */
    private static ResearchProgression.Stage researchStage(Archive archive) {
        return switch (archive) {
            case SYNTHETIC_MANUFACTURING_PLANT, ANDROID_COMMAND_BUNKER,
                    ANDROID_RELAY_OUTPOST, FIELD_LOGISTICS_DEPOT, QUANTUM_RELAY_STATION
                    -> ResearchProgression.Stage.AUTOMATION_DRONES;
            case MATTER_REFINERY, ABANDONED_MATTER_LAB, MATTER_OBSERVATORY
                    -> ResearchProgression.Stage.MATTER_TECHNOLOGY;
            case FUSION_RESEARCH_COMPLEX -> ResearchProgression.Stage.FUSION_RESEARCH;
            case ANOMALY_RESEARCH_SITE, BLACK_SITE -> ResearchProgression.Stage.ANOMALY_ENGINEERING;
        };
    }
    @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        Archive archive = archive(stack);
        lines.add(Component.literal(archive == null
                ? "Use on a powered Matter Analyzer to recover research."
                : "Analyzer dossier: " + archive.finding));
        lines.add(Component.literal("Analysis consumes the dossier; first archive awards XP and equipment per player."));
    }
}
