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

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, value = Dist.CLIENT)
public final class MatterValueTooltipEvents {
    private MatterValueTooltipEvents() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        // Minecraft rebuilds its client search trees by generating tooltips for
        // a very large number of items immediately after recipe sync. Never run
        // recursive recipe valuation from this callback: doing so blocks the
        // render thread during world join. The UI-safe lookup returns explicit,
        // tag, dynamic or already-cached recipe values, otherwise a cheap fallback.
        MatterValueRegistry.MatterValue value = MatterValueRegistry.getMatterValueForTooltip(stack);
        if (!value.hasMatter()) {
            return;
        }

        event.getToolTip().add(Component.literal("Matter: " + value.value() + " kM")
                .withStyle(ChatFormatting.AQUA));
        if (Screen.hasShiftDown()) {
            event.getToolTip().add(Component.literal("Matter source: " + sourceName(value.source()))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static String sourceName(MatterValueRegistry.ValueSource source) {
        return switch (source) {
            case DYNAMIC -> "dynamic contents";
            case EXPLICIT -> "explicit base value";
            case TAG_BASE -> "material tag base value";
            case RECIPE -> "recipe component subtotal";
            case FALLBACK -> "unresolved-item fallback";
            case NONE -> "none";
        };
    }
}
