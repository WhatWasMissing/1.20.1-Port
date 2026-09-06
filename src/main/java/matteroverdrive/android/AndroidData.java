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
    private static final int[] LEVEL_XP = {0, 0, 150, 400, 800, 1_400, 2_250, 3_400, 5_000, 7_200, 10_000};
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
        HEAD(1, "rogue_android_part_head"), CHEST(2, "rogue_android_part_chest"),
        ARMS(4, "rogue_android_part_arms"), LEGS(8, "rogue_android_part_legs");
        public final int bit; public final String itemId;
        Part(int bit, String itemId) { this.bit = bit; this.itemId = itemId; }
    }

    public enum Ability {
        CLOAK("Cloak", Part.HEAD, 1), FORCE_FIELD("Force Field", Part.CHEST, 2),
        SHOCKWAVE("Sonic Shockwave", Part.ARMS, 3), TELEPORT("Ender Teleport", Part.LEGS, 4);
        public final String displayName; public final Part requiredPart; public final int requiredLevel;
        Ability(String displayName, Part requiredPart, int requiredLevel) {
            this.displayName = displayName; this.requiredPart = requiredPart; this.requiredLevel = requiredLevel;
        }
    }

    /** Five total Ascension Points by level 10. Higher tiers require prior investment in the same branch. */
    public enum Perk {
        EFFICIENT_CORE(2, 0, "Paracausal Core", "All Android abilities consume 20% less FE."),
        REINFORCED_FRAME(2, 1, "Tritanium Skeleton", "Reduce all incoming damage by 10%."),
        SUSTAINED_SYSTEMS(2, 2, "Closed-Circuit Systems", "Installed body parts consume 40% less passive FE."),
        GHOST_PROTOCOL(3, 0, "Ghost Protocol", "Cloak consumes 40% less FE."),
        BARRIER_MATRIX(3, 1, "Aegis Matrix", "Force Field consumes 40% less FE."),
        QUICK_CHARGE(3, 2, "Rapid Induction", "Handheld batteries charge the Android three times faster."),
        RESONANT_PULSE(4, 0, "Resonant Detonation", "Sonic Shockwave gains +4 damage."),
        PHASE_CAPACITOR(4, 1, "Phase Capacitor", "Ender Teleport gains 6 blocks of range."),
        LEARNING_MATRIX(4, 2, "Adaptive Learning Matrix", "Future Android XP gains are increased by 40%."),
        WIDEBAND_PULSE(5, 0, "Wideband Rupture", "Sonic Shockwave radius expands by 4 blocks."),
        RAPID_BLINK(5, 1, "Blink Accelerator", "Ender Teleport cooldown is 25% shorter."),
        COOLDOWN_ROUTER(5, 2, "Parallel Cognition", "Shockwave and Teleport cooldowns are 20% shorter."),
        COMBAT_SERVOS(6, 0, "Siege Servos", "Powered melee attacks gain +5 damage and stronger knockback."),
        KINETIC_PLATING(6, 1, "Kinetic Plating", "Reduce incoming damage by a further 15%."),
        REACTIVE_PLATING(6, 2, "Reactive Plating", "Reduce incoming damage by another 10%."),
        SHOCK_RECYCLER(7, 0, "Pulse Recycler", "Sonic Shockwave consumes 35% less FE."),
        BLINK_RECYCLER(7, 1, "Phase Recycler", "Ender Teleport consumes 35% less FE."),
        SELF_REPAIR(7, 2, "Nanite Reconstruction", "Continuously spend FE to repair damaged health."),
        SHOCK_MOMENTUM(8, 0, "Gravitic Aftershock", "Sonic Shockwave launches targets dramatically farther."),
        PHASE_STABILIZER(8, 1, "Phase Stabilizer", "Ender Teleport gains another 6 blocks of range."),
        SILENT_CLOAK(8, 2, "Null-Signature Cloak", "Cloak receives a second 35% efficiency reduction and suppresses nearby targeting."),
        OVERCHARGED_PULSE(9, 0, "Overcharged Singularity", "Sonic Shockwave gains +6 damage and briefly weakens survivors."),
        NEURAL_ACCELERATOR(9, 1, "Neural Accelerator", "Leg servos provide significantly stronger movement speed and jump control."),
        TACTICAL_SCAN(9, 2, "Predator Optics", "Expose hostile targets through terrain at greatly increased range."),
        APEX_CORE(10, 0, "Apex Reactor Core", "ASSAULT CAPSTONE: ability costs fall another 35%; Shockwave cooldown is halved and kills recycle FE."),
        ADAMANT_CHASSIS(10, 1, "Adamant Chassis", "CHASSIS CAPSTONE: heavy mitigation and Force Field absorbs 75% of incoming damage."),
        EMERGENCY_PROTOCOL(10, 2, "Last-Stand Protocol", "UTILITY CAPSTONE: critical health automatically triggers powered resistance, regeneration and absorption."),
        LONG_RANGE_BLINK(10, 0, "Trans-Dimensional Blink", "ASSAULT CAPSTONE: extreme blink range and the destination trace can phase through intervening blocks."),
        ADAPTIVE_ARMOR(10, 1, "Adaptive Armor", "CHASSIS CAPSTONE: further mitigation and stronger powered mobility while damaged."),
        SYNTHETIC_PERFECTION(10, 2, "Synthetic Perfection", "UTILITY CAPSTONE: lower FE use, lower damage taken and enhanced nanite repair while highly charged.");

        public final int level; public final int branch; public final String displayName; public final String description;
        Perk(int level, int branch, String displayName, String description) {
            this.level = level; this.branch = branch; this.displayName = displayName; this.description = description;
        }
    }

    private AndroidData() {}
    private static CompoundTag data(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT)) persistent.put(ROOT, new CompoundTag());
        return persistent.getCompound(ROOT);
    }
    private static void save(Player player, CompoundTag data) { player.getPersistentData().put(ROOT, data); }
    public static boolean isAndroid(Player player) { return data(player).getBoolean(ACTIVE); }

    public static long getSelectedPerks(Player player) {
        CompoundTag state = data(player);
        long validMask = Perk.values().length >= 64 ? -1L : (1L << Perk.values().length) - 1L;
        long selected = state.getLong(PERKS) & validMask;
        long backup = state.getLong(PERKS_BACKUP) & validMask;
        if (selected == 0L && backup != 0L) { selected = backup; state.putLong(PERKS, selected); save(player, state); }
        else if (selected != backup) { state.putLong(PERKS_BACKUP, selected); save(player, state); }
        return selected;
    }
    private static void writeSelectedPerks(CompoundTag state, long perks) {
        long validMask = Perk.values().length >= 64 ? -1L : (1L << Perk.values().length) - 1L;
        long selected = perks & validMask; state.putLong(PERKS, selected); state.putLong(PERKS_BACKUP, selected);
    }
    public static boolean hasPerk(Player player, Perk perk) { return (getSelectedPerks(player) & (1L << perk.ordinal())) != 0L; }
    public static boolean hasPerkAtLevel(Player player, int level) {
        for (Perk perk : Perk.values()) if (perk.level == level && hasPerk(player, perk)) return true;
        return false;
    }
    public static int getSpentSkillPoints(Player player) { return Long.bitCount(getSelectedPerks(player)); }
    public static final int PERK_RESET_COST = 50_000;
    public static final int PERK_REFUND_COST = 10_000;
    public static int skillPointsForLevel(int level) { return Math.max(0, Mth.clamp(level, 1, MAX_LEVEL) / 2); }
    public static int getAvailableSkillPoints(Player player) { return Math.max(0, skillPointsForLevel(getLevel(player)) - getSpentSkillPoints(player)); }

    public static int requiredBranchInvestment(Perk perk) {
        if (perk.level >= 10) return 3;
        if (perk.level >= 8) return 2;
        if (perk.level >= 5) return 1;
        return 0;
    }
    public static int getPriorBranchInvestments(Player player, Perk perk) {
        return priorBranchInvestments(getSelectedPerks(player), perk);
    }
    private static int priorBranchInvestments(long mask, Perk perk) {
        int count = 0;
        for (Perk candidate : Perk.values()) {
            if (candidate.branch == perk.branch && candidate.level < perk.level
                    && (mask & (1L << candidate.ordinal())) != 0L) count++;
        }
        return count;
    }
    public static boolean meetsPerkPrerequisites(Player player, Perk perk) {
        return getPriorBranchInvestments(player, perk) >= requiredBranchInvestment(perk);
    }
    private static boolean maskMeetsPrerequisites(long mask) {
        for (Perk perk : Perk.values()) {
            if ((mask & (1L << perk.ordinal())) != 0L
                    && priorBranchInvestments(mask, perk) < requiredBranchInvestment(perk)) return false;
        }
        return true;
    }

    public static boolean selectPerk(Player player, Perk perk) {
        if (!isAndroid(player) || getLevel(player) < perk.level || hasPerk(player, perk)
                || getAvailableSkillPoints(player) <= 0 || !meetsPerkPrerequisites(player, perk)) return false;
        CompoundTag state = data(player); writeSelectedPerks(state, getSelectedPerks(player) | (1L << perk.ordinal())); save(player, state); return true;
    }
    public static boolean tryRefundPerk(Player player, Perk perk) {
        if (!isAndroid(player) || perk == null || !hasPerk(player, perk)) return false;
        long nextMask = getSelectedPerks(player) & ~(1L << perk.ordinal());
        if (!maskMeetsPrerequisites(nextMask) || !tryConsumeEnergy(player, PERK_REFUND_COST)) return false;
        CompoundTag state = data(player); writeSelectedPerks(state, nextMask); save(player, state); return true;
    }
    public static boolean tryResetPerks(Player player) {
        if (!isAndroid(player) || getSelectedPerks(player) == 0L || !tryConsumeEnergy(player, PERK_RESET_COST)) return false;
        CompoundTag state = data(player); writeSelectedPerks(state, 0L); state.putBoolean(CLOAK_ENABLED, false); state.putBoolean(SHIELD_ENABLED, false); save(player, state); return true;
    }

    public static int scaleAbilityEnergy(Player player, int base, Perk specialisedEfficiency) {
        double m = 1.0D;
        if (hasPerk(player, Perk.EFFICIENT_CORE)) m *= 0.80D;
        if (specialisedEfficiency != null && hasPerk(player, specialisedEfficiency)) {
            m *= specialisedEfficiency == Perk.GHOST_PROTOCOL || specialisedEfficiency == Perk.BARRIER_MATRIX ? 0.60D : 0.65D;
        }
        if (hasPerk(player, Perk.APEX_CORE)) m *= 0.65D;
        if (hasPerk(player, Perk.SYNTHETIC_PERFECTION)) m *= 0.85D;
        if (specialisedEfficiency == Perk.GHOST_PROTOCOL && hasPerk(player, Perk.SILENT_CLOAK)) m *= 0.65D;
        return Math.max(1, (int)Math.ceil(base * m));
    }
    public static float incomingDamageMultiplier(Player player) {
        double m = 1.0D;
        if (hasPerk(player, Perk.REINFORCED_FRAME)) m *= 0.90D;
        if (hasPerk(player, Perk.KINETIC_PLATING)) m *= 0.85D;
        if (hasPerk(player, Perk.ADAPTIVE_ARMOR)) m *= 0.85D;
        if (hasPerk(player, Perk.ADAMANT_CHASSIS)) m *= 0.80D;
        if (hasPerk(player, Perk.REACTIVE_PLATING)) m *= 0.90D;
        if (hasPerk(player, Perk.SYNTHETIC_PERFECTION)) m *= 0.90D;
        return (float)m;
    }

    public static int getExperience(Player player) {
        CompoundTag state = data(player);
        if (!state.contains(EXPERIENCE)) {
            int migrated = state.getBoolean(ACTIVE) ? 150 + Integer.bitCount(state.getInt(PARTS) & 15) * 75 : 0;
            state.putInt(EXPERIENCE, migrated); save(player, state); return migrated;
        }
        return Math.max(0, state.getInt(EXPERIENCE));
    }
    public static int getLevel(Player player) {
        int xp = getExperience(player), level = 1;
        while (level < MAX_LEVEL && xp >= experienceForLevel(level + 1)) level++;
        return level;
    }
    public static int experienceForLevel(int level) { return LEVEL_XP[Mth.clamp(level, 1, MAX_LEVEL)]; }
    public static int experienceIntoLevel(Player player) { return getExperience(player) - experienceForLevel(getLevel(player)); }
    public static int experienceToNextLevel(Player player) {
        int level = getLevel(player); return level >= MAX_LEVEL ? 0 : experienceForLevel(level + 1) - getExperience(player);
    }
    public static int addExperience(Player player, int amount) {
        if (amount > 0 && hasPerk(player, Perk.LEARNING_MATRIX)) amount = (int)Math.ceil(amount * 1.40D);
        int before = getExperience(player), after = Mth.clamp(before + Math.max(0, amount), 0, experienceForLevel(MAX_LEVEL));
        if (after != before) { long perks = getSelectedPerks(player); CompoundTag state = data(player); state.putInt(EXPERIENCE, after); writeSelectedPerks(state, perks); save(player, state); }
        return after - before;
    }

    public static int getEnergy(Player player) { return Mth.clamp(data(player).getInt(ENERGY), 0, ENERGY_CAPACITY); }
    public static int getParts(Player player) { return data(player).getInt(PARTS) & 15; }
    public static boolean hasPart(Player player, Part part) { return (getParts(player) & part.bit) != 0; }
    public static void activate(Player player) {
        CompoundTag state = data(player); state.putBoolean(ACTIVE, true); state.putInt(ENERGY, Math.max(25_000, getEnergy(player)));
        boolean migrated = !state.contains(EXPERIENCE); if (migrated) state.putInt(EXPERIENCE, 150 + Integer.bitCount(getParts(player)) * 75);
        save(player, state); if (!migrated) addExperience(player, 100);
    }
    public static int receiveEnergy(Player player, int amount) {
        if (!isAndroid(player)) return 0; int accepted = Math.min(Math.max(0, amount), ENERGY_CAPACITY - getEnergy(player));
        if (accepted > 0) setEnergy(player, getEnergy(player) + accepted); return accepted;
    }
    public static int consumeEnergy(Player player, int amount) { int used = Math.min(Math.max(0, amount), getEnergy(player)); if (used > 0) setEnergy(player, getEnergy(player) - used); return used; }
    public static boolean tryConsumeEnergy(Player player, int amount) { int requested = Math.max(0, amount), stored = getEnergy(player); if (requested == 0) return true; if (stored < requested) return false; setEnergy(player, stored - requested); return true; }
    public static void setEnergy(Player player, int amount) { CompoundTag state = data(player); state.putInt(ENERGY, Mth.clamp(amount, 0, ENERGY_CAPACITY)); save(player, state); }

    public static boolean installPart(Player player, Part part) {
        if (!isAndroid(player) || hasPart(player, part)) return false;
        CompoundTag state = data(player); state.putInt(PARTS, getParts(player) | part.bit);
        if (!isAbilityUnlocked(player, getSelectedAbility(player))) for (Ability ability : Ability.values()) if (ability.requiredPart == part) { state.putInt(SELECTED_ABILITY, ability.ordinal()); break; }
        save(player, state); addExperience(player, 75); return true;
    }
    public static Ability getSelectedAbility(Player player) { Ability[] v = Ability.values(); return v[Mth.clamp(data(player).getInt(SELECTED_ABILITY), 0, v.length - 1)]; }
    public static void setSelectedAbility(Player player, Ability ability) { CompoundTag state = data(player); state.putInt(SELECTED_ABILITY, ability.ordinal()); save(player, state); }
    public static Ability cycleAbility(Player player) {
        if (!isAndroid(player)) return null; Ability[] v = Ability.values(); int start = getSelectedAbility(player).ordinal();
        for (int i = 1; i <= v.length; i++) { Ability a = v[(start + i) % v.length]; if (isAbilityUnlocked(player, a)) { setSelectedAbility(player, a); return a; } }
        return null;
    }
    public static boolean isAbilityUnlocked(Player player, Ability ability) { return isAndroid(player) && getLevel(player) >= ability.requiredLevel && hasPart(player, ability.requiredPart); }
    public static boolean isCloakEnabled(Player player) { return data(player).getBoolean(CLOAK_ENABLED); }
    public static void setCloakEnabled(Player player, boolean enabled) { CompoundTag state = data(player); state.putBoolean(CLOAK_ENABLED, enabled && isAbilityUnlocked(player, Ability.CLOAK)); save(player, state); }
    public static boolean isShieldEnabled(Player player) { return data(player).getBoolean(SHIELD_ENABLED); }
    public static void setShieldEnabled(Player player, boolean enabled) { CompoundTag state = data(player); state.putBoolean(SHIELD_ENABLED, enabled && isAbilityUnlocked(player, Ability.FORCE_FIELD)); save(player, state); }
    public static void setCooldownUntil(Player player, Ability ability, long gameTime) { CompoundTag state = data(player), cooldowns = state.getCompound(COOLDOWNS); cooldowns.putLong(ability.name(), Math.max(0L, gameTime)); state.put(COOLDOWNS, cooldowns); save(player, state); }
    public static int getRemainingCooldown(Player player, Ability ability, long gameTime) { long r = data(player).getCompound(COOLDOWNS).getLong(ability.name()) - gameTime; return r <= 0L ? 0 : (int)Math.min(Integer.MAX_VALUE, r); }
    public static int getActiveAbilityFlags(Player player) { int flags = 0; if (isCloakEnabled(player)) flags |= 1; if (isShieldEnabled(player)) flags |= 2; return flags; }
    public static int deactivate(Player player) {
        CompoundTag state = data(player); int installed = getParts(player); state.putBoolean(ACTIVE, false); state.putInt(ENERGY, 0); state.putInt(PARTS, 0); state.putInt(SELECTED_ABILITY, 0); state.putBoolean(CLOAK_ENABLED, false); state.putBoolean(SHIELD_ENABLED, false); state.remove(COOLDOWNS); save(player, state); return installed;
    }
    public static ItemStack partStack(Part part) { return new ItemStack(ModItems.get(part.itemId).get()); }
    public static void copyTo(Player original, Player clone) { clone.getPersistentData().put(ROOT, original.getPersistentData().getCompound(ROOT).copy()); }
}
