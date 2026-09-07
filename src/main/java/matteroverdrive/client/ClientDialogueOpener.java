package matteroverdrive.client;

import matteroverdrive.client.screen.NpcDialogueScreen;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class ClientDialogueOpener {
    private ClientDialogueOpener() {}

    public static void open(String speaker, String title, List<String> lines) {
        Minecraft.getInstance().setScreen(new NpcDialogueScreen(speaker, title, lines));
    }
}
