package matteroverdrive.network;

import matteroverdrive.client.ClientDialogueOpener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record NpcDialoguePacket(String speaker, String title, List<String> lines) {
    public NpcDialoguePacket {
        speaker = speaker == null ? "" : speaker;
        title = title == null ? "" : title;
        lines = List.copyOf(lines == null ? List.of() : lines);
    }

    public static void encode(NpcDialoguePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.speaker, 96);
        buffer.writeUtf(packet.title, 128);
        buffer.writeVarInt(Math.min(12, packet.lines.size()));
        for (int i = 0; i < Math.min(12, packet.lines.size()); i++) buffer.writeUtf(packet.lines.get(i), 512);
    }

    public static NpcDialoguePacket decode(FriendlyByteBuf buffer) {
        String speaker = buffer.readUtf(96);
        String title = buffer.readUtf(128);
        int count = Math.min(12, Math.max(0, buffer.readVarInt()));
        List<String> lines = new ArrayList<>(count);
        for (int i = 0; i < count; i++) lines.add(buffer.readUtf(512));
        return new NpcDialoguePacket(speaker, title, lines);
    }

    public static void handle(NpcDialoguePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientDialogueOpener.open(packet.speaker, packet.title, packet.lines)));
        context.setPacketHandled(true);
    }
}
