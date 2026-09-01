package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AndroidHudOverlay {
    private static final int CAPACITY = 100_000;
    private AndroidHudOverlay() {}

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        if (!AndroidClientState.isActive() || Minecraft.getInstance().options.hideGui) return;
        GuiGraphics graphics = event.getGuiGraphics();
        int x = 8;
        int y = event.getWindow().getGuiScaledHeight() - 58;
        int energy = AndroidClientState.energy();
        graphics.fill(x, y, x + 112, y + 27, 0xB0101820);
        graphics.fill(x, y, x + 112, y + 1, 0xFF53E6FF);
        graphics.drawString(Minecraft.getInstance().font, "ANDROID CORE", x + 6, y + 5, 0xFFE8F8FF, false);
        graphics.fill(x + 6, y + 16, x + 106, y + 20, 0xFF26333D);
        int filled = Math.max(0, Math.min(100, Math.round(100.0F * energy / CAPACITY)));
        graphics.fill(x + 6, y + 16, x + 6 + filled, y + 20, energy < 15_000 ? 0xFFFF8A2B : 0xFF35CFFF);
        graphics.drawString(Minecraft.getInstance().font, compact(energy) + " / 100k FE", x + 6, y + 22, 0xFFB5DFFF, false);
    }
    private static String compact(int value) {
        return value >= 1_000 ? String.format("%.1fk", value / 1_000.0D) : Integer.toString(value);
    }
}