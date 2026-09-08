package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.menu.EnergyPipeMenu;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Forge Energy relay used by the existing heavy_matter_pipe block ID. */
public class EnergyPipeBlockEntity extends BlockEntity implements MenuProvider {
    public static final int BUFFER_CAPACITY = 8_192;
    public static final int TRANSFER_PER_SIDE = 1_024;
    private final MachineEnergyStorage energy = new MachineEnergyStorage(BUFFER_CAPACITY, TRANSFER_PER_SIDE, TRANSFER_PER_SIDE, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energy);
    private int lastOutput;
    private long routeSequence;
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored() & 0xffff;
                case 1 -> (energy.getEnergyStored() >>> 16) & 0xffff;
                case 2 -> energy.getMaxEnergyStored() & 0xffff;
                case 3 -> (energy.getMaxEnergyStored() >>> 16) & 0xffff;
                case 4 -> lastOutput;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 5; }
    };

    public EnergyPipeBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ENERGY_PIPE.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EnergyPipeBlockEntity pipe) {
        // Reactor IO performs the source-side handoff. The cable only relays its
        // own buffer here, so placement order cannot race two source paths.
        pipe.lastOutput = pipe.pushEnergy();
    }

    private int pushEnergy() {
        if (level == null || energy.getEnergyStored() <= 0) return 0;

        BlockPos nextHop = findNextHopToReceiver();
        if (nextHop == null) return 0;

        BlockEntity neighbor = level.getBlockEntity(nextHop);
        if (neighbor == null) return 0;

        Direction direction = directionTo(nextHop);
        IEnergyStorage receiver;
        if (neighbor instanceof EnergyPipeBlockEntity cable) {
            receiver = cable.getEnergy();
        } else {
            receiver = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
        }
        if (receiver == null || receiver == energy || !receiver.canReceive()) return 0;

        int offered = Math.min(TRANSFER_PER_SIDE, energy.getEnergyStored());
        int accepted = receiver.receiveEnergy(offered, true);
        if (accepted <= 0) return 0;

        int extracted = energy.extractEnergy(accepted, false);
        if (extracted <= 0) return 0;

        int received = receiver.receiveEnergy(extracted, false);
        if (received < extracted) {
            energy.setEnergyStored(energy.getEnergyStored() + extracted - received);
        }
        if (received > 0) {
            routeSequence++;
            setChanged();
        }
        return received;
    }

    /**
     * Finds a first hop toward an actual energy receiver. All reachable receiver
     * branches are collected first, then the starting pipe rotates between their
     * distinct first hops. This avoids a stable BFS/direction-order bias where the
     * same endpoint can monopolise a branched cable network forever.
     */
    @Nullable
    private BlockPos findNextHopToReceiver() {
        if (level == null) return null;

        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        Map<BlockPos, BlockPos> firstHop = new HashMap<>();
        List<BlockPos> receiverHops = new ArrayList<>();
        pending.add(worldPosition);
        visited.add(worldPosition);

        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos candidate = current.relative(direction);
                BlockEntity neighbor = level.getBlockEntity(candidate);

                if (neighbor instanceof EnergyPipeBlockEntity) {
                    if (visited.add(candidate)) {
                        firstHop.put(candidate,
                                current.equals(worldPosition) ? candidate : firstHop.get(current));
                        pending.addLast(candidate);
                    }
                    continue;
                }

                if (neighbor == null || candidate.equals(worldPosition)
                        || neighbor instanceof FusionReactorIOBlockEntity
                        || neighbor instanceof FusionReactorControllerBlockEntity) {
                    continue;
                }

                IEnergyStorage receiver = neighbor.getCapability(
                        ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
                if (receiver != null && receiver.canReceive()) {
                    BlockPos hop = current.equals(worldPosition) ? candidate : firstHop.get(current);
                    if (hop != null && !receiverHops.contains(hop)) {
                        receiverHops.add(hop.immutable());
                    }
                }
            }
        }

        if (receiverHops.isEmpty()) return null;
        int index = (int) Math.floorMod(routeSequence, (long) receiverHops.size());
        return receiverHops.get(index);
    }

    private Direction directionTo(BlockPos target) {
        for (Direction direction : Direction.values()) {
            if (worldPosition.relative(direction).equals(target)) return direction;
        }
        return Direction.UP;
    }

    public MachineEnergyStorage getEnergy() { return energy; }
    public ContainerData getData() { return data; }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putLong("RouteSequence", routeSequence);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergyStored(tag.getInt("Energy"));
        routeSequence = Math.max(0L, tag.getLong("RouteSequence"));
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) return energyCapability.cast();
        return super.getCapability(capability, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); energyCapability.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); energyCapability = LazyOptional.of(() -> energy); }
    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.heavy_matter_pipe"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new EnergyPipeMenu(id, inventory, this); }
}
