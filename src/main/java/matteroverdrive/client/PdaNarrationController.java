package matteroverdrive.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Client-only long-form read-aloud controller for PDA pages and NPC dialogue. */
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
                        "Long-form read aloud requires Minecraft Narrator (System or All) in Accessibility settings. Short PDA callouts still use the local offline voice engine when available.")
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
        PdaEmbeddedAudio.stopVoice();
        Minecraft.getInstance().getNarrator().clear();
        speaking = false;
        lastText = "";
    }

    public static boolean isSpeaking() {
        return speaking || PdaEmbeddedAudio.isVoicePlaying();
    }

    public static String lastText() {
        return lastText;
    }
}
