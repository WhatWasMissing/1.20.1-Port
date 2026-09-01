package matteroverdrive.item;

import matteroverdrive.android.AndroidData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class AndroidPartItem extends Item {
    private final AndroidData.Part part;

    public AndroidPartItem(Properties properties, AndroidData.Part part) {
        super(properties);
        this.part = part;
    }

    public AndroidData.Part getPart() { return part; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Install at an Android Station.").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal(switch (part) {
            case HEAD -> "Head: night vision";
            case CHEST -> "Chest: damage resistance";
            case ARMS -> "Arms: increased melee damage";
            case LEGS -> "Legs: increased movement speed";
        }).withStyle(ChatFormatting.GRAY));
    }
}