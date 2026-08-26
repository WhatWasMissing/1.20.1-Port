package matteroverdrive.blockentity;

import matteroverdrive.block.MatterAnalyzerBlock;
import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.item.MatterDustItem;
import matteroverdrive.item.PatternDriveItem;
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

    public static final int PROGRESS_AMOUNT_PER_ITEM = 20;
    public static final int ANALYZE_SPEED = 800;
    public static final int ENERGY_DRAIN_PER_ITEM = 64000;
    public static final int ENERGY_STORAGE = 512000;
    public static final int ENERGY_ITEM_TRANSFER_PER_TICK = 16000;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case INPUT_SLOT -> MatterValueRegistry.containsMatter(stack)
                        && (!(stack.getItem() instanceof MatterDustItem dust) || dust.isRefined());
                case ENERGY_SLOT -> stack.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::canExtract).orElse(false);
                case DRIVE_SLOT -> stack.is(ModItems.get("pattern_drive").get()) || stack.is(ModItems.get("creative_pattern_drive").get());
                default -> false;
            };
        }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };

    private final MachineEnergyStorage energyStorage = new MachineEnergyStorage(ENERGY_STORAGE, ENERGY_STORAGE, ENERGY_STORAGE, this::setChanged);
    private LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> items);
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energyStorage);
    private int analyzeTime;
    private boolean running;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> analyzeTime;
                case 1 -> ANALYZE_SPEED;
                case 2 -> lowWord(energyStorage.getEnergyStored());
                case 3 -> highWord(energyStorage.getEnergyStored());
                case 4 -> lowWord(energyStorage.getMaxEnergyStored());
                case 5 -> highWord(energyStorage.getMaxEnergyStored());
                case 6 -> getPatternProgress();
                case 7 -> getInputMatter();
                case 8 -> getEnergyDrainPerTick();
                default -> 0;
            };
        }
        @Override public void set(int index, int value) { if (index == 0) analyzeTime = Math.max(0, value); }
        @Override public int getCount() { return 9; }
    };

    public MatterAnalyzerBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.MATTER_ANALYZER.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MatterAnalyzerBlockEntity analyzer) {
        analyzer.chargeFromEnergyItem();
        analyzer.manageAnalyze();
        if (state.hasProperty(MatterAnalyzerBlock.ACTIVE) && state.getValue(MatterAnalyzerBlock.ACTIVE) != analyzer.running) {
            level.setBlock(pos, state.setValue(MatterAnalyzerBlock.ACTIVE, analyzer.running), 3);
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

    private void manageAnalyze() {
        if (!canAnalyze()) {
            running = false;
            analyzeTime = 0;
            return;
        }
        int drain = getEnergyDrainPerTick();
        if (energyStorage.getEnergyStored() < drain) { running = false; return; }
        running = true;
        energyStorage.extractEnergy(drain, false);
        analyzeTime++;
        if (analyzeTime >= ANALYZE_SPEED) {
            analyzeTime = 0;
            analyzeItem();
        }
    }

    private boolean canAnalyze() {
        ItemStack input = items.getStackInSlot(INPUT_SLOT);
        int matter = MatterValueRegistry.getMatter(input);
        if (input.isEmpty() || matter <= 0) {
            return false;
        }

        ItemStack drive = items.getStackInSlot(DRIVE_SLOT);
        if (!drive.isEmpty()) {
            return PatternDriveItem.canRecordAnalysis(drive, input);
        }

        return findNetworkStorageFor(input) != null;
    }

    private void analyzeItem() {
        ItemStack input = items.getStackInSlot(INPUT_SLOT);
        int matter = MatterValueRegistry.getMatter(input);
        if (matter <= 0 || !canAnalyze()) {
            return;
        }

        boolean recorded;
        ItemStack drive = items.getStackInSlot(DRIVE_SLOT);
        if (!drive.isEmpty()) {
            recorded = PatternDriveItem.recordAnalysis(drive, input, matter, PROGRESS_AMOUNT_PER_ITEM);
        } else {
            PatternStorageBlockEntity storage = findNetworkStorageFor(input);
            recorded = storage != null && storage.addAnalysis(input, matter, PROGRESS_AMOUNT_PER_ITEM);
        }

        if (!recorded) {
            return;
        }
        input.shrink(1);
        if (input.isEmpty()) {
            items.setStackInSlot(INPUT_SLOT, ItemStack.EMPTY);
        }
        setChanged();
    }

    private PatternStorageBlockEntity findNetworkStorageFor(ItemStack input) {
        if (level == null) {
            return null;
        }
        for (PatternStorageBlockEntity storage : MatterNetworkUtil.findConnected(level, worldPosition, PatternStorageBlockEntity.class)) {
            if (storage.canAcceptAnalysis(input)) {
                return storage;
            }
        }
        return null;
    }

    private int getPatternProgress() {
        ItemStack input = items.getStackInSlot(INPUT_SLOT);
        ItemStack drive = items.getStackInSlot(DRIVE_SLOT);
        if (!drive.isEmpty()) {
            return PatternDriveItem.getProgressFor(drive, input);
        }
        if (level != null && !input.isEmpty()) {
            int best = 0;
            for (PatternStorageBlockEntity storage : MatterNetworkUtil.findConnected(level, worldPosition, PatternStorageBlockEntity.class)) {
                best = Math.max(best, storage.getProgressFor(input));
            }
            return best;
        }
        return 0;
    }

    public int getInputMatter() { return MatterValueRegistry.getMatter(items.getStackInSlot(INPUT_SLOT)); }
    public int getEnergyDrainPerTick() { return Math.max(1, ENERGY_DRAIN_PER_ITEM / ANALYZE_SPEED); }
    public ItemStackHandler getItemHandler() { return items; }
    public MachineEnergyStorage getEnergyStorage() { return energyStorage; }
    public ContainerData getContainerData() { return data; }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        for (int slot=0; slot<items.getSlots(); slot++) {
            ItemStack stack=items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5, stack.copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); tag.put("Items", items.serializeNBT()); tag.putInt("Energy", energyStorage.getEnergyStored()); tag.putInt("AnalyzeTime", analyzeTime); tag.putBoolean("Running", running);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); if (tag.contains("Items")) items.deserializeNBT(tag.getCompound("Items")); energyStorage.setEnergyStored(tag.getInt("Energy")); analyzeTime=Math.max(0,tag.getInt("AnalyzeTime")); running=tag.getBoolean("Running");
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemCapability.cast();
        if (cap == ForgeCapabilities.ENERGY) return energyCapability.cast();
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps(){super.invalidateCaps();itemCapability.invalidate();energyCapability.invalidate();}
    @Override public void reviveCaps(){super.reviveCaps();itemCapability=LazyOptional.of(()->items);energyCapability=LazyOptional.of(()->energyStorage);}
    @Override public Component getDisplayName(){return Component.translatable("block.matteroverdrive.matter_analyzer");}
    @Nullable @Override public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player){return new MatterAnalyzerMenu(containerId, playerInventory, this);}
    private static int lowWord(int value){return value & 0xFFFF;} private static int highWord(int value){return (value>>>16)&0xFFFF;}
}
