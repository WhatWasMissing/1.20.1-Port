package matteroverdrive.android;

import matteroverdrive.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Persistent Android state owned by a player rather than a block. */
public final class AndroidData {
    public static final int ENERGY_CAPACITY = 100_000;\n    public static final int MAX_LEVEL = 10;\n    private static final String EXPERIENCE = "Experience";
    private static final String ROOT = "MatterOverdriveAndroid";
    private static final String ACTIVE = "Active";
    private static final String ENERGY = "Energy";
    private static final String PARTS = "Parts";
    private static final String SELECTED_ABILITY = "SelectedAbility";
    private static final String CLOAK_ENABLED = "CloakEnabled";
    private static final String SHIELD_ENABLED = "ShieldEnabled";
    private static final String COOLDOWNS = "AbilityCooldowns";

    public enum Part {
        HEAD(1, "rogue_android_part_head"),
        CHEST(2, "rogue_android_part_chest"),
        ARMS(4, "rogue_android_part_arms"),
        LEGS(8, "rogue_android_part_legs");

        public final int bit;
        public final String itemId;

        Part(int bit, String itemId) {
            this.bit = bit;
            this.itemId = itemId;
        }
    }

    public enum Ability {
        CLOAK("Cloak", Part.HEAD),
        FORCE_FIELD("Force Field", Part.CHEST),
        SHOCKWAVE("Sonic Shockwave", Part.ARMS),
        TELEPORT("Ender Teleport", Part.LEGS);

        public final String displayName;
        public final Part requiredPart;

        Ability(String displayName, Part requiredPart) {
            this.displayName = displayName;
            this.requiredPart = requiredPart;
        }
    }

    private AndroidData() {
    }

    private static CompoundTag data(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }

    private static void save(Player player, CompoundTag data) {
        player.getPersistentData().put(ROOT, data);
    }

    public static boolean isAndroid(Player player) {
        return data(player).getBoolean(ACTIVE);
    }

    public static int getExperience(Player player) {\n        return Math.max(0, data(player).getInt(EXPERIENCE));\n    }\n\n    public static int getLevel(Player player) {\n        int xp = getExperience(player);\n        int level = 1;\n        while (level < MAX_LEVEL && xp >= experienceForLevel(level + 1)) {\n            level++;\n        }\n        return level;\n    }\n\n    public static int experienceForLevel(int level) {\n        int clamped = Mth.clamp(level, 1, MAX_LEVEL);\n        return (clamped - 1) * 1_000;\n    }\n\n    public static int experienceIntoLevel(Player player) {\n        return getExperience(player) - experienceForLevel(getLevel(player));\n    }\n\n    public static int experienceToNextLevel(Player player) {\n        int level = getLevel(player);\n        return level >= MAX_LEVEL ? 0 : experienceForLevel(level + 1) - getExperience(player);\n    }\n\n    public static int addExperience(Player player, int amount) {\n        int before = getExperience(player);\n        int after = Mth.clamp(before + Math.max(0, amount), 0, experienceForLevel(MAX_LEVEL));\n        if (after != before) {\n            CompoundTag state = data(player);\n            state.putInt(EXPERIENCE, after);\n            save(player, state);\n        }\n        return after - before;\n    }\n\n    public static int getEnergy(Player player) {
        return Mth.clamp(data(player).getInt(ENERGY), 0, ENERGY_CAPACITY);
    }

    public static int getParts(Player player) {
        return data(player).getInt(PARTS) & 15;
    }

    public static boolean hasPart(Player player, Part part) {
        return (getParts(player) & part.bit) != 0;
    }

    public static void activate(Player player) {
        CompoundTag data = data(player);
        data.putBoolean(ACTIVE, true);
        data.putInt(ENERGY, Math.max(25_000, getEnergy(player)));\n        if (!data.contains(EXPERIENCE)) {\n            data.putInt(EXPERIENCE, 0);\n        }
        save(player, data);
    }

    public static int receiveEnergy(Player player, int amount) {
        if (!isAndroid(player)) {
            return 0;
        }
        int accepted = Math.min(Math.max(0, amount), ENERGY_CAPACITY - getEnergy(player));
        if (accepted > 0) {
            setEnergy(player, getEnergy(player) + accepted);
        }
        return accepted;
    }

    public static int consumeEnergy(Player player, int amount) {
        int used = Math.min(Math.max(0, amount), getEnergy(player));
        if (used > 0) {
            setEnergy(player, getEnergy(player) - used);
        }
        return used;
    }

