package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.entity.DroneEntity;
import matteroverdrive.item.DroneDeploymentCoreItem;
import matteroverdrive.item.MachineUpgradeInventory;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.machine.MachineRedstoneMode;
import matteroverdrive.menu.DroneFabricatorMenu;
import matteroverdrive.network.ItemNetworkUtil;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * Server-authoritative Drone Core assembly line. Inputs are deliberately retained until the
 * final tick, so an outage pauses production without destroying expensive components.
 */
public final class DroneFabricatorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int ENERGY_CAPACITY = 200_000, INPUT_RATE = 2_048, FABRICATION_FE_PER_TICK = 128,
            FABRICATION_TICKS = 240, SLOT_PLASMA_CORE = 0, SLOT_CIRCUITS = 1, SLOT_PLATES = 2, SLOT_OUTPUT = 3, SLOT_COUNT = 4,
            UPGRADE_SLOT_COUNT = 2,
            DATA_COUNT = 14;
    private final MachineEnergyStorage energy = new MachineEnergyStorage(ENERGY_CAPACITY, INPUT_RATE, 0, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private LazyOptional<ItemStackHandler> itemCap = LazyOptional.empty();
    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case SLOT_PLASMA_CORE -> stack.is(ModItems.get("plasma_core").get());
                case SLOT_CIRCUITS -> stack.is(ModItems.get("isolinear_circuit_mk2").get());
                case SLOT_PLATES -> stack.is(ModItems.get("tritanium_plate").get());
                default -> false;
            };
        }
        @Override public int getSlotLimit(int slot) { return slot == SLOT_OUTPUT ? 1 : 64; }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private final MachineUpgradeInventory upgrades = new MachineUpgradeInventory(UPGRADE_SLOT_COUNT,
            upgrade -> upgrade == MachineUpgradeItem.Upgrade.SPEED || upgrade == MachineUpgradeItem.Upgrade.POWER,
            this::setChanged);
    private int progress, completedCores, selectedRole, redstoneMode = MachineRedstoneMode.DISABLED, networkChannel;
    private boolean running;
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> low(energy.getEnergyStored()); case 1 -> high(energy.getEnergyStored());
                case 2 -> low(energy.getMaxEnergyStored()); case 3 -> high(energy.getMaxEnergyStored());
                case 4 -> progress; case 5 -> fabricationTicks(); case 6 -> running ? 1 : 0;
                case 7 -> selectedRole; case 8 -> completedCores; case 9 -> redstoneMode;
                case 10 -> inventory.getStackInSlot(SLOT_PLASMA_CORE).getCount();
                case 11 -> inventory.getStackInSlot(SLOT_CIRCUITS).getCount();
                case 12 -> inventory.getStackInSlot(SLOT_PLATES).getCount();
                case 13 -> networkChannel;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) { }
        @Override public int getCount() { return DATA_COUNT; }
    };

    public DroneFabricatorBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.DRONE_FABRICATOR.get(), pos, state); }
    public static void serverTick(Level level, BlockPos pos, BlockState state, DroneFabricatorBlockEntity fabricator) { fabricator.tick(); }

    private void tick() {
        if (level == null || level.isClientSide) return;
        energy.beginUsageTick(level.getGameTime());
        pullAdjacentEnergy();
        pullAdjacentIngredients();
        pullNetworkIngredients();
        running = false;
        if (!MachineRedstoneMode.allowsWork(level, worldPosition, redstoneMode) || !hasIngredients() || !canOutput()) return;
        int energyCost = fabricationEnergyPerTick();
        if (energy.getEnergyStored() < energyCost) return;
        if (energy.consumeEnergy(energyCost, level.getGameTime()) != energyCost) return;
        running = true;
        if (++progress < fabricationTicks()) return;
        consumeIngredients();
        ItemStack result = new ItemStack(ModItems.DRONE_DEPLOYMENT_CORE.get());
        result.getOrCreateTag().putByte("DroneRole", (byte) selectedRole);
        inventory.setStackInSlot(SLOT_OUTPUT, result);
        progress = 0;
        completedCores = Math.min(Integer.MAX_VALUE, completedCores + 1);
        setChanged();
    }

    private boolean hasIngredients() {
        return inventory.getStackInSlot(SLOT_PLASMA_CORE).is(ModItems.get("plasma_core").get())
                && inventory.getStackInSlot(SLOT_CIRCUITS).is(ModItems.get("isolinear_circuit_mk2").get()) && inventory.getStackInSlot(SLOT_CIRCUITS).getCount() >= 2
                && inventory.getStackInSlot(SLOT_PLATES).is(ModItems.get("tritanium_plate").get()) && inventory.getStackInSlot(SLOT_PLATES).getCount() >= 4;
    }
    private boolean canOutput() { return inventory.getStackInSlot(SLOT_OUTPUT).isEmpty(); }
    private void consumeIngredients() {
        inventory.extractItem(SLOT_PLASMA_CORE, 1, false);
        inventory.extractItem(SLOT_CIRCUITS, 2, false);
        inventory.extractItem(SLOT_PLATES, 4, false);
    }
    private void pullAdjacentEnergy() {
        int remaining = Math.min(INPUT_RATE, energy.getMaxEnergyStored() - energy.getEnergyStored());
        for (Direction direction : Direction.values()) {
            if (remaining <= 0) break;
            BlockEntity neighbour = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbour == null) continue;
            IEnergyStorage source = neighbour.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (source == null || !source.canExtract()) continue;
            int accepted = energy.receiveEnergy(source.extractEnergy(remaining, true), true);
            if (accepted > 0) remaining -= energy.receiveEnergy(source.extractEnergy(accepted, false), false);
        }
    }
    /**
     * Pulls only configured recipe ingredients from adjacent inventories. The scan is bounded to
     * six neighbours and sixteen items per tick, so it cannot traverse or force-load a network.
     */
    private void pullAdjacentIngredients() {
        int budget = 16;
        for (Direction direction : Direction.values()) {
            if (budget <= 0) break;
            BlockEntity neighbour = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbour == null) continue;
            IItemHandler source = neighbour.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).orElse(null);
            if (source == null) continue;
            for (int targetSlot = SLOT_PLASMA_CORE; targetSlot <= SLOT_PLATES && budget > 0; targetSlot++) {
                ItemStack target = inventory.getStackInSlot(targetSlot);
                ItemStack wanted = switch (targetSlot) {
                    case SLOT_PLASMA_CORE -> new ItemStack(ModItems.get("plasma_core").get());
                    case SLOT_CIRCUITS -> new ItemStack(ModItems.get("isolinear_circuit_mk2").get());
                    default -> new ItemStack(ModItems.get("tritanium_plate").get());
                };
                int required = targetSlot == SLOT_PLASMA_CORE ? 1 : (targetSlot == SLOT_CIRCUITS ? 2 : 4);
                int missing = Math.max(0, required - (target.is(wanted.getItem()) ? target.getCount() : 0));
                if (missing == 0) continue;
                for (int sourceSlot = 0; sourceSlot < source.getSlots() && missing > 0 && budget > 0; sourceSlot++) {
                    ItemStack available = source.getStackInSlot(sourceSlot);
                    if (!available.is(wanted.getItem())) continue;
                    int amount = Math.min(Math.min(missing, available.getCount()), budget);
                    ItemStack simulated = source.extractItem(sourceSlot, amount, true);
                    if (simulated.isEmpty()) continue;
                    ItemStack extracted = source.extractItem(sourceSlot, simulated.getCount(), false);
                    ItemStack remainder = inventory.insertItem(targetSlot, extracted, false);
                    if (!remainder.isEmpty()) source.insertItem(sourceSlot, remainder, false);
                    int moved = extracted.getCount() - remainder.getCount();
                    if (moved > 0) { missing -= moved; budget -= moved; }
                }
            }
        }
    }
    /** Pulls only missing configured recipe ingredients from the selected item-network channel, capped per tick. */
    private void pullNetworkIngredients() {
        int budget = 16;
        for (int targetSlot = SLOT_PLASMA_CORE; targetSlot <= SLOT_PLATES && budget > 0; targetSlot++) {
            ItemStack target = inventory.getStackInSlot(targetSlot);
            ItemStack wanted = switch (targetSlot) {
                case SLOT_PLASMA_CORE -> new ItemStack(ModItems.get("plasma_core").get());
                case SLOT_CIRCUITS -> new ItemStack(ModItems.get("isolinear_circuit_mk2").get());
                default -> new ItemStack(ModItems.get("tritanium_plate").get());
            };
            int required = targetSlot == SLOT_PLASMA_CORE ? 1 : (targetSlot == SLOT_CIRCUITS ? 2 : 4);
            int present = target.is(wanted.getItem()) ? target.getCount() : 0;
            int missing = Math.max(0, required - present);
            if (missing <= 0) continue;
            int moved = ItemNetworkUtil.pullMatchingItem(level, worldPosition, inventory, targetSlot, wanted,
                    Math.min(missing, budget), level.getGameTime() + targetSlot, networkChannel);
            budget -= moved;
        }
    }
    public int fabricationTicks() {
        return Math.max(40, (int) Math.round(FABRICATION_TICKS * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::speed)));
    }
    public int fabricationEnergyPerTick() {
        return Math.max(32, (int) Math.round(FABRICATION_FE_PER_TICK * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerUsage)));
    }
    /** Only a player with the appropriate dossier clearance may program an advanced assembly template. */
    public boolean cycleSelectedRole(ServerPlayer player) {
        int candidate = selectedRole;
        for (int attempts = 0; attempts < 5; attempts++) {
            candidate = (candidate + 1) % 5;
            if (DroneDeploymentCoreItem.hasResearchClearance(player, (byte) candidate)) {
                selectedRole = candidate;
                setChanged();
                return true;
            }
        }
        return false;
    }
    public int cycleRedstoneMode() { redstoneMode = MachineRedstoneMode.next(redstoneMode); setChanged(); return redstoneMode; }
    public int networkChannel() { return networkChannel; }
    public int cycleNetworkChannel() { networkChannel = (networkChannel + 1) & 15; setChanged(); return networkChannel; }
    public ItemStackHandler getInventory() { return inventory; }
    public MachineUpgradeInventory getUpgrades() { return upgrades; }
    public MachineEnergyStorage getEnergyStorage() { return energy; }
    public ContainerData getContainerData() { return data; }
    public int selectedRole() { return selectedRole; }
    public int redstoneMode() { return redstoneMode; }
    public void dropContents() {
        if (level == null || level.isClientSide) return;
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) Containers.dropItemStack(level, worldPosition.getX() + .5D, worldPosition.getY() + .5D, worldPosition.getZ() + .5D, stack.copy());
        }
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            ItemStack stack = upgrades.getStackInSlot(slot);
            if (!stack.isEmpty()) Containers.dropItemStack(level, worldPosition.getX() + .5D, worldPosition.getY() + .5D, worldPosition.getZ() + .5D, stack.copy());
        }
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); tag.put("Inventory", inventory.serializeNBT()); tag.put("Upgrades", upgrades.serializeNBT()); tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Progress", progress); tag.putInt("CompletedCores", completedCores); tag.putInt("SelectedRole", selectedRole); tag.putInt("RedstoneMode", redstoneMode); tag.putInt("NetworkChannel", networkChannel);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); inventory.deserializeNBT(tag.getCompound("Inventory")); if (tag.contains("Upgrades")) upgrades.deserializeNBT(tag.getCompound("Upgrades")); energy.setEnergyStored(tag.getInt("Energy"));
        progress = Math.max(0, Math.min(FABRICATION_TICKS - 1, tag.getInt("Progress"))); completedCores = Math.max(0, tag.getInt("CompletedCores"));
        selectedRole = Math.max(DroneEntity.ROLE_COMBAT, Math.min(DroneEntity.ROLE_REACTOR_MAINTENANCE, tag.getInt("SelectedRole")));
        redstoneMode = MachineRedstoneMode.sanitize(tag.getInt("RedstoneMode")); networkChannel = tag.getInt("NetworkChannel") & 15;
    }
    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.drone_fabricator"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) { return new DroneFabricatorMenu(id, playerInventory, this); }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) return energyCap.cast();
        if (capability == ForgeCapabilities.ITEM_HANDLER) { if (!itemCap.isPresent()) itemCap = LazyOptional.of(() -> inventory); return itemCap.cast(); }
        return super.getCapability(capability, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); energyCap.invalidate(); itemCap.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); energyCap = LazyOptional.of(() -> energy); itemCap = LazyOptional.empty(); }
    private static int low(int value) { return value & 0xffff; }
    private static int high(int value) { return value >>> 16 & 0xffff; }
}
