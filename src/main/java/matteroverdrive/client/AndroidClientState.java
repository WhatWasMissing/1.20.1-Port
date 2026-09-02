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

    private AndroidClientState() {
    }

    public static void set(boolean nextActive, int nextEnergy, int nextParts,
                           int nextSelectedAbility, int nextCooldownTicks, int nextActiveAbilityFlags) {
        active = nextActive;
        energy = Math.max(0, nextEnergy);
        parts = nextParts & 15;
        selectedAbility = Math.max(0, Math.min(AndroidData.Ability.values().length - 1, nextSelectedAbility));
        cooldownTicks = Math.max(0, nextCooldownTicks);
        activeAbilityFlags = nextActiveAbilityFlags & 3;
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
