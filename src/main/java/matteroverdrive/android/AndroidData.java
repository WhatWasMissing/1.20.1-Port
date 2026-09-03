package matteroverdrive.android;

import matteroverdrive.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Persistent Android state owned by a player rather than a block. */
public final class AndroidData {
    public static final int ENERGY_CAPACITY = 100_000;
    public static final int MAX_LEVEL = 10;
    private static final String EXPERIENCE = "Experience";
    private static final String ROOT = "MatterOverdriveAndroid";
    private static final String ACTIVE = "Active";
    private static final String ENERGY = "Energy";
    private static final String PARTS = "Parts";
    private static final String SELECTED_ABILITY = "SelectedAbility";
    private static final String CLOAK_ENABLED = "CloakEnabled";
    private static final String SHIELD_ENABLED = "ShieldEnabled";
    private static final String COOLDOWNS = "AbilityCooldowns";
    private static final String PERKS = "SelectedPerks";
    private static final String PERKS_BACKUP = "SelectedPerksBackup";

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
        CLOAK("Cloak", Part.HEAD, 1),
        FORCE_FIELD("Force Field", Part.CHEST, 2),
        SHOCKWAVE("Sonic Shockwave", Part.ARMS, 3),
        TELEPORT("Ender Teleport", Part.LEGS, 4);

        public final String displayName;
        public final Part requiredPart;
        public final int requiredLevel;

