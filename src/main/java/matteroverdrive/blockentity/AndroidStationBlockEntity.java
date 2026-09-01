package matteroverdrive.blockentity;

import matteroverdrive.android.AndroidData;
import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.menu.AndroidStationMenu;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.UUID;

public class AndroidStationBlockEntity extends BlockEntity implements MenuProvider {
    // A one-tick hand-off buffer prevents a disconnected station from behaving
    // like a hidden 100k FE battery after it has been powered once.
    public static final int TRANSFER_PER_TICK = 2_000;
    public static final int ENERGY_CAPACITY = TRANSFER_PER_TICK;
    private final MachineEnergyStorage energy = new MachineEnergyStorage(ENERGY_CAPACITY, TRANSFER_PER_TICK, 0, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energy);
    private UUID linkedPlayer;
    private int lastTransfer;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            ServerPlayer player = linked();
            return switch (index) {
                case 0 -> player != null && AndroidData.isAndroid(player) ? 1 : 0;
                case 1 -> player == null ? 0 : AndroidData.getParts(player);
                case 2 -> player == null ? 0 : low(AndroidData.getEnergy(player));
                case 3 -> player == null ? 0 : high(AndroidData.getEnergy(player));
                case 4 -> low(AndroidData.ENERGY_CAPACITY);
                case 5 -> high(AndroidData.ENERGY_CAPACITY);
                case 6 -> low(energy.getEnergyStored());
                case 7 -> high(energy.getEnergyStored());
                case 8 -> lastTransfer;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 9; }
    };

    public AndroidStationBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ANDROID_STATION.get(), pos, state); }
    public void link(ServerPlayer player) { linkedPlayer = player.getUUID(); setChanged(); }
    private ServerPlayer linked() {
        return level == null || level.getServer() == null || linkedPlayer == null ? null
                : level.getServer().getPlayerList().getPlayer(linkedPlayer);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AndroidStationBlockEntity station) {
        station.pullAdjacentEnergy();
        station.lastTransfer = station.chargeLinkedAndroid();
    }

    private void pullAdjacentEnergy() {
        if (level == null) return;
        int remaining = Math.min(TRANSFER_PER_TICK, energy.getMaxEnergyStored() - energy.getEnergyStored());
        for (Direction direction : Direction.values()) {
            if (remaining <= 0) break;
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null) continue;
            IEnergyStorage source = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (source == null || !source.canExtract()) continue;
            int offered = source.extractEnergy(remaining, true);
            int accepted = energy.receiveEnergy(offered, true);
            if (accepted <= 0) continue;
            int extracted = source.extractEnergy(accepted, false);
            int received = energy.receiveEnergy(extracted, false);
            if (received < extracted && source.canReceive()) source.receiveEnergy(extracted - received, false);
            remaining -= received;
        }
    }

    private int chargeLinkedAndroid() {
        ServerPlayer player = linked();
        if (player == null || !AndroidData.isAndroid(player) || player.distanceToSqr(worldPosition.getX() + .5D, worldPosition.getY() + .5D, worldPosition.getZ() + .5D) > 64.0D) {
            linkedPlayer = null;
            return 0;
        }
        int offer = Math.min(TRANSFER_PER_TICK, energy.getEnergyStored());
        int accepted = AndroidData.receiveEnergy(player, offer);
        return energy.consumeEnergy(accepted, level.getGameTime());
    }

    public ContainerData getContainerData() { return data; }
    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.android_station"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new AndroidStationMenu(id, inventory, this); }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energy.getEnergyStored());
        if (linkedPlayer != null) tag.putUUID("LinkedPlayer", linkedPlayer);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergyStored(tag.getInt("Energy"));
        linkedPlayer = tag.hasUUID("LinkedPlayer") ? tag.getUUID("LinkedPlayer") : null;
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        return cap == ForgeCapabilities.ENERGY ? energyCapability.cast() : super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); energyCapability.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); energyCapability = LazyOptional.of(() -> energy); }
    private static int low(int value) { return value & 0xFFFF; }
    private static int high(int value) { return value >>> 16 & 0xFFFF; }
}