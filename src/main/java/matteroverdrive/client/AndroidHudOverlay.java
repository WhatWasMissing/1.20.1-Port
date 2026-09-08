package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.android.AndroidClassAbilities;
import matteroverdrive.android.AndroidLoadout;
import matteroverdrive.android.AndroidUltimates;
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
        int panelHeight = 112;
        int y = event.getWindow().getGuiScaledHeight() - panelHeight - 24;
        int width = 230;
        int energy = AndroidClientState.energy();

        graphics.fill(x, y, x + width, y + panelHeight, 0xB0101820);
        graphics.fill(x, y, x + width, y + 1, 0xFF53E6FF);
        graphics.drawString(minecraft.font, "ANDROID CORE", x + 6, y + 5, 0xFFE8F8FF, false);

        graphics.fill(x + 6, y + 16, x + width - 6, y + 20, 0xFF26333D);
        int barWidth = width - 12;
        int filled = Math.max(0, Math.min(barWidth, Math.round(barWidth * energy / (float) CAPACITY)));
        graphics.fill(x + 6, y + 16, x + 6 + filled, y + 20,
                energy < 15_000 ? 0xFFFF8A2B : 0xFF35CFFF);
        graphics.drawString(minecraft.font, compact(energy) + " / 100k FE",
                x + 6, y + 23, 0xFFB5DFFF, false);

        int level = AndroidClientState.level();
        int levelStart = (level - 1) * 100;
        int unspent = Math.max(0, level - Long.bitCount(AndroidClientState.selectedPerks()));
        String skillTreeKey = AndroidKeyMappings.OPEN_SKILL_TREE.getTranslatedKeyMessage().getString();
        String progression = (level >= 10
                ? "LEVEL 10  MAX XP"
                : String.format("LEVEL %d  XP %d / %d", level,
                        Math.max(0, AndroidClientState.experience() - levelStart), 100))
                + (unspent > 0 ? "  " + skillTreeKey + ":" + unspent : "");
        graphics.drawString(minecraft.font, progression, x + 6, y + 34, 0xFFFFD27A, false);

        graphics.drawString(minecraft.font,
                Component.literal("CORE ABILITY: " + AndroidClientState.abilityName()),
                x + 6, y + 46, 0xFFE8F8FF, false);

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
            state = String.format("CORE COOLDOWN %.1fs", AndroidClientState.cooldownTicks() / 20.0D);
            color = 0xFFFFB45B;
        } else {
            String cycleKey = AndroidKeyMappings.CYCLE_ABILITY.getTranslatedKeyMessage().getString();
            String activateKey = AndroidKeyMappings.ACTIVATE_ABILITY.getTranslatedKeyMessage().getString();
            state = cycleKey + ": CYCLE   " + activateKey + ": ACTIVATE";
            color = 0xFF9BB6C3;
        }
        graphics.drawString(minecraft.font, state, x + 6, y + 57, color, false);

        AndroidLoadout.Specialization spec = AndroidClientState.specialization();
        drawAbilitySlot(graphics, minecraft, x + 6, y + 72, "H", AndroidClassAbilities.classAbilityName(spec),
                AndroidClientState.classAbilityCooldownTicks(), AndroidClassAbilities.classEnergyCost(spec),
                AndroidClassAbilities.REQUIRED_LEVEL, energy);
        drawAbilitySlot(graphics, minecraft, x + 6, y + 84, "N", AndroidClassAbilities.techAbilityName(spec),
                AndroidClientState.techAbilityCooldownTicks(), AndroidClassAbilities.techEnergyCost(spec),
                AndroidClassAbilities.REQUIRED_LEVEL, energy);
        drawAbilitySlot(graphics, minecraft, x + 6, y + 96, "G", spec.ultimate.displayName,
                AndroidClientState.ultimateCooldownTicks(), spec.ultimate.energyCost,
                AndroidUltimates.REQUIRED_LEVEL, energy);
    }

    private static void drawAbilitySlot(GuiGraphics graphics, Minecraft minecraft, int x, int y, String key,
                                        String name, int cooldownTicks, int cost, int requiredLevel, int energy) {
        String status;
        int color;
        if (AndroidClientState.level() < requiredLevel) {
            status = "LOCK L" + requiredLevel;
            color = 0xFF8F979F;
        } else if (cooldownTicks > 0) {
            status = String.format("%.1fs", cooldownTicks / 20.0D);
            color = 0xFFFFB45B;
        } else if (energy < cost) {
            status = "NEEDS " + compact(cost) + " FE";
            color = 0xFFFF6060;
        } else {
            status = "READY";
            color = 0xFF65FF9A;
        }
        graphics.drawString(minecraft.font, key + "  " + name + "  //  " + status, x, y, color, false);
    }

    private static String compact(int value) {
        return value >= 1_000 ? String.format("%.1fk", value / 1_000.0D) : Integer.toString(value);
    }
}
