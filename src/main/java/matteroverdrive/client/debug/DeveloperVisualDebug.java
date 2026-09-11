package matteroverdrive.client.debug;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client-only visual diagnostics used while developing Matter Overdrive screens and models.
 * Everything defaults off and has no gameplay/server effect.
 */
public final class DeveloperVisualDebug {
    private static final int NORMAL = 0xFF58A6FF;
    private static final int HOVERED = 0xFFFFD166;
    private static final int OVERLAP = 0xFFFF5D73;
    private static final int ANCHOR = 0xFFE6EDF3;
    private static final int PANEL = 0xD9101419;
    private static final int TEXT = 0xFFF0F6FC;
    private static final int MUTED = 0xFFAAB7C4;

    private static boolean guiOverlayEnabled;
    private static boolean modelOverlayEnabled;
    private static ModelTransformSnapshot lastModelTransform;

    private DeveloperVisualDebug() {}

    public static boolean isGuiOverlayEnabled() {
        return guiOverlayEnabled;
    }

    public static boolean toggleGuiOverlay() {
        guiOverlayEnabled = !guiOverlayEnabled;
        return guiOverlayEnabled;
    }

    public static boolean isModelOverlayEnabled() {
        return modelOverlayEnabled;
    }

    public static boolean toggleModelOverlay() {
        modelOverlayEnabled = !modelOverlayEnabled;
        return modelOverlayEnabled;
    }

    /**
     * Render diagnostics for the real clickable regions of an opt-in screen.
     * Regions that overlap are highlighted red; the hovered region shows exact geometry.
     */
    public static void renderButtonDiagnostics(GuiGraphics graphics, Font font,
                                               Map<Button, ?> regions,
                                               int mouseX, int mouseY,
                                               int screenWidth, int screenHeight) {
        if (!guiOverlayEnabled || regions.isEmpty()) return;

        List<Button> buttons = new ArrayList<>(regions.keySet());
        boolean[] overlaps = new boolean[buttons.size()];
        int overlapPairs = 0;
        int overlapPixels = 0;
        for (int i = 0; i < buttons.size(); i++) {
            for (int j = i + 1; j < buttons.size(); j++) {
                int area = intersectionArea(buttons.get(i), buttons.get(j));
                if (area > 0) {
                    overlaps[i] = true;
                    overlaps[j] = true;
                    overlapPairs++;
                    overlapPixels += area;
                }
            }
        }

        Button hovered = null;
        Object hoveredValue = null;
        for (int i = 0; i < buttons.size(); i++) {
            Button button = buttons.get(i);
            boolean isHovered = contains(button, mouseX, mouseY);
            int color = overlaps[i] ? OVERLAP : isHovered ? HOVERED : NORMAL;
            outline(graphics, button.getX(), button.getY(), button.getWidth(), button.getHeight(), color);
            cross(graphics, button.getX(), button.getY(), color);
            cross(graphics, button.getX() + button.getWidth() / 2, button.getY() + button.getHeight() / 2, ANCHOR);
            if (isHovered) {
                hovered = button;
                hoveredValue = regions.get(button);
            }
        }

        int panelWidth = Math.min(306, Math.max(220, screenWidth - 8));
        int panelHeight = hovered == null ? 32 : 58;
        int panelX = Math.max(4, screenWidth - panelWidth - 4);
        int panelY = Math.max(4, screenHeight - panelHeight - 4);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL);
        int textWidth = Math.max(1, panelWidth - 10);
        graphics.drawString(font, fit(font,
                "DEV GUI [F8]  regions=" + buttons.size() + "  overlaps=" + overlapPairs + " (" + overlapPixels + "px)", textWidth),
                panelX + 5, panelY + 5, overlapPairs > 0 ? OVERLAP : TEXT, false);
        graphics.drawString(font, fit(font, "mouse=" + mouseX + "," + mouseY, textWidth),
                panelX + 5, panelY + 17, MUTED, false);

        if (hovered != null) {
            String id = regionName(hovered, hoveredValue);
            graphics.drawString(font, fit(font, id, textWidth), panelX + 5, panelY + 31, HOVERED, false);
            graphics.drawString(font, fit(font,
                    "x=" + hovered.getX() + " y=" + hovered.getY()
                            + " w=" + hovered.getWidth() + " h=" + hovered.getHeight()
                            + " center=" + (hovered.getX() + hovered.getWidth() / 2)
                            + "," + (hovered.getY() + hovered.getHeight() / 2),
                    textWidth),
                    panelX + 5, panelY + 43, TEXT, false);
        }
    }

    /**
     * Renderer hooks and the JSON model inspector can publish the active transform here.
     * The latest value is intentionally client-local and ephemeral.
     */
    public static void recordModelTransform(String itemId, String context,
                                            float translateX, float translateY, float translateZ,
                                            float rotateX, float rotateY, float rotateZ,
                                            float scaleX, float scaleY, float scaleZ) {
        if (!modelOverlayEnabled) return;
        lastModelTransform = new ModelTransformSnapshot(itemId, context,
                translateX, translateY, translateZ,
                rotateX, rotateY, rotateZ,
                scaleX, scaleY, scaleZ);
    }

    public static ModelTransformSnapshot lastModelTransform() {
        return lastModelTransform;
    }

    private static String regionName(Button button, Object value) {
        if (value instanceof Enum<?> enumValue) return enumValue.getDeclaringClass().getSimpleName() + "." + enumValue.name();
        if (value != null) return value.getClass().getSimpleName() + " // " + button.getMessage().getString();
        return button.getMessage().getString();
    }

    private static String fit(Font font, String value, int maxWidth) {
        if (value == null || maxWidth <= 0 || font.width(value) <= maxWidth) return value;
        String ellipsis = "…";
        return font.plainSubstrByWidth(value, Math.max(0, maxWidth - font.width(ellipsis))) + ellipsis;
    }

    private static boolean contains(Button button, int x, int y) {
        return x >= button.getX() && x < button.getX() + button.getWidth()
                && y >= button.getY() && y < button.getY() + button.getHeight();
    }

    private static int intersectionArea(Button a, Button b) {
        int left = Math.max(a.getX(), b.getX());
        int top = Math.max(a.getY(), b.getY());
        int right = Math.min(a.getX() + a.getWidth(), b.getX() + b.getWidth());
        int bottom = Math.min(a.getY() + a.getHeight(), b.getY() + b.getHeight());
        if (right <= left || bottom <= top) return 0;
        return (right - left) * (bottom - top);
    }

    private static void outline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static void cross(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x - 2, y, x + 3, y + 1, color);
        graphics.fill(x, y - 2, x + 1, y + 3, color);
    }

    public record ModelTransformSnapshot(String itemId, String context,
                                         float translateX, float translateY, float translateZ,
                                         float rotateX, float rotateY, float rotateZ,
                                         float scaleX, float scaleY, float scaleZ) {}
}
