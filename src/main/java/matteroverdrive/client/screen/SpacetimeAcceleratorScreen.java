package matteroverdrive.client.screen;

import matteroverdrive.menu.SpacetimeAcceleratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class SpacetimeAcceleratorScreen extends AbstractContainerScreen<SpacetimeAcceleratorMenu> {
    private static final String[] PAGES = {"HOME", "TASKS", "UPGRADES"};
    private int page;

    public SpacetimeAcceleratorScreen(SpacetimeAcceleratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 330;
        imageHeight = 184;
        inventoryLabelY = 91;
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
            addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> clickMenu(1))
                    .bounds(leftPos + 188, topPos + 70, 55, 16).build());
            addRenderableWidget(Button.builder(Component.literal("MAT+"), button -> clickMenu(2))
                    .bounds(leftPos + 248, topPos + 70, 55, 16).build());
        }
    }

    private void clickMenu(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
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
        int x = leftPos;
        int y = topPos;
        MachineScreenStyle.drawFrame(graphics, x, y, imageWidth, imageHeight, inventoryLabelY,
                MachineScreenStyle.PURPLE);
        MachineScreenStyle.drawSection(graphics, x + 18, y + 31, 149, 54);
        MachineScreenStyle.drawSection(graphics, x + 176, y + 25, 145, 65);

        MachineScreenStyle.drawVerticalBar(graphics, x + 8, y + 31, 7, 40,
                menu.getEnergy(), menu.getEnergyCapacity(), MachineScreenStyle.BLUE);
        MachineScreenStyle.drawVerticalBar(graphics, x + 16, y + 31, 7, 40,
                menu.getMatter(), menu.getMatterCapacity(), MachineScreenStyle.PURPLE);
        MachineScreenStyle.drawHorizontalBar(graphics, x + 28, y + 74, 87, 6,
                menu.getPulseTimer(), menu.getPulseInterval(), MachineScreenStyle.PURPLE);

        for (int slot = 0; slot < 4; slot++) {
            MachineScreenStyle.drawSlot(graphics, x + 52 + slot * 18, y + 64);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);

        graphics.drawString(font, "FE " + menu.getEnergy() + "/" + menu.getEnergyCapacity(),
                28, 32, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Matter " + menu.getMatter() + "/" + menu.getMatterCapacity(),
                28, 42, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Pulse " + menu.getPulseInterval() + "t  Radius " + menu.getRadius(),
                28, 52, MachineScreenStyle.MUTED, false);

        String status;
        int statusColor;
        if (menu.isRedstoneBlocked()) {
            status = "Stopped by redstone signal";
            statusColor = MachineScreenStyle.RED;
        } else if (menu.isActive()) {
            status = "Active | " + menu.getEnergyPerTick() + " FE/t | last "
                    + menu.getLastAcceleratedTargets();
            statusColor = MachineScreenStyle.TEXT;
        } else {
            status = "Needs FE + matter";
            statusColor = MachineScreenStyle.AMBER;
        }
        graphics.drawString(font, status, 28, 62, statusColor, false);

        switch (page) {
            case 1 -> renderTasks(graphics);
            case 2 -> renderUpgrades(graphics);
            default -> renderHome(graphics);
        }
    }

    private void renderHome(GuiGraphics graphics) {
        graphics.drawString(font, "SPACE-TIME FIELD", 201, 51, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, menu.isActive() ? "FIELD ACTIVE" : "FIELD IDLE", 188, 62,
                menu.isActive() ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "2r x 2r legacy footprint", 188, 86,
                MachineScreenStyle.MUTED, false);
    }

    private void renderTasks(GuiGraphics graphics) {
        int interval = Math.max(1, menu.getPulseInterval());
        int timer = Math.min(interval, Math.max(0, menu.getPulseTimer()));
        int percent = timer * 100 / interval;
        graphics.drawString(font, "ACCELERATION TASK", 194, 51, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, "Next pulse " + percent + "%", 188, 64, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Interval " + interval + " t / "
                        + String.format(Locale.ROOT, "%.2f", interval / 20.0D) + " s",
                188, 75, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Last targets " + menu.getLastAcceleratedTargets(), 188, 86,
                MachineScreenStyle.CYAN, false);
    }

    private void renderUpgrades(GuiGraphics graphics) {
        graphics.drawString(font, "1.12 UPGRADE EFFECTS", 188, 51, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, "Speed: pulse interval", 188, 63, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Range: radius (x6 max)", 188, 74, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Power/Storage/Matter affect cost", 188, 85, MachineScreenStyle.MUTED, false);
    }
}
