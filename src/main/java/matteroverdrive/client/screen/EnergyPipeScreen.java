package matteroverdrive.client.screen;

import matteroverdrive.menu.EnergyPipeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EnergyPipeScreen extends AbstractContainerScreen<EnergyPipeMenu> {
    public EnergyPipeScreen(EnergyPipeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 260;
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
        int x = leftPos, y = topPos;
        MachineScreenStyle.drawFrame(graphics, x, y, imageWidth, imageHeight, inventoryLabelY,
                MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 29, 142, 18);
        MachineScreenStyle.drawHorizontalBar(graphics, x + 18, y + 42, 140, 4,
                menu.stored(), menu.capacity(), MachineScreenStyle.CYAN);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 49, 142, 18);
        MachineScreenStyle.drawSection(graphics, x + 170, y + 25, 81, 42);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, menu.stored() + " / " + menu.capacity() + " FE", 130),
                18, 31, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Last output " + menu.output() + " FE/t", 130),
                20, 52, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Relay: all six sides",
                20, 61, MachineScreenStyle.MUTED, false);

        graphics.drawString(font, MachineScreenStyle.fit(font, "ENERGY RELAY", 70), 177, 32, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, percent() + "% full", 70), 177, 44, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Out " + menu.output() + " FE/t", 70), 177, 56,
                menu.output() > 0 ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
    }

    private int percent() {
        return menu.capacity() <= 0 ? 0 : Math.min(100, menu.stored() * 100 / menu.capacity());
    }
}
