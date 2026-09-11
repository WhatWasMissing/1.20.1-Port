package matteroverdrive.network;

import matteroverdrive.client.ClientPdaNotificationManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record FacilityDiscoveryPacket(String siteId, String facility, String recordTitle,
                                      int archiveIndex, String classification) {
    public FacilityDiscoveryPacket {
        siteId = siteId == null ? "" : siteId;
        facility = facility == null ? "" : facility;
        recordTitle = recordTitle == null ? "" : recordTitle;
        classification = classification == null ? "" : classification;
    }

    public static void encode(FacilityDiscoveryPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.siteId, 96);
        buffer.writeUtf(packet.facility, 160);
        buffer.writeUtf(packet.recordTitle, 192);
        buffer.writeVarInt(packet.archiveIndex);
        buffer.writeUtf(packet.classification, 128);
    }

    public static FacilityDiscoveryPacket decode(FriendlyByteBuf buffer) {
        return new FacilityDiscoveryPacket(buffer.readUtf(96), buffer.readUtf(160), buffer.readUtf(192),
                buffer.readVarInt(), buffer.readUtf(128));
    }

    public static void handle(FacilityDiscoveryPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientPdaNotificationManager.showFacilityBanner(packet.siteId, packet.facility,
                        packet.recordTitle, packet.archiveIndex, packet.classification)));
        context.setPacketHandled(true);
    }
}
