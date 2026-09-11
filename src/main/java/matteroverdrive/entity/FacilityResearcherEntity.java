package matteroverdrive.entity;

import matteroverdrive.network.ModNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Locale;

/**
 * Post-collapse expedition NPC used to make recovered facilities feel inhabited.
 * These are contemporary explorers, not resurrected members of the historic cast.
 */
public class FacilityResearcherEntity extends Villager {
    public enum Role {
        FIELD_RESEARCHER("Field Researcher"),
        SALVAGER("Salvage Specialist"),
        RECOVERY_SPECIALIST("Recovery Specialist"),
        ICARUS_ENGINEER("Reactor Recovery Engineer"),
        JANUS_MEDIC("Anomaly Field Medic"),
        ARCHIVIST("Incident Archivist");

        private final String title;
        Role(String title) { this.title = title; }
        public String title() { return title; }

        static Role from(String value) {
            try { return Role.valueOf(value.toUpperCase(Locale.ROOT)); }
            catch (IllegalArgumentException ex) { return FIELD_RESEARCHER; }
        }
    }

    private Role role = Role.FIELD_RESEARCHER;

    public FacilityResearcherEntity(EntityType<? extends FacilityResearcherEntity> type, Level level) {
        super(type, level);
        setVillagerData(new VillagerData(VillagerType.PLAINS, VillagerProfession.LIBRARIAN, 3));
        setPersistenceRequired();
    }

    public void setRole(Role role) {
        this.role = role == null ? Role.FIELD_RESEARCHER : role;
        setCustomName(Component.literal(this.role.title()));
        setCustomNameVisible(true);
        setPersistenceRequired();
    }

    public Role getRole() { return role; }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            ModNetwork.openDialogue(serverPlayer, role.title(), dialogueTitle(), dialogueLines());
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    private String dialogueTitle() {
        return switch (role) {
            case FIELD_RESEARCHER -> "Recovered Site Survey";
            case SALVAGER -> "Salvage Notes";
            case RECOVERY_SPECIALIST -> "Recovery Brief";
            case ICARUS_ENGINEER -> "ICARUS Safety Brief";
            case JANUS_MEDIC -> "Resonance Exposure Advisory";
            case ARCHIVIST -> "Incident Archive";
        };
    }

    private List<String> dialogueLines() {
        return switch (role) {
            case FIELD_RESEARCHER -> List.of(
                    "These ruins are evidence first and salvage second.",
                    "If your PDA authenticates a local record, compare its cross-references before moving on.");
            case SALVAGER -> List.of(
                    "I only take what the site can lose without erasing the story.",
                    "Guarded caches are usually intact for a reason. Clear the room before you start reading labels.");
            case RECOVERY_SPECIALIST -> List.of(
                    "We follow old distress routes and whatever ECHO-9 left behind.",
                    "Some signals still arrive with timestamps that do not agree with the clock.");
            case ICARUS_ENGINEER -> List.of(
                    "The reactor hardware failed after the shutdown chain was overridden, not before.",
                    "Do not mistake damaged containment for permission to recreate the experiment here.");
            case JANUS_MEDIC -> List.of(
                    "Resonance exposure is not conventional radiation sickness.",
                    "If nearby synthetics begin finishing each other's sentences, leave the containment zone together.");
            case ARCHIVIST -> List.of(
                    "The archive is intentionally non-linear. Discovery order is not incident order.",
                    "Corroborated reconstructions matter more than any single testimony, even ORPHEUS records.");
        };
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("MatterOverdriveResearchRole", role.name());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("MatterOverdriveResearchRole")) setRole(Role.from(tag.getString("MatterOverdriveResearchRole")));
    }
}
