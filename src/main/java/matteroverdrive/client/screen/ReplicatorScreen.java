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
        imageWidth = 176;
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
            Button tab = Button.builder(Component.literal(PAGES[i]), button -> {
                page = target;
                rebuildButtons();
            }).bounds(leftPos + 5 + i * 42, topPos + 5, 40, 15).build();
            tab.active = page != i;
            addRenderableWidget(tab);
        }
        if (page == 0) {
            addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> {
                if (minecraft != null && minecraft.gameMode != null) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
                }
            }).bounds(leftPos + 117, topPos + 102, 52, 16).build());
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
        MachineScreenStyle.drawSection(graphics, x + 17, y + 27, 142, 88);

        if (page == 0) {
            int[][] slots = {{19, 43}, {61, 43}, {115, 34}, {139, 52}};
            for (int[] slot : slots) {
                MachineScreenStyle.drawSlot(graphics, x + slot[0], y + slot[1]);
            }
            MachineScreenStyle.drawLegacyProgressArrow(graphics, x + 83, y + 43,
                    menu.getProgress(), menu.getMaxProgress());
            MachineScreenStyle.drawLegacyEnergyMeter(graphics, x + 6, y + 30,
                    menu.getEnergy(), menu.getEnergyCapacity());
            MachineScreenStyle.drawLegacyMatterMeter(graphics, x + 154, y + 30,
                    menu.getMatter(), menu.getMatterCapacity());
        } else if (page == 3) {
            for (int slot = 0; slot < 4; slot++) {
                MachineScreenStyle.drawSlot(graphics, x + 52 + slot * 18, y + 57);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 21, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, MachineScreenStyle.MUTED, false);
        switch (page) {
            case 1 -> renderTasks(graphics);
            case 2 -> renderConfig(graphics);
            case 3 -> renderUpgrades(graphics);
            default -> renderHome(graphics);
        }
    }

    private void renderHome(GuiGraphics graphics) {
        if (menu.getNetworkTaskAmount() > 0) {
            graphics.drawString(font, "Network x" + menu.getNetworkTaskAmount(), 93, 21, MachineScreenStyle.CYAN, false);
        }
        graphics.drawString(font, menu.getEnergy() + " / " + menu.getEnergyCapacity() + " FE", 18, 29, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.getMatter() + " / " + menu.getMatterCapacity() + " kM", 18, 39, MachineScreenStyle.MUTED, false);
        if (menu.getPatternMatter() > 0) {
            graphics.drawString(font, "Pattern " + menu.getPatternProgress() + "% | " + menu.getPatternMatter() + " kM",
                    18, 72, MachineScreenStyle.TEXT, false);
            graphics.drawString(font, menu.getEnergyPerTick() + " FE/t", 18, 83, MachineScreenStyle.CYAN, false);
        }
        graphics.drawString(font, cycleText(), 18, 94, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, failureText(), 18, 105, MachineScreenStyle.DANGER, false);
    }

    private void renderTasks(GuiGraphics graphics) {
        int progress = menu.getMaxProgress() <= 0 ? 0 : Math.min(100, menu.getProgress() * 100 / menu.getMaxProgress());
        graphics.drawString(font, "REPLICATION TASKS", 38, 35, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, menu.getPatternMatter() > 0 ? "Local replication active" : "No local replication queued",
                24, 50, menu.getPatternMatter() > 0 ? MachineScreenStyle.TEXT : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Local progress: " + progress + "%", 24, 63, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Pattern learned: " + menu.getPatternProgress() + "%", 24, 75, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, "Network queue: " + menu.getNetworkTaskAmount(), 24, 87,
                menu.getNetworkTaskAmount() > 0 ? MachineScreenStyle.CYAN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, cycleText(), 24, 99, MachineScreenStyle.DEBUG, false);
    }

    private void renderConfig(GuiGraphics graphics) {
        graphics.drawString(font, "MACHINE CONFIGURATION", 28, 35, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Network task intake is automatic", 21, 52, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "when the current item network can", 20, 65, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "supply a valid learned pattern.", 25, 76, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Pattern drive and battery slots", 24, 92, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "remain on the Home page.", 36, 103, MachineScreenStyle.MUTED, false);
    }

    private void renderUpgrades(GuiGraphics graphics) {
        graphics.drawString(font, "MACHINE UPGRADES", 43, 35, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, "4 installed upgrade slots", 29, 81, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Upgrade effects are applied by", 24, 94, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "the live replication backend.", 29, 105, MachineScreenStyle.MUTED, false);
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
