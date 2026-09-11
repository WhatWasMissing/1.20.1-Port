package matteroverdrive.client;

import matteroverdrive.client.screen.FirstLoginBriefingScreen;
import net.minecraft.client.Minecraft;

public final class ClientWelcomeOpener {
    private ClientWelcomeOpener() {}

    public static void openFirstWorldBriefing() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new FirstLoginBriefingScreen(minecraft.screen));
        ClientPdaVoiceOpener.play("field_link");
    }
}
