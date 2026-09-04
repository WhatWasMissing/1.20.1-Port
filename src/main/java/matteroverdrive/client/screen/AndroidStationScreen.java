package matteroverdrive.client.screen;

import matteroverdrive.android.AndroidData;
import matteroverdrive.menu.AndroidStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AndroidStationScreen extends AbstractContainerScreen<AndroidStationMenu> {
    public AndroidStationScreen(AndroidStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176; imageHeight = 232; inventoryLabelY = 120;
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics); super.render(graphics, mouseX, mouseY, partialTick); renderTooltip(graphics, mouseX, mouseY);
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineScreenStyle.drawFrame(graphics, leftPos, topPos, imageWidth, imageHeight, inventoryLabelY, MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics, leftPos + 17, topPos + 29, 142, 85);
        MachineScreenStyle.drawHorizontalBar(graphics, leftPos + 25, topPos + 56, 126, 5, menu.androidEnergy(), menu.androidCapacity(), MachineScreenStyle.CYAN);
        MachineScreenStyle.drawDebugPanel(graphics, leftPos + 17, topPos + 116, 142, 13);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        if (!menu.androidActive()) graphics.drawString(font, "Use a blue Android Pill to convert", 21, 34, MachineScreenStyle.MUTED, false);
        else {
            graphics.drawString(font, "ANDROID ONLINE", 52, 34, MachineScreenStyle.CYAN, false);
            graphics.drawString(font, menu.androidEnergy() + " / " + menu.androidCapacity() + " FE", 34, 45, MachineScreenStyle.TEXT, false);
            String parts = installed(AndroidData.Part.HEAD, "HEAD") + "  " + installed(AndroidData.Part.CHEST, "CHEST") + "  "
                    + installed(AndroidData.Part.ARMS, "ARMS") + "  " + installed(AndroidData.Part.LEGS, "LEGS");
            graphics.drawString(font, parts, 19, 67, MachineScreenStyle.MUTED, false);
        }
        graphics.drawString(font, "Lv " + menu.androidLevel() + "  XP " + menu.experienceIntoLevel() + "/" + menu.experienceToNextLevel(), 22, 86, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Perk choices: " + menu.availableSkillPoints(), 22, 98, MachineScreenStyle.CYAN, false);
        AndroidData.Ability[] abilities = AndroidData.Ability.values();
        int selected = Math.max(0, Math.min(menu.selectedAbilityOrdinal(), abilities.length - 1));
        graphics.drawString(font, "Selected: " + abilities[selected].displayName, 22, 108, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Station " + menu.stationEnergy() + " FE", 20, 118, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, menu.lastTransfer() + " FE/t", 111, 118, MachineScreenStyle.DEBUG, false);
        graphics.drawString(font, "Hold a bionic part and right-click to install", 16, 76, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, MachineScreenStyle.MUTED, false);
    }
    private String installed(AndroidData.Part part, String name) { return (menu.parts() & part.bit) != 0 ? name : "--"; }
}