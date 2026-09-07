package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.compat.PointBlankDestinyCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Player-facing bridge information for optional Point Blank Destiny weapons. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PointBlankDestinyCompatEvents {
    private PointBlankDestinyCompatEvents() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        PointBlankDestinyCompat.Weapon weapon = PointBlankDestinyCompat.definition(event.getItemStack());
        if (weapon == null) return;

        event.getToolTip().add(Component.literal("Matter Overdrive // Destiny Integration")
                .withStyle(ChatFormatting.AQUA));
        event.getToolTip().add(Component.literal("Android Aspects and Fragments apply to player-sourced weapon damage.")
                .withStyle(ChatFormatting.DARK_GRAY));
        event.getToolTip().add(Component.literal("Matter value: " + weapon.matterValue())
                .withStyle(ChatFormatting.GRAY));
    }
}
