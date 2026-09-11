package matteroverdrive.client.screen;

import matteroverdrive.menu.InscriberMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class InscriberScreen extends AbstractContainerScreen<InscriberMenu> {
    private static final String[] PAGES = {"HOME", "TASKS", "UPGRADES"};
    private int page;

    public InscriberScreen(InscriberMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 330;
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
            }).bounds(leftPos + 178 + (i & 1) * 72, topPos + 27 + (i >> 1) * 17, 68, 15).build();
            tab.active = page != i;
            addRenderableWidget(tab);
        }
        if (page == 0) {
            addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> clickMenu(1))
                    .bounds(leftPos + 217, topPos + 150, 56, 18).build());
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
                MachineScreenStyle.GREEN);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 32, 142, 64);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 99, 142, 28);
        MachineScreenStyle.drawSection(graphics, x + 176, y + 25, 145, 157);

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
        graphics.drawString(font, MachineScreenStyle.fit(font, menu.getEnergy() + " / " + menu.getEnergyCapacity() + " FE", 130),
                18, 29, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, recipeName(menu.getRecipeTier()) + " | "
                        + (menu.isRunning() ? "running" : "idle"),
                135), 20, 102, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, String.format(Locale.ROOT, "Cycle %d t / %.2f s | %d FE/t",
                        menu.getCycleTime(), menu.getCycleTime() / 20.0D, menu.getEnergyPerTick()),
                135), 20, 112, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Total " + menu.getTotalEnergy() + " FE", 135),
                20, 122, MachineScreenStyle.DEBUG, false);

        switch (page) {
            case 1 -> renderTasks(graphics);
            case 2 -> renderUpgrades(graphics);
            default -> renderHome(graphics);
        }
    }

    private void renderHome(GuiGraphics graphics) {
        graphics.drawString(font, "INSCRIBER STATUS", 204, 52, MachineScreenStyle.GREEN, false);
        graphics.drawString(font, menu.isRunning() ? "PROCESSING" : "IDLE", 190, 72,
                menu.isRunning() ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Recipe: " + recipeName(menu.getRecipeTier()), 145),
                190, 88, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Demand: " + menu.getEnergyPerTick() + " FE/t", 145),
                190, 104, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Cycle cost: " + menu.getTotalEnergy() + " FE", 145),
                190, 120, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Tasks shows live operation.",
                190, 136, MachineScreenStyle.MUTED, false);
    }

    private void renderTasks(GuiGraphics graphics) {
        int progress = menu.getCycleTime() <= 0 ? 0
                : Math.min(100, menu.getProgress() * 100 / menu.getCycleTime());
        graphics.drawString(font, "INSCRIPTION TASK", 204, 52, MachineScreenStyle.GREEN, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, menu.getRecipeTier() > 0
                        ? recipeName(menu.getRecipeTier()) : "No valid recipe",
                145), 190, 72, menu.getRecipeTier() > 0 ? MachineScreenStyle.TEXT : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Progress: " + progress + "%", 145), 190, 88,
                menu.isRunning() ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Elapsed: " + menu.getProgress() + " / " + menu.getCycleTime() + " t", 145),
                190, 104, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Power: " + menu.getEnergyPerTick() + " FE/t", 145),
                190, 120, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Total: " + menu.getTotalEnergy() + " FE", 145),
                190, 136, MachineScreenStyle.MUTED, false);
    }

    private void renderUpgrades(GuiGraphics graphics) {
        graphics.drawString(font, "UPGRADES", 218, 52, MachineScreenStyle.GREEN, false);
        graphics.drawString(font, "4 physical slots shown left", 190, 72,
                MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Upgrades modify the live", 190, 90,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "cycle and power profile.", 190, 102,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Changes apply immediately.", 190, 120,
                MachineScreenStyle.CYAN, false);
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
