package matteroverdrive.client.screen;

import matteroverdrive.menu.MatterAnalyzerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MatterAnalyzerScreen extends AbstractContainerScreen<MatterAnalyzerMenu> {
    private static final ResourceLocation SCAN = new ResourceLocation("matteroverdrive", "textures/gui/elements/screen.png");
    private static final String[] PAGES = {"HOME", "TASKS", "CONFIG", "UPGRADES"};
    private final float[] bars = new float[26];
    private int page;

    public MatterAnalyzerScreen(MatterAnalyzerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 330;
        imageHeight = 196;
        inventoryLabelY = 103;
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
            }).bounds(leftPos + 180 + i * 36, topPos + 29, 34, 15).build();
            tab.active = page != i;
            addRenderableWidget(tab);
        }
        if (page == 0) {
            addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> clickMenu(1))
                    .bounds(leftPos + 217, topPos + 127, 56, 18).build());
        } else if (page == 2) {
            addRenderableWidget(Button.builder(Component.literal("CYCLE RS"), button -> clickMenu(2))
                    .bounds(leftPos + 214, topPos + 91, 62, 18).build());
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
        int x = leftPos;
        int y = topPos;
        MachineScreenStyle.drawFrame(graphics, x, y, imageWidth, imageHeight, inventoryLabelY, MachineScreenStyle.PURPLE);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 27, 142, 69);
        MachineScreenStyle.drawSection(graphics, x + 176, y + 25, 145, 137);

        graphics.blit(SCAN, x + 30, y + 28, 0, 0, 117, 47, 117, 47);
        drawWave(graphics, x + 37, y + 36, menu.getProgress(), menu.getMaxProgress(), menu.getInputMatter());
        MachineScreenStyle.drawSlot(graphics, x + 25, y + 43);
        MachineScreenStyle.drawSlot(graphics, x + 79, y + 43);
        MachineScreenStyle.drawSlot(graphics, x + 133, y + 43);
        for (int slot = 0; slot < 4; slot++) {
            MachineScreenStyle.drawSlot(graphics, x + 52 + slot * 18, y + 79);
        }
        MachineScreenStyle.drawVerticalBar(graphics, x + 8, y + 30, 7, 40,
                menu.getEnergy(), menu.getEnergyCapacity(), MachineScreenStyle.RED);
    }

    private void drawWave(GuiGraphics graphics, int x, int y, int value, int max, int seed) {
        float progress = max <= 0 ? 0 : Math.min(1F, (float) value / max);
        int active = (int) Math.floor(progress * 26F);
        for (int i = 0; i < 26; i++) {
            float target = 0;
            if (i < active) {
                double noise = Math.sin((seed + 17) * .019 + i * .73) + Math.sin((seed + 3) * .007 + i * 1.91) * .5;
                target = (float) Math.max(.08, Math.min(1, (noise + 1.5) / 3));
            }
            bars[i] += (target - bars[i]) * .12F;
            int height = Math.round(bars[i] * 30);
            if (height > 0) {
                int bx = x + i * 4;
                graphics.fill(bx, y + 31 - height, bx + 2, y + 31, 0xFFBFE4E6);
                graphics.fill(bx, y + 29 - height, bx + 2, y + 30 - height, 0xFF73E8FF);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, menu.getEnergy() + " / " + menu.getEnergyCapacity() + " FE", 18, 29, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Pattern " + menu.getPatternProgress() + "%", 93, 76, MachineScreenStyle.PURPLE, false);
        if (menu.getInputMatter() > 0) {
            graphics.drawString(font, menu.getInputMatter() + " kM | " + menu.getEnergyPerTick() + " FE/t",
                    18, 87, MachineScreenStyle.TEXT, false);
        }

        switch (page) {
            case 1 -> renderTasks(graphics);
            case 2 -> renderConfig(graphics);
            case 3 -> renderUpgrades(graphics);
            default -> renderHome(graphics);
        }
    }

    private void renderHome(GuiGraphics graphics) {
        graphics.drawString(font, "ANALYZER STATUS", 205, 52, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, menu.getInputMatter() > 0 ? "Material loaded" : "Waiting for material", 194, 70,
                menu.getInputMatter() > 0 ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Pattern knowledge " + menu.getPatternProgress() + "%", 190, 84, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Demand " + menu.getEnergyPerTick() + " FE/t", 190, 98, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Use Tasks for live scan details.", 186, 116, MachineScreenStyle.MUTED, false);
    }

    private void renderTasks(GuiGraphics graphics) {
        int progress = menu.getMaxProgress() <= 0 ? 0 : Math.min(100, menu.getProgress() * 100 / menu.getMaxProgress());
        graphics.drawString(font, "ANALYSIS TASK", 207, 52, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, menu.getInputMatter() > 0 ? "Active: " + menu.getInputMatter() + " kM" : "No material queued",
                190, 70, menu.getInputMatter() > 0 ? MachineScreenStyle.TEXT : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Scan " + progress + "%", 190, 84, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Pattern " + menu.getPatternProgress() + "%", 190, 98, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Demand " + menu.getEnergyPerTick() + " FE/t", 190, 112, MachineScreenStyle.MUTED, false);
    }

    private void renderConfig(GuiGraphics graphics) {
        String redstone = switch (menu.getRedstoneMode()) {
            case 0 -> "LOW";
            case 1 -> "HIGH";
            default -> "NONE";
        };
        graphics.drawString(font, "CONFIGURATION", 208, 52, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Redstone: " + redstone, 190, 72, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Controls when analysis", 190, 116, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "is allowed to run.", 190, 128, MachineScreenStyle.MUTED, false);
    }

    private void renderUpgrades(GuiGraphics graphics) {
        graphics.drawString(font, "UPGRADES", 218, 52, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, "4 physical slots shown left", 190, 72, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Installed speed/efficiency", 190, 90, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "effects are applied live.", 190, 102, MachineScreenStyle.MUTED, false);
    }
}
