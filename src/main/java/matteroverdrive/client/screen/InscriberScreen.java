package matteroverdrive.client.screen;

import matteroverdrive.menu.InscriberMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class InscriberScreen extends AbstractContainerScreen<InscriberMenu> {
    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> {
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
        }).bounds(leftPos + imageWidth - 72, topPos + 4, 68, 20).build());
    }

    public InscriberScreen(InscriberMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 230;
        inventoryLabelY = 133;
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
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        graphics.fill(leftPos + 8, topPos + 20, leftPos + 14, topPos + 69, 0xFF4A4A4A);
        int energy = scale(menu.getEnergy(), menu.getEnergyCapacity(), 48);
        graphics.fill(leftPos + 9, topPos + 68 - energy, leftPos + 13, topPos + 68, 0xFFCC3333);
        slot(graphics, leftPos + 26, topPos + 43);
        slot(graphics, leftPos + 78, topPos + 43);
        slot(graphics, leftPos + 104, topPos + 43);
        slot(graphics, leftPos + 132, topPos + 43);
        for (int slot = 0; slot < 4; slot++) {
            slot(graphics, leftPos + 52 + slot * 18, topPos + 77);
        }
        graphics.fill(leftPos + 48, topPos + 48, leftPos + 91, topPos + 53, 0xFF5B5B5B);
        int progress = scale(menu.getProgress(), menu.getCycleTime(), 42);
        graphics.fill(leftPos + 48, topPos + 48, leftPos + 48 + progress, topPos + 53, 0xFF51A86B);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(font, menu.getEnergy() + " / " + menu.getEnergyCapacity() + " FE",
                18, 20, 0x404040, false);
        graphics.drawString(font, "[DEBUG] Recipe: " + recipeName(menu.getRecipeTier())
                + " | " + (menu.isRunning() ? "running" : "idle"), 18, 100, 0x8A5A00, false);
        graphics.drawString(font, String.format(Locale.ROOT,
                "[DEBUG] Cycle: %d t / %.2f s | %d FE/t",
                menu.getCycleTime(), menu.getCycleTime() / 20.0D, menu.getEnergyPerTick()),
                18, 110, 0x8A5A00, false);
        graphics.drawString(font, "[DEBUG] Total: " + menu.getTotalEnergy() + " FE",
                18, 120, 0x8A5A00, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    private static String recipeName(int tier) {
        return switch (tier) {
            case 2 -> "Mk1 -> Mk2";
            case 3 -> "Mk2 -> Mk3";
            case 4 -> "Mk3 -> Mk4";
            default -> "none";
        };
    }

    private static void slot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 20, y + 20, 0xFF454545);
        graphics.fill(x + 1, y + 1, x + 19, y + 19, 0xFF9A9A9A);
    }

    private static int scale(int value, int max, int pixels) {
        return max <= 0 || value <= 0 ? 0
                : Math.min(pixels, Math.max(1, (int) Math.round((double) value * pixels / max)));
    }
}
