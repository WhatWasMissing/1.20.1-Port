package matteroverdrive.client.screen;

import net.minecraft.client.gui.GuiGraphics;

public final class LegacyGuiPrimitives {
    private LegacyGuiPrimitives() {
    }

    public static void drawDualRing(GuiGraphics graphics, int centerX, int centerY, int radius,
                                    int energy, int energyCapacity, int matter, int matterCapacity) {
        double energyProgress = ratio(energy, energyCapacity);
        double matterProgress = ratio(matter, matterCapacity);
        drawArc(graphics, centerX, centerY, radius, Math.PI * 0.55D, Math.PI * 1.45D,
                40, energyProgress, MachineScreenStyle.RED);
        drawArc(graphics, centerX, centerY, radius, -Math.PI * 0.45D, Math.PI * 0.45D,
                40, matterProgress, MachineScreenStyle.BLUE);
        drawArc(graphics, centerX, centerY, radius - 4, Math.PI * 0.55D, Math.PI * 1.45D,
                40, 1.0D, 0x39464F);
        drawArc(graphics, centerX, centerY, radius - 4, -Math.PI * 0.45D, Math.PI * 0.45D,
                40, 1.0D, 0x39464F);
    }

    public static void drawStatusLamp(GuiGraphics graphics, int x, int y, boolean active, int activeColor) {
        int color = active ? activeColor : 0x3A4248;
        graphics.fill(x, y, x + 6, y + 6, 0xFF000000 | color);
        graphics.fill(x + 1, y + 1, x + 5, y + 5, active ? 0xFFFFFFFF : 0xFF68747C);
        if (active) graphics.fill(x + 2, y + 2, x + 4, y + 4, 0xFF000000 | color);
    }

    private static void drawArc(GuiGraphics graphics, int centerX, int centerY, int radius,
                                double start, double end, int segments, double progress, int color) {
        int lit = (int) Math.round(Math.max(0.0D, Math.min(1.0D, progress)) * segments);
        for (int i = 0; i < segments; i++) {
            double t = segments <= 1 ? 0.0D : (double) i / (segments - 1);
            double angle = start + (end - start) * t;
            int x = centerX + (int) Math.round(Math.cos(angle) * radius);
            int y = centerY + (int) Math.round(Math.sin(angle) * radius);
            int drawColor = i < lit ? color : 0x27343D;
            graphics.fill(x - 1, y - 1, x + 2, y + 2, 0xFF000000 | drawColor);
        }
    }

    private static double ratio(int value, int max) {
        if (max <= 0 || value <= 0) return 0.0D;
        return Math.min(1.0D, (double) value / max);
    }
}
