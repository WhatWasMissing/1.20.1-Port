package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Animated exploration/presentation HUD shared by all players. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PresentationOverlay {
    private PresentationOverlay() {}

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) return;
        GuiGraphics graphics = event.getGuiGraphics();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        renderFacilityBanner(graphics, minecraft, width);
        renderHazard(graphics, minecraft, width, height);
        renderCaption(graphics, minecraft, width, height);
    }

    private static void renderFacilityBanner(GuiGraphics graphics, Minecraft minecraft, int width) {
        ClientPdaNotificationManager.FacilityBanner banner = ClientPdaNotificationManager.banner();
        if (banner == null) return;

        int panelW = Math.min(390, Math.max(230, width - 44));
        int panelH = 54;
        float enter = Math.min(1.0F, banner.age() / 10.0F);
        float exit = Math.min(1.0F, banner.ticksLeft() / 16.0F);
        float factor = smooth(Math.min(enter, exit));
        int x = (width - panelW) / 2;
        int y = Math.round(-panelH - 4 + (panelH + 16) * factor);
        int alpha = Math.max(0x28, Math.min(0xEE, Math.round(0xEE * factor)));

        graphics.fill(x, y, x + panelW, y + panelH, (alpha << 24) | 0x07161D);
        graphics.fill(x, y + panelH - 2, x + panelW, y + panelH, (alpha << 24) | 0x4FD7E8);
        graphics.fill(x + 1, y + 1, x + 4, y + panelH - 2, (alpha << 24) | 0xFFB34E);

        graphics.drawCenteredString(minecraft.font, "FACILITY IDENTIFIED", width / 2, y + 8, 0xFF66DDEB);
        graphics.drawCenteredString(minecraft.font, fit(minecraft, banner.facility(), panelW - 28), width / 2, y + 21, 0xFFF1F7F9);
        String footer = "ARCHIVE " + String.format("%02d", banner.archiveIndex())
                + " // " + (banner.classification().isBlank() ? "UNCLASSIFIED" : banner.classification());
        graphics.drawCenteredString(minecraft.font, fit(minecraft, footer, panelW - 28), width / 2, y + 36, 0xFF91AAB2);
    }

    private static void renderHazard(GuiGraphics graphics, Minecraft minecraft, int width, int height) {
        ClientPdaNotificationManager.Hazard hazard = ClientPdaNotificationManager.hazard();
        if (hazard == null || !hazard.active()) return;

        int panelW = Math.min(300, Math.max(170, width / 3));
        int x = Math.max(8, width - panelW - 8);
        int y = 8;
        int severity = hazard.severity();
        boolean pulse = ((minecraft.player.tickCount / 8) & 1) == 0;
        int accent = switch (severity) {
            case 3 -> pulse ? 0xFFFF554F : 0xFFB92B2B;
            case 2 -> pulse ? 0xFFFFB347 : 0xFFC57922;
            default -> 0xFF59D7E5;
        };
        int background = severity >= 3 ? 0xD52A090A : severity == 2 ? 0xD52B1A08 : 0xD50A1B22;

        graphics.fill(x, y, x + panelW, y + 38, background);
        graphics.fill(x, y, x + 3, y + 38, accent);
        graphics.fill(x + 3, y, x + panelW, y + 2, accent);
        graphics.drawString(minecraft.font,
                severity >= 3 ? "CRITICAL ENVIRONMENT" : severity == 2 ? "ENVIRONMENTAL WARNING" : "ENVIRONMENTAL ADVISORY",
                x + 9, y + 7, accent, false);
        graphics.drawString(minecraft.font, fit(minecraft, hazard.title(), panelW - 18), x + 9, y + 18, 0xFFF0F4F5, false);
        if (!hazard.detail().isBlank()) {
            graphics.drawString(minecraft.font, fit(minecraft, hazard.detail(), panelW - 18), x + 9, y + 28, 0xFFB8C3C8, false);
        }
    }

    private static void renderCaption(GuiGraphics graphics, Minecraft minecraft, int width, int height) {
        String caption = ClientPdaNotificationManager.caption();
        if (caption == null || caption.isBlank()) return;
        int age = ClientPdaNotificationManager.captionAge();
        int left = ClientPdaNotificationManager.captionTicksLeft();
        float enter = Math.min(1.0F, age / 8.0F);
        float exit = Math.min(1.0F, left / 12.0F);
        float factor = smooth(Math.min(enter, exit));
        int maxW = Math.min(560, width - 36);
        String text = fit(minecraft, caption, maxW - 40);
        int panelW = Math.min(maxW, minecraft.font.width(text) + 40);
        int x = (width - panelW) / 2;
        int y = Math.max(8, height - 74);
        int alpha = Math.max(0x30, Math.min(0xE8, Math.round(0xE8 * factor)));
        graphics.fill(x, y, x + panelW, y + 28, (alpha << 24) | 0x08151C);
        graphics.fill(x, y, x + 3, y + 28, (alpha << 24) | 0x4FD7E8);
        graphics.drawString(minecraft.font, "PDA", x + 10, y + 6, 0xFF5ADDEB, false);
        graphics.drawString(minecraft.font, text, x + 32, y + 6, 0xFFEAF3F5, false);
    }

    private static float smooth(float value) {
        float x = Math.max(0.0F, Math.min(1.0F, value));
        return x * x * (3.0F - 2.0F * x);
    }

    private static String fit(Minecraft minecraft, String text, int maxWidth) {
        if (text == null) return "";
        if (minecraft.font.width(text) <= maxWidth) return text;
        int usable = Math.max(0, maxWidth - minecraft.font.width("…"));
        return minecraft.font.plainSubstrByWidth(text, usable) + "…";
    }
}
