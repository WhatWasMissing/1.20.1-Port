package matteroverdrive.client.screen;

import matteroverdrive.menu.EnergyPipeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EnergyPipeScreen extends AbstractContainerScreen<EnergyPipeMenu> {
    public EnergyPipeScreen(EnergyPipeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 169;
        inventoryLabelY = 72;
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
                MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 29, 142, 18);
        MachineScreenStyle.drawHorizontalBar(graphics, x + 18, y + 42, 140, 4,
                menu.stored(), menu.capacity(), MachineScreenStyle.CYAN);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 49, 142, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, menu.stored() + " / " + menu.capacity() + " FE",
                18, 31, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Last output " + menu.output() + " FE/t",
                20, 52, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Relay: all six sides",
                20, 61, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
    }
}
