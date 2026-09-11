package matteroverdrive.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/** Compact bottom dialogue box for scientist and story conversations. */
public final class NpcDialogueScreen extends Screen {
    private static final int BOX_HEIGHT = 132;
    private static final int LINE_HEIGHT = 11;
    private static final int TEXT_TOP = 47;
    private static final int TEXT_BOTTOM_PADDING = 31;

    private final String speaker;
    private final String heading;
    private final List<String> sourceLines;
    private final List<FormattedCharSequence> wrapped = new ArrayList<>();
    private int page;
    private int linesPerPage;

    public NpcDialogueScreen(String speaker, String heading, List<String> lines) {
        super(Component.literal(heading == null ? "Dialogue" : heading));
        this.speaker = speaker == null || speaker.isBlank() ? "Scientist" : speaker;
        this.heading = heading == null ? "" : heading;
        this.sourceLines = List.copyOf(lines == null ? List.of() : lines);
    }

    @Override
    protected void init() {
        wrapped.clear();
        int textWidth = Math.max(180, Math.min(520, width - 72));
        for (String line : sourceLines) {
            if (!wrapped.isEmpty()) wrapped.add(FormattedCharSequence.EMPTY);
            wrapped.addAll(font.split(Component.literal(line), textWidth));
        }
        linesPerPage = Math.max(1, (BOX_HEIGHT - TEXT_TOP - TEXT_BOTTOM_PADDING) / LINE_HEIGHT + 1);
        page = Math.min(page, pageCount() - 1);
        rebuildButtons();
    }

    private void rebuildButtons() {
        clearWidgets();
        int pages = pageCount();
        int buttonWidth = Math.min(92, Math.max(40, (width - 60) / 2));
        int gap = Math.max(4, Math.min(8, width - buttonWidth * 2 - 40));
        int right = Math.max(20, width - 20);
        int continueX = right - buttonWidth;
        int backX = continueX - gap - buttonWidth;
        if (page > 0) {
            addRenderableWidget(Button.builder(Component.literal("Back"), b -> {
                page--;
                rebuildButtons();
            }).bounds(Math.max(20, backX), height - 39, buttonWidth, 20).build());
        }
        String label = page + 1 < pages ? "Continue" : "Close";
        addRenderableWidget(Button.builder(Component.literal(label), b -> {
            if (page + 1 < pages) {
                page++;
                rebuildButtons();
            } else {
                onClose();
            }
        }).bounds(continueX, height - 39, buttonWidth, 20).build());
    }

    private int pageCount() {
        return Math.max(1, (wrapped.size() + Math.max(1, linesPerPage) - 1) / Math.max(1, linesPerPage));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int boxX = 20;
        int boxY = Math.max(36, height - 168);
        int boxW = width - 40;
        graphics.fill(boxX, boxY, boxX + boxW, boxY + BOX_HEIGHT, 0xE810141B);
        graphics.fill(boxX, boxY, boxX + boxW, boxY + 2, 0xFF58C7D8);
        graphics.drawString(font, fit(speaker, boxW - 28), boxX + 14, boxY + 12, 0xFF58C7D8, false);
        if (!heading.isBlank()) graphics.drawString(font, fit(heading, boxW - 28), boxX + 14, boxY + 27, 0xFFE7EDF2, false);

        int pages = pageCount();
        if (pages > 1) {
            String marker = (page + 1) + " / " + pages;
            graphics.drawString(font, marker, boxX + boxW - 14 - font.width(marker), boxY + 12, 0xFF89949D, false);
        }

        int y = boxY + TEXT_TOP;
        int start = page * linesPerPage;
        int end = Math.min(wrapped.size(), start + linesPerPage);
        for (int i = start; i < end; i++) {
            graphics.drawString(font, wrapped.get(i), boxX + 14, y, 0xFFD3D8DD, false);
            y += LINE_HEIGHT;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override public boolean isPauseScreen() { return false; }

    private String fit(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        int available = Math.max(0, maxWidth - font.width("…"));
        return font.plainSubstrByWidth(text, available) + "…";
    }
}
