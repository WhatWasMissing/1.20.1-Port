package matteroverdrive.block;

import matteroverdrive.blockentity.EnergyPipeBlockEntity;
import matteroverdrive.blockentity.HybridConduitBlockEntity;
import matteroverdrive.registry.ModExtraBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Higher-tier conduit: same FE behavior as Heavy Energy Cable and also a Matter transport path. */
public class HybridConduitBlock extends EnergyPipeBlock {
    public HybridConduitBlock(Properties properties) { super(properties); }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new HybridConduitBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModExtraBlockEntities.HYBRID_CONDUIT.get()) return null;
        return (tickerLevel, tickerPos, tickerState, entity) -> EnergyPipeBlockEntity.serverTick(tickerLevel, tickerPos, tickerState, (HybridConduitBlockEntity) entity);
    }
}