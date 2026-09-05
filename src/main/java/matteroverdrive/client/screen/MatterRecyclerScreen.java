package matteroverdrive.client.screen;

import matteroverdrive.menu.MatterRecyclerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class MatterRecyclerScreen extends AbstractContainerScreen<MatterRecyclerMenu> {
    private static final String[] PAGES = {"HOME", "TASKS", "UPGRADES"};
    private int page;

    public MatterRecyclerScreen(MatterRecyclerMenu menu, Inventory inventory, Component title) {
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
            addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> {
                if (minecraft != null && minecraft.gameMode != null) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
                }
            }).bounds(leftPos + 257, topPos + 70, 56, 16).build());
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
        MachineScreenStyle.drawSection(graphics, x + 17, y + 32, 142, 54);
        MachineScreenStyle.drawSection(graphics, x + 176, y + 25, 145, 65);

        MachineScreenStyle.drawSlot(graphics, x + 25, y + 43);
        MachineScreenStyle.drawSlot(graphics, x + 79, y + 43);
        MachineScreenStyle.drawSlot(graphics, x + 133, y + 43);
        for (int slot = 0; slot < 4; slot++) {
            MachineScreenStyle.drawSlot(graphics, x + 52 + slot * 18, y + 65);
        }

        MachineScreenStyle.drawHorizontalBar(graphics, x + 48, y + 48, 43, 6,
                menu.getProgress(), menu.getMaxProgress(), MachineScreenStyle.GREEN);
        MachineScreenStyle.drawVerticalBar(graphics, x + 8, y + 30, 7, 40,
                menu.getEnergy(), menu.getEnergyCapacity(), MachineScreenStyle.RED);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.getEnergy() + " / " + menu.getEnergyCapacity() + " FE",
                18, 29, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.getMatter() > 0 ? menu.getMatter() + " kM" : "Waiting for recyclable input",
                48, 56, menu.getMatter() > 0 ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);

        switch (page) {
            case 1 -> renderTasks(graphics);
            case 2 -> renderUpgrades(graphics);
            default -> renderHome(graphics);
        }
    }

    private void renderHome(GuiGraphics graphics) {
        boolean active = menu.getMatter() > 0 && menu.getProgress() > 0;
        graphics.drawString(font, "RECYCLER STATUS", 204, 51, MachineScreenStyle.GREEN, false);
        graphics.drawString(font, active ? "RECYCLING" : "IDLE", 188, 64,
                active ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Matter yield " + menu.getMatter() + " kM", 188, 76,
                MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Demand " + menu.getEnergyPerTick() + " FE/t", 188, 87,
                MachineScreenStyle.CYAN, false);
    }

    private void renderTasks(GuiGraphics graphics) {
        int max = Math.max(1, menu.getMaxProgress());
        int progress = Math.min(max, Math.max(0, menu.getProgress()));
        int percent = progress * 100 / max;
        graphics.drawString(font, "RECYCLING TASK", 202, 51, MachineScreenStyle.GREEN, false);
        graphics.drawString(font, "Progress " + percent + "%", 188, 64, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Cycle " + progress + " / " + max + " t", 188, 75,
                MachineScreenStyle.TEXT, false);
        graphics.drawString(font, String.format(Locale.ROOT, "Duration %.2f s", max / 20.0D),
                188, 86, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Yield " + menu.getMatter() + " kM | " + menu.getEnergyPerTick() + " FE/t",
                188, 97, MachineScreenStyle.CYAN, false);
    }

    private void renderUpgrades(GuiGraphics graphics) {
        graphics.drawString(font, "UPGRADES", 219, 51, MachineScreenStyle.GREEN, false);
        graphics.drawString(font, "4 physical slots shown left", 188, 64,
                MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Installed machine upgrades", 188, 76,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "modify the live recycle cycle", 188, 87,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "and FE demand backend.", 188, 98,
                MachineScreenStyle.MUTED, false);
    }
}
