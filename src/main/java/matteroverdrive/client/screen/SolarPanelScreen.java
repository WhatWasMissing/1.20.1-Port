package matteroverdrive.client.screen;

import matteroverdrive.blockentity.SolarPanelBlockEntity;
import matteroverdrive.menu.SolarPanelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class SolarPanelScreen extends AbstractContainerScreen<SolarPanelMenu> {
    public SolarPanelScreen(SolarPanelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 204;
        inventoryLabelY = 111;
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
                MachineScreenStyle.AMBER);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 32, 142, 39);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 73, 142, 31);

        MachineScreenStyle.drawVerticalBar(graphics, x + 8, y + 30, 7, 40,
                menu.getEnergy(), menu.getEnergyCapacity(), MachineScreenStyle.AMBER);
        for (int slot = 0; slot < 2; slot++) {
            MachineScreenStyle.drawSlot(graphics, x + 70 + slot * 18, y + 43);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.getEnergy() + " / " + menu.getEnergyCapacity() + " FE",
                18, 29, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Generation " + menu.getCurrentGeneration() + "/"
                        + SolarPanelBlockEntity.PEAK_GENERATION + " FE/t | sent " + menu.getLastOutput(),
                20, 76, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, String.format(Locale.ROOT, "Sky %s | light %d | sun %.3f",
                        menu.dimensionHasSky() && menu.canSeeSky() ? "yes" : "no",
                        menu.getEffectiveSkyLight(), menu.getDaylightFactor()),
                20, 86, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Output " + menu.getMaxOutputPerSide() + " FE/t/side",
                20, 96, MachineScreenStyle.DEBUG, false);
    }
}
