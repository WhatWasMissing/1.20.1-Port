package matteroverdrive.client;

import matteroverdrive.client.screen.DocumentationScreen;
import matteroverdrive.client.screen.ReactorAssemblyGuideScreen;
import matteroverdrive.item.DocumentationItem;
import net.minecraft.client.Minecraft;

/** Shared entry points for player-facing Matter Overdrive documentation. */
public final class ClientGuideHooks {
    private ClientGuideHooks() {
    }

    public static void openReactorGuide() {
        Minecraft.getInstance().setScreen(new ReactorAssemblyGuideScreen());
    }

    /**
     * Opens the main manual through GuideME when available, otherwise through
     * Matter Overdrive's bundled documentation screen. GuideME is deliberately
     * an enhancement rather than a gameplay dependency.
     */
    public static void openSystemGuide() {
        if (!GuideMeCompatEvents.openGuide()) {
            Minecraft.getInstance().setScreen(new DocumentationScreen(DocumentationItem.Document.SYSTEM_GUIDE.id));
        }
    }
}
