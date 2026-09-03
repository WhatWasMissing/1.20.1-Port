package matteroverdrive.network;

import matteroverdrive.item.weapon.OmniToolItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client request to use the legacy Omni Tool attack input.
 *
 * No target, direction, damage or timing data is accepted from the client. The
 * server reads the player's current main hand and look vector, while the item
 * itself enforces cooldown, energy, heat and spectator checks.
 */
public record OmniToolFirePacket() {
    public static void encode(OmniToolFirePacket packet, FriendlyByteBuf buffer) {
    }

    public static OmniToolFirePacket decode(FriendlyByteBuf buffer) {
        return new OmniToolFirePacket();
    }

    public static void handle(OmniToolFirePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> {
                ItemStack stack = sender.getMainHandItem();
                if (stack.getItem() instanceof OmniToolItem omniTool) {
                    omniTool.fireFromInput(sender, stack);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
