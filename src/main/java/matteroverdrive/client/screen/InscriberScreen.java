package matteroverdrive.client.screen;

import matteroverdrive.menu.InscriberMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class InscriberScreen extends AbstractContainerScreen<InscriberMenu> {
    public InscriberScreen(InscriberMenu menu, Inventory inventory, Component title) {
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
                MachineScreenStyle.GREEN);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 32, 142, 64);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 99, 142, 28);

        MachineScreenStyle.drawVerticalBar(graphics, x + 8, y + 30, 7, 40,
                menu.getEnergy(), menu.getEnergyCapacity(), MachineScreenStyle.RED);
        MachineScreenStyle.drawSlot(graphics, x + 26, y + 43);
        MachineScreenStyle.drawSlot(graphics, x + 78, y + 43);
        MachineScreenStyle.drawSlot(graphics, x + 104, y + 43);
        MachineScreenStyle.drawSlot(graphics, x + 132, y + 43);
        for (int slot = 0; slot < 4; slot++) {
            MachineScreenStyle.drawSlot(graphics, x + 52 + slot * 18, y + 77);
        }
        MachineScreenStyle.drawHorizontalBar(graphics, x + 48, y + 48, 43, 6,
                menu.getProgress(), menu.getCycleTime(), MachineScreenStyle.GREEN);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.getEnergy() + " / " + menu.getEnergyCapacity() + " FE",
                18, 29, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, recipeName(menu.getRecipeTier()) + " | "
                        + (menu.isRunning() ? "running" : "idle"),
                20, 102, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, String.format(Locale.ROOT, "Cycle %d t / %.2f s | %d FE/t",
                        menu.getCycleTime(), menu.getCycleTime() / 20.0D, menu.getEnergyPerTick()),
                20, 112, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Total " + menu.getTotalEnergy() + " FE",
                20, 122, MachineScreenStyle.DEBUG, false);
    }

    private static String recipeName(int tier) {
        return switch (tier) {
            case 2 -> "Mk1 -> Mk2";
            case 3 -> "Mk2 -> Mk3";
            case 4 -> "Mk3 -> Mk4";
            default -> "No recipe";
        };
    }
}
