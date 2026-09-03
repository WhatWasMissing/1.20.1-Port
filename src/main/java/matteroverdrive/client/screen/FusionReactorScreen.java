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
        MachineScreenStyle.drawSection(graphics, x + 17, y + 29, 326, 44);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 77, 326, 60);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 141, 326, 43);

        MachineScreenStyle.drawHorizontalBar(graphics, x + 18, y + 43, 110, 5,
                menu.energy(), menu.capacity(), MachineScreenStyle.RED);
        MachineScreenStyle.drawHorizontalBar(graphics, x + 232, y + 43, 110, 5,
                menu.matter(), menu.matterCapacity(), MachineScreenStyle.BLUE);
        for (int slot = 0; slot < 4; slot++) {
            MachineScreenStyle.drawSlot(graphics,
                    x + SLOT_X_OFFSET + 51 + slot * 18,
                    y + 51);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Energy " + menu.energy() + " / " + menu.capacity() + " FE",
                18, 31, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Matter " + menu.matter() + " / " + menu.matterCapacity() + " kM",
                232, 31, MachineScreenStyle.MUTED, false);

        graphics.drawString(font, "Structure " + (menu.valid() ? "VALID" : faultText())
                        + " | ring " + menu.ringDirection().getName().toUpperCase(Locale.ROOT),
                20, 80, menu.valid() ? MachineScreenStyle.GREEN : MachineScreenStyle.DANGER, false);
        graphics.drawString(font, "Reactor " + (menu.reactorEnabled() ? "ENABLED" : "SCRAMMED")
                        + " | redstone " + redstoneText(),
                20, 91, menu.reactorEnabled() && menu.redstoneAllowsOperation()
                        ? MachineScreenStyle.GREEN : MachineScreenStyle.DANGER, false);
        graphics.drawString(font, "Potential " + menu.output()
                        + " FE/t | generated " + menu.generatedLastTick()
                        + " FE/t | used " + menu.connectedUsage(),
                20, 102, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Efficiency " + Math.round(menu.efficiency() * 100)
                        + "% | drain " + format(menu.matterDrain())
                        + " kM/t | consumed " + menu.matterConsumedLastTick(),
                20, 113, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Anomaly offset " + menu.anomalyDistance()
                        + " | mass " + format(menu.unsuppressedMass())
                        + " | IO " + menu.ioCount() + " | stabilizers " + menu.stabilizerCount(),
                20, 124, MachineScreenStyle.MUTED, false);

        graphics.drawString(font, "Gravity " + format(menu.anomalyRange())
                        + " blocks | suppression " + Math.round((1.0D - menu.anomalySuppression()) * 100)
                        + "% | affected " + menu.affectedEntityCount(),
                20, 144, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Horizon " + format(menu.eventHorizon())
                        + " | inside " + menu.horizonEntityCount()
                        + " | last feed " + menu.lastConsumedMatter() + " kM / "
                        + menu.lastConsumedEntityCount(),
                20, 155, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Ring power " + menu.internalMachineCount()
                        + " machines | sent " + menu.internalPowerLastTick() + " FE/t",
                20, 166, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Block/fluid hazard ACTIVE | range "
                        + format(menu.blockHazardRange()) + " | broken "
                        + menu.destroyedBlocksLastCycle(),
                20, 177, MachineScreenStyle.DANGER, false);
        graphics.drawString(font, playerInventoryTitle,
                SLOT_X_OFFSET + 8, inventoryLabelY, MachineScreenStyle.MUTED, false);
    }

    private String faultText() {
        return switch (menu.fault()) {
            case 3 -> "wrong coil/IO position";
            case 4 -> "wrong hull position";
            case 5 -> "no anomaly at ring centre";
            case 6 -> "no matter";
            case 7 -> "wrong controller-side position";
            case 8 -> "structure area unloaded";
            case 9 -> "anomaly data unavailable";
            case 10 -> "scrammed";
            case 11 -> "waiting for redstone";
            case 12 -> "redstone shutdown";
            case 13 -> "energy storage full";
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
        return String.format(Locale.ROOT, "%.4f", value);
    }
}
