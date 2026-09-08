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
        imageWidth = 370;
        imageHeight = 184;
        inventoryLabelY = 92;
    }

    @Override protected void init() { super.init(); rebuildButtons(); }

    private void rebuildButtons() {
        clearWidgets();
        for (int i = 0; i < PAGES.length; i++) {
            final int target = i;
            Button tab = Button.builder(Component.literal(PAGES[i]), button -> { page = target; rebuildButtons(); })
                    .bounds(leftPos + 190 + i * 57, topPos + 28, 54, 15).build();
            tab.active = page != i;
            addRenderableWidget(tab);
        }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineScreenStyle.drawFrame(graphics, leftPos, topPos, imageWidth, imageHeight,
                inventoryLabelY, MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics, leftPos + 17, topPos + 27, 151, 62);
        MachineScreenStyle.drawSection(graphics, leftPos + 181, topPos + 25, 180, 150);
        for (int[] position : STATION_SLOT_POSITIONS) {
            MachineScreenStyle.drawSlot(graphics, leftPos + position[0] - 1, topPos + position[1] - 1);
        }
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "WEAPON", 20, 26, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "BAT CLR BAR", 59, 26, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "SIGHT  UTIL A/B", 59, 67, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, MachineScreenStyle.MUTED, false);
        switch (page) {
            case 1 -> renderModules(graphics);
            case 2 -> renderStats(graphics);
            default -> renderHome(graphics);
        }
    }

    private ItemStack snapshot() {
        ItemStack weapon = menu.getSlot(0).getItem();
        if (weapon.isEmpty()) return ItemStack.EMPTY;
        ItemStack copy = weapon.copy();
        for (int i = 0; i < WeaponSystem.MODULE_SLOT_COUNT; i++) {
            WeaponSystem.setModule(copy, i, menu.getSlot(i + 1).getItem());
        }
        return copy;
    }

    private void renderHome(GuiGraphics graphics) {
        ItemStack weapon = snapshot();
        graphics.drawString(font, "LIVE WEAPON STATUS", 218, 51, MachineScreenStyle.CYAN, false);
        if (!(weapon.getItem() instanceof EnergyWeaponItem gun)) {
            graphics.drawString(font, "Insert a Matter Overdrive energy weapon.", 194, 72, MachineScreenStyle.MUTED, false);
            graphics.drawString(font, "All seven physical slots stay live", 194, 89, MachineScreenStyle.TEXT, false);
            graphics.drawString(font, "while changing station pages.", 194, 102, MachineScreenStyle.TEXT, false);
            return;
        }
        int energy = gun.getEnergyStored(weapon);
        int capacity = gun.getCapacity(weapon);
        int heat = Math.round(gun.getHeat(weapon));
        int maxHeat = gun.getMaxHeat(weapon);
        boolean overheated = gun.isOverheated(weapon);
        int energyColor = energy <= 0 ? MachineScreenStyle.DANGER : MachineScreenStyle.CYAN;

        graphics.drawString(font, trim(weapon.getHoverName().getString(), 27), 194, 68, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Frame: " + friendlyType(gun.getWeaponType()), 194, 81, MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "FE: " + (energy == Integer.MAX_VALUE ? "INFINITE" : energy + " / " + capacity), 194, 96, energyColor, false);
        graphics.drawString(font, "Heat: " + heat + " / " + maxHeat + (overheated ? "  OVERHEATED" : ""),
                194, 109, overheated ? MachineScreenStyle.DANGER : MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Modules: " + WeaponSystem.installedModuleCount(weapon) + " / 6", 194, 124, MachineScreenStyle.GREEN, false);
        graphics.drawString(font, "Sight: " + sightName(weapon), 194, 137, MachineScreenStyle.PURPLE, false);
        String readiness = overheated ? "COOL WEAPON BEFORE FIRING"
                : energy <= 0 ? "RELOAD FROM ENERGY PACK / BATTERY"
                : "COMBAT READY";
        graphics.drawString(font, readiness, 194, 154,
                overheated || energy <= 0 ? MachineScreenStyle.AMBER : MachineScreenStyle.GREEN, false);
    }

    private void renderModules(GuiGraphics graphics) {
        ItemStack weapon = menu.getSlot(0).getItem();
        graphics.drawString(font, "MODULE LAYOUT", 225, 51, MachineScreenStyle.CYAN, false);
        if (weapon.isEmpty()) {
            graphics.drawString(font, "Insert an energy weapon first.", 194, 72, MachineScreenStyle.MUTED, false);
            graphics.drawString(font, "Modules map into six functional roles", 194, 89, MachineScreenStyle.TEXT, false);
            graphics.drawString(font, "and remain physically editable here.", 194, 102, MachineScreenStyle.TEXT, false);
            return;
        }
        String[] names = {"Battery", "Color", "Barrel", "Sights", "Utility A", "Utility B"};
        for (int i = 0; i < 6; i++) {
            ItemStack module = menu.getSlot(i + 1).getItem();
            int color = module.isEmpty() ? MachineScreenStyle.MUTED : MachineScreenStyle.GREEN;
            String value = module.isEmpty() ? "EMPTY" : trim(module.getHoverName().getString(), 18);
            graphics.drawString(font, names[i], 194, 68 + i * 15, MachineScreenStyle.MUTED, false);
            graphics.drawString(font, value, 253, 68 + i * 15, color, false);
        }
        graphics.drawString(font, "Changes apply to the effective weapon snapshot immediately.",
                194, 158, MachineScreenStyle.CYAN, false);
    }

    private void renderStats(GuiGraphics graphics) {
        ItemStack weapon = snapshot();
        graphics.drawString(font, "EFFECTIVE MODIFIERS", 214, 51, MachineScreenStyle.CYAN, false);
        if (!(weapon.getItem() instanceof EnergyWeaponItem gun)) {
            graphics.drawString(font, "Insert a weapon to inspect real stats.", 194, 72, MachineScreenStyle.MUTED, false);
            return;
        }
        graphics.drawString(font, String.format(Locale.ROOT, "Damage       x%.2f", WeaponSystem.damageMultiplier(weapon)), 194, 69, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, String.format(Locale.ROOT, "Energy/shot  x%.2f", WeaponSystem.energyMultiplier(weapon)), 194, 83, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, String.format(Locale.ROOT, "Cooldown     x%.2f", WeaponSystem.cooldownMultiplier(weapon)), 194, 97, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, String.format(Locale.ROOT, "Range        x%.2f", WeaponSystem.rangeMultiplier(weapon)), 194, 111, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Capacity     " + gun.getCapacity(weapon) + " FE", 194, 125, MachineScreenStyle.CYAN, false);
        boolean scope = WeaponSystem.hasEffect(weapon, WeaponModuleItem.Effect.SNIPER_SCOPE);
        String zoom = scope ? "0.85x override"
                : gun.getWeaponType() == EnergyWeaponItem.WeaponType.ION_SNIPER ? "0.40x Ion base" : "1.00x base";
        graphics.drawString(font, "Aim zoom     " + zoom, 194, 139, scope ? MachineScreenStyle.PURPLE : MachineScreenStyle.MUTED, false);
        ItemStack barrel = menu.getSlot(WeaponSystem.BARREL_SLOT + 1).getItem();
        String barrelName = barrel.getItem() instanceof WeaponModuleItem module ? module.getEffect().name() : "STANDARD";
        graphics.drawString(font, "Barrel       " + barrelName, 194, 153,
                barrel.isEmpty() ? MachineScreenStyle.MUTED : MachineScreenStyle.PURPLE, false);
        graphics.drawString(font, "All numbers above come from installed modules.", 194, 166, MachineScreenStyle.GREEN, false);
    }

    private String sightName(ItemStack weapon) {
        if (WeaponSystem.hasEffect(weapon, WeaponModuleItem.Effect.SNIPER_SCOPE)) return "Sniper Scope";
        if (WeaponSystem.hasEffect(weapon, WeaponModuleItem.Effect.HOLO_SIGHTS)) return "Holo Sights";
        return "Standard";
    }

    private String friendlyType(EnergyWeaponItem.WeaponType type) {
        return switch (type) {
            case PHASER -> "Phaser";
            case PHASER_RIFLE -> "Phaser Rifle";
            case ION_SNIPER -> "Ion Sniper";
            case PLASMA_SHOTGUN -> "Plasma Shotgun";
        };
    }

    private String trim(String value, int max) {
        return value.length() <= max ? value : value.substring(0, Math.max(0, max - 1)) + "…";
    }
}
