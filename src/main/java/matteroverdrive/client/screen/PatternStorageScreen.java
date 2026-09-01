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
        imageHeight = 220;
        inventoryLabelY = 127;
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
        MachineScreenStyle.drawSection(graphics, x + 17, y + 28, 142, 70);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 101, 142, 20);

        MachineScreenStyle.drawSlot(graphics, x + 25, y + 43);
        for (int i = 0; i < 6; i++) {
            MachineScreenStyle.drawSlot(graphics,
                    x + 61 + (i % 3) * 24,
                    y + 31 + (i / 3) * 24);
        }
        for (int slot = 0; slot < 4; slot++) {
            MachineScreenStyle.drawSlot(graphics, x + 52 + slot * 18, y + 79);
        }
        MachineScreenStyle.drawVerticalBar(graphics, x + 8, y + 30, 7, 40,
                menu.getEnergy(), menu.getEnergyCapacity(), MachineScreenStyle.RED);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, menu.getEnergy() + " / " + menu.getEnergyCapacity() + " FE",
                18, 20, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Patterns " + menu.getPatternCount() + "/12 | Idle "
                        + menu.getIdleEnergyUsePerTick() + " FE/t",
                20, 104, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Capacity " + menu.getEnergyCapacity() + " FE",
                20, 114, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
    }
}