    /**
     * Atomically spends Android FE. Failed actions never drain a partial remainder.
     */
    public static boolean tryConsumeEnergy(Player player, int amount) {
        int requested = Math.max(0, amount);
        if (requested == 0) {
            return true;
        }
        int stored = getEnergy(player);
        if (stored < requested) {
            return false;
        }
        setEnergy(player, stored - requested);
        return true;
    }

    public static void setEnergy(Player player, int amount) {
        CompoundTag data = data(player);
        data.putInt(ENERGY, Mth.clamp(amount, 0, ENERGY_CAPACITY));
        save(player, data);
    }

    public static boolean installPart(Player player, Part part) {
        if (!isAndroid(player) || hasPart(player, part)) {
            return false;
        }
        CompoundTag data = data(player);
        data.putInt(PARTS, getParts(player) | part.bit);
        if (!isAbilityUnlocked(player, getSelectedAbility(player))) {
            for (Ability ability : Ability.values()) {
                if (ability.requiredPart == part) {
                    data.putInt(SELECTED_ABILITY, ability.ordinal());
                    break;
                }
            }
        }
        save(player, data);
        return true;
    }

    public static Ability getSelectedAbility(Player player) {
        Ability[] values = Ability.values();
        int selected = Mth.clamp(data(player).getInt(SELECTED_ABILITY), 0, values.length - 1);
        return values[selected];
    }

    public static void setSelectedAbility(Player player, Ability ability) {
        CompoundTag data = data(player);
        data.putInt(SELECTED_ABILITY, ability.ordinal());
        save(player, data);
    }

    public static Ability cycleAbility(Player player) {
        if (!isAndroid(player)) {
            return null;
        }
        Ability[] values = Ability.values();
        int start = getSelectedAbility(player).ordinal();
        for (int offset = 1; offset <= values.length; offset++) {
            Ability candidate = values[(start + offset) % values.length];
            if (isAbilityUnlocked(player, candidate)) {
                setSelectedAbility(player, candidate);
                return candidate;
            }
        }
        return null;
    }

    public static boolean isAbilityUnlocked(Player player, Ability ability) {
        return isAndroid(player) && hasPart(player, ability.requiredPart);
    }

    public static boolean isCloakEnabled(Player player) {
        return data(player).getBoolean(CLOAK_ENABLED);
    }

    public static void setCloakEnabled(Player player, boolean enabled) {
        CompoundTag data = data(player);
        data.putBoolean(CLOAK_ENABLED, enabled && isAbilityUnlocked(player, Ability.CLOAK));
        save(player, data);
    }

    public static boolean isShieldEnabled(Player player) {
        return data(player).getBoolean(SHIELD_ENABLED);
    }

    public static void setShieldEnabled(Player player, boolean enabled) {
        CompoundTag data = data(player);
        data.putBoolean(SHIELD_ENABLED, enabled && isAbilityUnlocked(player, Ability.FORCE_FIELD));
        save(player, data);
    }

    public static void setCooldownUntil(Player player, Ability ability, long gameTime) {
        CompoundTag data = data(player);
        CompoundTag cooldowns = data.getCompound(COOLDOWNS);
        cooldowns.putLong(ability.name(), Math.max(0L, gameTime));
        data.put(COOLDOWNS, cooldowns);
        save(player, data);
    }

    public static int getRemainingCooldown(Player player, Ability ability, long gameTime) {
        long remaining = data(player).getCompound(COOLDOWNS).getLong(ability.name()) - gameTime;
        return remaining <= 0L ? 0 : (int) Math.min(Integer.MAX_VALUE, remaining);
    }

    public static int getActiveAbilityFlags(Player player) {
        int flags = 0;
        if (isCloakEnabled(player)) {
            flags |= 1;
        }
        if (isShieldEnabled(player)) {
            flags |= 2;
        }
        return flags;
    }

    public static int deactivate(Player player) {
        CompoundTag data = data(player);
        int installed = getParts(player);
        data.putBoolean(ACTIVE, false);
        data.putInt(ENERGY, 0);
        data.putInt(PARTS, 0);
        data.putInt(SELECTED_ABILITY, 0);
        data.putBoolean(CLOAK_ENABLED, false);
        data.putBoolean(SHIELD_ENABLED, false);
        data.remove(COOLDOWNS);
        save(player, data);
        return installed;
    }

    public static ItemStack partStack(Part part) {
        return new ItemStack(ModItems.get(part.itemId).get());
    }

    public static void copyTo(Player original, Player clone) {
        CompoundTag root = original.getPersistentData().getCompound(ROOT);
        clone.getPersistentData().put(ROOT, root.copy());
    }
}
