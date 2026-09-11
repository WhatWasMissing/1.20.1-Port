package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.world.WorldOnboardingSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModWelcomeEvents {
    private static final Map<UUID, Integer> PENDING_BRIEFING = new HashMap<>();
    private static final int BRIEFING_DELAY_TICKS = 40;

    private ModWelcomeEvents() { }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.sendSystemMessage(
                Component.literal("[Matter Overdrive] ").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                        .append(Component.literal(MatterOverdrive.DISPLAY_VERSION).withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(" • Made by " + MatterOverdrive.AUTHOR).withStyle(ChatFormatting.GRAY))
        );
        player.sendSystemMessage(
                Component.literal("Data Pad: personal archive and campaign. Technical Manual: machine operation and reference.")
                        .withStyle(ChatFormatting.DARK_AQUA)
        );

        WorldOnboardingSavedData data = WorldOnboardingSavedData.get(player.serverLevel());
        if (!data.hasBriefing(player.getUUID())) {
            PENDING_BRIEFING.put(player.getUUID(), BRIEFING_DELAY_TICKS);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        Integer remaining = PENDING_BRIEFING.get(player.getUUID());
        if (remaining == null) return;
        if (remaining > 0) {
            PENDING_BRIEFING.put(player.getUUID(), remaining - 1);
            return;
        }

        PENDING_BRIEFING.remove(player.getUUID());
        WorldOnboardingSavedData data = WorldOnboardingSavedData.get(player.serverLevel());
        if (data.markBriefed(player.getUUID())) {
            ModNetwork.openWelcomeBriefing(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PENDING_BRIEFING.remove(event.getEntity().getUUID());
    }
}
