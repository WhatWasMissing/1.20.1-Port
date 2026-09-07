package matteroverdrive.android;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/** Destiny-style swappable build layer on top of permanent Android progression. */
public final class AndroidLoadout {
    private static final String ROOT = "MatterOverdriveAndroidLoadout";
    private static final String ASPECTS = "Aspects";
    private static final String FRAGMENTS = "Fragments";
    public static final int MAX_ASPECTS = 2;
    public static final int MAX_FRAGMENT_CAPACITY = 5;

    public enum Aspect {
        VANGUARD_PROTOCOL("Vanguard Protocol", "Assault", 2,
                "Ability damage is increased and powered melee hits strike harder."),
        TEMPORAL_OVERDRIVE("Temporal Overdrive", "Assault", 3,
                "At high Android charge, combat systems overclock movement and damage."),
        AEGIS_WEAVE("Aegis Weave", "Chassis", 3,
                "Incoming damage is reduced; Force Field gains an additional defensive layer."),
        NANITE_BASTION("Nanite Bastion", "Chassis", 2,
                "Critical health activates an efficient powered self-repair routine."),
        HUNTER_ARRAY("Hunter-Killer Array", "Utility", 3,
                "Periodically marks nearby hostile targets through terrain."),
        RECURSIVE_CORE("Recursive Core", "Utility", 2,
                "Improves auxiliary charging and rewards ability damage with recovered FE.");

        public final String displayName;
        public final String branch;
        public final int fragmentSlots;
        public final String description;

        Aspect(String displayName, String branch, int fragmentSlots, String description) {
            this.displayName = displayName;
            this.branch = branch;
            this.fragmentSlots = fragmentSlots;
            this.description = description;
        }
    }

    public enum Fragment {
        AMPLITUDE("Fragment of Amplitude", "Ability hits deal 8% more damage."),
        WARDING("Fragment of Warding", "Reduce incoming damage by 5%."),
        BARRIER("Fragment of the Barrier", "Force Field reduces incoming damage by an additional 10%."),
        INDUCTION("Fragment of Induction", "Crouch-charging from held batteries gains an additional 256 FE/t."),
        FEEDBACK("Fragment of Feedback", "Damaging an enemy with an Android ability restores 150 FE."),
        RECOVERY("Fragment of Recovery", "Nanite repair restores additional health."),
        PREDATION("Fragment of Predation", "Hostile-target scans gain 8 blocks of range."),
        MOBILITY("Fragment of Mobility", "Powered Androids receive a persistent movement-speed boost."),
        RESOLVE("Fragment of Resolve", "Take 10% less damage while below 35% health."),
        VEIL("Fragment of the Veil", "Cloaking also increases movement speed."),
        SURGE("Fragment of Surge", "Above 75% Android FE, weapon and melee damage are increased by 5%."),
        CONSERVATION("Fragment of Conservation", "Passive Android loadout effects consume less FE."),
        SYNAPSE("Fragment of Synapse", "Ability damage has a chance to briefly accelerate movement."),
        BULWARK("Fragment of Bulwark", "While Force Field is active, gain knockback resistance through Resistance."));

        public final String displayName;
        public final String description;

        Fragment(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
    }

    private AndroidLoadout() {}

    private static CompoundTag data(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT)) persistent.put(ROOT, new CompoundTag());
        return persistent.getCompound(ROOT);
    }

    private static void save(Player player, CompoundTag state) {
        player.getPersistentData().put(ROOT, state);
    }

    public static int getAspectMask(Player player) {
        int validMask = (1 << Aspect.values().length) - 1;
        return data(player).getInt(ASPECTS) & validMask;
    }

    public static int getFragmentMask(Player player) {
        int validMask = (1 << Fragment.values().length) - 1;
        return data(player).getInt(FRAGMENTS) & validMask;
    }

    public static boolean hasAspect(Player player, Aspect aspect) {
        return (getAspectMask(player) & (1 << aspect.ordinal())) != 0;
    }

    public static boolean hasFragment(Player player, Fragment fragment) {
        return (getFragmentMask(player) & (1 << fragment.ordinal())) != 0;
    }

    public static int aspectCount(Player player) {
        return Integer.bitCount(getAspectMask(player));
    }

    public static int fragmentCount(Player player) {
        return Integer.bitCount(getFragmentMask(player));
    }

    public static int fragmentCapacity(Player player) {
        int slots = 0;
        int mask = getAspectMask(player);
        for (Aspect aspect : Aspect.values()) {
            if ((mask & (1 << aspect.ordinal())) != 0) slots += aspect.fragmentSlots;
        }
        return Math.min(MAX_FRAGMENT_CAPACITY, slots);
    }

    public static boolean toggleAspect(Player player, Aspect aspect) {
        if (!AndroidData.isAndroid(player)) return false;
        CompoundTag state = data(player);
        int mask = getAspectMask(player);
        int bit = 1 << aspect.ordinal();
        if ((mask & bit) != 0) {
            mask &= ~bit;
            state.putInt(ASPECTS, mask);
            trimFragmentsToCapacity(state, fragmentCapacityForMask(mask));
            save(player, state);
            return true;
        }
        if (Integer.bitCount(mask) >= MAX_ASPECTS) return false;
        state.putInt(ASPECTS, mask | bit);
        save(player, state);
        return true;
    }

    public static boolean toggleFragment(Player player, Fragment fragment) {
        if (!AndroidData.isAndroid(player)) return false;
        CompoundTag state = data(player);
        int mask = getFragmentMask(player);
        int bit = 1 << fragment.ordinal();
        if ((mask & bit) != 0) {
            state.putInt(FRAGMENTS, mask & ~bit);
            save(player, state);
            return true;
        }
        if (Integer.bitCount(mask) >= fragmentCapacity(player)) return false;
        state.putInt(FRAGMENTS, mask | bit);
        save(player, state);
        return true;
    }

    public static void clear(Player player) {
        player.getPersistentData().remove(ROOT);
    }

    public static void copyTo(Player original, Player clone) {
        if (original.getPersistentData().contains(ROOT)) {
            clone.getPersistentData().put(ROOT, original.getPersistentData().getCompound(ROOT).copy());
        }
    }

    private static int fragmentCapacityForMask(int aspectMask) {
        int slots = 0;
        for (Aspect aspect : Aspect.values()) {
            if ((aspectMask & (1 << aspect.ordinal())) != 0) slots += aspect.fragmentSlots;
        }
        return Math.min(MAX_FRAGMENT_CAPACITY, slots);
    }

    private static void trimFragmentsToCapacity(CompoundTag state, int capacity) {
        int mask = state.getInt(FRAGMENTS);
        while (Integer.bitCount(mask) > capacity) {
            int highest = 31 - Integer.numberOfLeadingZeros(mask);
            mask &= ~(1 << highest);
        }
        state.putInt(FRAGMENTS, mask);
    }
}
