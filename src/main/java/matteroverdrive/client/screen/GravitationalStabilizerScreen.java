package matteroverdrive.client.screen;

import matteroverdrive.menu.GravitationalStabilizerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GravitationalStabilizerScreen extends AbstractContainerScreen<GravitationalStabilizerMenu> {
    private static final String[] PAGES = {"HOME", "BEAM", "UPGRADES"};
    private int page;

    public GravitationalStabilizerScreen(GravitationalStabilizerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 330;
        imageHeight = 202;
        inventoryLabelY = 89;
    }

    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }

    private void rebuildButtons() {
        clearWidgets();
        addRenderableWidget(Button.builder(Component.literal("RS MODE"), button -> clickMenu(1))
                .bounds(leftPos + 252, topPos + 5, 69, 16).build());
        for (int i = 0; i < PAGES.length; i++) {
            final int target = i;
            Button tab = Button.builder(Component.literal(PAGES[i]), b -> {
                page = target;
                rebuildButtons();
            }).bounds(leftPos + 183 + i * 45, topPos + 29, 43, 15).build();
            tab.active = page != i;
            addRenderableWidget(tab);
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
        int x = leftPos, y = topPos;
        MachineScreenStyle.drawFrame(graphics, x, y, imageWidth, imageHeight, inventoryLabelY,
                MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 25, 142, 39);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 66, 142, 20);
        MachineScreenStyle.drawSection(graphics, x + 176, y + 25, 145, 61);
        for (int slot = 0; slot < 4; slot++) {
            MachineScreenStyle.drawSlot(graphics, x + 52 + slot * 18, y + 36);
        }
        MachineScreenStyle.drawHorizontalBar(graphics, x + 25, y + 56, 126, 5,
                menu.energy(), menu.energyCapacity(), MachineScreenStyle.CYAN);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Power upgrades", 25, 29, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.energy() + " / " + menu.energyCapacity() + " FE",
                25, 45, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Draw " + menu.powerUsed() + " / " + menu.requiredPower() + " FE/t",
                20, 69, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, status(), 20, 78, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, MachineScreenStyle.MUTED, false);

        switch (page) {
            case 1 -> renderBeam(graphics);
            case 2 -> renderUpgrades(graphics);
            default -> renderHome(graphics);
        }
    }

    private void renderHome(GuiGraphics graphics) {
        graphics.drawString(font, "STABILIZER STATUS", 202, 52, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, menu.isPowered() ? "POWERED" : "NO REACTOR POWER", 188, 66,
                menu.isPowered() ? MachineScreenStyle.GREEN : MachineScreenStyle.DANGER, false);
        graphics.drawString(font, "Redstone " + redstoneLabel(), 188, 78,
                menu.redstoneAllowsOperation() ? MachineScreenStyle.CYAN : MachineScreenStyle.DANGER, false);
    }

    private void renderBeam(GuiGraphics graphics) {
        graphics.drawString(font, "BEAM TELEMETRY", 205, 52, MachineScreenStyle.CYAN, false);
        if (menu.anomalyDistance() >= 0) {
            graphics.drawString(font, "Anomaly lock " + menu.anomalyDistance() + " blocks", 188, 66,
                    MachineScreenStyle.GREEN, false);
        } else {
            graphics.drawString(font, "No anomaly lock", 188, 66, MachineScreenStyle.MUTED, false);
        }
        graphics.drawString(font, menu.isBeamBlocked()
                        ? "Blocked at " + menu.beamBlockedDistance() + " blocks" : "Beam path clear",
                188, 78, menu.isBeamBlocked() ? MachineScreenStyle.DANGER : MachineScreenStyle.CYAN, false);
    }

    private void renderUpgrades(GuiGraphics graphics) {
        graphics.drawString(font, "UPGRADES", 218, 52, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "4 physical slots shown left", 188, 66,
                MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Required " + menu.requiredPower() + " FE/t", 188, 78,
                MachineScreenStyle.MUTED, false);
    }

    private String status() {
        if (!menu.redstoneAllowsOperation()) return "Paused by redstone (" + redstoneLabel() + ")";
        if (!menu.isPowered()) return "Waiting for reactor power";
        if (menu.anomalyDistance() >= 0) return "Locked: anomaly " + menu.anomalyDistance() + " blocks";
        if (menu.isBeamBlocked()) return "Beam blocked: " + menu.beamBlockedDistance() + " blocks";
        return "Powered: no anomaly in beam";
    }

    private String redstoneLabel() {
        return switch (menu.redstoneMode()) {
            case 1 -> "HIGH";
            case 2 -> "LOW";
            default -> "IGNORED";
        };
    }
}
