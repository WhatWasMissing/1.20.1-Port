package matteroverdrive.block;

import matteroverdrive.blockentity.FusionReactorControllerBlockEntity;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
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

public class FusionReactorControllerBlock extends BaseEntityBlock {
    public FusionReactorControllerBlock(Properties properties) { super(properties); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FusionReactorControllerBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.FUSION_REACTOR_CONTROLLER.get()) return null;
        return (tickerLevel, tickerPos, tickerState, entity) -> FusionReactorControllerBlockEntity.serverTick(tickerLevel, tickerPos, tickerState, (FusionReactorControllerBlockEntity) entity);
    }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof FusionReactorControllerBlockEntity reactor)
            NetworkHooks.openScreen(serverPlayer, reactor, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!oldState.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof FusionReactorControllerBlockEntity reactor) reactor.dropUpgrades();
        super.onRemove(oldState, level, pos, newState, moving);
    }
}
