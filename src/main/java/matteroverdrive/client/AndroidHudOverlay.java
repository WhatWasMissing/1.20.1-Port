package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AndroidHudOverlay {
    private static final int CAPACITY = 100_000;

    private AndroidHudOverlay() {
    }

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!AndroidClientState.isActive() || minecraft.options.hideGui) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int x = 8;
        int y = event.getWindow().getGuiScaledHeight() - 82;
        int width = 142;
        int energy = AndroidClientState.energy();

        graphics.fill(x, y, x + width, y + 57, 0xB0101820);
        graphics.fill(x, y, x + width, y + 1, 0xFF53E6FF);
        graphics.drawString(minecraft.font, "ANDROID CORE", x + 6, y + 5, 0xFFE8F8FF, false);

        graphics.fill(x + 6, y + 16, x + width - 6, y + 20, 0xFF26333D);
        int barWidth = width - 12;
        int filled = Math.max(0, Math.min(barWidth, Math.round(barWidth * energy / (float) CAPACITY)));
        graphics.fill(x + 6, y + 16, x + 6 + filled, y + 20,
                energy < 15_000 ? 0xFFFF8A2B : 0xFF35CFFF);
        graphics.drawString(minecraft.font, compact(energy) + " / 100k FE",
                x + 6, y + 23, 0xFFB5DFFF, false);

        graphics.drawString(minecraft.font,
                Component.literal("ABILITY: " + AndroidClientState.abilityName()),
                x + 6, y + 35, 0xFFE8F8FF, false);

        String state;
        int color;
        if (energy <= 0) {
            state = "CORE OFFLINE: SPEED LIMITED";
            color = 0xFFFF6060;
        } else if (AndroidClientState.isCloakEnabled() && AndroidClientState.isForceFieldEnabled()) {
            state = "CLOAK + FIELD ON";
            color = 0xFF65FF9A;
        } else if (AndroidClientState.isForceFieldEnabled()) {
            state = "FORCE FIELD ON";
            color = 0xFF65FF9A;
        } else if (AndroidClientState.isCloakEnabled()) {
            state = "CLOAK ON";
            color = 0xFF65FF9A;
        } else if (AndroidClientState.isSelectedAbilityActive()) {
            state = "ACTIVE";
            color = 0xFF65FF9A;
        } else if (AndroidClientState.cooldownTicks() > 0) {
            state = String.format("COOLDOWN %.1fs", AndroidClientState.cooldownTicks() / 20.0D);
            color = 0xFFFFB45B;
        } else {
            state = "V: CYCLE   B: ACTIVATE";
            color = 0xFF9BB6C3;
        }
        graphics.drawString(minecraft.font, state, x + 6, y + 46, color, false);
    }

    private static String compact(int value) {
        return value >= 1_000 ? String.format("%.1fk", value / 1_000.0D) : Integer.toString(value);
    }
}
