package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.compat.AutomationItemHandler;
import matteroverdrive.item.MachineUpgradeInventory;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.item.PatternDriveItem;
import matteroverdrive.menu.PatternStorageMenu;
import matteroverdrive.network.PatternData;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PatternStorageBlockEntity extends BlockEntity implements MenuProvider {
    public static final int ENERGY_SLOT = 0;
    public static final int FIRST_DRIVE_SLOT = 1;
    public static final int DRIVE_COUNT = 6;
    public static final int SLOT_COUNT = 7;
    public static final int UPGRADE_SLOT_COUNT = 4;

    public static final int ENERGY_CAPACITY = 64000;
    public static final int ENERGY_TRANSFER = 128;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == ENERGY_SLOT) {
                return stack.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::canExtract).orElse(false);
            }
            return slot >= FIRST_DRIVE_SLOT && slot < SLOT_COUNT && stack.getItem() instanceof PatternDriveItem;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final IItemHandler automationItems = new AutomationItemHandler(items, slot -> true, slot -> true);
    private final MachineEnergyStorage energyStorage =
            new MachineEnergyStorage(ENERGY_CAPACITY, ENERGY_TRANSFER, ENERGY_TRANSFER, this::setChanged);
    private final MachineUpgradeInventory upgrades = new MachineUpgradeInventory(
            UPGRADE_SLOT_COUNT,
            upgrade -> upgrade == MachineUpgradeItem.Upgrade.POWER
                    || upgrade == MachineUpgradeItem.Upgrade.POWER_STORAGE,
            this::onUpgradesChanged
    );
    private LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> automationItems);
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energyStorage);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> lowWord(energyStorage.getEnergyStored());
                case 1 -> highWord(energyStorage.getEnergyStored());
                case 2 -> lowWord(energyStorage.getMaxEnergyStored());
                case 3 -> highWord(energyStorage.getMaxEnergyStored());
                case 4 -> getPatternCount();
                case 5 -> getIdleEnergyUsePerTick();
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 6; }
    };

    public PatternStorageBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.PATTERN_STORAGE.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PatternStorageBlockEntity storage) {
        storage.chargeFromEnergyItem();
    }

    private void chargeFromEnergyItem() {
        ItemStack stack = items.getStackInSlot(ENERGY_SLOT);
        if (stack.isEmpty() || energyStorage.getEnergyStored() >= energyStorage.getMaxEnergyStored()) return;
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(source -> {
            if (!source.canExtract()) return;
            int request = Math.min(ENERGY_TRANSFER, energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
            int offered = source.extractEnergy(request, true);
            int accepted = energyStorage.receiveEnergy(offered, false);
            if (accepted > 0) source.extractEnergy(accepted, false);
        });
    }

    private void onUpgradesChanged() {
        energyStorage.setCapacity((int) Math.min(Integer.MAX_VALUE,
                Math.round(ENERGY_CAPACITY * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerStorage))));
        setChanged();
    }

    public int getIdleEnergyUsePerTick() { return 0; }
    public boolean isNetworkActive() { return energyStorage.getEnergyStored() > 0; }

    public boolean canAcceptAnalysis(ItemStack analyzed) {
        if (!isNetworkActive() || analyzed.isEmpty()) return false;
        boolean hasFreePatternSlot = false;
        for (int slot = FIRST_DRIVE_SLOT; slot < SLOT_COUNT; slot++) {
            ItemStack drive = items.getStackInSlot(slot);
            if (!(drive.getItem() instanceof PatternDriveItem)) continue;
            if (PatternDriveItem.matches(drive, analyzed)) return PatternDriveItem.getProgressFor(drive, analyzed) < 100;
            if (PatternDriveItem.getPatterns(drive).size() < PatternDriveItem.getCapacity(drive)) hasFreePatternSlot = true;
        }
        return hasFreePatternSlot;
    }

    public int getProgressFor(ItemStack analyzed) {
        int best = 0;
        for (int slot = FIRST_DRIVE_SLOT; slot < SLOT_COUNT; slot++) {
            ItemStack drive = items.getStackInSlot(slot);
            if (drive.getItem() instanceof PatternDriveItem) best = Math.max(best, PatternDriveItem.getProgressFor(drive, analyzed));
        }
        return best;
    }

    public boolean addAnalysis(ItemStack analyzed, int matter, int progressAdded) {
        if (!isNetworkActive() || analyzed.isEmpty() || matter <= 0) return false;
        for (int slot = FIRST_DRIVE_SLOT; slot < SLOT_COUNT; slot++) {
            ItemStack drive = items.getStackInSlot(slot);
            if (drive.getItem() instanceof PatternDriveItem && PatternDriveItem.matches(drive, analyzed)) {
                boolean changed = PatternDriveItem.recordAnalysis(drive, analyzed, matter, progressAdded);
                if (changed) setChanged();
                return changed;
            }
        }
        for (int slot = FIRST_DRIVE_SLOT; slot < SLOT_COUNT; slot++) {
            ItemStack drive = items.getStackInSlot(slot);
            if (drive.getItem() instanceof PatternDriveItem && PatternDriveItem.canRecordAnalysis(drive, analyzed)) {
                boolean changed = PatternDriveItem.recordAnalysis(drive, analyzed, matter, progressAdded);
                if (changed) setChanged();
                return changed;
            }
        }
        return false;
    }

    public List<PatternData> getPatterns() {
        List<PatternData> patterns = new ArrayList<>();
        for (int slot = FIRST_DRIVE_SLOT; slot < SLOT_COUNT; slot++) {
            ItemStack drive = items.getStackInSlot(slot);
            if (drive.getItem() instanceof PatternDriveItem) {
                for (PatternData pattern : PatternDriveItem.getPatterns(drive)) patterns.add(pattern.copy());
            }
        }
        return patterns;
    }

    public int getPatternCount() {
        int count = 0;
        for (int slot = FIRST_DRIVE_SLOT; slot < SLOT_COUNT; slot++) {
            ItemStack drive = items.getStackInSlot(slot);
            if (drive.getItem() instanceof PatternDriveItem) count += PatternDriveItem.getPatterns(drive).size();
        }
        return count;
    }

    public ItemStackHandler getItemHandler() { return items; }
    public MachineEnergyStorage getEnergyStorage() { return energyStorage; }
    public MachineUpgradeInventory getUpgradeInventory() { return upgrades; }
    public ContainerData getContainerData() { return data; }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5, stack.copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            ItemStack stack = upgrades.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5, stack.copy());
                upgrades.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.put("Upgrades", upgrades.serializeNBT());
        tag.putInt("Energy", energyStorage.getEnergyStored());
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) items.deserializeNBT(tag.getCompound("Items"));
        if (tag.contains("Upgrades")) upgrades.deserializeNBT(tag.getCompound("Upgrades"));
        onUpgradesChanged();
        energyStorage.setEnergyStored(tag.getInt("Energy"));
    }

    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemCapability.cast();
        if (cap == ForgeCapabilities.ENERGY) return energyCapability.cast();
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); itemCapability.invalidate(); energyCapability.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); itemCapability = LazyOptional.of(() -> automationItems); energyCapability = LazyOptional.of(() -> energyStorage); }
    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.pattern_storage"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) { return new PatternStorageMenu(containerId, playerInventory, this); }
    private static int lowWord(int value) { return value & 0xFFFF; }
    private static int highWord(int value) { return (value >>> 16) & 0xFFFF; }
}
