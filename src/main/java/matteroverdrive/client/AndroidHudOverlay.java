package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.android.AndroidClassAbilities;
import matteroverdrive.android.AndroidData;
import matteroverdrive.android.AndroidLoadout;
import matteroverdrive.android.AndroidUltimates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AndroidHudOverlay {
    private static final int PANEL_WIDTH = 252;
    private static final int PANEL_HEIGHT = 148;

    private AndroidHudOverlay() {}

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!AndroidClientState.isActive() || minecraft.options.hideGui) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int guiWidth = event.getWindow().getGuiScaledWidth();
        int guiHeight = event.getWindow().getGuiScaledHeight();
        int panelWidth = Math.min(PANEL_WIDTH, Math.max(120, guiWidth - 16));
        int panelHeight = PANEL_HEIGHT;
        int x = 8;
        int y = Math.max(8, guiHeight - panelHeight - 18);
        int energy = AndroidClientState.energy();
        int capacity = Math.max(1, AndroidClientState.energyCapacity());
        int lowEnergyThreshold = Math.max(1, capacity * 15 / 100);

        graphics.fill(x, y, x + panelWidth, y + panelHeight, 0xC0101820);
        graphics.fill(x, y, x + panelWidth, y + 2, 0xFF53E6FF);
        graphics.fill(x + 1, y + 2, x + 3, y + panelHeight - 1, 0x8047BBD0);

        AndroidLoadout.Specialization spec = AndroidClientState.specialization();
        graphics.drawString(minecraft.font, fit(minecraft, "ANDROID CORE // " + spec.displayName.toUpperCase(), panelWidth - 14),
                x + 7, y + 6, 0xFFE8F8FF, false);

        int barX = x + 7;
        int barY = y + 18;
        int barWidth = panelWidth - 14;
        graphics.fill(barX, barY, barX + barWidth, barY + 6, 0xFF26333D);
        int filled = Math.max(0, Math.min(barWidth, Math.round(barWidth * energy / (float) capacity)));
        graphics.fill(barX, barY, barX + filled, barY + 6,
                energy < lowEnergyThreshold ? 0xFFFF8A2B : 0xFF35CFFF);
        graphics.drawString(minecraft.font, compact(energy) + " / " + compact(capacity) + " FE",
                barX, barY + 8, energy < lowEnergyThreshold ? 0xFFFFB45B : 0xFFB5DFFF, false);

        int level = AndroidClientState.level();
        int unspent = Math.max(0, AndroidData.skillPointsForLevel(level) - Long.bitCount(AndroidClientState.selectedPerks()));
        int levelStart = AndroidData.experienceForLevel(level);
        int levelEnd = level >= AndroidData.MAX_LEVEL ? levelStart : AndroidData.experienceForLevel(level + 1);
        int intoLevel = Math.max(0, AndroidClientState.experience() - levelStart);
        int levelSpan = Math.max(1, levelEnd - levelStart);
        String progression = level >= AndroidData.MAX_LEVEL
                ? "LEVEL 10 // MAX XP"
                : String.format("LEVEL %d // XP %d/%d", level, intoLevel, levelSpan);
        if (unspent > 0) progression += " // " + unspent + " POINT" + (unspent == 1 ? "" : "S");
        graphics.drawString(minecraft.font, fit(minecraft, progression, panelWidth - 14), barX, y + 36, 0xFFFFD27A, false);

        String passive = AndroidClientState.artifact().displayName;
        graphics.drawString(minecraft.font, fit(minecraft,
                "LOADOUT // A" + AndroidClientState.aspectCount() + " F" + AndroidClientState.fragmentCount()
                        + " // " + passive, panelWidth - 14),
                barX, y + 47, 0xFFB5C8D2, false);

        String coreState = coreState();
        int coreColor = coreStateColor();
        graphics.drawString(minecraft.font, fit(minecraft,
                "CORE // " + AndroidClientState.abilityName() + " // " + coreState, panelWidth - 14),
                barX, y + 59, coreColor, false);

        int slotY = y + 75;
        drawAbilitySlot(graphics, minecraft, barX, slotY, panelWidth - 14,
                "H", AndroidClassAbilities.classAbilityName(spec),
                AndroidClientState.classAbilityCooldownTicks(), AndroidClassAbilities.classCooldownTicks(spec),
                AndroidClassAbilities.classEnergyCost(spec), AndroidClassAbilities.REQUIRED_LEVEL, energy);
        drawAbilitySlot(graphics, minecraft, barX, slotY + 23, panelWidth - 14,
                "N", AndroidClassAbilities.techAbilityName(spec),
                AndroidClientState.techAbilityCooldownTicks(), AndroidClassAbilities.techCooldownTicks(spec),
                AndroidClassAbilities.techEnergyCost(spec), AndroidClassAbilities.REQUIRED_LEVEL, energy);
        drawAbilitySlot(graphics, minecraft, barX, slotY + 46, panelWidth - 14,
                "G", spec.ultimate.displayName,
                AndroidClientState.ultimateCooldownTicks(), spec.ultimate.cooldownTicks,
                spec.ultimate.energyCost, AndroidUltimates.REQUIRED_LEVEL, energy);
    }

    private static String coreState() {
        if (AndroidClientState.energy() <= 0) return "OFFLINE";
        if (AndroidClientState.isCloakEnabled() && AndroidClientState.isForceFieldEnabled()) return "CLOAK + FIELD";
        if (AndroidClientState.isForceFieldEnabled()) return "FIELD ACTIVE";
        if (AndroidClientState.isCloakEnabled()) return "CLOAK ACTIVE";
        if (AndroidClientState.cooldownTicks() > 0) return String.format("%.1fs", AndroidClientState.cooldownTicks() / 20.0D);
        return "READY";
    }

    private static int coreStateColor() {
        if (AndroidClientState.energy() <= 0) return 0xFFFF6060;
        if (AndroidClientState.isCloakEnabled() || AndroidClientState.isForceFieldEnabled()) return 0xFF65FF9A;
        if (AndroidClientState.cooldownTicks() > 0) return 0xFFFFB45B;
        return 0xFFE8F8FF;
    }

    private static void drawAbilitySlot(GuiGraphics graphics, Minecraft minecraft, int x, int y, int width,
                                        String key, String name, int cooldownTicks, int baseCooldownTicks,
                                        int cost, int requiredLevel, int energy) {
        boolean locked = AndroidClientState.level() < requiredLevel;
        boolean insufficient = !locked && cooldownTicks <= 0 && energy < cost;
        boolean ready = !locked && cooldownTicks <= 0 && !insufficient;

        int border = ready ? 0xFF65FF9A : locked ? 0xFF626A70 : insufficient ? 0xFFFF6060 : 0xFFFFB45B;
        graphics.fill(x, y, x + width, y + 20, 0xA018242C);
        graphics.fill(x, y, x + 2, y + 20, border);
        graphics.fill(x + 2, y + 18, x + width, y + 20, 0xFF26333D);

        if (cooldownTicks > 0 && baseCooldownTicks > 0) {
            float progress = 1.0F - Math.min(1.0F, cooldownTicks / (float) baseCooldownTicks);
            int progressWidth = Math.max(0, Math.min(width - 2, Math.round((width - 2) * progress)));
            graphics.fill(x + 2, y + 18, x + 2 + progressWidth, y + 20, 0xFF53E6FF);
        } else if (ready) {
            graphics.fill(x + 2, y + 18, x + width, y + 20, 0xFF65FF9A);
        }

        String status;
        if (locked) status = "LOCK L" + requiredLevel;
        else if (cooldownTicks > 0) status = String.format("%.1fs", cooldownTicks / 20.0D);
        else if (insufficient) status = "NEED " + compact(cost);
        else status = "READY";

        graphics.drawString(minecraft.font, key, x + 6, y + 5, border, false);
        graphics.drawString(minecraft.font, fit(minecraft, name, Math.max(20, width - 90)), x + 20, y + 5, 0xFFE8F8FF, false);
        String right = status + " // " + compact(cost) + " FE";
        int rightX = Math.max(x + 84, x + width - minecraft.font.width(right) - 5);
        graphics.drawString(minecraft.font, right, rightX, y + 5, border, false);
    }

    private static String compact(int value) {
        return value >= 1_000 ? String.format("%.1fk", value / 1_000.0D) : Integer.toString(value);
    }

    private static String fit(Minecraft minecraft, String text, int maxWidth) {
        if (minecraft.font.width(text) <= maxWidth) return text;
        int usable = Math.max(0, maxWidth - minecraft.font.width("…"));
        return minecraft.font.plainSubstrByWidth(text, usable) + "…";
    }
}
