package matteroverdrive.network;

import matteroverdrive.client.screen.DroneManagementScreen;
import matteroverdrive.entity.DroneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** Snapshot of loaded linked drones for the operator management screen. */
public record DroneStatusPacket(List<DroneInfo> drones) {
    private static final int MAX_DRONES = 24;

    public record DroneInfo(UUID id, String name, byte mode, int health, int maxHealth, int distance) {}

    public static DroneStatusPacket from(ServerPlayer player) {
        List<DroneInfo> result = new ArrayList<>();
        for (DroneEntity drone : DroneCommandPacket.ownedLoadedDrones(player)) {
            result.add(new DroneInfo(
                    drone.getUUID(),
                    drone.getDisplayName().getString(),
                    drone.getCommandMode(),
                    Math.max(0, Math.round(drone.getHealth())),
                    Math.max(1, Math.round(drone.getMaxHealth())),
                    Math.max(0, (int) Math.round(Math.sqrt(drone.distanceToSqr(player))))));
            if (result.size() >= MAX_DRONES) break;
        }
        result.sort((a, b) -> Integer.compare(a.distance, b.distance));
        return new DroneStatusPacket(List.copyOf(result));
    }

    public static void encode(DroneStatusPacket packet, FriendlyByteBuf buffer) {
        int count = Math.min(MAX_DRONES, packet.drones.size());
        buffer.writeVarInt(count);
        for (int i = 0; i < count; i++) {
            DroneInfo info = packet.drones.get(i);
            buffer.writeUUID(info.id);
            buffer.writeUtf(info.name, 96);
            buffer.writeByte(info.mode);
            buffer.writeVarInt(info.health);
            buffer.writeVarInt(info.maxHealth);
            buffer.writeVarInt(info.distance);
        }
    }

    public static DroneStatusPacket decode(FriendlyByteBuf buffer) {
        int count = Math.min(MAX_DRONES, buffer.readVarInt());
        List<DroneInfo> drones = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            drones.add(new DroneInfo(buffer.readUUID(), buffer.readUtf(96), buffer.readByte(),
                    buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()));
        }
        return new DroneStatusPacket(List.copyOf(drones));
    }

    public static void handle(DroneStatusPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreen(new DroneManagementScreen(packet.drones));
        }));
        context.setPacketHandled(true);
    }
}
