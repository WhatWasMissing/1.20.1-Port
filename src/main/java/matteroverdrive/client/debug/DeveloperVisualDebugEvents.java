package matteroverdrive.client.debug;

import matteroverdrive.MatterOverdrive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class DeveloperVisualDebugEvents {
    private static final int MODEL_PANEL = 0xD9101419;
    private static final int MODEL_TEXT = 0xFFF0F6FC;
    private static final int MODEL_MUTED = 0xFFAAB7C4;
    private static final int MODEL_ACCENT = 0xFFFFD166;

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
            if (enabled) DeveloperModelTransformInspector.clearCache();
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

    @SubscribeEvent
    public static void onHudRender(RenderGuiEvent.Post event) {
        if (!DeveloperVisualDebug.isModelOverlayEnabled()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) return;

        DeveloperModelTransformInspector.Inspection inspection =
                DeveloperModelTransformInspector.inspectHeldItem(minecraft);
        renderModelPanel(event.getGuiGraphics(), minecraft, inspection);
    }

    private static void renderModelPanel(GuiGraphics graphics, Minecraft minecraft,
                                         DeveloperModelTransformInspector.Inspection inspection) {
        int x = 6;
        int y = 6;
        int width = Math.min(350, Math.max(230, minecraft.getWindow().getGuiScaledWidth() - 12));
        int height = inspection.transform() == null ? 43 : 68;
        graphics.fill(x, y, x + width, y + height, MODEL_PANEL);
        graphics.drawString(minecraft.font, "DEV MODEL [F9]  " + inspection.itemId(), x + 6, y + 6, MODEL_ACCENT, false);
        graphics.drawString(minecraft.font, "context: " + inspection.context(), x + 6, y + 18, MODEL_TEXT, false);

        if (inspection.transform() == null) {
            graphics.drawString(minecraft.font, shorten(inspection.source(), 50), x + 6, y + 30, MODEL_MUTED, false);
            return;
        }

        DeveloperModelTransformInspector.TransformData transform = inspection.transform();
        graphics.drawString(minecraft.font, "rot  " + vec(transform.rotation()), x + 6, y + 30, MODEL_TEXT, false);
        graphics.drawString(minecraft.font, "pos  " + vec(transform.translation()), x + 6, y + 42, MODEL_TEXT, false);
        graphics.drawString(minecraft.font, "scale " + vec(transform.scale()), x + 6, y + 54, MODEL_TEXT, false);
    }

    private static String vec(float[] values) {
        return String.format("[%.2f, %.2f, %.2f]", values[0], values[1], values[2]);
    }

    private static String shorten(String value, int max) {
        return value.length() <= max ? value : value.substring(0, Math.max(0, max - 1)) + "…";
    }
}
