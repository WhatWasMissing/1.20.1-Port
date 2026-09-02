package matteroverdrive.network;

import matteroverdrive.client.ClientDocumentationOpener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record DocumentationOpenPacket(int documentId) {
    public static void encode(DocumentationOpenPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.documentId);
    }

    public static DocumentationOpenPacket decode(FriendlyByteBuf buffer) {
        return new DocumentationOpenPacket(buffer.readVarInt());
    }

    public static void handle(DocumentationOpenPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> ClientDocumentationOpener.open(packet.documentId)));
        context.setPacketHandled(true);
    }
}
