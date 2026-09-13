package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.machine.MachineSideConfigurationData;
import matteroverdrive.registry.ModExtraBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Same-dimension wireless FE relay. Energy only travels across explicit player-created links. */
public class QuantumPowerRelayBlockEntity extends BlockEntity {
    public static final int CAPACITY = 1_000_000;
    public static final int TRANSFER = 16_384;
    public static final int RANGE = 256;
    public static final int MAX_LINKS = 8;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, TRANSFER, TRANSFER, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private final Set<BlockPos> links = new HashSet<>();
    private int lastWireless;
    private long cursor;

    public QuantumPowerRelayBlockEntity(BlockPos pos, BlockState state) { super(ModExtraBlockEntities.QUANTUM_POWER_RELAY.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, QuantumPowerRelayBlockEntity relay) {
        relay.pullAdjacent();
        relay.lastWireless = relay.transferToLinkedPeers();
        relay.pushAdjacent();
    }

    private void pullAdjacent() {
        if (level == null) return;
        int remaining = Math.min(TRANSFER, energy.getMaxEnergyStored() - energy.getEnergyStored());
        for (Direction direction : Direction.values()) {
            if (remaining <= 0) break;
            BlockPos neighborPos = worldPosition.relative(direction);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);
            if (neighbor == null || neighbor instanceof QuantumPowerRelayBlockEntity) continue;
            if (!MachineSideConfigurationData.allowsOutput(level, neighborPos, direction.getOpposite(), MachineSideConfigurationData.Resource.ENERGY)) continue;
            IEnergyStorage source = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (source == null || !source.canExtract()) continue;
            int accepted = Math.min(source.extractEnergy(remaining, true), energy.receiveEnergy(remaining, true));
            if (accepted > 0) remaining -= energy.receiveEnergy(source.extractEnergy(accepted, false), false);
        }
    }

    /**
     * Balance linked relays instead of blindly filling every peer. A bidirectional link is
     * intentionally symmetric, so transferring only from the fuller relay to the emptier
     * relay prevents the two server ticks from immediately shuttling the same FE back again.
     */
    private int transferToLinkedPeers() {
        if (level == null || energy.getEnergyStored() <= 0 || links.isEmpty()) return 0;
        double rangeSq = (double) RANGE * RANGE;
        List<QuantumPowerRelayBlockEntity> peers = new ArrayList<>();
        for (BlockPos target : List.copyOf(links)) {
            if (worldPosition.distSqr(target) > rangeSq || !level.hasChunkAt(target)) continue;
            BlockEntity be = level.getBlockEntity(target);
            if (be instanceof QuantumPowerRelayBlockEntity peer && peer.isLinkedTo(worldPosition)
                    && peer.energy.getEnergyStored() < peer.energy.getMaxEnergyStored()) peers.add(peer);
        }
        if (peers.isEmpty()) return 0;
        peers.sort(Comparator.comparingLong(peer -> peer.worldPosition.asLong()));
        int start = (int)Math.floorMod(cursor++, (long)peers.size());
        int budget = Math.min(TRANSFER, energy.getEnergyStored());
        int total = 0;
        for (int i = 0; i < peers.size() && budget > 0; i++) {
            QuantumPowerRelayBlockEntity peer = peers.get((start + i) % peers.size());
            int difference = energy.getEnergyStored() - peer.energy.getEnergyStored();
            if (difference <= 1) continue;
            int balanceAmount = Math.max(1, difference / 2);
            int offer = Math.min(budget, balanceAmount);
            int accepted = peer.energy.receiveEnergy(offer, true);
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
            BlockPos neighborPos = worldPosition.relative(direction);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);
            if (neighbor == null || neighbor instanceof QuantumPowerRelayBlockEntity) continue;
            if (!MachineSideConfigurationData.allowsInput(level, neighborPos, direction.getOpposite(), MachineSideConfigurationData.Resource.ENERGY)) continue;
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

    public boolean addLink(BlockPos target) {
        if (target == null || target.equals(worldPosition) || links.size() >= MAX_LINKS || worldPosition.distSqr(target) > (double)RANGE * RANGE) return false;
        boolean changed = links.add(target.immutable());
        if (changed) setChanged();
        return changed;
    }
    public boolean removeLink(BlockPos target) { boolean changed = links.remove(target); if (changed) setChanged(); return changed; }
    public boolean isLinkedTo(BlockPos target) { return links.contains(target); }
    public int getLinkCount() { return links.size(); }
    public void clearLinks() {
        if (links.isEmpty()) return;
        if (level != null) {
            for (BlockPos target : List.copyOf(links)) {
                if (!level.hasChunkAt(target)) continue;
                BlockEntity be = level.getBlockEntity(target);
                if (be instanceof QuantumPowerRelayBlockEntity peer) peer.removeLink(worldPosition);
            }
        }
        links.clear();
        setChanged();
    }

    public InteractionResult onUse(ServerPlayer player, InteractionHand hand) {
        player.sendSystemMessage(Component.literal("QUANTUM POWER RELAY  " + energy.getEnergyStored() + " / " + CAPACITY + " FE").withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("Explicit links: " + links.size() + "/" + MAX_LINKS + " | last wireless transfer " + lastWireless + " FE | max link distance " + RANGE).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("Use a Quantum Linker on two relays to pair them. Sneak-use the Linker on a relay to clear its links.").withStyle(ChatFormatting.DARK_GRAY));
        return InteractionResult.CONSUME;
    }

    public int comparatorLevel() { return (int)((long)energy.getEnergyStored() * 15L / CAPACITY); }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); tag.putInt("Energy", energy.getEnergyStored()); tag.putLong("Cursor", cursor);
        ListTag list = new ListTag(); for (BlockPos pos : links) { CompoundTag row = new CompoundTag(); row.putLong("Pos", pos.asLong()); list.add(row); } tag.put("Links", list);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); energy.setEnergyStored(tag.getInt("Energy")); cursor = tag.getLong("Cursor"); links.clear();
        if (tag.contains("Links", Tag.TAG_LIST)) { ListTag list = tag.getList("Links", Tag.TAG_COMPOUND); for (int i=0;i<list.size() && links.size()<MAX_LINKS;i++) links.add(BlockPos.of(list.getCompound(i).getLong("Pos"))); }
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) { return cap == ForgeCapabilities.ENERGY ? energyCap.cast() : super.getCapability(cap, side); }
    @Override public void invalidateCaps() { super.invalidateCaps(); energyCap.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); energyCap = LazyOptional.of(() -> energy); }
}
