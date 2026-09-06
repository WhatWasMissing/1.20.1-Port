package matteroverdrive.block;

import matteroverdrive.blockentity.PylonBlockEntity;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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

public class PylonBlock extends BaseEntityBlock {
    public PylonBlock(Properties properties) { super(properties.noOcclusion()); }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PylonBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.PYLON.get()) return null;
        return (tickerLevel, pos, tickerState, blockEntity) ->
                PylonBlockEntity.serverTick(tickerLevel, pos, tickerState, (PylonBlockEntity) blockEntity);
    }

    @Override
    public InteractionResult use(
            BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof PylonBlockEntity pylon)) return InteractionResult.PASS;

        if (pylon.isFormed()) {
            PylonBlockEntity root = pylon.getRoot();
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, root, root.getBlockPos());
            }
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown()) {
            if (pylon.tryFormStructure()) {
                PylonBlockEntity root = pylon.getRoot();
                player.displayClientMessage(Component.literal(
                        "Dimensional Pylon formed | 2x3x2 structure | main "
                                + root.getBlockPos().toShortString()), true);
            } else {
                pylon.nextChannel();
                player.displayClientMessage(Component.literal(
                        "Compatibility relay channel " + pylon.getChannel()
                                + " | no valid 2x3x2 Dimensional Pylon detected"), true);
            }
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(Component.literal(
                "Unformed Pylon | relay channel " + pylon.getChannel()
                        + " | shift-right-click a complete 2x3x2 Pylon structure to form"), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(
            BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!oldState.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof PylonBlockEntity pylon
                && pylon.isFormed()) {
            pylon.invalidateStructure();
        }
        super.onRemove(oldState, level, pos, newState, moving);
    }
}
