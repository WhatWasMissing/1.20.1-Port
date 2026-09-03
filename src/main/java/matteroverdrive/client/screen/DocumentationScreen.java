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
import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;

public class DocumentationScreen extends Screen {
    private static final int PANEL_COLOR = 0xF0101820;
    private static final int BORDER_COLOR = 0xFF33CCFF;
    private static final int TEXT_COLOR = 0xFFE6F7FF;
    private static final int MUTED_COLOR = 0xFF9BB6C3;
    private static final int CHECK_COLOR = 0xFFFFD166;

    private final DocumentationItem.Document document;
    private final List<String> sourceLines = new ArrayList<>();
    private final List<List<RenderLine>> pages = new ArrayList<>();
    private final Map<String, Integer> sectionPages = new LinkedHashMap<>();
    private static final Map<Integer, Integer> LAST_PAGES = new LinkedHashMap<>();
    private static boolean memoryLoaded;
    private boolean indexOpen;
    private int pageIndex;
    private int viewportHeight;

    public DocumentationScreen(int documentId) {
        super(Component.literal(DocumentationItem.Document.fromId(documentId).title));
        this.document = DocumentationItem.Document.fromId(documentId);
        loadDocument();
        loadPageMemory();
    }

    private void loadDocument() {
        ResourceLocation resource = new ResourceLocation(MatterOverdrive.MOD_ID, document.resourcePath);
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
        viewportHeight = Math.max(40, height - 82);
        rebuildPages(Math.max(80, Math.min(500, width - 74)));
        pageIndex = Math.max(0, Math.min(LAST_PAGES.getOrDefault(document.ordinal(), 0), pages.size() - 1));
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearWidgets();
        if (indexOpen) {
            int column = 0;
            int row = 0;
            for (Map.Entry<String, Integer> entry : sectionPages.entrySet()) {
                int x = width / 2 - 270 + column * 275;
                int y = 42 + row * 22;
                int target = entry.getValue();
                String label = entry.getKey();
                if (label.length() > 32) label = label.substring(0, 29) + "...";
                addRenderableWidget(Button.builder(Component.literal(label), button -> jumpTo(target))
                        .bounds(x, y, 265, 20).build());
                if (++column == 2) { column = 0; row++; }
            }
            addRenderableWidget(Button.builder(Component.literal("Back to guide"), button -> {
                indexOpen = false;
                rebuildWidgets();
            }).bounds(width / 2 - 55, height - 27, 110, 20).build());
            return;
        }
        addRenderableWidget(Button.builder(Component.literal("< Previous"), button -> {
            pageIndex = Math.max(0, pageIndex - 1);
            rememberPage();
        }).bounds(width / 2 - 170, height - 27, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Index"), button -> {
            rememberPage();
            indexOpen = true;
            rebuildWidgets();
        }).bounds(width / 2 - 65, height - 27, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Next >"), button -> {
            pageIndex = Math.min(Math.max(0, pages.size() - 1), pageIndex + 1);
            rememberPage();
        }).bounds(width / 2 + 20, height - 27, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(width / 2 - 40, height - 51, 80, 20).build());
    }

    private void jumpTo(int target) {
        pageIndex = Math.max(0, Math.min(target, pages.size() - 1));
        indexOpen = false;
        rememberPage();
        rebuildWidgets();
    }

    private void rebuildPages(int maxWidth) {
        pages.clear();
        sectionPages.clear();
        List<RenderLine> current = new ArrayList<>();
        int used = 0;
        for (String raw : sourceLines) {
            // Give each major section its own page so headings are never buried
            // in a dense wall of text.
            if (raw.startsWith("# ") && !current.isEmpty()) {
                pages.add(current);
                current = new ArrayList<>();
                used = 0;
            }
            if (raw.startsWith("# ")) {
                sectionPages.put(raw.substring(2).trim(), pages.size());
            }
            Component line;
            int height = 12;
            if (raw.isBlank()) {
                line = Component.empty();
                height = 6;
            } else if (raw.startsWith("#")) {
                line = Component.literal(raw.replaceFirst("^#+\\s*", ""))
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
                height = raw.startsWith("# ") ? 15 : 13;
            } else if (raw.startsWith("## ")) {
                line = Component.literal(raw.replaceFirst("^##\\s*", ""))
                        .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD);
                height = 13;
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
                if (!current.isEmpty() && used + lineHeight > viewportHeight) {
                    pages.add(current);
                    current = new ArrayList<>();
                    used = 0;
                }
                current.add(new RenderLine(wrapped.get(index), lineHeight));
                used += lineHeight;
            }
        }
        if (!current.isEmpty() || pages.isEmpty()) {
            pages.add(current);
        }
        pageIndex = Math.min(pageIndex, pages.size() - 1);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        if (indexOpen) {
            int panelWidth = Math.min(600, width - 24);
            int left = (width - panelWidth) / 2;
            int right = left + panelWidth;
            int top = 10;
            int bottom = height - 18;
            graphics.fill(left, top, right, bottom, PANEL_COLOR);
            graphics.fill(left, top, right, top + 1, BORDER_COLOR);
            graphics.fill(left, top, left + 1, bottom, BORDER_COLOR);
            graphics.fill(right - 1, top, right, bottom, BORDER_COLOR);
            graphics.drawCenteredString(font, Component.literal("Guide Index"), width / 2, top + 10, BORDER_COLOR);
            graphics.drawCenteredString(font, Component.literal("Select a section"), width / 2, top + 24, MUTED_COLOR);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }
        int panelWidth = Math.min(600, width - 24);
        int left = (width - panelWidth) / 2;
        int right = left + panelWidth;
        int top = 10;
        int bottom = height - 58;
        graphics.fill(left, top, right, bottom, PANEL_COLOR);
        graphics.fill(left, top, right, top + 1, BORDER_COLOR);
        graphics.fill(left, bottom - 1, right, bottom, BORDER_COLOR);
        graphics.fill(left, top, left + 1, bottom, BORDER_COLOR);
        graphics.fill(right - 1, top, right, bottom, BORDER_COLOR);
        graphics.drawCenteredString(font, title, width / 2, top + 9, BORDER_COLOR);
        graphics.drawCenteredString(font, Component.literal("Matter Overdrive Alpha 0.2 • Made by MVQ1303"),
                width / 2, top + 21, MUTED_COLOR);
        graphics.drawCenteredString(font, Component.literal(String.format("Page %d / %d", pageIndex + 1, pages.size())),
                width / 2, bottom + 7, MUTED_COLOR);

        int textLeft = left + 15;
        int textTop = top + 38;
        int y = textTop;
        if (!pages.isEmpty()) {
            for (RenderLine line : pages.get(pageIndex)) {
                if (line.text != FormattedCharSequence.EMPTY) {
                    int color = line.text.toString().startsWith("□") ? CHECK_COLOR : TEXT_COLOR;
                    graphics.drawString(font, line.text, textLeft, y, color, false);
                }
                y += line.height;
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN || keyCode == GLFW.GLFW_KEY_RIGHT) {
            pageIndex = Math.min(pages.size() - 1, pageIndex + 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_UP || keyCode == GLFW.GLFW_KEY_LEFT) {
            pageIndex = Math.max(0, pageIndex - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_HOME) {
            pageIndex = 0;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_END) {
            pageIndex = Math.max(0, pages.size() - 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record RenderLine(FormattedCharSequence text, int height) {
    }
}
