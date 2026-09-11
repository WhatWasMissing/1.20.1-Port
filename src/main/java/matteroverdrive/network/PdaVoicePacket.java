package matteroverdrive.network;

import matteroverdrive.client.ClientPdaVoiceOpener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PdaVoicePacket(String lineId) {
    public PdaVoicePacket {
        lineId = lineId == null ? "" : lineId;
    }

    public static void encode(PdaVoicePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.lineId, 64);
    }

    public static PdaVoicePacket decode(FriendlyByteBuf buffer) {
        return new PdaVoicePacket(buffer.readUtf(64));
    }

    public static void handle(PdaVoicePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPdaVoiceOpener.play(packet.lineId)));
        context.setPacketHandled(true);
    }
}
