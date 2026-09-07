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
    private final String speaker;
    private final String heading;
    private final List<String> sourceLines;
    private final List<FormattedCharSequence> wrapped = new ArrayList<>();

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
        addRenderableWidget(Button.builder(Component.literal("Continue"), b -> onClose())
                .bounds(width - 118, height - 39, 92, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int boxX = 20;
        int boxY = Math.max(36, height - 168);
        int boxW = width - 40;
        int boxH = 132;
        graphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xE810141B);
        graphics.fill(boxX, boxY, boxX + boxW, boxY + 2, 0xFF58C7D8);
        graphics.drawString(font, speaker, boxX + 14, boxY + 12, 0xFF58C7D8, false);
        if (!heading.isBlank()) graphics.drawString(font, heading, boxX + 14, boxY + 27, 0xFFE7EDF2, false);
        int y = boxY + 47;
        for (FormattedCharSequence line : wrapped) {
            if (y > boxY + boxH - 31) break;
            graphics.drawString(font, line, boxX + 14, y, 0xFFD3D8DD, false);
            y += 11;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override public boolean isPauseScreen() { return false; }
}
