package matteroverdrive.client.screen;

import matteroverdrive.android.AndroidData;
import matteroverdrive.menu.AndroidStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AndroidStationScreen extends AbstractContainerScreen<AndroidStationMenu> {
    private static final ResourceLocation HEAD = tex("android_slot_head.png");
    private static final ResourceLocation CHEST = tex("android_slot_chest.png");
    private static final ResourceLocation ARMS = tex("android_slot_arms.png");
    private static final ResourceLocation LEGS = tex("android_slot_legs.png");
    private static final ResourceLocation FEATURE = tex("android_feature_icon_bg.png");
    private static final ResourceLocation FEATURE_ACTIVE = tex("android_feature_icon_bg_active.png");

    public AndroidStationScreen(AndroidStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 232;
        inventoryLabelY = 120;
    }

    private static ResourceLocation tex(String name) {
        return new ResourceLocation("matteroverdrive", "textures/gui/items/" + name);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("CYCLE"), button -> {
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
        }).bounds(leftPos + 18, topPos + 102, 54, 18).build());
        addRenderableWidget(Button.builder(Component.literal("SKILL TREE"), button -> {
            if (minecraft != null) minecraft.setScreen(new AndroidSkillTreeScreen());
        }).bounds(leftPos + 75, topPos + 102, 83, 18).build());
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
        MachineScreenStyle.drawSection(graphics, leftPos + 17, topPos + 29, 142, 71);
        MachineScreenStyle.drawHorizontalBar(graphics, leftPos + 25, topPos + 51, 126, 5,
                menu.androidEnergy(), menu.androidCapacity(), MachineScreenStyle.CYAN);

        drawPart(graphics, leftPos + 25, topPos + 62, AndroidData.Part.HEAD, HEAD);
        drawPart(graphics, leftPos + 57, topPos + 62, AndroidData.Part.CHEST, CHEST);
        drawPart(graphics, leftPos + 89, topPos + 62, AndroidData.Part.ARMS, ARMS);
        drawPart(graphics, leftPos + 121, topPos + 62, AndroidData.Part.LEGS, LEGS);

        AndroidData.Ability[] abilities = AndroidData.Ability.values();
        int selected = Math.max(0, Math.min(menu.selectedAbilityOrdinal(), abilities.length - 1));
        AndroidData.Ability ability = abilities[selected];
        boolean unlocked = menu.androidLevel() >= ability.requiredLevel && (menu.parts() & ability.requiredPart.bit) != 0;
        boolean active = switch (ability) {
            case CLOAK -> (menu.activeAbilityFlags() & 1) != 0;
            case FORCE_FIELD -> (menu.activeAbilityFlags() & 2) != 0;
            default -> false;
        };
        graphics.blit(active ? FEATURE_ACTIVE : FEATURE, leftPos + 19, topPos + 79, 0, 0, 22, 22, 22, 22);
        if (!unlocked) graphics.fill(leftPos + 22, topPos + 82, leftPos + 38, topPos + 98, 0x88000000);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        if (!menu.androidActive()) {
            graphics.drawString(font, "ANDROID OFFLINE", 47, 33, MachineScreenStyle.DANGER, false);
            graphics.drawString(font, "Use a blue Android Pill to convert", 18, 43, MachineScreenStyle.MUTED, false);
        } else {
            graphics.drawString(font, "ANDROID ONLINE", 49, 33, MachineScreenStyle.CYAN, false);
            graphics.drawString(font, menu.androidEnergy() + " / " + menu.androidCapacity() + " FE", 33, 43, MachineScreenStyle.TEXT, false);
        }

        AndroidData.Ability[] abilities = AndroidData.Ability.values();
        int selected = Math.max(0, Math.min(menu.selectedAbilityOrdinal(), abilities.length - 1));
        AndroidData.Ability ability = abilities[selected];
        boolean unlocked = menu.androidLevel() >= ability.requiredLevel && (menu.parts() & ability.requiredPart.bit) != 0;
        boolean active = switch (ability) {
            case CLOAK -> (menu.activeAbilityFlags() & 1) != 0;
            case FORCE_FIELD -> (menu.activeAbilityFlags() & 2) != 0;
            default -> false;
        };
        graphics.drawString(font, ability.displayName, 44, 81, unlocked ? MachineScreenStyle.TEXT : MachineScreenStyle.DANGER, false);
        graphics.drawString(font, unlocked ? (active ? "ACTIVE" : "READY") : "LOCKED - " + ability.requiredPart.name() + " Lv " + ability.requiredLevel,
                44, 90, active ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Lv " + menu.androidLevel() + "  XP " + menu.experienceIntoLevel() + "/" + menu.experienceToNextLevel()
                        + "  P " + menu.availableSkillPoints(), 20, 122, MachineScreenStyle.TEXT, false);
    }

    private void drawPart(GuiGraphics graphics, int x, int y, AndroidData.Part part, ResourceLocation icon) {
        boolean installed = (menu.parts() & part.bit) != 0;
        graphics.blit(icon, x, y, 0, 0, 16, 16, 16, 16);
        if (!installed) graphics.fill(x, y, x + 16, y + 16, 0x99000000);
        else {
            graphics.fill(x, y + 15, x + 16, y + 16, 0xFF00D8FF);
            graphics.fill(x + 15, y, x + 16, y + 16, 0xFF00D8FF);
        }
    }
}
