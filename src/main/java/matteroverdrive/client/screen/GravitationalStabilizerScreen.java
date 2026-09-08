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
        imageWidth = 344;
        imageHeight = 210;
        inventoryLabelY = 97;
    }

    @Override protected void init() { super.init(); rebuildButtons(); }

    private void rebuildButtons() {
        clearWidgets();
        addRenderableWidget(Button.builder(Component.literal("RS MODE"), button -> clickMenu(1))
                .bounds(leftPos + 266, topPos + 5, 69, 16).build());
        for (int i = 0; i < PAGES.length; i++) {
            final int target = i;
            Button tab = Button.builder(Component.literal(PAGES[i]), b -> {
                page = target;
                rebuildButtons();
            }).bounds(leftPos + 190 + i * 47, topPos + 29, 45, 15).build();
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
        MachineScreenStyle.drawSection(graphics, x + 17, y + 25, 150, 47);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 75, 150, 19);
        MachineScreenStyle.drawSection(graphics, x + 181, y + 25, 154, 69);
        for (int slot = 0; slot < 4; slot++) {
            MachineScreenStyle.drawSlot(graphics, x + 54 + slot * 18, y + 39);
        }
        MachineScreenStyle.drawHorizontalBar(graphics, x + 25, y + 62, 134, 5,
                menu.energy(), menu.energyCapacity(), MachineScreenStyle.CYAN);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "UPGRADE BUS", 25, 29, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.energy() + " / " + menu.energyCapacity() + " FE",
                25, 49, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "DRAW " + menu.powerUsed() + " / " + menu.requiredPower() + " FE/t",
                20, 78, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, compactStatus(), 20, 87, statusColor(), false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, MachineScreenStyle.MUTED, false);

        switch (page) {
            case 1 -> renderBeam(graphics);
            case 2 -> renderUpgrades(graphics);
            default -> renderHome(graphics);
        }
    }

    private void renderHome(GuiGraphics graphics) {
        graphics.drawString(font, "STABILIZER STATUS", 207, 52, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, menu.isPowered() ? "POWER LINK: ONLINE" : "POWER LINK: OFFLINE", 193, 67,
                menu.isPowered() ? MachineScreenStyle.GREEN : MachineScreenStyle.DANGER, false);
        graphics.drawString(font, "Redstone: " + redstoneLabel()
                        + (menu.redstoneAllowsOperation() ? " / ALLOW" : " / PAUSE"),
                193, 80, menu.redstoneAllowsOperation() ? MachineScreenStyle.CYAN : MachineScreenStyle.DANGER, false);
        graphics.drawString(font, menu.anomalyDistance() >= 0
                        ? "Anomaly lock: " + menu.anomalyDistance() + " blocks"
                        : "Anomaly lock: NONE",
                193, 93, menu.anomalyDistance() >= 0 ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
    }

    private void renderBeam(GuiGraphics graphics) {
        graphics.drawString(font, "BEAM TELEMETRY", 210, 52, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, menu.anomalyDistance() >= 0
                        ? "LOCKED // " + menu.anomalyDistance() + " blocks"
                        : "LOCK // NONE",
                193, 67, menu.anomalyDistance() >= 0 ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.isBeamBlocked()
                        ? "PATH // BLOCKED @ " + menu.beamBlockedDistance()
                        : "PATH // CLEAR",
                193, 80, menu.isBeamBlocked() ? MachineScreenStyle.DANGER : MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "POWER // " + menu.powerUsed() + " / " + menu.requiredPower() + " FE/t",
                193, 93, menu.isPowered() ? MachineScreenStyle.TEXT : MachineScreenStyle.AMBER, false);
        graphics.drawString(font, "A clear beam + FE + allowed RS is required.", 193, 109, MachineScreenStyle.MUTED, false);
    }

    private void renderUpgrades(GuiGraphics graphics) {
        graphics.drawString(font, "UPGRADES", 225, 52, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "4 physical slots remain active", 193, 68, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Required draw: " + menu.requiredPower() + " FE/t", 193, 82, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Use range/power upgrades to tune", 193, 98, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "beam reach and reactor-side support.", 193, 111, MachineScreenStyle.MUTED, false);
    }

    private String compactStatus() {
        if (!menu.redstoneAllowsOperation()) return "PAUSED: REDSTONE " + redstoneLabel();
        if (!menu.isPowered()) return "WAITING: REACTOR POWER";
        if (menu.isBeamBlocked()) return "BLOCKED @ " + menu.beamBlockedDistance() + " BLOCKS";
        if (menu.anomalyDistance() >= 0) return "STABILIZING @ " + menu.anomalyDistance() + " BLOCKS";
        return "READY: NO ANOMALY IN BEAM";
    }

    private int statusColor() {
        if (!menu.redstoneAllowsOperation() || !menu.isPowered() || menu.isBeamBlocked()) return MachineScreenStyle.DANGER;
        return menu.anomalyDistance() >= 0 ? MachineScreenStyle.GREEN : MachineScreenStyle.AMBER;
    }

    private String redstoneLabel() {
        return switch (menu.redstoneMode()) {
            case 1 -> "HIGH";
            case 2 -> "LOW";
            default -> "IGNORED";
        };
    }
}
