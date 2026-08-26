package matteroverdrive.capability;

public interface IMatterStorage {
    int receiveMatter(int maxReceive, boolean simulate);

    int extractMatter(int maxExtract, boolean simulate);

    int getMatterStored();

    int getMatterCapacity();

    boolean canReceive();

    boolean canExtract();
}
