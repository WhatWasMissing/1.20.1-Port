package matteroverdrive.client;

import matteroverdrive.android.AndroidData;
import matteroverdrive.android.AndroidLoadout;

/** Client mirror of the local player's Android state, updated by the server packet. */
public final class AndroidClientState {
    private static boolean active;
    private static int energy;
    private static int parts;
    private static int selectedAbility;
    private static int cooldownTicks;
    private static int activeAbilityFlags;
    private static int experience;
    private static int level;
    private static long selectedPerks;
    private static int aspectMask;
    private static int fragmentMask;
    private static int artifactOrdinal;
    private static int dronePerkMask;
    private static int specializationOrdinal;
    private static int ultimateCooldownTicks;
    private static int classAbilityCooldownTicks;
    private static int techAbilityCooldownTicks;

    private AndroidClientState() {}

    public static void set(boolean nextActive, int nextEnergy, int nextParts,
                           int nextSelectedAbility, int nextCooldownTicks, int nextActiveAbilityFlags,
                           int nextExperience, int nextLevel, long nextSelectedPerks,
                           int nextAspectMask, int nextFragmentMask, int nextArtifactOrdinal, int nextDronePerkMask,
                           int nextSpecializationOrdinal, int nextUltimateCooldownTicks,
                           int nextClassAbilityCooldownTicks, int nextTechAbilityCooldownTicks) {
        active = nextActive;
        energy = Math.max(0, nextEnergy);
        parts = nextParts & 15;
        selectedAbility = Math.max(0, Math.min(AndroidData.Ability.values().length - 1, nextSelectedAbility));
        cooldownTicks = Math.max(0, nextCooldownTicks);
        activeAbilityFlags = nextActiveAbilityFlags & 3;
        experience = Math.max(0, nextExperience);
        level = Math.max(1, Math.min(AndroidData.MAX_LEVEL, nextLevel));
        selectedPerks = nextSelectedPerks;
        aspectMask = nextAspectMask;
        fragmentMask = nextFragmentMask;
        artifactOrdinal = Math.max(0, Math.min(AndroidLoadout.Artifact.values().length - 1, nextArtifactOrdinal));
        dronePerkMask = nextDronePerkMask;
        specializationOrdinal = Math.max(0, Math.min(AndroidLoadout.Specialization.values().length - 1, nextSpecializationOrdinal));
        ultimateCooldownTicks = Math.max(0, nextUltimateCooldownTicks);
        classAbilityCooldownTicks = Math.max(0, nextClassAbilityCooldownTicks);
        techAbilityCooldownTicks = Math.max(0, nextTechAbilityCooldownTicks);
    }

    /** Keep screen/HUD cooldown readouts moving smoothly between authoritative server syncs. */
    public static void clientTickCooldowns() {
        if (cooldownTicks > 0) cooldownTicks--;
        if (ultimateCooldownTicks > 0) ultimateCooldownTicks--;
        if (classAbilityCooldownTicks > 0) classAbilityCooldownTicks--;
        if (techAbilityCooldownTicks > 0) techAbilityCooldownTicks--;
    }

    public static boolean isActive() { return active; }
    public static int energy() { return energy; }
    public static int parts() { return parts; }
    public static int selectedAbility() { return selectedAbility; }
    public static String abilityName() { return AndroidData.Ability.values()[selectedAbility].displayName; }
    public static int cooldownTicks() { return cooldownTicks; }
    public static int experience() { return experience; }
    public static int level() { return level; }
    public static long selectedPerks() { return selectedPerks; }
    public static int aspectMask() { return aspectMask; }
    public static int fragmentMask() { return fragmentMask; }
    public static int aspectCount() { return Integer.bitCount(aspectMask); }
    public static int fragmentCount() { return Integer.bitCount(fragmentMask); }
    public static int artifactOrdinal() { return artifactOrdinal; }
    public static AndroidLoadout.Artifact artifact() { return AndroidLoadout.Artifact.values()[artifactOrdinal]; }
    public static int dronePerkMask() { return dronePerkMask; }
    public static int specializationOrdinal() { return specializationOrdinal; }
    public static AndroidLoadout.Specialization specialization() { return AndroidLoadout.Specialization.values()[specializationOrdinal]; }
    public static AndroidLoadout.Ultimate ultimate() { return specialization().ultimate; }
    public static int ultimateCooldownTicks() { return ultimateCooldownTicks; }
    public static int classAbilityCooldownTicks() { return classAbilityCooldownTicks; }
    public static int techAbilityCooldownTicks() { return techAbilityCooldownTicks; }
    public static boolean ultimateReady() { return level >= 4 && ultimateCooldownTicks <= 0; }
    public static boolean classAbilityReady() { return level >= 2 && classAbilityCooldownTicks <= 0; }
    public static boolean techAbilityReady() { return level >= 2 && techAbilityCooldownTicks <= 0; }
    public static boolean hasPerk(AndroidData.Perk perk) { return (selectedPerks & (1L << perk.ordinal())) != 0L; }
    public static boolean hasAspect(AndroidLoadout.Aspect aspect) { return (aspectMask & (1 << aspect.ordinal())) != 0; }
    public static boolean hasFragment(AndroidLoadout.Fragment fragment) { return (fragmentMask & (1 << fragment.ordinal())) != 0; }
    public static boolean hasDronePerk(AndroidLoadout.DronePerk perk) { return (dronePerkMask & (1 << perk.ordinal())) != 0; }
    public static boolean isCloakEnabled() { return (activeAbilityFlags & 1) != 0; }
    public static boolean isForceFieldEnabled() { return (activeAbilityFlags & 2) != 0; }
    public static boolean isSelectedAbilityActive() {
        return (selectedAbility == AndroidData.Ability.CLOAK.ordinal() && isCloakEnabled())
                || (selectedAbility == AndroidData.Ability.FORCE_FIELD.ordinal() && isForceFieldEnabled());
    }
}
