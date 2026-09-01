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

/** Compact live charge and heat readout for any held Matter Overdrive energy weapon. */
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
        int x = width - 126;
        int y = height - 64;

        int energy = weapon.getEnergyStored(stack);
        int capacity = weapon.getCapacity(stack);
        int heat = Math.round(weapon.getHeat(stack));
        int maxHeat = weapon.getMaxHeat(stack);
        boolean overheated = weapon.isOverheated(stack);

        graphics.fill(x, y, x + 120, y + 48, 0xB0101820);
        graphics.fill(x, y, x + 120, y + 1, 0xFF4EDCFF);
        String name = stack.getHoverName().getString().toUpperCase();
        graphics.drawString(minecraft.font, name, x + 6, y + 5, 0xFFE8F8FF, false);

        bar(graphics, x + 6, y + 19, energy, Math.max(1, capacity), 0xFF35CFFF);
        graphics.drawString(minecraft.font, "CHARGE " + compact(energy) + "/" + compact(capacity), x + 6, y + 27, 0xFFB5DFFF, false);

        int heatColour = overheated ? 0xFFFF3322 : heat * 2 >= maxHeat ? 0xFFFFA62B : 0xFFFFD34D;
        bar(graphics, x + 6, y + 37, heat, Math.max(1, maxHeat), heatColour);
        String heatText = overheated ? "OVERHEATED" : "HEAT " + heat + "/" + maxHeat;
        graphics.drawString(minecraft.font, heatText, x + 6, y + 45, heatColour, false);
    }

    private static void bar(GuiGraphics graphics, int x, int y, int value, int max, int colour) {
        graphics.fill(x, y, x + 108, y + 4, 0xFF26333D);
        int filled = Math.max(0, Math.min(108, Math.round(108.0F * value / max)));
        graphics.fill(x, y, x + filled, y + 4, colour);
    }

    private static String compact(int value) {
        return value >= 1_000_000 ? String.format("%.1fM", value / 1_000_000.0D)
                : value >= 1_000 ? String.format("%.1fk", value / 1_000.0D) : Integer.toString(value);
    }
}