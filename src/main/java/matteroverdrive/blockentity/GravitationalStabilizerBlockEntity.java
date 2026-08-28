package matteroverdrive.blockentity;

import matteroverdrive.block.GravitationalStabilizerBlock;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GravitationalStabilizerBlockEntity extends BlockEntity {
    private static final int MAX_DISTANCE = 63;
    private static final int SUPPRESSION_DURATION = 20;
    private static final double SUPPRESSION_AMOUNT = 0.7D;

    private int anomalyDistance = -1;
    private boolean beamBlocked;

    public GravitationalStabilizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRAVITATIONAL_STABILIZER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  GravitationalStabilizerBlockEntity stabilizer) {
        stabilizer.anomalyDistance = -1;
        stabilizer.beamBlocked = false;
        if (!level.hasNeighborSignal(pos)) {
            return;
        }

        Direction facing = state.getValue(GravitationalStabilizerBlock.FACING);
        for (int distance = 1; distance <= MAX_DISTANCE; distance++) {
            BlockPos targetPos = pos.relative(facing, distance);
            if (!level.hasChunkAt(targetPos)) {
                return;
            }

            BlockState targetState = level.getBlockState(targetPos);
            if (level.getBlockEntity(targetPos) instanceof GravitationalAnomalyBlockEntity anomaly) {
                anomaly.suppress(pos, SUPPRESSION_DURATION, SUPPRESSION_AMOUNT);
                stabilizer.anomalyDistance = distance;
                return;
            }
            if (!targetState.isAir() && targetState.canOcclude()) {
                stabilizer.beamBlocked = true;
                return;
            }
        }
    }

    public int getAnomalyDistance() {
        return anomalyDistance;
    }

    public boolean isBeamBlocked() {
        return beamBlocked;
    }
}
