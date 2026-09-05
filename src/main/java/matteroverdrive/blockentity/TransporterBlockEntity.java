package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.item.MachineUpgradeInventory;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.item.TransportFlashDriveItem;
import matteroverdrive.menu.TransporterMenu;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TransporterBlockEntity extends BlockEntity implements MenuProvider {
    public static final int DRIVE_SLOT = 0;
    public static final int ENERGY_SLOT = 1;
    public static final int SLOT_COUNT = 2;
    public static final int UPGRADE_SLOT_COUNT = 5;

    public static final int ENERGY_CAPACITY = 1024000;
    public static final int ENERGY_PER_BLOCK = 16;
    public static final int TRANSPORT_TIME = 70;
    public static final int TRANSPORT_DELAY = 80;
    public static final int TRANSPORT_RANGE = 32;
    public static final int MAX_ENTITIES_PER_TRANSPORT = 3;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == DRIVE_SLOT
                    ? stack.getItem() instanceof TransportFlashDriveItem
                    : stack.getCapability(ForgeCapabilities.ENERGY)
                            .map(IEnergyStorage::canExtract).orElse(false);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final MachineEnergyStorage energy =
            new MachineEnergyStorage(ENERGY_CAPACITY, 256, 32_000, this::setChanged);
    private final MachineUpgradeInventory upgrades = new MachineUpgradeInventory(
            UPGRADE_SLOT_COUNT,
            upgrade -> upgrade == MachineUpgradeItem.Upgrade.SPEED
                    || upgrade == MachineUpgradeItem.Upgrade.POWER
                    || upgrade == MachineUpgradeItem.Upgrade.RANGE
                    || upgrade == MachineUpgradeItem.Upgrade.POWER_STORAGE,
            this::upgradesChanged
    );

    private LazyOptional<IItemHandler> itemCap = LazyOptional.of(() -> items);
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    private final List<Destination> destinations = new ArrayList<>();
    private int selectedDestination;
    private int progress;
    private int cooldown;
    private boolean running;
    private int lastTransportedEntities;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> cycle();
                case 2 -> low(energy.getEnergyStored());
                case 3 -> high(energy.getEnergyStored());
                case 4 -> low(energy.getMaxEnergyStored());
                case 5 -> high(energy.getMaxEnergyStored());
                case 6 -> targetValid() ? 1 : 0;
                case 7 -> distance();
                case 8 -> energyCost();
                case 9 -> range();
                case 10 -> cooldown;
                case 11 -> running ? 1 : 0;
                case 12 -> destinations.size();
                case 13 -> destinations.isEmpty() ? -1 : selectedDestination;
                case 14 -> lastTransportedEntities;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) progress = Math.max(0, value);
        }

        @Override
        public int getCount() {
            return 15;
        }
    };

    public TransporterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRANSPORTER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TransporterBlockEntity transporter) {
        transporter.energy.beginUsageTick(level.getGameTime());
        transporter.charge();
        transporter.tickTransport();
    }

    private void charge() {
        ItemStack stack = items.getStackInSlot(ENERGY_SLOT);
        if (stack.isEmpty() || energy.getEnergyStored() >= energy.getMaxEnergyStored()) return;
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(source -> {
            int offer = source.extractEnergy(16_000, true);
            int received = energy.receiveEnergy(offer, false);
            if (received > 0) source.extractEnergy(received, false);
        });
    }

    private void tickTransport() {
        if (cooldown > 0) {
            cooldown--;
            running = false;
            return;
        }

        List<Entity> entities = entitiesOnPad();
        if (!targetValid() || energy.getEnergyStored() < energyCost() || entities.isEmpty()) {
            progress = 0;
            running = false;
            return;
        }

        running = true;
        progress++;
        if (progress < cycle()) return;

        BlockPos target = selectedTarget();
        int count = Math.min(MAX_ENTITIES_PER_TRANSPORT, entities.size());
        for (int i = 0; i < count; i++) {
            Entity entity = entities.get(i);
            entity.teleportTo(target.getX() + 0.5D, target.getY() + 1.0D, target.getZ() + 0.5D);
        }

        lastTransportedEntities = count;
        energy.consumeEnergy(energyCost(), level.getGameTime());
        progress = 0;
        cooldown = delay();
        setChanged();
    }

    private List<Entity> entitiesOnPad() {
        return level == null
                ? List.of()
                : level.getEntities((Entity) null,
                        new AABB(worldPosition, worldPosition.offset(1, 2, 1)), entity -> entity.isAlive());
    }

    private BlockPos selectedTarget() {
        if (!destinations.isEmpty()) {
            clampSelectedDestination();
            return destinations.get(selectedDestination).pos();
        }
        ItemStack drive = items.getStackInSlot(DRIVE_SLOT);
        return TransportFlashDriveItem.hasTarget(drive, level)
                ? TransportFlashDriveItem.getTarget(drive)
                : worldPosition;
    }

    private boolean hasTarget() {
        if (!destinations.isEmpty()) return true;
        return level != null && TransportFlashDriveItem.hasTarget(items.getStackInSlot(DRIVE_SLOT), level);
    }

    private boolean targetValid() {
        if (level == null || !hasTarget()) return false;
        BlockPos target = selectedTarget();
        boolean sameColumnTooClose = target.getX() == worldPosition.getX()
                && target.getZ() == worldPosition.getZ()
                && target.getY() < worldPosition.getY() + 4
                && target.getY() > worldPosition.getY() - 4;
        return !sameColumnTooClose && distance() < range();
    }

    private int distance() {
        if (!hasTarget()) return 0;
        return (int) Math.round(Math.sqrt(worldPosition.distSqr(selectedTarget())));
    }

    private int cycle() {
        return Math.max(1, (int) Math.round(
                TRANSPORT_TIME * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::speed)));
    }

    private int delay() {
        return Math.max(1, (int) Math.round(
                TRANSPORT_DELAY * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::speed)));
    }

    private int range() {
        return Math.max(1, (int) Math.round(
                TRANSPORT_RANGE * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::range)));
    }

    private int energyCost() {
        return Math.max(1, (int) Math.round(distance() * ENERGY_PER_BLOCK
                * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerUsage)));
    }

    private void upgradesChanged() {
        energy.setCapacity((int) Math.min(Integer.MAX_VALUE, Math.round(
                ENERGY_CAPACITY * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerStorage))));
        setChanged();
    }

    public boolean importDriveDestination() {
        if (level == null) return false;
        ItemStack drive = items.getStackInSlot(DRIVE_SLOT);
        if (!TransportFlashDriveItem.hasTarget(drive, level)) return false;

        BlockPos target = TransportFlashDriveItem.getTarget(drive);
        for (int i = 0; i < destinations.size(); i++) {
            if (destinations.get(i).pos().equals(target)) {
                selectedDestination = i;
                setChanged();
                return true;
            }
        }

        String name = drive.hasCustomHoverName()
                ? drive.getHoverName().getString()
                : "Location " + (destinations.size() + 1);
        destinations.add(new Destination(target.immutable(), name));
        selectedDestination = destinations.size() - 1;
        setChanged();
        return true;
    }

    public boolean cycleDestination(int direction) {
        if (destinations.isEmpty()) return false;
        selectedDestination = Math.floorMod(selectedDestination + direction, destinations.size());
        progress = 0;
        running = false;
        setChanged();
        return true;
    }

    public boolean removeSelectedDestination() {
        if (destinations.isEmpty()) return false;
        clampSelectedDestination();
        destinations.remove(selectedDestination);
        if (destinations.isEmpty()) selectedDestination = 0;
        else selectedDestination = Math.min(selectedDestination, destinations.size() - 1);
        progress = 0;
        running = false;
        setChanged();
        return true;
    }

    public String getSelectedDestinationName() {
        if (destinations.isEmpty()) return "Flash Drive";
        clampSelectedDestination();
        return destinations.get(selectedDestination).name();
    }

    private void clampSelectedDestination() {
        if (destinations.isEmpty()) {
            selectedDestination = 0;
        } else {
            selectedDestination = Math.max(0, Math.min(selectedDestination, destinations.size() - 1));
        }
    }

    public ItemStackHandler getItemHandler() { return items; }
    public MachineEnergyStorage getEnergy() { return energy; }
    public MachineUpgradeInventory getUpgrades() { return upgrades; }
    public ContainerData getData() { return data; }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            drop(stack);
            items.setStackInSlot(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < upgrades.getSlots(); i++) {
            ItemStack stack = upgrades.getStackInSlot(i);
            drop(stack);
            upgrades.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    private void drop(ItemStack stack) {
        if (!stack.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, stack.copy());
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.put("Upgrades", upgrades.serializeNBT());
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putBoolean("InfiniteEnergy", energy.isInfiniteEnergy());
        tag.putInt("Progress", progress);
        tag.putInt("Cooldown", cooldown);
        tag.putInt("SelectedTransport", selectedDestination);
        tag.putInt("LastTransportedEntities", lastTransportedEntities);

        ListTag destinationTags = new ListTag();
        for (Destination destination : destinations) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Pos", destination.pos().asLong());
            entry.putString("Name", destination.name());
            destinationTags.add(entry);
        }
        tag.put("TransportLocations", destinationTags);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) items.deserializeNBT(tag.getCompound("Items"));
        if (tag.contains("Upgrades")) upgrades.deserializeNBT(tag.getCompound("Upgrades"));
        upgradesChanged();
        energy.setEnergyStored(tag.getInt("Energy"));
        energy.setInfiniteEnergy(tag.getBoolean("InfiniteEnergy"));
        progress = Math.max(0, tag.getInt("Progress"));
        cooldown = Math.max(0, tag.getInt("Cooldown"));
        selectedDestination = Math.max(0, tag.getInt("SelectedTransport"));
        lastTransportedEntities = Math.max(0, tag.getInt("LastTransportedEntities"));

        destinations.clear();
        ListTag destinationTags = tag.getList("TransportLocations", Tag.TAG_COMPOUND);
        for (int i = 0; i < destinationTags.size(); i++) {
            CompoundTag entry = destinationTags.getCompound(i);
            destinations.add(new Destination(BlockPos.of(entry.getLong("Pos")),
                    entry.getString("Name")));
        }
        clampSelectedDestination();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable net.minecraft.core.Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) return itemCap.cast();
        if (capability == ForgeCapabilities.ENERGY) return energyCap.cast();
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCap.invalidate();
        energyCap.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemCap = LazyOptional.of(() -> items);
        energyCap = LazyOptional.of(() -> energy);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.matteroverdrive.transporter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new TransporterMenu(id, inventory, this);
    }

    private record Destination(BlockPos pos, String name) {}

    private static int low(int value) { return value & 65_535; }
    private static int high(int value) { return value >>> 16 & 65_535; }
}
