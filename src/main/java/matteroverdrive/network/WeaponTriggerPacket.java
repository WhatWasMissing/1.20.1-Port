package matteroverdrive.network;

import matteroverdrive.item.weapon.EnergyWeaponItem;
import matteroverdrive.item.weapon.VexMythoclastItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client request for the dedicated firearm trigger (left mouse button).
 *
 * The packet carries only trigger edge state. Target, direction, charge duration,
 * cooldown and damage stay server-authoritative. Existing Item use/release hooks are
 * reused deliberately so each weapon keeps its tested fire, heat, FE and charge logic.
 */
public record WeaponTriggerPacket(boolean pressed) {
    public static void encode(WeaponTriggerPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.pressed);
    }

    public static WeaponTriggerPacket decode(FriendlyByteBuf buffer) {
        return new WeaponTriggerPacket(buffer.readBoolean());
    }

    public static void handle(WeaponTriggerPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> {
                if (packet.pressed) {
                    ItemStack stack = sender.getMainHandItem();
                    if (!isFirearm(stack) || sender.isSpectator()) return;

                    // Right-click has historically used crouch for reload/mode actions.
                    // The attack trigger must still work while crouched, so invoke the
                    // existing use path with that modifier masked for this one call only.
                    boolean shifted = sender.isShiftKeyDown();
                    if (shifted) sender.setShiftKeyDown(false);
                    try {
                        stack.getItem().use(sender.level(), sender, InteractionHand.MAIN_HAND);
                    } finally {
                        if (shifted) sender.setShiftKeyDown(true);
                    }
                } else {
                    ItemStack active = sender.getUseItem();
                    if (isFirearm(active)) sender.releaseUsingItem();
                }
            });
        }
        context.setPacketHandled(true);
    }

    public static boolean isFirearm(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof EnergyWeaponItem
                || stack.getItem() instanceof VexMythoclastItem);
    }
}
