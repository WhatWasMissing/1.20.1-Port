package matteroverdrive.blockentity;

import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;

public class ChargingStationBlockEntity extends BlockEntity {
    public static final int MAX_TRANSFER_PER_TICK = 4_096;

    private final ItemStackHandler battery = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
            return energy != null && energy.canReceive();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public ChargingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHARGING_STATION.get(), pos, state);
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, ChargingStationBlockEntity station) {
        station.chargeBattery();
    }

    private void chargeBattery() {
        if (level == null || level.isClientSide) {
            return;
        }

        ItemStack stack = battery.getStackInSlot(0);
        if (stack.isEmpty()) {
            return;
        }

        IEnergyStorage target = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (target == null || !target.canReceive()) {
            return;
        }

        int remaining = Math.min(
                MAX_TRANSFER_PER_TICK,
                Math.max(0, target.getMaxEnergyStored() - target.getEnergyStored()));
        if (remaining <= 0) {
            return;
        }

        int transferred = 0;
        for (Direction direction : Direction.values()) {
            if (remaining <= 0) {
                break;
            }

            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null) {
                continue;
            }

            IEnergyStorage source = neighbor
                    .getCapability(ForgeCapabilities.ENERGY, direction.getOpposite())
                    .orElse(null);
            if (source == null || !source.canExtract()) {
                continue;
            }

            int available = source.extractEnergy(remaining, true);
            int accepted = target.receiveEnergy(available, true);
            if (accepted <= 0) {
                continue;
            }

            int extracted = source.extractEnergy(accepted, false);
            if (extracted <= 0) {
                continue;
            }

            int received = target.receiveEnergy(extracted, false);
            transferred += received;
            remaining -= received;

            if (received < extracted) {
                break;
            }
        }

        if (transferred > 0) {
            setChanged();
        }
    }

    public boolean hasBattery() {
        return !battery.getStackInSlot(0).isEmpty();
    }

    public ItemStack getBattery() {
        return battery.getStackInSlot(0);
    }

    public boolean insertBattery(ItemStack stack) {
        if (hasBattery() || stack.isEmpty() || !battery.isItemValid(0, stack)) {
            return false;
        }
        ItemStack inserted = stack.copy();
        inserted.setCount(1);
        battery.setStackInSlot(0, inserted);
        return true;
    }

    public ItemStack removeBattery() {
        return battery.extractItem(0, 1, false);
    }

    public void dropBattery() {
        if (level == null || level.isClientSide) {
            return;
        }
        ItemStack stack = removeBattery();
        if (!stack.isEmpty()) {
            Containers.dropItemStack(
                    level,
                    worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.5D,
                    worldPosition.getZ() + 0.5D,
                    stack);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Battery", battery.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Battery")) {
            battery.deserializeNBT(tag.getCompound("Battery"));
        }
    }
}
