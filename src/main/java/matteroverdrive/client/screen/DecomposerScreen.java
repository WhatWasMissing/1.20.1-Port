package matteroverdrive.client.screen;

import matteroverdrive.menu.DecomposerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DecomposerScreen extends AbstractContainerScreen<DecomposerMenu> {
    public DecomposerScreen(DecomposerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 196;
        inventoryLabelY = 103;
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

        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        graphics.fill(x, y, x + imageWidth, y + 1, 0xFF373737);
        graphics.fill(x, y + imageHeight - 1, x + imageWidth, y + imageHeight, 0xFF373737);
        graphics.fill(x, y, x + 1, y + imageHeight, 0xFF373737);
        graphics.fill(x + imageWidth - 1, y, x + imageWidth, y + imageHeight, 0xFF373737);

        drawSlot(graphics, x + 25, y + 43);
        drawSlot(graphics, x + 79, y + 43);
        drawSlot(graphics, x + 133, y + 43);
        for (int slot = 0; slot < 4; slot++) {
            drawSlot(graphics, x + 52 + slot * 18, y + 79);
        }

        int progressWidth = scale(menu.getProgress(), menu.getMaxProgress(), 42);
        graphics.fill(x + 48, y + 48, x + 91, y + 53, 0xFF5B5B5B);
        graphics.fill(x + 48, y + 48, x + 48 + progressWidth, y + 53, 0xFF00A5C8);

        int energyHeight = scale(menu.getEnergy(), menu.getEnergyCapacity(), 48);
        graphics.fill(x + 8, y + 20, x + 14, y + 69, 0xFF4A4A4A);
        graphics.fill(x + 9, y + 68 - energyHeight, x + 13, y + 68, 0xFFCC3333);

        int matterHeight = scale(menu.getMatter(), menu.getMatterCapacity(), 48);
        graphics.fill(x + 162, y + 20, x + 168, y + 69, 0xFF4A4A4A);
        graphics.fill(x + 163, y + 68 - matterHeight, x + 167, y + 68, 0xFF3388CC);
    }

    private void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 20, y + 20, 0xFF454545);
        graphics.fill(x + 1, y + 1, x + 19, y + 19, 0xFF9A9A9A);
    }

    private int scale(int value, int max, int pixels) {
        if (max <= 0 || value <= 0) {
            return 0;
        }
        return Math.min(pixels, Math.max(1, (int) Math.round((double) value * pixels / max)));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);

        String energy = menu.getEnergy() + " / " + menu.getEnergyCapacity() + " FE";
        String matter = menu.getMatter() + " / " + menu.getMatterCapacity() + " kM";
        String failure = String.format(java.util.Locale.ROOT, "[DEBUG] Failure: %.4f%%", menu.getFailureChancePercent());
        graphics.drawString(font, energy, 18, 20, 0x404040, false);
        graphics.drawString(font, matter, 18, 30, 0x404040, false);
        graphics.drawString(font, failure, 48, 68, 0x7A1F1F, false);

        int inputMatter = menu.getInputMatterValue();
        if (inputMatter > 0) {
            graphics.drawString(font, inputMatter + " kM  |  " + menu.getEnergyPerTick() + " FE/t", 48, 58, 0x404040, false);
        }
    }
}
