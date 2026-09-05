package matteroverdrive.client.screen;

import matteroverdrive.menu.FusionReactorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class FusionReactorScreen extends AbstractContainerScreen<FusionReactorMenu> {
    private static final int SLOT_X_OFFSET = 92;

    public FusionReactorScreen(FusionReactorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 360;
        imageHeight = 286;
        inventoryLabelY = 189;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            }
        }).bounds(leftPos + imageWidth - 57, topPos + 5, 52, 16).build());
        addRenderableWidget(Button.builder(Component.literal("RUN / SCRAM"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 2);
            }
        }).bounds(leftPos + 151, topPos + 5, 74, 16).build());
        addRenderableWidget(Button.builder(Component.literal("RS MODE"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 3);
            }
        }).bounds(leftPos + 227, topPos + 5, 70, 16).build());
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

        MachineScreenStyle.drawSection(graphics, x + 17, y + 29, 326, 43);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 76, 326, 75);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 155, 326, 29);

        MachineScreenStyle.drawHorizontalBar(graphics, x + 18, y + 43, 110, 5,
                menu.energy(), menu.capacity(), MachineScreenStyle.RED);
        MachineScreenStyle.drawHorizontalBar(graphics, x + 232, y + 43, 110, 5,
                menu.matter(), menu.matterCapacity(), MachineScreenStyle.BLUE);

        for (int slot = 0; slot < 4; slot++) {
            MachineScreenStyle.drawSlot(graphics,
                    x + SLOT_X_OFFSET + 51 + slot * 18,
                    y + 51);
        }

        LegacyGuiPrimitives.drawDualRing(graphics, x + 180, y + 112, 31,
                menu.energy(), menu.capacity(), menu.matter(), menu.matterCapacity());
        LegacyGuiPrimitives.drawStatusLamp(graphics, x + 28, y + 88,
                menu.valid(), MachineScreenStyle.GREEN);
        LegacyGuiPrimitives.drawStatusLamp(graphics, x + 28, y + 101,
                menu.reactorEnabled(), MachineScreenStyle.PURPLE);
        LegacyGuiPrimitives.drawStatusLamp(graphics, x + 28, y + 114,
                menu.redstoneAllowsOperation(), MachineScreenStyle.CYAN);
        LegacyGuiPrimitives.drawStatusLamp(graphics, x + 28, y + 127,
                menu.stabilizerCount() > 0, MachineScreenStyle.BLUE);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Energy " + menu.energy() + " / " + menu.capacity() + " FE",
                18, 31, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Matter " + menu.matter() + " / " + menu.matterCapacity() + " kM",
                232, 31, MachineScreenStyle.MUTED, false);

        graphics.drawString(font, "STRUCTURE", 40, 85,
                menu.valid() ? MachineScreenStyle.GREEN : MachineScreenStyle.DANGER, false);
        graphics.drawString(font, menu.valid() ? "VALID" : faultText(), 40, 94,
                menu.valid() ? MachineScreenStyle.GREEN : MachineScreenStyle.DANGER, false);
        graphics.drawString(font, "REACTOR", 40, 107, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.reactorEnabled() ? "RUNNING" : "SCRAMMED", 40, 116,
                menu.reactorEnabled() ? MachineScreenStyle.GREEN : MachineScreenStyle.DANGER, false);
        graphics.drawString(font, "REDSTONE " + redstoneText(), 40, 129,
                menu.redstoneAllowsOperation() ? MachineScreenStyle.CYAN : MachineScreenStyle.DANGER, false);

        graphics.drawCenteredString(font, Math.round(menu.efficiency() * 100) + "%",
                180, 104, MachineScreenStyle.TEXT);
        graphics.drawCenteredString(font, "EFF", 180, 115, MachineScreenStyle.MUTED);
        graphics.drawCenteredString(font, "+" + menu.generatedLastTick() + " FE/t",
                180, 126, MachineScreenStyle.RED);

        graphics.drawString(font, "OUTPUT", 247, 85, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Potential " + menu.output() + " FE/t", 247, 96, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Demand " + menu.connectedUsage() + " FE/t", 247, 107, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Ring " + menu.internalPowerLastTick() + " FE/t", 247, 118, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Drain " + format(menu.matterDrain()) + " kM/t", 247, 129, MachineScreenStyle.BLUE, false);
        graphics.drawString(font, "IO " + menu.ioCount() + " | Stabilizers " + menu.stabilizerCount(),
                247, 140, MachineScreenStyle.MUTED, false);

        graphics.drawString(font, "ANOMALY  mass " + format(menu.unsuppressedMass())
                        + " | gravity " + format(menu.anomalyRange())
                        + " | horizon " + format(menu.eventHorizon()),
                20, 158, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Suppression " + Math.round((1.0D - menu.anomalySuppression()) * 100)
                        + "% | affected " + menu.affectedEntityCount()
                        + " | inside " + menu.horizonEntityCount()
                        + " | last feed " + menu.lastConsumedMatter() + " kM / " + menu.lastConsumedEntityCount(),
                20, 169, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Hazard range " + format(menu.blockHazardRange())
                        + " | broken " + menu.destroyedBlocksLastCycle()
                        + " | ring " + menu.ringDirection().getName().toUpperCase(Locale.ROOT)
                        + " | anomaly offset " + menu.anomalyDistance(),
                20, 180, MachineScreenStyle.DANGER, false);

        graphics.drawString(font, playerInventoryTitle,
                SLOT_X_OFFSET + 8, inventoryLabelY, MachineScreenStyle.MUTED, false);
    }

    private String faultText() {
        return switch (menu.fault()) {
            case 3 -> "wrong coil/IO";
            case 4 -> "wrong hull";
            case 5 -> "no anomaly";
            case 6 -> "no matter";
            case 7 -> "wrong side";
            case 8 -> "area unloaded";
            case 9 -> "anomaly unavailable";
            case 10 -> "scrammed";
            case 11 -> "waiting redstone";
            case 12 -> "redstone shutdown";
            case 13 -> "energy full";
            default -> "checking";
        };
    }

    private String redstoneText() {
        String mode = switch (menu.redstoneMode()) {
            case 1 -> "HIGH";
            case 2 -> "LOW";
            default -> "IGNORED";
        };
        return mode + (menu.redstoneAllowsOperation() ? " / RUN" : " / PAUSED");
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
