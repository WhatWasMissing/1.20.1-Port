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

/** Compact top-right tracker for active Matter Overdrive quests and contracts. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class QuestTrackerOverlay {
    private static final int WIDTH = 214;
    private static final int MAX_VISIBLE = 4;
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
        int x = event.getWindow().getGuiScaledWidth() - WIDTH - 10;
        int y = 10;
        int visible = Math.min(MAX_VISIBLE, entries.size());

        g.fill(x, y, x + WIDTH, y + 18, PANEL);
        g.fill(x, y, x + 2, y + 18, ACCENT);
        g.drawString(font, "ACTIVE QUESTS", x + 8, y + 5, TITLE, false);
        y += 22;

        for (int i = 0; i < visible; i++) {
            QuestTrackerClientState.Entry entry = entries.get(i);
            List<FormattedCharSequence> objective = font.split(Component.literal(entry.objective()), WIDTH - 16);
            int objectiveLines = Math.min(2, objective.size());
            int height = 36 + objectiveLines * 10;

            g.fill(x, y, x + WIDTH, y + height, PANEL);
            g.fill(x, y, x + 2, y + height, i == 0 ? ACCENT : 0xFF56616B);
            g.drawString(font, entry.title(), x + 8, y + 5, TITLE, false);
            if (!entry.stage().isBlank()) {
                int stageWidth = font.width(entry.stage());
                g.drawString(font, entry.stage(), x + WIDTH - stageWidth - 7, y + 5, MUTED, false);
            }
            for (int line = 0; line < objectiveLines; line++) {
                g.drawString(font, objective.get(line), x + 8, y + 16 + line * 10, MUTED, false);
            }
            g.drawString(font, entry.progress(), x + 8, y + height - 12, PROGRESS, false);
            y += height + 4;
        }

        if (entries.size() > visible) {
            String remaining = "+" + (entries.size() - visible) + " MORE ACTIVE";
            int w = font.width(remaining);
            g.drawString(font, remaining, x + WIDTH - w, y + 1, MUTED, false);
        }
    }
}
