package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.network.WeaponTriggerPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Modern firearm mouse contract:
 * - attack / left mouse = trigger
 * - use / right mouse = ADS only
 * - shift + right mouse = the weapon's existing reload/mode action
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class WeaponTriggerInput {
    private static boolean attackWasDown;
    private static boolean aimWasDown;
    private static int heldTicks;

    private WeaponTriggerInput() {}

    public static int heldTicks() {
        return heldTicks;
    }

    public static boolean isAiming() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null && minecraft.screen == null
                && WeaponTriggerPacket.isFirearm(minecraft.player.getMainHandItem())
                && minecraft.options.keyUse.isDown()
                && !minecraft.player.isShiftKeyDown();
    }

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null
                || !WeaponTriggerPacket.isFirearm(minecraft.player.getMainHandItem())) return;

        if (event.isAttack()) {
            // Do not punch/mine with the same click used to fire.
            event.setSwingHand(false);
            event.setCanceled(true);
            return;
        }

        if (event.isUseItem() && !minecraft.player.isShiftKeyDown()) {
            // ADS is presentation-only. Cancelling vanilla use prevents RMB from entering
            // the historical Item#use firing loop. Shift-RMB remains reload/mode control.
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        boolean firearm = minecraft.player != null && minecraft.level != null && minecraft.screen == null
                && WeaponTriggerPacket.isFirearm(minecraft.player.getMainHandItem());
        boolean attackDown = firearm && minecraft.options.keyAttack.isDown();
        boolean aimDown = firearm && minecraft.options.keyUse.isDown() && !minecraft.player.isShiftKeyDown();

        // Send either edge. In particular, ADS may be pressed/released while automatic
        // fire is already held, and the server needs that change for spread calculations.
        if (attackDown != attackWasDown || aimDown != aimWasDown) {
            ModNetwork.weaponTrigger(attackDown, aimDown);
        }

        if (attackDown) heldTicks++;
        else heldTicks = 0;

        // Leaving a firearm while either state is active must release the server use item.
        if (!firearm && (attackWasDown || aimWasDown)) {
            ModNetwork.weaponTrigger(false, false);
        }

        attackWasDown = attackDown;
        aimWasDown = aimDown;
    }
}
