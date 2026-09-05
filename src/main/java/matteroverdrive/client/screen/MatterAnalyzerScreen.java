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
        imageWidth = 176;
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
            }).bounds(leftPos + 5 + i * 42, topPos + 5, 40, 15).build();
            tab.active = page != i;
            addRenderableWidget(tab);
        }
        if (page == 0) {
            addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> clickMenu(1))
                    .bounds(leftPos + 117, topPos + 78, 52, 16).build());
        } else if (page == 2) {
            addRenderableWidget(Button.builder(Component.literal("CYCLE RS"), button -> clickMenu(2))
                    .bounds(leftPos + 55, topPos + 59, 66, 18).build());
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

        if (page == 0) {
            graphics.blit(SCAN, x + 30, y + 28, 0, 0, 117, 47, 117, 47);
            drawWave(graphics, x + 37, y + 36, menu.getProgress(), menu.getMaxProgress(), menu.getInputMatter());
            MachineScreenStyle.drawSlot(graphics, x + 25, y + 43);
            MachineScreenStyle.drawSlot(graphics, x + 79, y + 43);
            MachineScreenStyle.drawSlot(graphics, x + 133, y + 43);
            MachineScreenStyle.drawVerticalBar(graphics, x + 8, y + 30, 7, 40,
                    menu.getEnergy(), menu.getEnergyCapacity(), MachineScreenStyle.RED);
        } else if (page == 3) {
            for (int slot = 0; slot < 4; slot++) {
                MachineScreenStyle.drawSlot(graphics, x + 52 + slot * 18, y + 52);
            }
        }
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
        graphics.drawString(font, menu.getEnergy() + " / " + menu.getEnergyCapacity() + " FE", 18, 29, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Pattern " + menu.getPatternProgress() + "%", 93, 76, MachineScreenStyle.PURPLE, false);
        if (menu.getInputMatter() > 0) {
            graphics.drawString(font, menu.getInputMatter() + " kM | " + menu.getEnergyPerTick() + " FE/t",
                    18, 87, MachineScreenStyle.TEXT, false);
        }
    }

    private void renderTasks(GuiGraphics graphics) {
        int progress = menu.getMaxProgress() <= 0 ? 0 : Math.min(100, menu.getProgress() * 100 / menu.getMaxProgress());
        graphics.drawString(font, "ANALYSIS TASK", 49, 34, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, menu.getInputMatter() > 0 ? "Active material: " + menu.getInputMatter() + " kM" : "No material queued",
                25, 49, menu.getInputMatter() > 0 ? MachineScreenStyle.TEXT : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Scan progress: " + progress + "%", 25, 61, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Pattern learned: " + menu.getPatternProgress() + "%", 25, 73, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Demand: " + menu.getEnergyPerTick() + " FE/t", 25, 85, MachineScreenStyle.MUTED, false);
    }

    private void renderConfig(GuiGraphics graphics) {
        String redstone = switch (menu.getRedstoneMode()) {
            case 0 -> "LOW";
            case 1 -> "HIGH";
            default -> "NONE";
        };
        graphics.drawString(font, "MACHINE CONFIGURATION", 28, 34, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Redstone mode: " + redstone, 35, 48, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Cycle how this analyzer responds", 22, 81, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "to a redstone signal.", 39, 91, MachineScreenStyle.MUTED, false);
    }

    private void renderUpgrades(GuiGraphics graphics) {
        graphics.drawString(font, "MACHINE UPGRADES", 43, 34, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, "4 installed upgrade slots", 29, 74, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Speed / efficiency upgrades affect", 20, 85, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "the live analyzer backend.", 31, 94, MachineScreenStyle.MUTED, false);
    }
}
