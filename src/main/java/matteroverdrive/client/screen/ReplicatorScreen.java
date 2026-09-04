package matteroverdrive.client.screen;

import matteroverdrive.menu.ReplicatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ReplicatorScreen extends AbstractContainerScreen<ReplicatorMenu> {
    public ReplicatorScreen(ReplicatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 218;
        inventoryLabelY = 125;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            }
        }).bounds(leftPos + imageWidth - 57, topPos + 5, 52, 16).build());
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
                MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 29, 142, 67);
        MachineScreenStyle.drawDebugPanel(graphics, x + 17, y + 75, 142, 23);

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
        if (menu.getNetworkTaskAmount() > 0) {
            graphics.drawString(font, "Network x" + menu.getNetworkTaskAmount(),
                    93, 9, MachineScreenStyle.CYAN, false);
        }
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.getEnergy() + " / " + menu.getEnergyCapacity() + " FE",
                18, 29, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.getMatter() + " / " + menu.getMatterCapacity() + " kM",
                18, 39, MachineScreenStyle.MUTED, false);
        if (menu.getPatternMatter() > 0) {
            graphics.drawString(font, "Pattern " + menu.getPatternProgress() + "% | "
                            + menu.getPatternMatter() + " kM",
                    18, 58, MachineScreenStyle.TEXT, false);
            graphics.drawString(font, menu.getEnergyPerTick() + " FE/t",
                    18, 67, MachineScreenStyle.CYAN, false);
        }
        String cycle = menu.getMaxProgress() > 0
                ? String.format(java.util.Locale.ROOT, "Cycle %d t | %.2f s",
                        menu.getMaxProgress(), menu.getMaxProgress() / 20.0D)
                : "Cycle inactive";
        String failure = menu.getPatternMatter() > 0
                ? String.format(java.util.Locale.ROOT, "Failure %.4f%%", menu.getFailChance() * 100.0D)
                : "Failure inactive";
        graphics.drawString(font, cycle, 20, 79, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, failure, 20, 89, MachineScreenStyle.DANGER, false);
    }
}
