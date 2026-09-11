package matteroverdrive.client;

import matteroverdrive.client.screen.DocumentationScreen;
import net.minecraft.client.Minecraft;

/**
 * Opens technical documentation. The System Guide uses GuideME when available,
 * but always falls back to Matter Overdrive's bundled documentation screen.
 */
public final class ClientDocumentationOpener {
    public static final int SYSTEM_GUIDE_DOCUMENT_ID = 2;

    private ClientDocumentationOpener() {
    }

    public static void open(int documentId) {
        if (documentId == SYSTEM_GUIDE_DOCUMENT_ID && GuideMeCompatEvents.openGuide()) {
            return;
        }
        Minecraft.getInstance().setScreen(new DocumentationScreen(documentId));
    }

    public static void openTechnicalManual() {
        open(SYSTEM_GUIDE_DOCUMENT_ID);
    }
}
