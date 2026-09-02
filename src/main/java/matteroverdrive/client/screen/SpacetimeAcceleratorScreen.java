package matteroverdrive.client.screen;

import matteroverdrive.menu.SpacetimeAcceleratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SpacetimeAcceleratorScreen extends AbstractContainerScreen<SpacetimeAcceleratorMenu> {
    public SpacetimeAcceleratorScreen(SpacetimeAcceleratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 184;
        inventoryLabelY = 91;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            }
        }).bounds(leftPos + 119, topPos + 5, 52, 16).build());
        addRenderableWidget(Button.builder(Component.literal("MAT+"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 2);
            }
        }).bounds(leftPos + 120, topPos + 66, 47, 16).build());
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
        MachineScreenStyle.drawSection(graphics, x + 18, y + 31, 149, 54);

        MachineScreenStyle.drawVerticalBar(graphics, x + 8, y + 31, 7, 40,
                menu.getEnergy(), menu.getEnergyCapacity(), MachineScreenStyle.BLUE);
        MachineScreenStyle.drawVerticalBar(graphics, x + 16, y + 31, 7, 40,
                menu.getMatter(), menu.getMatterCapacity(), MachineScreenStyle.PURPLE);
        MachineScreenStyle.drawHorizontalBar(graphics, x + 28, y + 74, 87, 6,
                menu.getPulseTimer(), menu.getPulseInterval(), MachineScreenStyle.PURPLE);

        for (int slot = 0; slot < 4; slot++) {
            MachineScreenStyle.drawSlot(graphics, x + 52 + slot * 18, y + 64);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);

        graphics.drawString(font, "FE " + menu.getEnergy() + "/" + menu.getEnergyCapacity(),
                28, 32, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Matter " + menu.getMatter() + "/" + menu.getMatterCapacity(),
                28, 42, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Pulse " + menu.getPulseInterval() + "t  Radius " + menu.getRadius(),
                28, 52, MachineScreenStyle.MUTED, false);

        String status;
        int statusColor;
        if (menu.isRedstoneBlocked()) {
            status = "Stopped by redstone signal";
            statusColor = MachineScreenStyle.RED;
        } else if (menu.isActive()) {
            status = "Active | " + menu.getEnergyPerTick() + " FE/t | last "
                    + menu.getLastAcceleratedTargets();
            statusColor = MachineScreenStyle.TEXT;
        } else {
            status = "Needs FE + matter";
            statusColor = MachineScreenStyle.AMBER;
        }
        graphics.drawString(font, status, 28, 62, statusColor, false);
    }
}
