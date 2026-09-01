package matteroverdrive.client.screen;

import matteroverdrive.menu.GravitationalStabilizerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GravitationalStabilizerScreen
        extends AbstractContainerScreen<GravitationalStabilizerMenu> {
    public GravitationalStabilizerScreen(GravitationalStabilizerMenu menu,
                                         Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 202;
        inventoryLabelY = 89;
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
        MachineScreenStyle.drawFrame(graphics, x, y, imageWidth, imageHeight,
                inventoryLabelY, MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 25, 142, 39);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 66, 142, 20);
        for (int slot = 0; slot < 4; slot++) {
            MachineScreenStyle.drawSlot(graphics, x + 52 + slot * 18, y + 36);
        }
        MachineScreenStyle.drawHorizontalBar(graphics, x + 25, y + 56, 126, 5,
                menu.energy(), menu.energyCapacity(), MachineScreenStyle.CYAN);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Power upgrades", 25, 29, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.energy() + " / " + menu.energyCapacity() + " FE",
                25, 45, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Draw " + menu.powerUsed() + " / "
                        + menu.requiredPower() + " FE/t",
                20, 69, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, status(), 20, 78, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
    }

    private String status() {
        if (!menu.isPowered()) {
            return "Waiting for reactor power";
        }
        if (menu.anomalyDistance() >= 0) {
            return "Locked: anomaly " + menu.anomalyDistance() + " blocks";
        }
        if (menu.isBeamBlocked()) {
            return "Beam blocked: " + menu.beamBlockedDistance() + " blocks";
        }
        return "Powered: no anomaly in beam";
    }
}
