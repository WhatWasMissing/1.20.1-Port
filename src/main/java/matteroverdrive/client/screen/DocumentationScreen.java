package matteroverdrive.client.screen;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.DocumentationItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DocumentationScreen extends Screen {
    private static final int PANEL_COLOR = 0xF0101820;
    private static final int BORDER_COLOR = 0xFF33CCFF;
    private static final int TEXT_COLOR = 0xFFE6F7FF;
    private static final int MUTED_COLOR = 0xFF9BB6C3;
    private static final int CHECK_COLOR = 0xFFFFD166;

    private final DocumentationItem.Document document;
    private final List<String> sourceLines = new ArrayList<>();
    private final List<RenderLine> renderedLines = new ArrayList<>();
    private int scroll;
    private int contentHeight;
    private int viewportHeight;

    public DocumentationScreen(int documentId) {
        super(Component.literal(DocumentationItem.Document.fromId(documentId).title));
        this.document = DocumentationItem.Document.fromId(documentId);
        loadDocument();
    }

    private void loadDocument() {
        ResourceLocation resource = new ResourceLocation(
                MatterOverdrive.MOD_ID, document.resourcePath);
        Minecraft.getInstance().getResourceManager().getResource(resource).ifPresentOrElse(found -> {
            try (BufferedReader reader = found.openAsReader()) {
                sourceLines.addAll(reader.lines().toList());
            } catch (IOException exception) {
                sourceLines.add("Unable to read bundled documentation: " + exception.getMessage());
            }
        }, () -> sourceLines.add("Bundled documentation resource is missing: " + resource));
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(width / 2 - 40, height - 27, 80, 20).build());
        rebuildLines(Math.max(80, Math.min(560, width - 54)));
    }

    private void rebuildLines(int maxWidth) {
        renderedLines.clear();
        contentHeight = 0;
        for (String raw : sourceLines) {
            if (raw.isBlank()) {
                renderedLines.add(new RenderLine(FormattedCharSequence.EMPTY, 6));
                contentHeight += 6;
                continue;
            }

            Component line;
            int height = 11;
            if (raw.startsWith("#")) {
                String heading = raw.replaceFirst("^#+\\s*", "");
                line = Component.literal(heading).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
                height = raw.startsWith("# ") ? 15 : 13;
            } else if (raw.startsWith("- [ ] ")) {
                line = Component.literal("□ " + raw.substring(6)).withStyle(ChatFormatting.YELLOW);
            } else if (raw.startsWith("- ")) {
                line = Component.literal("• " + raw.substring(2));
            } else {
                line = Component.literal(raw);
            }

            List<FormattedCharSequence> wrapped = font.split(line, maxWidth);
            for (int index = 0; index < wrapped.size(); index++) {
                int lineHeight = index == wrapped.size() - 1 ? height : 11;
                renderedLines.add(new RenderLine(wrapped.get(index), lineHeight));
                contentHeight += lineHeight;
            }
        }
        viewportHeight = Math.max(20, height - 82);
        scroll = Math.min(scroll, maxScroll());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int panelWidth = Math.min(600, width - 24);
        int left = (width - panelWidth) / 2;
        int right = left + panelWidth;
        int top = 10;
        int bottom = height - 34;

        graphics.fill(left, top, right, bottom, PANEL_COLOR);
        graphics.fill(left, top, right, top + 1, BORDER_COLOR);
        graphics.fill(left, bottom - 1, right, bottom, BORDER_COLOR);
        graphics.fill(left, top, left + 1, bottom, BORDER_COLOR);
        graphics.fill(right - 1, top, right, bottom, BORDER_COLOR);
        graphics.drawCenteredString(font, title, width / 2, top + 9, BORDER_COLOR);
        graphics.drawCenteredString(font, Component.literal("Matter Overdrive Alpha 0.2 • Made by MVQ1303"),
                width / 2, top + 21, MUTED_COLOR);

        int textLeft = left + 15;
        int textTop = top + 38;
        int textBottom = bottom - 8;
        viewportHeight = textBottom - textTop;
        graphics.enableScissor(textLeft, textTop, right - 18, textBottom);

        int y = textTop - scroll;
        for (RenderLine line : renderedLines) {
            if (y + line.height >= textTop && y < textBottom && line.text != FormattedCharSequence.EMPTY) {
                int color = line.text.toString().startsWith("□") ? CHECK_COLOR : TEXT_COLOR;
                graphics.drawString(font, line.text, textLeft, y, color, false);
            }
            y += line.height;
        }
        graphics.disableScissor();

        if (maxScroll() > 0) {
            int trackTop = textTop;
            int trackBottom = textBottom;
            int trackHeight = trackBottom - trackTop;
            int thumbHeight = Math.max(18, trackHeight * viewportHeight / Math.max(viewportHeight, contentHeight));
            int thumbTravel = trackHeight - thumbHeight;
            int thumbTop = trackTop + (maxScroll() == 0 ? 0 : thumbTravel * scroll / maxScroll());
            graphics.fill(right - 10, trackTop, right - 7, trackBottom, 0xFF26333D);
            graphics.fill(right - 10, thumbTop, right - 7, thumbTop + thumbHeight, BORDER_COLOR);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll = Math.max(0, Math.min(maxScroll(), scroll - (int) Math.signum(delta) * 30));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            scroll = Math.min(maxScroll(), scroll + Math.max(30, viewportHeight - 20));
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            scroll = Math.max(0, scroll - Math.max(30, viewportHeight - 20));
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_HOME) {
            scroll = 0;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_END) {
            scroll = maxScroll();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private int maxScroll() {
        return Math.max(0, contentHeight - viewportHeight);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record RenderLine(FormattedCharSequence text, int height) {
    }
}
