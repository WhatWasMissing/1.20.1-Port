package matteroverdrive.block;

import matteroverdrive.blockentity.EnergyPipeBlockEntity;
import matteroverdrive.blockentity.HybridConduitBlockEntity;
import matteroverdrive.registry.ModExtraBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Higher-tier conduit: same FE behavior as Heavy Energy Cable and also a Matter transport path. */
public class HybridConduitBlock extends EnergyPipeBlock {
    public HybridConduitBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canVisuallyConnect(LevelAccessor level, BlockPos pos, Direction direction, BlockState neighbour) {
        // Hybrid conduits bridge both pipe families. Recognising VisualPipeBlock by class
        // keeps the render state symmetric with matter/network pipes instead of relying
        // on a block entity appearing a tick later, which previously left visible gaps.
        return super.canVisuallyConnect(level, pos, direction, neighbour)
                || neighbour.getBlock() instanceof VisualPipeBlock;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HybridConduitBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModExtraBlockEntities.HYBRID_CONDUIT.get()) return null;
        return (tickerLevel, tickerPos, tickerState, entity) ->
                EnergyPipeBlockEntity.serverTick(tickerLevel, tickerPos, tickerState, (HybridConduitBlockEntity) entity);
    }
}
