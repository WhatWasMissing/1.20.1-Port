package matteroverdrive.client.screen;

import matteroverdrive.menu.MatterAnalyzerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MatterAnalyzerScreen extends AbstractContainerScreen<MatterAnalyzerMenu> {
    public MatterAnalyzerScreen(MatterAnalyzerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 196;
        inventoryLabelY = 103;
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
                MachineScreenStyle.PURPLE);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 32, 142, 64);

        MachineScreenStyle.drawSlot(graphics, x + 25, y + 43);
        MachineScreenStyle.drawSlot(graphics, x + 79, y + 43);
        MachineScreenStyle.drawSlot(graphics, x + 133, y + 43);
        for (int slot = 0; slot < 4; slot++) {
            MachineScreenStyle.drawSlot(graphics, x + 52 + slot * 18, y + 79);
        }

        MachineScreenStyle.drawHorizontalBar(graphics, x + 48, y + 48, 43, 6,
                menu.getProgress(), menu.getMaxProgress(), MachineScreenStyle.PURPLE);
        MachineScreenStyle.drawVerticalBar(graphics, x + 8, y + 30, 7, 40,
                menu.getEnergy(), menu.getEnergyCapacity(), MachineScreenStyle.RED);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.getEnergy() + " / " + menu.getEnergyCapacity() + " FE",
                18, 29, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Pattern " + menu.getPatternProgress() + "%",
                104, 29, MachineScreenStyle.PURPLE, false);
        if (menu.getInputMatter() > 0) {
            graphics.drawString(font, menu.getInputMatter() + " kM | " + menu.getEnergyPerTick() + " FE/t",
                    48, 58, MachineScreenStyle.TEXT, false);
        }
        String cycle = String.format(java.util.Locale.ROOT, "%d t | %.2f s",
                menu.getMaxProgress(), menu.getMaxProgress() / 20.0D);
        graphics.drawString(font, cycle, 48, 68, MachineScreenStyle.DEBUG, false);
    }
}
