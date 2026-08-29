package matteroverdrive.capability;

import net.minecraftforge.energy.EnergyStorage;

/** Energy buffer with an opt-in infinite-energy mode for machine testing. */
public class MachineEnergyStorage extends EnergyStorage {
    private final Runnable changeListener;
    private boolean infiniteEnergy;

    public MachineEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable changeListener) {
        super(capacity, maxReceive, maxExtract);
        this.changeListener = changeListener;
    }

    @Override public int getEnergyStored() { return infiniteEnergy ? capacity : super.getEnergyStored(); }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (infiniteEnergy) return 0;
        int received = super.receiveEnergy(maxReceive, simulate);
        if (!simulate && received > 0) changed();
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (infiniteEnergy) return Math.min(Math.max(0, maxExtract), capacity);
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (!simulate && extracted > 0) changed();
        return extracted;
    }

    public boolean isInfiniteEnergy() { return infiniteEnergy; }

    public boolean toggleInfiniteEnergy() {
        setInfiniteEnergy(!infiniteEnergy);
        return infiniteEnergy;
    }

    public void setInfiniteEnergy(boolean enabled) {
        if (infiniteEnergy != enabled) {
            infiniteEnergy = enabled;
            changed();
        }
    }

    public void setCapacity(int capacity) {
        int clampedCapacity = Math.max(0, capacity);
        if (this.capacity != clampedCapacity) {
            this.capacity = clampedCapacity;
            if (energy > this.capacity) energy = this.capacity;
            changed();
        }
    }

    public void setEnergyStored(int amount) {
        int clamped = Math.max(0, Math.min(capacity, amount));
        if (energy != clamped) {
            energy = clamped;
            changed();
        }
    }

    private void changed() {
        if (changeListener != null) changeListener.run();
    }
}
