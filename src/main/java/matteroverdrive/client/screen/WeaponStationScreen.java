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
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xff20242b);
        graphics.fill(leftPos + 6, topPos + 28, leftPos + 122, topPos + 76, 0xff15191f);
        for (int[] position : STATION_SLOT_POSITIONS) {
            int x = leftPos + position[0] - 1;
            int y = topPos + position[1] - 1;
            graphics.fill(x, y, x + 18, y + 18, 0xff5c6673);
            graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xff111419);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0xffffff);
        graphics.drawString(font, "Weapon", 20, 20, 0xffd7dce2);
        graphics.drawString(font, "Battery / colour / barrel", 58, 20, 0xffaeb7c2);
        graphics.drawString(font, "Sights / utility / utility", 58, 68, 0xffaeb7c2);
        graphics.drawString(font, "Inventory", 8, imageHeight - 94, 0xffffff);
    }
}
