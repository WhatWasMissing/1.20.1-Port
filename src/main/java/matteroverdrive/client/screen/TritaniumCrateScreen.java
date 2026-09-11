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
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        MachineScreenStyle.drawFrame(graphics, x, y, imageWidth, imageHeight, inventoryLabelY,
                MachineScreenStyle.BLUE);
        MachineScreenStyle.drawSection(graphics, x + 6, y + 15, 164, 112);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 130, 142, 11);
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                MachineScreenStyle.drawSlot(graphics,
                        x + 7 + column * 18,
                        y + 17 + row * 18);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Used " + menu.getUsedSlots() + " / "
                        + TritaniumCrateBlockEntity.SLOT_COUNT
                        + " | Items " + menu.getTotalItemCount(),
                135), 20, 132, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
    }
}
