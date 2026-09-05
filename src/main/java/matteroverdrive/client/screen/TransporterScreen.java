package matteroverdrive.client.screen;

import matteroverdrive.menu.TransporterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TransporterScreen extends AbstractContainerScreen<TransporterMenu> {
    private static final String[] PAGES = {"HOME", "TASKS", "LOCATIONS", "UPGRADES"};
    private int page;

    public TransporterScreen(TransporterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 350;
        imageHeight = 230;
        inventoryLabelY = 133;
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
            }).bounds(leftPos + 180 + i * 40, topPos + 29, 38, 15).build();
            tab.active = page != i;
            addRenderableWidget(tab);
        }

        if (page == 0) {
            addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> clickMenu(1))
                    .bounds(leftPos + 224, topPos + 150, 56, 18).build());
        } else if (page == 2) {
            addRenderableWidget(Button.builder(Component.literal("IMPORT"), button -> clickMenu(2))
                    .bounds(leftPos + 186, topPos + 126, 52, 16).build());
            addRenderableWidget(Button.builder(Component.literal("<"), button -> clickMenu(3))
                    .bounds(leftPos + 242, topPos + 126, 24, 16).build());
            addRenderableWidget(Button.builder(Component.literal(">"), button -> clickMenu(4))
                    .bounds(leftPos + 269, topPos + 126, 24, 16).build());
            addRenderableWidget(Button.builder(Component.literal("REMOVE"), button -> clickMenu(5))
                    .bounds(leftPos + 296, topPos + 126, 48, 16).build());
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
                MachineScreenStyle.BLUE);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 32, 142, 63);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 98, 142, 29);
        MachineScreenStyle.drawSection(graphics, x + 176, y + 25, 165, 157);

        MachineScreenStyle.drawSlot(graphics, x + 43, y + 43);
        MachineScreenStyle.drawSlot(graphics, x + 111, y + 43);
        for (int slot = 0; slot < 5; slot++) {
            MachineScreenStyle.drawSlot(graphics, x + 42 + slot * 18, y + 77);
        }
        MachineScreenStyle.drawVerticalBar(graphics, x + 8, y + 30, 7, 40,
                menu.e(), menu.cap(), MachineScreenStyle.RED);
        MachineScreenStyle.drawHorizontalBar(graphics, x + 67, y + 48, 31, 6,
                menu.progress(), menu.cycle(), MachineScreenStyle.BLUE);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.e() + " / " + menu.cap() + " FE",
                18, 29, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.target() ? "Destination linked" : "Bind/import a destination",
                20, 101, menu.target() ? MachineScreenStyle.BLUE : MachineScreenStyle.DANGER, false);
        graphics.drawString(font, "Distance " + menu.dist() + " / " + menu.range()
                        + " | cost " + menu.cost() + " FE",
                20, 111, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Cycle " + menu.cycle() + " t | "
                        + (menu.running() ? "running" : "idle") + " | delay " + menu.cooldown(),
                20, 121, MachineScreenStyle.DEBUG, false);

        switch (page) {
            case 1 -> renderTasks(graphics);
            case 2 -> renderLocations(graphics);
            case 3 -> renderUpgrades(graphics);
            default -> renderHome(graphics);
        }
    }

    private void renderHome(GuiGraphics graphics) {
        graphics.drawString(font, "TRANSPORT STATUS", 214, 52, MachineScreenStyle.BLUE, false);
        graphics.drawString(font, menu.target() ? "DESTINATION READY" : "NO DESTINATION", 190, 72,
                menu.target() ? MachineScreenStyle.GREEN : MachineScreenStyle.DANGER, false);
        graphics.drawString(font, "Range: " + menu.dist() + " / " + menu.range(),
                190, 88, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Transfer cost: " + menu.cost() + " FE",
                190, 104, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, menu.running() ? "Transport cycle active" : "Transporter idle",
                190, 120, menu.running() ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Last transported: " + menu.lastTransportedEntities() + " / 3 entities",
                190, 136, MachineScreenStyle.MUTED, false);
    }

    private void renderTasks(GuiGraphics graphics) {
        int progress = menu.cycle() <= 0 ? 0 : Math.min(100, menu.progress() * 100 / menu.cycle());
        graphics.drawString(font, "TRANSPORT TASK", 217, 52, MachineScreenStyle.BLUE, false);
        graphics.drawString(font, menu.target() ? "Destination acquired" : "Waiting for destination",
                190, 72, menu.target() ? MachineScreenStyle.TEXT : MachineScreenStyle.DANGER, false);
        graphics.drawString(font, "Progress: " + progress + "%",
                190, 88, menu.running() ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Elapsed: " + menu.progress() + " / " + menu.cycle() + " t",
                190, 104, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Energy cost: " + menu.cost() + " FE",
                190, 120, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Cooldown: " + menu.cooldown() + " t",
                190, 136, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Cycle cap: 3 entities",
                190, 152, MachineScreenStyle.MUTED, false);
    }

    private void renderLocations(GuiGraphics graphics) {
        graphics.drawString(font, "SAVED LOCATIONS", 210, 52, MachineScreenStyle.BLUE, false);
        int count = menu.destinationCount();
        if (count <= 0) {
            graphics.drawString(font, "No machine locations saved", 190, 72,
                    MachineScreenStyle.AMBER, false);
            graphics.drawString(font, "Insert a bound flash drive", 190, 88,
                    MachineScreenStyle.MUTED, false);
            graphics.drawString(font, "then press IMPORT.", 190, 100,
                    MachineScreenStyle.MUTED, false);
            graphics.drawString(font, "Direct drive transport still works", 190, 112,
                    MachineScreenStyle.CYAN, false);
        } else {
            graphics.drawString(font, "Saved: " + count, 190, 72,
                    MachineScreenStyle.TEXT, false);
            graphics.drawString(font, "Selected: " + (menu.selectedDestination() + 1) + " / " + count,
                    190, 88, MachineScreenStyle.GREEN, false);
            graphics.drawString(font, "Distance: " + menu.dist() + " blocks", 190, 104,
                    MachineScreenStyle.CYAN, false);
            graphics.drawString(font, "Use < > to change destination", 190, 116,
                    MachineScreenStyle.MUTED, false);
        }
    }

    private void renderUpgrades(GuiGraphics graphics) {
        graphics.drawString(font, "UPGRADES", 228, 52, MachineScreenStyle.BLUE, false);
        graphics.drawString(font, "5 physical slots shown left", 190, 72,
                MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Speed: cycle + cooldown", 190, 90,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Power: energy cost", 190, 102,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Range: maximum distance", 190, 114,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Power Storage: FE capacity", 190, 126,
                MachineScreenStyle.MUTED, false);
    }
}
