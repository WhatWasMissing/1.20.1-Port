package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.matter.MatterValueRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class MatterTooltipEvents {
    private MatterTooltipEvents() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        if (Screen.hasShiftDown()) {
            int value = MatterValueRegistry.getMatter(stack);
            event.getToolTip().add(Component.literal("Matter value: " + value + " kM per item")
                    .withStyle(ChatFormatting.AQUA));
        } else {
            event.getToolTip().add(Component.literal("Hold SHIFT for matter value")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
