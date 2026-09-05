package matteroverdrive.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

public class DroneEntity extends Monster implements RangedAttackMob {
    private byte droneType;
    @Nullable private UUID ownerUuid;

    public DroneEntity(EntityType<? extends DroneEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.MOVEMENT_SPEED, .6D)
                .add(Attributes.FOLLOW_RANGE, 16D)
                .add(Attributes.ARMOR, 14D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1D, 25, 16F));
        goalSelector.addGoal(3, new FollowOwnerGoal(this));
        goalSelector.addGoal(6, new RandomStrollGoal(this, .9D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                player -> ownerUuid == null));
    }

    @Nullable
    public LivingEntity getOwner() {
        if (ownerUuid == null || !(level() instanceof ServerLevel serverLevel)) return null;
        return serverLevel.getPlayerByUUID(ownerUuid);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (ownerUuid != null) {
            if (ownerUuid.equals(target.getUUID())) return false;
            if (target instanceof Player) return false;
            LivingEntity owner = getOwner();
            if (owner != null && owner.isAlliedTo(target)) return false;
        }
        return super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (ownerUuid != null && ownerUuid.equals(entity.getUUID())) return true;
        if (entity instanceof DroneEntity other && ownerUuid != null && ownerUuid.equals(other.ownerUuid)) return true;
        LivingEntity owner = getOwner();
        if (owner != null && owner.isAlliedTo(entity)) return true;
        return super.isAlliedTo(entity);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (level().isClientSide || !canAttack(target)) return;
        Arrow arrow = new Arrow(level(), this);
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double dy = target.getEyeY() - arrow.getY();
        double h = Math.sqrt(dx * dx + dz * dz);
        arrow.setBaseDamage(3D);
        arrow.shoot(dx, dy + h * .1D, dz, 1.6F, 3F);
        level().addFreshEntity(arrow);
        playSound(net.minecraft.sounds.SoundEvents.BLAZE_SHOOT, .6F, 1.8F);
    }

    public byte getDroneType() { return droneType; }
    public void setDroneType(byte type) { droneType = type; }
    @Nullable public UUID getOwnerUuid() { return ownerUuid; }
    public void setOwnerUuid(@Nullable UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
        if (ownerUuid != null) setTarget(null);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("DroneType", droneType);
        if (ownerUuid != null) tag.putUUID("OwnerUUID", ownerUuid);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        droneType = tag.getByte("DroneType");
        ownerUuid = tag.hasUUID("OwnerUUID") ? tag.getUUID("OwnerUUID") : null;
    }

    private static final class FollowOwnerGoal extends Goal {
        private final DroneEntity drone;
        @Nullable private LivingEntity owner;

        private FollowOwnerGoal(DroneEntity drone) {
            this.drone = drone;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            owner = drone.getOwner();
            return owner != null && !owner.isSpectator() && drone.distanceToSqr(owner) > 25D;
        }

        @Override
        public boolean canContinueToUse() {
            return owner != null && owner.isAlive() && drone.distanceToSqr(owner) > 9D;
        }

        @Override
        public void start() {
            if (owner != null) drone.getNavigation().moveTo(owner, 1.15D);
        }

        @Override
        public void tick() {
            if (owner == null) return;
            drone.getLookControl().setLookAt(owner, 10F, drone.getMaxHeadXRot());
            if (drone.getNavigation().isDone()) drone.getNavigation().moveTo(owner, 1.15D);
        }

        @Override
        public void stop() {
            owner = null;
            drone.getNavigation().stop();
        }
    }
}
