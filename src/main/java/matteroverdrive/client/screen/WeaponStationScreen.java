package matteroverdrive.client.screen;

import matteroverdrive.menu.WeaponStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class WeaponStationScreen extends AbstractContainerScreen<WeaponStationMenu> {
    private static final int[][] STATION_SLOT_POSITIONS = {
            {26, 36}, {62, 36}, {80, 36}, {98, 36}, {62, 54}, {80, 54}, {98, 54}
    };

    public WeaponStationScreen(WeaponStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 74;
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
        MachineScreenStyle.drawSection(graphics, leftPos + 17, topPos + 27, 142, 44);
        for (int[] position : STATION_SLOT_POSITIONS) {
            MachineScreenStyle.drawSlot(graphics,
                    leftPos + position[0] - 1,
                    topPos + position[1] - 1);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Weapon", 20, 26, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Modules", 60, 26, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
    }
}
