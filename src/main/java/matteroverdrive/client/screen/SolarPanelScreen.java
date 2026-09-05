package matteroverdrive.client.screen;

import matteroverdrive.blockentity.SolarPanelBlockEntity;
import matteroverdrive.menu.SolarPanelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class SolarPanelScreen extends AbstractContainerScreen<SolarPanelMenu> {
    private static final String[] PAGES = {"HOME", "GEN", "UPGRADES"};
    private int page;

    public SolarPanelScreen(SolarPanelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 330;
        imageHeight = 204;
        inventoryLabelY = 111;
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
            }).bounds(leftPos + 257, topPos + 76, 56, 16).build());
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
                MachineScreenStyle.AMBER);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 32, 142, 39);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 73, 142, 31);
        MachineScreenStyle.drawSection(graphics, x + 176, y + 25, 145, 80);

        MachineScreenStyle.drawVerticalBar(graphics, x + 8, y + 30, 7, 40,
                menu.getEnergy(), menu.getEnergyCapacity(), MachineScreenStyle.AMBER);
        for (int slot = 0; slot < 2; slot++) {
            MachineScreenStyle.drawSlot(graphics, x + 70 + slot * 18, y + 43);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.getEnergy() + " / " + menu.getEnergyCapacity() + " FE",
                18, 29, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Generation " + menu.getCurrentGeneration() + "/"
                        + SolarPanelBlockEntity.PEAK_GENERATION + " FE/t | sent " + menu.getLastOutput(),
                20, 76, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, String.format(Locale.ROOT, "Sky %s | light %d | sun %.3f",
                        menu.dimensionHasSky() && menu.canSeeSky() ? "yes" : "no",
                        menu.getEffectiveSkyLight(), menu.getDaylightFactor()),
                20, 86, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Output " + menu.getMaxOutputPerSide() + " FE/t/side",
                20, 96, MachineScreenStyle.DEBUG, false);

        switch (page) {
            case 1 -> renderGeneration(graphics);
            case 2 -> renderUpgrades(graphics);
            default -> renderHome(graphics);
        }
    }

    private void renderHome(GuiGraphics graphics) {
        boolean producing = menu.getCurrentGeneration() > 0;
        graphics.drawString(font, "SOLAR STATUS", 213, 51, MachineScreenStyle.AMBER, false);
        graphics.drawString(font, producing ? "GENERATING" : "IDLE", 188, 64,
                producing ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Buffer " + menu.getEnergy() + "/" + menu.getEnergyCapacity(),
                188, 76, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Sent last tick " + menu.getLastOutput() + " FE", 188, 88,
                MachineScreenStyle.CYAN, false);
    }

    private void renderGeneration(GuiGraphics graphics) {
        int peak = Math.max(1, SolarPanelBlockEntity.PEAK_GENERATION);
        int percent = Math.min(100, Math.max(0, menu.getCurrentGeneration() * 100 / peak));
        graphics.drawString(font, "GENERATION", 215, 51, MachineScreenStyle.AMBER, false);
        graphics.drawString(font, "Current " + menu.getCurrentGeneration() + " FE/t (" + percent + "%)",
                188, 64, MachineScreenStyle.GREEN, false);
        graphics.drawString(font, "Peak " + peak + " FE/t", 188, 76,
                MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Sky access " + (menu.dimensionHasSky() && menu.canSeeSky() ? "YES" : "NO"),
                188, 88, (menu.dimensionHasSky() && menu.canSeeSky()) ? MachineScreenStyle.CYAN : MachineScreenStyle.DANGER, false);
        graphics.drawString(font, String.format(Locale.ROOT, "Light %d | daylight %.3f",
                        menu.getEffectiveSkyLight(), menu.getDaylightFactor()),
                188, 100, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Per-side output cap " + menu.getMaxOutputPerSide() + " FE/t",
                188, 112, MachineScreenStyle.MUTED, false);
    }

    private void renderUpgrades(GuiGraphics graphics) {
        graphics.drawString(font, "UPGRADES", 219, 51, MachineScreenStyle.AMBER, false);
        graphics.drawString(font, "2 physical slots shown left", 188, 64,
                MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Installed upgrades affect the", 188, 76,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "live panel buffer/output backend", 188, 88,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "without hiding inventory slots.", 188, 100,
                MachineScreenStyle.MUTED, false);
    }
}
