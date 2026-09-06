package matteroverdrive.android;

import net.minecraft.world.entity.player.Player;

/** Derived branch-specialisation bonuses layered over the persistent 0.3 perk mask. */
public final class AndroidMastery {
    public static final int TIER_ONE = 2;
    public static final int TIER_TWO = 3;
    public static final int TIER_THREE = 4;

    private AndroidMastery() {}

    public static int investment(Player player, int branch) {
        long selected = AndroidData.getSelectedPerks(player);
        int count = 0;
        for (AndroidData.Perk perk : AndroidData.Perk.values()) {
            if (perk.branch == branch && (selected & (1L << perk.ordinal())) != 0L) count++;
        }
        return count;
    }

    public static int tier(Player player, int branch) {
        return tierForInvestment(investment(player, branch));
    }

    public static int tierForInvestment(int investment) {
        if (investment >= TIER_THREE) return 3;
        if (investment >= TIER_TWO) return 2;
        if (investment >= TIER_ONE) return 1;
        return 0;
    }

    public static double assaultDamageMultiplier(Player player) {
        return switch (tier(player, 0)) {
            case 3 -> 1.20D;
            case 2 -> 1.12D;
            case 1 -> 1.06D;
            default -> 1.0D;
        };
    }

    public static double chassisDamageMultiplier(Player player) {
        return switch (tier(player, 1)) {
            case 3 -> 0.84D;
            case 2 -> 0.90D;
            case 1 -> 0.95D;
            default -> 1.0D;
        };
    }

    public static double utilityPassiveEnergyMultiplier(Player player) {
        return switch (tier(player, 2)) {
            case 3 -> 0.60D;
            case 2 -> 0.72D;
            case 1 -> 0.85D;
            default -> 1.0D;
        };
    }

    public static double utilityChargeMultiplier(Player player) {
        return switch (tier(player, 2)) {
            case 3 -> 1.75D;
            case 2 -> 1.45D;
            case 1 -> 1.20D;
            default -> 1.0D;
        };
    }

    public static String tierName(int tier) {
        return switch (tier) {
            case 3 -> "MASTERWORK";
            case 2 -> "SPECIALIST";
            case 1 -> "CALIBRATED";
            default -> "UNFOCUSED";
        };
    }
}
