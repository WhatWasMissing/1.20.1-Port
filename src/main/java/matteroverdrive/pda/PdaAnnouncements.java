package matteroverdrive.pda;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

/**
 * Canonical PDA announcement path.
 *
 * Text is always delivered and is the gameplay-safe fallback. A future optional
 * client speech provider can be attached here using {@link PdaMessage#id()}
 * without requiring pre-rendered voice files in the Matter Overdrive jar.
 */
public final class PdaAnnouncements {
    private PdaAnnouncements() {
    }

    public static void send(ServerPlayer player, PdaMessage message, ChatFormatting style, Object... args) {
        player.sendSystemMessage(message.component(args).withStyle(style));
    }
}
