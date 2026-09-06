package matteroverdrive.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Recovered legacy MOTileEntityMachine redstone behavior. */
public final class MachineRedstoneMode {
    public static final int LOW = 0;
    public static final int HIGH = 1;
    public static final int DISABLED = 2;

    private MachineRedstoneMode() {}

    public static int sanitize(int mode) {
        return mode < LOW || mode > DISABLED ? DISABLED : mode;
    }

    public static int next(int mode) {
        return (sanitize(mode) + 1) % 3;
    }

    public static boolean allowsWork(Level level, BlockPos pos, int mode) {
        mode = sanitize(mode);
        if (mode == DISABLED) return true;
        boolean powered = level.hasNeighborSignal(pos);
        return mode == HIGH ? powered : !powered;
    }

    public static String label(int mode) {
        return switch (sanitize(mode)) {
            case LOW -> "LOW";
            case HIGH -> "HIGH";
            default -> "DISABLED";
        };
    }
}
