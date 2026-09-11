package matteroverdrive.client;

import matteroverdrive.pda.PdaVoiceLineCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Client endpoint for short contextual PDA callouts. */
public final class ClientPdaVoiceOpener {
    private ClientPdaVoiceOpener() {}

    public static void play(String lineId) {
        String line = PdaVoiceLineCatalog.line(lineId);
        if (line.isBlank()) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(
                    Component.literal("[PDA] ").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                            .append(Component.literal(line).withStyle(ChatFormatting.WHITE)),
                    true);
        }

        // Short callouts are voiced when the player's Minecraft narrator is active.
        // Text is always shown so the system never depends on audio for information.
        if (minecraft.getNarrator().isActive()) {
            PdaNarrationController.read("P D A. " + line);
        }
    }
}
