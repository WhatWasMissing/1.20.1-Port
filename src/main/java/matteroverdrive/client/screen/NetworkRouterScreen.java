package matteroverdrive.client.screen;

import matteroverdrive.menu.NetworkRouterMenu;
import matteroverdrive.network.ItemNetworkUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class NetworkRouterScreen extends AbstractContainerScreen<NetworkRouterMenu> {
    public NetworkRouterScreen(NetworkRouterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title); imageWidth = 230; imageHeight = 230; inventoryLabelX = 34; inventoryLabelY = 124;
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { renderBackground(graphics); super.render(graphics, mouseX, mouseY, partialTick); renderTooltip(graphics, mouseX, mouseY); }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int accent = menu.routeStatus() == ItemNetworkUtil.MoveStatus.GRAPH_LIMIT ? MachineScreenStyle.RED : MachineScreenStyle.CYAN;
        MachineScreenStyle.drawFrame(graphics, leftPos, topPos, imageWidth, imageHeight, inventoryLabelY, accent);
        MachineScreenStyle.drawSection(graphics, leftPos + 12, topPos + 28, 88, 76);
        MachineScreenStyle.drawSection(graphics, leftPos + 106, topPos + 28, 112, 76);
        MachineScreenStyle.drawDebugPanel(graphics, leftPos + 12, topPos + 106, 206, 14);
        MachineScreenStyle.drawSlot(graphics, leftPos + 44, topPos + 48);
        for (int slot = 0; slot < 4; slot++) MachineScreenStyle.drawSlot(graphics, leftPos + 115 + slot * 18, topPos + 48);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "FILTER / THROUGHPUT", 18, 31, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, fit(menu.filterModeLabel(), 80), 18, 43, menu.filterMode() == 2 ? MachineScreenStyle.CYAN : MachineScreenStyle.TEXT, false);
        String filterDetail = menu.filterMode() == 2 ? "Destinations " + menu.destinationCount() : menu.filterMode() == 1 ? "Exact item + NBT" : "Any item";
        graphics.drawString(font, fit(filterDetail, 80), 18, 72, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, fit("Budget " + menu.itemBudget() + " items/t", 80), 18, 83, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, fit("CH " + menu.channel() + "  PRI " + menu.priority(), 80), 18, 94, MachineScreenStyle.DEBUG, false);

        graphics.drawString(font, "ROUTING CORE", 112, 31, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, fit(menu.routeStatusLabel(), 100), 112, 43, statusColor(), false);
        graphics.drawString(font, fit(menu.executing() ? "ACTIVE EXECUTOR" : "GRAPH MEMBER", 100), 112, 72, menu.executing() ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, fit("E " + menu.endpoints() + "  N " + menu.nodes() + "  R " + menu.routerCount(), 100), 112, 83, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, fit("FE " + menu.energy() + " | 10/item", 100), 112, 94, MachineScreenStyle.MUTED, false);

        String debug = "Moved " + menu.lastMoved() + " | FE/t " + menu.lastEnergyCost() + " | stalls " + menu.stalledTicks() + " | q " + menu.historySize() + "/16";
        graphics.drawString(font, fit(debug, 198), 16, 108, MachineScreenStyle.DEBUG, false);
        String flags = "Blocked switches " + menu.disabledSwitches() + " | pylons " + menu.pylonLinks() + " | guarded sinks " + menu.protectedSinks() + (menu.graphTruncated() ? " | GRAPH CAPPED" : "");
        graphics.drawString(font, fit(flags, 198), 16, 118, menu.graphTruncated() ? MachineScreenStyle.RED : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, MachineScreenStyle.MUTED, false);
    }
    private String fit(String value, int maxWidth) { return font.plainSubstrByWidth(value, maxWidth); }
    private int statusColor() { return switch (menu.routeStatus()) {
        case MOVED -> MachineScreenStyle.GREEN; case NO_ENERGY, GRAPH_LIMIT -> MachineScreenStyle.RED;
        case SECONDARY_ROUTER, IDLE, NO_SOURCE_ITEMS -> MachineScreenStyle.MUTED; default -> MachineScreenStyle.CYAN;
    }; }
}
