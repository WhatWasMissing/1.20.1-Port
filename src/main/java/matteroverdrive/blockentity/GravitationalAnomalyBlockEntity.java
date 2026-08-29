package matteroverdrive.blockentity;

import matteroverdrive.matter.MatterValueRegistry;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
    private static final int ITEM_ATTRACTION_INTERVAL = 5;
    private static final double MAX_ITEM_ATTRACTION_RANGE = 32.0D;

    private final Map<BlockPos, Suppressor> suppressors = new HashMap<>();
    private long mass;
    private boolean massInitialized;
    private int tickCounter;

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
            anomaly.attractAndConsumeItems(level);
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

    public double getRealMass() {
        return getRealMassUnsuppressed() * getSuppression();
    }

    public double getMaxRange() {
        return Math.sqrt(getRealMass() * (GRAVITATIONAL_CONSTANT / 0.01D));
    }

    public double getBlockBreakRange() {
        return getMaxRange() / 2.0D;
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
        AABB bounds = new AABB(worldPosition).inflate(range);
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, bounds, ItemEntity::isAlive)) {
            Vec3 pull = centre.subtract(itemEntity.position());
            double distanceSquared = pull.lengthSqr();
            if (distanceSquared <= 1.0D) {
                ItemStack stack = itemEntity.getItem();
                long itemMatter = Math.max(1, MatterValueRegistry.getMatter(stack));
                mass = safeAdd(mass, itemMatter * stack.getCount());
                itemEntity.discard();
                setChanged();
                continue;
            }

            double acceleration = Math.min(0.15D, getAcceleration(distanceSquared));
            Vec3 motion = itemEntity.getDeltaMovement().scale(0.9D)
                    .add(pull.normalize().scale(acceleration));
            itemEntity.setDeltaMovement(motion);
            itemEntity.hurtMarked = true;
        }
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
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mass = tag.getLong("Mass");
        massInitialized = tag.getBoolean("MassInitialized") || tag.contains("Mass");
    }

    private record Suppressor(long expiresAt, double amount) {
    }
}
