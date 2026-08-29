package matteroverdrive.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class DebugMatterContainerItem extends MatterContainerItem {
    public static final int DEBUG_CAPACITY = 1_000_000;

    public DebugMatterContainerItem(Properties properties) {
        super(properties, DEBUG_CAPACITY);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("Debug Matter Container");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Contains up to 1,000,000 kM of matter"));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
