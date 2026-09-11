package matteroverdrive.client.screen;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.client.ClientDataPadOpener;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/** Three-page, once-per-world field briefing shown after the player's first login. */
public final class FirstLoginBriefingScreen extends Screen {
    private static final int CYAN = 0xFF4DE7FF;
    private static final int TEXT = 0xFFEAFBFF;
    private static final int MUTED = 0xFF92B3BC;
    private static final int ORANGE = 0xFFFFB34E;
    private static final int PANEL = 0xEE07131C;
    private final Screen parent;
    private int page;

    public FirstLoginBriefingScreen(Screen parent) {
        super(Component.literal("Matter Overdrive Field Briefing"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearWidgets();
        int bottom = Math.min(height - 24, height / 2 + 142);
        int center = width / 2;
        if (page > 0) {
            addRenderableWidget(Button.builder(Component.literal("< BACK"), b -> {
                page--;
                init();
            }).bounds(center - 154, bottom - 22, 92, 20).build());
        }
        if (page < 2) {
            addRenderableWidget(Button.builder(Component.literal("CONTINUE >"), b -> {
                page++;
                init();
            }).bounds(center + 62, bottom - 22, 92, 20).build());
        } else {
            addRenderableWidget(Button.builder(Component.literal("ENTER WORLD"), b -> onClose())
                    .bounds(center + 62, bottom - 22, 92, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("SKIP"), b -> onClose())
                .bounds(center - 46, bottom - 22, 92, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int panelW = Math.min(600, Math.max(360, width - 40));
        int panelH = Math.min(310, Math.max(238, height - 54));
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;
        int right = left + panelW;
        int bottom = top + panelH;

        graphics.fill(left, top, right, bottom, PANEL);
        graphics.fill(left, top, right, top + 2, CYAN);
        graphics.fill(left, bottom - 1, right, bottom, 0xFF16424F);
        graphics.drawString(font, Component.literal("MATTER OVERDRIVE // FIELD LINK"), left + 18, top + 16, CYAN, false);
        graphics.drawString(font, Component.literal("PORT " + MatterOverdrive.DISPLAY_VERSION + " • MVQ1303"),
                right - 18 - font.width("PORT " + MatterOverdrive.DISPLAY_VERSION + " • MVQ1303"), top + 16, MUTED, false);
        graphics.drawString(font, Component.literal(String.format("BRIEFING %02d / 03", page + 1)), left + 18, top + 34, MUTED, false);

        int x = left + 24;
        int y = top + 60;
        int maxWidth = panelW - 48;
        Page content = pageContent(page);
        graphics.drawString(font, Component.literal(content.title()), x, y, ORANGE, false);
        y += 18;
        y = paragraph(graphics, content.lead(), x, y, maxWidth, TEXT);
        y += 8;
        for (String line : content.points()) {
            y = paragraph(graphics, "• " + line, x + 8, y, maxWidth - 8, TEXT);
            y += 4;
        }
        if (!content.note().isBlank() && y < bottom - 56) {
            y += 4;
            paragraph(graphics, content.note(), x, y, maxWidth, MUTED);
        }

        graphics.drawString(font, Component.literal("This briefing appears once per player, per world."),
                left + 18, bottom - 40, MUTED, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private Page pageContent(int index) {
        return switch (index) {
            case 1 -> new Page("YOUR FIELD KIT",
                    "Matter Overdrive is intentionally discovered through machines, ruins, people and recovered records rather than one mandatory quest corridor.",
                    List.of(
                            "The Data Pad is your personal archive: research, Field Operations, contracts, scans and incident records.",
                            "The technical manual explains machines and systems. Use it when you want exact operating instructions.",
                            "Matter technology progresses from analysis and decomposition into replication, automation, fusion power and anomaly engineering."),
                    "Researchers, salvagers and synthetic survivors now occupy appropriate recovered facilities. They can provide context that the old ruins cannot.");
            case 2 -> new Page("THE OVERDRIVE INCIDENT",
                    "Sixteen major facilities preserve a non-linear evidence trail. Their chronological order is not their discovery order.",
                    List.of(
                            "Recover local records. The PDA automatically cross-references independent evidence.",
                            "Completed evidence chains unlock Incident reconstructions rather than simply dumping the answer into chat.",
                            "ORPHEUS security, rogue Androids, M-zero resonance and anomaly hazards can still be active inside apparently abandoned sites."),
                    "One standing instruction appears across records that should not share a causal source: DO NOT COMPLETE THE LOOP.");
            default -> new Page("FIELD LINK ESTABLISHED",
                    "Matter Overdrive infrastructure has been detected in this world. The port keeps the original mod's matter-and-Android identity while expanding it into a larger exploration and systems campaign.",
                    List.of(
                            "Explore normally. Major structures are designed as traversable locations with entrances, objectives, side rewards and return routes.",
                            "Follow whatever catches your attention first: matter processing, Android progression, drones, weapons, fusion engineering or the incident archive.",
                            "The retired Star Map is not part of progression. World discovery is grounded in structures, field research and the PDA."),
                    "PDA callouts are always captioned. If Minecraft Narrator is enabled, important advisories and database events are also spoken aloud.");
        };
    }

    private int paragraph(GuiGraphics graphics, String text, int x, int y, int maxWidth, int color) {
        for (FormattedCharSequence line : font.split(Component.literal(text), maxWidth)) {
            graphics.drawString(font, line, x, y, color, false);
            y += 11;
        }
        return y;
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    private record Page(String title, String lead, List<String> points, String note) { }
}
