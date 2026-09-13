package matteroverdrive.client;

import matteroverdrive.client.screen.DataPadScreen;
import net.minecraft.client.Minecraft;

import java.util.List;

/** The PDA always opens its compact live screen; GuideME remains one click away as the full manual. */
public final class ClientDataPadOpener {
    private ClientDataPadOpener() {}
    public static void open(List<String> history) { Minecraft.getInstance().setScreen(new DataPadScreen(history)); }
}
