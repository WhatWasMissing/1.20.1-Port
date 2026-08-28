package matteroverdrive.client.screen;

import matteroverdrive.menu.WeaponStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class WeaponStationScreen extends AbstractContainerScreen<WeaponStationMenu> {
    public WeaponStationScreen(WeaponStationMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); imageWidth=176; imageHeight=166; }
    @Override protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) { graphics.fill(leftPos, topPos, leftPos+imageWidth, topPos+imageHeight, 0xff20242b); }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) { graphics.drawString(font, title, 8, 6, 0xffffff); graphics.drawString(font, "Gun modules", 44, 20, 0xffaaaaaa); graphics.drawString(font, "Inventory", 8, imageHeight-94, 0xffffff); }
}
