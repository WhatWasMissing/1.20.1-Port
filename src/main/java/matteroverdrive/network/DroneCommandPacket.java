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
    private static final double MANAGEMENT_RANGE = 192.0D;

    public static DroneCommandPacket requestStatus() {
        return new DroneCommandPacket(REQUEST_STATUS, DroneEntity.MODE_FOLLOW, new UUID(0L, 0L));
    }

    public static DroneCommandPacket setAll(byte mode) {
        return new DroneCommandPacket(SET_ALL, mode, new UUID(0L, 0L));
    }

    public static DroneCommandPacket setOne(UUID droneId, byte mode) {
        return new DroneCommandPacket(SET_ONE, mode, droneId);
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
            if (packet.action != REQUEST_STATUS) {
                byte mode = clampMode(packet.mode);
                List<DroneEntity> drones = ownedLoadedDrones(player);
                if (packet.action == SET_ALL) {
                    for (DroneEntity drone : drones) drone.setCommandMode(mode);
                } else if (packet.action == SET_ONE) {
                    for (DroneEntity drone : drones) {
                        if (drone.getUUID().equals(packet.droneId)) {
                            drone.setCommandMode(mode);
                            break;
                        }
                    }
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
                drone -> drone.isAlive() && owner.equals(drone.getOwnerUuid()));
    }

    private static byte clampMode(byte mode) {
        return (byte) Math.max(DroneEntity.MODE_FOLLOW, Math.min(DroneEntity.MODE_HOLD, mode));
    }
}
