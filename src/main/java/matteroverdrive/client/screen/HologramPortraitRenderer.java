package matteroverdrive.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;

import java.util.Locale;

/** Lightweight procedural portrait panel for human and synthetic contacts. */
public final class HologramPortraitRenderer {
    private HologramPortraitRenderer() {}

    public static void render(GuiGraphics graphics, Font font, int x, int y, int width, int height,
                              String speaker, boolean synthetic, int tick) {
        if (width < 36 || height < 44) return;
        int accent = synthetic ? 0xFFFFB34E : 0xFF58C7D8;
        graphics.fill(x, y, x + width, y + height, 0xCC07151C);
        graphics.fill(x, y, x + width, y + 1, accent);
        graphics.fill(x, y + height - 1, x + width, y + height, accent);
        graphics.fill(x, y, x + 1, y + height, accent);
        graphics.fill(x + width - 1, y, x + width, y + height, accent);

        int cx = x + width / 2;
        int headTop = y + 12;
        int headW = Math.max(14, width / 3);
        int headH = Math.max(18, height / 3);

        if (synthetic) {
            graphics.fill(cx - headW / 2, headTop, cx + headW / 2, headTop + headH, 0xB0295961);
            graphics.fill(cx - headW / 2 - 3, headTop + 6, cx + headW / 2 + 3, headTop + headH - 5, 0x7020444B);
            int eyeY = headTop + headH / 2 - 2;
            graphics.fill(cx - headW / 3, eyeY, cx - 2, eyeY + 2, accent);
            graphics.fill(cx + 2, eyeY, cx + headW / 3, eyeY + 2, accent);
            graphics.fill(cx - 1, headTop + headH - 5, cx + 2, headTop + headH + 6, 0xA0357A83);
        } else {
            graphics.fill(cx - headW / 2, headTop + 2, cx + headW / 2, headTop + headH, 0xA02D6872);
            graphics.fill(cx - headW / 2 + 3, headTop, cx + headW / 2 - 3, headTop + 5, 0xA0418994);
            int eyeY = headTop + headH / 2;
            graphics.fill(cx - headW / 4, eyeY, cx - headW / 4 + 2, eyeY + 1, accent);
            graphics.fill(cx + headW / 4 - 2, eyeY, cx + headW / 4, eyeY + 1, accent);
            graphics.fill(cx - 2, headTop + headH, cx + 2, headTop + headH + 7, 0x803A7882);
        }

        int shoulderY = headTop + headH + 8;
        graphics.fill(cx - headW, shoulderY, cx + headW, Math.min(y + height - 18, shoulderY + 14),
                synthetic ? 0x803B4D2B : 0x80274E58);

        int scanRange = Math.max(1, height - 8);
        int scanY = y + 4 + Math.floorMod(tick, scanRange);
        graphics.fill(x + 2, scanY, x + width - 2, scanY + 1, synthetic ? 0x50FFB34E : 0x5058C7D8);
        for (int sy = y + 4; sy < y + height - 3; sy += 5) {
            graphics.fill(x + 2, sy, x + width - 2, sy + 1, 0x1600FFFF);
        }

        String code = contactCode(speaker);
        int labelX = x + Math.max(3, (width - font.width(code)) / 2);
        graphics.drawString(font, code, labelX, y + height - 12, accent, false);
    }

    private static String contactCode(String speaker) {
        String value = speaker == null ? "" : speaker.toUpperCase(Locale.ROOT);
        if (value.contains("MORROW")) return "MRW-17";
        if (value.contains("CHORUS")) return "CHR-LINK";
        if (value.contains("HEPHAESTUS")) return "HEP-NODE";
        if (value.contains("ICARUS")) return "ICR-FIELD";
        if (value.contains("JANUS")) return "JAN-MED";
        if (value.contains("ARCHIVIST")) return "ARC-OPS";
        if (value.contains("SALVAGE")) return "SALV-OPS";
        if (value.contains("RECOVERY")) return "RCV-OPS";
        if (value.contains("RESEARCH")) return "SCI-FIELD";
        return "CONTACT";
    }
}
