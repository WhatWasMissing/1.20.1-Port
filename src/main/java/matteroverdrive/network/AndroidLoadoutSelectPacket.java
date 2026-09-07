package matteroverdrive.network;

import matteroverdrive.android.AndroidLoadout;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AndroidLoadoutSelectPacket(int category, int index) {
    public static void encode(AndroidLoadoutSelectPacket packet, FriendlyByteBuf buffer) {
        buffer.writeByte(packet.category);
        buffer.writeVarInt(packet.index);
    }

    public static AndroidLoadoutSelectPacket decode(FriendlyByteBuf buffer) {
        return new AndroidLoadoutSelectPacket(buffer.readUnsignedByte(), buffer.readVarInt());
    }

    public static void handle(AndroidLoadoutSelectPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        context.enqueueWork(() -> {
            if (player == null) return;
            boolean changed = false;
            if (packet.category == 0 && packet.index >= 0 && packet.index < AndroidLoadout.Aspect.values().length) {
                changed = AndroidLoadout.toggleAspect(player, AndroidLoadout.Aspect.values()[packet.index]);
            } else if (packet.category == 1 && packet.index >= 0 && packet.index < AndroidLoadout.Fragment.values().length) {
                changed = AndroidLoadout.toggleFragment(player, AndroidLoadout.Fragment.values()[packet.index]);
            } else if (packet.category == 2) {
                AndroidLoadout.clear(player);
                changed = true;
            } else if (packet.category == 3 && packet.index >= 0 && packet.index < AndroidLoadout.Artifact.values().length) {
                changed = AndroidLoadout.selectArtifact(player, AndroidLoadout.Artifact.values()[packet.index]);
            } else if (packet.category == 4 && packet.index >= 0 && packet.index < AndroidLoadout.DronePerk.values().length) {
                changed = AndroidLoadout.toggleDronePerk(player, AndroidLoadout.DronePerk.values()[packet.index]);
            } else if (packet.category == 5 && packet.index >= 0 && packet.index < AndroidLoadout.Specialization.values().length) {
                changed = AndroidLoadout.selectSpecialization(player, AndroidLoadout.Specialization.values()[packet.index]);
            }
            if (!changed) {
                player.sendSystemMessage(Component.literal("Android loadout limit reached, prerequisite missing, or selection unavailable.")
                        .withStyle(ChatFormatting.RED));
            }
            ModNetwork.syncAndroidState(player);
        });
        context.setPacketHandled(true);
    }
}
