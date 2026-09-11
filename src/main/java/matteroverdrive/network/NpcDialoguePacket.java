package matteroverdrive.network;

import matteroverdrive.client.ClientDialogueOpener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record NpcDialoguePacket(String dialogueId, String nodeId, String speaker, String title,
                                List<String> lines, List<ChoiceOption> choices) {
    public record ChoiceOption(String id, String label) {
        public ChoiceOption {
            id = id == null ? "" : id;
            label = label == null ? "" : label;
        }
    }

    public NpcDialoguePacket {
        dialogueId = dialogueId == null ? "" : dialogueId;
        nodeId = nodeId == null ? "" : nodeId;
        speaker = speaker == null ? "" : speaker;
        title = title == null ? "" : title;
        lines = List.copyOf(lines == null ? List.of() : lines);
        choices = List.copyOf(choices == null ? List.of() : choices);
    }

    /** Compatibility constructor for one-way legacy conversations. */
    public NpcDialoguePacket(String speaker, String title, List<String> lines) {
        this("", "", speaker, title, lines, List.of());
    }

    public static void encode(NpcDialoguePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.dialogueId, 64);
        buffer.writeUtf(packet.nodeId, 64);
        buffer.writeUtf(packet.speaker, 96);
        buffer.writeUtf(packet.title, 128);
        buffer.writeVarInt(Math.min(12, packet.lines.size()));
        for (int i = 0; i < Math.min(12, packet.lines.size()); i++) buffer.writeUtf(packet.lines.get(i), 512);
        buffer.writeVarInt(Math.min(4, packet.choices.size()));
        for (int i = 0; i < Math.min(4, packet.choices.size()); i++) {
            ChoiceOption choice = packet.choices.get(i);
            buffer.writeUtf(choice.id, 64);
            buffer.writeUtf(choice.label, 160);
        }
    }

    public static NpcDialoguePacket decode(FriendlyByteBuf buffer) {
        String dialogueId = buffer.readUtf(64);
        String nodeId = buffer.readUtf(64);
        String speaker = buffer.readUtf(96);
        String title = buffer.readUtf(128);
        int count = Math.min(12, Math.max(0, buffer.readVarInt()));
        List<String> lines = new ArrayList<>(count);
        for (int i = 0; i < count; i++) lines.add(buffer.readUtf(512));
        int choiceCount = Math.min(4, Math.max(0, buffer.readVarInt()));
        List<ChoiceOption> choices = new ArrayList<>(choiceCount);
        for (int i = 0; i < choiceCount; i++) {
            choices.add(new ChoiceOption(buffer.readUtf(64), buffer.readUtf(160)));
        }
        return new NpcDialoguePacket(dialogueId, nodeId, speaker, title, lines, choices);
    }

    public static void handle(NpcDialoguePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientDialogueOpener.open(packet.dialogueId, packet.nodeId,
                        packet.speaker, packet.title, packet.lines, packet.choices)));
        context.setPacketHandled(true);
    }
}
