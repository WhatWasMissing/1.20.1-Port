package matteroverdrive.client.screen;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.menu.WeaponStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WeaponStationScreen extends AbstractContainerScreen<WeaponStationMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MatterOverdrive.MOD_ID, "textures/gui/weapon_station.png");

    public WeaponStationScreen(WeaponStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
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
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0xD8EEF2, false);
        graphics.drawString(font, "Gun modules", 44, 20, 0x8FB9C2, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0xD8EEF2, false);
    }
}
