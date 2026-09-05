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
        imageWidth = 176;
        imageHeight = 207;
        inventoryLabelY = 94;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("KILL ALL"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            }
        }).bounds(leftPos + 101, topPos + 70, 58, 18).build());
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
        MachineScreenStyle.drawSection(graphics, leftPos + 17, topPos + 27, 142, 61);
        MachineScreenStyle.drawHorizontalBar(graphics, leftPos + 24, topPos + 39, 126, 5,
                menu.energy(), menu.capacity(), MachineScreenStyle.PURPLE);
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
                : String.format(java.util.Locale.ROOT, "Next spawn: %.1f s", menu.ticksUntilSpawn() / 20.0D);
        graphics.drawString(font, next, 24, 61, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "30% melee / 70% ranged", 24, 76, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
    }
}
