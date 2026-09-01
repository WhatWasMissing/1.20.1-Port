package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.menu.NetworkRouterMenu;
import matteroverdrive.network.ItemNetworkUtil;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

public class NetworkRouterBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 10_000, TRANSFER = 1_000, ENERGY_PER_ITEM = 10;
    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, TRANSFER, 0, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private final ItemStackHandler filter = new ItemStackHandler(1) {
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private int endpoints, nodes, pylons, lastMoved;
    private long routeCursor;
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored() & 0xffff;
                case 1 -> energy.getEnergyStored() >>> 16 & 0xffff;
                case 2 -> endpoints;
                case 3 -> nodes;
                case 4 -> pylons;
                case 5 -> lastMoved;
                case 6 -> filter.getStackInSlot(0).isEmpty() ? 0 : 1;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 7; }
    };

    public NetworkRouterBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.NETWORK_ROUTER.get(), pos, state); }
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
            IEnergyStorage source = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (source == null || !source.canExtract()) continue;
            int accepted = Math.min(source.extractEnergy(remaining, true), energy.receiveEnergy(remaining, true));
            if (accepted > 0) remaining -= energy.receiveEnergy(source.extractEnergy(accepted, false), false);
        }
    }
    private void route() {
        if (level == null) return;
        ItemNetworkUtil.Scan scan = ItemNetworkUtil.scan(level, worldPosition);
        endpoints = scan.endpoints().size(); nodes = scan.nodes(); pylons = scan.pylons();
        int max = Math.min(64, energy.getEnergyStored() / ENERGY_PER_ITEM);
        lastMoved = ItemNetworkUtil.moveOneStack(level, scan.endpoints(), filter.getStackInSlot(0), max, routeCursor++);
        if (lastMoved > 0) energy.consumeEnergy(lastMoved * ENERGY_PER_ITEM, level.getGameTime());
    }
    public ItemStackHandler getFilter() { return filter; }
    public ContainerData getData() { return data; }
    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.network_router"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new NetworkRouterMenu(id, inventory, this); }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.put("Filter", filter.serializeNBT()); tag.putInt("Energy", energy.getEnergyStored()); tag.putLong("Cursor", routeCursor); }
    @Override public void load(CompoundTag tag) { super.load(tag); filter.deserializeNBT(tag.getCompound("Filter")); energy.setEnergyStored(tag.getInt("Energy")); routeCursor = tag.getLong("Cursor"); }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) { return cap == ForgeCapabilities.ENERGY ? energyCap.cast() : super.getCapability(cap, side); }
    @Override public void invalidateCaps() { super.invalidateCaps(); energyCap.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); energyCap = LazyOptional.of(() -> energy); }
}