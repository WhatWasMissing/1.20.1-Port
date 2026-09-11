package matteroverdrive.client.screen;

import matteroverdrive.android.AndroidChassisData;
import matteroverdrive.android.AndroidData;
import matteroverdrive.menu.AndroidStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Central Android equipment/workbench screen for body parts, chassis hardware and progression. */
public class AndroidStationScreen extends AbstractContainerScreen<AndroidStationMenu> {
    private static final int PANEL_X = 8;
    private static final int BODY_Y = 51;
    private static final int CHASSIS_Y = 77;
    private static final int ROW_H = 15;

    public AndroidStationScreen(AndroidStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 272;
        imageHeight = 236;
        inventoryLabelX = 55;
        inventoryLabelY = 146;
    }

    @Override
    protected void init() {
        super.init();
        addServerButton("CYCLE", 10, 28, 52, 18, 1);
        addRenderableWidget(Button.builder(Component.literal("SKILLS"), button -> {
            if (minecraft != null) minecraft.setScreen(new AndroidSkillTreeScreen());
        }).bounds(leftPos + 66, topPos + 28, 60, 18).build());
        addRenderableWidget(Button.builder(Component.literal("CLASS MATRIX"), button -> {
            if (minecraft != null) minecraft.setScreen(new AndroidClassLoadoutScreen());
        }).bounds(leftPos + 130, topPos + 28, 88, 18).build());

        AndroidData.Part[] parts = AndroidData.Part.values();
        String[] partNames = {"HEAD", "TORSO", "ARMS", "LEGS"};
        for (int i = 0; i < parts.length; i++) {
            int x = 10 + i * 63;
            addServerButton(partNames[i], x, BODY_Y, 58, 18, AndroidStationMenu.PART_BUTTON_BASE + i);
        }

        for (AndroidChassisData.Slot slot : AndroidChassisData.Slot.values()) {
            AndroidChassisData.Module first = null;
            AndroidChassisData.Module second = null;
            for (AndroidChassisData.Module module : AndroidChassisData.Module.values()) {
                if (module.slot != slot) continue;
                if (first == null) first = module; else { second = module; break; }
            }
            int y = CHASSIS_Y + slot.ordinal() * ROW_H;
            if (first != null) addServerButton(shortName(first), 73, y, 89, 14,
                    AndroidStationMenu.CHASSIS_BUTTON_BASE + first.ordinal());
            if (second != null) addServerButton(shortName(second), 166, y, 89, 14,
                    AndroidStationMenu.CHASSIS_BUTTON_BASE + second.ordinal());
        }
    }

    private void addServerButton(String label, int x, int y, int width, int height, int id) {
        addRenderableWidget(Button.builder(Component.literal(label), button -> {
            if (minecraft != null && minecraft.gameMode != null)
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }).bounds(leftPos + x, topPos + y, width, height).build());
    }

    private static String shortName(AndroidChassisData.Module module) {
        return switch (module) {
            case CAPACITOR_CORE -> "CAPACITOR";
            case OVERCLOCK_CORE -> "OVERCLOCK";
            case LIGHTWEIGHT_FRAME -> "LIGHTWEIGHT";
            case REINFORCED_FRAME -> "REINFORCED";
            case AGILITY_MUSCLES -> "AGILITY";
            case SIEGE_MUSCLES -> "SIEGE";
            case HUNTER_OPTICS -> "HUNTER";
            case PRECISION_OPTICS -> "PRECISION";
            case STEALTH_SHELL -> "STEALTH";
            case REACTIVE_SHELL -> "REACTIVE";
        };
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineScreenStyle.drawFrame(graphics, leftPos, topPos, imageWidth, imageHeight, inventoryLabelY, MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics, leftPos + PANEL_X, topPos + 48, imageWidth - PANEL_X * 2, 104);

        int energy = menu.androidEnergy();
        int capacity = Math.max(1, menu.androidCapacity());
        MachineScreenStyle.drawHorizontalBar(graphics, leftPos + 222, topPos + 17, 40, 5,
                energy, capacity, MachineScreenStyle.CYAN);

        for (int i = 0; i < AndroidData.Part.values().length; i++) {
            boolean installed = (menu.parts() & AndroidData.Part.values()[i].bit) != 0;
            int x = leftPos + 10 + i * 63;
            if (installed) {
                graphics.fill(x, topPos + BODY_Y + 16, x + 58, topPos + BODY_Y + 18, MachineScreenStyle.GREEN);
            }
        }

        for (AndroidChassisData.Module module : AndroidChassisData.Module.values()) {
            if (!menu.chassisEquipped(module)) continue;
            int y = topPos + CHASSIS_Y + module.slot.ordinal() * ROW_H;
            boolean first = isFirst(module);
            int x = leftPos + (first ? 73 : 166);
            graphics.fill(x, y + 12, x + 89, y + 14, MachineScreenStyle.GREEN);
        }
    }

    private static boolean isFirst(AndroidChassisData.Module module) {
        for (AndroidChassisData.Module candidate : AndroidChassisData.Module.values()) {
            if (candidate.slot == module.slot) return candidate == module;
        }
        return false;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        if (!menu.androidActive()) {
            graphics.drawString(font, MachineScreenStyle.fit(font,
                    "ANDROID OFFLINE - convert before installing hardware", imageWidth - 16), 8, 18, MachineScreenStyle.DANGER, false);
        } else {
            String status = "Lv " + menu.androidLevel() + "  |  " + menu.androidEnergy() + "/" + menu.androidCapacity() + " FE"
                    + "  |  " + menu.availableSkillPoints() + " pts";
            graphics.drawString(font, MachineScreenStyle.fit(font, status, imageWidth - 16), 8, 18, MachineScreenStyle.CYAN, false);
        }

        graphics.drawString(font, MachineScreenStyle.fit(font, "BODY SYSTEMS - click to install/remove", imageWidth - 20),
                10, 40, MachineScreenStyle.MUTED, false);

        AndroidData.Ability[] abilities = AndroidData.Ability.values();
        int selected = Math.max(0, Math.min(menu.selectedAbilityOrdinal(), abilities.length - 1));
        AndroidData.Ability ability = abilities[selected];
        graphics.drawString(font, MachineScreenStyle.fit(font, "Core: " + ability.displayName, imageWidth - 230),
                222, 29, MachineScreenStyle.MUTED, false);

        for (AndroidChassisData.Slot slot : AndroidChassisData.Slot.values()) {
            int y = CHASSIS_Y + slot.ordinal() * ROW_H + 3;
            int ordinal = menu.chassisModuleOrdinal(slot);
            String marker = ordinal >= 0 ? ">" : "-";
            graphics.drawString(font, marker + " " + prettySlot(slot), 12, y, ordinal >= 0 ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        }

        graphics.drawString(font, MachineScreenStyle.fit(font,
                "Click equipped hardware again to remove it. Swapping returns the old module.", imageWidth - 20),
                10, 143, MachineScreenStyle.MUTED, false);
    }

    private static String prettySlot(AndroidChassisData.Slot slot) {
        return switch (slot) {
            case CORE -> "CORE";
            case FRAME -> "FRAME";
            case MUSCLES -> "MUSCLES";
            case OPTICS -> "OPTICS";
            case SHELL -> "SHELL";
        };
    }
}
