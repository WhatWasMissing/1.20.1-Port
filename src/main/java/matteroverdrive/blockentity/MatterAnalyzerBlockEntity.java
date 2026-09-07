package matteroverdrive.blockentity;

import matteroverdrive.block.MatterAnalyzerBlock;
import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.item.MatterDustItem;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.item.MachineUpgradeInventory;
import matteroverdrive.item.PatternDriveItem;
import matteroverdrive.machine.MachineRedstoneMode;
import matteroverdrive.matter.MatterValueRegistry;
import matteroverdrive.menu.MatterAnalyzerMenu;
import matteroverdrive.network.MatterNetworkUtil;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.registry.ModItems;
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

public class MatterAnalyzerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int ENERGY_SLOT = 1;
    public static final int DRIVE_SLOT = 2;
    public static final int SLOT_COUNT = 3;
    public static final int UPGRADE_SLOT_COUNT = 4;
    public static final int PROGRESS_AMOUNT_PER_ITEM = 20;
    public static final int ANALYZE_SPEED = 800;
    public static final int ENERGY_DRAIN_PER_ITEM = 64000;
    public static final int ENERGY_STORAGE = 512000;
    public static final int ENERGY_ITEM_TRANSFER_PER_TICK = 16000;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case INPUT_SLOT -> MatterValueRegistry.containsMatter(level, stack)
                        && (!(stack.getItem() instanceof MatterDustItem dust) || dust.isRefined());
                case ENERGY_SLOT -> stack.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::canExtract).orElse(false);
                case DRIVE_SLOT -> stack.is(ModItems.get("pattern_drive").get()) || stack.is(ModItems.get("creative_pattern_drive").get());
                default -> false;
            };
        }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };

    private final MachineEnergyStorage energyStorage = new MachineEnergyStorage(ENERGY_STORAGE, ENERGY_STORAGE, ENERGY_STORAGE, this::setChanged);
    private final MachineUpgradeInventory upgrades = new MachineUpgradeInventory(
            UPGRADE_SLOT_COUNT,
            u -> u == MachineUpgradeItem.Upgrade.SPEED || u == MachineUpgradeItem.Upgrade.POWER
                    || u == MachineUpgradeItem.Upgrade.POWER_STORAGE || u == MachineUpgradeItem.Upgrade.HYPER_SPEED,
            this::onUpgradesChanged);
    private LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> items);
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energyStorage);
    private int analyzeTime;
    private boolean running;
    private MachineRedstoneMode redstoneMode = MachineRedstoneMode.NONE;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case 0 -> analyzeTime;
                case 1 -> getSpeed();
                case 2 -> lowWord(energyStorage.getEnergyStored());
                case 3 -> highWord(energyStorage.getEnergyStored());
                case 4 -> lowWord(energyStorage.getMaxEnergyStored());
                case 5 -> highWord(energyStorage.getMaxEnergyStored());
                case 6 -> getPatternProgress();
                case 7 -> getInputMatter();
                case 8 -> getEnergyDrainPerTick();
                case 9 -> redstoneMode.id();
                default -> 0;
            };
        }
        @Override public void set(int i, int v) { if (i == 0) analyzeTime = Math.max(0, v); }
        @Override public int getCount() { return 10; }
    };

    public MatterAnalyzerBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.MATTER_ANALYZER.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MatterAnalyzerBlockEntity analyzer) {
        analyzer.energyStorage.beginUsageTick(level.getGameTime());
        analyzer.chargeFromEnergyItem();
        if (!analyzer.redstoneMode.allows(level, pos)) {
            analyzer.running = false;
            analyzer.updateActive(level, pos, state);
            return;
        }
        analyzer.manageAnalyze();
        analyzer.updateActive(level, pos, state);
    }

    private void updateActive(Level level, BlockPos pos, BlockState state) {
        if (state.hasProperty(MatterAnalyzerBlock.ACTIVE) && state.getValue(MatterAnalyzerBlock.ACTIVE) != running) {
            level.setBlock(pos, state.setValue(MatterAnalyzerBlock.ACTIVE, running), 3);
        }
    }

    private void chargeFromEnergyItem() {
        ItemStack stack = items.getStackInSlot(ENERGY_SLOT);
        if (stack.isEmpty() || energyStorage.getEnergyStored() >= energyStorage.getMaxEnergyStored()) return;
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(source -> {
            if (!source.canExtract()) return;
            int request = Math.min(ENERGY_ITEM_TRANSFER_PER_TICK, energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
            int offered = source.extractEnergy(request, true);
            int accepted = energyStorage.receiveEnergy(offered, false);
            if (accepted > 0) source.extractEnergy(accepted, false);
        });
    }

    private void onUpgradesChanged() {
        energyStorage.setCapacity((int) Math.min(Integer.MAX_VALUE,
                Math.round(ENERGY_STORAGE * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerStorage))));
        setChanged();
    }

    private void manageAnalyze() {
        if (!canAnalyze()) { running = false; analyzeTime = 0; return; }
        int drain = getEnergyDrainPerTick();
        if (energyStorage.getEnergyStored() < drain) { running = false; return; }
        running = true;
        energyStorage.consumeEnergy(drain, level.getGameTime());
        analyzeTime++;
        if (analyzeTime >= getSpeed()) { analyzeTime = 0; analyzeItem(); }
    }

    private boolean canAnalyze() {
        ItemStack input = items.getStackInSlot(INPUT_SLOT);
        int matter = MatterValueRegistry.getMatter(level, input);
        if (input.isEmpty() || matter <= 0) return false;
        ItemStack drive = items.getStackInSlot(DRIVE_SLOT);
        if (!drive.isEmpty()) return PatternDriveItem.canRecordAnalysis(drive, input);
        return findNetworkStorageFor(input) != null;
    }

    private void analyzeItem() {
        ItemStack input = items.getStackInSlot(INPUT_SLOT);
        int matter = MatterValueRegistry.getMatter(level, input);
        if (matter <= 0 || !canAnalyze()) return;
        boolean recorded;
        ItemStack drive = items.getStackInSlot(DRIVE_SLOT);
        if (!drive.isEmpty()) recorded = PatternDriveItem.recordAnalysis(drive, input, matter, PROGRESS_AMOUNT_PER_ITEM);
        else {
            PatternStorageBlockEntity storage = findNetworkStorageFor(input);
            recorded = storage != null && storage.addAnalysis(input, matter, PROGRESS_AMOUNT_PER_ITEM);
        }
        if (!recorded) return;
        input.shrink(1);
        if (input.isEmpty()) items.setStackInSlot(INPUT_SLOT, ItemStack.EMPTY);
        setChanged();
    }

    private PatternStorageBlockEntity findNetworkStorageFor(ItemStack input) {
        if (level == null) return null;
        for (PatternStorageBlockEntity storage : MatterNetworkUtil.findConnected(level, worldPosition, PatternStorageBlockEntity.class)) {
            if (storage.canAcceptAnalysis(input)) return storage;
        }
        return null;
    }

    private int getPatternProgress() {
        ItemStack input = items.getStackInSlot(INPUT_SLOT);
        ItemStack drive = items.getStackInSlot(DRIVE_SLOT);
        if (!drive.isEmpty()) return PatternDriveItem.getProgressFor(drive, input);
        if (level != null && !input.isEmpty()) {
            int best = 0;
            for (PatternStorageBlockEntity storage : MatterNetworkUtil.findConnected(level, worldPosition, PatternStorageBlockEntity.class)) {
                best = Math.max(best, storage.getProgressFor(input));
            }
            return best;
        }
        return 0;
    }

    public int getInputMatter() { return MatterValueRegistry.getMatter(level, items.getStackInSlot(INPUT_SLOT)); }
    public int getSpeed() { return Math.max(1, (int) Math.round(ANALYZE_SPEED * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::speed))); }
    public int getEnergyDrainPerTick() {
        int total = Math.max(1, (int) Math.round(ENERGY_DRAIN_PER_ITEM * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerUsage)));
        return Math.max(1, total / getSpeed());
    }
    public MachineRedstoneMode cycleRedstoneMode() { redstoneMode = redstoneMode.next(); setChanged(); return redstoneMode; }
    public ItemStackHandler getItemHandler() { return items; }
    public MachineEnergyStorage getEnergyStorage() { return energyStorage; }
    public MachineUpgradeInventory getUpgradeInventory() { return upgrades; }
    public ContainerData getContainerData() { return data; }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5, stack.copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            ItemStack stack = upgrades.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5, stack.copy());
                upgrades.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.put("Upgrades", upgrades.serializeNBT());
        tag.putInt("Energy", energyStorage.getEnergyStored());
        tag.putBoolean("InfiniteEnergy", energyStorage.isInfiniteEnergy());
        tag.putInt("AnalyzeTime", analyzeTime);
        tag.putBoolean("Running", running);
        tag.putInt("RedstoneMode", redstoneMode.id());
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) items.deserializeNBT(tag.getCompound("Items"));
        if (tag.contains("Upgrades")) upgrades.deserializeNBT(tag.getCompound("Upgrades"));
        onUpgradesChanged();
        energyStorage.setEnergyStored(tag.getInt("Energy"));
        energyStorage.setInfiniteEnergy(tag.getBoolean("InfiniteEnergy"));
        analyzeTime = Math.max(0, tag.getInt("AnalyzeTime"));
        running = tag.getBoolean("Running");
        redstoneMode = tag.contains("RedstoneMode") ? MachineRedstoneMode.byId(tag.getInt("RedstoneMode")) : MachineRedstoneMode.NONE;
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemCapability.cast();
        if (cap == ForgeCapabilities.ENERGY) return energyCapability.cast();
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); itemCapability.invalidate(); energyCapability.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); itemCapability = LazyOptional.of(() -> items); energyCapability = LazyOptional.of(() -> energyStorage); }
    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.matter_analyzer"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new MatterAnalyzerMenu(id, inv, this); }
    private static int lowWord(int value) { return value & 0xffff; }
    private static int highWord(int value) { return (value >>> 16) & 0xffff; }
}
