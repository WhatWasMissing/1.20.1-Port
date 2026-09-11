package matteroverdrive.network;

import matteroverdrive.client.ClientPdaNotificationManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record EnvironmentalHazardPacket(String id, String title, String detail, int severity) {
    public EnvironmentalHazardPacket {
        id = id == null ? "none" : id;
        title = title == null ? "" : title;
        detail = detail == null ? "" : detail;
        severity = Math.max(0, Math.min(3, severity));
    }

    public static void encode(EnvironmentalHazardPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.id, 96);
        buffer.writeUtf(packet.title, 160);
        buffer.writeUtf(packet.detail, 192);
        buffer.writeVarInt(packet.severity);
    }

    public static EnvironmentalHazardPacket decode(FriendlyByteBuf buffer) {
        return new EnvironmentalHazardPacket(buffer.readUtf(96), buffer.readUtf(160),
                buffer.readUtf(192), buffer.readVarInt());
    }

    public static void handle(EnvironmentalHazardPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientPdaNotificationManager.setHazard(packet.id, packet.title, packet.detail, packet.severity)));
        context.setPacketHandled(true);
    }
}
