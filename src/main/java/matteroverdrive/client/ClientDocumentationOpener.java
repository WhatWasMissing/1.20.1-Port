package matteroverdrive.client;

import matteroverdrive.client.screen.DocumentationScreen;
import net.minecraft.client.Minecraft;

public final class ClientDocumentationOpener {
    private ClientDocumentationOpener() {
    }

    public static void open(int documentId) {
        Minecraft.getInstance().setScreen(new DocumentationScreen(documentId));
    }
}
