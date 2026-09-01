package matteroverdrive.block;

import matteroverdrive.blockentity.AndroidSpawnerBlockEntity;
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

public class AndroidSpawnerBlock extends BaseEntityBlock {
    public AndroidSpawnerBlock(Properties properties) { super(properties); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AndroidSpawnerBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.ANDROID_SPAWNER.get()) return null;
        return (tickLevel, tickPos, tickState, entity) -> AndroidSpawnerBlockEntity.serverTick(tickLevel, tickPos, tickState, (AndroidSpawnerBlockEntity) entity);
    }
}