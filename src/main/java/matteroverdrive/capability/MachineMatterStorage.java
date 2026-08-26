package matteroverdrive.capability;

public class MachineMatterStorage implements IMatterStorage {
    private final int capacity;
    private final boolean allowReceive;
    private final boolean allowExtract;
    private final Runnable changeListener;
    private int stored;

    public MachineMatterStorage(int capacity, boolean allowReceive, boolean allowExtract, Runnable changeListener) {
        this.capacity = Math.max(0, capacity);
        this.allowReceive = allowReceive;
        this.allowExtract = allowExtract;
        this.changeListener = changeListener;
    }

    @Override
    public int receiveMatter(int maxReceive, boolean simulate) {
        if (!allowReceive || maxReceive <= 0) {
            return 0;
        }
        int accepted = Math.min(capacity - stored, maxReceive);
        if (!simulate && accepted > 0) {
            stored += accepted;
            changed();
        }
        return accepted;
    }

    @Override
    public int extractMatter(int maxExtract, boolean simulate) {
        if (!allowExtract || maxExtract <= 0) {
            return 0;
        }
        int extracted = Math.min(stored, maxExtract);
        if (!simulate && extracted > 0) {
            stored -= extracted;
            changed();
        }
        return extracted;
    }

    public int addMatterInternal(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int accepted = Math.min(capacity - stored, amount);
        if (!simulate && accepted > 0) {
            stored += accepted;
            changed();
        }
        return accepted;
    }

    public void setMatterStored(int amount) {
        int clamped = Math.max(0, Math.min(capacity, amount));
        if (stored != clamped) {
            stored = clamped;
            changed();
        }
    }

    @Override
    public int getMatterStored() {
        return stored;
    }

    @Override
    public int getMatterCapacity() {
        return capacity;
    }

    @Override
    public boolean canReceive() {
        return allowReceive;
    }

    @Override
    public boolean canExtract() {
        return allowExtract;
    }

    private void changed() {
        if (changeListener != null) {
            changeListener.run();
        }
    }
}
