package matteroverdrive.client.screen;

import matteroverdrive.menu.StarMapMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Modern Star Map shell with the navigable presentation that existed in the
 * original 1.7 line. Contract information remains available while the deeper
 * galaxy/travel simulation is restored incrementally.
 */
public class StarMapScreen extends AbstractContainerScreen<StarMapMenu> {
    private float zoom = 1.0F;
    private float panX;
    private float panY;
    private double dragX;
    private double dragY;
    private boolean dragging;

    private static final int[][] STARS = {
            {-58,-18,1},{-43,16,0},{-28,-8,0},{-12,20,1},{2,-22,0},{18,5,1},
            {32,-13,0},{47,17,0},{58,-4,1},{-51,4,0},{-35,-24,0},{-20,6,1},
            {-3,-2,1},{12,24,0},{26,-27,0},{39,1,1},{53,27,0},{-61,27,0}
    };

    public StarMapScreen(StarMapMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 222;
        imageHeight = 196;
        inventoryLabelY = 83;
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

        int mapX = leftPos + 10;
        int mapY = topPos + 25;
        int mapW = 202;
        int mapH = 52;
        graphics.fill(mapX, mapY, mapX + mapW, mapY + mapH, 0xEE081117);
        graphics.renderOutline(mapX, mapY, mapW, mapH, MachineScreenStyle.CYAN);

        int cx = mapX + mapW / 2 + Math.round(panX);
        int cy = mapY + mapH / 2 + Math.round(panY);
        for (int[] star : STARS) {
            int sx = cx + Math.round(star[0] * zoom);
            int sy = cy + Math.round(star[1] * zoom);
            if (sx > mapX + 2 && sx < mapX + mapW - 2 && sy > mapY + 2 && sy < mapY + mapH - 2) {
                int size = star[2] == 1 ? 2 : 1;
                int color = star[2] == 1 ? 0xFF73E8FF : 0xFFC7DCE3;
                graphics.fill(sx, sy, sx + size, sy + size, color);
            }
        }
        graphics.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFFFFD56A);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Active contracts: " + menu.active(), 12, 81,
                MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Ready to redeem: " + menu.complete(), 12, 92,
                menu.complete() > 0 ? MachineScreenStyle.CYAN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, String.format("Map zoom %.1fx", zoom), 136, 81,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Wheel: zoom  Drag: pan", 112, 92,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY + 28,
                MachineScreenStyle.MUTED, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        zoom = Math.max(0.45F, Math.min(2.5F, zoom + (float) delta * 0.15F));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mapX = leftPos + 10;
        int mapY = topPos + 25;
        if (button == 0 && mouseX >= mapX && mouseX < mapX + 202 && mouseY >= mapY && mouseY < mapY + 52) {
            dragging = true;
            dragX = mouseX;
            dragY = mouseY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragDeltaX, double dragDeltaY) {
        if (dragging && button == 0) {
            panX += (float) (mouseX - dragX);
            panY += (float) (mouseY - dragY);
            panX = Math.max(-80.0F, Math.min(80.0F, panX));
            panY = Math.max(-40.0F, Math.min(40.0F, panY));
            dragX = mouseX;
            dragY = mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragDeltaX, dragDeltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
