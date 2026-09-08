package matteroverdrive.client.screen;

import matteroverdrive.menu.AndroidSpawnerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class AndroidSpawnerScreen extends AbstractContainerScreen<AndroidSpawnerMenu> {
    public AndroidSpawnerScreen(AndroidSpawnerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 300;
        imageHeight = 214;
        inventoryLabelY = 95;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("SQUAD COLOR"), button -> click(2))
                .bounds(leftPos + 174, topPos + 48, 104, 18).build());
        addRenderableWidget(Button.builder(Component.literal("CYCLE MODE"), button -> click(3))
                .bounds(leftPos + 174, topPos + 68, 104, 18).build());
        addRenderableWidget(Button.builder(Component.literal("SET COMMANDER"), button -> click(4))
                .bounds(leftPos + 174, topPos + 88, 104, 18).build());
        addRenderableWidget(Button.builder(Component.literal("DISMISS SQUAD"), button -> click(1))
                .bounds(leftPos + 174, topPos + 108, 104, 18).build());
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
        MachineScreenStyle.drawFrame(graphics, leftPos, topPos, imageWidth, imageHeight,
                inventoryLabelY, MachineScreenStyle.PURPLE);
        MachineScreenStyle.drawSection(graphics, leftPos + 17, topPos + 27, 142, 66);
        MachineScreenStyle.drawSection(graphics, leftPos + 166, topPos + 27, 121, 108);
        MachineScreenStyle.drawHorizontalBar(graphics, leftPos + 24, topPos + 39, 126, 5,
                menu.energy(), menu.capacity(), MachineScreenStyle.PURPLE);
        for (int slot = 0; slot < 6; slot++) {
            MachineScreenStyle.drawSlot(graphics, leftPos + 34 + slot * 18, topPos + 72);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, menu.energy() + " / " + menu.capacity() + " FE",
                24, 29, MachineScreenStyle.MUTED, false);

        int spawned = menu.spawned();
        int cap = menu.maxSpawned();
        graphics.drawString(font, "SQUAD " + spawned + " / " + cap, 24, 50,
                spawned >= cap ? MachineScreenStyle.AMBER : MachineScreenStyle.TEXT, false);
        String next = spawned >= cap ? "CAP REACHED"
                : String.format(Locale.ROOT, "NEXT UNIT %.1fs", menu.ticksUntilSpawn() / 20.0D);
        graphics.drawString(font, next, 24, 60,
                spawned >= cap ? MachineScreenStyle.AMBER : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "PATROL DRIVES " + menu.patrolTargets() + " / 6", 24, 70,
                menu.patrolTargets() > 0 ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);

        graphics.drawString(font, "SQUAD CONTROL", 174, 31, MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, "Color: " + menu.squadColorName(), 174, 39, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Mode: " + menu.squadModeName(), 229, 39, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Spawn mix: 30% melee / 70% ranged", 174, 129, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "ESCORT uses stable formation slots", 174, 140, MachineScreenStyle.GREEN, false);
        graphics.drawString(font, "PATROL uses the six drive targets", 174, 151, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "HOLD stops movement; GUARD returns home", 174, 162, MachineScreenStyle.MUTED, false);

        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, MachineScreenStyle.MUTED, false);
    }
}
