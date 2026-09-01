package matteroverdrive.block;

import matteroverdrive.blockentity.ChargingStationBlockEntity;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class ChargingStationBlock extends BaseEntityBlock {
    public ChargingStationBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChargingStationBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.CHARGING_STATION.get()) {
            return null;
        }
        return (tickerLevel, pos, tickerState, blockEntity) ->
                ChargingStationBlockEntity.serverTick(
                        tickerLevel, pos, tickerState, (ChargingStationBlockEntity) blockEntity);
    }

    @Override
    public InteractionResult use(
            BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ChargingStationBlockEntity station)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (station.hasBattery()) {
            if (held.isEmpty() || player.isShiftKeyDown()) {
                ItemStack retrieved = station.removeBattery();
                if (held.isEmpty()) {
                    player.setItemInHand(hand, retrieved);
                } else if (!player.getInventory().add(retrieved)) {
                    player.drop(retrieved, false);
                }
                player.displayClientMessage(Component.literal("Removed item from Charging Station"), true);
            } else {
                ItemStack charging = station.getBattery();
                IEnergyStorage energy = charging.getCapability(ForgeCapabilities.ENERGY).orElse(null);
                if (energy != null) {
                    player.displayClientMessage(Component.literal(
                            "Charging: " + energy.getEnergyStored() + " / " + energy.getMaxEnergyStored() + " FE"), true);
                }
            }
            return InteractionResult.CONSUME;
        }

        IEnergyStorage energy = held.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (energy == null || !energy.canReceive()) {
            player.displayClientMessage(Component.literal("Hold a rechargeable battery or energy item"), true);
            return InteractionResult.CONSUME;
        }

        if (station.insertBattery(held)) {
            held.shrink(1);
            player.displayClientMessage(Component.literal(
                    "Inserted item - charging up to "
                            + ChargingStationBlockEntity.MAX_TRANSFER_PER_TICK + " FE/t"), true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(
            BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!oldState.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ChargingStationBlockEntity station) {
                station.dropBattery();
            }
        }
        super.onRemove(oldState, level, pos, newState, moving);
    }
}
