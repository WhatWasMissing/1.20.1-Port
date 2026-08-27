package matteroverdrive.client.screen;

import matteroverdrive.blockentity.TritaniumCrateBlockEntity;
import matteroverdrive.menu.TritaniumCrateMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TritaniumCrateScreen extends AbstractContainerScreen<TritaniumCrateMenu> {
    public TritaniumCrateScreen(TritaniumCrateMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 236;
        inventoryLabelY = 145;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                int slotX = leftPos + 7 + column * 18;
                int slotY = topPos + 17 + row * 18;
                graphics.fill(slotX, slotY, slotX + 20, slotY + 20, 0xFF454545);
                graphics.fill(slotX + 1, slotY + 1, slotX + 19, slotY + 19, 0xFF9A9A9A);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(
                font,
                "[DEBUG] Used: " + menu.getUsedSlots() + " / "
                        + TritaniumCrateBlockEntity.SLOT_COUNT
                        + " | Items: " + menu.getTotalItemCount(),
                8, 132, 0x8A5A00, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }
}
