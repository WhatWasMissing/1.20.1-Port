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
import java.util.List;

/** Same-dimension wireless FE relay. Relays on the same channel share energy while loaded. */
public class QuantumPowerRelayBlockEntity extends BlockEntity {
    public static final int CAPACITY = 1_000_000;
    public static final int TRANSFER = 16_384;
    public static final int RANGE = 256;
    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, TRANSFER, TRANSFER, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private int channel;
    private int lastWireless;
    private long cursor;

    public QuantumPowerRelayBlockEntity(BlockPos pos, BlockState state) { super(ModExtraBlockEntities.QUANTUM_POWER_RELAY.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, QuantumPowerRelayBlockEntity relay) {
        relay.pullAdjacent();
        relay.lastWireless = relay.balanceWireless();
        relay.pushAdjacent();
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
        int radius = RANGE;
        List<QuantumPowerRelayBlockEntity> peers = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-radius, -16, -radius), worldPosition.offset(radius, 16, radius))) {
            if (!server.hasChunkAt(pos) || pos.equals(worldPosition)) continue;
            if (server.getBlockEntity(pos) instanceof QuantumPowerRelayBlockEntity peer && peer.channel == channel
                    && peer.energy.getEnergyStored() < peer.energy.getMaxEnergyStored()) peers.add(peer);
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
            channel = (channel + 1) & 15;
            setChanged();
            player.sendSystemMessage(Component.literal("Quantum relay channel -> " + channel).withStyle(ChatFormatting.AQUA));
        } else {
            player.sendSystemMessage(Component.literal("QUANTUM POWER RELAY  CH " + channel + "  " + energy.getEnergyStored() + " / " + CAPACITY + " FE").withStyle(ChatFormatting.AQUA));
            player.sendSystemMessage(Component.literal("Wireless transfer last tick: " + lastWireless + " FE | loaded peers within " + RANGE + " blocks").withStyle(ChatFormatting.GRAY));
        }
        return InteractionResult.CONSUME;
    }

    public int getChannel() { return channel; }
    public void setChannel(int value) { channel = value & 15; setChanged(); }
    public int comparatorLevel() { return (int)((long)energy.getEnergyStored() * 15L / CAPACITY); }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.putInt("Energy", energy.getEnergyStored()); tag.putInt("Channel", channel); tag.putLong("Cursor", cursor); }
    @Override public void load(CompoundTag tag) { super.load(tag); energy.setEnergyStored(tag.getInt("Energy")); channel = tag.getInt("Channel") & 15; cursor = tag.getLong("Cursor"); }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) { return cap == ForgeCapabilities.ENERGY ? energyCap.cast() : super.getCapability(cap, side); }
    @Override public void invalidateCaps() { super.invalidateCaps(); energyCap.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); energyCap = LazyOptional.of(() -> energy); }
}