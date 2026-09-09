package matteroverdrive.network;

import matteroverdrive.client.AndroidClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AndroidStatePacket(boolean active, int energy, int energyCapacity, int parts,
                                 int selectedAbility, int cooldownTicks, int activeAbilityFlags, int experience, int level,
                                 long selectedPerks, int aspectMask, int fragmentMask, int artifactOrdinal, int dronePerkMask,
                                 int specializationOrdinal, int ultimateCooldownTicks,
                                 int classAbilityCooldownTicks, int techAbilityCooldownTicks) {
    public static void encode(AndroidStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.active);
        buffer.writeVarInt(packet.energy);
        buffer.writeVarInt(packet.energyCapacity);
        buffer.writeByte(packet.parts);
        buffer.writeByte(packet.selectedAbility);
        buffer.writeVarInt(packet.cooldownTicks);
        buffer.writeByte(packet.activeAbilityFlags);
        buffer.writeVarInt(packet.experience);
        buffer.writeByte(packet.level);
        buffer.writeVarLong(packet.selectedPerks);
        buffer.writeVarInt(packet.aspectMask);
        buffer.writeVarInt(packet.fragmentMask);
        buffer.writeVarInt(packet.artifactOrdinal);
        buffer.writeVarInt(packet.dronePerkMask);
        buffer.writeVarInt(packet.specializationOrdinal);
        buffer.writeVarInt(packet.ultimateCooldownTicks);
        buffer.writeVarInt(packet.classAbilityCooldownTicks);
        buffer.writeVarInt(packet.techAbilityCooldownTicks);
    }

    public static AndroidStatePacket decode(FriendlyByteBuf buffer) {
        return new AndroidStatePacket(buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt(), buffer.readUnsignedByte(),
                buffer.readUnsignedByte(), buffer.readVarInt(), buffer.readUnsignedByte(), buffer.readVarInt(),
                buffer.readUnsignedByte(), buffer.readVarLong(), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(AndroidStatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> AndroidClientState.set(
                packet.active, packet.energy, packet.energyCapacity, packet.parts, packet.selectedAbility, packet.cooldownTicks,
                packet.activeAbilityFlags, packet.experience, packet.level, packet.selectedPerks,
                packet.aspectMask, packet.fragmentMask, packet.artifactOrdinal, packet.dronePerkMask,
                packet.specializationOrdinal, packet.ultimateCooldownTicks,
                packet.classAbilityCooldownTicks, packet.techAbilityCooldownTicks)));
        context.setPacketHandled(true);
    }
}
