package matteroverdrive.client.screen;

import matteroverdrive.client.PdaNarrationController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsive bottom communication panel shared by legacy scientists and the
 * present-day field-team/synthetic NPC roster.
 */
public final class NpcDialogueScreen extends Screen {
    private static final int LINE_HEIGHT = 11;
    private static final int CYAN = 0xFF58C7D8;
    private static final int ORANGE = 0xFFFFB34E;
    private static final int TEXT = 0xFFDCE9ED;
    private static final int MUTED = 0xFF899DA6;

    private final String speaker;
    private final String heading;
    private final List<String> sourceLines;
    private final List<FormattedCharSequence> wrapped = new ArrayList<>();
    private int page;
    private int linesPerPage;
    private int boxHeight;
    private int portraitWidth;

    public NpcDialogueScreen(String speaker, String heading, List<String> lines) {
        super(Component.literal(heading == null || heading.isBlank() ? "Dialogue" : heading));
        this.speaker = speaker == null || speaker.isBlank() ? "Unknown Contact" : speaker;
        this.heading = heading == null ? "" : heading;
        this.sourceLines = List.copyOf(lines == null ? List.of() : lines);
    }

    @Override
    protected void init() {
        wrapped.clear();
        boxHeight = Math.min(220, Math.max(142, height / 2));
        portraitWidth = width >= 460 ? 92 : width >= 360 ? 66 : 0;
        int textWidth = Math.max(130, Math.min(600, width - 76 - (portraitWidth > 0 ? portraitWidth + 12 : 0)));
        for (String line : sourceLines) {
            if (!wrapped.isEmpty()) wrapped.add(FormattedCharSequence.EMPTY);
            wrapped.addAll(font.split(Component.literal(line == null ? "" : line), textWidth));
        }
        if (wrapped.isEmpty()) wrapped.addAll(font.split(Component.literal("..."), textWidth));
        linesPerPage = Math.max(1, (boxHeight - 92) / LINE_HEIGHT);
        page = Math.max(0, Math.min(page, pageCount() - 1));
        rebuildButtons();
    }

    private void rebuildButtons() {
        clearWidgets();
        int boxY = Math.max(22, height - boxHeight - 20);
        int buttonY = boxY + boxHeight - 28;
        int left = 20;
        int right = width - 20;

        addRenderableWidget(Button.builder(Component.literal(PdaNarrationController.isSpeaking() ? "STOP VOICE" : "READ ALOUD"), b -> {
            if (PdaNarrationController.isSpeaking()) PdaNarrationController.stop();
            else PdaNarrationController.read(speaker + ". " + heading + ". " + currentDialogueText());
            rebuildButtons();
        }).bounds(left + 12, buttonY, 92, 20).build());

        int pages = pageCount();
        if (page > 0) {
            addRenderableWidget(Button.builder(Component.literal("< BACK"), b -> {
                PdaNarrationController.stop();
                page--;
                rebuildButtons();
            }).bounds(right - 190, buttonY, 82, 20).build());
        }
        String label = page + 1 < pages ? "CONTINUE >" : "CLOSE";
        addRenderableWidget(Button.builder(Component.literal(label), b -> {
            PdaNarrationController.stop();
            if (page + 1 < pages) {
                page++;
                rebuildButtons();
            } else {
                onClose();
            }
        }).bounds(right - 100, buttonY, 88, 20).build());
    }

    private int pageCount() {
        return Math.max(1, (wrapped.size() + Math.max(1, linesPerPage) - 1) / Math.max(1, linesPerPage));
    }

    private String currentDialogueText() {
        return sourceLines.isEmpty() ? "No dialogue data received." : String.join(" ", sourceLines);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int boxX = 20;
        int boxY = Math.max(22, height - boxHeight - 20);
        int boxW = Math.max(220, width - 40);
        graphics.fill(boxX, boxY, boxX + boxW, boxY + boxHeight, 0xF20A141B);
        graphics.fill(boxX, boxY, boxX + boxW, boxY + 2, CYAN);
        graphics.fill(boxX, boxY + 40, boxX + boxW, boxY + 41, 0xFF183A44);

        String channel = syntheticSpeaker() ? "SYNTHETIC CONTACT" : "FIELD COMMUNICATION";
        graphics.drawString(font, Component.literal(channel), boxX + 14, boxY + 10, syntheticSpeaker() ? ORANGE : CYAN, false);
        graphics.drawString(font, fit(speaker, boxW - 150), boxX + 14, boxY + 24, TEXT, false);
        if (!heading.isBlank()) {
            String headingText = fit(heading, Math.max(80, boxW / 2));
            graphics.drawString(font, headingText, boxX + boxW - 14 - font.width(headingText), boxY + 24, MUTED, false);
        }

        int pages = pageCount();
        if (pages > 1) {
            String marker = String.format("%02d / %02d", page + 1, pages);
            graphics.drawString(font, marker, boxX + boxW - 14 - font.width(marker), boxY + 10, MUTED, false);
        }

        int textX = boxX + 14;
        if (portraitWidth > 0) {
            int portraitHeight = Math.max(48, Math.min(108, boxHeight - 88));
            int tick = Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount;
            HologramPortraitRenderer.render(graphics, font, boxX + 14, boxY + 50,
                    portraitWidth, portraitHeight, speaker, syntheticSpeaker(), tick);
            textX += portraitWidth + 12;
        }

        int y = boxY + 51;
        int start = page * linesPerPage;
        int end = Math.min(wrapped.size(), start + linesPerPage);
        for (int i = start; i < end && y < boxY + boxHeight - 38; i++) {
            graphics.drawString(font, wrapped.get(i), textX, y, TEXT, false);
            y += LINE_HEIGHT;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private boolean syntheticSpeaker() {
        String value = speaker.toLowerCase(java.util.Locale.ROOT);
        return value.contains("morrow") || value.contains("chorus") || value.contains("hephaestus")
                || value.contains("android") || value.contains("synthetic");
    }

    @Override
    public void onClose() {
        PdaNarrationController.stop();
        super.onClose();
    }

    @Override public boolean isPauseScreen() { return false; }

    private String fit(String text, int maxWidth) {
        if (text == null || font.width(text) <= maxWidth) return text == null ? "" : text;
        int available = Math.max(0, maxWidth - font.width("…"));
        return font.plainSubstrByWidth(text, available) + "…";
    }
}
