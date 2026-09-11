package matteroverdrive.android;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/** Swappable subclass/Aspect/Fragment/passive/Drone-specialisation layer over permanent Android progression. */
public final class AndroidLoadout {
    private static final String ROOT = "MatterOverdriveAndroidLoadout";
    private static final String ASPECTS = "Aspects";
    private static final String FRAGMENTS = "Fragments";
    /* Kept as Artifact in NBT for save compatibility; player-facing UI calls this the Passive Protocol. */
    private static final String ARTIFACT = "Artifact";
    private static final String DRONE_PERKS = "DronePerks";
    private static final String SPECIALIZATION = "Specialization";
    public static final int MAX_ASPECTS = 2;
    public static final int MAX_FRAGMENT_CAPACITY = 6;
    public static final int MAX_DRONE_PERKS = 9;

    public enum Ultimate {
        SINGULARITY_CASCADE("Singularity Cascade", "12-block kinetic detonation: 16-33 damage by distance, heavy launch, Weakness III and Slowness II.", 34_000, 800),
        CITADEL_PROTOCOL("Citadel Protocol", "25s Resistance IV, Absorption V, Regeneration III and Fire Resistance; 15s Strength II plus an 8-block defensive repulse.", 28_000, 1_000),
        PHASE_DOMINION("Phase Dominion", "20s Speed IV, cloak and Resistance II; suppress enemies in 24 blocks and immediately restore 4,000 FE.", 30_000, 850),
        OVERMIND_ASCENDANT("Overmind Ascendant", "Fully heal drones in 40 blocks and grant 30s Strength IV, Resistance IV, Regeneration III, Speed III and Absorption III.", 26_000, 800),
        EXECUTION_LATTICE("Execution Lattice", "Strike every hostile within 28 blocks for 18 base damage plus marked-target and missing-health execution damage; refund FE per target.", 32_000, 850),
        RAILSTORM_PROTOCOL("Railstorm Protocol", "40-block forward precision volley dealing 40 damage, or 52 to marked targets, then grants 10s Strength III.", 34_000, 850),
        SIEGE_ENGINE("Siege Engine", "20s Resistance III, Strength III and Absorption IV plus a 12-block 24-damage breach blast.", 36_000, 900),
        NANITE_BLOOM("Nanite Bloom", "20-block corrosion field: 12 damage, Poison IV, Weakness III and allied repair; heals you 12 HP and empowers drones.", 30_000, 850),
        EVENT_HORIZON("Event Horizon", "22-block gravity collapse: drag enemies inward, deal 22 damage, apply Slowness V + Weakness III, and gain 12s Resistance III.", 35_000, 900);

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
        ASSAULT("Singularity Breaker", "Aggressive shockwave pressure, powered melee and close-range collapse effects.", Ultimate.SINGULARITY_CASCADE),
        CHASSIS("Citadel", "Front-line durability, force-field control and nanite survival.", Ultimate.CITADEL_PROTOCOL),
        UTILITY("Phase Stalker", "Phase mobility, cloak, target acquisition and energy recovery.", Ultimate.PHASE_DOMINION),
        DRONE_COMMANDER("Drone Commander", "Command, sustain and overclock linked synthetic drones.", Ultimate.OVERMIND_ASCENDANT),
        HUNTER_KILLER("Hunter-Killer", "Marked-target pursuit, execution windows and aggressive target chaining.", Ultimate.EXECUTION_LATTICE),
        PRECISION_FRAME("Precision Frame", "Long-range target prediction, disciplined positioning and high-value precision bursts.", Ultimate.RAILSTORM_PROTOCOL),
        SIEGE_FRAME("Siege Frame", "Heavy ordnance, advancing pressure and sustained battlefield suppression.", Ultimate.SIEGE_ENGINE),
        NANITE_WEAVER("Nanite Weaver", "Regeneration, corrosion and adaptive nanite control across allies and enemies.", Ultimate.NANITE_BLOOM),
        GRAVITY_CORE("Gravity Core", "Mass manipulation, crowd control and localized gravitational collapse.", Ultimate.EVENT_HORIZON);

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
        VANGUARD_PROTOCOL("Vanguard Protocol", "Assault", 2, "Android ability damage +25%; other direct player damage +15%."),
        TEMPORAL_OVERDRIVE("Temporal Overdrive", "Assault", 3, "Above 75% FE, spend about 100 FE/s to maintain Speed III and Strength II."),
        SINGULARITY_LATTICE("Singularity Lattice", "Assault", 2, "Shockwave gains +4 damage, +2 blocks radius and stronger control; Teleport creates a 6-block 8-damage arrival burst."),
        AEGIS_WEAVE("Aegis Weave", "Chassis", 3, "Reduce all incoming damage by 18%, stacking with Force Field and other defensive fragments."),
        NANITE_BASTION("Nanite Bastion", "Chassis", 2, "Below 55% health, spend about 240 FE/s to repair 3 HP/s before Recovery/Nanite passive bonuses."),
        REACTIVE_EXOSHELL("Reactive Exoshell", "Chassis", 2, "When damaged, trigger 6s Resistance III + Absorption II; can retrigger every 8s."),
        HUNTER_ARRAY("Hunter-Killer Array", "Utility", 3, "Every 2s, scan 26 blocks and mark hostiles for 4.5s while applying 3s Weakness I."),
        RECURSIVE_CORE("Recursive Core", "Utility", 2, "Held-battery crouch charging gains +512 FE/t; every damaging Android ability hit restores 250 FE."),
        PHASE_NAVIGATOR("Phase Navigator", "Utility", 3, "Teleport gains +6 blocks range, 20% shorter cooldown, stronger post-blink buffs and +1,000 FE; cloak exit is also stronger."),
        COMMAND_UPLINK("Command Uplink", "Drone Commander", 3, "Owned drone damage +15% and command-support effects are reinforced."),
        SWARM_LOGIC("Swarm Logic", "Drone Commander", 3, "With multiple drones, fleet damage scales up and nearby drone repair is strengthened."),
        GUARDIAN_DIRECTIVE("Guardian Directive", "Drone Commander", 2, "Nearby owned drones continuously receive at least Resistance II."),
        PREDATOR_CHAIN("Predator Chain", "Hunter-Killer", 3, "Damage against glowing targets +20% and each hit restores 250 FE."),
        EXECUTION_ROUTER("Execution Router", "Hunter-Killer", 2, "Deal +20% damage to glowing targets or enemies at/below 40% health."),
        STABILIZED_OPTICS("Stabilized Optics", "Precision Frame", 3, "Deal +22% damage to glowing targets."),
        BALLISTIC_PREDICTION("Ballistic Prediction", "Precision Frame", 2, "While nearly stationary, every 2s scan 36 blocks to mark targets and apply Weakness II."),
        HEAVY_ORDNANCE("Heavy Ordnance", "Siege Frame", 3, "Non-ability weapon/melee damage +20%."),
        MOBILE_FORTRESS("Mobile Fortress", "Siege Frame", 2, "Above 50% FE, maintain Resistance II and periodically refresh Absorption I."),
        REPAIR_SWARM("Repair Swarm", "Nanite Weaver", 3, "Repair yourself for 2 HP every 2s when hurt; nearby drones repair 1.5 HP/s."),
        CORROSIVE_CLOUD("Corrosive Cloud", "Nanite Weaver", 2, "Damaging Android abilities apply 6s Poison II and 5s Weakness I."),
        MASS_DRIVER("Mass Driver", "Gravity Core", 3, "Android ability damage +22% and ability hits strongly displace enemies."),
        GRAVITIC_WELL("Gravitic Well", "Gravity Core", 2, "Every 2s, hostiles within 11 blocks are pulled inward and receive 3.5s Slowness III.");

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
                case "Hunter-Killer" -> Specialization.HUNTER_KILLER;
                case "Precision Frame" -> Specialization.PRECISION_FRAME;
                case "Siege Frame" -> Specialization.SIEGE_FRAME;
                case "Nanite Weaver" -> Specialization.NANITE_WEAVER;
                case "Gravity Core" -> Specialization.GRAVITY_CORE;
                default -> Specialization.ASSAULT;
            };
        }
    }

    public enum Fragment {
        AMPLITUDE("Fragment of Amplitude", "Android ability damage +15%."),
        WARDING("Fragment of Warding", "Reduce incoming damage by 10%."),
        BARRIER("Fragment of the Barrier", "While Force Field is active, reduce remaining incoming damage by another 15%."),
        INDUCTION("Fragment of Induction", "Held-battery crouch charging gains +384 FE/t."),
        FEEDBACK("Fragment of Feedback", "Every damaging Android ability hit restores 350 FE."),
        RECOVERY("Fragment of Recovery", "Nanite Bastion repair increases from 3 to 4.5 HP/s before other bonuses."),
        PREDATION("Fragment of Predation", "Hunter Array scan range gains 10 blocks."),
        MOBILITY("Fragment of Mobility", "Powered Androids continuously receive Speed II."),
        RESOLVE("Fragment of Resolve", "Reduce incoming damage by another 18% while at/below 35% health."),
        VEIL("Fragment of the Veil", "While cloaked, maintain Speed III; Phase Stability raises this to Speed IV."),
        SURGE("Fragment of Surge", "Above 75% Android FE, player damage +10%."),
        CONSERVATION("Fragment of Conservation", "Recurring Android loadout effects cost 35% less FE."),
        SYNAPSE("Fragment of Synapse", "Damaging ability hits have a 45% chance to grant 3.5s Speed III."),
        BULWARK("Fragment of Bulwark", "While Force Field is active, continuously gain Resistance III."),
        AFTERSHOCK("Fragment of Aftershock", "Damaging Android abilities deal +15% damage."),
        TRANSLOCATION("Fragment of Translocation", "After Teleport, gain an extra level of Speed and 2 extra seconds of post-blink mobility."),
        OVERFLOW("Fragment of Overflow", "Above 75% FE, Nanite Bastion repairs an additional 1 HP/s."),
        HARMONICS("Fragment of Harmonics", "Every damaging Android ability hit restores 200 FE."),
        SENTINEL("Fragment of the Sentinel", "Owned drones near you continuously gain Resistance II or better."),
        PACK_TACTICS("Fragment of Pack Tactics", "With multiple drones deployed, fleet damage scales upward with drone count."),
        REPAIR_BEACON("Fragment of Repair Beacon", "Owned drones regenerate while close to their operator."),
        TARGET_LINK("Fragment of Target Link", "Owned drones deal +20% damage to glowing targets."),
        ESCORT("Fragment of Escort", "Take 10% less damage while an owned drone is nearby."),
        ORDNANCE("Fragment of Ordnance", "Owned drone damage +20%.");

        public final String displayName;
        public final String description;
        Fragment(String displayName, String description) { this.displayName = displayName; this.description = description; }
    }

    /** Internal enum name retained for save/network compatibility; these are selectable passive protocols. */
    public enum Artifact {
        NONE("No Passive", "No additional passive protocol is selected."),
        OVERCLOCKED_RELAY("Overclock Protocol", "Ability damage +18% and each damaging ability hit restores 150 FE; recurring loadout effects cost 8% more FE."),
        AEGIS_PRISM("Aegis Protocol", "Reduce all incoming damage by 8%; while Force Field is active, reduce the remaining damage by another 18%."),
        NANITE_CROWN("Nanite Recovery", "Makes recurring repair cheaper and substantially stronger; below 70% health it also repairs 2 HP every 2s for 100 FE."),
        HUNTER_LENS("Hunter Protocol", "Hunter Array range +18 blocks; marked targets take +15% player damage and +20% drone damage."),
        PHASE_ANCHOR("Phase Stability", "Cloak movement from Fragment of the Veil becomes Speed IV and lasts longer."),
        SWARM_BEACON("Swarm Support", "Nearby drones gain stronger resistance and repair, plus +18% damage."),
        CAPACITOR_HEART("Capacitor Feedback", "Damaging ability hits restore 400 FE at high charge; while above 50% FE, regenerate 250 FE/s."),
        THERMAL_LATTICE("Thermal Lattice", "Energy weapon shots generate 35% less heat and cool twice as quickly while carried."),
        REACTOR_SYMBIOTE("Reactor Symbiote", "A carried linked Reactor Remote can draw 1,200 FE/s from its loaded, running Fusion Reactor into your Android reserve.");

        public final String displayName;
        public final String description;
        Artifact(String displayName, String description) { this.displayName = displayName; this.description = description; }
    }

    public enum DronePerk {
        COMMAND_AUTHORITY(2, "Command Authority", "Extends the effective command/support envelope for your owned drone fleet."),
        TARGETING_SUITE(3, "Targeting Suite", "Owned drone damage +18%."),
        REINFORCED_DRONES(4, "Reinforced Drones", "Owned drones near you continuously gain Resistance II."),
        FIELD_REPAIR(5, "Field Repair", "Owned drones regenerate near their operator."),
        HUNTER_NETWORK(6, "Hunter Network", "Owned drones gain the marked-target damage bonus against glowing enemies."),
        SWARM_COHESION(7, "Swarm Cohesion", "Multiple deployed drones increase fleet damage, scaling with fleet size."),
        ESCORT_PROTOCOL(8, "Escort Protocol", "Take 10% less damage while an owned drone is nearby."),
        ORDNANCE_LINK(9, "Ordnance Link", "Owned drone damage gains another +22% multiplier."),
        OVERMIND(10, "Synthetic Overmind", "DRONE CAPSTONE: drone damage +25% and nearby drones gain stronger Resistance and regeneration.");

        public final int level;
        public final String displayName;
        public final String description;
        DronePerk(int level, String displayName, String description) { this.level = level; this.displayName = displayName; this.description = description; }
    }

    private AndroidLoadout() {}

    private static CompoundTag data(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT)) persistent.put(ROOT, new CompoundTag());
        return persistent.getCompound(ROOT);
    }

    private static void save(Player player, CompoundTag state) { player.getPersistentData().put(ROOT, state); }

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
        if (mask != 0) for (Aspect aspect : Aspect.values()) if ((mask & (1 << aspect.ordinal())) != 0) return aspect.specialization();
        return Specialization.ASSAULT;
    }

    public static boolean selectSpecialization(Player player, Specialization specialization) {
        if (!AndroidData.isAndroid(player) || specialization == null) return false;
        CompoundTag state = data(player);
        Specialization current = getSpecialization(player);
        state.putInt(SPECIALIZATION, specialization.ordinal());
        if (current != specialization) {
            int nextMask = 0;
            for (Aspect aspect : Aspect.values()) if (aspect.specialization() == specialization && (getAspectMask(player) & (1 << aspect.ordinal())) != 0) nextMask |= 1 << aspect.ordinal();
            state.putInt(ASPECTS, nextMask);
            trimFragmentsToCapacity(state, fragmentCapacityForMask(nextMask));
        }
        save(player, state);
        return true;
    }

    public static boolean hasAspect(Player player, Aspect aspect) { return (getAspectMask(player) & (1 << aspect.ordinal())) != 0; }
    public static boolean hasFragment(Player player, Fragment fragment) { return (getFragmentMask(player) & (1 << fragment.ordinal())) != 0; }
    public static boolean hasArtifact(Player player, Artifact artifact) { return artifact != Artifact.NONE && getArtifact(player) == artifact; }
    public static boolean hasDronePerk(Player player, DronePerk perk) { return (getDronePerkMask(player) & (1 << perk.ordinal())) != 0; }
    public static int aspectCount(Player player) { return Integer.bitCount(getAspectMask(player)); }
    public static int fragmentCount(Player player) { return Integer.bitCount(getFragmentMask(player)); }
    public static int dronePerkCount(Player player) { return Integer.bitCount(getDronePerkMask(player)); }
    public static int fragmentCapacity(Player player) { return fragmentCapacityForMask(getAspectMask(player)); }

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
        if ((mask & bit) != 0) { state.putInt(FRAGMENTS, mask & ~bit); save(player, state); return true; }
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
            for (DronePerk candidate : DronePerk.values()) if (candidate.level >= perk.level) nextMask &= ~(1 << candidate.ordinal());
            state.putInt(DRONE_PERKS, nextMask);
            save(player, state);
            return true;
        }
        if (Integer.bitCount(mask) >= MAX_DRONE_PERKS) return false;
        for (DronePerk prior : DronePerk.values()) if (prior.level < perk.level && (mask & (1 << prior.ordinal())) == 0) return false;
        state.putInt(DRONE_PERKS, mask | bit);
        save(player, state);
        return true;
    }

    public static void clear(Player player) { player.getPersistentData().remove(ROOT); }

    public static void copyTo(Player original, Player clone) {
        if (original.getPersistentData().contains(ROOT)) clone.getPersistentData().put(ROOT, original.getPersistentData().getCompound(ROOT).copy());
    }

    private static int fragmentCapacityForMask(int aspectMask) {
        int slots = 0;
        for (Aspect aspect : Aspect.values()) if ((aspectMask & (1 << aspect.ordinal())) != 0) slots += aspect.fragmentSlots;
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
