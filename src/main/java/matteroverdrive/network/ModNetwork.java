package matteroverdrive.network;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.android.AndroidData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "1";
    private static int nextId;
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MatterOverdrive.MOD_ID, "network"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    private ModNetwork() {}
    public static void register() {
        CHANNEL.registerMessage(nextId++, AndroidStatePacket.class, AndroidStatePacket::encode,
                AndroidStatePacket::decode, AndroidStatePacket::handle);
    }
    public static void syncAndroidState(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new AndroidStatePacket(
                AndroidData.isAndroid(player), AndroidData.getEnergy(player), AndroidData.getParts(player)));
    }
}