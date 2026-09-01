package matteroverdrive.block;

import matteroverdrive.blockentity.BalancedGravitationalAnomalyBlockEntity;
import matteroverdrive.blockentity.GravitationalAnomalyBlockEntity;
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

public class GravitationalAnomalyBlock extends BaseEntityBlock {
    public GravitationalAnomalyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BalancedGravitationalAnomalyBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.GRAVITATIONAL_ANOMALY.get()) {
            return null;
        }
        return (tickerLevel, tickerPos, tickerState, entity) -> {
            GravitationalAnomalyBlockEntity anomaly = (GravitationalAnomalyBlockEntity) entity;
            GravitationalAnomalyBlockEntity.serverTick(tickerLevel, tickerPos, tickerState, anomaly);
            if (anomaly instanceof BalancedGravitationalAnomalyBlockEntity balanced) {
                balanced.applyPostTickBalance();
            }
        };
    }
}
