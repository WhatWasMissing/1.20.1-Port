package matteroverdrive.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Modern equivalent of the 1.7 ForceGlass connected-face behaviour.
 * Adjacent Matter Overdrive industrial glass blocks suppress their shared face.
 */
public class IndustrialGlassBlock extends Block {
    public IndustrialGlassBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        if (adjacentBlockState.getBlock() instanceof IndustrialGlassBlock) {
            return true;
        }
        return super.skipRendering(state, adjacentBlockState, side);
    }
}
