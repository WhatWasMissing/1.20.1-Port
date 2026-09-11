package matteroverdrive.client.screen;

import matteroverdrive.blockentity.FusionReactorControllerBlockEntity;
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
        imageWidth = 376;
        imageHeight = 296;
        inventoryLabelY = 199;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> click(1))
                .bounds(leftPos + imageWidth - 57, topPos + 5, 52, 16).build());
        addRenderableWidget(Button.builder(Component.literal("RUN / SCRAM"), button -> click(2))
                .bounds(leftPos + 157, topPos + 5, 78, 16).build());
        addRenderableWidget(Button.builder(Component.literal("RS MODE"), button -> click(3))
                .bounds(leftPos + 237, topPos + 5, 70, 16).build());
        addRenderableWidget(Button.builder(Component.literal("PROFILE"), button -> click(4))
                .bounds(leftPos + 17, topPos + 5, 70, 16).build());
    }

    private void click(int id) {
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
                MachineScreenStyle.PURPLE);

        MachineScreenStyle.drawSection(graphics, x + 17, y + 29, 342, 43);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 76, 342, 79);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 159, 342, 35);

        MachineScreenStyle.drawHorizontalBar(graphics, x + 18, y + 43, 118, 5,
                menu.energy(), menu.capacity(), MachineScreenStyle.RED);
        MachineScreenStyle.drawHorizontalBar(graphics, x + 240, y + 43, 118, 5,
                menu.matter(), menu.matterCapacity(), MachineScreenStyle.BLUE);
        MachineScreenStyle.drawHorizontalBar(graphics, x + 18, y + 67, 118, 4,
                menu.heat(), FusionReactorControllerBlockEntity.MAX_HEAT, MachineScreenStyle.DANGER);
        MachineScreenStyle.drawHorizontalBar(graphics, x + 240, y + 67, 118, 4,
                menu.stability(), FusionReactorControllerBlockEntity.MAX_STABILITY, MachineScreenStyle.GREEN);

        for (int slot = 0; slot < 4; slot++) {
            MachineScreenStyle.drawSlot(graphics,
                    x + SLOT_X_OFFSET + 59 + slot * 18,
                    y + 51);
        }

        LegacyGuiPrimitives.drawDualRing(graphics, x + 188, y + 114, 32,
                menu.energy(), menu.capacity(), menu.matter(), menu.matterCapacity());
        LegacyGuiPrimitives.drawStatusLamp(graphics, x + 28, y + 89,
                menu.valid(), MachineScreenStyle.GREEN);
        LegacyGuiPrimitives.drawStatusLamp(graphics, x + 28, y + 103,
                menu.reactorEnabled(), MachineScreenStyle.PURPLE);
        LegacyGuiPrimitives.drawStatusLamp(graphics, x + 28, y + 117,
                menu.redstoneAllowsOperation(), MachineScreenStyle.CYAN);
        LegacyGuiPrimitives.drawStatusLamp(graphics, x + 28, y + 131,
                menu.stabilizerCount() > 0, MachineScreenStyle.BLUE);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "BUFFER " + menu.energy() + " / " + menu.capacity() + " FE", 145),
                18, 31, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "MATTER " + menu.matter() + " / " + menu.matterCapacity() + " kM", 130),
                240, 31, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "HEAT " + menu.heat() + "/1000", 18, 57,
                menu.heat() > 800 ? MachineScreenStyle.DANGER : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "STABILITY " + menu.stability() + "/1000", 240, 57,
                menu.stability() < 250 ? MachineScreenStyle.DANGER : MachineScreenStyle.MUTED, false);

        graphics.drawString(font, "STRUCTURE", 40, 85,
                menu.valid() ? MachineScreenStyle.GREEN : MachineScreenStyle.DANGER, false);
        graphics.drawString(font, menu.valid() ? "VALID" : faultText(), 40, 95,
                menu.valid() ? MachineScreenStyle.GREEN : MachineScreenStyle.DANGER, false);
        graphics.drawString(font, "REACTOR", 40, 109, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.reactorEnabled() ? "RUN ENABLED" : "SCRAMMED", 40, 119,
                menu.reactorEnabled() ? MachineScreenStyle.GREEN : MachineScreenStyle.DANGER, false);
        graphics.drawString(font, "REDSTONE", 40, 133, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, redstoneText(), 40, 143,
                menu.redstoneAllowsOperation() ? MachineScreenStyle.CYAN : MachineScreenStyle.DANGER, false);

        graphics.drawCenteredString(font, Math.round(menu.efficiency() * 100) + "%",
                188, 104, MachineScreenStyle.TEXT);
        graphics.drawCenteredString(font, "EFFICIENCY", 188, 116, MachineScreenStyle.MUTED);
        graphics.drawCenteredString(font, MachineScreenStyle.fit(font, "+" + menu.generatedLastTick() + " FE/t", 72),
                188, 129, menu.generatedLastTick() > 0 ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED);
        graphics.drawCenteredString(font, "ACTUAL", 188, 141, MachineScreenStyle.MUTED);

        graphics.drawString(font, "OUTPUT BUS", 262, 85, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "PROFILE", 262, 150, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.operatingModeLabel(), 312, 150,
                menu.operatingMode() == 1 ? MachineScreenStyle.DANGER
                        : menu.operatingMode() == 2 ? MachineScreenStyle.BLUE : MachineScreenStyle.GREEN, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Potential  " + menu.output() + " FE/t", 100), 252, 98, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Generated  " + menu.generatedLastTick() + " FE/t", 100), 252, 111,
                menu.generatedLastTick() > 0 ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Demand     " + menu.connectedUsage() + " FE/t", 100), 252, 124, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Ring bus   " + menu.internalPowerLastTick() + " FE/t", 100), 252, 137, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "IO " + menu.ioCount() + " / Stabilizers " + menu.stabilizerCount(), 100),
                252, 148, MachineScreenStyle.MUTED, false);

        graphics.drawString(font, fit("ANOMALY // raw mass " + format(menu.unsuppressedMass())
                        + " // gravity " + format(menu.anomalyRange())
                        + " // horizon " + format(menu.eventHorizon()), 330),
                20, 162, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, fit("SUPPRESSION " + Math.round((1.0D - menu.anomalySuppression()) * 100)
                        + "% // affected " + menu.affectedEntityCount()
                        + " // inside horizon " + menu.horizonEntityCount(), 330),
                20, 173, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, fit("LAST FEED " + menu.lastConsumedMatter() + " kM / " + menu.lastConsumedEntityCount()
                        + " entities // blocks broken " + menu.destroyedBlocksLastCycle(), 330),
                20, 184, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, fit("HAZARD " + format(menu.blockHazardRange())
                        + " blocks // ring " + menu.ringDirection().getName().toUpperCase(Locale.ROOT)
                        + " // anomaly offset " + menu.anomalyDistance()
                        + " // matter drain " + format(menu.matterDrain()) + " kM/t", 330),
                20, 195, MachineScreenStyle.DANGER, false);

        graphics.drawString(font, playerInventoryTitle,
                SLOT_X_OFFSET + 8, inventoryLabelY, MachineScreenStyle.MUTED, false);
    }

    private String faultText() {
        return switch (menu.fault()) {
            case 3 -> "wrong coil / IO";
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
            case 14 -> "THERMAL RUNAWAY — SCRAMMED";
            case 15 -> "CONTAINMENT FAILURE — SCRAMMED";
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

    private String fit(String value, int maxWidth) {
        if (font.width(value) <= maxWidth) return value;
        int usable = Math.max(0, maxWidth - font.width("…"));
        return font.plainSubstrByWidth(value, usable) + "…";
    }
}
