package matteroverdrive.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BalancedGravitationalAnomalyBlockEntity extends GravitationalAnomalyBlockEntity {
    private static final double MAX_EVENT_HORIZON = 2.0D;

    public BalancedGravitationalAnomalyBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public double getEventHorizon() {
        return Math.min(MAX_EVENT_HORIZON, super.getEventHorizon());
    }

    public void applyPostTickBalance() {
        RemainingBalanceFixes.boostConsumedEntityMass(this);
    }
}
