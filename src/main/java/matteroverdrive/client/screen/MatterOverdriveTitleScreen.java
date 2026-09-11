package matteroverdrive.client.screen;

import matteroverdrive.MatterOverdrive;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/** Dedicated Matter Overdrive landing/title page reachable from Minecraft's title screen. */
public final class MatterOverdriveTitleScreen extends Screen {
    private static final int BG = 0xFF02070B;
    private static final int PANEL = 0xF0081720;
    private static final int CYAN = 0xFF40E2FF;
    private static final int TEXT = 0xFFEAFBFF;
    private static final int MUTED = 0xFF7FABB7;
    private static final int ORANGE = 0xFFFFB34E;
    private final Screen parent;
    private int page;

    public MatterOverdriveTitleScreen(Screen parent) {
        super(Component.literal("Matter Overdrive"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearWidgets();
        int y = height - 42;
        int center = width / 2;
        if (page > 0) {
            addRenderableWidget(Button.builder(Component.literal("< PREV"), b -> {
                page--;
                init();
            }).bounds(center - 162, y, 90, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("RETURN"), b -> onClose())
                .bounds(center - 45, y, 90, 20).build());
        if (page < 2) {
            addRenderableWidget(Button.builder(Component.literal("NEXT >"), b -> {
                page++;
                init();
            }).bounds(center + 72, y, 90, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, BG);
        int scanStep = 22;
        for (int y = 0; y < height; y += scanStep) graphics.fill(0, y, width, y + 1, 0x160FC5DD);
        for (int x = 0; x < width; x += 38) graphics.fill(x, 0, x + 1, height, 0x0E0FC5DD);

        int panelW = Math.min(720, Math.max(390, width - 60));
        int panelH = Math.min(360, Math.max(250, height - 72));
        int left = (width - panelW) / 2;
        int top = Math.max(18, (height - panelH) / 2 - 8);
        int right = left + panelW;
        int bottom = top + panelH;
        graphics.fill(left, top, right, bottom, PANEL);
        graphics.fill(left, top, right, top + 2, CYAN);
        graphics.fill(left + 14, top + 55, right - 14, top + 56, 0xFF17505F);

        graphics.drawString(font, Component.literal("MATTER OVERDRIVE"), left + 20, top + 18, CYAN, false);
        graphics.drawString(font, Component.literal("THE OVERDRIVE INCIDENT"), left + 20, top + 34, ORANGE, false);
        String version = "FORGE 1.20.1 PORT • " + MatterOverdrive.DISPLAY_VERSION + " • " + MatterOverdrive.AUTHOR;
        graphics.drawString(font, Component.literal(version), right - 20 - font.width(version), top + 20, MUTED, false);

        Page content = content(page);
        int x = left + 24;
        int y = top + 76;
        int maxWidth = panelW - 48;
        graphics.drawString(font, Component.literal(content.title()), x, y, TEXT, false);
        y += 18;
        y = paragraph(graphics, content.lead(), x, y, maxWidth, TEXT);
        y += 10;
        for (String line : content.points()) {
            y = paragraph(graphics, "• " + line, x + 8, y, maxWidth - 8, MUTED);
            y += 5;
        }
        if (!content.footer().isBlank() && y < bottom - 40) {
            y += 5;
            paragraph(graphics, content.footer(), x, y, maxWidth, ORANGE);
        }

        String marker = String.format("SYSTEM PAGE %02d / 03", page + 1);
        graphics.drawString(font, Component.literal(marker), right - 20 - font.width(marker), bottom - 24, MUTED, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private Page content(int index) {
        return switch (index) {
            case 1 -> new Page("SYSTEMS",
                    "Matter Overdrive is built around interacting systems rather than a single content ladder.",
                    List.of(
                            "Matter: scan, analyse, decompose, store patterns and replicate matter into useful forms.",
                            "Industry: networks, routing, logistics drones, transport, charging and automated production.",
                            "Androids: conversion, energy, abilities, specialisations, fragments, aspects and drone command.",
                            "Power: solar systems, heavy FE infrastructure, fusion containment and anomaly engineering.",
                            "Combat: energy weapons, modules, hostile synthetic factions, security remnants and experimental creatures."),
                    "The technical manual documents operation. The PDA documents what happened and what you have discovered.");
            case 2 -> new Page("EXPLORE / RECOVER / DECIDE",
                    "The modern port treats the world as the campaign map. Structures are authored locations with approaches, readable routes, occupants, objectives, rewards and exits.",
                    List.of(
                            "Recover all sixteen facility records to reconstruct the Overdrive Incident.",
                            "Speak to present-day researchers, salvagers, medics, archivists and synthetic survivors.",
                            "Expect different threats at HELIX, Bastion, ICARUS, JANUS, ORPHEUS and Frontier sites.",
                            "Field Operations and contracts provide repeatable work after the main evidence trail."),
                    "The Star Map remains retired. Discovery belongs to the world, not a disconnected galaxy menu.");
            default -> new Page("PROJECT STATUS: ACTIVE",
                    "A modern Forge 1.20.1 continuation of Matter Overdrive, preserving the original mod's matter technology, Android identity and industrial science-fiction tone while expanding it into a broader playable campaign.",
                    List.of(
                            "Legacy parity and new systems coexist; neither is treated as disposable filler.",
                            "World generation uses native chunk-safe Structure and StructurePiece generation.",
                            "The Overdrive Incident ties machines, facilities, Android factions and anomaly systems into one setting.",
                            "Accessibility-first PDA narration keeps every voiced advisory captioned and every long record readable."),
                    "Some evidence is older than the machines that produced it. That is not a formatting error.");
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

    @Override public boolean isPauseScreen() { return false; }

    private record Page(String title, String lead, List<String> points, String footer) { }
}
