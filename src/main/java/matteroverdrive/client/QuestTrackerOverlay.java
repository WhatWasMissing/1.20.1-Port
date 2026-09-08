package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/** Responsive top-right tracker for active Matter Overdrive quests and contracts. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class QuestTrackerOverlay {
    private static final int MIN_WIDTH = 150;
    private static final int MAX_WIDTH = 214;
    private static final int MAX_VISIBLE = 4;
    private static final int MARGIN = 8;
    private static final int PANEL = 0xB8111418;
    private static final int ACCENT = 0xFF73D9E6;
    private static final int TITLE = 0xFFF0EEE8;
    private static final int MUTED = 0xFFA0A7AE;
    private static final int PROGRESS = 0xFFB7D57A;

    private QuestTrackerOverlay() {}

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || minecraft.screen != null) return;
        List<QuestTrackerClientState.Entry> entries = QuestTrackerClientState.entries();
        if (entries.isEmpty()) return;

        GuiGraphics g = event.getGuiGraphics();
        Font font = minecraft.font;
        int guiWidth = event.getWindow().getGuiScaledWidth();
        int guiHeight = event.getWindow().getGuiScaledHeight();
        int panelWidth = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, guiWidth / 3));
        panelWidth = Math.min(panelWidth, Math.max(110, guiWidth - MARGIN * 2));
        int x = Math.max(MARGIN, guiWidth - panelWidth - MARGIN);
        int y = MARGIN;
        int visible = Math.min(MAX_VISIBLE, entries.size());

        g.fill(x, y, x + panelWidth, y + 18, PANEL);
        g.fill(x, y, x + 2, y + 18, ACCENT);
        g.drawString(font, fit(font, "ACTIVE QUESTS / CONTRACTS", panelWidth - 16), x + 8, y + 5, TITLE, false);
        y += 22;

        for (int i = 0; i < visible; i++) {
            QuestTrackerClientState.Entry entry = entries.get(i);
            String stage = fit(font, entry.stage(), Math.max(42, panelWidth / 3));
            int stageWidth = stage.isBlank() ? 0 : font.width(stage) + 8;
            int titleWidth = Math.max(42, panelWidth - 16 - stageWidth);
            String title = fit(font, entry.title(), titleWidth);
            List<FormattedCharSequence> objective = font.split(Component.literal(entry.objective()), Math.max(60, panelWidth - 16));
            int objectiveLines = Math.max(1, Math.min(2, objective.size()));
            int height = 36 + objectiveLines * 10;
            if (y + height > guiHeight - MARGIN) break;

            g.fill(x, y, x + panelWidth, y + height, PANEL);
            g.fill(x, y, x + 2, y + height, i == 0 ? ACCENT : 0xFF56616B);
            g.drawString(font, title, x + 8, y + 5, TITLE, false);
            if (!stage.isBlank()) {
                g.drawString(font, stage, x + panelWidth - font.width(stage) - 7, y + 5, MUTED, false);
            }
            if (objective.isEmpty()) {
                g.drawString(font, "", x + 8, y + 16, MUTED, false);
            } else {
                for (int line = 0; line < objectiveLines; line++) {
                    g.drawString(font, objective.get(line), x + 8, y + 16 + line * 10, MUTED, false);
                }
            }
            g.drawString(font, fit(font, entry.progress(), panelWidth - 16), x + 8, y + height - 12, PROGRESS, false);
            y += height + 4;
        }

        if (entries.size() > visible && y + 10 < guiHeight - MARGIN) {
            String remaining = "+" + (entries.size() - visible) + " MORE ACTIVE";
            int w = font.width(remaining);
            g.drawString(font, remaining, Math.max(x + 8, x + panelWidth - w), y + 1, MUTED, false);
        }
    }

    private static String fit(Font font, String text, int maxWidth) {
        if (text == null || text.isBlank()) return "";
        if (font.width(text) <= maxWidth) return text;
        String ellipsis = "…";
        int usable = Math.max(0, maxWidth - font.width(ellipsis));
        return font.plainSubstrByWidth(text, usable) + ellipsis;
    }
}
