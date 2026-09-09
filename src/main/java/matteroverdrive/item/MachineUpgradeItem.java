package matteroverdrive.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class MachineUpgradeItem extends Item {
    private final Upgrade upgrade;

    public MachineUpgradeItem(Properties properties, Upgrade upgrade) { super(properties); this.upgrade = upgrade; }
    public Upgrade getUpgrade() { return upgrade; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addMultiplier(tooltip, "Speed", upgrade.speed());
        addMultiplier(tooltip, "Power usage", upgrade.powerUsage());
        addMultiplier(tooltip, "Failure chance", upgrade.failureChance());
        addMultiplier(tooltip, "Range", upgrade.range());
        addMultiplier(tooltip, "Power storage", upgrade.powerStorage());
        addMultiplier(tooltip, "Matter storage", upgrade.matterStorage());
        addMultiplier(tooltip, "Matter usage", upgrade.matterUsage());
        if (upgrade.parallelLanes() > 0) tooltip.add(Component.literal("Parallel lanes: +" + upgrade.parallelLanes()));
    }

    private static void addMultiplier(List<Component> tooltip, String label, double multiplier) {
        if (multiplier != 1.0D) tooltip.add(Component.literal(label + ": x" + formatMultiplier(multiplier)));
    }
    private static String formatMultiplier(double multiplier) { return multiplier == Math.rint(multiplier) ? Integer.toString((int) multiplier) : Double.toString(multiplier); }

    public enum Upgrade {
        SPEED("upgrade_speed", 0.75D, 1.25D, 1.25D, 1.0D, 1.0D, 1.0D, 1.25D, 0),
        POWER("upgrade_power", 1.5D, 0.75D, 1.25D, 1.0D, 1.0D, 1.0D, 1.0D, 0),
        FAILSAFE("upgrade_failsafe", 1.25D, 1.25D, 0.5D, 1.0D, 1.0D, 1.0D, 1.25D, 0),
        RANGE("upgrade_range", 1.0D, 1.5D, 1.0D, 4.0D, 1.0D, 1.0D, 1.5D, 0),
        POWER_STORAGE("upgrade_power_storage", 1.0D, 1.0D, 1.0D, 1.0D, 2.0D, 1.0D, 1.0D, 0),
        HYPER_SPEED("upgrade_hyper_speed", 0.15D, 2.0D, 1.25D, 1.0D, 1.0D, 1.0D, 2.0D, 0),
        MATTER_STORAGE("upgrade_matter_storage", 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 2.0D, 1.0D, 0),
        PARALLEL("upgrade_parallel_processing", 1.0D, 1.15D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1);

        private final String itemId;
        private final double speed, powerUsage, failureChance, range, powerStorage, matterStorage, matterUsage;
        private final int parallelLanes;

        Upgrade(String itemId, double speed, double powerUsage, double failureChance, double range,
                double powerStorage, double matterStorage, double matterUsage, int parallelLanes) {
            this.itemId = itemId; this.speed = speed; this.powerUsage = powerUsage; this.failureChance = failureChance;
            this.range = range; this.powerStorage = powerStorage; this.matterStorage = matterStorage; this.matterUsage = matterUsage;
            this.parallelLanes = parallelLanes;
        }

        public static @Nullable Upgrade fromItemId(String itemId) { for (Upgrade upgrade : values()) if (upgrade.itemId.equals(itemId)) return upgrade; return null; }
        public double speed() { return speed; }
        public double powerUsage() { return powerUsage; }
        public double failureChance() { return failureChance; }
        public double range() { return range; }
        public double powerStorage() { return powerStorage; }
        public double matterStorage() { return matterStorage; }
        public double matterUsage() { return matterUsage; }
        public int parallelLanes() { return parallelLanes; }
    }
}