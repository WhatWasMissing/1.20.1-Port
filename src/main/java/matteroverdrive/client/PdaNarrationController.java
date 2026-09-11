package matteroverdrive.client;

import net.minecraft.client.Minecraft;

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

    public static void read(String text) {
        if (text == null || text.isBlank()) return;
        stop();
        String normalized = text.replace('\n', ' ').replaceAll("\\s+", " ").trim();
        Minecraft.getInstance().getNarrator().sayNow(normalized);
        lastText = normalized;
        speaking = true;
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
