package matteroverdrive.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

public class DroneEntity extends Monster implements RangedAttackMob {
    public static final byte MODE_FOLLOW = 0;
    public static final byte MODE_DEFENSIVE = 1;
    public static final byte MODE_PASSIVE = 2;
    public static final byte MODE_AGGRESSIVE = 3;

    private static final double FOLLOW_START_SQR = 25D;
    private static final double FOLLOW_STOP_SQR = 9D;
    private static final double FOLLOW_HEIGHT = 1.75D;
    private static final double FLIGHT_ACCEL = 0.085D;
    private static final double FLIGHT_DAMPING = 0.82D;
    private static final double MAX_FLIGHT_SPEED = 0.48D;
    private static final double COMBAT_STANDOFF = 7.0D;

    private byte droneType;
    @Nullable private UUID ownerUuid;
    private byte commandMode = MODE_FOLLOW;
    private int defensivePoll;

    public DroneEntity(EntityType<? extends DroneEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.MOVEMENT_SPEED, .6D)
                .add(Attributes.FOLLOW_RANGE, 16D)
                .add(Attributes.ARMOR, 14D);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1D, 25, 16F));
        goalSelector.addGoal(3, new FollowOwnerGoal(this));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, player -> ownerUuid == null));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                mob -> ownerUuid != null && commandMode == MODE_AGGRESSIVE && mob != this && canAttack(mob)));
    }

    @Override
    public void aiStep() {
        setNoGravity(true);
        super.aiStep();
        if (level().isClientSide) return;

        LivingEntity target = getTarget();
        if (target != null && target.isAlive() && canAttack(target)) {
            combatFlight(target);
        } else if (ownerUuid == null) {
            idleHover();
        }

        if (ownerUuid == null) return;
        if (commandMode == MODE_FOLLOW || commandMode == MODE_PASSIVE) {
            if (getTarget() != null) setTarget(null);
            return;
        }
        if (--defensivePoll > 0) return;
        defensivePoll = 10;
        LivingEntity owner = getOwner();
        if (owner == null) return;
        LivingEntity attacker = owner.getLastHurtByMob();
        if (attacker != null && attacker.isAlive() && canAttack(attacker)) setTarget(attacker);
    }

    private void combatFlight(LivingEntity target) {
        Vec3 delta = target.position().add(0D, target.getBbHeight() * 0.65D + 1.0D, 0D).subtract(position());
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        Vec3 desired;
        if (horizontal < COMBAT_STANDOFF - 1.0D) {
            desired = new Vec3(-delta.x, delta.y * 0.25D, -delta.z);
        } else if (horizontal > COMBAT_STANDOFF + 2.0D) {
            desired = delta;
        } else {
            desired = new Vec3(-delta.z * 0.35D, delta.y, delta.x * 0.35D);
        }
        steerFlight(desired, 0.75D);
        getLookControl().setLookAt(target, 20F, getMaxHeadXRot());
    }

    private void idleHover() {
        Vec3 motion = getDeltaMovement();
        double correction = onGround() ? 0.08D : 0D;
        setDeltaMovement(motion.x * 0.90D, motion.y * 0.78D + correction, motion.z * 0.90D);
    }

    private void steerFlight(Vec3 desired, double speedScale) {
        if (desired.lengthSqr() < 0.0001D) {
            idleHover();
            return;
        }
        Vec3 wanted = desired.normalize().scale(MAX_FLIGHT_SPEED * speedScale);
        Vec3 current = getDeltaMovement().scale(FLIGHT_DAMPING);
        Vec3 next = current.add(wanted.subtract(current).scale(FLIGHT_ACCEL / (1.0D - FLIGHT_DAMPING)));
        double max = MAX_FLIGHT_SPEED * Math.max(0.5D, speedScale);
        if (next.lengthSqr() > max * max) next = next.normalize().scale(max);
        setDeltaMovement(next);
    }

    @Nullable
    public LivingEntity getOwner() {
        if (ownerUuid == null || !(level() instanceof ServerLevel serverLevel)) return null;
        return serverLevel.getPlayerByUUID(ownerUuid);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (ownerUuid != null) {
            if (commandMode == MODE_FOLLOW || commandMode == MODE_PASSIVE) return false;
            if (ownerUuid.equals(target.getUUID())) return false;
            LivingEntity owner = getOwner();
            if (owner != null && owner.isAlliedTo(target)) return false;
            if (target instanceof Player) return false;
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
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (ownerUuid == null) {
            setOwnerUuid(player.getUUID());
            commandMode = MODE_FOLLOW;
            player.displayClientMessage(Component.literal("Drone linked. Mode: FOLLOW"), true);
            return InteractionResult.CONSUME;
        }
        if (!ownerUuid.equals(player.getUUID())) {
            player.displayClientMessage(Component.literal("Drone is linked to another operator"), true);
            return InteractionResult.CONSUME;
        }
        if (player.isShiftKeyDown()) {
            ownerUuid = null;
            commandMode = MODE_FOLLOW;
            setTarget(null);
            getNavigation().stop();
            player.displayClientMessage(Component.literal("Drone link released"), true);
            return InteractionResult.CONSUME;
        }
        commandMode = (byte) ((commandMode + 1) % 4);
        setTarget(null);
        player.displayClientMessage(Component.literal("Drone mode: " + commandModeName()), true);
        return InteractionResult.CONSUME;
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
    public byte getCommandMode() { return commandMode; }
    public String commandModeName() {
        return switch (commandMode) {
            case MODE_DEFENSIVE -> "DEFENSIVE";
            case MODE_PASSIVE -> "PASSIVE";
            case MODE_AGGRESSIVE -> "AGGRESSIVE";
            default -> "FOLLOW";
        };
    }
    public void setOwnerUuid(@Nullable UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
        if (ownerUuid != null) setTarget(null);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("DroneType", droneType);
        tag.putByte("CommandMode", commandMode);
        if (ownerUuid != null) tag.putUUID("OwnerUUID", ownerUuid);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setNoGravity(true);
        droneType = tag.getByte("DroneType");
        ownerUuid = tag.hasUUID("OwnerUUID") ? tag.getUUID("OwnerUUID") : null;
        commandMode = tag.contains("CommandMode") ? (byte) Math.max(MODE_FOLLOW, Math.min(MODE_AGGRESSIVE, tag.getByte("CommandMode"))) : MODE_FOLLOW;
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
            return owner != null && !owner.isSpectator() && drone.distanceToSqr(owner) > FOLLOW_START_SQR;
        }

        @Override
        public boolean canContinueToUse() {
            return owner != null && owner.isAlive() && drone.distanceToSqr(owner) > FOLLOW_STOP_SQR;
        }

        @Override
        public void start() {
            drone.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (owner == null) return;
            drone.getLookControl().setLookAt(owner, 15F, drone.getMaxHeadXRot());
            Vec3 target = owner.position().add(0D, owner.getBbHeight() + FOLLOW_HEIGHT, 0D);
            Vec3 delta = target.subtract(drone.position());
            drone.steerFlight(delta, 1.0D);
        }

        @Override
        public void stop() {
            owner = null;
            drone.getNavigation().stop();
            Vec3 motion = drone.getDeltaMovement();
            drone.setDeltaMovement(motion.x * 0.65D, motion.y * 0.50D, motion.z * 0.65D);
        }
    }
}
