package matteroverdrive.network;

import matteroverdrive.blockentity.StarMapBlockEntity;
import matteroverdrive.menu.StarMapMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record StarMapShipDispatchPacket(BlockPos mapPos, int shipType, int q, int s, int p) {
    public static void encode(StarMapShipDispatchPacket packet, FriendlyByteBuf buf) { buf.writeBlockPos(packet.mapPos); buf.writeVarInt(packet.shipType); buf.writeVarInt(packet.q); buf.writeVarInt(packet.s); buf.writeVarInt(packet.p); }
    public static StarMapShipDispatchPacket decode(FriendlyByteBuf buf) { return new StarMapShipDispatchPacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()); }
    public static void handle(StarMapShipDispatchPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get(); ServerPlayer sender = context.getSender();
        if (sender != null) context.enqueueWork(() -> {
            if (!(sender.containerMenu instanceof StarMapMenu menu) || !menu.mapPos().equals(packet.mapPos)) return;
            if (sender.distanceToSqr(packet.mapPos.getX()+.5, packet.mapPos.getY()+.5, packet.mapPos.getZ()+.5) > 64) return;
            if (sender.level().getBlockEntity(packet.mapPos) instanceof StarMapBlockEntity map) map.requestShipDispatch(sender, packet.shipType, packet.q, packet.s, packet.p);
        });
        context.setPacketHandled(true);
    }
}
