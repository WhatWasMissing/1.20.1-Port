package matteroverdrive.client;

import matteroverdrive.client.screen.NpcDialogueScreen;
import matteroverdrive.network.NpcDialoguePacket;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class ClientDialogueOpener {
    private ClientDialogueOpener() {}

    public static void open(String dialogueId, String nodeId, String speaker, String title,
                            List<String> lines, List<NpcDialoguePacket.ChoiceOption> choices) {
        Minecraft.getInstance().setScreen(new NpcDialogueScreen(dialogueId, nodeId, speaker, title, lines, choices));
    }

    public static void open(String speaker, String title, List<String> lines) {
        open("", "", speaker, title, lines, List.of());
    }
}
