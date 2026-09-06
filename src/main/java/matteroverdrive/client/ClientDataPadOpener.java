package matteroverdrive.client;

import matteroverdrive.client.screen.DataPadScreen;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class ClientDataPadOpener {
    private ClientDataPadOpener() {
    }

    public static void open(List<String> history) {
        // GuideME is an optional integration. When it is present, the Matter
        // Overdrive Data Pad is the player-facing entry point to the registered
        // matteroverdrive:guide manual. If GuideME is absent, failed to register,
        // or cannot open its start page, retain the built-in Data Pad UI.
        if (GuideMeCompatEvents.isAvailable() && GuideMeCompatEvents.openGuide()) {
            return;
        }
        Minecraft.getInstance().setScreen(new DataPadScreen(history));
    }
}
