package matteroverdrive.network;

import matteroverdrive.entity.DroneEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** Server-authoritative commands for the loaded drones linked to one operator. */
public record DroneCommandPacket(int action, byte mode, UUID droneId) {
    public static final int REQUEST_STATUS = 0;
    public static final int SET_ALL = 1;
    public static final int SET_ONE = 2;
    public static final int RECALL_ALL = 3;
    private static final double MANAGEMENT_RANGE = 192.0D;
    public static final int MAX_MANAGED_DRONES = 24;

    public static DroneCommandPacket requestStatus() {
        return new DroneCommandPacket(REQUEST_STATUS, DroneEntity.MODE_FOLLOW, new UUID(0L, 0L));
    }

    public static DroneCommandPacket setAll(byte mode) {
        return new DroneCommandPacket(SET_ALL, mode, new UUID(0L, 0L));
    }

    public static DroneCommandPacket setOne(UUID droneId, byte mode) {
        return new DroneCommandPacket(SET_ONE, mode, droneId);
    }

    public static DroneCommandPacket recallAll() {
        return new DroneCommandPacket(RECALL_ALL, DroneEntity.MODE_FOLLOW, new UUID(0L, 0L));
    }

    public static void encode(DroneCommandPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.action);
        buffer.writeByte(packet.mode);
        buffer.writeUUID(packet.droneId);
    }

    public static DroneCommandPacket decode(FriendlyByteBuf buffer) {
        return new DroneCommandPacket(buffer.readVarInt(), buffer.readByte(), buffer.readUUID());
    }

    public static void handle(DroneCommandPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }
        context.enqueueWork(() -> {
            List<DroneEntity> drones = ownedLoadedDrones(player);
            if (packet.action == SET_ALL || packet.action == SET_ONE) {
                byte mode = clampMode(packet.mode);
                if (packet.action == SET_ALL) {
                    for (DroneEntity drone : drones) drone.setCommandMode(mode);
                } else {
                    for (DroneEntity drone : drones) {
                        if (drone.getUUID().equals(packet.droneId)) {
                            drone.setCommandMode(mode);
                            break;
                        }
                    }
                }
            } else if (packet.action == RECALL_ALL) {
                int recalled = 0;
                for (DroneEntity drone : drones) {
                    if (drone.recallTo(player)) recalled++;
                }
                if (recalled > 0) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                            "Drone recall complete: " + recalled + " linked drone" + (recalled == 1 ? "" : "s") + " returned")
                            .withStyle(net.minecraft.ChatFormatting.AQUA), true);
                } else if (!drones.isEmpty()) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                            "Drone recall blocked: clear space around the operator")
                            .withStyle(net.minecraft.ChatFormatting.GOLD), true);
                }
            }
            ModNetwork.sendDroneStatus(player);
        });
        context.setPacketHandled(true);
    }

    public static List<DroneEntity> ownedLoadedDrones(ServerPlayer player) {
        AABB area = player.getBoundingBox().inflate(MANAGEMENT_RANGE);
        UUID owner = player.getUUID();
        return player.serverLevel().getEntitiesOfClass(DroneEntity.class, area,
                        drone -> drone.isAlive() && owner.equals(drone.getOwnerUuid())).stream()
                .sorted((a, b) -> Double.compare(a.distanceToSqr(player), b.distanceToSqr(player)))
                .limit(MAX_MANAGED_DRONES)
                .toList();
    }

    private static byte clampMode(byte mode) {
        return DroneEntity.clampCommandMode(mode);
    }
}
