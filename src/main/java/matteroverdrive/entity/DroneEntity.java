package matteroverdrive.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import matteroverdrive.registry.ModItems;
import matteroverdrive.item.DroneDeploymentCoreItem;
import matteroverdrive.blockentity.GravitationalStabilizerBlockEntity;
import matteroverdrive.blockentity.FusionReactorControllerBlockEntity;
import matteroverdrive.network.ItemNetworkUtil;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class DroneEntity extends Monster implements RangedAttackMob {
    public static final byte ROLE_COMBAT = 0;
    public static final byte ROLE_REPAIR = 1;
    public static final byte ROLE_LOGISTICS = 2;
    public static final byte ROLE_SURVEY = 3;
    public static final byte ROLE_REACTOR_MAINTENANCE = 4;
    public static final int MAX_DRONE_ENERGY = 10_000;
    public static final byte MODE_FOLLOW = 0;
    public static final byte MODE_DEFENSIVE = 1;
    public static final byte MODE_PASSIVE = 2;
    public static final byte MODE_AGGRESSIVE = 3;
    public static final byte MODE_HOLD = 4;
    public static final byte MODE_PATROL = 5;
    /** Logistics-only route mode: stay at a configured loaded inventory and collect into it. */
    public static final byte MODE_LOGISTICS = 6;

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
    private static final double PATROL_RADIUS = 16.0D;
    private static final double PATROL_RADIUS_SQR = PATROL_RADIUS * PATROL_RADIUS;

    private byte droneType;
    @Nullable private UUID ownerUuid;
    private byte commandMode = MODE_FOLLOW;
    @Nullable private BlockPos patrolAnchor;
    @Nullable private BlockPos logisticsTarget;
    private int defensivePoll;
    private int droneEnergy = MAX_DRONE_ENERGY;
    private int rolePoll;
    private long logisticsCursor;

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
                mob -> ownerUuid != null && (commandMode == MODE_AGGRESSIVE || commandMode == MODE_PATROL)
                        && mob != this && canAttack(mob)));
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
        boolean holding = commandMode == MODE_HOLD && ownerUuid != null;
        boolean routingLogistics = commandMode == MODE_LOGISTICS && droneType == ROLE_LOGISTICS && logisticsTarget != null;
        if (holding) {
            if (getTarget() != null) setTarget(null);
            getNavigation().stop();
            idleHover();
        } else if (target != null) {
            combatFlight(target);
        } else if (commandMode == MODE_PATROL && ownerUuid != null) {
            patrolFlight();
        } else if (routingLogistics) {
            logisticsFlight();
        } else if (ownerUuid == null) {
            idleHover();
        }

        if (ownerUuid == null) return;
        if (droneEnergy < MAX_DRONE_ENERGY && tickCount % 10 == 0) droneEnergy = Math.min(MAX_DRONE_ENERGY, droneEnergy + 5);
        if (--rolePoll <= 0) {
            rolePoll = droneType == ROLE_LOGISTICS ? 10 : 40;
            performRoleWork();
        }
        if (holding || routingLogistics) return;
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

    private void performRoleWork() {
        LivingEntity owner = getOwner();
        boolean operatorNearby = owner != null && distanceToSqr(owner) <= 24D * 24D;
        if (droneType == ROLE_LOGISTICS && commandMode == MODE_LOGISTICS && logisticsTarget != null) {
            performLogisticsDelivery();
            return;
        }
        if (commandMode != MODE_PATROL && !operatorNearby) return;
        if (droneType == ROLE_REPAIR) {
            if (droneEnergy < 80) return;
            boolean repaired = false;
            if (operatorNearby && owner.getHealth() < owner.getMaxHealth()) { owner.heal(2.0F); repaired = true; }
            for (DroneEntity ally : level().getEntitiesOfClass(DroneEntity.class, getBoundingBox().inflate(8D),
                    drone -> drone != this && ownerUuid != null && ownerUuid.equals(drone.ownerUuid) && drone.isAlive())) {
                if (ally.getHealth() < ally.getMaxHealth()) { ally.heal(2.0F); repaired = true; }
            }
            if (repaired) droneEnergy -= 80;
        } else if (droneType == ROLE_LOGISTICS && operatorNearby && owner instanceof Player player) {
            if (droneEnergy < 25) return;
            for (ItemEntity item : level().getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(6D),
                    entity -> entity.isAlive() && !entity.getItem().isEmpty())) {
                var remaining = item.getItem().copy();
                int before = remaining.getCount();
                player.getInventory().add(remaining);
                if (remaining.getCount() >= before) continue;
                if (remaining.isEmpty()) item.discard(); else item.setItem(remaining);
                droneEnergy -= 25;
                break;
            }
        } else if (droneType == ROLE_REACTOR_MAINTENANCE) {
            maintainStabilizer();
        } else if (droneType == ROLE_SURVEY) {
            if (droneEnergy < 100) return;
            boolean marked = false;
            for (Monster hostile : level().getEntitiesOfClass(Monster.class, getBoundingBox().inflate(18D),
                    mob -> mob != this && mob.isAlive() && (!(mob instanceof DroneEntity drone) || !isFriendlyDrone(drone)))) {
                hostile.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0, true, false));
                marked = true;
            }
            if (marked) droneEnergy -= 100;
        }
    }

    /** Bounded local containment support; this never force-loads a chunk. */
    private void maintainStabilizer() {
        if (droneEnergy < 500) return;
        BlockPos min = blockPosition().offset(-4, -4, -4);
        BlockPos max = blockPosition().offset(4, 4, 4);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (!level().hasChunkAt(pos)) continue;
            if (level().getBlockEntity(pos) instanceof FusionReactorControllerBlockEntity reactor) {
                int spent = reactor.applyMaintenancePulse(Math.min(500, droneEnergy));
                if (spent > 0) { droneEnergy -= spent; return; }
            }
            if (!(level().getBlockEntity(pos) instanceof GravitationalStabilizerBlockEntity stabilizer)) continue;
            var storage = stabilizer.getCapability(ForgeCapabilities.ENERGY).orElse(null);
            if (storage == null || !storage.canReceive()) continue;
            int accepted = storage.receiveEnergy(Math.min(500, droneEnergy), false);
            if (accepted > 0) { droneEnergy -= accepted; return; }
        }
    }

    /** Flies to a player-configured inventory without ever loading the target chunk. */
    private void logisticsFlight() {
        if (logisticsTarget == null || !level().hasChunkAt(logisticsTarget)) { idleHover(); return; }
        Vec3 destination = Vec3.atCenterOf(logisticsTarget).add(0D, 1.25D, 0D);
        Vec3 delta = destination.subtract(position()).add(fleetSeparation());
        if (delta.lengthSqr() > 1.5D) steerFlight(delta, 0.85D); else idleHover();
    }

    /** Bounded item-to-inventory transfer used only by an explicitly routed Logistics drone. */
    private void performLogisticsDelivery() {
        if (logisticsTarget == null || !level().hasChunkAt(logisticsTarget) || droneEnergy < 25
                || distanceToSqr(Vec3.atCenterOf(logisticsTarget)) > 10D * 10D) return;
        BlockEntity target = level().getBlockEntity(logisticsTarget);
        if (target == null) return;
        IItemHandler inventory = target.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (inventory == null) return;
        int networkMoved = ItemNetworkUtil.pullAnyItemToDestination(level(), logisticsTarget, logisticsTarget,
                inventory, 16, logisticsCursor++, 0);
        if (networkMoved > 0) { droneEnergy -= 25; return; }
        for (ItemEntity item : level().getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(8D),
                entity -> entity.isAlive() && !entity.getItem().isEmpty())) {
            ItemStack remaining = item.getItem().copy();
            int before = remaining.getCount();
            for (int slot = 0; slot < inventory.getSlots() && !remaining.isEmpty(); slot++)
                remaining = inventory.insertItem(slot, remaining, false);
            if (remaining.getCount() >= before) continue;
            if (remaining.isEmpty()) item.discard(); else item.setItem(remaining);
            droneEnergy -= 25;
            return;
        }
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

    /** Orbits a persistent guard point while no valid patrol target is present. */
    private void patrolFlight() {
        if (patrolAnchor == null) patrolAnchor = blockPosition();
        int phaseSeed = getUUID().hashCode() & 1023;
        double phase = (tickCount + phaseSeed) * 0.025D;
        double orbitRadius = 4.0D + ((phaseSeed >>> 3) & 3) * 0.65D;
        Vec3 target = Vec3.atCenterOf(patrolAnchor).add(
                Math.cos(phase) * orbitRadius,
                2.0D + Math.sin(phase * 0.7D) * 0.8D,
                Math.sin(phase) * orbitRadius);
        Vec3 delta = target.subtract(position()).add(fleetSeparation());
        if (delta.lengthSqr() > 1.0D) steerFlight(delta, 0.75D); else idleHover();
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
            if (commandMode == MODE_FOLLOW || commandMode == MODE_PASSIVE || commandMode == MODE_HOLD || commandMode == MODE_LOGISTICS) return false;
            if (ownerUuid.equals(target.getUUID())) return false;
            LivingEntity owner = getOwner();
            if (owner != null && owner.isAlliedTo(target)) return false;
            if (target instanceof Player) return false;
            if (commandMode == MODE_PATROL && patrolAnchor != null
                    && target.distanceToSqr(Vec3.atCenterOf(patrolAnchor)) > PATROL_RADIUS_SQR) return false;
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
            setCommandMode(MODE_FOLLOW);
            player.displayClientMessage(Component.literal("Drone linked. Mode: FOLLOW"), true);
            return InteractionResult.CONSUME;
        }
        if (!ownerUuid.equals(player.getUUID())) {
            player.displayClientMessage(Component.literal("Drone is linked to another operator"), true);
            return InteractionResult.CONSUME;
        }
        if (player.getItemInHand(hand).is(ModItems.DRONE_DEPLOYMENT_CORE.get())) {
            ItemStack core = player.getItemInHand(hand);
            if (!(player instanceof ServerPlayer server) || !DroneDeploymentCoreItem.hasResearchClearance(server, DroneDeploymentCoreItem.role(core))) {
                player.displayClientMessage(DroneDeploymentCoreItem.clearanceMessage(DroneDeploymentCoreItem.role(core)), true);
                return InteractionResult.FAIL;
            }
            setDroneType(DroneDeploymentCoreItem.role(core));
            BlockPos target = DroneDeploymentCoreItem.logisticsTarget(core, level());
            setLogisticsTarget(target);
            if (droneType == ROLE_LOGISTICS && target != null) setCommandMode(MODE_LOGISTICS);
            player.displayClientMessage(Component.literal("Drone reprogrammed: " + roleName()), true);
            return InteractionResult.CONSUME;
        }
        if (player.isShiftKeyDown()) {
            ownerUuid = null;
            setCommandMode(MODE_FOLLOW);
            player.displayClientMessage(Component.literal("Drone link released"), true);
            return InteractionResult.CONSUME;
        }
        setCommandMode(nextCommandMode(commandMode));
        player.displayClientMessage(Component.literal("Drone mode: " + commandModeName()), true);
        return InteractionResult.CONSUME;
    }

    private static byte nextCommandMode(byte current) {
        return switch (current) {
            case MODE_FOLLOW -> MODE_HOLD;
            case MODE_HOLD -> MODE_DEFENSIVE;
            case MODE_DEFENSIVE -> MODE_PASSIVE;
            case MODE_PASSIVE -> MODE_AGGRESSIVE;
            case MODE_AGGRESSIVE -> MODE_PATROL;
            case MODE_PATROL -> MODE_LOGISTICS;
            default -> MODE_FOLLOW;
        };
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (level().isClientSide || !canAttack(target)) return;
        if (ownerUuid != null && droneType != ROLE_COMBAT) return;
        if (ownerUuid != null && droneEnergy < 60) return;
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
        if (ownerUuid != null) droneEnergy -= 60;
        playSound(net.minecraft.sounds.SoundEvents.BLAZE_SHOOT, .6F, 1.8F);
    }

    public byte getDroneType() { return droneType; }
    public void setDroneType(byte type) {
        droneType = (byte)Math.max(ROLE_COMBAT, Math.min(ROLE_REACTOR_MAINTENANCE, type));
        if (droneType != ROLE_LOGISTICS && commandMode == MODE_LOGISTICS) commandMode = MODE_FOLLOW;
        applyRoleAttributes();
    }
    public int getDroneEnergy() { return droneEnergy; }
    /**
     * Recharges this drone from a powered machine. The logical server owns the
     * energy mutation; callers receive the exact amount accepted for FE
     * accounting and diagnostics.
     */
    public int receiveDroneEnergy(int offered) {
        if (offered <= 0 || level().isClientSide) return 0;
        int accepted = Math.min(offered, MAX_DRONE_ENERGY - droneEnergy);
        if (accepted > 0) {
            droneEnergy += accepted;
        }
        return accepted;
    }
    public String roleName() {
        return switch (droneType) {
            case ROLE_REPAIR -> "REPAIR";
            case ROLE_LOGISTICS -> "LOGISTICS";
            case ROLE_SURVEY -> "SURVEY";
            case ROLE_REACTOR_MAINTENANCE -> "REACTOR";
            default -> "COMBAT";
        };
    }
    private void applyRoleAttributes() {
        double health = switch (droneType) {
            case ROLE_REPAIR -> 24D;
            case ROLE_LOGISTICS -> 18D;
            case ROLE_SURVEY -> 16D;
            case ROLE_REACTOR_MAINTENANCE -> 22D;
            default -> 28D;
        };
        var maxHealth = getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) maxHealth.setBaseValue(health);
        if (getHealth() > health) setHealth((float)health);
    }
    @Nullable public UUID getOwnerUuid() { return ownerUuid; }
    public byte getCommandMode() { return commandMode; }
    public void setCommandMode(byte mode) {
        byte next = clampCommandMode(mode);
        if (next == MODE_LOGISTICS && (droneType != ROLE_LOGISTICS || logisticsTarget == null)) next = MODE_FOLLOW;
        if (next == MODE_PATROL && commandMode != MODE_PATROL) patrolAnchor = blockPosition();
        commandMode = next;
        setTarget(null);
        getNavigation().stop();
        if (commandMode == MODE_HOLD) setDeltaMovement(Vec3.ZERO);
    }
    public String commandModeName() {
        return switch (commandMode) {
            case MODE_DEFENSIVE -> "DEFENSIVE";
            case MODE_PASSIVE -> "PASSIVE";
            case MODE_AGGRESSIVE -> "AGGRESSIVE";
            case MODE_HOLD -> "HOLD";
            case MODE_PATROL -> "PATROL";
            case MODE_LOGISTICS -> "LOGISTICS";
            default -> "FOLLOW";
        };
    }
    public void setOwnerUuid(@Nullable UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
        if (ownerUuid != null) setTarget(null);
    }
    @Nullable public BlockPos getPatrolAnchor() { return patrolAnchor; }
    @Nullable public BlockPos getLogisticsTarget() { return logisticsTarget; }
    public void setLogisticsTarget(@Nullable BlockPos target) {
        logisticsTarget = target == null ? null : target.immutable();
        if (logisticsTarget == null && commandMode == MODE_LOGISTICS) commandMode = MODE_FOLLOW;
    }

    /** Returns a loaded linked drone to a clear formation position beside its operator. */
    public boolean recallTo(ServerPlayer operator) {
        if (level().isClientSide || ownerUuid == null || !ownerUuid.equals(operator.getUUID())
                || level() != operator.level()) return false;

        Vec3 look = operator.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0D, look.z);
        if (forward.lengthSqr() < 0.001D) forward = new Vec3(0D, 0D, 1D);
        else forward = forward.normalize();
        Vec3 right = new Vec3(-forward.z, 0D, forward.x);
        Vec3 base = operator.position().add(0D, operator.getBbHeight() + FOLLOW_HEIGHT, 0D);
        Vec3[] offsets = {
                forward.scale(-3D).add(right.scale(-2D)), forward.scale(-3D).add(right.scale(2D)),
                forward.scale(-5D).add(right.scale(-2D)), forward.scale(-5D).add(right.scale(2D)),
                forward.scale(-4D).add(0D, 1.5D, 0D), forward.scale(-4D).add(0D, -0.8D, 0D)
        };
        for (Vec3 offset : offsets) {
            Vec3 candidate = base.add(offset);
            AABB moved = getBoundingBox().move(candidate.subtract(position()));
            if (!level().noCollision(this, moved)) continue;
            boolean occupied = !level().getEntitiesOfClass(DroneEntity.class, moved.inflate(0.45D),
                    other -> other != this && ownerUuid.equals(other.ownerUuid) && other.isAlive()).isEmpty();
            if (occupied) continue;
            teleportTo(candidate.x, candidate.y, candidate.z);
            setDeltaMovement(Vec3.ZERO);
            setCommandMode(MODE_FOLLOW);
            return true;
        }
        return false;
    }

    public static byte clampCommandMode(byte mode) {
        return (byte) Math.max(MODE_FOLLOW, Math.min(MODE_LOGISTICS, mode));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("DroneType", droneType);
        tag.putByte("CommandMode", commandMode);
        tag.putInt("DroneEnergy", droneEnergy);
        if (patrolAnchor != null) tag.putLong("PatrolAnchor", patrolAnchor.asLong());
        if (logisticsTarget != null) tag.putLong("LogisticsTarget", logisticsTarget.asLong());
        tag.putLong("LogisticsCursor", logisticsCursor);
        if (ownerUuid != null) tag.putUUID("OwnerUUID", ownerUuid);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setNoGravity(true);
        setDroneType(tag.getByte("DroneType"));
        droneEnergy = tag.contains("DroneEnergy") ? Math.max(0, Math.min(MAX_DRONE_ENERGY, tag.getInt("DroneEnergy"))) : MAX_DRONE_ENERGY;
        ownerUuid = tag.hasUUID("OwnerUUID") ? tag.getUUID("OwnerUUID") : null;
        commandMode = tag.contains("CommandMode") ? clampCommandMode(tag.getByte("CommandMode")) : MODE_FOLLOW;
        patrolAnchor = tag.contains("PatrolAnchor") ? BlockPos.of(tag.getLong("PatrolAnchor"))
                : commandMode == MODE_PATROL ? blockPosition() : null;
        logisticsTarget = tag.contains("LogisticsTarget") ? BlockPos.of(tag.getLong("LogisticsTarget")) : null;
        logisticsCursor = Math.max(0L, tag.getLong("LogisticsCursor"));
        if (commandMode == MODE_LOGISTICS && (droneType != ROLE_LOGISTICS || logisticsTarget == null)) commandMode = MODE_FOLLOW;
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
            if (drone.commandMode == MODE_HOLD || drone.commandMode == MODE_PATROL || drone.commandMode == MODE_LOGISTICS) return false;
            owner = drone.getOwner();
            return owner != null && !owner.isSpectator() && drone.distanceToSqr(owner) > FOLLOW_START_SQR;
        }

        @Override
        public boolean canContinueToUse() {
            return drone.commandMode != MODE_HOLD && drone.commandMode != MODE_PATROL && drone.commandMode != MODE_LOGISTICS && owner != null && owner.isAlive()
                    && drone.distanceToSqr(owner) > FOLLOW_STOP_SQR;
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
