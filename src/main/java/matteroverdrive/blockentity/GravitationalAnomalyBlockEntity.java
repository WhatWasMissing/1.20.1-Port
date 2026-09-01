package matteroverdrive.blockentity;

import matteroverdrive.matter.MatterValueRegistry;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GravitationalAnomalyBlockEntity extends BlockEntity {
    public static final double STRENGTH_MULTIPLIER = 0.00001D;
    public static final double GRAVITATIONAL_CONSTANT = 6.67384D;
    private static final double SPEED_OF_LIGHT = 2.99792458D;
    private static final int ITEM_ATTRACTION_INTERVAL = 5;
    private static final double MAX_ITEM_ATTRACTION_RANGE = 32.0D;
    private static final double MAX_ENTITY_EFFECT_RANGE = 32.0D;
    private static final double MAX_ENTITY_ACCELERATION = 0.10D;
    private static final double MAX_ENTITY_SPEED = 0.35D;
    private static final float MAX_HORIZON_DAMAGE = 8.0F;

    private final Map<BlockPos, Suppressor> suppressors = new HashMap<>();
    private long mass;
    private boolean massInitialized;
    private int tickCounter;
    private int affectedEntityCount;
    private int horizonEntityCount;
    private long consumedMatterThisCycle;
    private int consumedEntitiesThisCycle;
    private int lastConsumedMatter;
    private int lastConsumedEntityCount;
    private double nearestEntityDistance = -1.0D;

    public GravitationalAnomalyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRAVITATIONAL_ANOMALY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  GravitationalAnomalyBlockEntity anomaly) {
        if (!anomaly.massInitialized) {
            anomaly.mass = 2_048L + level.random.nextInt(8_193);
            anomaly.massInitialized = true;
            anomaly.setChanged();
        }

        long gameTime = level.getGameTime();
        Iterator<Map.Entry<BlockPos, Suppressor>> iterator = anomaly.suppressors.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().expiresAt() < gameTime) {
                iterator.remove();
            }
        }

        anomaly.tickCounter++;
        if (anomaly.tickCounter % ITEM_ATTRACTION_INTERVAL == 0) {
            anomaly.beginConsumptionCycle();
            anomaly.attractAndConsumeItems(level);
            anomaly.affectLivingEntities(level);
            anomaly.finishConsumptionCycle();
        }
    }

    public void suppress(BlockPos source, int durationTicks, double amount) {
        if (level == null) {
            return;
        }
        double clampedAmount = Math.max(0.0D, Math.min(1.0D, amount));
        suppressors.put(source.immutable(),
                new Suppressor(level.getGameTime() + Math.max(1, durationTicks), clampedAmount));
    }

    public long getMass() {
        return mass;
    }

    public double getRealMassUnsuppressed() {
        return Math.log1p(Math.max(0L, mass) * STRENGTH_MULTIPLIER);
    }

    public double getSuppression() {
        double suppression = 1.0D;
        for (Suppressor suppressor : suppressors.values()) {
            suppression *= suppressor.amount();
        }
        return suppression;
    }

    public int getActiveSuppressorCount() {
        return suppressors.size();
    }

    public int getAffectedEntityCount() {
        return affectedEntityCount;
    }

    public double getNearestEntityDistance() {
        return nearestEntityDistance;
    }

    public int getHorizonEntityCount() {
        return horizonEntityCount;
    }

    public int getLastConsumedMatter() {
        return lastConsumedMatter;
    }

    public int getLastConsumedEntityCount() {
        return lastConsumedEntityCount;
    }

    public double getRealMass() {
        return getRealMassUnsuppressed() * getSuppression();
    }

    public double getMaxRange() {
        return Math.sqrt(getRealMass() * (GRAVITATIONAL_CONSTANT / 0.01D));
    }

    public double getBlockBreakRange() {
        return getMaxRange() / 2.0D;
    }

    /** Matches the original mod's event-horizon calculation. */
    public double getEventHorizon() {
        return Math.max((2.0D * GRAVITATIONAL_CONSTANT * getRealMass())
                / (SPEED_OF_LIGHT * SPEED_OF_LIGHT), 0.5D);
    }

    public double getAcceleration(double distanceSquared) {
        return GRAVITATIONAL_CONSTANT * (getRealMass() / Math.max(distanceSquared, 0.0001D));
    }

    private void attractAndConsumeItems(Level level) {
        double range = Math.min(MAX_ITEM_ATTRACTION_RANGE, getMaxRange());
        if (range < 0.5D) {
            return;
        }

        Vec3 centre = Vec3.atCenterOf(worldPosition);
        double eventHorizonSquared = getEventHorizon() * getEventHorizon();
        AABB bounds = new AABB(worldPosition).inflate(range);
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, bounds, ItemEntity::isAlive)) {
            Vec3 pull = centre.subtract(itemEntity.position());
            double distanceSquared = pull.lengthSqr();
            if (distanceSquared <= eventHorizonSquared) {
                ItemStack stack = itemEntity.getItem();
                if (!stack.isEmpty()) {
                    long itemMatter = Math.max(1, MatterValueRegistry.getMatter(stack));
                    recordConsumption(itemMatter * (long) stack.getCount());
                    horizonEntityCount++;
                    itemEntity.discard();
                }
                continue;
            }

            double acceleration = Math.min(0.15D, getAcceleration(distanceSquared));
            Vec3 motion = itemEntity.getDeltaMovement().scale(0.9D)
                    .add(pull.normalize().scale(acceleration));
            itemEntity.setDeltaMovement(motion);
            itemEntity.hurtMarked = true;
        }
    }

    private void affectLivingEntities(Level level) {
        affectedEntityCount = 0;
        nearestEntityDistance = -1.0D;

        double range = Math.min(MAX_ENTITY_EFFECT_RANGE, getMaxRange());
        if (range < 0.5D) {
            return;
        }

        Vec3 centre = Vec3.atCenterOf(worldPosition);
        double eventHorizon = getEventHorizon();
        AABB bounds = new AABB(worldPosition).inflate(range);
        for (LivingEntity livingEntity : level.getEntitiesOfClass(
                LivingEntity.class, bounds, LivingEntity::isAlive)) {
            if (livingEntity.getItemBySlot(EquipmentSlot.CHEST)
                    .is(ModItems.get("spacetime_equalizer").get())) {
                continue;
            }

            Vec3 pull = centre.subtract(livingEntity.position());
            double distanceSquared = pull.lengthSqr();
            double distance = Math.sqrt(distanceSquared);
            if (nearestEntityDistance < 0.0D || distance < nearestEntityDistance) {
                nearestEntityDistance = distance;
            }

            if (distance <= eventHorizon) {
                horizonEntityCount++;
                consumeLivingEntity(level, livingEntity, distance);
                if (!livingEntity.isAlive()) {
                    continue;
                }
            }

            double acceleration = Math.min(MAX_ENTITY_ACCELERATION,
                    getAcceleration(distanceSquared));
            Vec3 motion = livingEntity.getDeltaMovement().scale(0.96D)
                    .add(pull.normalize().scale(acceleration));
            if (motion.lengthSqr() > MAX_ENTITY_SPEED * MAX_ENTITY_SPEED) {
                motion = motion.normalize().scale(MAX_ENTITY_SPEED);
            }
            livingEntity.setDeltaMovement(motion);
            livingEntity.hurtMarked = true;
            affectedEntityCount++;
        }
    }

    private void beginConsumptionCycle() {
        horizonEntityCount = 0;
        consumedMatterThisCycle = 0L;
        consumedEntitiesThisCycle = 0;
    }

    private void finishConsumptionCycle() {
        if (consumedEntitiesThisCycle <= 0) {
            return;
        }
        lastConsumedMatter = (int) Math.min(Integer.MAX_VALUE, consumedMatterThisCycle);
        lastConsumedEntityCount = consumedEntitiesThisCycle;
        setChanged();
    }

    private void recordConsumption(long absorbedMatter) {
        long clampedMatter = Math.max(1L, absorbedMatter);
        mass = safeAdd(mass, clampedMatter);
        consumedMatterThisCycle = safeAdd(consumedMatterThisCycle, clampedMatter);
        if (consumedEntitiesThisCycle < Integer.MAX_VALUE) {
            consumedEntitiesThisCycle++;
        }
    }

    private void consumeLivingEntity(Level level, LivingEntity livingEntity, double distance) {
        float healthBefore = livingEntity.getHealth();
        if (!livingEntity.hurt(level.damageSources().magic(), getHorizonDamage(distance))) {
            return;
        }
        float healthLost = Math.max(0.0F,
                healthBefore - Math.max(0.0F, livingEntity.getHealth()));
        long absorbed = Math.max(1L, Math.round(healthLost));
        if (!livingEntity.isAlive()) {
            absorbed = Math.max(absorbed, Math.round(livingEntity.getMaxHealth()));
        }
        recordConsumption(Math.min(Long.MAX_VALUE / 64L, absorbed) * 64L);
    }

    private float getHorizonDamage(double distance) {
        double range = Math.max(0.5D, getMaxRange());
        double falloff = Math.max(0.0D, 1.0D - distance / range);
        double damage = Math.max(1.0D, getRealMass() * 4.0D * falloff);
        return (float) Math.min(MAX_HORIZON_DAMAGE, damage);
    }

    private static long safeAdd(long left, long right) {
        if (right > 0L && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("Mass", mass);
        tag.putBoolean("MassInitialized", massInitialized);
        tag.putInt("LastConsumedMatter", lastConsumedMatter);
        tag.putInt("LastConsumedEntityCount", lastConsumedEntityCount);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mass = tag.getLong("Mass");
        massInitialized = tag.getBoolean("MassInitialized") || tag.contains("Mass");
        lastConsumedMatter = Math.max(0, tag.getInt("LastConsumedMatter"));
        lastConsumedEntityCount = Math.max(0, tag.getInt("LastConsumedEntityCount"));
    }

    private record Suppressor(long expiresAt, double amount) {
    }
}
