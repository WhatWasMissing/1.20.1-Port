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
        inventoryLabelY = 86;
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
        MachineScreenStyle.drawFrame(graphics, x, y, imageWidth, imageHeight, inventoryLabelY,
                MachineScreenStyle.PURPLE);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 26, 142, 48);
        for (int i = 0; i < 12; i++) {
            Slot slot = menu.slots.get(i);
            MachineScreenStyle.drawSlot(graphics, x + slot.x - 1, y + slot.y - 1);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Patterns " + menu.getPatternCount() + " | Queue "
                        + menu.getQueueSize() + "/8",
                8, 18, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, "Click a pattern to request x1",
                8, 75, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && minecraft != null && minecraft.gameMode != null) {
            for (int i = 0; i < 12; i++) {
                Slot slot = menu.slots.get(i);
                double x = leftPos + slot.x;
                double y = topPos + slot.y;
                if (slot.hasItem() && mouseX >= x && mouseX < x + 16
                        && mouseY >= y && mouseY < y + 16) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
