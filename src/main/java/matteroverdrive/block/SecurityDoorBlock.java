package matteroverdrive.block;

import matteroverdrive.world.FacilityRestorationSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/** Powered industrial shutter. Generated facility shutters obey persistent restoration access. */
public final class SecurityDoorBlock extends HorizontalDirectionalBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    private static final VoxelShape CLOSED_X = Block.box(0, 0, 6, 16, 16, 10);
    private static final VoxelShape CLOSED_Z = Block.box(6, 0, 0, 10, 16, 16);
    private static final VoxelShape OPEN_X = Block.box(0, 0, 6, 3, 16, 10);
    private static final VoxelShape OPEN_Z = Block.box(6, 0, 0, 10, 16, 3);

    public SecurityDoorBlock(Properties properties) {
        super(properties.noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(OPEN, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean xPlane = state.getValue(FACING).getAxis() == Direction.Axis.Z;
        if (state.getValue(OPEN)) return xPlane ? OPEN_X : OPEN_Z;
        return xPlane ? CLOSED_X : CLOSED_Z;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (level instanceof ServerLevel server && !FacilityRestorationSavedData.isAccessUnlocked(server, pos)) {
            closeColumn(level, pos);
            player.displayClientMessage(Component.literal("ACCESS DENIED - restore emergency power and control hardware first")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }
        setColumnOpen(level, pos, !state.getValue(OPEN));
        return InteractionResult.CONSUME;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor,
                                BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide) return;
        if (level instanceof ServerLevel server && !FacilityRestorationSavedData.isAccessUnlocked(server, pos)) {
            if (state.getValue(OPEN)) closeColumn(level, pos);
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(OPEN)) setColumnOpen(level, pos, powered);
    }

    private void closeColumn(Level level, BlockPos pos) {
        setColumnOpen(level, pos, false);
    }

    private void setColumnOpen(Level level, BlockPos pos, boolean open) {
        for (int dy = -3; dy <= 3; dy++) {
            BlockPos part = pos.offset(0, dy, 0);
            BlockState partState = level.getBlockState(part);
            if (partState.getBlock() != this || partState.getValue(OPEN) == open) continue;
            level.setBlock(part, partState.setValue(OPEN, open), 3);
        }
    }
}
