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

/** Opens the PDA with server-authoritative journal, lore and contact state. */
public record DataPadOpenPacket(List<String> history, int loreMask, int fieldTrust, int syntheticTrust, int archiveInsight) {
    public DataPadOpenPacket {
        history = List.copyOf(history == null ? List.of() : history);
        loreMask &= StructureLoreCatalog.ALL_RECORDS_MASK;
        fieldTrust = Math.max(-20, Math.min(20, fieldTrust));
        syntheticTrust = Math.max(-20, Math.min(20, syntheticTrust));
        archiveInsight = Math.max(0, Math.min(100, archiveInsight));
    }

    public static void encode(DataPadOpenPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.history.size());
        for (String entry : packet.history) buffer.writeUtf(entry, 512);
        buffer.writeVarInt(packet.loreMask & StructureLoreCatalog.ALL_RECORDS_MASK);
        buffer.writeInt(packet.fieldTrust);
        buffer.writeInt(packet.syntheticTrust);
        buffer.writeVarInt(packet.archiveInsight);
    }

    public static DataPadOpenPacket decode(FriendlyByteBuf buffer) {
        int count = Math.min(64, Math.max(0, buffer.readVarInt()));
        List<String> history = new ArrayList<>(count);
        for (int i = 0; i < count; i++) history.add(buffer.readUtf(512));
        int loreMask = buffer.readVarInt() & StructureLoreCatalog.ALL_RECORDS_MASK;
        int fieldTrust = buffer.readInt();
        int syntheticTrust = buffer.readInt();
        int archiveInsight = buffer.readVarInt();
        return new DataPadOpenPacket(history, loreMask, fieldTrust, syntheticTrust, archiveInsight);
    }

    public static void handle(DataPadOpenPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientDataPadOpener.open(packet.history, packet.loreMask, packet.fieldTrust,
                        packet.syntheticTrust, packet.archiveInsight)));
        context.setPacketHandled(true);
    }
}
