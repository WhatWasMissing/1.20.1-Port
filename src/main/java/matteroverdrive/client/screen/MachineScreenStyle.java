package matteroverdrive.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;

public final class MachineScreenStyle {
    public static final int TEXT = 0xEAF4F7;
    public static final int MUTED = 0x9EB1B8;
    public static final int DEBUG = 0xF0B35A;
    public static final int DANGER = 0xFF7A7A;
    public static final int CYAN = 0x38C7E8;
    public static final int BLUE = 0x4C9EEB;
    public static final int GREEN = 0x57C47A;
    public static final int PURPLE = 0xB16DE0;
    public static final int AMBER = 0xF2B84B;
    public static final int RED = 0xE45D5D;

    private static final int OUTER = 0xFF070B0F;
    private static final int PANEL = 0xFF131A21;
    private static final int HEADER = 0xFF1C2730;
    private static final int BAR_BACK = 0xFF27343D;

    private static final ResourceLocation LEGACY_MACHINE_FRAME =
            ResourceLocation.fromNamespaceAndPath("matteroverdrive", "textures/gui/elements/base_gui_hotbar.png");
    private static final ResourceLocation LEGACY_SLOT =
            ResourceLocation.fromNamespaceAndPath("matteroverdrive", "textures/gui/elements/slot_small.png");
    private static final ResourceLocation LEGACY_PROGRESS =
            ResourceLocation.fromNamespaceAndPath("matteroverdrive", "textures/gui/elements/progress_arrow_right.png");
    private static final ResourceLocation LEGACY_ENERGY =
            ResourceLocation.fromNamespaceAndPath("matteroverdrive", "textures/gui/elements/fe.png");
    private static final ResourceLocation LEGACY_MATTER =
            ResourceLocation.fromNamespaceAndPath("matteroverdrive", "textures/gui/elements/matter.png");

    private static final int FRAME_W = 92;
    private static final int FRAME_H = 77;
    private static final int FRAME_LEFT = 57;
    private static final int FRAME_RIGHT = 34;
    private static final int FRAME_TOP = 42;
    private static final int FRAME_BOTTOM = 34;

    private MachineScreenStyle() {}

    /** Fits dynamic machine status text to a panel without relying on character counts. */
    public static String fit(Font font, String value, int maxWidth) {
        if (font.width(value) <= maxWidth) return value;
        return font.plainSubstrByWidth(value, Math.max(0, maxWidth - font.width("…"))) + "…";
    }

    public static void drawFrame(GuiGraphics graphics, int x, int y, int width, int height,
                                 int inventoryLabelY, int accent) {
        if (width >= FRAME_LEFT + FRAME_RIGHT + 1 && height >= FRAME_TOP + FRAME_BOTTOM + 1) {
            drawLegacyNineSlice(graphics, x, y, width, height);
            // Keep the recovered legacy frame, but give every modernized machine a consistent readable chrome.
            graphics.fill(x + 4, y + 4, x + width - 4, y + 22, 0xA8121B22);
        } else {
            drawStandaloneFrame(graphics, x, y, width, height, accent);
        }
        int accentColor = 0xFF000000 | accent;
        graphics.fill(x + 4, y + 4, x + 7, y + 23, accentColor);
        graphics.fill(x + 7, y + 21, x + Math.min(width - 5, 58), y + 23, accentColor);
        if (inventoryLabelY > 0 && inventoryLabelY + 1 < height) {
            graphics.fill(x + 7, y + inventoryLabelY - 5, x + width - 7, y + inventoryLabelY - 4, 0x804A5A63);
        }
    }

    private static void drawLegacyNineSlice(GuiGraphics graphics, int x, int y, int width, int height) {
        int centerWidth = width - FRAME_LEFT - FRAME_RIGHT;
        int centerHeight = height - FRAME_TOP - FRAME_BOTTOM;
        int sourceCenterX = FRAME_LEFT;
        int sourceRightX = FRAME_LEFT + 1;
        int sourceCenterY = FRAME_TOP;
        int sourceBottomY = FRAME_TOP + 1;

        drawStretch(graphics, LEGACY_MACHINE_FRAME, x, y,
                FRAME_LEFT, FRAME_TOP, 0, 0, FRAME_LEFT, FRAME_TOP, FRAME_W, FRAME_H);
        drawStretch(graphics, LEGACY_MACHINE_FRAME, x + FRAME_LEFT, y,
                centerWidth, FRAME_TOP, sourceCenterX, 0, 1, FRAME_TOP, FRAME_W, FRAME_H);
        drawStretch(graphics, LEGACY_MACHINE_FRAME, x + FRAME_LEFT + centerWidth, y,
                FRAME_RIGHT, FRAME_TOP, sourceRightX, 0, FRAME_RIGHT, FRAME_TOP, FRAME_W, FRAME_H);

        drawStretch(graphics, LEGACY_MACHINE_FRAME, x, y + FRAME_TOP,
                FRAME_LEFT, centerHeight, 0, sourceCenterY, FRAME_LEFT, 1, FRAME_W, FRAME_H);
        drawStretch(graphics, LEGACY_MACHINE_FRAME, x + FRAME_LEFT, y + FRAME_TOP,
                centerWidth, centerHeight, sourceCenterX, sourceCenterY, 1, 1, FRAME_W, FRAME_H);
        drawStretch(graphics, LEGACY_MACHINE_FRAME, x + FRAME_LEFT + centerWidth, y + FRAME_TOP,
                FRAME_RIGHT, centerHeight, sourceRightX, sourceCenterY, FRAME_RIGHT, 1, FRAME_W, FRAME_H);

        drawStretch(graphics, LEGACY_MACHINE_FRAME, x, y + FRAME_TOP + centerHeight,
                FRAME_LEFT, FRAME_BOTTOM, 0, sourceBottomY, FRAME_LEFT, FRAME_BOTTOM, FRAME_W, FRAME_H);
        drawStretch(graphics, LEGACY_MACHINE_FRAME, x + FRAME_LEFT, y + FRAME_TOP + centerHeight,
                centerWidth, FRAME_BOTTOM, sourceCenterX, sourceBottomY, 1, FRAME_BOTTOM, FRAME_W, FRAME_H);
        drawStretch(graphics, LEGACY_MACHINE_FRAME, x + FRAME_LEFT + centerWidth, y + FRAME_TOP + centerHeight,
                FRAME_RIGHT, FRAME_BOTTOM, sourceRightX, sourceBottomY, FRAME_RIGHT, FRAME_BOTTOM, FRAME_W, FRAME_H);
    }

