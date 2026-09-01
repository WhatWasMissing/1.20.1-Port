package matteroverdrive.block;

import matteroverdrive.blockentity.GravitationalStabilizerBlockEntity;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import matteroverdrive.item.MachineUpgradeItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

import java.util.Locale;

public class GravitationalStabilizerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public GravitationalStabilizerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing;
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            facing = context.getHorizontalDirection();
        } else {
            facing = findAlignedAnomaly(context);
            if (facing == null) {
                facing = context.getHorizontalDirection();
            }
        }
        return defaultBlockState().setValue(FACING, facing);
    }

    private static Direction findAlignedAnomaly(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos origin = context.getClickedPos();
        for (Direction direction : Direction.values()) {
            for (int distance = 1; distance <= 63; distance++) {
                BlockPos candidate = origin.relative(direction, distance);
                if (!level.hasChunkAt(candidate)) {
                    break;
                }
                if (level.getBlockState(candidate).is(
                        matteroverdrive.registry.ModBlocks.get("gravitational_anomaly").get())) {
                    return direction;
                }
            }
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player) {
            player.displayClientMessage(Component.literal(
                    "Stabilizer front points " + state.getValue(FACING).getName().toUpperCase(Locale.ROOT)
                            + " | beam travels this way | insert power upgrades and connect it to the reactor"), true);
        }
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

    private static Direction nextHorizontal(Direction facing) {
        return switch (facing) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            default -> Direction.NORTH;
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof GravitationalStabilizerBlockEntity stabilizer) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() instanceof MachineUpgradeItem && stabilizer.insertUpgrade(held)) {
                held.shrink(1);
                player.displayClientMessage(Component.literal("Inserted stabiliser power upgrade"), true);
                return InteractionResult.CONSUME;
            }
            if (player.isShiftKeyDown()) {
                Direction next = nextHorizontal(state.getValue(FACING));
                level.setBlock(pos, state.setValue(FACING, next), 3);
                player.displayClientMessage(Component.literal(
                        "Stabilizer rotated. Front points "
                                + next.getName().toUpperCase(Locale.ROOT)
                                + " | beam travels this way"), true);
                return InteractionResult.CONSUME;
            }
            Component status;
            if (!stabilizer.isPowered()) {
                status = Component.literal("Stabilizer inactive: waiting for reactor power (" + stabilizer.requiredPowerForDisplay() + " FE/t).");
            } else if (stabilizer.getAnomalyDistance() >= 0) {
                status = Component.literal("Stabilizer locked onto an anomaly "
                        + stabilizer.getAnomalyDistance() + " blocks away.");
            } else if (stabilizer.isBeamBlocked()) {
                status = Component.literal("Stabilizer beam is blocked "
                        + stabilizer.getBeamBlockedDistance() + " blocks away to the "
                        + state.getValue(FACING).getName().toUpperCase(Locale.ROOT) + ".");
            } else {
                status = Component.literal("Stabilizer powered, but no anomaly found within 63 blocks to the "
                        + state.getValue(FACING).getName().toUpperCase(Locale.ROOT) + ".");
            }
            player.displayClientMessage(status, true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
