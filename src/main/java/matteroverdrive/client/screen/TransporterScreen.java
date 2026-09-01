package matteroverdrive.client.screen;

import matteroverdrive.menu.TransporterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TransporterScreen extends AbstractContainerScreen<TransporterMenu> {
    public TransporterScreen(TransporterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 230;
        inventoryLabelY = 133;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            }
        }).bounds(leftPos + imageWidth - 57, topPos + 5, 52, 16).build());
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
        MachineScreenStyle.drawSection(graphics, x + 17, y + 32, 142, 63);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 98, 142, 29);

        MachineScreenStyle.drawSlot(graphics, x + 43, y + 43);
        MachineScreenStyle.drawSlot(graphics, x + 111, y + 43);
        for (int slot = 0; slot < 5; slot++) {
            MachineScreenStyle.drawSlot(graphics, x + 42 + slot * 18, y + 77);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.e() + " / " + menu.cap() + " FE",
                18, 29, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.target() ? "Destination linked" : "Bind a Transport Flash Drive",
                20, 101, menu.target() ? MachineScreenStyle.BLUE : MachineScreenStyle.DANGER, false);
        graphics.drawString(font, "Distance " + menu.dist() + " / " + menu.range()
                        + " | cost " + menu.cost() + " FE",
                20, 111, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Cycle " + menu.cycle() + " t | "
                        + (menu.running() ? "running" : "idle") + " | delay " + menu.cooldown(),
                20, 121, MachineScreenStyle.DEBUG, false);
    }
}
