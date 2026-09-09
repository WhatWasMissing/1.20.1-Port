package matteroverdrive.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/** Reusable industrial construction details for facilities and player builds. */
public final class IndustrialDetailBlock extends HorizontalDirectionalBlock {
    public enum Kind { CATWALK, RAILING, CABLE_TRAY, WARNING_LIGHT, DAMAGED_PANEL }

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape CATWALK = Block.box(0, 0, 0, 16, 3, 16);
    private static final VoxelShape RAIL_X = Shapes.or(
            Block.box(0, 0, 7, 16, 3, 9),
            Block.box(0, 3, 7, 2, 16, 9),
            Block.box(14, 3, 7, 16, 16, 9),
            Block.box(0, 8, 7, 16, 10, 9),
            Block.box(0, 14, 7, 16, 16, 9));
    private static final VoxelShape RAIL_Z = Shapes.or(
            Block.box(7, 0, 0, 9, 3, 16),
            Block.box(7, 3, 0, 9, 16, 2),
            Block.box(7, 3, 14, 9, 16, 16),
            Block.box(7, 8, 0, 9, 10, 16),
            Block.box(7, 14, 0, 9, 16, 16));
    private static final VoxelShape TRAY_X = Block.box(0, 12, 4, 16, 16, 12);
    private static final VoxelShape TRAY_Z = Block.box(4, 12, 0, 12, 16, 16);
    private static final VoxelShape PANEL_N = Block.box(2, 2, 14, 14, 14, 16);
    private static final VoxelShape PANEL_S = Block.box(2, 2, 0, 14, 14, 2);
    private static final VoxelShape PANEL_W = Block.box(14, 2, 2, 16, 14, 14);
    private static final VoxelShape PANEL_E = Block.box(0, 2, 2, 2, 14, 14);

    private final Kind kind;

    public IndustrialDetailBlock(Properties properties, Kind kind) {
        super(properties.noOcclusion());
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return switch (kind) {
            case CATWALK -> CATWALK;
            case RAILING -> facing.getAxis() == Direction.Axis.Z ? RAIL_X : RAIL_Z;
            case CABLE_TRAY -> facing.getAxis() == Direction.Axis.Z ? TRAY_X : TRAY_Z;
            case WARNING_LIGHT, DAMAGED_PANEL -> switch (facing) {
                case SOUTH -> PANEL_S;
                case WEST -> PANEL_W;
                case EAST -> PANEL_E;
                default -> PANEL_N;
            };
        };
    }
}
