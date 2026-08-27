package matteroverdrive.block;

import matteroverdrive.blockentity.FusionReactorIOBlockEntity;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

public class FusionReactorIOBlock extends BaseEntityBlock {
    public FusionReactorIOBlock(Properties properties) { super(properties); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FusionReactorIOBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.FUSION_REACTOR_IO.get()) return null;
        return (tickerLevel, tickerPos, tickerState, entity) -> FusionReactorIOBlockEntity.serverTick(tickerLevel, tickerPos, tickerState, (FusionReactorIOBlockEntity) entity);
    }
}
