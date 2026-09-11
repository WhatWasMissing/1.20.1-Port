package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.weapon.EnergyWeaponItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Live charge, heat and firing-state readout for held Matter Overdrive energy weapons. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class WeaponHudOverlay {
    private WeaponHudOverlay() {}

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui) return;
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof EnergyWeaponItem weapon)) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        int panelWidth = 138;
        panelWidth = Math.min(panelWidth, Math.max(120, width - 12));
        int x = Math.max(6, width - panelWidth - 6);
        int y = Math.max(6, height - 78);

        int energy = weapon.getEnergyStored(stack);
        int capacity = weapon.getCapacity(stack);
        int heat = Math.round(weapon.getHeat(stack));
        int maxHeat = weapon.getMaxHeat(stack);
        boolean overheated = weapon.isOverheated(stack);
        boolean empty = energy <= 0;

        graphics.fill(x, y, x + panelWidth, y + 62, 0xC0101820);
        graphics.fill(x, y, x + panelWidth, y + 2, overheated ? 0xFFFF4B36 : 0xFF4EDCFF);
        String name = stack.getHoverName().getString().toUpperCase();
        graphics.drawString(minecraft.font, fit(minecraft, name, panelWidth - 54), x + 6, y + 6, 0xFFE8F8FF, false);

        String state = overheated ? "OVERHEATED" : empty ? "NO INTERNAL FE" : "READY";
        int stateColour = overheated || empty ? 0xFFFF5F52 : 0xFF65FF9A;
        graphics.drawString(minecraft.font, state, x + panelWidth - minecraft.font.width(state) - 6, y + 6, stateColour, false);

        bar(graphics, x + 6, y + 19, panelWidth - 12, energy, Math.max(1, capacity), 0xFF35CFFF);
        graphics.drawString(minecraft.font, "FE " + compact(energy) + " / " + compact(capacity), x + 6, y + 26, 0xFFB5DFFF, false);

        int heatColour = overheated ? 0xFFFF3322 : heat * 3 >= maxHeat * 2 ? 0xFFFF8A2B : 0xFFFFD34D;
        bar(graphics, x + 6, y + 37, panelWidth - 12, heat, Math.max(1, maxHeat), heatColour);
        graphics.drawString(minecraft.font, "HEAT " + heat + " / " + maxHeat, x + 6, y + 44, heatColour, false);

        String footer;
        if (weapon.getWeaponType() == EnergyWeaponItem.WeaponType.PHASER) {
            int power = weapon.getPhaserPower(stack);
            footer = power < 3 ? "MODE STUN " + (power + 1) : "MODE KILL " + (power - 2);
        } else {
            footer = player.isUsingItem() ? "AIM / CHARGE ACTIVE" : "SHIFT + USE: RELOAD";
        }
        graphics.drawString(minecraft.font, fit(minecraft, footer, panelWidth - 12), x + 6, y + 53, 0xFF9BB6C3, false);
    }

    private static void bar(GuiGraphics graphics, int x, int y, int width, int value, int max, int colour) {
        graphics.fill(x, y, x + width, y + 4, 0xFF26333D);
        int filled = Math.max(0, Math.min(width, Math.round(width * value / (float) max)));
        graphics.fill(x, y, x + filled, y + 4, colour);
    }

    private static String compact(int value) {
        return value >= 1_000_000 ? String.format("%.1fM", value / 1_000_000.0D)
                : value >= 1_000 ? String.format("%.1fk", value / 1_000.0D) : Integer.toString(value);
    }

    private static String fit(Minecraft minecraft, String text, int maxWidth) {
        if (minecraft.font.width(text) <= maxWidth) return text;
        int usable = Math.max(0, maxWidth - minecraft.font.width("…"));
        return minecraft.font.plainSubstrByWidth(text, usable) + "…";
    }
}
