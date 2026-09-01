package matteroverdrive.client;

/** Client mirror of the local player's Android state, updated by the server packet. */
public final class AndroidClientState {
    private static boolean active;
    private static int energy;
    private static int parts;

    private AndroidClientState() {}
    public static void set(boolean nextActive, int nextEnergy, int nextParts) {
        active = nextActive; energy = Math.max(0, nextEnergy); parts = nextParts & 15;
    }
    public static boolean isActive() { return active; }
    public static int energy() { return energy; }
    public static int parts() { return parts; }
}