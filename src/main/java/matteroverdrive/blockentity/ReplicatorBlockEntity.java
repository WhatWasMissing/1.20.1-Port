package matteroverdrive.blockentity;

import matteroverdrive.block.ReplicatorBlock;
import matteroverdrive.capability.IMatterStorage;
import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.capability.MachineMatterStorage;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.item.MatterDustItem;
import matteroverdrive.item.PatternDriveItem;
import matteroverdrive.menu.ReplicatorMenu;
import matteroverdrive.network.PatternData;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import java.util.Random;

public class ReplicatorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int DRIVE_SLOT = 0;
    public static final int ENERGY_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int FAILURE_SLOT = 3;
    public static final int SLOT_COUNT = 4;

    public static final double FAIL_CHANCE = 0.005D;
    public static final int REPLICATE_SPEED_PER_MATTER = 120;
    public static final int REPLICATE_ENERGY_PER_MATTER = 16000;
    public static final int MATTER_STORAGE = 1024;
    public static final int ENERGY_STORAGE = 512000;
    public static final int ENERGY_ITEM_TRANSFER_PER_TICK = 16000;

    private static final Random RANDOM = new Random();

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case DRIVE_SLOT -> stack.is(ModItems.get("pattern_drive").get())
                        || stack.is(ModItems.get("creative_pattern_drive").get());
                case ENERGY_SLOT -> stack.getCapability(ForgeCapabilities.ENERGY)
                        .map(IEnergyStorage::canExtract).orElse(false);
                case OUTPUT_SLOT, FAILURE_SLOT -> false;
                default -> false;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final MachineEnergyStorage energyStorage =
            new MachineEnergyStorage(ENERGY_STORAGE, ENERGY_STORAGE, ENERGY_STORAGE, this::setChanged);
    private final MachineMatterStorage matterStorage =
            new MachineMatterStorage(MATTER_STORAGE, true, false, this::setChanged);
    private LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> items);
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energyStorage);
    private LazyOptional<IMatterStorage> matterCapability = LazyOptional.of(() -> matterStorage);

    private int replicateTime;
    private boolean running;

    private ItemStack networkPattern = ItemStack.EMPTY;
    private int networkPatternMatter;
    private int networkPatternProgress;
    private int networkTaskAmount;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> replicateTime;
                case 1 -> getSpeed();
                case 2 -> lowWord(energyStorage.getEnergyStored());
                case 3 -> highWord(energyStorage.getEnergyStored());
                case 4 -> lowWord(energyStorage.getMaxEnergyStored());
                case 5 -> highWord(energyStorage.getMaxEnergyStored());
                case 6 -> matterStorage.getMatterStored();
                case 7 -> matterStorage.getMatterCapacity();
                case 8 -> getCurrentMatterCost();
                case 9 -> getCurrentPatternProgress();
                case 10 -> getEnergyDrainPerTick();
                case 11 -> (int) Math.round(getFailChance() * 10000.0D);
                case 12 -> hasNetworkTask() ? networkTaskAmount : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                replicateTime = Math.max(0, value);
            }
        }

        @Override
        public int getCount() {
            return 13;
        }
    };

    public ReplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REPLICATOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ReplicatorBlockEntity replicator) {
        replicator.chargeFromEnergyItem();
        replicator.manageReplicate();
        if (state.hasProperty(ReplicatorBlock.ACTIVE) && state.getValue(ReplicatorBlock.ACTIVE) != replicator.running) {
            level.setBlock(pos, state.setValue(ReplicatorBlock.ACTIVE, replicator.running), 3);
        }
    }

    private void chargeFromEnergyItem() {
        ItemStack stack = items.getStackInSlot(ENERGY_SLOT);
        if (stack.isEmpty() || energyStorage.getEnergyStored() >= energyStorage.getMaxEnergyStored()) {
            return;
        }
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(source -> {
            if (!source.canExtract()) {
                return;
            }
            int request = Math.min(ENERGY_ITEM_TRANSFER_PER_TICK,
                    energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
            int offered = source.extractEnergy(request, true);
            int accepted = energyStorage.receiveEnergy(offered, false);
            if (accepted > 0) {
                source.extractEnergy(accepted, false);
            }
        });
    }

    private void manageReplicate() {
        if (!canReplicate()) {
            running = false;
            replicateTime = 0;
            return;
        }
        int drain = getEnergyDrainPerTick();
        if (energyStorage.getEnergyStored() < drain) {
            running = false;
            return;
        }
        running = true;
        energyStorage.extractEnergy(drain, false);
        replicateTime++;
        if (replicateTime >= getSpeed()) {
            replicateTime = 0;
            replicateItem();
        }
    }

    public boolean queueNetworkReplication(PatternData pattern, int amount) {
        if (pattern == null || pattern.stack().isEmpty() || pattern.matter() <= 0 || pattern.progress() <= 0
                || amount <= 0) {
            return false;
        }

        if (hasNetworkTask()) {
            boolean samePattern = ItemStack.isSameItemSameTags(networkPattern, pattern.stack())
                    && networkPatternMatter == pattern.matter()
                    && networkPatternProgress == pattern.progress();
            if (!samePattern || networkTaskAmount > Integer.MAX_VALUE - amount) {
                return false;
            }
            networkTaskAmount += amount;
            setChanged();
            return true;
        }

        networkPattern = pattern.stack().copy();
        networkPattern.setCount(1);
        networkPatternMatter = pattern.matter();
        networkPatternProgress = pattern.progress();
        networkTaskAmount = amount;
        replicateTime = 0;
        setChanged();
        return true;
    }

    public boolean hasNetworkTask() {
        return networkTaskAmount > 0 && !networkPattern.isEmpty();
    }

    private ItemStack getCurrentPattern() {
        if (hasNetworkTask()) {
            return networkPattern.copy();
        }
        return PatternDriveItem.getPatternStack(items.getStackInSlot(DRIVE_SLOT));
    }

    private int getCurrentMatterCost() {
        return hasNetworkTask() ? networkPatternMatter : PatternDriveItem.getMatter(items.getStackInSlot(DRIVE_SLOT));
    }

    private int getCurrentPatternProgress() {
        return hasNetworkTask() ? networkPatternProgress : PatternDriveItem.getProgress(items.getStackInSlot(DRIVE_SLOT));
    }

    private boolean canReplicate() {
        ItemStack pattern = getCurrentPattern();
        int matter = getCurrentMatterCost();
        int progress = getCurrentPatternProgress();
        return !pattern.isEmpty() && matter > 0 && progress > 0
                && matterStorage.getMatterStored() >= matter
                && canPutInOutput(pattern)
                && canPutFailure(matter);
    }

    private boolean canPutInOutput(ItemStack pattern) {
        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameTags(output, pattern) && output.getCount() < output.getMaxStackSize();
    }

    private boolean canPutFailure(int matter) {
        ItemStack output = items.getStackInSlot(FAILURE_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        return output.is(ModItems.get("matter_dust").get())
                && MatterDustItem.getMatter(output) == matter
                && output.getCount() < output.getMaxStackSize();
    }

    private void replicateItem() {
        ItemStack pattern = getCurrentPattern();
        int matter = getCurrentMatterCost();
        if (pattern.isEmpty() || matter <= 0 || matterStorage.getMatterStored() < matter) {
            return;
        }

        boolean networkTask = hasNetworkTask();
        boolean failed = RANDOM.nextDouble() < getFailChance();
        if (failed) {
            ItemStack failure = items.getStackInSlot(FAILURE_SLOT);
            if (failure.isEmpty()) {
                ItemStack dust = new ItemStack(ModItems.get("matter_dust").get());
                MatterDustItem.setMatter(dust, matter);
                items.setStackInSlot(FAILURE_SLOT, dust);
            } else {
                failure.grow(1);
            }
        } else {
            ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
            if (output.isEmpty()) {
                items.setStackInSlot(OUTPUT_SLOT, pattern.copy());
            } else {
                output.grow(1);
            }
        }

        if (networkTask) {
            networkTaskAmount--;
            if (networkTaskAmount <= 0) {
                clearNetworkTask();
            }
        }

        matterStorage.setMatterStored(matterStorage.getMatterStored() - matter);
        setChanged();
    }

    private void clearNetworkTask() {
        networkPattern = ItemStack.EMPTY;
        networkPatternMatter = 0;
        networkPatternProgress = 0;
        networkTaskAmount = 0;
    }

    public int getSpeed() {
        int matter = getCurrentMatterCost();
        if (matter <= 0) {
            return 0;
        }
        double scaled = Math.log1p(matter);
        scaled *= scaled;
        int speed = (int) Math.round((REPLICATE_SPEED_PER_MATTER * Math.log1p(scaled * 0.05D) * 10.0D) - 60.0D) + 60;
        return Math.max(1, speed);
    }

    public int getEnergyDrainMax() {
        int matter = getCurrentMatterCost();
        if (matter <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.round(Math.log1p(matter * 0.05D) * 4.0D * REPLICATE_ENERGY_PER_MATTER));
    }

    public int getEnergyDrainPerTick() {
        int speed = getSpeed();
        return speed <= 0 ? 0 : Math.max(1, getEnergyDrainMax() / speed);
    }

    public double getFailChance() {
        int progress = getCurrentPatternProgress();
        double progressChance = 1.0D - (progress / 100.0D);
        return Math.min(1.0D, FAIL_CHANCE + progressChance);
    }

    public ItemStackHandler getItemHandler() {
        return items;
    }

    public MachineEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public MachineMatterStorage getMatterStorage() {
        return matterStorage;
    }

    public ContainerData getContainerData() {
        return data;
    }

    public void dropContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5, stack.copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putInt("Energy", energyStorage.getEnergyStored());
        tag.putInt("Matter", matterStorage.getMatterStored());
        tag.putInt("ReplicateTime", replicateTime);
        tag.putBoolean("Running", running);
        if (hasNetworkTask()) {
            CompoundTag stackTag = new CompoundTag();
            networkPattern.save(stackTag);
            tag.put("NetworkPattern", stackTag);
            tag.putInt("NetworkPatternMatter", networkPatternMatter);
            tag.putInt("NetworkPatternProgress", networkPatternProgress);
            tag.putInt("NetworkTaskAmount", networkTaskAmount);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        energyStorage.setEnergyStored(tag.getInt("Energy"));
        matterStorage.setMatterStored(tag.getInt("Matter"));
        replicateTime = Math.max(0, tag.getInt("ReplicateTime"));
        running = tag.getBoolean("Running");
        clearNetworkTask();
        if (tag.contains("NetworkPattern", Tag.TAG_COMPOUND)) {
            ItemStack loaded = ItemStack.of(tag.getCompound("NetworkPattern"));
            int matter = tag.getInt("NetworkPatternMatter");
            int progress = tag.getInt("NetworkPatternProgress");
            int amount = tag.getInt("NetworkTaskAmount");
            if (!loaded.isEmpty() && matter > 0 && progress > 0 && amount > 0) {
                networkPattern = loaded;
                networkPattern.setCount(1);
                networkPatternMatter = matter;
                networkPatternProgress = Math.min(100, progress);
                networkTaskAmount = amount;
            }
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCapability.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCapability.cast();
        }
        if (cap == ModCapabilities.MATTER) {
            return matterCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
        energyCapability.invalidate();
        matterCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemCapability = LazyOptional.of(() -> items);
        energyCapability = LazyOptional.of(() -> energyStorage);
        matterCapability = LazyOptional.of(() -> matterStorage);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.matteroverdrive.replicator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ReplicatorMenu(containerId, playerInventory, this);
    }

    private static int lowWord(int value) {
        return value & 0xFFFF;
    }

    private static int highWord(int value) {
        return (value >>> 16) & 0xFFFF;
    }
}
