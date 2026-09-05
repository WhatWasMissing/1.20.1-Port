package matteroverdrive.network;

import matteroverdrive.blockentity.StarMapBlockEntity;
import matteroverdrive.menu.StarMapMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StarMapTravelPacket(BlockPos mapPos, int quadrant, int star, int planet) {
    public static void encode(StarMapTravelPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.mapPos);
        buffer.writeVarInt(packet.quadrant);
        buffer.writeVarInt(packet.star);
        buffer.writeVarInt(packet.planet);
    }

    public static StarMapTravelPacket decode(FriendlyByteBuf buffer) {
        return new StarMapTravelPacket(buffer.readBlockPos(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(StarMapTravelPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> {
                if (!(sender.containerMenu instanceof StarMapMenu menu) || !menu.mapPos().equals(packet.mapPos)) return;
                if (sender.distanceToSqr(packet.mapPos.getX() + 0.5D, packet.mapPos.getY() + 0.5D,
                        packet.mapPos.getZ() + 0.5D) > 64.0D) return;
                if (sender.level().getBlockEntity(packet.mapPos) instanceof StarMapBlockEntity map) {
                    map.requestTravel(sender, packet.quadrant, packet.star, packet.planet);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
