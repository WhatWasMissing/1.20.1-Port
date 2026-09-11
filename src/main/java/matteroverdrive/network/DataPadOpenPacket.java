package matteroverdrive.network;

import matteroverdrive.client.ClientDataPadOpener;
import matteroverdrive.world.StructureLoreCatalog;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Opens the PDA with the player's server-authoritative journal and lore archive state. */
public record DataPadOpenPacket(List<String> history, int loreMask) {
    public DataPadOpenPacket {
        history = List.copyOf(history);
        loreMask &= StructureLoreCatalog.ALL_RECORDS_MASK;
    }

    public static void encode(DataPadOpenPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.history.size());
        for (String entry : packet.history) {
            buffer.writeUtf(entry, 512);
        }
        buffer.writeVarInt(packet.loreMask & StructureLoreCatalog.ALL_RECORDS_MASK);
    }

    public static DataPadOpenPacket decode(FriendlyByteBuf buffer) {
        int count = Math.min(64, Math.max(0, buffer.readVarInt()));
        List<String> history = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            history.add(buffer.readUtf(512));
        }
        int loreMask = buffer.readVarInt() & StructureLoreCatalog.ALL_RECORDS_MASK;
        return new DataPadOpenPacket(history, loreMask);
    }

    public static void handle(DataPadOpenPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> ClientDataPadOpener.open(packet.history, packet.loreMask)));
        context.setPacketHandled(true);
    }
}
