package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.registry.OverhaulContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import javax.annotation.Nullable;

/** Compact fuelled reactor designed to participate directly in the existing Forge Energy cable network. */
public class QuantumFluxReactorBlockEntity extends BlockEntity {
    public static final int CAPACITY = 4_000_000;
    public static final int BASE_OUTPUT = 4_096;
    public static final int MAX_TRANSFER = 16_384;
    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, 0, MAX_TRANSFER, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private int fuelTicks;
    private int fuelStrength;
    private int heat;
    private int lastOutput;

    public QuantumFluxReactorBlockEntity(BlockPos pos, BlockState state) { super(OverhaulContent.QUANTUM_FLUX_REACTOR_BE.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, QuantumFluxReactorBlockEntity reactor) {
        if (reactor.fuelTicks > 0) {
            int generated = BASE_OUTPUT * Math.max(1, reactor.fuelStrength);
            reactor.energy.setEnergyStored(Math.min(CAPACITY, reactor.energy.getEnergyStored() + generated));
            reactor.fuelTicks--;
            reactor.heat = Math.min(10_000, reactor.heat + 3 + reactor.fuelStrength * 2);
            if (reactor.fuelTicks == 0) reactor.fuelStrength = 0;
        } else reactor.heat = Math.max(0, reactor.heat - 4);
        reactor.lastOutput = reactor.pushEnergy();
        if (level.getGameTime() % 20L == 0L) reactor.setChanged();
    }

    public boolean addFuel(int strength) {
        if (fuelTicks > 20 * 60 * 8) return false;
        int s = Math.max(1, Math.min(2, strength));
        fuelTicks += s == 2 ? 4_800 : 2_400;
        fuelStrength = Math.max(fuelStrength, s);
        setChanged();
        return true;
    }

    private int pushEnergy() {
        if (level == null || energy.getEnergyStored() <= 0) return 0;
        int moved = 0;
        for (Direction direction : Direction.values()) {
            BlockEntity target = level.getBlockEntity(worldPosition.relative(direction));
            if (target == null) continue;
            IEnergyStorage receiver = target.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (receiver == null || !receiver.canReceive()) continue;
            int offered = Math.min(MAX_TRANSFER - moved, energy.getEnergyStored());
            if (offered <= 0) break;
            int accepted = receiver.receiveEnergy(offered, true);
            if (accepted <= 0) continue;
            int extracted = energy.extractEnergy(accepted, false);
            int received = receiver.receiveEnergy(extracted, false);
            if (received < extracted) energy.setEnergyStored(energy.getEnergyStored() + extracted - received);
            moved += received;
            if (moved >= MAX_TRANSFER) break;
        }
        return moved;
    }

    public int getEnergyStored() { return energy.getEnergyStored(); }
    public int getCapacity() { return CAPACITY; }
    public int getHeat() { return heat; }
    public int getHeatPercent() { return heat / 100; }
    public int getFuelSeconds() { return fuelTicks / 20; }
    public int getLastOutput() { return lastOutput; }
    public boolean isHazardous() { return heat >= 7_000; }
    public boolean isCritical() { return heat >= 9_200; }

    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.putInt("Energy", energy.getEnergyStored()); tag.putInt("FuelTicks", fuelTicks); tag.putInt("FuelStrength", fuelStrength); tag.putInt("Heat", heat); }
    @Override public void load(CompoundTag tag) { super.load(tag); energy.setEnergyStored(tag.getInt("Energy")); fuelTicks=tag.getInt("FuelTicks"); fuelStrength=tag.getInt("FuelStrength"); heat=tag.getInt("Heat"); }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) { if (cap == ForgeCapabilities.ENERGY) return energyCap.cast(); return super.getCapability(cap, side); }
    @Override public void invalidateCaps(){super.invalidateCaps();energyCap.invalidate();}
    @Override public void reviveCaps(){super.reviveCaps();energyCap=LazyOptional.of(() -> energy);}
}
