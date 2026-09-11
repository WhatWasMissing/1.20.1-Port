package matteroverdrive.network;

import matteroverdrive.client.ClientWelcomeOpener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Opens the client field briefing after the player's first login to a world. */
public record WelcomeBriefingPacket() {
    public static void encode(WelcomeBriefingPacket packet, FriendlyByteBuf buffer) { }
    public static WelcomeBriefingPacket decode(FriendlyByteBuf buffer) { return new WelcomeBriefingPacket(); }

    public static void handle(WelcomeBriefingPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientWelcomeOpener.openFirstWorldBriefing()));
        context.setPacketHandled(true);
    }
}
