package matteroverdrive.client.screen;

import matteroverdrive.menu.FusionReactorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FusionReactorScreen extends AbstractContainerScreen<FusionReactorMenu> {
    public FusionReactorScreen(FusionReactorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176; imageHeight = 223; inventoryLabelY = 126;
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics); super.render(graphics, mouseX, mouseY); renderTooltip(graphics, mouseX, mouseY);
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xffc6c6c6);
        for (int slot = 0; slot < 4; slot++) {
            int x = leftPos + 51 + slot * 18, y = topPos + 51;
            graphics.fill(x, y, x + 20, y + 20, 0xff454545); graphics.fill(x + 1, y + 1, x + 19, y + 19, 0xff9a9a9a);
        }
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(font, menu.energy() + " / " + menu.capacity() + " FE", 18, 21, 0x404040, false);
        graphics.drawString(font, "Matter: " + menu.matter() + " / " + menu.matterCapacity() + " kM", 18, 32, 0x404040, false);
        graphics.drawString(font, "[DEBUG] Structure: " + (menu.valid() ? "VALID" : faultText()), 18, 79, menu.valid() ? 0x227722 : 0xaa2222, false);
        graphics.drawString(font, "[DEBUG] Output: " + menu.output() + " FE/t | efficiency: " + Math.round(menu.efficiency() * 100) + "%", 18, 90, 0x8a5a00, false);
        graphics.drawString(font, "[DEBUG] Anomaly distance: " + menu.anomalyDistance() + " | matter drain: " + String.format(java.util.Locale.ROOT, "%.4f", menu.matterDrain()) + " kM/t", 18, 101, 0x8a5a00, false);
        graphics.drawString(font, "[DEBUG] Layout: 4 coils beside controller, IO above", 18, 112, 0x8a5a00, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }
    private String faultText() {
        return switch (menu.fault()) {
            case 3 -> "missing coil"; case 4 -> "missing IO"; case 5 -> "no anomaly in range";
            case 6 -> "no matter"; default -> "checking";
        };
    }
}
