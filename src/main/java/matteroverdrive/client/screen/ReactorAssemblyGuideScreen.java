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
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        previousButton = addRenderableWidget(Button.builder(Component.literal("< Previous"), button -> {
            pageIndex = Math.max(0, pageIndex - 1);
            updateButtons();
        }).bounds(left + 12, top + PANEL_HEIGHT - 28, 82, 20).build());
        nextButton = addRenderableWidget(Button.builder(Component.literal("Next >"), button -> {
            pageIndex = Math.min(PAGES.size() - 1, pageIndex + 1);
            updateButtons();
        }).bounds(left + PANEL_WIDTH - 94, top + PANEL_HEIGHT - 28, 82, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(left + (PANEL_WIDTH - 60) / 2, top + PANEL_HEIGHT - 28, 60, 20).build());
        updateButtons();
    }

    private void updateButtons() {
        previousButton.active = pageIndex > 0;
        nextButton.active = pageIndex < PAGES.size() - 1;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        MachineScreenStyle.drawStandaloneFrame(graphics, left, top, PANEL_WIDTH, PANEL_HEIGHT,
                MachineScreenStyle.CYAN);

        Page page = PAGES.get(pageIndex);
        graphics.drawCenteredString(font, Component.translatable(page.titleKey()),
                width / 2, top + 9, MachineScreenStyle.TEXT);
        graphics.drawString(font, (pageIndex + 1) + " / " + PAGES.size(),
                left + PANEL_WIDTH - 35, top + 9, MachineScreenStyle.MUTED, false);

        int imageX = width / 2 - IMAGE_WIDTH / 2;
        int imageY = top + 31;
        MachineScreenStyle.drawSection(graphics, imageX - 3, imageY - 3,
                IMAGE_WIDTH + 6, IMAGE_HEIGHT + 6);
        graphics.blit(page.texture(), imageX, imageY, 0, 0,
                IMAGE_WIDTH, IMAGE_HEIGHT, SOURCE_IMAGE_WIDTH, SOURCE_IMAGE_HEIGHT);

        int textY = imageY + IMAGE_HEIGHT + 10;
        MachineScreenStyle.drawSection(graphics, left + 10, textY - 5,
                PANEL_WIDTH - 20, PANEL_HEIGHT - (textY - top) - 34);
        if (pageIndex == 1 || pageIndex == 2) {
            graphics.drawString(font, "Overlay: blue=hull  orange=coil/IO  purple=side",
                    left + 14, textY, MachineScreenStyle.AMBER, false);
            textY += 10;
        }
        List<FormattedCharSequence> lines = font.split(
                Component.translatable(page.bodyKey()), PANEL_WIDTH - 28);
        for (FormattedCharSequence line : lines) {
            if (textY > top + PANEL_HEIGHT - 38) {
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
        return new ResourceLocation(MatterOverdrive.MOD_ID, path);
    }

    private record Page(String titleKey, String bodyKey, ResourceLocation texture) {
    }
}
