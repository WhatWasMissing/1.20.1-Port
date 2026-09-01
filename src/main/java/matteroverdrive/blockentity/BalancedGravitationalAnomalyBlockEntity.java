package matteroverdrive.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class BalancedGravitationalAnomalyBlockEntity extends GravitationalAnomalyBlockEntity {
    private static final double MAX_EVENT_HORIZON = 2.0D;
    private long lastBalancedMass = Long.MIN_VALUE;

    public BalancedGravitationalAnomalyBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public double getEventHorizon() {
        return Math.min(MAX_EVENT_HORIZON, super.getEventHorizon());
    }

    public void applyPostTickBalance() {
        long currentMass = getMass();
        if (currentMass == lastBalancedMass) {
            return;
        }
        if (getLastConsumedEntityCount() <= 0 || getLastConsumedMatter() <= 0) {
            lastBalancedMass = currentMass;
            return;
        }

        RemainingBalanceFixes.boostConsumedEntityMass(this);
        lastBalancedMass = getMass();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lastBalancedMass = getMass();
    }
}
