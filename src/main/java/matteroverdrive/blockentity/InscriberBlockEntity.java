package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.compat.AutomationItemHandler;
import matteroverdrive.item.MachineUpgradeInventory;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.menu.InscriberMenu;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
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

public class InscriberBlockEntity extends BlockEntity implements MenuProvider {
    public static final int PRIMARY_SLOT = 0;
    public static final int SECONDARY_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int ENERGY_SLOT = 3;
    public static final int SLOT_COUNT = 4;
    public static final int UPGRADE_SLOT_COUNT = 4;

    public static final int ENERGY_CAPACITY = 512000;
    public static final int ENERGY_TRANSFER = 256;
    public static final int MK2_TIME = 300;
    public static final int MK2_ENERGY = 64000;
    public static final int MK3_TIME = 600;
    public static final int MK3_ENERGY = 88000;
    public static final int MK4_TIME = 1200;
    public static final int MK4_ENERGY = 114000;
    public static final int ENERGY_ITEM_TRANSFER_PER_TICK = 16000;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case PRIMARY_SLOT -> isPrimaryCircuit(stack);
                case SECONDARY_SLOT -> isInscribingMaterial(stack);
                case OUTPUT_SLOT -> false;
                case ENERGY_SLOT -> stack.getCapability(ForgeCapabilities.ENERGY)
                        .map(IEnergyStorage::canExtract).orElse(false);
                default -> false;
            };
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == PRIMARY_SLOT ? 1 : super.getSlotLimit(slot);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final IItemHandler automationItems = new AutomationItemHandler(
            items,
            slot -> slot == PRIMARY_SLOT || slot == SECONDARY_SLOT || slot == ENERGY_SLOT,
            slot -> slot == OUTPUT_SLOT);
    private final MachineEnergyStorage energyStorage =
            new MachineEnergyStorage(ENERGY_CAPACITY, ENERGY_TRANSFER, ENERGY_TRANSFER, this::setChanged);
    private final MachineUpgradeInventory upgrades = new MachineUpgradeInventory(
            UPGRADE_SLOT_COUNT,
            upgrade -> upgrade == MachineUpgradeItem.Upgrade.SPEED
                    || upgrade == MachineUpgradeItem.Upgrade.POWER
                    || upgrade == MachineUpgradeItem.Upgrade.POWER_STORAGE
                    || upgrade == MachineUpgradeItem.Upgrade.HYPER_SPEED,
            this::onUpgradesChanged
    );
    private LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> automationItems);
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energyStorage);

    private int progress;
    private boolean running;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            Recipe recipe = getRecipe();
            return switch (index) {
                case 0 -> progress;
                case 1 -> getCycleTime(recipe);
                case 2 -> lowWord(energyStorage.getEnergyStored());
                case 3 -> highWord(energyStorage.getEnergyStored());
                case 4 -> lowWord(energyStorage.getMaxEnergyStored());
                case 5 -> highWord(energyStorage.getMaxEnergyStored());
                case 6 -> recipe == null ? 0 : recipe.id;
                case 7 -> getEnergyPerTick(recipe);
                case 8 -> lowWord(getTotalEnergy(recipe));
                case 9 -> highWord(getTotalEnergy(recipe));
                case 10 -> running ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) progress = Math.max(0, value);
        }

        @Override public int getCount() { return 11; }
    };

    public InscriberBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.INSCRIBER.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, InscriberBlockEntity inscriber) {
        inscriber.energyStorage.beginUsageTick(level.getGameTime());
        inscriber.chargeFromEnergyItem();
        inscriber.manageInscription();
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

    private void manageInscription() {
        Recipe recipe = getRecipe();
        if (!canInscribe(recipe)) { running = false; progress = 0; return; }
        int drain = getEnergyPerTick(recipe);
        if (energyStorage.getEnergyStored() < drain) { running = false; return; }
        running = true;
        energyStorage.consumeEnergy(drain, level.getGameTime());
        progress++;
        if (progress >= getCycleTime(recipe)) inscribe(recipe);
    }

    private boolean canInscribe(@Nullable Recipe recipe) { return recipe != null && canPutInOutput(recipe); }

    private boolean canPutInOutput(Recipe recipe) {
        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
        return output.isEmpty() || (output.is(recipe.output) && output.getCount() < output.getMaxStackSize());
    }

    private void inscribe(Recipe recipe) {
        if (!canInscribe(recipe)) return;
        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) items.setStackInSlot(OUTPUT_SLOT, new ItemStack(recipe.output));
        else output.grow(1);
        consume(PRIMARY_SLOT);
        consume(SECONDARY_SLOT);
        progress = 0;
        setChanged();
    }

    private void consume(int slot) {
        ItemStack stack = items.getStackInSlot(slot);
        stack.shrink(1);
        if (stack.isEmpty()) items.setStackInSlot(slot, ItemStack.EMPTY);
    }

    @Nullable
    private Recipe getRecipe() {
        ItemStack primary = items.getStackInSlot(PRIMARY_SLOT);
        ItemStack secondary = items.getStackInSlot(SECONDARY_SLOT);
        if (primary.is(ModItems.get("isolinear_circuit_mk1").get()) && secondary.is(net.minecraft.world.item.Items.GOLD_INGOT))
            return new Recipe(2, ModItems.get("isolinear_circuit_mk2").get(), MK2_TIME, MK2_ENERGY);
        if (primary.is(ModItems.get("isolinear_circuit_mk2").get()) && secondary.is(net.minecraft.world.item.Items.DIAMOND))
            return new Recipe(3, ModItems.get("isolinear_circuit_mk3").get(), MK3_TIME, MK3_ENERGY);
        if (primary.is(ModItems.get("isolinear_circuit_mk3").get()) && secondary.is(net.minecraft.world.item.Items.EMERALD))
            return new Recipe(4, ModItems.get("isolinear_circuit_mk4").get(), MK4_TIME, MK4_ENERGY);
        return null;
    }

    private static boolean isPrimaryCircuit(ItemStack stack) {
        return stack.is(ModItems.get("isolinear_circuit_mk1").get())
                || stack.is(ModItems.get("isolinear_circuit_mk2").get())
                || stack.is(ModItems.get("isolinear_circuit_mk3").get());
    }

    private static boolean isInscribingMaterial(ItemStack stack) {
        return stack.is(net.minecraft.world.item.Items.GOLD_INGOT)
                || stack.is(net.minecraft.world.item.Items.DIAMOND)
                || stack.is(net.minecraft.world.item.Items.EMERALD);
    }

    public int getCycleTime(@Nullable Recipe recipe) {
        if (recipe == null) return 0;
        return Math.max(1, (int) Math.round(recipe.time * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::speed)));
    }

    public int getTotalEnergy(@Nullable Recipe recipe) {
        if (recipe == null) return 0;
        return Math.max(1, (int) Math.round(recipe.energy * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerUsage)));
    }

    public int getEnergyPerTick(@Nullable Recipe recipe) {
        int cycle = getCycleTime(recipe);
        int energy = getTotalEnergy(recipe);
        return cycle <= 0 ? 0 : Math.max(1, energy / cycle);
    }

    private void onUpgradesChanged() {
        energyStorage.setCapacity((int) Math.min(Integer.MAX_VALUE, Math.round(
                ENERGY_CAPACITY * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerStorage))));
        setChanged();
    }

    public ItemStackHandler getItemHandler() { return items; }
    public MachineEnergyStorage getEnergyStorage() { return energyStorage; }
    public MachineUpgradeInventory getUpgradeInventory() { return upgrades; }
    public ContainerData getContainerData() { return data; }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        for (int slot = 0; slot < items.getSlots(); slot++) { drop(items.getStackInSlot(slot)); items.setStackInSlot(slot, ItemStack.EMPTY); }
        for (int slot = 0; slot < upgrades.getSlots(); slot++) { drop(upgrades.getStackInSlot(slot)); upgrades.setStackInSlot(slot, ItemStack.EMPTY); }
    }

    private void drop(ItemStack stack) {
        if (!stack.isEmpty()) Containers.dropItemStack(level, worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5, stack.copy());
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.put("Upgrades", upgrades.serializeNBT());
        tag.putInt("Energy", energyStorage.getEnergyStored());
        tag.putBoolean("InfiniteEnergy", energyStorage.isInfiniteEnergy());
        tag.putInt("Progress", progress);
        tag.putBoolean("Running", running);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) deserializeItems(tag.getCompound("Items"));
        if (tag.contains("Upgrades")) upgrades.deserializeNBT(tag.getCompound("Upgrades"));
        onUpgradesChanged();
        energyStorage.setEnergyStored(tag.getInt("Energy"));
        energyStorage.setInfiniteEnergy(tag.getBoolean("InfiniteEnergy"));
        progress = Math.max(0, tag.getInt("Progress"));
        running = tag.getBoolean("Running");
    }

    private void deserializeItems(CompoundTag itemTag) {
        for (int slot = 0; slot < SLOT_COUNT; slot++) items.setStackInSlot(slot, ItemStack.EMPTY);
        ListTag serialized = itemTag.getList("Items", Tag.TAG_COMPOUND);
        for (int index = 0; index < serialized.size(); index++) {
            CompoundTag entry = serialized.getCompound(index);
            int slot = entry.getByte("Slot") & 0xFF;
            if (slot >= 0 && slot < SLOT_COUNT) items.setStackInSlot(slot, ItemStack.of(entry));
        }
    }

    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) return itemCapability.cast();
        if (capability == ForgeCapabilities.ENERGY) return energyCapability.cast();
        return super.getCapability(capability, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); itemCapability.invalidate(); energyCapability.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); itemCapability = LazyOptional.of(() -> automationItems); energyCapability = LazyOptional.of(() -> energyStorage); }

    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.inscriber"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new InscriberMenu(id, inventory, this); }

    private record Recipe(int id, Item output, int time, int energy) {}
    private static int lowWord(int value) { return value & 0xFFFF; }
    private static int highWord(int value) { return (value >>> 16) & 0xFFFF; }
}
