package matteroverdrive.client.screen;

import matteroverdrive.menu.ChargingStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class ChargingStationScreen extends AbstractContainerScreen<ChargingStationMenu> {
    public ChargingStationScreen(ChargingStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 197;
        inventoryLabelY = 84;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        MachineScreenStyle.drawFrame(graphics, x, y, imageWidth, imageHeight,
                inventoryLabelY, MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 29, 142, 40);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 71, 142, 13);
        MachineScreenStyle.drawSlot(graphics, x + 79, y + 41);
        MachineScreenStyle.drawHorizontalBar(graphics, x + 25, y + 57, 126, 5,
                menu.batteryEnergy(), menu.batteryCapacity(), MachineScreenStyle.CYAN);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Insert rechargeable item", 26, 31,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.batteryEnergy() + " / " + menu.batteryCapacity() + " FE",
                26, 47, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Input " + menu.stationEnergy() + " FE",
                20, 73, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, menu.lastTransferred() + " FE/t",
                113, 73, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
    }
}
