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
import net.minecraft.nbt.ListTag;
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
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NetworkRouterBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 10_000, TRANSFER = 1_000, ENERGY_PER_ITEM = 10, BASE_ITEM_BUDGET = 64,
            UPGRADE_SLOT_COUNT = 4, DATA_COUNT = 22, LEGACY_TASK_QUEUE_SIZE = 16;
    private static final int MAX_MOVES_PER_TICK = 64;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, TRANSFER, 0, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private final ItemStackHandler filter = new ItemStackHandler(1) {
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private final MachineUpgradeInventory upgrades = new MachineUpgradeInventory(UPGRADE_SLOT_COUNT,
            upgrade -> upgrade == MachineUpgradeItem.Upgrade.SPEED || upgrade == MachineUpgradeItem.Upgrade.HYPER_SPEED,
            this::setChanged);
    private final Set<BlockPos> routeSinks = new HashSet<>();
    private final ArrayDeque<RouteTrace> routeHistory = new ArrayDeque<>();

    private int endpoints, nodes, pylons, lastMoved, routerCount, disabledSwitches, pylonLinks, stalledTicks;
    private boolean executing, graphTruncated;
    private ItemNetworkUtil.MoveStatus lastStatus = ItemNetworkUtil.MoveStatus.IDLE;
    private long routeCursor;
    private int channel;
    private int priority;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            ItemStack filterStack = filter.getStackInSlot(0);
            boolean destinationDrive = NetworkFlashDriveItem.isNetworkFlashDrive(filterStack);
            return switch (index) {
                case 0 -> energy.getEnergyStored() & 0xffff; case 1 -> energy.getEnergyStored() >>> 16 & 0xffff;
                case 2 -> endpoints; case 3 -> nodes; case 4 -> pylons; case 5 -> lastMoved;
                case 6 -> filterStack.isEmpty() ? 0 : 1;
                case 7 -> destinationDrive ? NetworkFlashDriveItem.getConnections(filterStack).size() : 0;
                case 8 -> filterStack.isEmpty() ? 0 : destinationDrive ? 2 : 1;
                case 9 -> itemBudget(); case 10 -> routerCount; case 11 -> lastStatus.ordinal(); case 12 -> stalledTicks;
                case 13 -> executing ? 1 : 0; case 14 -> routeSinks.size(); case 15 -> disabledSwitches; case 16 -> pylonLinks;
                case 17 -> graphTruncated ? 1 : 0; case 18 -> routeHistory.size(); case 19 -> lastMoved * ENERGY_PER_ITEM;
                case 20 -> channel; case 21 -> priority; default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    public NetworkRouterBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.NETWORK_ROUTER.get(), pos, state); }
    public static void serverTick(Level level, BlockPos pos, BlockState state, NetworkRouterBlockEntity router) { router.pullEnergy(); router.route(); }

    private void pullEnergy() {
        if (level == null) return;
        int remaining = Math.min(TRANSFER, energy.getMaxEnergyStored() - energy.getEnergyStored());
        for (Direction direction : Direction.values()) {
            if (remaining <= 0) break;
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null) continue;
            IEnergyStorage source = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (source == null || !source.canExtract()) continue;
            int accepted = Math.min(source.extractEnergy(remaining, true), energy.receiveEnergy(remaining, true));
            if (accepted > 0) remaining -= energy.receiveEnergy(source.extractEnergy(accepted, false), false);
        }
    }

    private void route() {
        if (level == null) return;
        ItemNetworkUtil.Scan scan = ItemNetworkUtil.scan(level, worldPosition, channel);
        endpoints = scan.endpoints().size(); nodes = scan.nodes(); pylons = scan.pylons(); routerCount = scan.routers().size();
        disabledSwitches = scan.disabledSwitches(); pylonLinks = scan.pylonLinks(); graphTruncated = scan.truncated(); lastMoved = 0; executing = false;

        NetworkRouterBlockEntity stateOwner = routingStateOwner(scan);
        stateOwner.pruneRouteSinks(scan);
        BlockPos executor = executorPos(scan);
        if (executor == null) { setRouteStatus(graphTruncated ? ItemNetworkUtil.MoveStatus.GRAPH_LIMIT : ItemNetworkUtil.MoveStatus.NO_ENERGY, false); return; }
        if (!executor.equals(worldPosition)) { setRouteStatus(ItemNetworkUtil.MoveStatus.SECONDARY_ROUTER, false); return; }

        executing = true;
        int remainingBudget = Math.min(itemBudget(), energy.getEnergyStored() / ENERGY_PER_ITEM);
        if (remainingBudget <= 0) { setRouteStatus(ItemNetworkUtil.MoveStatus.NO_ENERGY, false); return; }
        int movedTotal = 0, attempts = 0;
        ItemNetworkUtil.MoveStatus terminal = ItemNetworkUtil.MoveStatus.IDLE;
        while (remainingBudget > 0 && attempts++ < MAX_MOVES_PER_TICK) {
            ItemNetworkUtil.MoveResult result = ItemNetworkUtil.moveOneStack(level, scan.endpoints(), filter.getStackInSlot(0), remainingBudget,
                    stateOwner.routeCursor++, stateOwner.routeSinks);
            terminal = result.status();
            if (result.count() <= 0) break;
            int moved = Math.min(result.count(), remainingBudget);
            movedTotal += moved; remainingBudget -= moved; stateOwner.routeSinks.add(result.destination().immutable());
            energy.consumeEnergy(moved * ENERGY_PER_ITEM, level.getGameTime()); recordTrace(result.source(), result.destination(), moved);
            if (energy.getEnergyStored() < ENERGY_PER_ITEM) break;
            remainingBudget = Math.min(remainingBudget, energy.getEnergyStored() / ENERGY_PER_ITEM);
        }
        lastMoved = movedTotal; stateOwner.lastMoved = movedTotal;
        if (movedTotal > 0) setRouteStatus(ItemNetworkUtil.MoveStatus.MOVED, true);
        else setRouteStatus(graphTruncated && terminal == ItemNetworkUtil.MoveStatus.IDLE ? ItemNetworkUtil.MoveStatus.GRAPH_LIMIT : terminal, false);
        stateOwner.setChanged();
    }

    private void setRouteStatus(ItemNetworkUtil.MoveStatus status, boolean moved) {
        if (status == null) status = ItemNetworkUtil.MoveStatus.IDLE;
        lastStatus = status; stalledTicks = moved ? 0 : Math.min(Integer.MAX_VALUE, stalledTicks + 1); setChanged();
    }
    private void recordTrace(BlockPos source, BlockPos destination, int count) {
        if (source == null || destination == null || count <= 0 || level == null) return;
        routeHistory.addFirst(new RouteTrace(level.getGameTime(), count, source.immutable(), destination.immutable()));
        while (routeHistory.size() > LEGACY_TASK_QUEUE_SIZE) routeHistory.removeLast();
    }

    public List<RouteTrace> routeHistory() { return List.copyOf(new ArrayList<>(routeHistory)); }
    public int itemBudget() {
        double speedMultiplier = upgrades.getMultiplier(MachineUpgradeItem.Upgrade::speed);
        if (speedMultiplier <= 0.0D) return BASE_ITEM_BUDGET;
        return Math.max(1, Math.min(1024, (int)Math.round(BASE_ITEM_BUDGET / speedMultiplier)));
    }

    private NetworkRouterBlockEntity routingStateOwner(ItemNetworkUtil.Scan scan) {
        NetworkRouterBlockEntity best = this;
        for (BlockPos routerPos : scan.routers()) {
            if (!(level.getBlockEntity(routerPos) instanceof NetworkRouterBlockEntity router) || router.channel != channel) continue;
            if (router.priority > best.priority || (router.priority == best.priority && router.worldPosition.asLong() < best.worldPosition.asLong())) best = router;
        }
        return best;
    }

    @Nullable private BlockPos executorPos(ItemNetworkUtil.Scan scan) {
        NetworkRouterBlockEntity best = null;
        for (BlockPos routerPos : scan.routers()) {
            if (!(level.getBlockEntity(routerPos) instanceof NetworkRouterBlockEntity router) || router.channel != channel
                    || router.energy.getEnergyStored() < ENERGY_PER_ITEM) continue;
            if (best == null || router.priority > best.priority || (router.priority == best.priority && router.worldPosition.asLong() < best.worldPosition.asLong())) best = router;
        }
        return best == null ? (energy.getEnergyStored() >= ENERGY_PER_ITEM ? worldPosition : null) : best.worldPosition;
    }

    private void pruneRouteSinks(ItemNetworkUtil.Scan scan) {
        Iterator<BlockPos> iterator = routeSinks.iterator();
        while (iterator.hasNext()) {
            BlockPos sink = iterator.next(); ItemNetworkUtil.Endpoint endpoint = null;
            for (ItemNetworkUtil.Endpoint candidate : scan.endpoints()) if (candidate.pos().equals(sink)) { endpoint = candidate; break; }
            if (endpoint == null || !ItemNetworkUtil.endpointHasItems(level, endpoint)) iterator.remove();
        }
    }

    public int getChannel() { return channel; }
    public void setChannel(int value) { channel = value & 15; routeSinks.clear(); routeCursor = 0; setChanged(); }
    public int cycleChannel() { setChannel(channel + 1); return channel; }
    public int getPriority() { return priority; }
    public void setPriority(int value) { priority = Math.max(-16, Math.min(16, value)); setChanged(); }
    public int cyclePriority() { setPriority(priority >= 16 ? -16 : priority + 1); return priority; }
    public ItemStackHandler getFilter() { return filter; }
    public MachineUpgradeInventory getUpgrades() { return upgrades; }
    public ContainerData getData() { return data; }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        ItemStack stack = filter.getStackInSlot(0);
        if (!stack.isEmpty()) { Containers.dropItemStack(level, worldPosition.getX()+0.5D, worldPosition.getY()+0.5D, worldPosition.getZ()+0.5D, stack.copy()); filter.setStackInSlot(0, ItemStack.EMPTY); }
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            ItemStack upgrade = upgrades.getStackInSlot(slot);
            if (!upgrade.isEmpty()) { Containers.dropItemStack(level, worldPosition.getX()+0.5D, worldPosition.getY()+0.5D, worldPosition.getZ()+0.5D, upgrade.copy()); upgrades.setStackInSlot(slot, ItemStack.EMPTY); }
        }
    }

    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.network_router"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new NetworkRouterMenu(id, inventory, this); }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); tag.put("Filter", filter.serializeNBT()); tag.put("Upgrades", upgrades.serializeNBT()); tag.putInt("Energy", energy.getEnergyStored());
        tag.putLong("Cursor", routeCursor); tag.putLongArray("RouteSinks", routeSinks.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putInt("RouterStatus", lastStatus.ordinal()); tag.putInt("StalledTicks", stalledTicks); tag.putInt("Channel", channel); tag.putInt("Priority", priority);
        ListTag history = new ListTag();
        for (RouteTrace trace : routeHistory) { CompoundTag entry = new CompoundTag(); entry.putLong("Tick", trace.tick()); entry.putInt("Count", trace.count()); entry.putLong("Source", trace.source().asLong()); entry.putLong("Destination", trace.destination().asLong()); history.add(entry); }
        tag.put("RouteHistory", history);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag); filter.deserializeNBT(tag.getCompound("Filter")); if (tag.contains("Upgrades")) upgrades.deserializeNBT(tag.getCompound("Upgrades"));
        energy.setEnergyStored(tag.getInt("Energy")); routeCursor = tag.getLong("Cursor"); routeSinks.clear(); for (long packed : tag.getLongArray("RouteSinks")) routeSinks.add(BlockPos.of(packed));
        lastStatus = ItemNetworkUtil.MoveStatus.byOrdinal(tag.getInt("RouterStatus")); stalledTicks = Math.max(0, tag.getInt("StalledTicks")); channel = tag.getInt("Channel") & 15; priority = Math.max(-16, Math.min(16, tag.getInt("Priority")));
        routeHistory.clear();
        if (tag.contains("RouteHistory", Tag.TAG_LIST)) {
            ListTag history = tag.getList("RouteHistory", Tag.TAG_COMPOUND);
            for (int i = 0; i < history.size() && routeHistory.size() < LEGACY_TASK_QUEUE_SIZE; i++) {
                CompoundTag entry = history.getCompound(i);
                routeHistory.addLast(new RouteTrace(entry.getLong("Tick"), Math.max(0, entry.getInt("Count")), BlockPos.of(entry.getLong("Source")), BlockPos.of(entry.getLong("Destination"))));
            }
        }
    }

    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) { return cap == ForgeCapabilities.ENERGY ? energyCap.cast() : super.getCapability(cap, side); }
    @Override public void invalidateCaps() { super.invalidateCaps(); energyCap.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); energyCap = LazyOptional.of(() -> energy); }
    public record RouteTrace(long tick, int count, BlockPos source, BlockPos destination) {}
}