package matteroverdrive.client;

import matteroverdrive.client.screen.DataPadScreen;
import net.minecraft.client.Minecraft;

import java.util.List;

/** Opens Matter Overdrive's player-specific PDA. GuideME never replaces this screen. */
public final class ClientDataPadOpener {
    private static boolean startupAnnounced;

    private ClientDataPadOpener() { }

    public static void open(List<String> history, int loreMask) {
        Minecraft.getInstance().setScreen(new DataPadScreen(history, loreMask));
        if (!startupAnnounced) {
            startupAnnounced = true;
            ClientPdaVoiceOpener.play("database_ready");
        }
    }
}
