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
        // Living-entity mass is recorded by the anomaly at the moment damage is dealt.
        // Keep this hook for compatibility with the block ticker, but do not apply a
        // second reflection-based bonus or repeat the previous entity's contribution.
        lastBalancedMass = getMass();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lastBalancedMass = getMass();
    }
}
