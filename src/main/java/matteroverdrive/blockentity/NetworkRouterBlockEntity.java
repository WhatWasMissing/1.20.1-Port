package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.item.MachineUpgradeInventory;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.item.NetworkFlashDriveItem;
import matteroverdrive.menu.NetworkRouterMenu;
import matteroverdrive.network.ItemNetworkUtil;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NetworkRouterBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 10_000;
    public static final int TRANSFER = 1_000;
    public static final int ENERGY_PER_ITEM = 10;
    public static final int BASE_ITEM_BUDGET = 64;
    public static final int UPGRADE_SLOT_COUNT = 4;
    private static final int MAX_MOVES_PER_TICK = 64;

    private final MachineEnergyStorage energy =
            new MachineEnergyStorage(CAPACITY, TRANSFER, 0, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private final ItemStackHandler filter = new ItemStackHandler(1) {
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private final MachineUpgradeInventory upgrades = new MachineUpgradeInventory(
            UPGRADE_SLOT_COUNT,
            upgrade -> upgrade == MachineUpgradeItem.Upgrade.SPEED
                    || upgrade == MachineUpgradeItem.Upgrade.HYPER_SPEED,
            this::setChanged
    );
    private final Set<BlockPos> routeSinks = new HashSet<>();
    private int endpoints;
    private int nodes;
    private int pylons;
    private int lastMoved;
    private long routeCursor;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            ItemStack filterStack = filter.getStackInSlot(0);
            boolean destinationDrive = NetworkFlashDriveItem.isNetworkFlashDrive(filterStack);
            return switch (index) {
                case 0 -> energy.getEnergyStored() & 0xffff;
                case 1 -> energy.getEnergyStored() >>> 16 & 0xffff;
                case 2 -> endpoints;
                case 3 -> nodes;
                case 4 -> pylons;
                case 5 -> lastMoved;
                case 6 -> filterStack.isEmpty() ? 0 : 1;
                case 7 -> destinationDrive ? NetworkFlashDriveItem.getConnections(filterStack).size() : 0;
                case 8 -> filterStack.isEmpty() ? 0 : destinationDrive ? 2 : 1;
                case 9 -> itemBudget();
                default -> 0;
            };
        }

        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 10; }
    };

    public NetworkRouterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NETWORK_ROUTER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, NetworkRouterBlockEntity router) {
        router.pullEnergy();
        router.route();
    }

    private void pullEnergy() {
        if (level == null) return;
        int remaining = Math.min(TRANSFER, energy.getMaxEnergyStored() - energy.getEnergyStored());
        for (Direction direction : Direction.values()) {
            if (remaining <= 0) break;
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null) continue;
            IEnergyStorage source = neighbor.getCapability(
                    ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (source == null || !source.canExtract()) continue;
            int accepted = Math.min(source.extractEnergy(remaining, true), energy.receiveEnergy(remaining, true));
            if (accepted > 0) {
                remaining -= energy.receiveEnergy(source.extractEnergy(accepted, false), false);
            }
        }
    }

    private void route() {
        if (level == null) return;
        ItemNetworkUtil.Scan scan = ItemNetworkUtil.scan(level, worldPosition);
        endpoints = scan.endpoints().size();
        nodes = scan.nodes();
        pylons = scan.pylons();
        lastMoved = 0;

        NetworkRouterBlockEntity stateOwner = routingStateOwner(scan);
        stateOwner.pruneRouteSinks(scan);
        if (!isRoutingExecutor(scan)) return;

        int remainingBudget = Math.min(itemBudget(), energy.getEnergyStored() / ENERGY_PER_ITEM);
        int movedTotal = 0;
        int attempts = 0;
        while (remainingBudget > 0 && attempts++ < MAX_MOVES_PER_TICK) {
            ItemNetworkUtil.MoveResult result = ItemNetworkUtil.moveOneStack(
                    level, scan.endpoints(), filter.getStackInSlot(0), remainingBudget,
                    stateOwner.routeCursor++, stateOwner.routeSinks);
            if (result.count() <= 0) break;

            int moved = Math.min(result.count(), remainingBudget);
            movedTotal += moved;
            remainingBudget -= moved;
            stateOwner.routeSinks.add(result.destination().immutable());
            energy.consumeEnergy(moved * ENERGY_PER_ITEM, level.getGameTime());

            if (energy.getEnergyStored() < ENERGY_PER_ITEM) break;
            remainingBudget = Math.min(remainingBudget, energy.getEnergyStored() / ENERGY_PER_ITEM);
        }

        lastMoved = movedTotal;
        stateOwner.lastMoved = movedTotal;
        stateOwner.setChanged();
    }

    public int itemBudget() {
        double speedMultiplier = upgrades.getMultiplier(MachineUpgradeItem.Upgrade::speed);
        if (speedMultiplier <= 0.0D) return BASE_ITEM_BUDGET;
        return Math.max(1, Math.min(1024,
                (int) Math.round(BASE_ITEM_BUDGET / speedMultiplier)));
    }

    private NetworkRouterBlockEntity routingStateOwner(ItemNetworkUtil.Scan scan) {
        for (BlockPos routerPos : scan.routers()) {
            if (level.getBlockEntity(routerPos) instanceof NetworkRouterBlockEntity router) return router;
        }
        return this;
    }

    private boolean isRoutingExecutor(ItemNetworkUtil.Scan scan) {
        for (BlockPos routerPos : scan.routers()) {
            if (level.getBlockEntity(routerPos) instanceof NetworkRouterBlockEntity router
                    && router.energy.getEnergyStored() >= ENERGY_PER_ITEM) {
                return routerPos.equals(worldPosition);
            }
        }
        return energy.getEnergyStored() >= ENERGY_PER_ITEM;
    }

    private void pruneRouteSinks(ItemNetworkUtil.Scan scan) {
        Iterator<BlockPos> iterator = routeSinks.iterator();
        while (iterator.hasNext()) {
            BlockPos sink = iterator.next();
            ItemNetworkUtil.Endpoint endpoint = null;
            for (ItemNetworkUtil.Endpoint candidate : scan.endpoints()) {
                if (candidate.pos().equals(sink)) {
                    endpoint = candidate;
                    break;
                }
            }
            if (endpoint == null || !ItemNetworkUtil.endpointHasItems(level, endpoint)) iterator.remove();
        }
    }

    public ItemStackHandler getFilter() { return filter; }
    public MachineUpgradeInventory getUpgrades() { return upgrades; }
    public ContainerData getData() { return data; }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        ItemStack stack = filter.getStackInSlot(0);
        if (!stack.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, stack.copy());
            filter.setStackInSlot(0, ItemStack.EMPTY);
        }
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            ItemStack upgrade = upgrades.getStackInSlot(slot);
            if (!upgrade.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, upgrade.copy());
                upgrades.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.matteroverdrive.network_router");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new NetworkRouterMenu(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Filter", filter.serializeNBT());
        tag.put("Upgrades", upgrades.serializeNBT());
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putLong("Cursor", routeCursor);
        tag.putLongArray("RouteSinks", routeSinks.stream().mapToLong(BlockPos::asLong).toArray());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        filter.deserializeNBT(tag.getCompound("Filter"));
        if (tag.contains("Upgrades")) upgrades.deserializeNBT(tag.getCompound("Upgrades"));
        energy.setEnergyStored(tag.getInt("Energy"));
        routeCursor = tag.getLong("Cursor");
        routeSinks.clear();
        for (long packed : tag.getLongArray("RouteSinks")) routeSinks.add(BlockPos.of(packed));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        return cap == ForgeCapabilities.ENERGY ? energyCap.cast() : super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyCap = LazyOptional.of(() -> energy);
    }
}
