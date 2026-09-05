package matteroverdrive.client.screen;

import matteroverdrive.menu.PatternStorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PatternStorageScreen extends AbstractContainerScreen<PatternStorageMenu> {
    private static final String[] PAGES = {"HOME", "DRIVES", "UPGRADES"};
    private int page;

    public PatternStorageScreen(PatternStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 330;
        imageHeight = 220;
        inventoryLabelY = 127;
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
        MachineScreenStyle.drawFrame(graphics, x, y, imageWidth, imageHeight, inventoryLabelY,
                MachineScreenStyle.PURPLE);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 28, 142, 70);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 101, 142, 20);
        MachineScreenStyle.drawSection(graphics, x + 176, y + 25, 145, 96);

        MachineScreenStyle.drawSlot(graphics, x + 25, y + 43);
        for (int i = 0; i < 6; i++) {
            MachineScreenStyle.drawSlot(graphics, x + 61 + (i % 3) * 24, y + 31 + (i / 3) * 24);
        }
        for (int slot = 0; slot < 4; slot++) {
            MachineScreenStyle.drawSlot(graphics, x + 52 + slot * 18, y + 79);
        }
        MachineScreenStyle.drawVerticalBar(graphics, x + 8, y + 30, 7, 40,
                menu.getEnergy(), menu.getEnergyCapacity(), MachineScreenStyle.RED);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, menu.getEnergy() + " / " + menu.getEnergyCapacity() + " FE",
                18, 20, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Patterns " + menu.getPatternCount() + "/12 | Idle "
                        + menu.getIdleEnergyUsePerTick() + " FE/t",
                20, 104, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Capacity " + menu.getEnergyCapacity() + " FE",
                20, 114, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);

        switch (page) {
            case 1 -> renderDrives(graphics);
            case 2 -> renderUpgrades(graphics);
            default -> renderHome(graphics);
        }
    }

    private void renderHome(GuiGraphics graphics) {
        graphics.drawString(font, "PATTERN STORAGE", 202, 52, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, "Stored patterns " + menu.getPatternCount() + " / 12", 188, 70,
                menu.getPatternCount() > 0 ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Idle draw " + menu.getIdleEnergyUsePerTick() + " FE/t", 188, 84,
                MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Buffer " + menu.getEnergy() + " / " + menu.getEnergyCapacity(), 188, 98,
                MachineScreenStyle.TEXT, false);
    }

    private void renderDrives(GuiGraphics graphics) {
        graphics.drawString(font, "PATTERN DRIVES", 203, 52, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, "6 physical drive slots shown left", 188, 70,
                MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Indexed patterns " + menu.getPatternCount() + " / 12", 188, 84,
                MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Drive contents feed Pattern Monitor", 188, 98,
                MachineScreenStyle.MUTED, false);
    }

    private void renderUpgrades(GuiGraphics graphics) {
        graphics.drawString(font, "UPGRADES", 218, 52, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, "4 physical slots shown left", 188, 70,
                MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Storage upgrades alter FE capacity", 188, 84,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Installed effects apply live", 188, 98,
                MachineScreenStyle.CYAN, false);
    }
}
