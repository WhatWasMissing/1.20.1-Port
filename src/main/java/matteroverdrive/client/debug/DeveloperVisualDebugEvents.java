package matteroverdrive.client.debug;

import matteroverdrive.MatterOverdrive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class DeveloperVisualDebugEvents {
    private DeveloperVisualDebugEvents() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft minecraft = Minecraft.getInstance();
        while (DeveloperVisualDebugKeyMappings.TOGGLE_GUI_OVERLAY.consumeClick()) {
            boolean enabled = DeveloperVisualDebug.toggleGuiOverlay();
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(
                        Component.literal("Matter Overdrive GUI diagnostics: " + (enabled ? "ON" : "OFF")),
                        true
                );
            }
        }
        while (DeveloperVisualDebugKeyMappings.TOGGLE_MODEL_OVERLAY.consumeClick()) {
            boolean enabled = DeveloperVisualDebug.toggleModelOverlay();
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(
                        Component.literal("Matter Overdrive model diagnostics: " + (enabled ? "ON" : "OFF")),
                        true
                );
            }
        }
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!DeveloperVisualDebug.isGuiOverlayEnabled()) return;

        Map<Button, Object> regions = new LinkedHashMap<>();
        for (GuiEventListener child : event.getScreen().children()) {
            if (child instanceof Button button && button.visible) regions.put(button, null);
        }
        DeveloperVisualDebug.renderButtonDiagnostics(
                event.getGuiGraphics(),
                Minecraft.getInstance().font,
                regions,
                event.getMouseX(),
                event.getMouseY(),
                event.getScreen().width,
                event.getScreen().height
        );
    }
}
