package matteroverdrive.client.screen;

import net.minecraft.client.gui.GuiGraphics;

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
    private static final int INNER = 0xFF0E141A;
    private static final int INVENTORY = 0xFF11171D;
    private static final int SLOT_EDGE = 0xFF3C4E5A;
    private static final int SLOT_INNER = 0xFF0A0F13;
    private static final int BAR_BACK = 0xFF27343D;

    private MachineScreenStyle() {
    }

    public static void drawFrame(GuiGraphics graphics, int x, int y, int width, int height,
                                 int inventoryLabelY, int accent) {
        graphics.fill(x, y, x + width, y + height, OUTER);
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, PANEL);
        graphics.fill(x + 4, y + 4, x + width - 4, y + 23, HEADER);
        graphics.fill(x + 4, y + 23, x + width - 4, y + 25, 0xFF25343D);
        graphics.fill(x + 4, y + 23, x + 44, y + 25, 0xFF000000 | accent);

        int inventoryTop = y + Math.max(28, inventoryLabelY - 3);
        graphics.fill(x + 5, y + 28, x + width - 5, inventoryTop - 3, INNER);
        graphics.fill(x + 5, inventoryTop, x + width - 5, y + height - 5, INVENTORY);
        graphics.fill(x + 5, inventoryTop, x + width - 5, inventoryTop + 1, 0xFF2B3B45);
    }

    public static void drawSection(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFF1A242C);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF0B1116);
    }

    public static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 20, y + 20, SLOT_EDGE);
        graphics.fill(x + 1, y + 1, x + 19, y + 19, 0xFF1B252C);
        graphics.fill(x + 2, y + 2, x + 18, y + 18, SLOT_INNER);
        graphics.fill(x + 2, y + 2, x + 18, y + 3, 0xFF536A77);
    }

    public static void drawHorizontalBar(GuiGraphics graphics, int x, int y, int width, int height,
                                         int value, int max, int color) {
        graphics.fill(x, y, x + width, y + height, BAR_BACK);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF0A0F13);
        int amount = scale(value, max, Math.max(0, width - 2));
        if (amount > 0) {
            graphics.fill(x + 1, y + 1, x + 1 + amount, y + height - 1, 0xFF000000 | color);
        }
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
        graphics.fill(x, y, x + width, y + height, 0xFF253039);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF10171D);
    }

    private static int scale(int value, int max, int pixels) {
        if (max <= 0 || value <= 0 || pixels <= 0) {
            return 0;
        }
        return Math.min(pixels, Math.max(1, (int) Math.round((double) value * pixels / max)));
    }
}
