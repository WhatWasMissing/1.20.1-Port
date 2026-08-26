package matteroverdrive.capability;

import net.minecraftforge.energy.EnergyStorage;

public class MachineEnergyStorage extends EnergyStorage {
    private final Runnable changeListener;

    public MachineEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable changeListener) {
        super(capacity, maxReceive, maxExtract);
        this.changeListener = changeListener;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (!simulate && received > 0) {
            changed();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (!simulate && extracted > 0) {
            changed();
        }
        return extracted;
    }

    public void setEnergyStored(int amount) {
        int clamped = Math.max(0, Math.min(capacity, amount));
        if (energy != clamped) {
            energy = clamped;
            changed();
        }
    }

    private void changed() {
        if (changeListener != null) {
            changeListener.run();
        }
    }
}
