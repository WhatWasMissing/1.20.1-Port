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
 *
 * Vanilla mining/melee is suppressed while a firearm owns the attack input. Only trigger
 * edges are sent; the server owns the actual use duration and weapon gameplay rules.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class WeaponTriggerInput {
    private static boolean attackWasDown;
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
            // ADS is presentation-only. Cancelling vanilla use prevents RMB from also
            // entering the historical fire/use loop. Shift-RMB is deliberately left
            // alone for reload, Phaser power mode and Mythoclast mode switching.
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

        if (attackDown && !attackWasDown) {
            heldTicks = 0;
            ModNetwork.weaponTrigger(true);
        }
        if (attackDown) {
            heldTicks++;
        } else if (attackWasDown) {
            ModNetwork.weaponTrigger(false);
            heldTicks = 0;
        } else if (!firearm) {
            heldTicks = 0;
        }
        attackWasDown = attackDown;
    }
}
