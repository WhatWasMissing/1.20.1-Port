package matteroverdrive.client.screen;

import matteroverdrive.menu.NetworkSwitchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class NetworkSwitchScreen extends AbstractContainerScreen<NetworkSwitchMenu> {
    private Button toggleButton;

    public NetworkSwitchScreen(NetworkSwitchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 230;
        imageHeight = 190;
        inventoryLabelX = 34;
        inventoryLabelY = 91;
    }

    @Override
    protected void init() {
        super.init();
        toggleButton = addRenderableWidget(Button.builder(toggleLabel(), button -> {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, NetworkSwitchMenu.BUTTON_TOGGLE);
                    }
                })
                .bounds(leftPos + 150, topPos + 64, 64, 20)
                .build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (toggleButton != null) {
            toggleButton.setMessage(toggleLabel());
        }
    }

    private Component toggleLabel() {
        return Component.literal(menu.isEnabled() ? "DISABLE" : "ENABLE");
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
                inventoryLabelY, menu.isEnabled() ? MachineScreenStyle.CYAN : MachineScreenStyle.RED);

        MachineScreenStyle.drawSection(graphics, leftPos + 12, topPos + 31, 126, 56);
        MachineScreenStyle.drawSection(graphics, leftPos + 146, topPos + 31, 72, 56);

        int centerX = leftPos + 75;
        int centerY = topPos + 59;
        graphics.fill(centerX - 17, centerY - 7, centerX + 17, centerY + 7,
                menu.isEnabled() ? 0xFF174552 : 0xFF4D2525);

        drawLink(graphics, Direction.NORTH, centerX, centerY, centerX, centerY - 20);
        drawLink(graphics, Direction.SOUTH, centerX, centerY, centerX, centerY + 20);
        drawLink(graphics, Direction.WEST, centerX, centerY, centerX - 42, centerY);
        drawLink(graphics, Direction.EAST, centerX, centerY, centerX + 42, centerY);
        drawVerticalMarker(graphics, Direction.UP, centerX - 25, centerY - 15);
        drawVerticalMarker(graphics, Direction.DOWN, centerX + 25, centerY + 11);
    }

    private void drawLink(GuiGraphics graphics, Direction direction, int x1, int y1, int x2, int y2) {
        int color = menu.isConnected(direction) ? 0xFF38C7E8 : 0xFF39444A;
        if (x1 == x2) {
            graphics.fill(x1 - 1, Math.min(y1, y2), x1 + 1, Math.max(y1, y2) + 1, color);
        } else {
            graphics.fill(Math.min(x1, x2), y1 - 1, Math.max(x1, x2) + 1, y1 + 1, color);
        }
    }

    private void drawVerticalMarker(GuiGraphics graphics, Direction direction, int x, int y) {
        int color = menu.isConnected(direction) ? 0xFF38C7E8 : 0xFF39444A;
        graphics.fill(x, y, x + 8, y + 8, color);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "NETWORK LINKS", 18, 33, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "SW", 68, 55, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "N", 72, 36, sideColor(Direction.NORTH), false);
        graphics.drawString(font, "S", 72, 76, sideColor(Direction.SOUTH), false);
        graphics.drawString(font, "W", 27, 55, sideColor(Direction.WEST), false);
        graphics.drawString(font, "E", 117, 55, sideColor(Direction.EAST), false);
        graphics.drawString(font, "U", 46, 39, sideColor(Direction.UP), false);
        graphics.drawString(font, "D", 98, 72, sideColor(Direction.DOWN), false);

        graphics.drawString(font, "STATUS", 152, 33, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, menu.isEnabled() ? "ONLINE" : "OFFLINE", 152, 47,
                menu.isEnabled() ? MachineScreenStyle.GREEN : MachineScreenStyle.RED, false);
        graphics.drawString(font, "Links: " + menu.getConnectionCount() + " / 6", 152, 57,
                MachineScreenStyle.TEXT, false);

        graphics.drawString(font, "Shift-right-click the block for a quick toggle.", 18, 88,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
    }

    private int sideColor(Direction direction) {
        return menu.isConnected(direction) ? MachineScreenStyle.CYAN : MachineScreenStyle.MUTED;
    }
}
