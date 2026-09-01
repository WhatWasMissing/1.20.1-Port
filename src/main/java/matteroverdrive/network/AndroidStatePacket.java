package matteroverdrive.network;

import matteroverdrive.client.AndroidClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AndroidStatePacket(boolean active, int energy, int parts) {
    public static void encode(AndroidStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.active);
        buffer.writeVarInt(packet.energy);
        buffer.writeByte(packet.parts);
    }
    public static AndroidStatePacket decode(FriendlyByteBuf buffer) {
        return new AndroidStatePacket(buffer.readBoolean(), buffer.readVarInt(), buffer.readUnsignedByte());
    }
    public static void handle(AndroidStatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> AndroidClientState.set(packet.active, packet.energy, packet.parts)));
        context.setPacketHandled(true);
    }
}