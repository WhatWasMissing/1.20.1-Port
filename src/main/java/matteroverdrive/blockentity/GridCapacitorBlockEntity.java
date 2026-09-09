package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.registry.ModExtraBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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

/** Large bidirectional FE surge buffer for Matter Overdrive power grids. */
public class GridCapacitorBlockEntity extends BlockEntity {
    public static final int CAPACITY = 16_000_000;
    public static final int TRANSFER = 32_768;
    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, TRANSFER, TRANSFER, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private int lastInput, lastOutput;
    private int outputCursor;

    public GridCapacitorBlockEntity(BlockPos pos, BlockState state) { super(ModExtraBlockEntities.GRID_CAPACITOR.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GridCapacitorBlockEntity node) {
        node.lastInput = node.pullEnergy();
        node.lastOutput = node.pushEnergy();
    }

    private int pullEnergy() {
        if (level == null || energy.getEnergyStored() >= energy.getMaxEnergyStored()) return 0;
        int remaining = Math.min(TRANSFER, energy.getMaxEnergyStored() - energy.getEnergyStored());
        int total = 0;
        for (Direction direction : Direction.values()) {
            if (remaining <= 0) break;
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null || neighbor instanceof GridCapacitorBlockEntity) continue;
            IEnergyStorage source = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (source == null || !source.canExtract()) continue;
            int offered = source.extractEnergy(remaining, true);
            int accepted = energy.receiveEnergy(offered, true);
            if (accepted <= 0) continue;
            int extracted = source.extractEnergy(accepted, false);
            int received = energy.receiveEnergy(extracted, false);
            total += received;
            remaining -= received;
        }
        return total;
    }

    private int pushEnergy() {
        if (level == null || energy.getEnergyStored() <= 0) return 0;
        Direction[] dirs = Direction.values();
        int budget = Math.min(TRANSFER, energy.getEnergyStored());
        int total = 0;
        for (int i = 0; i < dirs.length && budget > 0; i++) {
            Direction direction = dirs[(outputCursor + i) % dirs.length];
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null || neighbor instanceof GridCapacitorBlockEntity) continue;
            IEnergyStorage receiver = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (receiver == null || !receiver.canReceive()) continue;
            int accepted = receiver.receiveEnergy(budget, true);
            if (accepted <= 0) continue;
            int extracted = energy.extractEnergy(accepted, false);
            int received = receiver.receiveEnergy(extracted, false);
            if (received < extracted) energy.setEnergyStored(energy.getEnergyStored() + extracted - received);
            total += received;
            budget -= received;
        }
        outputCursor = (outputCursor + 1) % dirs.length;
        return total;
    }

    public InteractionResult onUse(ServerPlayer player, InteractionHand hand) {
        player.sendSystemMessage(Component.literal("GRID CAPACITOR  " + energy.getEnergyStored() + " / " + CAPACITY + " FE")
                .withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("Last tick: +" + lastInput + " / -" + lastOutput + " FE | surge headroom " + (CAPACITY - energy.getEnergyStored()))
                .withStyle(ChatFormatting.GRAY));
        return InteractionResult.CONSUME;
    }

    public int comparatorLevel() { return CAPACITY <= 0 ? 0 : Math.min(15, (int)((long)energy.getEnergyStored() * 15L / CAPACITY)); }
    public MachineEnergyStorage getEnergy() { return energy; }

    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.putInt("Energy", energy.getEnergyStored()); tag.putInt("OutputCursor", outputCursor); }
    @Override public void load(CompoundTag tag) { super.load(tag); energy.setEnergyStored(tag.getInt("Energy")); outputCursor = Math.floorMod(tag.getInt("OutputCursor"), 6); }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) { return cap == ForgeCapabilities.ENERGY ? energyCap.cast() : super.getCapability(cap, side); }
    @Override public void invalidateCaps() { super.invalidateCaps(); energyCap.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); energyCap = LazyOptional.of(() -> energy); }
}