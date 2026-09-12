package matteroverdrive.entity;

import matteroverdrive.dialogue.ContactQuestServices;
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

import java.util.Locale;

/** Post-collapse expedition NPC used to make recovered facilities feel inhabited. */
public class FacilityResearcherEntity extends Villager {
    public enum Role {
        FIELD_RESEARCHER("Field Researcher", "researcher.field"),
        SALVAGER("Salvage Specialist", "researcher.salvager"),
        RECOVERY_SPECIALIST("Recovery Specialist", "researcher.recovery"),
        ICARUS_ENGINEER("Reactor Recovery Engineer", "researcher.icarus"),
        JANUS_MEDIC("Anomaly Field Medic", "researcher.janus"),
        ARCHIVIST("Incident Archivist", "researcher.archivist");

        private final String title;
        private final String dialogueId;
        Role(String title, String dialogueId) {
            this.title = title;
            this.dialogueId = dialogueId;
        }
        public String title() { return title; }
        public String dialogueId() { return dialogueId; }

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

    /** Field-team NPCs are witnesses rather than disposable combat bait. */
    @Override
    public boolean canBeSeenAsEnemy() {
        return false;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) ContactQuestServices.useHuman(serverPlayer, role);
            else ModNetwork.openBranchingDialogue(serverPlayer, role.dialogueId(), role.title());
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
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
