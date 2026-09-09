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
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final int MAX_COLUMN_HEIGHT = 16;
    private static final VoxelShape CLOSED_X = Block.box(0, 0, 6, 16, 16, 10);
    private static final VoxelShape CLOSED_Z = Block.box(6, 0, 0, 10, 16, 16);
    private static final VoxelShape OPEN_X = Block.box(0, 0, 6, 3, 16, 10);
    private static final VoxelShape OPEN_Z = Block.box(6, 0, 0, 10, 16, 3);

    public SecurityDoorBlock(Properties properties) {
        super(properties.noOcclusion());
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(POWERED, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean powered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(OPEN, powered)
                .setValue(POWERED, powered);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, POWERED);
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
            syncColumn(level, pos, false, columnPowered(level, pos));
            player.displayClientMessage(Component.literal("ACCESS DENIED - restore emergency power and control hardware first")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }

        boolean powered = columnPowered(level, pos);
        if (powered) syncColumn(level, pos, true, true);
        else syncColumn(level, pos, !state.getValue(OPEN), false);
        return InteractionResult.CONSUME;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor,
                                BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide) return;

        boolean powered = columnPowered(level, pos);
        boolean wasPowered = columnStoredPowered(level, pos);
        if (level instanceof ServerLevel server && !FacilityRestorationSavedData.isAccessUnlocked(server, pos)) {
            if (state.getValue(OPEN) || powered != wasPowered) syncColumn(level, pos, false, powered);
            return;
        }

        // Only a redstone state transition controls OPEN. This preserves a manual OPEN state
        // when unrelated neighbours update while still ensuring every segment follows power
        // applied to any segment of the generated three-block shutter.
        if (powered != wasPowered) syncColumn(level, pos, powered, powered);
    }

    private boolean columnPowered(Level level, BlockPos pos) {
        BlockPos base = columnBase(level, pos);
        for (int dy = 0; dy < MAX_COLUMN_HEIGHT; dy++) {
            BlockPos part = base.above(dy);
            if (level.getBlockState(part).getBlock() != this) break;
            if (level.hasNeighborSignal(part)) return true;
        }
        return false;
    }

    private boolean columnStoredPowered(Level level, BlockPos pos) {
        BlockPos base = columnBase(level, pos);
        for (int dy = 0; dy < MAX_COLUMN_HEIGHT; dy++) {
            BlockState partState = level.getBlockState(base.above(dy));
            if (partState.getBlock() != this) break;
            if (partState.getValue(POWERED)) return true;
        }
        return false;
    }

    private BlockPos columnBase(Level level, BlockPos pos) {
        BlockPos base = pos;
        for (int i = 1; i < MAX_COLUMN_HEIGHT; i++) {
            BlockPos below = base.below();
            if (level.getBlockState(below).getBlock() != this) break;
            base = below;
        }
        return base;
    }

    private void syncColumn(Level level, BlockPos pos, boolean open, boolean powered) {
        BlockPos base = columnBase(level, pos);
        for (int dy = 0; dy < MAX_COLUMN_HEIGHT; dy++) {
            BlockPos part = base.above(dy);
            BlockState partState = level.getBlockState(part);
            if (partState.getBlock() != this) break;
            BlockState next = partState.setValue(OPEN, open).setValue(POWERED, powered);
            if (next == partState) continue;
            // Client update only: avoid recursive neighbour callbacks fighting while the
            // vertical column is being synchronized segment-by-segment.
            level.setBlock(part, next, Block.UPDATE_CLIENTS);
        }
    }
}
