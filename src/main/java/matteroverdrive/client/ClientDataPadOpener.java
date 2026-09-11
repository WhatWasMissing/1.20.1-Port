package matteroverdrive.client;

import matteroverdrive.client.screen.DataPadScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Opens Matter Overdrive's player-specific PDA. GuideME never replaces this screen. */
public final class ClientDataPadOpener {
    private static boolean startupAnnounced;

    private ClientDataPadOpener() { }

    public static void open(List<String> history, int loreMask, int fieldTrust, int syntheticTrust, int archiveInsight) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new DataPadScreen(history, loreMask));
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.literal(
                    "CONTACT LINK // FIELD " + fieldTrust + " // SYNTHETIC " + syntheticTrust
                            + " // ARCHIVE INSIGHT " + archiveInsight)
                    .withStyle(ChatFormatting.DARK_AQUA), true);
        }
        if (!startupAnnounced) {
            startupAnnounced = true;
            ClientPdaVoiceOpener.play("database_ready");
        }
    }
}