    private static void drawStretch(GuiGraphics graphics, ResourceLocation texture,
                                    int x, int y, int width, int height,
                                    int u, int v, int sourceWidth, int sourceHeight,
                                    int textureWidth, int textureHeight) {
        if (width <= 0 || height <= 0 || sourceWidth <= 0 || sourceHeight <= 0) return;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale((float) width / (float) sourceWidth,
                (float) height / (float) sourceHeight, 1.0F);
        graphics.blit(texture, 0, 0, u, v, sourceWidth, sourceHeight, textureWidth, textureHeight);
        graphics.pose().popPose();
    }

    public static void drawStandaloneFrame(GuiGraphics graphics, int x, int y, int width, int height,
                                           int accent) {
        graphics.fill(x, y, x + width, y + height, OUTER);
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, PANEL);
        graphics.fill(x + 4, y + 4, x + width - 4, y + 23, HEADER);
        graphics.fill(x + 4, y + 23, x + width - 4, y + 25, 0xFF25343D);
        graphics.fill(x + 4, y + 23, x + 44, y + 25, 0xFF000000 | accent);
    }

    public static void drawSection(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xB82B3943);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xD80B1116);
        graphics.fill(x + 2, y + 2, x + width - 2, y + 3, 0x80394A55);
    }

    public static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 19, y + 19, 0x80465B66);
        graphics.blit(LEGACY_SLOT, x, y, 0, 0, 18, 18, 18, 18);
    }

    public static void drawLegacyProgressArrow(GuiGraphics graphics, int x, int y, int value, int max) {
        graphics.blit(LEGACY_PROGRESS, x, y, 0, 0, 24, 16, 48, 16);
        int amount = scale(value, max, 24);
        if (amount > 0) graphics.blit(LEGACY_PROGRESS, x, y, 24, 0, amount, 16, 48, 16);
    }

    public static void drawLegacyEnergyMeter(GuiGraphics graphics, int x, int y, int value, int max) {
        drawLegacyMeter(graphics, LEGACY_ENERGY, x, y, value, max);
    }

    public static void drawLegacyMatterMeter(GuiGraphics graphics, int x, int y, int value, int max) {
        drawLegacyMeter(graphics, LEGACY_MATTER, x, y, value, max);
    }

    private static void drawLegacyMeter(GuiGraphics graphics, ResourceLocation texture,
                                        int x, int y, int value, int max) {
        graphics.blit(texture, x, y, 0, 0, 16, 42, 32, 64);
        int amount = max <= 0 ? 42 : scale(value, max, 42);
        if (amount > 0) {
            graphics.blit(texture, x, y + 42 - amount, 16, 42 - amount,
                    16, amount, 32, 64);
        }
    }

    public static void drawHorizontalBar(GuiGraphics graphics, int x, int y, int width, int height,
                                         int value, int max, int color) {
        graphics.fill(x, y, x + width, y + height, BAR_BACK);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF0A0F13);
        int amount = scale(value, max, Math.max(0, width - 2));
        if (amount > 0) graphics.fill(x + 1, y + 1, x + 1 + amount, y + height - 1, 0xFF000000 | color);
    }

    public static void drawVerticalBar(GuiGraphics graphics, int x, int y, int width, int height,
                                       int value, int max, int color) {
        graphics.fill(x, y, x + width, y + height, BAR_BACK);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF0A0F13);
        int amount = scale(value, max, Math.max(0, height - 2));
        if (amount > 0) {
            graphics.fill(x + 1, y + height - 1 - amount, x + width - 1, y + height - 1,
                    0xFF000000 | color);
        }
    }

    public static void drawDebugPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xD8253039);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xD810171D);
    }

    private static int scale(int value, int max, int pixels) {
        if (max <= 0 || value <= 0 || pixels <= 0) return 0;
        return Math.min(pixels, Math.max(1, (int) Math.round((double) value * pixels / max)));
    }
}
