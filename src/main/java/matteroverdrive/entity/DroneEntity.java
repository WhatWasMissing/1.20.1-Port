package matteroverdrive.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class DroneEntity extends Monster implements RangedAttackMob {
    public static final byte MODE_FOLLOW = 0;
    public static final byte MODE_DEFENSIVE = 1;
    public static final byte MODE_PASSIVE = 2;
    public static final byte MODE_AGGRESSIVE = 3;

    private static final double FOLLOW_START_SQR = 9D;
    private static final double FOLLOW_STOP_SQR = 4D;
    private static final double FOLLOW_HEIGHT = 1.35D;
    private static final double FLIGHT_ACCEL = 0.085D;
    private static final double FLIGHT_DAMPING = 0.82D;
    private static final double MAX_FLIGHT_SPEED = 0.48D;
    private static final double COMBAT_STANDOFF = 7.0D;
    private static final double FLEET_SCAN_RADIUS = 48.0D;
    private static final double FLEET_SEPARATION_RADIUS = 2.25D;
    private static final double CATCH_UP_DISTANCE_SQR = 34D * 34D;

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
        if (target != null && (!target.isAlive() || !canAttack(target))) {
            setTarget(null);
            target = null;
        }
        if (target != null) {
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
        steerFlight(desired.add(fleetSeparation()), 0.75D);
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

    /** Push fleet members away from one another without making them physically shove the player. */
    private Vec3 fleetSeparation() {
        if (ownerUuid == null) return Vec3.ZERO;
        Vec3 separation = Vec3.ZERO;
        AABB area = getBoundingBox().inflate(FLEET_SEPARATION_RADIUS);
        for (DroneEntity other : level().getEntitiesOfClass(DroneEntity.class, area,
                drone -> drone != this && ownerUuid.equals(drone.ownerUuid) && drone.isAlive())) {
            Vec3 away = position().subtract(other.position());
            double distanceSqr = away.lengthSqr();
            if (distanceSqr < 0.0001D) {
                double sign = (getUUID().hashCode() & 1) == 0 ? 1.0D : -1.0D;
                away = new Vec3(sign, 0.15D, -sign);
                distanceSqr = away.lengthSqr();
            }
            double strength = Math.max(0.0D, (FLEET_SEPARATION_RADIUS * FLEET_SEPARATION_RADIUS - distanceSqr)
                    / (FLEET_SEPARATION_RADIUS * FLEET_SEPARATION_RADIUS));
            separation = separation.add(away.normalize().scale(1.35D * strength));
        }
        return separation;
    }

    @Nullable
    public LivingEntity getOwner() {
        if (ownerUuid == null || !(level() instanceof ServerLevel serverLevel)) return null;
        return serverLevel.getPlayerByUUID(ownerUuid);
    }

    private boolean isFriendlyDrone(DroneEntity other) {
        if (other == this) return true;
        if (ownerUuid == null) return false;
        if (ownerUuid.equals(other.ownerUuid)) return true;
        LivingEntity owner = getOwner();
        LivingEntity otherOwner = other.getOwner();
        return owner != null && otherOwner != null && owner.isAlliedTo(otherOwner);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof DroneEntity other && isFriendlyDrone(other)) return false;
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
        if (entity instanceof DroneEntity other && isFriendlyDrone(other)) return true;
        LivingEntity owner = getOwner();
        if (owner != null && owner.isAlliedTo(entity)) return true;
        return super.isAlliedTo(entity);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof DroneEntity other && isFriendlyDrone(other)) return false;
        if (ownerUuid != null && attacker != null && ownerUuid.equals(attacker.getUUID())) return false;
        return super.hurt(source, amount);
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
        double damage = 3D;
        if (hasEffect(MobEffects.DAMAGE_BOOST) && getEffect(MobEffects.DAMAGE_BOOST) != null) {
            damage += 1.5D * (getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() + 1);
        }
        arrow.setBaseDamage(damage);
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
            Vec3 target = formationTarget();
            if (drone.distanceToSqr(owner) > CATCH_UP_DISTANCE_SQR && trySafeCatchUp(target)) return;
            Vec3 delta = target.subtract(drone.position()).add(drone.fleetSeparation());
            drone.steerFlight(delta, drone.distanceToSqr(target) > 100D ? 1.35D : 1.0D);
        }

        private Vec3 formationTarget() {
            if (owner == null || drone.ownerUuid == null) return drone.position();
            List<DroneEntity> fleet = owner.level().getEntitiesOfClass(DroneEntity.class,
                    owner.getBoundingBox().inflate(FLEET_SCAN_RADIUS),
                    other -> drone.ownerUuid.equals(other.ownerUuid) && other.isAlive());
            fleet.sort(Comparator.comparing(other -> other.getUUID().toString()));
            int index = Math.max(0, fleet.indexOf(drone));
            int row = index / 4;
            int position = index % 4;
            double[] lateralSlots = {-1.8D, 1.8D, -3.4D, 3.4D};
            double lateral = lateralSlots[position];
            double trailing = 2.6D + row * 2.2D + (position >= 2 ? 1.2D : 0.0D);

            Vec3 look = owner.getLookAngle();
            Vec3 forward = new Vec3(look.x, 0D, look.z);
            if (forward.lengthSqr() < 0.001D) forward = new Vec3(0D, 0D, 1D);
            else forward = forward.normalize();
            Vec3 right = new Vec3(-forward.z, 0D, forward.x);
            double vertical = owner.getBbHeight() + FOLLOW_HEIGHT + (index % 3) * 0.28D;
            return owner.position().add(forward.scale(-trailing)).add(right.scale(lateral)).add(0D, vertical, 0D);
        }

        private boolean trySafeCatchUp(Vec3 desired) {
            if (owner == null) return false;
            Vec3[] offsets = {
                    Vec3.ZERO,
                    new Vec3(1.5D, 0D, 0D), new Vec3(-1.5D, 0D, 0D),
                    new Vec3(0D, 0D, 1.5D), new Vec3(0D, 0D, -1.5D),
                    new Vec3(0D, 1.5D, 0D), new Vec3(0D, -1.0D, 0D)
            };
            for (Vec3 offset : offsets) {
                Vec3 candidate = desired.add(offset);
                if (candidate.distanceToSqr(owner.position()) < 4.0D) continue;
                AABB moved = drone.getBoundingBox().move(candidate.subtract(drone.position()));
                if (!drone.level().noCollision(drone, moved)) continue;
                boolean occupied = !drone.level().getEntitiesOfClass(DroneEntity.class, moved.inflate(0.55D),
                        other -> other != drone && drone.ownerUuid != null && drone.ownerUuid.equals(other.ownerUuid)).isEmpty();
                if (occupied) continue;
                drone.teleportTo(candidate.x, candidate.y, candidate.z);
                drone.setDeltaMovement(Vec3.ZERO);
                return true;
            }
            return false;
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
