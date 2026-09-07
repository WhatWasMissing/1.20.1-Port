package matteroverdrive.android;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/** Swappable subclass/Aspect/Fragment/Artifact/Drone-specialisation layer over permanent Android progression. */
public final class AndroidLoadout {
    private static final String ROOT = "MatterOverdriveAndroidLoadout";
    private static final String ASPECTS = "Aspects";
    private static final String FRAGMENTS = "Fragments";
    private static final String ARTIFACT = "Artifact";
    private static final String DRONE_PERKS = "DronePerks";
    private static final String SPECIALIZATION = "Specialization";
    public static final int MAX_ASPECTS = 2;
    public static final int MAX_FRAGMENT_CAPACITY = 6;
    public static final int MAX_DRONE_PERKS = 5;

    public enum Ultimate {
        SINGULARITY_CASCADE("Singularity Cascade", "Collapse the local combat space into an explosive kinetic pulse.", 45_000, 900),
        CITADEL_PROTOCOL("Citadel Protocol", "Become a mobile fortress with heavy resistance, absorption and nanite repair.", 35_000, 1_200),
        PHASE_DOMINION("Phase Dominion", "Enter an accelerated phase state while suppressing and exposing nearby hostiles.", 40_000, 1_000),
        OVERMIND_ASCENDANT("Overmind Ascendant", "Fully repair and overclock linked drones while exposing targets across the command radius.", 30_000, 900);

        public final String displayName;
        public final String description;
        public final int energyCost;
        public final int cooldownTicks;

        Ultimate(String displayName, String description, int energyCost, int cooldownTicks) {
            this.displayName = displayName;
            this.description = description;
            this.energyCost = energyCost;
            this.cooldownTicks = cooldownTicks;
        }
    }

    public enum Specialization {
        ASSAULT("Assault", "Aggressive ability damage, shockwave pressure and powered melee.", Ultimate.SINGULARITY_CASCADE),
        CHASSIS("Chassis", "Front-line durability, force-field control and nanite survival.", Ultimate.CITADEL_PROTOCOL),
        UTILITY("Utility", "Phase mobility, target acquisition and recursive energy support.", Ultimate.PHASE_DOMINION),
        DRONE_COMMANDER("Drone Commander", "Command, sustain and overclock linked synthetic drones.", Ultimate.OVERMIND_ASCENDANT);

        public final String displayName;
        public final String description;
        public final Ultimate ultimate;

        Specialization(String displayName, String description, Ultimate ultimate) {
            this.displayName = displayName;
            this.description = description;
            this.ultimate = ultimate;
        }
    }

    public enum Aspect {
        VANGUARD_PROTOCOL("Vanguard Protocol", "Assault", 2,
                "Ability damage is increased and powered melee hits strike harder."),
        TEMPORAL_OVERDRIVE("Temporal Overdrive", "Assault", 3,
                "At high Android charge, combat systems overclock movement and damage."),
        SINGULARITY_LATTICE("Singularity Lattice", "Assault", 2,
                "Shockwave and teleport effects gain stronger offensive follow-through."),
        AEGIS_WEAVE("Aegis Weave", "Chassis", 3,
                "Incoming damage is reduced; Force Field gains an additional defensive layer."),
        NANITE_BASTION("Nanite Bastion", "Chassis", 2,
                "Critical health activates an efficient powered self-repair routine."),
        REACTIVE_EXOSHELL("Reactive Exoshell", "Chassis", 2,
                "Taking repeated damage hardens the synthetic frame for a short period."),
        HUNTER_ARRAY("Hunter-Killer Array", "Utility", 3,
                "Periodically marks nearby hostile targets through terrain."),
        RECURSIVE_CORE("Recursive Core", "Utility", 2,
                "Improves auxiliary charging and rewards ability damage with recovered FE."),
        PHASE_NAVIGATOR("Phase Navigator", "Utility", 3,
                "Movement systems become more efficient after teleporting or cloaking."),
        COMMAND_UPLINK("Command Uplink", "Drone Commander", 3,
                "Linked drones inherit combat telemetry and gain improved targeting support."),
        SWARM_LOGIC("Swarm Logic", "Drone Commander", 3,
                "Multiple linked drones reinforce one another and recover faster near their operator."),
        GUARDIAN_DIRECTIVE("Guardian Directive", "Drone Commander", 2,
                "Defensive drones prioritise protecting their operator and become harder to destroy.");

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

