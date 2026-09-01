package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;

import java.lang.reflect.Field;

public final class RemainingBalanceFixes {
    private static final int REACTOR_OUTPUT_MULTIPLIER = 8;
    private static final long LIVING_MASS_MULTIPLIER = 64L;

    private static final Field ANOMALY_MASS = field(GravitationalAnomalyBlockEntity.class, "mass");
    private static final Field REACTOR_ENERGY = field(FusionReactorControllerBlockEntity.class, "energy");
    private static final Field REACTOR_OUTPUT_POTENTIAL = field(FusionReactorControllerBlockEntity.class, "outputPotential");
    private static final Field REACTOR_GENERATED_LAST_TICK = field(FusionReactorControllerBlockEntity.class, "generatedLastTick");

    private RemainingBalanceFixes() {
    }

    static void boostConsumedEntityMass(GravitationalAnomalyBlockEntity anomaly) {
        int consumedEntities = anomaly.getLastConsumedEntityCount();
        int consumedMatter = anomaly.getLastConsumedMatter();
        if (consumedEntities <= 0 || consumedMatter <= 0) {
            return;
        }

        try {
            long currentMass = ANOMALY_MASS.getLong(anomaly);
            long bonus = Math.multiplyExact((long) consumedMatter, LIVING_MASS_MULTIPLIER - 1L);
            long nextMass;
            try {
                nextMass = Math.addExact(currentMass, bonus);
            } catch (ArithmeticException overflow) {
                nextMass = Long.MAX_VALUE;
            }
            ANOMALY_MASS.setLong(anomaly, nextMass);
            anomaly.setChanged();
        } catch (IllegalAccessException | ArithmeticException ignored) {
        }
    }

    public static void boostFusionOutput(FusionReactorControllerBlockEntity reactor) {
        try {
            MachineEnergyStorage energy = (MachineEnergyStorage) REACTOR_ENERGY.get(reactor);
            int generated = REACTOR_GENERATED_LAST_TICK.getInt(reactor);
            int potential = REACTOR_OUTPUT_POTENTIAL.getInt(reactor);

            REACTOR_OUTPUT_POTENTIAL.setInt(reactor, multiplyClamped(potential, REACTOR_OUTPUT_MULTIPLIER));
            if (generated <= 0) {
                return;
            }

            int desiredTotal = multiplyClamped(generated, REACTOR_OUTPUT_MULTIPLIER);
            int extraWanted = Math.max(0, desiredTotal - generated);
            int room = Math.max(0, energy.getMaxEnergyStored() - energy.getEnergyStored());
            int extra = Math.min(extraWanted, room);
            if (extra > 0) {
                energy.setEnergyStored(energy.getEnergyStored() + extra);
                REACTOR_GENERATED_LAST_TICK.setInt(reactor, generated + extra);
                reactor.setChanged();
            }
        } catch (IllegalAccessException ignored) {
        }
    }

    private static int multiplyClamped(int value, int multiplier) {
        long result = (long) value * multiplier;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, result));
    }

    private static Field field(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
