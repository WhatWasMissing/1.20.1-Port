package matteroverdrive.client.screen;

import matteroverdrive.menu.ChargingStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ChargingStationScreen extends AbstractContainerScreen<ChargingStationMenu> {
    private static final String[] PAGES = {"HOME", "ANDROID", "UPGRADES"};
    private int page;

    public ChargingStationScreen(ChargingStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 330;
        imageHeight = 213;
        inventoryLabelY = 101;
    }

    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }

    private void rebuildButtons() {
        clearWidgets();
        for (int i = 0; i < PAGES.length; i++) {
            final int target = i;
            Button tab = Button.builder(Component.literal(PAGES[i]), button -> {
                page = target;
                rebuildButtons();
            }).bounds(leftPos + 183 + i * 45, topPos + 29, 43, 15).build();
            tab.active = page != i;
            addRenderableWidget(tab);
        }
        if (page == 0) {
            addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> {
                if (minecraft != null && minecraft.gameMode != null) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
                }
            }).bounds(leftPos + 257, topPos + 72, 56, 16).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        MachineScreenStyle.drawFrame(graphics, x, y, imageWidth, imageHeight,
                inventoryLabelY, MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 27, 142, 64);
        MachineScreenStyle.drawSection(graphics, x + 176, y + 25, 145, 70);

        MachineScreenStyle.drawVerticalBar(graphics, x + 8, y + 31, 7, 44,
                menu.stationEnergy(), menu.stationCapacity(), MachineScreenStyle.BLUE);
        MachineScreenStyle.drawSlot(graphics, x + 79, y + 41);
        MachineScreenStyle.drawHorizontalBar(graphics, x + 25, y + 58, 126, 5,
                menu.batteryEnergy(), menu.batteryCapacity(), MachineScreenStyle.CYAN);
        for (int slot = 0; slot < 4; slot++) {
            MachineScreenStyle.drawSlot(graphics, x + 52 + slot * 18, y + 67);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "ITEM CHARGING", 49, 30, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, menu.batteryCapacity() > 0
                        ? menu.batteryEnergy() + " / " + menu.batteryCapacity() + " FE"
                        : "Insert rechargeable item",
                27, 48, menu.batteryCapacity() > 0 ? MachineScreenStyle.TEXT : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Item " + menu.lastItemTransferred() + " FE/t", 25, 84,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);

        switch (page) {
            case 1 -> renderAndroid(graphics);
            case 2 -> renderUpgrades(graphics);
            default -> renderHome(graphics);
        }
    }

    private void renderHome(GuiGraphics graphics) {
        graphics.drawString(font, "CHARGING STATION", 202, 51, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Buffer " + menu.stationEnergy() + "/" + menu.stationCapacity(),
                184, 64, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Android + item charging active", 184, 78,
                MachineScreenStyle.MUTED, false);
    }

    private void renderAndroid(GuiGraphics graphics) {
        graphics.drawString(font, "ANDROID CHARGING", 199, 51, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Range " + menu.androidRange() + " blocks", 184, 64,
                MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Max " + menu.maxAndroidCharge() + " FE/t each", 184, 75,
                MachineScreenStyle.TEXT, false);
        graphics.drawString(font, menu.androidsCharged() + " Android(s) | "
                        + menu.lastAndroidTransferred() + " FE/t",
                184, 86, menu.androidsCharged() > 0 ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
    }

    private void renderUpgrades(GuiGraphics graphics) {
        graphics.drawString(font, "LEGACY UPGRADES", 203, 51, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Range: wireless radius", 184, 64, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Power: Android charge rate", 184, 75, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Storage: internal FE buffer", 184, 86, MachineScreenStyle.TEXT, false);
    }
}