        Ability(String displayName, Part requiredPart, int requiredLevel) {
            this.displayName = displayName;
            this.requiredPart = requiredPart;
            this.requiredLevel = requiredLevel;
        }
    }

    public enum Perk {
        EFFICIENT_CORE(1, 0, "Efficient Core", "All Android abilities use 10% less FE."),
        REINFORCED_FRAME(1, 1, "Reinforced Frame", "Reduce incoming damage by 5%."),
        GHOST_PROTOCOL(2, 0, "Ghost Protocol", "Cloak uses 25% less FE."),
        BARRIER_MATRIX(2, 1, "Barrier Matrix", "Force Field uses 25% less FE."),
        RESONANT_PULSE(3, 0, "Resonant Pulse", "Sonic Shockwave deals 2 more damage."),
        PHASE_CAPACITOR(3, 1, "Phase Capacitor", "Ender Teleport gains 4 blocks of range."),
        WIDEBAND_PULSE(4, 0, "Wideband Pulse", "Sonic Shockwave gains 2 blocks of radius."),
        RAPID_BLINK(4, 1, "Rapid Blink", "Ender Teleport cooldown is 15% shorter."),
        COMBAT_SERVOS(5, 0, "Combat Servos", "Direct melee attacks deal 2 more damage."),
        KINETIC_PLATING(5, 1, "Kinetic Plating", "Reduce incoming damage by another 10%."),
        SHOCK_RECYCLER(6, 0, "Shock Recycler", "Sonic Shockwave uses 25% less FE."),
        BLINK_RECYCLER(6, 1, "Blink Recycler", "Ender Teleport uses 25% less FE."),
        SHOCK_MOMENTUM(7, 0, "Shock Momentum", "Sonic Shockwave launches targets farther."),
        PHASE_STABILIZER(7, 1, "Phase Stabilizer", "Ender Teleport gains another 4 blocks of range."),
        NEURAL_ACCELERATOR(8, 0, "Neural Accelerator", "Leg servos provide stronger movement speed."),
        ADAPTIVE_ARMOR(8, 1, "Adaptive Armor", "Reduce incoming damage by another 10%."),
        OVERCHARGED_PULSE(9, 0, "Overcharged Pulse", "Sonic Shockwave deals 3 more damage."),
        LONG_RANGE_BLINK(9, 1, "Long-range Blink", "Ender Teleport gains another 8 blocks of range."),
        APEX_CORE(10, 0, "Apex Core", "All Android abilities use a further 15% less FE."),
        ADAMANT_CHASSIS(10, 1, "Adamant Chassis", "Reduce incoming damage by another 15%."),
        SUSTAINED_SYSTEMS(1, 2, "Sustained Systems", "Installed parts use 25% less passive FE."),
        QUICK_CHARGE(2, 2, "Quick Charge", "Handheld batteries charge the Android twice as fast."),
        LEARNING_MATRIX(3, 2, "Learning Matrix", "Future Android XP gains are increased by 25%."),
        COOLDOWN_ROUTER(4, 2, "Cooldown Router", "Shockwave and Teleport cooldowns are 10% shorter."),
        REACTIVE_PLATING(5, 2, "Reactive Plating", "Reduce incoming damage by 5%."),
        SELF_REPAIR(6, 2, "Self Repair", "Spend FE to regenerate health while injured."),
        SILENT_CLOAK(7, 2, "Silent Cloak", "Cloak uses a further 15% less FE."),
        TACTICAL_SCAN(8, 2, "Tactical Scan", "Periodically highlight nearby hostile mobs."),
        EMERGENCY_PROTOCOL(9, 2, "Emergency Protocol", "Gain powered resistance at critically low health."),
        SYNTHETIC_PERFECTION(10, 2, "Synthetic Perfection", "Use 10% less ability FE and take 5% less damage.");

        public final int level;
        public final int branch;
        public final String displayName;
        public final String description;

        Perk(int level, int branch, String displayName, String description) {
            this.level = level;
            this.branch = branch;
            this.displayName = displayName;
            this.description = description;
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

    public static long getSelectedPerks(Player player) {
        CompoundTag state = data(player);
        long validMask = (1L << Perk.values().length) - 1L;
        long selected = state.getLong(PERKS) & validMask;
        long backup = state.getLong(PERKS_BACKUP) & validMask;

        // XP and level writes must never erase installed choices. The mirrored
        // value also repairs saves affected by the level-up persistence regression.
        if (selected == 0L && backup != 0L) {
            selected = backup;
            state.putLong(PERKS, selected);
            save(player, state);
        } else if (selected != backup) {
            state.putLong(PERKS_BACKUP, selected);
            save(player, state);
        }
        return selected;
    }

    private static void writeSelectedPerks(CompoundTag state, long perks) {
        long validMask = (1L << Perk.values().length) - 1L;
        long selected = perks & validMask;
        state.putLong(PERKS, selected);
        state.putLong(PERKS_BACKUP, selected);
    }

    public static boolean hasPerk(Player player, Perk perk) {
        return (getSelectedPerks(player) & (1L << perk.ordinal())) != 0L;
    }

    public static boolean hasPerkAtLevel(Player player, int level) {
        for (Perk perk : Perk.values()) {
            if (perk.level == level && hasPerk(player, perk)) return true;
        }
        return false;
    }

    public static int getSpentSkillPoints(Player player) {
        return Long.bitCount(getSelectedPerks(player));
    }

    public static final int PERK_RESET_COST = 25_000;
    public static final int PERK_REFUND_COST = 2_500;

    public static int getAvailableSkillPoints(Player player) {
        return Math.max(0, getLevel(player) - getSpentSkillPoints(player));
    }

    public static boolean selectPerk(Player player, Perk perk) {
        if (!isAndroid(player) || getLevel(player) < perk.level || hasPerkAtLevel(player, perk.level)
                || getAvailableSkillPoints(player) <= 0) {
            return false;
        }
        CompoundTag state = data(player);
        writeSelectedPerks(state, getSelectedPerks(player) | (1L << perk.ordinal()));
        save(player, state);
        return true;
    }

    public static boolean tryRefundPerk(Player player, Perk perk) {
        if (!isAndroid(player) || perk == null || !hasPerk(player, perk)
                || !tryConsumeEnergy(player, PERK_REFUND_COST)) {
            return false;
        }
        CompoundTag state = data(player);
        writeSelectedPerks(state, getSelectedPerks(player) & ~(1L << perk.ordinal()));
        save(player, state);
        return true;
    }

    public static boolean tryResetPerks(Player player) {
        if (!isAndroid(player) || getSelectedPerks(player) == 0L
                || !tryConsumeEnergy(player, PERK_RESET_COST)) {
            return false;
        }
        CompoundTag state = data(player);
        writeSelectedPerks(state, 0L);
        state.putBoolean(CLOAK_ENABLED, false);
        state.putBoolean(SHIELD_ENABLED, false);
        save(player, state);
        return true;
    }

    public static int scaleAbilityEnergy(Player player, int base, Perk specialisedEfficiency) {
        double multiplier = 1.0D;
        if (hasPerk(player, Perk.EFFICIENT_CORE)) multiplier *= 0.90D;
        if (hasPerk(player, specialisedEfficiency)) multiplier *= 0.75D;
        if (hasPerk(player, Perk.APEX_CORE)) multiplier *= 0.85D;
        if (hasPerk(player, Perk.SYNTHETIC_PERFECTION)) multiplier *= 0.90D;
        if (specialisedEfficiency == Perk.GHOST_PROTOCOL && hasPerk(player, Perk.SILENT_CLOAK)) multiplier *= 0.85D;
        return Math.max(1, (int) Math.ceil(base * multiplier));
    }

    public static float incomingDamageMultiplier(Player player) {
        double multiplier = 1.0D;
        if (hasPerk(player, Perk.REINFORCED_FRAME)) multiplier *= 0.95D;
        if (hasPerk(player, Perk.KINETIC_PLATING)) multiplier *= 0.90D;
        if (hasPerk(player, Perk.ADAPTIVE_ARMOR)) multiplier *= 0.90D;
        if (hasPerk(player, Perk.ADAMANT_CHASSIS)) multiplier *= 0.85D;
        if (hasPerk(player, Perk.REACTIVE_PLATING)) multiplier *= 0.95D;
        if (hasPerk(player, Perk.SYNTHETIC_PERFECTION)) multiplier *= 0.95D;
        return (float) multiplier;
    }

    public static int getExperience(Player player) {
        CompoundTag state = data(player);
        if (!state.contains(EXPERIENCE)) {
            // Migrate active players created before progression was added.
            int migrated = state.getBoolean(ACTIVE)
                    ? 100 + Integer.bitCount(state.getInt(PARTS) & 15) * 50
                    : 0;
            state.putInt(EXPERIENCE, migrated);
            save(player, state);
            return migrated;
        }
        return Math.max(0, state.getInt(EXPERIENCE));
    }

    public static int getLevel(Player player) {
        int xp = getExperience(player);
        int level = 1;
        while (level < MAX_LEVEL && xp >= experienceForLevel(level + 1)) {
            level++;
        }
        return level;
    }

    public static int experienceForLevel(int level) {
        int clamped = Mth.clamp(level, 1, MAX_LEVEL);
        return (clamped - 1) * 100;
    }

    public static int experienceIntoLevel(Player player) {
        return getExperience(player) - experienceForLevel(getLevel(player));
    }

    public static int experienceToNextLevel(Player player) {
        int level = getLevel(player);
        return level >= MAX_LEVEL ? 0 : experienceForLevel(level + 1) - getExperience(player);
    }

    public static int addExperience(Player player, int amount) {
        if (amount > 0 && hasPerk(player, Perk.LEARNING_MATRIX)) {
            amount = (int) Math.ceil(amount * 1.25D);
        }
        int before = getExperience(player);
        int after = Mth.clamp(before + Math.max(0, amount), 0, experienceForLevel(MAX_LEVEL));
        if (after != before) {
            long installedPerks = getSelectedPerks(player);
            CompoundTag state = data(player);
            state.putInt(EXPERIENCE, after);
            writeSelectedPerks(state, installedPerks);
            save(player, state);
        }
        return after - before;
    }

    public static int getEnergy(Player player) {
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
        data.putInt(ENERGY, Math.max(25_000, getEnergy(player)));
        boolean migrated = !data.contains(EXPERIENCE);
        if (migrated) {
            data.putInt(EXPERIENCE, 100 + Integer.bitCount(getParts(player)) * 50);
        }
        save(player, data);
        if (!migrated) {
            addExperience(player, 100);
        }
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
        addExperience(player, 50);
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
        return isAndroid(player) && getLevel(player) >= ability.requiredLevel && hasPart(player, ability.requiredPart);
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
