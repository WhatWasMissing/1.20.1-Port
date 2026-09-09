package matteroverdrive.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Removable capacity module used by the Matter Storage Matrix. */
public class MatterStorageCellItem extends Item {
    private final int capacity;
    public MatterStorageCellItem(Properties properties, int capacity) { super(properties); this.capacity = capacity; }
    public int capacity() { return capacity; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Matrix capacity: " + capacity + " Matter").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("Install in a Matter Storage Matrix; removable when stored Matter fits remaining cells.").withStyle(ChatFormatting.GRAY));
    }
}