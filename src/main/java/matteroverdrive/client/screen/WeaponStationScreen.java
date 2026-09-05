package matteroverdrive.client.screen;

import matteroverdrive.item.weapon.WeaponModuleItem;
import matteroverdrive.item.weapon.WeaponSystem;
import matteroverdrive.menu.WeaponStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public class WeaponStationScreen extends AbstractContainerScreen<WeaponStationMenu> {
    private static final int[][] STATION_SLOT_POSITIONS = {
            {26, 36}, {62, 36}, {80, 36}, {98, 36}, {62, 54}, {80, 54}, {98, 54}
    };
    private static final String[] PAGES = {"LOADOUT", "STATS"};
    private int page;

    public WeaponStationScreen(WeaponStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 330;
        imageHeight = 166;
        inventoryLabelY = 74;
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
            }).bounds(leftPos + 185 + i * 64, topPos + 29, 60, 15).build();
            tab.active = page != i;
            addRenderableWidget(tab);
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
                inventoryLabelY, MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics, leftPos + 17, topPos + 27, 142, 44);
        MachineScreenStyle.drawSection(graphics, leftPos + 176, topPos + 25, 145, 132);
        for (int[] position : STATION_SLOT_POSITIONS) {
            MachineScreenStyle.drawSlot(graphics,
                    leftPos + position[0] - 1,
                    topPos + position[1] - 1);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Weapon", 20, 26, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Battery Color Barrel", 59, 26, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Sights  Utility", 59, 67, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                MachineScreenStyle.MUTED, false);
        if (page == 1) renderStats(graphics); else renderLoadout(graphics);
    }

    private void renderLoadout(GuiGraphics graphics) {
        ItemStack weapon = menu.getSlot(0).getItem();
        graphics.drawString(font, "WEAPON LOADOUT", 203, 52, MachineScreenStyle.CYAN, false);
        if (weapon.isEmpty()) {
            graphics.drawString(font, "Insert an energy weapon", 190, 72, MachineScreenStyle.MUTED, false);
            graphics.drawString(font, "Modules are unpacked here", 190, 86, MachineScreenStyle.MUTED, false);
            graphics.drawString(font, "and saved back on removal.", 190, 98, MachineScreenStyle.MUTED, false);
            return;
        }
        graphics.drawString(font, trim(weapon.getHoverName().getString(), 22), 190, 70, MachineScreenStyle.TEXT, false);
        String[] names = {"Battery", "Color", "Barrel", "Sights", "Utility A", "Utility B"};
        for (int i = 0; i < 6; i++) {
            ItemStack module = menu.getSlot(i + 1).getItem();
            int color = module.isEmpty() ? MachineScreenStyle.MUTED : MachineScreenStyle.GREEN;
            graphics.drawString(font, names[i] + ": " + (module.isEmpty() ? "empty" : trim(module.getHoverName().getString(), 14)),
                    190, 86 + i * 11, color, false);
        }
    }

    private void renderStats(GuiGraphics graphics) {
        ItemStack weapon = menu.getSlot(0).getItem();
        graphics.drawString(font, "MODULE EFFECTS", 205, 52, MachineScreenStyle.CYAN, false);
        if (weapon.isEmpty()) {
            graphics.drawString(font, "Insert a weapon to inspect", 190, 72, MachineScreenStyle.MUTED, false);
            return;
        }
        ItemStack snapshot = weapon.copy();
        for (int i = 0; i < 6; i++) {
            WeaponSystem.setModule(snapshot, i, menu.getSlot(i + 1).getItem());
        }
        int modules = 0;
        for (int i = 1; i <= 6; i++) if (menu.getSlot(i).hasItem()) modules++;
        graphics.drawString(font, "Installed modules " + modules + " / 6", 190, 70, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, String.format(Locale.ROOT, "Damage x%.2f", WeaponSystem.damageMultiplier(snapshot)),
                190, 86, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, String.format(Locale.ROOT, "Energy x%.2f", WeaponSystem.energyMultiplier(snapshot)),
                190, 98, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, String.format(Locale.ROOT, "Cooldown x%.2f", WeaponSystem.cooldownMultiplier(snapshot)),
                190, 110, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, String.format(Locale.ROOT, "Range x%.2f", WeaponSystem.rangeMultiplier(snapshot)),
                190, 122, MachineScreenStyle.TEXT, false);
        ItemStack barrel = menu.getSlot(WeaponSystem.BARREL_SLOT + 1).getItem();
        if (barrel.getItem() instanceof WeaponModuleItem module) {
            graphics.drawString(font, "Barrel: " + module.getEffect().name(), 190, 138, MachineScreenStyle.PURPLE, false);
        } else {
            graphics.drawString(font, "Barrel: standard", 190, 138, MachineScreenStyle.MUTED, false);
        }
    }

    private String trim(String value, int max) {
        return value.length() <= max ? value : value.substring(0, Math.max(0, max - 1)) + "…";
    }
}
