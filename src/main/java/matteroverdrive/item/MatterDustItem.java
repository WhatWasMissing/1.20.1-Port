package matteroverdrive.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class MatterDustItem extends Item {
    private static final String MATTER_TAG = "Matter";
    private final boolean refined;

    public MatterDustItem(Properties properties, boolean refined) {
        super(properties);
        this.refined = refined;
    }

    public boolean isRefined() {
        return refined;
    }

    public static int getMatter(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) return 0;
        return Math.max(0, stack.getTag().getInt(MATTER_TAG));
    }

    public static void setMatter(ItemStack stack, int matter) {
        stack.getOrCreateTag().putInt(MATTER_TAG, Math.max(0, matter));
    }

    public static boolean sameMatter(ItemStack first, ItemStack second) {
        return !first.isEmpty() && !second.isEmpty()
                && first.getItem() == second.getItem()
                && getMatter(first) == getMatter(second);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int matter = getMatter(stack);
        if (matter > 0) tooltip.add(Component.translatable("tooltip.matteroverdrive.matter_value", matter));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
