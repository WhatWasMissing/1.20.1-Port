package matteroverdrive.network;

import matteroverdrive.dialogue.DialogueSessionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client request to choose one server-authored branch in the active NPC conversation. */
public record DialogueChoicePacket(String dialogueId, String nodeId, String choiceId) {
    public DialogueChoicePacket {
        dialogueId = safe(dialogueId);
        nodeId = safe(nodeId);
        choiceId = safe(choiceId);
    }

    public static void encode(DialogueChoicePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.dialogueId, 64);
        buffer.writeUtf(packet.nodeId, 64);
        buffer.writeUtf(packet.choiceId, 64);
    }

    public static DialogueChoicePacket decode(FriendlyByteBuf buffer) {
        return new DialogueChoicePacket(buffer.readUtf(64), buffer.readUtf(64), buffer.readUtf(64));
    }

    public static void handle(DialogueChoicePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> DialogueSessionManager.choose(sender,
                    packet.dialogueId, packet.nodeId, packet.choiceId));
        }
        context.setPacketHandled(true);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
