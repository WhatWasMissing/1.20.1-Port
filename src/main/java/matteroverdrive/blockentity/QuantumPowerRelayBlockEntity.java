package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.registry.ModExtraBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/** Same-dimension wireless FE relay. Relays on the same channel share energy while loaded. */
public class QuantumPowerRelayBlockEntity extends BlockEntity {
    public static final int CAPACITY = 1_000_000;
    public static final int TRANSFER = 16_384;
    public static final int RANGE = 256;
    private static final Map<ServerLevel, Map<Integer, Set<BlockPos>>> LOADED_RELAYS = new WeakHashMap<>();

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, TRANSFER, TRANSFER, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private int channel;
    private int registeredChannel = -1;
    private int lastWireless;
    private long cursor;

    public QuantumPowerRelayBlockEntity(BlockPos pos, BlockState state) { super(ModExtraBlockEntities.QUANTUM_POWER_RELAY.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, QuantumPowerRelayBlockEntity relay) {
        relay.ensureRegistered();
        relay.pullAdjacent();
        relay.lastWireless = relay.balanceWireless();
        relay.pushAdjacent();
    }

    private void ensureRegistered() {
        if (!(level instanceof ServerLevel server) || registeredChannel == channel) return;
        unregister();
        LOADED_RELAYS.computeIfAbsent(server, ignored -> new HashMap<>())
                .computeIfAbsent(channel, ignored -> new HashSet<>()).add(worldPosition.immutable());
        registeredChannel = channel;
    }

    private void unregister() {
        if (!(level instanceof ServerLevel server) || registeredChannel < 0) return;
        Map<Integer, Set<BlockPos>> byChannel = LOADED_RELAYS.get(server);
        if (byChannel != null) {
            Set<BlockPos> positions = byChannel.get(registeredChannel);
            if (positions != null) {
                positions.remove(worldPosition);
                if (positions.isEmpty()) byChannel.remove(registeredChannel);
            }
            if (byChannel.isEmpty()) LOADED_RELAYS.remove(server);
        }
        registeredChannel = -1;
    }

    private void pullAdjacent() {
        if (level == null) return;
        int remaining = Math.min(TRANSFER, energy.getMaxEnergyStored() - energy.getEnergyStored());
        for (Direction direction : Direction.values()) {
            if (remaining <= 0) break;
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null || neighbor instanceof QuantumPowerRelayBlockEntity) continue;
            IEnergyStorage source = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (source == null || !source.canExtract()) continue;
            int accepted = Math.min(source.extractEnergy(remaining, true), energy.receiveEnergy(remaining, true));
            if (accepted > 0) remaining -= energy.receiveEnergy(source.extractEnergy(accepted, false), false);
        }
    }

    private int balanceWireless() {
        if (!(level instanceof ServerLevel server) || energy.getEnergyStored() <= 0) return 0;
        Map<Integer, Set<BlockPos>> byChannel = LOADED_RELAYS.get(server);
        Set<BlockPos> positions = byChannel == null ? null : byChannel.get(channel);
        if (positions == null || positions.size() <= 1) return 0;

        double rangeSq = (double)RANGE * RANGE;
        List<QuantumPowerRelayBlockEntity> peers = new ArrayList<>();
        for (BlockPos pos : List.copyOf(positions)) {
            if (pos.equals(worldPosition)) continue;
            if (worldPosition.distSqr(pos) > rangeSq || !server.hasChunkAt(pos)) continue;
            BlockEntity be = server.getBlockEntity(pos);
            if (be instanceof QuantumPowerRelayBlockEntity peer && peer.channel == channel
                    && peer.energy.getEnergyStored() < peer.energy.getMaxEnergyStored()) peers.add(peer);
            else if (!(be instanceof QuantumPowerRelayBlockEntity)) positions.remove(pos);
        }
        if (peers.isEmpty()) return 0;
        peers.sort(Comparator.comparingLong(peer -> peer.worldPosition.asLong()));
        int start = (int)Math.floorMod(cursor++, (long)peers.size());
        int budget = Math.min(TRANSFER, energy.getEnergyStored());
        int total = 0;
        for (int i = 0; i < peers.size() && budget > 0; i++) {
            QuantumPowerRelayBlockEntity peer = peers.get((start + i) % peers.size());
            int accepted = peer.energy.receiveEnergy(budget, true);
            if (accepted <= 0) continue;
            int extracted = energy.extractEnergy(accepted, false);
            int received = peer.energy.receiveEnergy(extracted, false);
            if (received < extracted) energy.setEnergyStored(energy.getEnergyStored() + extracted - received);
            total += received;
            budget -= received;
        }
        return total;
    }

    private void pushAdjacent() {
        if (level == null || energy.getEnergyStored() <= 0) return;
        int budget = Math.min(TRANSFER, energy.getEnergyStored());
        for (Direction direction : Direction.values()) {
            if (budget <= 0) break;
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null || neighbor instanceof QuantumPowerRelayBlockEntity) continue;
            IEnergyStorage receiver = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (receiver == null || !receiver.canReceive()) continue;
            int accepted = receiver.receiveEnergy(budget, true);
            if (accepted <= 0) continue;
            int extracted = energy.extractEnergy(accepted, false);
            int received = receiver.receiveEnergy(extracted, false);
            if (received < extracted) energy.setEnergyStored(energy.getEnergyStored() + extracted - received);
            budget -= received;
        }
    }

    public InteractionResult onUse(ServerPlayer player, InteractionHand hand) {
        if (player.isCrouching()) {
            setChannel(channel + 1);
            player.sendSystemMessage(Component.literal("Quantum relay channel -> " + channel).withStyle(ChatFormatting.AQUA));
        } else {
            int peers = 0;
            if (level instanceof ServerLevel server) {
                Map<Integer, Set<BlockPos>> byChannel = LOADED_RELAYS.get(server);
                Set<BlockPos> positions = byChannel == null ? null : byChannel.get(channel);
                peers = positions == null ? 0 : Math.max(0, positions.size() - 1);
            }
            player.sendSystemMessage(Component.literal("QUANTUM POWER RELAY  CH " + channel + "  " + energy.getEnergyStored() + " / " + CAPACITY + " FE").withStyle(ChatFormatting.AQUA));
            player.sendSystemMessage(Component.literal("Wireless transfer last tick: " + lastWireless + " FE | loaded channel peers " + peers + " | range " + RANGE).withStyle(ChatFormatting.GRAY));
        }
        return InteractionResult.CONSUME;
    }

    public int getChannel() { return channel; }
    public void setChannel(int value) { unregister(); channel = value & 15; ensureRegistered(); setChanged(); }
    public int comparatorLevel() { return (int)((long)energy.getEnergyStored() * 15L / CAPACITY); }
    @Override public void onLoad() { super.onLoad(); ensureRegistered(); }
    @Override public void setRemoved() { unregister(); super.setRemoved(); }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.putInt("Energy", energy.getEnergyStored()); tag.putInt("Channel", channel); tag.putLong("Cursor", cursor); }
    @Override public void load(CompoundTag tag) { super.load(tag); energy.setEnergyStored(tag.getInt("Energy")); channel = tag.getInt("Channel") & 15; cursor = tag.getLong("Cursor"); }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) { return cap == ForgeCapabilities.ENERGY ? energyCap.cast() : super.getCapability(cap, side); }
    @Override public void invalidateCaps() { super.invalidateCaps(); energyCap.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); energyCap = LazyOptional.of(() -> energy); }
}