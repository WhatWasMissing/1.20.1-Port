package matteroverdrive.block;

import matteroverdrive.blockentity.GravitationalStabilizerBlockEntity;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class GravitationalStabilizerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public GravitationalStabilizerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GravitationalStabilizerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.GRAVITATIONAL_STABILIZER.get()) {
            return null;
        }
        return (tickerLevel, tickerPos, tickerState, entity) ->
                GravitationalStabilizerBlockEntity.serverTick(
                        tickerLevel, tickerPos, tickerState, (GravitationalStabilizerBlockEntity) entity);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof GravitationalStabilizerBlockEntity stabilizer) {
            Component status;
            if (!level.hasNeighborSignal(pos)) {
                status = Component.literal("Stabilizer inactive: supply a redstone signal.");
            } else if (stabilizer.getAnomalyDistance() >= 0) {
                status = Component.literal("Stabilizer locked onto an anomaly "
                        + stabilizer.getAnomalyDistance() + " blocks away.");
            } else if (stabilizer.isBeamBlocked()) {
                status = Component.literal("Stabilizer beam is blocked by an opaque block.");
            } else {
                status = Component.literal("No anomaly found within 63 blocks in the facing direction.");
            }
            player.displayClientMessage(status, true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
