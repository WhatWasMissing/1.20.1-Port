package matteroverdrive.client.screen;

import matteroverdrive.menu.DroneFabricatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Clear live status for the new fabrication loop; all production controls are server-side menu actions. */
public final class DroneFabricatorScreen extends AbstractContainerScreen<DroneFabricatorMenu> {
    public DroneFabricatorScreen(DroneFabricatorMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); imageWidth = 350; imageHeight = 213; inventoryLabelY = 101; }
    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("ROLE"), button -> click(1)).bounds(leftPos + 212, topPos + 39, 52, 16).build());
        addRenderableWidget(Button.builder(Component.literal("REDSTONE"), button -> click(2)).bounds(leftPos + 270, topPos + 39, 70, 16).build());
        addRenderableWidget(Button.builder(Component.literal("CHANNEL"), button -> click(3)).bounds(leftPos + 181, topPos + 39, 82, 16).build());
    }
    private void click(int id) { if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id); }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { renderBackground(graphics); super.render(graphics, mouseX, mouseY, partialTick); renderTooltip(graphics, mouseX, mouseY); }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        MachineScreenStyle.drawFrame(graphics, x, y, imageWidth, imageHeight, inventoryLabelY, MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics, x + 17, y + 27, 151, 64);
        MachineScreenStyle.drawSection(graphics, x + 181, y + 25, 159, 116);
        MachineScreenStyle.drawVerticalBar(graphics, x + 8, y + 31, 7, 44, menu.energy(), menu.capacity(), MachineScreenStyle.BLUE);
        for (int slotX : new int[]{27, 54, 81, 135}) MachineScreenStyle.drawSlot(graphics, x + slotX - 1, y + 41);
        for (int slotX : new int[]{108, 126}) MachineScreenStyle.drawSlot(graphics, x + slotX - 1, y + 57);
        int width = menu.maxProgress() <= 0 ? 0 : Math.min(130, 130 * menu.progress() / menu.maxProgress());
        graphics.fill(x + 194, y + 110, x + 324, y + 116, 0xff17242d);
        graphics.fill(x + 194, y + 110, x + 194 + width, y + 116, menu.running() ? MachineScreenStyle.CYAN : MachineScreenStyle.MUTED);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "INPUTS", 27, 26, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "CORE", 132, 26, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "DRONE ASSEMBLY GANTRY", 204, 29, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Role: " + DroneFabricatorMenu.roleName(menu.role()), 140), 194, 64, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "FE: " + menu.energy() + " / " + menu.capacity(), 140), 194, 77, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, menu.running() ? "ASSEMBLING " + menu.progress() + "/" + menu.maxProgress() : "AWAITING POWER / INPUTS", 140), 194, 91, menu.running() ? MachineScreenStyle.GREEN : MachineScreenStyle.AMBER, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "Completed: " + menu.completedCores(), 140), 194, 128, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "RS: " + menu.redstoneModeLabel(), 140), 194, 141, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "ITEM CH: " + menu.networkChannel(), 194, 153, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, MachineScreenStyle.fit(font, "AUTO-FEED: " + menu.plasmaCount() + "/1  " + menu.circuitCount() + "/2  " + menu.plateCount() + "/4", 150), 27, 70,
                menu.recipeReady() ? MachineScreenStyle.GREEN : MachineScreenStyle.AMBER, false);
        graphics.drawString(font, "UPG", 108, 50, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "1 plasma core · 2 circuits · 4 plates", 25, 84, MachineScreenStyle.MUTED, false);
    }
}
