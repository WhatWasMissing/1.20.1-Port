package matteroverdrive.android;

import matteroverdrive.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Persistent swappable hardware layer for converted Android players. */
public final class AndroidChassisData {
    private static final String ROOT = "MatterOverdriveAndroidChassis";

    public enum Slot { CORE, FRAME, MUSCLES, OPTICS, SHELL }

    public enum Module {
        CAPACITOR_CORE(Slot.CORE, "chassis_core_capacitor", "Capacitor Core"),
        OVERCLOCK_CORE(Slot.CORE, "chassis_core_overclock", "Overclock Core"),
        LIGHTWEIGHT_FRAME(Slot.FRAME, "chassis_frame_lightweight", "Lightweight Frame"),
        REINFORCED_FRAME(Slot.FRAME, "chassis_frame_reinforced", "Reinforced Frame"),
        AGILITY_MUSCLES(Slot.MUSCLES, "chassis_muscles_agility", "Agility Myomers"),
        SIEGE_MUSCLES(Slot.MUSCLES, "chassis_muscles_siege", "Siege Myomers"),
        HUNTER_OPTICS(Slot.OPTICS, "chassis_optics_hunter", "Hunter Optics"),
        PRECISION_OPTICS(Slot.OPTICS, "chassis_optics_precision", "Precision Optics"),
        STEALTH_SHELL(Slot.SHELL, "chassis_shell_stealth", "Stealth Shell"),
        REACTIVE_SHELL(Slot.SHELL, "chassis_shell_reactive", "Reactive Shell");

        public final Slot slot;
        public final String itemId;
        public final String displayName;
        Module(Slot slot, String itemId, String displayName) { this.slot = slot; this.itemId = itemId; this.displayName = displayName; }

        public static Module byItemId(String id) {
            for (Module module : values()) if (module.itemId.equals(id)) return module;
            return null;
        }
    }

    private AndroidChassisData() {}
    private static CompoundTag data(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT)) persistent.put(ROOT, new CompoundTag());
        return persistent.getCompound(ROOT);
    }
    private static void save(Player player, CompoundTag state) { player.getPersistentData().put(ROOT, state); }

    public static Module get(Player player, Slot slot) {
        String name = data(player).getString(slot.name());
        if (name.isBlank()) return null;
        try { return Module.valueOf(name); } catch (IllegalArgumentException ignored) { return null; }
    }

    public static boolean has(Player player, Module module) { return get(player, module.slot) == module; }

    /** Installs a module and returns the previously installed module, if any. */
    public static Module install(Player player, Module module) {
        if (!AndroidData.isAndroid(player) || module == null) return null;
        Module old = get(player, module.slot);
        CompoundTag state = data(player);
        state.putString(module.slot.name(), module.name());
        save(player, state);
        return old;
    }

    /** Removes and returns the module installed in a chassis slot. */
    public static Module remove(Player player, Slot slot) {
        if (!AndroidData.isAndroid(player) || slot == null) return null;
        Module old = get(player, slot);
        if (old == null) return null;
        CompoundTag state = data(player);
        state.remove(slot.name());
        save(player, state);
        return old;
    }

    public static ItemStack stack(Module module) { return module == null ? ItemStack.EMPTY : new ItemStack(ModItems.get(module.itemId).get()); }

    public static int energyCapacity(Player player) {
        int capacity = AndroidData.ENERGY_CAPACITY;
        if (has(player, Module.CAPACITOR_CORE)) capacity += 50_000;
        return capacity;
    }

    public static double recurringEnergyMultiplier(Player player) { return has(player, Module.OVERCLOCK_CORE) ? 1.10D : 1.0D; }

    public static float incomingDamageMultiplier(Player player) {
        float multiplier = 1.0F;
        if (has(player, Module.REINFORCED_FRAME)) multiplier *= 0.82F;
        if (has(player, Module.LIGHTWEIGHT_FRAME)) multiplier *= 1.08F;
        if (has(player, Module.REACTIVE_SHELL) && player.getHealth() <= player.getMaxHealth() * 0.5F) multiplier *= 0.82F;
        return multiplier;
    }

    public static float attackMultiplier(Player player) {
        if (has(player, Module.SIEGE_MUSCLES)) return 1.18F;
        if (has(player, Module.PRECISION_OPTICS)) return 1.10F;
        return 1.0F;
    }

    public static float movementMultiplier(Player player) {
        float multiplier = 1.0F;
        if (has(player, Module.LIGHTWEIGHT_FRAME)) multiplier *= 1.08F;
        if (has(player, Module.AGILITY_MUSCLES)) multiplier *= 1.10F;
        if (has(player, Module.SIEGE_MUSCLES)) multiplier *= 0.94F;
        return multiplier;
    }

    public static int passiveEnergyPerSecond(Player player) { return has(player, Module.OVERCLOCK_CORE) ? 350 : 0; }

    public static void copyTo(Player original, Player clone) {
        clone.getPersistentData().put(ROOT, original.getPersistentData().getCompound(ROOT).copy());
    }
}
