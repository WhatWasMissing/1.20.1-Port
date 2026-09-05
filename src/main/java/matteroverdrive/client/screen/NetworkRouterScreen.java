package matteroverdrive.client.screen;

import matteroverdrive.menu.NetworkRouterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class NetworkRouterScreen extends AbstractContainerScreen<NetworkRouterMenu> {
    public NetworkRouterScreen(NetworkRouterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 213;
        inventoryLabelY = 102;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineScreenStyle.drawFrame(graphics, leftPos, topPos, imageWidth, imageHeight,
                inventoryLabelY, MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics, leftPos + 17, topPos + 25, 142, 61);
        MachineScreenStyle.drawDebugPanel(graphics, leftPos + 17, topPos + 87, 142, 14);
        MachineScreenStyle.drawSlot(graphics, leftPos + 79, topPos + 40);
        for (int slot = 0; slot < 4; slot++) {
            MachineScreenStyle.drawSlot(graphics, leftPos + 52 + slot * 18, topPos + 64);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, menu.filterModeLabel(), 20, 27,
                menu.filterMode() == 2 ? MachineScreenStyle.CYAN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.filterMode() == 2
                        ? "Allowed destinations: " + menu.destinationCount()
                        : menu.filterMode() == 1 ? "Matching item only" : "Any item / any endpoint",
                20, 57, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Speed upgrades | budget " + menu.itemBudget() + " items/t",
                20, 78, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "E " + menu.endpoints() + " | N " + menu.nodes()
                        + " | P " + menu.pylons(), 20, 89, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "FE " + menu.energy(), 84, 89, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Moved " + menu.lastMoved(), 121, 89, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
    }
}
