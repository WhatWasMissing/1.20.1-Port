package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.weapon.OmniToolItem;
import matteroverdrive.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Restores the legacy Omni Tool mouse layout without a mixin.
 *
 * Attack is cancelled while the Omni Tool is in the main hand so vanilla never
 * mines or melee-attacks with the same click. A small, throttled request is sent
 * while the bound attack key is held; the server performs all authoritative
 * cooldown, energy, heat, direction and hit validation.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class OmniToolClientInput {
    private static long lastFireRequestTick = Long.MIN_VALUE;

    private OmniToolClientInput() {
    }

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null
                || !(minecraft.player.getMainHandItem().getItem() instanceof OmniToolItem)) {
            return;
        }

        event.setSwingHand(false);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null
                || !(minecraft.player.getMainHandItem().getItem() instanceof OmniToolItem)
                || !minecraft.options.keyAttack.isDown()) {
            return;
        }

        long tick = minecraft.level.getGameTime();
        if (lastFireRequestTick == Long.MIN_VALUE || tick < lastFireRequestTick
                || tick - lastFireRequestTick >= 2L) {
            lastFireRequestTick = tick;
            ModNetwork.fireOmniTool();
        }
    }
}
