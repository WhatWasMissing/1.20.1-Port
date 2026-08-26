package matteroverdrive.client.screen;

import matteroverdrive.menu.PatternMonitorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class PatternMonitorScreen extends AbstractContainerScreen<PatternMonitorMenu> {
    public PatternMonitorScreen(PatternMonitorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 178;
        inventoryLabelY = 85;
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
        for (int i = 0; i < 12; i++) {
            Slot slot = menu.slots.get(i);
            int sx = x + slot.x - 1;
            int sy = y + slot.y - 1;
            graphics.fill(sx, sy, sx + 18, sy + 18, 0xFF454545);
            graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF9A9A9A);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(font, "Patterns: " + menu.getPatternCount() + "  Queue: " + menu.getQueueSize() + "/8", 8, 16, 0x404040, false);
        graphics.drawString(font, "Click a pattern to request x1", 8, 78, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && minecraft != null && minecraft.gameMode != null) {
            for (int i = 0; i < 12; i++) {
                Slot slot = menu.slots.get(i);
                double x = leftPos + slot.x;
                double y = topPos + slot.y;
                if (slot.hasItem() && mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
