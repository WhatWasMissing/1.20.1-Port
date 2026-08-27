package matteroverdrive.client.screen;

import matteroverdrive.menu.EnergyPipeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EnergyPipeScreen extends AbstractContainerScreen<EnergyPipeMenu> {
    public EnergyPipeScreen(EnergyPipeMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); imageWidth = 176; imageHeight = 169; inventoryLabelY = 72; }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { renderBackground(graphics); super.render(graphics, mouseX, mouseY, partialTick); renderTooltip(graphics, mouseX, mouseY); }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) { graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xffc6c6c6); }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(font, menu.stored() + " / " + menu.capacity() + " FE", 18, 22, 0x404040, false);
        graphics.drawString(font, "[DEBUG] Last output: " + menu.output() + " FE/t", 18, 36, 0x8a5a00, false);
        graphics.drawString(font, "[DEBUG] Relay: all six sides", 18, 47, 0x8a5a00, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }
}