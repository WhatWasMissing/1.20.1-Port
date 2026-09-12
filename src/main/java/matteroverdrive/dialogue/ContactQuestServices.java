package matteroverdrive.dialogue;

import matteroverdrive.entity.DefectorAndroidEntity;
import matteroverdrive.entity.FacilityResearcherEntity;
import matteroverdrive.quest.FieldOperations;
import matteroverdrive.world.AmbientLoreSavedData;
import matteroverdrive.world.StructureLoreSavedData;
import matteroverdrive.world.TechnologyLoreSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/**
 * Crouch-interaction service channel for contemporary field contacts.
 * Normal interaction remains the branching conversation graph. Services are
 * deliberately based on existing progression rather than introducing a second
 * inventory/quest economy that would compete with Field Operations.
 */
public final class ContactQuestServices {
    private static final String ROOT_KEY = "MatterOverdriveContactNetwork";
    private static final String STAGE_KEY = "Stage_";
    private static final String COOLDOWN_KEY = "Cooldown_";

    private enum ServiceKind {
        SURVEY, SALVAGE, RECOVERY, REACTOR, MEDICAL, ARCHIVE, MORROW, CHORUS, HEPHAESTUS
    }

    private ContactQuestServices() {}

    public static void useHuman(ServerPlayer player, FacilityResearcherEntity.Role role) {
        switch (role) {
            case FIELD_RESEARCHER -> use(player, "researcher.field", role.title(),
                    FactionReputation.Faction.RECOVERY_NETWORK, FieldOperations.Doctrine.RECOVERY, ServiceKind.SURVEY);
            case SALVAGER -> use(player, "researcher.salvager", role.title(),
                    FactionReputation.Faction.RECOVERY_NETWORK, FieldOperations.Doctrine.RECOVERY, ServiceKind.SALVAGE);
            case RECOVERY_SPECIALIST -> use(player, "researcher.recovery", role.title(),
                    FactionReputation.Faction.RECOVERY_NETWORK, FieldOperations.Doctrine.RECOVERY, ServiceKind.RECOVERY);
            case ICARUS_ENGINEER -> use(player, "researcher.icarus", role.title(),
                    FactionReputation.Faction.RECOVERY_NETWORK, FieldOperations.Doctrine.SYSTEMS, ServiceKind.REACTOR);
            case JANUS_MEDIC -> use(player, "researcher.janus", role.title(),
                    FactionReputation.Faction.RECOVERY_NETWORK, FieldOperations.Doctrine.ANOMALY, ServiceKind.MEDICAL);
            case ARCHIVIST -> use(player, "researcher.archivist", role.title(),
                    FactionReputation.Faction.ARCHIVE_COUNCIL, FieldOperations.Doctrine.RECOVERY, ServiceKind.ARCHIVE);
        }
    }

    public static void useSynthetic(ServerPlayer player, DefectorAndroidEntity.Role role) {
        switch (role) {
            case MORROW_SCOUT -> use(player, "synthetic.morrow", role.title(),
                    FactionReputation.Faction.MORROW, FieldOperations.Doctrine.RECOVERY, ServiceKind.MORROW);
            case CHORUS_COURIER -> use(player, "synthetic.chorus", role.title(),
                    FactionReputation.Faction.CHORUS, FieldOperations.Doctrine.ANOMALY, ServiceKind.CHORUS);
            case HEPHAESTUS_LIAISON -> use(player, "synthetic.hephaestus", role.title(),
                    FactionReputation.Faction.HEPHAESTUS, FieldOperations.Doctrine.SYSTEMS, ServiceKind.HEPHAESTUS);
        }
    }

    public static int stage(ServerPlayer player, FactionReputation.Faction faction) {
        return root(player).getInt(STAGE_KEY + faction.name());
    }

    public static String questStatus(ServerPlayer player, FactionReputation.Faction faction) {
        int stage = stage(player, faction);
        if (stage >= 3) return faction.title() + " contact chain: COMPLETE";
        return faction.title() + " contact chain: stage " + stage + "/3 — " + requirement(player, faction, stage);
    }

