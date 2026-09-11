package matteroverdrive.client.screen;

import matteroverdrive.MatterOverdrive;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class ReactorAssemblyGuideScreen extends Screen {
    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 248;
    private static final int IMAGE_WIDTH = 200;
    private static final int IMAGE_HEIGHT = 100;
    private static final int SOURCE_IMAGE_WIDTH = 512;
    private static final int SOURCE_IMAGE_HEIGHT = 256;

    private static final List<Page> PAGES = List.of(
            new Page("screen.matteroverdrive.reactor_guide.overview.title",
                    "screen.matteroverdrive.reactor_guide.overview.body",
                    texture("textures/guide/fusion_reactor_sphere.png")),
            new Page("screen.matteroverdrive.reactor_guide.coils.title",
                    "screen.matteroverdrive.reactor_guide.coils.body",
                    texture("textures/guide/fusion_reactor_building_1.png")),
            new Page("screen.matteroverdrive.reactor_guide.hulls.title",
                    "screen.matteroverdrive.reactor_guide.hulls.body",
                    texture("textures/guide/fusion_reactor_building_2.png")),
            new Page("screen.matteroverdrive.reactor_guide.operation.title",
                    "screen.matteroverdrive.reactor_guide.operation.body",
                    texture("textures/guide/gravitational_anomaly_stabilizers.png"))
    );

    private int pageIndex;
    private Button previousButton;
    private Button nextButton;

    public ReactorAssemblyGuideScreen() {
        super(Component.translatable("screen.matteroverdrive.reactor_guide.title"));
    }

    @Override
    protected void init() {
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        int buttonWidth = Math.max(52, Math.min(82, (panelWidth - 36) / 3));
        previousButton = addRenderableWidget(Button.builder(Component.literal("< Previous"), button -> {
            pageIndex = Math.max(0, pageIndex - 1);
            updateButtons();
        }).bounds(left + 8, top + panelHeight - 28, buttonWidth, 20).build());
        nextButton = addRenderableWidget(Button.builder(Component.literal("Next >"), button -> {
            pageIndex = Math.min(PAGES.size() - 1, pageIndex + 1);
            updateButtons();
        }).bounds(left + panelWidth - buttonWidth - 8, top + panelHeight - 28, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(left + (panelWidth - buttonWidth) / 2, top + panelHeight - 28, buttonWidth, 20).build());
        updateButtons();
    }

    private void updateButtons() {
        previousButton.active = pageIndex > 0;
        nextButton.active = pageIndex < PAGES.size() - 1;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        MachineScreenStyle.drawStandaloneFrame(graphics, left, top, panelWidth, panelHeight,
                MachineScreenStyle.CYAN);

        Page page = PAGES.get(pageIndex);
        graphics.drawCenteredString(font, fit(Component.translatable(page.titleKey()).getString(), panelWidth - 36),
                width / 2, top + 9, MachineScreenStyle.TEXT);
        graphics.drawString(font, fit((pageIndex + 1) + " / " + PAGES.size(), 28),
                left + panelWidth - 35, top + 9, MachineScreenStyle.MUTED, false);

        int imageWidth = Math.min(IMAGE_WIDTH, panelWidth - 28);
        int imageHeight = Math.max(62, imageWidth * IMAGE_HEIGHT / IMAGE_WIDTH);
        int imageX = width / 2 - imageWidth / 2;
        int imageY = top + 31;
        MachineScreenStyle.drawSection(graphics, imageX - 3, imageY - 3,
                imageWidth + 6, imageHeight + 6);
        graphics.blit(page.texture(), imageX, imageY, 0, 0,
                imageWidth, imageHeight, SOURCE_IMAGE_WIDTH, SOURCE_IMAGE_HEIGHT);

        int textY = imageY + imageHeight + 10;
        MachineScreenStyle.drawSection(graphics, left + 10, textY - 5,
                panelWidth - 20, panelHeight - (textY - top) - 34);
        if (pageIndex == 1 || pageIndex == 2) {
            graphics.drawString(font, "Overlay: blue=hull  orange=coil/IO  purple=side",
                    left + 14, textY, MachineScreenStyle.AMBER, false);
            textY += 10;
        }
        List<FormattedCharSequence> lines = font.split(
                Component.translatable(page.bodyKey()), panelWidth - 28);
        for (FormattedCharSequence line : lines) {
            if (textY > top + panelHeight - 38) {
                break;
            }
            graphics.drawString(font, line, left + 14, textY, MachineScreenStyle.TEXT, false);
            textY += 10;
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, path);
    }

    private Component fit(String value, int maxWidth) {
        if (font.width(value) <= maxWidth) return Component.literal(value);
        int usable = Math.max(0, maxWidth - font.width("…"));
        return Component.literal(font.plainSubstrByWidth(value, usable) + "…");
    }

    private int panelWidth() {
        return Math.min(PANEL_WIDTH, Math.max(160, width - 20));
    }

    private int panelHeight() {
        return Math.min(PANEL_HEIGHT, Math.max(150, height - 20));
    }

    private record Page(String titleKey, String bodyKey, ResourceLocation texture) {
    }
}
