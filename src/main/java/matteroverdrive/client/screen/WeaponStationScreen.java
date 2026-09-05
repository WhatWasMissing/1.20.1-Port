package matteroverdrive.client.screen;

import matteroverdrive.item.weapon.EnergyWeaponItem;
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
    private static final String[] PAGES = {"HOME", "MODULES", "STATS"};
    private int page;

    public WeaponStationScreen(WeaponStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 350;
        imageHeight = 176;
        inventoryLabelY = 84;
    }

    @Override protected void init() { super.init(); rebuildButtons(); }

    private void rebuildButtons() {
        clearWidgets();
        for (int i = 0; i < PAGES.length; i++) {
            final int target = i;
            Button tab = Button.builder(Component.literal(PAGES[i]), button -> { page = target; rebuildButtons(); })
                    .bounds(leftPos + 176 + i * 55, topPos + 28, 52, 15).build();
            tab.active = page != i;
            addRenderableWidget(tab);
        }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics); super.render(graphics, mouseX, mouseY, partialTick); renderTooltip(graphics, mouseX, mouseY);
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineScreenStyle.drawFrame(graphics, leftPos, topPos, imageWidth, imageHeight, inventoryLabelY, MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics, leftPos + 17, topPos + 27, 142, 54);
        MachineScreenStyle.drawSection(graphics, leftPos + 170, topPos + 25, 171, 142);
        for (int[] position : STATION_SLOT_POSITIONS) MachineScreenStyle.drawSlot(graphics, leftPos + position[0] - 1, topPos + position[1] - 1);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Weapon", 20, 26, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Battery Color Barrel", 59, 26, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Sights  Utility", 59, 67, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, MachineScreenStyle.MUTED, false);
        switch (page) { case 1 -> renderModules(graphics); case 2 -> renderStats(graphics); default -> renderHome(graphics); }
    }

    private ItemStack snapshot() {
        ItemStack weapon = menu.getSlot(0).getItem();
        if (weapon.isEmpty()) return ItemStack.EMPTY;
        ItemStack copy = weapon.copy();
        for (int i = 0; i < WeaponSystem.MODULE_SLOT_COUNT; i++) WeaponSystem.setModule(copy, i, menu.getSlot(i + 1).getItem());
        return copy;
    }

    private void renderHome(GuiGraphics graphics) {
        ItemStack weapon = snapshot();
        graphics.drawString(font, "WEAPON STATUS", 216, 51, MachineScreenStyle.CYAN, false);
        if (!(weapon.getItem() instanceof EnergyWeaponItem gun)) {
            graphics.drawString(font, "Insert an energy weapon", 185, 72, MachineScreenStyle.MUTED, false);
            graphics.drawString(font, "All seven physical slots", 185, 87, MachineScreenStyle.MUTED, false);
            graphics.drawString(font, "remain accessible on every page.", 185, 99, MachineScreenStyle.MUTED, false);
            return;
        }
        int energy = gun.getEnergyStored(weapon), capacity = gun.getCapacity(weapon);
        int heat = Math.round(gun.getHeat(weapon)), maxHeat = gun.getMaxHeat(weapon);
        graphics.drawString(font, trim(weapon.getHoverName().getString(), 25), 185, 68, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Type " + gun.getWeaponType().name().replace('_', ' '), 185, 82, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Energy " + (energy == Integer.MAX_VALUE ? "INFINITE" : energy + " / " + capacity + " FE"), 185, 98, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Heat " + heat + " / " + maxHeat + (gun.isOverheated(weapon) ? " OVERHEATED" : ""), 185, 112, gun.isOverheated(weapon) ? MachineScreenStyle.DANGER : MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Modules " + WeaponSystem.installedModuleCount(weapon) + " / 6", 185, 128, MachineScreenStyle.GREEN, false);
        String sight = WeaponSystem.hasEffect(weapon, WeaponModuleItem.Effect.SNIPER_SCOPE) ? "Sniper Scope" : WeaponSystem.hasEffect(weapon, WeaponModuleItem.Effect.HOLO_SIGHTS) ? "Holo Sights" : "Standard sights";
        graphics.drawString(font, "Sight " + sight, 185, 142, MachineScreenStyle.MUTED, false);
    }

    private void renderModules(GuiGraphics graphics) {
        ItemStack weapon = menu.getSlot(0).getItem();
        graphics.drawString(font, "MODULE LAYOUT", 216, 51, MachineScreenStyle.CYAN, false);
        if (weapon.isEmpty()) {
            graphics.drawString(font, "Insert an energy weapon", 185, 70, MachineScreenStyle.MUTED, false);
            graphics.drawString(font, "Modules unpack into the", 185, 84, MachineScreenStyle.MUTED, false);
            graphics.drawString(font, "legacy six-role layout.", 185, 96, MachineScreenStyle.MUTED, false);
            return;
        }
        String[] names = {"Battery", "Color", "Barrel", "Sights", "Utility A", "Utility B"};
        for (int i = 0; i < 6; i++) {
            ItemStack module = menu.getSlot(i + 1).getItem();
            int color = module.isEmpty() ? MachineScreenStyle.MUTED : MachineScreenStyle.GREEN;
            graphics.drawString(font, names[i] + ": " + (module.isEmpty() ? "empty" : trim(module.getHoverName().getString(), 17)), 185, 68 + i * 14, color, false);
        }
    }

    private void renderStats(GuiGraphics graphics) {
        ItemStack weapon = snapshot();
        graphics.drawString(font, "EFFECTIVE STATS", 211, 51, MachineScreenStyle.CYAN, false);
        if (weapon.isEmpty()) { graphics.drawString(font, "Insert a weapon to inspect", 185, 72, MachineScreenStyle.MUTED, false); return; }
        graphics.drawString(font, String.format(Locale.ROOT, "Damage x%.2f", WeaponSystem.damageMultiplier(weapon)), 185, 70, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, String.format(Locale.ROOT, "Energy use x%.2f", WeaponSystem.energyMultiplier(weapon)), 185, 85, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, String.format(Locale.ROOT, "Cooldown x%.2f", WeaponSystem.cooldownMultiplier(weapon)), 185, 100, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, String.format(Locale.ROOT, "Range x%.2f", WeaponSystem.rangeMultiplier(weapon)), 185, 115, MachineScreenStyle.TEXT, false);
        boolean scope = WeaponSystem.hasEffect(weapon, WeaponModuleItem.Effect.SNIPER_SCOPE);
        graphics.drawString(font, scope ? "Scope zoom x0.85 while aiming" : "Scope zoom: none", 185, 130, scope ? MachineScreenStyle.PURPLE : MachineScreenStyle.MUTED, false);
        ItemStack barrel = menu.getSlot(WeaponSystem.BARREL_SLOT + 1).getItem();
        graphics.drawString(font, "Barrel " + (barrel.getItem() instanceof WeaponModuleItem module ? module.getEffect().name() : "STANDARD"), 185, 145, barrel.isEmpty() ? MachineScreenStyle.MUTED : MachineScreenStyle.PURPLE, false);
    }

    private String trim(String value, int max) { return value.length() <= max ? value : value.substring(0, Math.max(0, max - 1)) + "…"; }
}
