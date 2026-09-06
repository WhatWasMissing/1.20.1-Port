package matteroverdrive.client.screen;

import matteroverdrive.menu.PylonMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class PylonScreen extends AbstractContainerScreen<PylonMenu> {
    public PylonScreen(PylonMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 330;
        imageHeight = 213;
        inventoryLabelY = 101;
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
        MachineScreenStyle.drawFrame(graphics, x, y, imageWidth, imageHeight,
                inventoryLabelY, MachineScreenStyle.PURPLE);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 29, 142, 62);
        MachineScreenStyle.drawSection(graphics, x + 176, y + 25, 145, 70);

        MachineScreenStyle.drawVerticalBar(graphics, x + 8, y + 32, 7, 44,
                menu.energy(), menu.energyCapacity(), MachineScreenStyle.BLUE);
        MachineScreenStyle.drawVerticalBar(graphics, x + 17, y + 32, 7, 44,
                menu.matter(), menu.matterCapacity(), MachineScreenStyle.PURPLE);
        MachineScreenStyle.drawHorizontalBar(graphics, x + 31, y + 77, 116, 6,
                menu.charge(), menu.maxCharge(), MachineScreenStyle.AMBER);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);

        graphics.drawString(font, "DIMENSIONAL FIELD", 37, 31, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, "FE " + menu.energy() + "/" + menu.energyCapacity(),
                31, 45, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Matter " + menu.matter() + "/" + menu.matterCapacity(),
                31, 57, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Charge " + menu.charge() + "/" + menu.maxCharge(),
                31, 69, MachineScreenStyle.AMBER, false);

        graphics.drawString(font, "1.7 DIMENSIONAL PYLON", 188, 45,
                MachineScreenStyle.PURPLE, false);
        graphics.drawString(font,
                String.format(Locale.ROOT, "Rift strength %.1f%%", menu.dimensionalValue() * 100.0F),
                188, 59, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Generation " + menu.generatedPerTick() + " FE/t",
                188, 72, menu.generatedPerTick() > 0 ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Matter drain " + menu.matterDrainPerSecond() + " /s",
                188, 84, MachineScreenStyle.CYAN, false);

        if (!menu.formed()) {
            graphics.drawString(font, "Structure not formed | relay " + menu.relayChannel(),
                    31, 89, MachineScreenStyle.DANGER, false);
        }
    }
}
