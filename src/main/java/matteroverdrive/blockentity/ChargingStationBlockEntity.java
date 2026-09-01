package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.menu.ChargingStationMenu;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class ChargingStationBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_TRANSFER_PER_TICK = 4_096;
    public static final int ENERGY_CAPACITY = 100_000;

    private final MachineEnergyStorage internalEnergy =
            new MachineEnergyStorage(ENERGY_CAPACITY, MAX_TRANSFER_PER_TICK, 0, this::setChanged);
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
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> internalEnergy);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            ItemStack stack = battery.getStackInSlot(0);
            IEnergyStorage target = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
            return switch (index) {
                case 0 -> target == null ? 0 : low(target.getEnergyStored());
                case 1 -> target == null ? 0 : high(target.getEnergyStored());
                case 2 -> target == null ? 0 : low(target.getMaxEnergyStored());
                case 3 -> target == null ? 0 : high(target.getMaxEnergyStored());
                case 4 -> low(internalEnergy.getEnergyStored());
                case 5 -> high(internalEnergy.getEnergyStored());
                case 6 -> low(internalEnergy.getMaxEnergyStored());
                case 7 -> high(internalEnergy.getMaxEnergyStored());
                case 8 -> lastTransferred;
                default -> 0;
            };
        }

        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 9; }
    };

    private int lastTransferred;

    public ChargingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHARGING_STATION.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  ChargingStationBlockEntity station) {
        station.pullAdjacentEnergy();
        station.lastTransferred = station.chargeBattery();
    }

    /**
     * Keeps compatibility with adjacent pull-only FE sources. Reactor IO and Heavy
     * Energy Cables can also push directly into the same receive-only buffer.
     */
    private void pullAdjacentEnergy() {
        if (level == null || level.isClientSide) return;
        int remaining = Math.min(MAX_TRANSFER_PER_TICK,
                internalEnergy.getMaxEnergyStored() - internalEnergy.getEnergyStored());
        for (Direction direction : Direction.values()) {
            if (remaining <= 0) break;
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null) continue;
            IEnergyStorage source = neighbor.getCapability(
                    ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (source == null || !source.canExtract()) continue;

            int available = source.extractEnergy(remaining, true);
            int accepted = internalEnergy.receiveEnergy(available, true);
            if (accepted <= 0) continue;
            int extracted = source.extractEnergy(accepted, false);
            int received = internalEnergy.receiveEnergy(extracted, false);
            if (received < extracted && source.canReceive()) {
                source.receiveEnergy(extracted - received, false);
            }
            remaining -= received;
        }
    }

    private int chargeBattery() {
        if (level == null || level.isClientSide) return 0;
        ItemStack stack = battery.getStackInSlot(0);
        IEnergyStorage target = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (target == null || !target.canReceive()) return 0;

        int offered = Math.min(MAX_TRANSFER_PER_TICK, internalEnergy.getEnergyStored());
        if (offered <= 0) return 0;

        int accepted = target.receiveEnergy(offered, false);
        if (accepted <= 0) return 0;

        // accepted cannot exceed the buffered amount offered, so the internal
        // consumption is exact and is reported to the reactor usage readout.
        return internalEnergy.consumeEnergy(accepted, level.getGameTime());
    }

    public ItemStack getBattery() { return battery.getStackInSlot(0); }
    public boolean hasBattery() { return !getBattery().isEmpty(); }

    public boolean insertBattery(ItemStack stack) {
        if (hasBattery() || stack.isEmpty() || !battery.isItemValid(0, stack)) return false;
        ItemStack inserted = stack.copy();
        inserted.setCount(1);
        battery.setStackInSlot(0, inserted);
        return true;
    }

    public ItemStack removeBattery() {
        return battery.extractItem(0, 1, false);
    }

    public void dropBattery() {
        if (level == null || level.isClientSide) return;
        ItemStack stack = removeBattery();
        if (!stack.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, stack);
        }
    }

    public ItemStackHandler getBatteryInventory() { return battery; }
    public ContainerData getContainerData() { return data; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Battery", battery.serializeNBT());
        tag.putInt("InternalEnergy", internalEnergy.getEnergyStored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Battery")) battery.deserializeNBT(tag.getCompound("Battery"));
        internalEnergy.setEnergyStored(tag.getInt("InternalEnergy"));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyCapability.cast();
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
    }

    @Override public void reviveCaps() {
        super.reviveCaps();
        energyCapability = LazyOptional.of(() -> internalEnergy);
    }

    @Override public Component getDisplayName() {
        return Component.translatable("block.matteroverdrive.charging_station");
    }

    @Nullable
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ChargingStationMenu(id, inventory, this);
    }

    private static int low(int value) { return value & 0xFFFF; }
    private static int high(int value) { return (value >>> 16) & 0xFFFF; }
}
