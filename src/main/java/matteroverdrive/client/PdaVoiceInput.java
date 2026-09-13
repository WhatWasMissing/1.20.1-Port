package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Handles the global PDA voice toggle without interfering with text-entry screens. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PdaVoiceInput {
    private PdaVoiceInput() { }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        while (PdaKeyMappings.TOGGLE_VOICE.consumeClick()) {
            if (minecraft.screen != null) continue;
            boolean enabled = PdaVoiceSettings.toggle();
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(Component.literal(
                        "PDA voice: " + (enabled ? "ON" : "OFF") + " — captions remain enabled."), true);
            }
        }
    }
}
