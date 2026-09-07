package matteroverdrive.network;

import matteroverdrive.android.AndroidAbilities;
import matteroverdrive.android.AndroidUltimates;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AndroidAbilityPacket(int action) {
    public static void encode(AndroidAbilityPacket packet, FriendlyByteBuf buffer) {
        buffer.writeByte(packet.action);
    }

    public static AndroidAbilityPacket decode(FriendlyByteBuf buffer) {
        return new AndroidAbilityPacket(buffer.readUnsignedByte());
    }

    public static void handle(AndroidAbilityPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> {
                if (packet.action == AndroidUltimates.ACTION_ACTIVATE_ULTIMATE) AndroidUltimates.activate(sender);
                else AndroidAbilities.handleAction(sender, packet.action);
            });
        }
        context.setPacketHandled(true);
    }
}
