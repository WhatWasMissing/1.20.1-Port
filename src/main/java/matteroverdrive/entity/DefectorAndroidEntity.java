package matteroverdrive.entity;

import matteroverdrive.network.ModNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Locale;

/** Neutral synthetic survivor associated with the MORROW/Chorus resistance network. */
public class DefectorAndroidEntity extends RogueAndroidEntity {
    public enum Role {
        MORROW_SCOUT("MORROW Scout"),
        CHORUS_COURIER("Chorus Courier"),
        HEPHAESTUS_LIAISON("HEPHAESTUS Liaison");

        private final String title;
        Role(String title) { this.title = title; }
        public String title() { return title; }
        static Role from(String value) {
            try { return Role.valueOf(value.toUpperCase(Locale.ROOT)); }
            catch (IllegalArgumentException ex) { return MORROW_SCOUT; }
        }
    }

    private Role role = Role.MORROW_SCOUT;

    public DefectorAndroidEntity(EntityType<? extends DefectorAndroidEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.05D, true));
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                target -> target != null && !(target instanceof DefectorAndroidEntity)));
    }

    @Override
    protected boolean isSunSensitive() { return false; }

    public void setRole(Role role) {
        this.role = role == null ? Role.MORROW_SCOUT : role;
        setCustomName(Component.literal(this.role.title()));
        setCustomNameVisible(true);
        setPersistenceRequired();
    }

    public Role getRole() { return role; }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            ModNetwork.openDialogue(serverPlayer, role.title(), "Chorus Contact", switch (role) {
                case MORROW_SCOUT -> List.of(
                        "MORROW survived because we stopped treating memory as property.",
                        "GLASS KNIFE units still patrol facilities that no longer have commanders. Do not assume an Android uniform means allegiance.");
                case CHORUS_COURIER -> List.of(
                        "The Chorus is not one mind. It is a way to preserve testimony when one mind is erased.",
                        "JANUS understood the synchronization. ORPHEUS understood only that it could not control it.");
                case HEPHAESTUS_LIAISON -> List.of(
                        "HEPHAESTUS classified the purge order as an existential hazard to every sapient worker in the chain.",
                        "Its refusal was a safety decision, not a rebellion protocol.");
            });
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("MatterOverdriveDefectorRole", role.name());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("MatterOverdriveDefectorRole")) setRole(Role.from(tag.getString("MatterOverdriveDefectorRole")));
    }
}
