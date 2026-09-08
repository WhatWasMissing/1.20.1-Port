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
        graphics.drawString(font, "BUFFER " + menu.energy() + " / " + menu.capacity() + " FE",
                18, 31, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "MATTER " + menu.matter() + " / " + menu.matterCapacity() + " kM",
                240, 31, MachineScreenStyle.MUTED, false);

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
        graphics.drawCenteredString(font, "+" + menu.generatedLastTick() + " FE/t",
                188, 129, menu.generatedLastTick() > 0 ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED);
        graphics.drawCenteredString(font, "ACTUAL", 188, 141, MachineScreenStyle.MUTED);

        graphics.drawString(font, "OUTPUT BUS", 262, 85, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Potential  " + menu.output() + " FE/t", 252, 98, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Generated  " + menu.generatedLastTick() + " FE/t", 252, 111,
                menu.generatedLastTick() > 0 ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Demand     " + menu.connectedUsage() + " FE/t", 252, 124, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Ring bus   " + menu.internalPowerLastTick() + " FE/t", 252, 137, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "IO " + menu.ioCount() + " / Stabilizers " + menu.stabilizerCount(),
                252, 148, MachineScreenStyle.MUTED, false);

        graphics.drawString(font, "ANOMALY // raw mass " + format(menu.unsuppressedMass())
                        + " // gravity " + format(menu.anomalyRange())
                        + " // horizon " + format(menu.eventHorizon()),
                20, 162, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "SUPPRESSION " + Math.round((1.0D - menu.anomalySuppression()) * 100)
                        + "% // affected " + menu.affectedEntityCount()
                        + " // inside horizon " + menu.horizonEntityCount(),
                20, 173, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "LAST FEED " + menu.lastConsumedMatter() + " kM / " + menu.lastConsumedEntityCount()
                        + " entities // blocks broken " + menu.destroyedBlocksLastCycle(),
                20, 184, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "HAZARD " + format(menu.blockHazardRange())
                        + " blocks // ring " + menu.ringDirection().getName().toUpperCase(Locale.ROOT)
                        + " // anomaly offset " + menu.anomalyDistance()
                        + " // matter drain " + format(menu.matterDrain()) + " kM/t",
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
