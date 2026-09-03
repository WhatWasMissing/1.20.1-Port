package matteroverdrive.client;

import matteroverdrive.android.AndroidData;

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

    private AndroidClientState() {
    }

    public static void set(boolean nextActive, int nextEnergy, int nextParts,
                           int nextSelectedAbility, int nextCooldownTicks, int nextActiveAbilityFlags, int nextExperience, int nextLevel,
                           long nextSelectedPerks) {
        active = nextActive;
        energy = Math.max(0, nextEnergy);
        parts = nextParts & 15;
        selectedAbility = Math.max(0, Math.min(AndroidData.Ability.values().length - 1, nextSelectedAbility));
        cooldownTicks = Math.max(0, nextCooldownTicks);
        activeAbilityFlags = nextActiveAbilityFlags & 3;
        experience = Math.max(0, nextExperience);
        level = Math.max(1, Math.min(AndroidData.MAX_LEVEL, nextLevel));
        selectedPerks = nextSelectedPerks;
    }

    public static boolean isActive() {
        return active;
    }

    public static int energy() {
        return energy;
    }

    public static int parts() {
        return parts;
    }

    public static int selectedAbility() {
        return selectedAbility;
    }

    public static String abilityName() {
        return AndroidData.Ability.values()[selectedAbility].displayName;
    }

    public static int cooldownTicks() {
        return cooldownTicks;
    }

    public static int experience() {
        return experience;
    }

    public static int level() {
        return level;
    }

    public static long selectedPerks() {
        return selectedPerks;
    }

    public static boolean hasPerk(AndroidData.Perk perk) {
        return (selectedPerks & (1L << perk.ordinal())) != 0L;
    }

    public static boolean isCloakEnabled() {
        return (activeAbilityFlags & 1) != 0;
    }

    public static boolean isForceFieldEnabled() {
        return (activeAbilityFlags & 2) != 0;
    }

    public static boolean isSelectedAbilityActive() {
        return (selectedAbility == AndroidData.Ability.CLOAK.ordinal() && isCloakEnabled())
                || (selectedAbility == AndroidData.Ability.FORCE_FIELD.ordinal() && isForceFieldEnabled());
    }
}
