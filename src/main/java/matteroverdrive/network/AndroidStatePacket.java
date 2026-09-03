package matteroverdrive.network;

import matteroverdrive.client.AndroidClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AndroidStatePacket(boolean active, int energy, int parts,
                                 int selectedAbility, int cooldownTicks, int activeAbilityFlags, int experience, int level) {
    public static void encode(AndroidStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.active);
        buffer.writeVarInt(packet.energy);
        buffer.writeByte(packet.parts);
        buffer.writeByte(packet.selectedAbility);
        buffer.writeVarInt(packet.cooldownTicks);
        buffer.writeByte(packet.activeAbilityFlags);
        buffer.writeVarInt(packet.experience);
        buffer.writeByte(packet.level);
    }

    public static AndroidStatePacket decode(FriendlyByteBuf buffer) {
        return new AndroidStatePacket(
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readUnsignedByte(),
                buffer.readUnsignedByte(),
                buffer.readVarInt(),
                buffer.readUnsignedByte(),
                buffer.readVarInt(),
                buffer.readUnsignedByte()
        );
    }

    public static void handle(AndroidStatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> AndroidClientState.set(
                        packet.active,
                        packet.energy,
                        packet.parts,
                        packet.selectedAbility,
                        packet.cooldownTicks,
                        packet.activeAbilityFlags,
                        packet.experience,
                        packet.level
                )));
        context.setPacketHandled(true);
    }
}
