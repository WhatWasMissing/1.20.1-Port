package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModWelcomeEvents {
    private ModWelcomeEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        player.sendSystemMessage(
                Component.literal("[Matter Overdrive] ").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                        .append(Component.literal(MatterOverdrive.DISPLAY_VERSION)
                                .withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(" • Made by " + MatterOverdrive.AUTHOR)
                                .withStyle(ChatFormatting.GRAY))
        );
        player.sendSystemMessage(
                Component.literal("Use the M2 Testing Checklist and Current Feature Reference items for in-game documentation.")
                        .withStyle(ChatFormatting.DARK_AQUA)
        );
    }
}
