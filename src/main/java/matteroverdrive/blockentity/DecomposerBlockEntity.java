package matteroverdrive.blockentity;

import matteroverdrive.block.DecomposerBlock;
import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.capability.MachineMatterStorage;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.item.MatterDustItem;
import matteroverdrive.matter.MatterValueRegistry;
import matteroverdrive.menu.DecomposerMenu;
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
import java.util.Random;

public class DecomposerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int ENERGY_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int SLOT_COUNT = 3;

    public static final int MATTER_EXTRACT_SPEED = 32;
    public static final float FAIL_CHANCE = 0.005F;
    public static final int MATTER_STORAGE = 1024;
    public static final int ENERGY_STORAGE = 512000;
    public static final int DECOMPOSE_SPEED_PER_MATTER = 80;
    public static final int DECOMPOSE_ENERGY_PER_MATTER = 6000;
    public static final int ENERGY_ITEM_TRANSFER_PER_TICK = 16000;

    private static final Random RANDOM = new Random();

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case INPUT_SLOT -> MatterValueRegistry.containsMatter(stack)
                        && (!(stack.getItem() instanceof MatterDustItem dust) || dust.isRefined());
                case ENERGY_SLOT -> stack.getCapability(ForgeCapabilities.ENERGY)
                        .map(IEnergyStorage::canExtract)
                        .orElse(false);
                case OUTPUT_SLOT -> false;
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
            new MachineMatterStorage(MATTER_STORAGE, false, true, this::setChanged);

    private LazyOptional<IItemHandler> itemHandlerCapability = LazyOptional.of(() -> items);
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energyStorage);
    private LazyOptional<matteroverdrive.capability.IMatterStorage> matterCapability = LazyOptional.of(() -> matterStorage);

    private int decomposeTime;
    private long lastMatterExtractTick;
    private boolean running;

    // Vanilla menu data packets encode each data-slot value as a signed 16-bit
    // short. Split the 512000 FE values into low/high words so the GUI can
    // reconstruct them without truncation.
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> decomposeTime;
                case 1 -> getSpeed();
                case 2 -> lowWord(energyStorage.getEnergyStored());
                case 3 -> highWord(energyStorage.getEnergyStored());
                case 4 -> lowWord(energyStorage.getMaxEnergyStored());
                case 5 -> highWord(energyStorage.getMaxEnergyStored());
                case 6 -> matterStorage.getMatterStored();
                case 7 -> matterStorage.getMatterCapacity();
                case 8 -> getCurrentMatterValue();
                case 9 -> getEnergyDrainPerTick();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                decomposeTime = Math.max(0, value);
            }
        }

        @Override
        public int getCount() {
            return 10;
        }
    };

    private static int lowWord(int value) {
        return value & 0xFFFF;
    }

    private static int highWord(int value) {
        return (value >>> 16) & 0xFFFF;
    }

    public DecomposerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DECOMPOSER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DecomposerBlockEntity decomposer) {
        decomposer.chargeFromEnergyItem();
        decomposer.outputMatterToNeighbors();
        decomposer.manageDecompose();

        boolean active = decomposer.running;
        if (state.hasProperty(DecomposerBlock.ACTIVE) && state.getValue(DecomposerBlock.ACTIVE) != active) {
            level.setBlock(pos, state.setValue(DecomposerBlock.ACTIVE, active), 3);
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
            int request = Math.min(
                    ENERGY_ITEM_TRANSFER_PER_TICK,
                    energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored()
            );
            int simulated = source.extractEnergy(request, true);
            if (simulated <= 0) {
                return;
            }
            int accepted = energyStorage.receiveEnergy(simulated, false);
            if (accepted > 0) {
                source.extractEnergy(accepted, false);
            }
        });
    }

    private void outputMatterToNeighbors() {
        if (level == null || matterStorage.getMatterStored() <= 0) {
            return;
        }
        long gameTime = level.getGameTime();
        if (gameTime - lastMatterExtractTick < MATTER_EXTRACT_SPEED) {
            return;
        }
        lastMatterExtractTick = gameTime;

        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null || matterStorage.getMatterStored() <= 0) {
                continue;
            }
            neighbor.getCapability(ModCapabilities.MATTER, direction.getOpposite()).ifPresent(target -> {
                int offered = matterStorage.getMatterStored();
                int accepted = target.receiveMatter(offered, false);
                if (accepted > 0) {
                    matterStorage.extractMatter(accepted, false);
                }
            });
        }

        if (matterStorage.getMatterStored() > 0) {
            for (BlockEntity targetEntity : MatterNetworkUtil.findMatterTargets(level, worldPosition)) {
                if (matterStorage.getMatterStored() <= 0) {
                    break;
                }
                targetEntity.getCapability(ModCapabilities.MATTER).ifPresent(target -> {
                    int offered = matterStorage.getMatterStored();
                    int accepted = target.receiveMatter(offered, false);
                    if (accepted > 0) {
                        matterStorage.extractMatter(accepted, false);
                    }
                });
            }
        }
    }

    private void manageDecompose() {
        int matter = getCurrentMatterValue();
        if (!canDecompose(matter)) {
            running = false;
            decomposeTime = 0;
            return;
        }

        int energyPerTick = getEnergyDrainPerTick();
        if (energyStorage.getEnergyStored() < energyPerTick) {
            running = false;
            return;
        }

        running = true;
        energyStorage.extractEnergy(energyPerTick, false);
        decomposeTime++;

        if (decomposeTime >= getSpeed()) {
            decomposeTime = 0;
            decomposeItem(matter);
        }
    }

    private boolean canDecompose(int matter) {
        ItemStack input = items.getStackInSlot(INPUT_SLOT);
        return matter > 0
                && !input.isEmpty()
                && items.isItemValid(INPUT_SLOT, input)
                && matter <= matterStorage.getMatterCapacity() - matterStorage.getMatterStored()
                && canPutInOutput(matter);
    }

    private boolean canPutInOutput(int matter) {
        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        return output.is(ModItems.get("matter_dust").get())
                && MatterDustItem.getMatter(output) == matter
                && output.getCount() < output.getMaxStackSize();
    }

    private void decomposeItem(int matter) {
        ItemStack input = items.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty() || !canPutInOutput(matter)) {
            return;
        }

        if (RANDOM.nextFloat() < FAIL_CHANCE) {
            failDecompose(matter);
        } else {
            matterStorage.addMatterInternal(matter, false);
        }

        input.shrink(1);
        if (input.isEmpty()) {
            items.setStackInSlot(INPUT_SLOT, ItemStack.EMPTY);
        }
        setChanged();
    }

    private void failDecompose(int matter) {
        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            ItemStack dust = new ItemStack(ModItems.get("matter_dust").get());
            MatterDustItem.setMatter(dust, matter);
            items.setStackInSlot(OUTPUT_SLOT, dust);
        } else {
            output.grow(1);
        }
    }

    public int getCurrentMatterValue() {
        return MatterValueRegistry.getMatter(items.getStackInSlot(INPUT_SLOT));
    }

    public int getSpeed() {
        int matter = getCurrentMatterValue();
        if (matter <= 0) {
            return 0;
        }
        double scaled = Math.log1p(matter);
        scaled *= scaled;
        return Math.max(1, (int) Math.round((scaled + 6.0D) * DECOMPOSE_SPEED_PER_MATTER));
    }

    public int getEnergyDrainMax() {
        int matter = getCurrentMatterValue();
        if (matter <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.round(
                Math.log1p(matter * 0.01D) * 15.0D * DECOMPOSE_ENERGY_PER_MATTER
        ));
    }

    public int getEnergyDrainPerTick() {
        int speed = getSpeed();
        if (speed <= 0) {
            return 0;
        }
        return Math.max(1, getEnergyDrainMax() / speed);
    }

    public boolean isRunning() {
        return running;
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
                Containers.dropItemStack(
                        level,
                        worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D,
                        stack.copy()
                );
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
        tag.putInt("DecomposeTime", decomposeTime);
        tag.putLong("LastMatterExtractTick", lastMatterExtractTick);
        tag.putBoolean("Running", running);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        energyStorage.setEnergyStored(tag.getInt("Energy"));
        matterStorage.setMatterStored(tag.getInt("Matter"));
        decomposeTime = Math.max(0, tag.getInt("DecomposeTime"));
        lastMatterExtractTick = tag.getLong("LastMatterExtractTick");
        running = tag.getBoolean("Running");
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerCapability.cast();
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
        itemHandlerCapability.invalidate();
        energyCapability.invalidate();
        matterCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemHandlerCapability = LazyOptional.of(() -> items);
        energyCapability = LazyOptional.of(() -> energyStorage);
        matterCapability = LazyOptional.of(() -> matterStorage);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.matteroverdrive.decomposer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new DecomposerMenu(containerId, playerInventory, this);
    }
}
