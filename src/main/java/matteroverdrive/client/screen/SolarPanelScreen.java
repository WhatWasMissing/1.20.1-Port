package matteroverdrive.client.screen;

import matteroverdrive.blockentity.SolarPanelBlockEntity;
import matteroverdrive.menu.SolarPanelMenu;
import net.minecraft.client.gui.GuiGraphics;
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        graphics.fill(x + 8, y + 20, x + 14, y + 69, 0xFF4A4A4A);
        int energyHeight = scale(menu.getEnergy(), menu.getEnergyCapacity(), 48);
        graphics.fill(x + 9, y + 68 - energyHeight, x + 13, y + 68, 0xFFFFB52E);

        for (int slot = 0; slot < 2; slot++) {
            int slotX = x + 70 + slot * 18;
            graphics.fill(slotX, y + 43, slotX + 20, y + 63, 0xFF454545);
            graphics.fill(slotX + 1, y + 44, slotX + 19, y + 62, 0xFF9A9A9A);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(
                font,
                menu.getEnergy() + " / " + menu.getEnergyCapacity() + " FE",
                18, 20, 0x404040, false);

        graphics.drawString(
                font,
                "[DEBUG] Gen: " + menu.getCurrentGeneration() + "/"
                        + SolarPanelBlockEntity.PEAK_GENERATION + " FE/t"
                        + " | sent: " + menu.getLastOutput(),
                18, 74, 0x8A5A00, false);
        graphics.drawString(
                font,
                String.format(
                        Locale.ROOT,
                        "[DEBUG] Sky: %s | light: %d | sun: %.3f",
                        menu.dimensionHasSky() && menu.canSeeSky() ? "yes" : "no",
                        menu.getEffectiveSkyLight(),
                        menu.getDaylightFactor()),
                18, 84, 0x8A5A00, false);
        graphics.drawString(
                font,
                "[DEBUG] Output: " + menu.getMaxOutputPerSide() + " FE/t/side",
                18, 94, 0x8A5A00, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    private static int scale(int value, int max, int pixels) {
        return max <= 0 || value <= 0
                ? 0
                : Math.min(pixels, Math.max(1, (int) Math.round((double) value * pixels / max)));
    }
}
