package matteroverdrive.network;

import matteroverdrive.item.weapon.EnergyWeaponItem;
import matteroverdrive.item.weapon.VexMythoclastItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server-authoritative firearm trigger/ADS state.
 *
 * Left mouse owns the trigger. Right mouse is independent ADS presentation. The client
 * sends state transitions only; fire cadence, charge duration, FE, heat and damage remain
 * server-side in the existing weapon item use/release hooks.
 */
public record WeaponTriggerPacket(boolean pressed, boolean aiming) {
    private static final String TRIGGER_HELD = "MatterOverdriveWeaponTriggerHeld";
    private static final String AIMING = "MatterOverdriveWeaponAiming";

    public static void encode(WeaponTriggerPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.pressed);
        buffer.writeBoolean(packet.aiming);
    }

    public static WeaponTriggerPacket decode(FriendlyByteBuf buffer) {
        return new WeaponTriggerPacket(buffer.readBoolean(), buffer.readBoolean());
    }

    public static void handle(WeaponTriggerPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> applyState(sender, packet));
        }
        context.setPacketHandled(true);
    }

    private static void applyState(ServerPlayer sender, WeaponTriggerPacket packet) {
        CompoundTag data = sender.getPersistentData();
        boolean wasPressed = data.getBoolean(TRIGGER_HELD);
        boolean validFirearm = isFirearm(sender.getMainHandItem()) && !sender.isSpectator();
        boolean pressed = packet.pressed && validFirearm;

        data.putBoolean(AIMING, packet.aiming && validFirearm);
        data.putBoolean(TRIGGER_HELD, pressed);

        if (pressed && !wasPressed) {
            ItemStack stack = sender.getMainHandItem();

            // Shift-right-click remains reload/mode control. Holding shift must not turn
            // a left-click trigger request into that alternate-use path.
            boolean shifted = sender.isShiftKeyDown();
            if (shifted) sender.setShiftKeyDown(false);
            try {
                stack.getItem().use(sender.level(), sender, InteractionHand.MAIN_HAND);
            } finally {
                if (shifted) sender.setShiftKeyDown(true);
            }
        } else if (!pressed && wasPressed) {
            ItemStack active = sender.getUseItem();
            if (isFirearm(active)) sender.releaseUsingItem();
        }
    }

    public static boolean isFirearm(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof EnergyWeaponItem
                || stack.getItem() instanceof VexMythoclastItem);
    }

    public static boolean isAiming(Player player) {
        return player != null && player.getPersistentData().getBoolean(AIMING);
    }
}
