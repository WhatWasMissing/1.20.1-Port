package matteroverdrive.client.screen;

import matteroverdrive.menu.FusionReactorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class FusionReactorScreen extends AbstractContainerScreen<FusionReactorMenu> {
    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> {
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
        }).bounds(leftPos + imageWidth - 72, topPos + 4, 68, 20).build());
    }

    public FusionReactorScreen(FusionReactorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 241;
        inventoryLabelY = 144;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xffc6c6c6);
        for (int slot = 0; slot < 4; slot++) {
            int x = leftPos + 51 + slot * 18;
            int y = topPos + 51;
            graphics.fill(x, y, x + 20, y + 20, 0xff454545);
            graphics.fill(x + 1, y + 1, x + 19, y + 19, 0xff9a9a9a);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(font, menu.energy() + " / " + menu.capacity() + " FE",
                18, 21, 0x404040, false);
        graphics.drawString(font, "Matter: " + menu.matter() + " / "
                + menu.matterCapacity() + " kM", 18, 32, 0x404040, false);

        graphics.drawString(font, "Structure: " + (menu.valid() ? "VALID" : faultText())
                        + " | ring: " + menu.ringDirection().getName().toUpperCase(Locale.ROOT),
                18, 79, menu.valid() ? 0x227722 : 0xaa2222, false);
        graphics.drawString(font, "Output: " + menu.output() + " FE/t | efficiency: "
                + Math.round(menu.efficiency() * 100) + "%", 18, 90, 0x8a5a00, false);
        graphics.drawString(font, "Anomaly offset: " + menu.anomalyDistance()
                + " | mass: " + format(menu.unsuppressedMass())
                + " (safe " + format(menu.suppressedMass()) + ")",
                18, 101, 0x8a5a00, false);
        graphics.drawString(font, "Matter drain: " + format(menu.matterDrain())
                + " kM/t | linked IO: " + menu.ioCount(), 18, 112, 0x8a5a00, false);
        graphics.drawString(font, "Active stabilizers: " + menu.stabilizerCount(),
                18, 123, 0x8a5a00, false);
        graphics.drawString(font, "[DEBUG] Pull radius: " + format(menu.anomalyRange())
                        + " | block radius: " + format(menu.blockHazardRange())
                        + " | block hazard: DISABLED",
                18, 134, 0x8a5a00, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    private String faultText() {
        return switch (menu.fault()) {
            case 3 -> "wrong coil/IO position";
            case 4 -> "wrong hull position";
            case 5 -> "no anomaly at ring centre";
            case 6 -> "no matter";
            case 7 -> "wrong controller-side position";
            case 8 -> "structure area unloaded";
            case 9 -> "anomaly data unavailable";
            default -> "checking";
        };
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.4f", value);
    }
}
