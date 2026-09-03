package matteroverdrive.network;

import matteroverdrive.android.AndroidData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AndroidPerkSelectPacket(int perkId) {
    public static void encode(AndroidPerkSelectPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.perkId);
    }

    public static AndroidPerkSelectPacket decode(FriendlyByteBuf buffer) {
        return new AndroidPerkSelectPacket(buffer.readVarInt());
    }

    public static void handle(AndroidPerkSelectPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> {
                AndroidData.Perk[] perks = AndroidData.Perk.values();
                if (packet.perkId == -1) {
                    if (AndroidData.tryResetPerks(sender)) {
                        sender.displayClientMessage(Component.literal("Android perks reset for "
                                + AndroidData.PERK_RESET_COST + " FE.").withStyle(ChatFormatting.YELLOW), true);
                    } else {
                        sender.displayClientMessage(Component.literal("Perk reset requires selected perks and "
                                + AndroidData.PERK_RESET_COST + " Android FE.").withStyle(ChatFormatting.RED), true);
                    }
                    ModNetwork.syncAndroidState(sender);
                } else if (packet.perkId <= -2) {
                    long refundIndex = -2L - packet.perkId;
                    if (refundIndex >= 0L && refundIndex < perks.length) {
                        AndroidData.Perk perk = perks[(int) refundIndex];
                        if (AndroidData.tryRefundPerk(sender, perk)) {
                            sender.displayClientMessage(Component.literal("Refunded perk: " + perk.displayName
                                            + " for " + AndroidData.PERK_REFUND_COST + " FE.")
                                    .withStyle(ChatFormatting.YELLOW), true);
                        } else {
                            sender.displayClientMessage(Component.literal("That perk is not installed or you need "
                                            + AndroidData.PERK_REFUND_COST + " Android FE.")
                                    .withStyle(ChatFormatting.RED), true);
                        }
                        ModNetwork.syncAndroidState(sender);
                    }
                } else if (packet.perkId >= 0 && packet.perkId < perks.length) {
                    AndroidData.Perk perk = perks[packet.perkId];
                    if (AndroidData.selectPerk(sender, perk)) {
                        sender.displayClientMessage(Component.literal("Installed perk: " + perk.displayName)
                                .withStyle(ChatFormatting.GREEN), true);
                    } else {
                        sender.displayClientMessage(Component.literal("That perk is locked or its level already has a choice.")
                                .withStyle(ChatFormatting.RED), true);
                    }
                    ModNetwork.syncAndroidState(sender);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
