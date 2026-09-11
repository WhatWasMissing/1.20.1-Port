package matteroverdrive.network;

import matteroverdrive.client.ClientDroneStatusOpener;
import matteroverdrive.entity.DroneEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** Snapshot of loaded linked drones for the operator management screen. */
public record DroneStatusPacket(List<DroneInfo> drones) {
    private static final int MAX_DRONES = DroneCommandPacket.MAX_MANAGED_DRONES;

    public record DroneInfo(UUID id, String name, byte role, byte mode, int health, int maxHealth,
                            int energy, int maxEnergy, int distance, BlockPos patrolAnchor, BlockPos logisticsTarget) {}

    public static DroneStatusPacket from(ServerPlayer player) {
        List<DroneInfo> result = new ArrayList<>();
        for (DroneEntity drone : DroneCommandPacket.ownedLoadedDrones(player)) {
            result.add(new DroneInfo(
                    drone.getUUID(),
                    drone.getDisplayName().getString(),
                    drone.getDroneType(),
                    drone.getCommandMode(),
                    Math.max(0, Math.round(drone.getHealth())),
                    Math.max(1, Math.round(drone.getMaxHealth())),
                    drone.getDroneEnergy(),
                    DroneEntity.MAX_DRONE_ENERGY,
                    Math.max(0, (int) Math.round(Math.sqrt(drone.distanceToSqr(player)))),
                    drone.getPatrolAnchor(), drone.getLogisticsTarget()));
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
            buffer.writeByte(info.role);
            buffer.writeByte(info.mode);
            buffer.writeVarInt(info.health);
            buffer.writeVarInt(info.maxHealth);
            buffer.writeVarInt(info.energy);
            buffer.writeVarInt(info.maxEnergy);
            buffer.writeVarInt(info.distance);
            buffer.writeBoolean(info.patrolAnchor != null);
            if (info.patrolAnchor != null) buffer.writeBlockPos(info.patrolAnchor);
            buffer.writeBoolean(info.logisticsTarget != null);
            if (info.logisticsTarget != null) buffer.writeBlockPos(info.logisticsTarget);
        }
    }

    public static DroneStatusPacket decode(FriendlyByteBuf buffer) {
        int count = Mth.clamp(buffer.readVarInt(), 0, MAX_DRONES);
        List<DroneInfo> drones = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            UUID id = buffer.readUUID();
            String name = buffer.readUtf(96);
            byte role = buffer.readByte();
            byte mode = buffer.readByte();
            int health = buffer.readVarInt();
            int maxHealth = buffer.readVarInt();
            int energy = buffer.readVarInt();
            int maxEnergy = buffer.readVarInt();
            int distance = buffer.readVarInt();
            BlockPos patrolAnchor = buffer.readBoolean() ? buffer.readBlockPos() : null;
            BlockPos logisticsTarget = buffer.readBoolean() ? buffer.readBlockPos() : null;
            drones.add(new DroneInfo(id, name, role, mode, health, maxHealth, energy, maxEnergy, distance, patrolAnchor, logisticsTarget));
        }
        return new DroneStatusPacket(List.copyOf(drones));
    }

    public static void handle(DroneStatusPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientDroneStatusOpener.open(packet.drones)));
        context.setPacketHandled(true);
    }
}
