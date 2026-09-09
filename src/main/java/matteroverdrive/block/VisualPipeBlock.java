package matteroverdrive.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Visual/state shell for the legacy matter/network pipe shape.
 * Routing remains owned by the existing network code; this class only restores
 * the original centre-and-six-arms presentation and collision outline.
 */
public class VisualPipeBlock extends Block {
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;

    private static final VoxelShape CORE = Block.box(5, 5, 5, 11, 11, 11);
    private static final VoxelShape ARM_DOWN = Block.box(5, 0, 5, 11, 5, 11);
    private static final VoxelShape ARM_UP = Block.box(5, 11, 5, 11, 16, 11);
    private static final VoxelShape ARM_NORTH = Block.box(5, 5, 0, 11, 11, 5);
    private static final VoxelShape ARM_SOUTH = Block.box(5, 5, 11, 11, 11, 16);
    private static final VoxelShape ARM_WEST = Block.box(0, 5, 5, 5, 11, 11);
    private static final VoxelShape ARM_EAST = Block.box(11, 5, 5, 16, 11, 11);

    public VisualPipeBlock(Properties properties) {
        super(properties.noOcclusion());
        registerDefaultState(stateDefinition.any()
                .setValue(DOWN, false).setValue(UP, false)
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(WEST, false).setValue(EAST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DOWN, UP, NORTH, SOUTH, WEST, EAST);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return updateConnections(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return state.setValue(property(direction), canVisuallyConnect(level, pos, direction, neighbourState));
    }

    private BlockState updateConnections(BlockState state, LevelAccessor level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbourPos = pos.relative(direction);
            state = state.setValue(property(direction),
                    canVisuallyConnect(level, pos, direction, level.getBlockState(neighbourPos)));
        }
        return state;
    }

    protected boolean canVisuallyConnect(LevelAccessor level, BlockPos pos, Direction direction, BlockState neighbour) {
        BlockPos neighbourPos = pos.relative(direction);
        // Explicitly recognise the hybrid cable so a legacy Matter Transport Pipe grows
        // its arm into a Hybrid Conduit even during placement ticks where the hybrid BE
        // has not yet been observed. The hybrid side performs the reciprocal check.
        return neighbour.is(this) || neighbour.getBlock() instanceof HybridConduitBlock || level.getBlockEntity(neighbourPos) != null;
    }

    private static BooleanProperty property(Direction direction) {
        return switch (direction) {
            case DOWN -> DOWN;
            case UP -> UP;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE;
        if (state.getValue(DOWN)) shape = Shapes.or(shape, ARM_DOWN);
        if (state.getValue(UP)) shape = Shapes.or(shape, ARM_UP);
        if (state.getValue(NORTH)) shape = Shapes.or(shape, ARM_NORTH);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, ARM_SOUTH);
        if (state.getValue(WEST)) shape = Shapes.or(shape, ARM_WEST);
        if (state.getValue(EAST)) shape = Shapes.or(shape, ARM_EAST);
        return shape;
    }
}
