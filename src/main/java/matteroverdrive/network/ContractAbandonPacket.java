package matteroverdrive.network;

import matteroverdrive.item.ContractItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Removes one explicitly selected carried contract after server-side slot validation. */
public record ContractAbandonPacket(int inventorySlot) {
    public static void encode(ContractAbandonPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.inventorySlot);
    }

    public static ContractAbandonPacket decode(FriendlyByteBuf buffer) {
        return new ContractAbandonPacket(buffer.readVarInt());
    }

    public static void handle(ContractAbandonPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }
        context.enqueueWork(() -> {
            int slot = packet.inventorySlot;
            if (slot < 0 || slot >= player.getInventory().items.size()) return;
            ItemStack stack = player.getInventory().items.get(slot);
            if (!(stack.getItem() instanceof ContractItem)) return;
            String title = ContractItem.title(stack);
            player.getInventory().items.set(slot, ItemStack.EMPTY);
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
            if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Abandoned contract: " + title), true);
        });
        context.setPacketHandled(true);
    }
}
