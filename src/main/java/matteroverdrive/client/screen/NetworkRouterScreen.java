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
        MachineScreenStyle.drawFrame(graphics, leftPos, topPos, imageWidth, imageHeight,
                inventoryLabelY, MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics, leftPos + 17, topPos + 25, 142, 45);
        MachineScreenStyle.drawDebugPanel(graphics, leftPos + 17, topPos + 72, 142, 12);
        MachineScreenStyle.drawSlot(graphics, leftPos + 79, topPos + 40);
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
        graphics.drawString(font, "Endpoints " + menu.endpoints() + " | Nodes " + menu.nodes()
                        + " | Pylons " + menu.pylons(),
                20, 68, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "FE " + menu.energy(), 20, 74, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Moved " + menu.lastMoved() + "/t",
                112, 74, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
    }
}
