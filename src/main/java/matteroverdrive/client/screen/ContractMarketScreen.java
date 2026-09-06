package matteroverdrive.client.screen;

import matteroverdrive.blockentity.ContractMarketBlockEntity;
import matteroverdrive.menu.ContractMarketMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ContractMarketScreen extends AbstractContainerScreen<ContractMarketMenu> {
    public ContractMarketScreen(ContractMarketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 256;
        imageHeight = 220;
        inventoryLabelX = 47;
        inventoryLabelY = 119;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineScreenStyle.drawFrame(graphics, leftPos, topPos, imageWidth, imageHeight,
                inventoryLabelY, MachineScreenStyle.CYAN);

        // Real 18-slot contract board.
        MachineScreenStyle.drawSection(graphics, leftPos + 12, topPos + 31, 120, 76);
        for (int slot = 0; slot < ContractMarketMenu.MARKET_SLOT_COUNT; slot++) {
            int col = slot % 6;
            int row = slot / 6;
            MachineScreenStyle.drawSlot(graphics, leftPos + 17 + col * 18, topPos + 41 + row * 18);
        }

        // Live operator/status panel backed by synchronized server data.
        MachineScreenStyle.drawSection(graphics, leftPos + 140, topPos + 31, 104, 76);
        int occupied = menu.getOccupiedSlots();
        MachineScreenStyle.drawHorizontalBar(graphics, leftPos + 150, topPos + 83, 84, 8,
                occupied, ContractMarketBlockEntity.OFFER_SLOTS, MachineScreenStyle.CYAN);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "CONTRACT BOARD", 18, 33, MachineScreenStyle.CYAN, false);

        int occupied = menu.getOccupiedSlots();
        int free = Math.max(0, ContractMarketBlockEntity.OFFER_SLOTS - occupied);
        graphics.drawString(font, "MARKET STATUS", 146, 33, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Offers  " + occupied + " / " + ContractMarketBlockEntity.OFFER_SLOTS,
                150, 48, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Free    " + free,
                150, 59, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Next    " + formatTicks(menu.getRefreshTicks()),
                150, 70, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Take-only inventory", 150, 95, MachineScreenStyle.MUTED, false);

        graphics.drawString(font, "Complete contracts in your inventory, then use the Market to redeem.",
                18, 109, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
    }

    private static String formatTicks(int ticks) {
        int totalSeconds = Math.max(0, ticks) / 20;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }
}