    public static String serviceStatus(ServerPlayer player, FactionReputation.Faction faction) {
        long remaining = Math.max(0L, root(player).getLong(COOLDOWN_KEY + faction.name()) - player.serverLevel().getGameTime());
        return remaining <= 0L ? "service ready" : "service cooldown " + Math.max(1L, remaining / 20L) + "s";
    }

    private static void use(ServerPlayer player, String dialogueId, String contact,
                            FactionReputation.Faction faction, FieldOperations.Doctrine doctrine, ServiceKind kind) {
        FactionReputation.Tier tier = FactionReputation.tier(player, faction);
        int score = FactionReputation.score(player, faction);
        player.sendSystemMessage(Component.literal("CONTACT LINK // " + contact)
                .withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal(FactionReputation.status(player, faction))
                .withStyle(tier.atLeast(FactionReputation.Tier.TRUSTED) ? ChatFormatting.GREEN : ChatFormatting.GRAY));

        if (!tier.atLeast(FactionReputation.Tier.COOPERATIVE)) {
            player.sendSystemMessage(Component.literal("Service access withheld. Build trust through the normal conversation channel first.")
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }

        advanceContactChain(player, faction);
        player.sendSystemMessage(Component.literal(questStatus(player, faction)).withStyle(ChatFormatting.DARK_AQUA));

        if (FieldOperations.active(player) == null) {
            FieldOperations.Operation operation = FieldOperations.assignNext(player, doctrine);
            if (operation != null) {
                player.sendSystemMessage(Component.literal("FIELD WORK ASSIGNED: " + operation.title)
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        } else {
            player.sendSystemMessage(Component.literal("Existing field operation retained: " + FieldOperations.status(player))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        CompoundTag root = root(player);
        String cooldownKey = COOLDOWN_KEY + faction.name();
        long now = player.serverLevel().getGameTime();
        long readyAt = root.getLong(cooldownKey);
        if (readyAt > now) {
            player.sendSystemMessage(Component.literal("Support package unavailable for another "
                    + Math.max(1L, (readyAt - now) / 20L) + " seconds.").withStyle(ChatFormatting.GRAY));
            return;
        }

        applyService(player, kind, stage(player, faction));
        long cooldown = tier == FactionReputation.Tier.ALLIED ? 4_800L
                : tier == FactionReputation.Tier.TRUSTED ? 7_200L : 12_000L;
        root.putLong(cooldownKey, now + cooldown);
        saveRoot(player, root);
        player.sendSystemMessage(Component.literal("Support package authorized. " + serviceStatus(player, faction))
                .withStyle(ChatFormatting.GREEN));
    }

    private static void advanceContactChain(ServerPlayer player, FactionReputation.Faction faction) {
        CompoundTag root = root(player);
        int current = Math.max(0, Math.min(3, root.getInt(STAGE_KEY + faction.name())));
        if (current >= 3 || !stageRequirementMet(player, faction, current)) return;
        int next = current + 1;
        root.putInt(STAGE_KEY + faction.name(), next);
        saveRoot(player, root);
        player.giveExperiencePoints(35 + next * 25);
        player.sendSystemMessage(Component.literal("CONTACT CHAIN ADVANCED // " + faction.title() + " " + next + "/3")
                .withStyle(ChatFormatting.GREEN));
    }

    private static boolean stageRequirementMet(ServerPlayer player, FactionReputation.Faction faction, int stage) {
        var id = player.getUUID();
        DialogueStateSavedData dialogue = DialogueStateSavedData.get(player.serverLevel());
        int operations = FieldOperations.completions(player);
        int fieldLogs = AmbientLoreSavedData.get(player.serverLevel()).count(id);
        int primaryRecords = StructureLoreSavedData.get(player.serverLevel()).count(id);
        int technology = TechnologyLoreSavedData.get(player.serverLevel()).count(id);

        if (stage == 0) return FactionReputation.tier(player, faction).atLeast(FactionReputation.Tier.COOPERATIVE);
        if (stage == 1) return switch (faction) {
            case RECOVERY_NETWORK -> operations >= 1;
            case MORROW -> fieldLogs >= 8;
            case CHORUS -> primaryRecords >= 6 && dialogue.archiveInsight(id) >= 12;
            case HEPHAESTUS -> technology >= 16;
            case ARCHIVE_COUNCIL -> fieldLogs >= 12 && primaryRecords >= 6;
        };
        return switch (faction) {
            case RECOVERY_NETWORK -> operations >= 3 && dialogue.fieldTrust(id) >= 8;
            case MORROW -> fieldLogs >= 16 && dialogue.syntheticTrust(id) >= 8;
            case CHORUS -> primaryRecords >= 10 && dialogue.archiveInsight(id) >= 24;
            case HEPHAESTUS -> technology >= 32 && dialogue.syntheticTrust(id) >= 8;
            case ARCHIVE_COUNCIL -> primaryRecords >= 12 && dialogue.archiveInsight(id) >= 24;
        };
    }

    private static String requirement(ServerPlayer player, FactionReputation.Faction faction, int stage) {
        if (stage <= 0) return "reach Cooperative standing through conversation";
        if (stage == 1) return switch (faction) {
            case RECOVERY_NETWORK -> "complete at least one Field Operation";
            case MORROW -> "authenticate 8 physical Field Logs";
            case CHORUS -> "recover 6 primary records and reach 12 Archive Insight";
            case HEPHAESTUS -> "index 16 technologies";
            case ARCHIVE_COUNCIL -> "authenticate 12 Field Logs and recover 6 primary records";
        };
        return switch (faction) {
            case RECOVERY_NETWORK -> "complete 3 Field Operations and reach Field Trust 8";
            case MORROW -> "authenticate 16 Field Logs and reach Synthetic Trust 8";
            case CHORUS -> "recover 10 primary records and reach 24 Archive Insight";
            case HEPHAESTUS -> "index 32 technologies and reach Synthetic Trust 8";
            case ARCHIVE_COUNCIL -> "recover 12 primary records and reach 24 Archive Insight";
        };
    }

    private static void applyService(ServerPlayer player, ServiceKind kind, int stage) {
        int longDuration = 20 * (180 + stage * 60);
        int shortDuration = 20 * (60 + stage * 30);
        switch (kind) {
            case SURVEY -> {
                add(player, MobEffects.NIGHT_VISION, longDuration, 0);
                add(player, MobEffects.LUCK, shortDuration, 0);
            }
            case SALVAGE -> {
                add(player, MobEffects.DIG_SPEED, longDuration, 0);
                add(player, MobEffects.LUCK, longDuration, 0);
            }
            case RECOVERY -> {
                add(player, MobEffects.MOVEMENT_SPEED, longDuration, 0);
                add(player, MobEffects.NIGHT_VISION, longDuration, 0);
            }
            case REACTOR -> {
                add(player, MobEffects.FIRE_RESISTANCE, longDuration, 0);
                add(player, MobEffects.DAMAGE_RESISTANCE, shortDuration, 0);
            }
            case MEDICAL -> {
                player.removeEffect(MobEffects.CONFUSION);
                player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                add(player, MobEffects.REGENERATION, 20 * 12, Math.min(1, stage / 2));
                add(player, MobEffects.ABSORPTION, shortDuration, 0);
            }
            case ARCHIVE -> {
                add(player, MobEffects.NIGHT_VISION, longDuration, 0);
                add(player, MobEffects.LUCK, shortDuration, 0);
            }
            case MORROW -> {
                add(player, MobEffects.MOVEMENT_SPEED, longDuration, 0);
                add(player, MobEffects.DAMAGE_RESISTANCE, shortDuration, 0);
            }
            case CHORUS -> {
                add(player, MobEffects.NIGHT_VISION, longDuration, 0);
                add(player, MobEffects.LUCK, longDuration, 0);
            }
            case HEPHAESTUS -> {
                add(player, MobEffects.DIG_SPEED, longDuration, 0);
                add(player, MobEffects.ABSORPTION, shortDuration, 0);
            }
        }
    }

    private static void add(ServerPlayer player, net.minecraft.world.effect.MobEffect effect,
                            int duration, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false, true));
    }

    private static CompoundTag root(ServerPlayer player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        return persisted.getCompound(ROOT_KEY).copy();
    }

    private static void saveRoot(ServerPlayer player, CompoundTag root) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(ROOT_KEY, root);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }
}
