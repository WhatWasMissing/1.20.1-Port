package matteroverdrive.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class EnergyPackItem extends Item {
    public static final int ENERGY_AMOUNT = 32_000;

    public EnergyPackItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Reloads " + ENERGY_AMOUNT + " FE into an energy weapon").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Consumed automatically when a weapon cannot fire").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
