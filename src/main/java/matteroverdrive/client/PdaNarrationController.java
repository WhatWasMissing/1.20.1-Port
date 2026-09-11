package matteroverdrive.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Client-only spoken playback for recovered PDA lore.
 *
 * This deliberately uses Minecraft's built-in narrator instead of bundled voice
 * assets. Players therefore retain their normal platform narrator voice and can
 * stop playback instantly from the PDA.
 */
public final class PdaNarrationController {
    private static boolean speaking;
    private static String lastText = "";

    private PdaNarrationController() {}

    /** Returns true when narration was submitted to Minecraft's narrator. */
    public static boolean read(String text) {
        if (text == null || text.isBlank()) return false;
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.getNarrator().isActive()) {
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(Component.literal(
                        "PDA audio log unavailable: enable Minecraft Narrator (System or All) in Accessibility settings.")
                        .withStyle(ChatFormatting.YELLOW), true);
            }
            speaking = false;
            lastText = "";
            return false;
        }

        stop();
        String normalized = text.replace('\n', ' ').replaceAll("\\s+", " ").trim();
        minecraft.getNarrator().sayNow(normalized);
        lastText = normalized;
        speaking = true;
        return true;
    }

    public static void stop() {
        Minecraft.getInstance().getNarrator().clear();
        speaking = false;
        lastText = "";
    }

    public static boolean isSpeaking() {
        return speaking;
    }

    public static String lastText() {
        return lastText;
    }
}
