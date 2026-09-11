package matteroverdrive.client;

import matteroverdrive.client.screen.DroneManagementScreen;
import matteroverdrive.network.DroneStatusPacket;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class ClientDroneStatusOpener {
    private ClientDroneStatusOpener() {}

    public static void open(List<DroneStatusPacket.DroneInfo> drones) {
        Minecraft.getInstance().setScreen(new DroneManagementScreen(drones));
    }
}
