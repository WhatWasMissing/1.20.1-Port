package matteroverdrive.block;

import matteroverdrive.blockentity.ChargingStationBlockEntity;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class ChargingStationBlock extends BaseEntityBlock {
    public ChargingStationBlock(Properties properties) { super(properties); }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChargingStationBlockEntity(pos, state);
    }

    @Nullable
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.CHARGING_STATION.get()) return null;
        return (tickerLevel, pos, tickerState, entity) ->
                ChargingStationBlockEntity.serverTick(
                        tickerLevel, pos, tickerState, (ChargingStationBlockEntity) entity);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof ChargingStationBlockEntity station) {
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, station, pos);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos,
                         BlockState newState, boolean moving) {
        if (!oldState.is(newState.getBlock())) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof ChargingStationBlockEntity station) station.dropBattery();
        }
        super.onRemove(oldState, level, pos, newState, moving);
    }
}
