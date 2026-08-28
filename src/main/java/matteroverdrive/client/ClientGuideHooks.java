package matteroverdrive.client;

import matteroverdrive.client.screen.ReactorAssemblyGuideScreen;
import net.minecraft.client.Minecraft;

public final class ClientGuideHooks {
    private ClientGuideHooks() {
    }

    public static void openReactorGuide() {
        Minecraft.getInstance().setScreen(new ReactorAssemblyGuideScreen());
    }
}
