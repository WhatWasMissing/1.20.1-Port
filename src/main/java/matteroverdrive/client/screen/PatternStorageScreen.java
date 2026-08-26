package matteroverdrive.client.screen;

import matteroverdrive.menu.PatternStorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PatternStorageScreen extends AbstractContainerScreen<PatternStorageMenu> {
    public PatternStorageScreen(PatternStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 73;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        graphics.fill(x + 25, y + 43, x + 45, y + 63, 0xFF454545);
        graphics.fill(x + 26, y + 44, x + 44, y + 62, 0xFF9A9A9A);
        for (int i = 0; i < 6; i++) {
            int sx = x + 61 + (i % 3) * 24;
            int sy = y + 31 + (i / 3) * 24;
            graphics.fill(sx, sy, sx + 20, sy + 20, 0xFF454545);
            graphics.fill(sx + 1, sy + 1, sx + 19, sy + 19, 0xFF9A9A9A);
        }
        graphics.fill(x + 8, y + 20, x + 14, y + 69, 0xFF4A4A4A);
        int energyHeight = scale(menu.getEnergy(), menu.getEnergyCapacity(), 48);
        graphics.fill(x + 9, y + 68 - energyHeight, x + 13, y + 68, 0xFFCC3333);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(font, menu.getEnergy() + " / " + menu.getEnergyCapacity() + " FE", 18, 20, 0x404040, false);
        graphics.drawString(font, "Patterns: " + menu.getPatternCount() + " / 12", 62, 20, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    private static int scale(int value, int max, int pixels) {
        return max <= 0 || value <= 0 ? 0 : Math.min(pixels, Math.max(1, (int) Math.round((double) value * pixels / max)));
    }
}
