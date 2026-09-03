package matteroverdrive.network;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.android.AndroidData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.List;

public final class ModNetwork {
    private static final String PROTOCOL = "1";
    private static int nextId;
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MatterOverdrive.MOD_ID, "network"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(nextId++, AndroidStatePacket.class, AndroidStatePacket::encode,
                AndroidStatePacket::decode, AndroidStatePacket::handle);
        CHANNEL.registerMessage(nextId++, AndroidAbilityPacket.class, AndroidAbilityPacket::encode,
                AndroidAbilityPacket::decode, AndroidAbilityPacket::handle);
        CHANNEL.registerMessage(nextId++, AndroidPerkSelectPacket.class, AndroidPerkSelectPacket::encode,
                AndroidPerkSelectPacket::decode, AndroidPerkSelectPacket::handle);
        CHANNEL.registerMessage(nextId++, DataPadOpenPacket.class, DataPadOpenPacket::encode,
                DataPadOpenPacket::decode, DataPadOpenPacket::handle);
        CHANNEL.registerMessage(nextId++, DocumentationOpenPacket.class, DocumentationOpenPacket::encode,
                DocumentationOpenPacket::decode, DocumentationOpenPacket::handle);
    }

    public static void openDataPad(ServerPlayer player, List<String> history) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new DataPadOpenPacket(history));
    }

    public static void openDocumentation(ServerPlayer player, int documentId) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new DocumentationOpenPacket(documentId));
    }

    public static void syncAndroidState(ServerPlayer player) {
        AndroidData.Ability selected = AndroidData.getSelectedAbility(player);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new AndroidStatePacket(
                AndroidData.isAndroid(player),
                AndroidData.getEnergy(player),
                AndroidData.getParts(player),
                selected.ordinal(),
                AndroidData.getRemainingCooldown(player, selected, player.level().getGameTime()),
                AndroidData.getActiveAbilityFlags(player),
                AndroidData.getExperience(player),
                AndroidData.getLevel(player),
                AndroidData.getSelectedPerks(player)
        ));
    }
}
