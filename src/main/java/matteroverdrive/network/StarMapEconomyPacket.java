package matteroverdrive.network;

import matteroverdrive.blockentity.StarMapBlockEntity;
import matteroverdrive.menu.StarMapMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StarMapEconomyPacket(BlockPos mapPos, int action) {
    public static void encode(StarMapEconomyPacket p, FriendlyByteBuf b) { b.writeBlockPos(p.mapPos); b.writeVarInt(p.action); }
    public static StarMapEconomyPacket decode(FriendlyByteBuf b) { return new StarMapEconomyPacket(b.readBlockPos(), b.readVarInt()); }
    public static void handle(StarMapEconomyPacket p, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) context.enqueueWork(() -> {
            if (!(sender.containerMenu instanceof StarMapMenu menu) || !menu.mapPos().equals(p.mapPos)) return;
            if (sender.distanceToSqr(p.mapPos.getX() + .5, p.mapPos.getY() + .5, p.mapPos.getZ() + .5) > 64) return;
            if (sender.level().getBlockEntity(p.mapPos) instanceof StarMapBlockEntity map) map.requestEconomyAction(sender, p.action);
        });
        context.setPacketHandled(true);
    }
}
