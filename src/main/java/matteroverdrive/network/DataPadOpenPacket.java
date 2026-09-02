package matteroverdrive.network;

import matteroverdrive.client.ClientDataPadOpener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record DataPadOpenPacket(List<String> history) {
    public DataPadOpenPacket {
        history = List.copyOf(history);
    }

    public static void encode(DataPadOpenPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.history.size());
        for (String entry : packet.history) {
            buffer.writeUtf(entry, 512);
        }
    }

    public static DataPadOpenPacket decode(FriendlyByteBuf buffer) {
        int count = Math.min(64, Math.max(0, buffer.readVarInt()));
        List<String> history = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            history.add(buffer.readUtf(512));
        }
        return new DataPadOpenPacket(history);
    }

    public static void handle(DataPadOpenPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> ClientDataPadOpener.open(packet.history)));
        context.setPacketHandled(true);
    }
}