        public Specialization specialization() {
            return switch (branch) {
                case "Chassis" -> Specialization.CHASSIS;
                case "Utility" -> Specialization.UTILITY;
                case "Drone Commander" -> Specialization.DRONE_COMMANDER;
                default -> Specialization.ASSAULT;
            };
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
        SYNAPSE("Fragment of Synapse", "Ability damage can briefly accelerate movement."),
        BULWARK("Fragment of Bulwark", "Force Field grants stronger knockback resistance through Resistance."),
        AFTERSHOCK("Fragment of Aftershock", "Shockwave ability damage is increased by a further 10%."),
        TRANSLOCATION("Fragment of Translocation", "Teleporting grants a short burst of speed."),
        OVERFLOW("Fragment of Overflow", "High Android FE improves passive regeneration efficiency."),
        HARMONICS("Fragment of Harmonics", "Ability hits have a chance to return additional FE."),
        SENTINEL("Fragment of the Sentinel", "Owned drones near you gain Resistance."),
        PACK_TACTICS("Fragment of Pack Tactics", "Owned drones deal more damage when several are deployed."),
        REPAIR_BEACON("Fragment of Repair Beacon", "Owned drones regenerate while close to their operator."),
        TARGET_LINK("Fragment of Target Link", "Owned drones deal increased damage to glowing targets."),
        ESCORT("Fragment of Escort", "You take slightly less damage while an owned drone is nearby."),
        ORDNANCE("Fragment of Ordnance", "Owned drone ranged attacks deal additional damage.");

        public final String displayName;
        public final String description;

        Fragment(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
    }

    public enum Artifact {
        NONE("No Artifact", "No seasonal artifact is equipped."),
        OVERCLOCKED_RELAY("Overclocked Relay", "Ability damage +5%; passive loadout FE costs +10%."),
        AEGIS_PRISM("Aegis Prism", "Incoming damage -5% while Force Field is active."),
        NANITE_CROWN("Nanite Crown", "Low-health self repair is stronger and more efficient."),
        HUNTER_LENS("Hunter Lens", "Hunter Array marks farther and marked targets take more drone damage."),
        PHASE_ANCHOR("Phase Anchor", "Teleport and cloak movement bonuses last longer."),
        SWARM_BEACON("Swarm Beacon", "Owned drones near you gain regeneration and attack support."),
        CAPACITOR_HEART("Capacitor Heart", "Ability hits return additional FE while above half charge.");

        public final String displayName;
        public final String description;

        Artifact(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
    }

    public enum DronePerk {
        COMMAND_AUTHORITY(2, "Command Authority", "Owned drones remain effective at greater range."),
        TARGETING_SUITE(3, "Targeting Suite", "Owned drone attacks deal 12% more damage."),
        REINFORCED_DRONES(4, "Reinforced Drones", "Owned drones gain periodic Resistance near you."),
        FIELD_REPAIR(5, "Field Repair", "Owned drones slowly regenerate near their operator."),
        HUNTER_NETWORK(6, "Hunter Network", "Owned drones deal more damage to glowing targets."),
        SWARM_COHESION(7, "Swarm Cohesion", "Deploying multiple drones increases their damage."),
        ESCORT_PROTOCOL(8, "Escort Protocol", "Nearby owned drones slightly reduce damage you take."),
        ORDNANCE_LINK(9, "Ordnance Link", "Drone ranged attacks gain a further damage multiplier."),
        OVERMIND(10, "Synthetic Overmind", "DRONE CAPSTONE: linked drones gain damage, Resistance and regeneration near you.");

        public final int level;
        public final String displayName;
        public final String description;

        DronePerk(int level, String displayName, String description) {
            this.level = level;
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

    public static int getDronePerkMask(Player player) {
        int validMask = (1 << DronePerk.values().length) - 1;
        return data(player).getInt(DRONE_PERKS) & validMask;
    }

    public static Artifact getArtifact(Player player) {
        int ordinal = data(player).getInt(ARTIFACT);
        if (ordinal < 0 || ordinal >= Artifact.values().length) return Artifact.NONE;
        return Artifact.values()[ordinal];
    }

    public static Specialization getSpecialization(Player player) {
        CompoundTag state = data(player);
        if (state.contains(SPECIALIZATION)) {
            int ordinal = state.getInt(SPECIALIZATION);
            if (ordinal >= 0 && ordinal < Specialization.values().length) return Specialization.values()[ordinal];
        }
        int mask = getAspectMask(player);
        if (mask != 0) {
            for (Aspect aspect : Aspect.values()) {
                if ((mask & (1 << aspect.ordinal())) != 0) return aspect.specialization();
            }
        }
        return Specialization.ASSAULT;
    }

    public static boolean selectSpecialization(Player player, Specialization specialization) {
        if (!AndroidData.isAndroid(player) || specialization == null) return false;
        CompoundTag state = data(player);
        Specialization current = getSpecialization(player);
        state.putInt(SPECIALIZATION, specialization.ordinal());
        if (current != specialization) {
            int nextMask = 0;
            for (Aspect aspect : Aspect.values()) {
                if (aspect.specialization() == specialization && (getAspectMask(player) & (1 << aspect.ordinal())) != 0) {
                    nextMask |= 1 << aspect.ordinal();
                }
            }
            state.putInt(ASPECTS, nextMask);
            trimFragmentsToCapacity(state, fragmentCapacityForMask(nextMask));
        }
        save(player, state);
        return true;
    }

    public static boolean hasAspect(Player player, Aspect aspect) {
        return (getAspectMask(player) & (1 << aspect.ordinal())) != 0;
    }

    public static boolean hasFragment(Player player, Fragment fragment) {
        return (getFragmentMask(player) & (1 << fragment.ordinal())) != 0;
    }

    public static boolean hasArtifact(Player player, Artifact artifact) {
        return artifact != Artifact.NONE && getArtifact(player) == artifact;
    }

    public static boolean hasDronePerk(Player player, DronePerk perk) {
        return (getDronePerkMask(player) & (1 << perk.ordinal())) != 0;
    }

    public static int aspectCount(Player player) { return Integer.bitCount(getAspectMask(player)); }
    public static int fragmentCount(Player player) { return Integer.bitCount(getFragmentMask(player)); }
    public static int dronePerkCount(Player player) { return Integer.bitCount(getDronePerkMask(player)); }

    public static int fragmentCapacity(Player player) {
        return fragmentCapacityForMask(getAspectMask(player));
    }

    public static boolean toggleAspect(Player player, Aspect aspect) {
        if (!AndroidData.isAndroid(player) || aspect.specialization() != getSpecialization(player)) return false;
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

    public static boolean selectArtifact(Player player, Artifact artifact) {
        if (!AndroidData.isAndroid(player) || artifact == null) return false;
        CompoundTag state = data(player);
        state.putInt(ARTIFACT, artifact.ordinal());
        save(player, state);
        return true;
    }

    public static boolean toggleDronePerk(Player player, DronePerk perk) {
        if (!AndroidData.isAndroid(player) || AndroidData.getLevel(player) < perk.level) return false;
        CompoundTag state = data(player);
        int mask = getDronePerkMask(player);
        int bit = 1 << perk.ordinal();
        if ((mask & bit) != 0) {
            int nextMask = mask;
            for (DronePerk candidate : DronePerk.values()) {
                if (candidate.level >= perk.level) nextMask &= ~(1 << candidate.ordinal());
            }
            state.putInt(DRONE_PERKS, nextMask);
            save(player, state);
            return true;
        }
        if (Integer.bitCount(mask) >= MAX_DRONE_PERKS) return false;
        for (DronePerk prior : DronePerk.values()) {
            if (prior.level < perk.level && (mask & (1 << prior.ordinal())) == 0) return false;
        }
        state.putInt(DRONE_PERKS, mask | bit);
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
