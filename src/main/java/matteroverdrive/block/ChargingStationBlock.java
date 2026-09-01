package matteroverdrive.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

public class ChargingStationBlock extends Block {
    private static final int MAX_TRANSFER_PER_USE = 50_000;

    public ChargingStationBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        IEnergyStorage battery = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (battery == null || !battery.canReceive()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("Hold a rechargeable battery or energy item"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        int remaining = Math.min(MAX_TRANSFER_PER_USE,
                Math.max(0, battery.getMaxEnergyStored() - battery.getEnergyStored()));
        int transferred = 0;

        for (Direction direction : Direction.values()) {
            if (remaining <= 0) {
                break;
            }
            BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
            if (neighbor == null) {
                continue;
            }

            IEnergyStorage source = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite())
                    .orElse(null);
            if (source == null || !source.canExtract()) {
                continue;
            }

            int available = source.extractEnergy(remaining, true);
            int accepted = battery.receiveEnergy(available, true);
            if (accepted <= 0) {
                continue;
            }

            int extracted = source.extractEnergy(accepted, false);
            int received = battery.receiveEnergy(extracted, false);
            transferred += received;
            remaining -= received;

            if (received < extracted) {
                // A well-behaved FE item should accept exactly what it simulated. Avoid
                // probing further sources if an item violates that contract.
                break;
            }
        }

        if (transferred > 0) {
            player.displayClientMessage(Component.literal("Charged " + transferred + " FE"), true);
        } else if (battery.getEnergyStored() >= battery.getMaxEnergyStored()) {
            player.displayClientMessage(Component.literal("Battery fully charged"), true);
        } else {
            player.displayClientMessage(Component.literal("Charging Station needs an adjacent FE source"), true);
        }
        return InteractionResult.CONSUME;
    }
}
