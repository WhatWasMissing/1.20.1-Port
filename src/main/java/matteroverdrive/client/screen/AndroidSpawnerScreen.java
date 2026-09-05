package matteroverdrive.client.screen;

import matteroverdrive.menu.AndroidSpawnerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AndroidSpawnerScreen extends AbstractContainerScreen<AndroidSpawnerMenu> {
    public AndroidSpawnerScreen(AndroidSpawnerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 260;
        imageHeight = 207;
        inventoryLabelY = 88;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("KILL OWNED"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            }
        }).bounds(leftPos + 179, topPos + 68, 69, 18).build());
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
        MachineScreenStyle.drawSection(graphics, leftPos + 17, topPos + 27, 142, 59);
        MachineScreenStyle.drawSection(graphics, leftPos + 171, topPos + 27, 77, 59);
        MachineScreenStyle.drawHorizontalBar(graphics, leftPos + 24, topPos + 39, 126, 5,
                menu.energy(), menu.capacity(), MachineScreenStyle.PURPLE);
        for (int slot = 0; slot < 6; slot++) {
            MachineScreenStyle.drawSlot(graphics, leftPos + 34 + slot * 18, topPos + 68);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, menu.energy() + " / " + menu.capacity() + " FE",
                24, 29, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Spawned: " + menu.spawned() + " / " + menu.maxSpawned(),
                24, 50, menu.spawned() >= menu.maxSpawned()
                        ? MachineScreenStyle.AMBER : MachineScreenStyle.TEXT, false);
        String next = menu.spawned() >= menu.maxSpawned()
                ? "Population full"
                : String.format(java.util.Locale.ROOT, "Next: %.1f s", menu.ticksUntilSpawn() / 20.0D);
        graphics.drawString(font, next, 24, 59, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Targets", 181, 33, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, Integer.toString(menu.patrolTargets()), 181, 44,
                menu.patrolTargets() > 0 ? MachineScreenStyle.GREEN : MachineScreenStyle.AMBER, false);
        graphics.drawString(font, "Mix 30/70", 181, 55, MachineScreenStyle.CYAN, false);
    }
}
