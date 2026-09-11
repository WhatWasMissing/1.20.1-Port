package matteroverdrive.client.screen;

import matteroverdrive.menu.ReplicatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ReplicatorScreen extends AbstractContainerScreen<ReplicatorMenu> {
    private static final String[] PAGES = {"HOME", "TASKS", "CONFIG", "UPGRADES"};
    private int page;

    public ReplicatorScreen(ReplicatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 330;
        imageHeight = 218;
        inventoryLabelY = 125;
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
            int column = i & 1, row = i >> 1;
            Button tab = Button.builder(Component.literal(PAGES[i]), button -> {
                page = target;
                rebuildButtons();
            }).bounds(leftPos + 178 + column * 72, topPos + 27 + row * 17, 68, 15).build();
            tab.active = page != i;
            addRenderableWidget(tab);
        }
        if (page == 0) {
            addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> {
                if (minecraft != null && minecraft.gameMode != null) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
                }
            }).bounds(leftPos + 217, topPos + 132, 56, 18).build());
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
        MachineScreenStyle.drawFrame(graphics, x, y, imageWidth, imageHeight, inventoryLabelY, MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 27, 142, 91);
        MachineScreenStyle.drawSection(graphics, x + 176, y + 25, 145, 158);

        int[][] slots = {{19, 43}, {61, 43}, {115, 34}, {139, 52}};
        for (int[] slot : slots) {
            MachineScreenStyle.drawSlot(graphics, x + slot[0], y + slot[1]);
        }
        for (int slot = 0; slot < 4; slot++) {
            MachineScreenStyle.drawSlot(graphics, x + 52 + slot * 18, y + 101);
        }
        MachineScreenStyle.drawLegacyProgressArrow(graphics, x + 83, y + 43,
                menu.getProgress(), menu.getMaxProgress());
        MachineScreenStyle.drawLegacyEnergyMeter(graphics, x + 6, y + 30,
                menu.getEnergy(), menu.getEnergyCapacity());
        MachineScreenStyle.drawLegacyMatterMeter(graphics, x + 154, y + 30,
                menu.getMatter(), menu.getMatterCapacity());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, menu.getEnergy() + " / " + menu.getEnergyCapacity() + " FE", 130), 18, 29, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, menu.getMatter() + " / " + menu.getMatterCapacity() + " kM", 130), 18, 39, MachineScreenStyle.MUTED, false);
        if (menu.getPatternMatter() > 0) {
            graphics.drawString(font, MachineScreenStyle.fit(font, "Pattern " + menu.getPatternProgress() + "% | " + menu.getPatternMatter() + " kM", 130),
                    18, 72, MachineScreenStyle.TEXT, false);
            graphics.drawString(font, MachineScreenStyle.fit(font, menu.getEnergyPerTick() + " FE/t", 130), 18, 83, MachineScreenStyle.CYAN, false);
        }
        graphics.drawString(font, MachineScreenStyle.fit(font, cycleText(), 160), 18, 94, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, failureText(), 160), 18, 105, MachineScreenStyle.DANGER, false);

        switch (page) {
            case 1 -> renderTasks(graphics);
            case 2 -> renderConfig(graphics);
            case 3 -> renderUpgrades(graphics);
            default -> renderHome(graphics);
        }
    }

    private void renderHome(GuiGraphics graphics) {
        graphics.drawString(font, "REPLICATOR STATUS", 201, 52, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, menu.getPatternMatter() > 0 ? "Pattern loaded" : "Waiting for pattern", 190, 72,
                menu.getPatternMatter() > 0 ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Network queue " + menu.getNetworkTaskAmount(), 130), 190, 88,
                menu.getNetworkTaskAmount() > 0 ? MachineScreenStyle.CYAN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Demand " + menu.getEnergyPerTick() + " FE/t", 130), 190, 104, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Matter cost " + menu.getPatternMatter() + " kM", 130), 190, 120, MachineScreenStyle.MUTED, false);
    }

    private void renderTasks(GuiGraphics graphics) {
        int progress = menu.getMaxProgress() <= 0 ? 0 : Math.min(100, menu.getProgress() * 100 / menu.getMaxProgress());
        graphics.drawString(font, "REPLICATION TASKS", 198, 52, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, menu.getPatternMatter() > 0 ? "Local task active" : "No local task", 190, 72,
                menu.getPatternMatter() > 0 ? MachineScreenStyle.TEXT : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Local progress " + progress + "%", 130), 190, 88, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Pattern " + menu.getPatternProgress() + "%", 130), 190, 104, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Network queue " + menu.getNetworkTaskAmount(), 130), 190, 120,
                menu.getNetworkTaskAmount() > 0 ? MachineScreenStyle.CYAN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, cycleText(), 130), 190, 136, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, failureText(), 130), 190, 152, MachineScreenStyle.DANGER, false);
    }

    private void renderConfig(GuiGraphics graphics) {
        graphics.drawString(font, "CONFIGURATION", 208, 52, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Pattern drive", 190, 72, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Battery / FE input", 190, 88, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Network task intake", 190, 104, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Current network dispatch is", 190, 126, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "automatic when a valid learned", 190, 138, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "pattern can be supplied.", 190, 150, MachineScreenStyle.MUTED, false);
    }

    private void renderUpgrades(GuiGraphics graphics) {
        graphics.drawString(font, "UPGRADES", 218, 52, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, "4 physical slots shown left", 190, 72, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Installed upgrades affect live", 190, 94, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "cycle timing / machine demand.", 190, 106, MachineScreenStyle.MUTED, false);
    }

    private String cycleText() {
        return menu.getMaxProgress() > 0
                ? String.format(java.util.Locale.ROOT, "Cycle %d t | %.2f s", menu.getMaxProgress(), menu.getMaxProgress() / 20.0D)
                : "Cycle inactive";
    }

    private String failureText() {
        return menu.getPatternMatter() > 0
                ? String.format(java.util.Locale.ROOT, "Failure %.4f%%", menu.getFailChance() * 100.0D)
                : "Failure inactive";
    }
}
