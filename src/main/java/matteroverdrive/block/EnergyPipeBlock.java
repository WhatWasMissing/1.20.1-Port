package matteroverdrive.block;

import matteroverdrive.blockentity.EnergyPipeBlockEntity;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class EnergyPipeBlock extends BaseEntityBlock {
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

    public EnergyPipeBlock(Properties properties) {
        super(properties.noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(DOWN, false).setValue(UP, false).setValue(NORTH, false).setValue(SOUTH, false).setValue(WEST, false).setValue(EAST, false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(DOWN, UP, NORTH, SOUTH, WEST, EAST); }
    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        for (Direction direction : Direction.values()) state = state.setValue(property(direction), canVisuallyConnect(context.getLevel(), context.getClickedPos(), direction, context.getLevel().getBlockState(context.getClickedPos().relative(direction))));
        return state;
    }
    @Override public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) { return state.setValue(property(direction), canVisuallyConnect(level, pos, direction, neighbourState)); }
    protected boolean canVisuallyConnect(LevelAccessor level, BlockPos pos, Direction direction, BlockState neighbour) { return neighbour.is(this) || level.getBlockEntity(pos.relative(direction)) != null; }
    private static BooleanProperty property(Direction direction) { return switch (direction) { case DOWN -> DOWN; case UP -> UP; case NORTH -> NORTH; case SOUTH -> SOUTH; case WEST -> WEST; case EAST -> EAST; }; }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE; if (state.getValue(DOWN)) shape = Shapes.or(shape, ARM_DOWN); if (state.getValue(UP)) shape = Shapes.or(shape, ARM_UP); if (state.getValue(NORTH)) shape = Shapes.or(shape, ARM_NORTH); if (state.getValue(SOUTH)) shape = Shapes.or(shape, ARM_SOUTH); if (state.getValue(WEST)) shape = Shapes.or(shape, ARM_WEST); if (state.getValue(EAST)) shape = Shapes.or(shape, ARM_EAST); return shape;
    }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new EnergyPipeBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.ENERGY_PIPE.get()) return null;
        return (tickerLevel, tickerPos, tickerState, entity) -> EnergyPipeBlockEntity.serverTick(tickerLevel, tickerPos, tickerState, (EnergyPipeBlockEntity) entity);
    }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof EnergyPipeBlockEntity pipe) NetworkHooks.openScreen(serverPlayer, pipe, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
