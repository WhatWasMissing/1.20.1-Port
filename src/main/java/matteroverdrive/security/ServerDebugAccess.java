package matteroverdrive.security;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** Server-side gate for development-only machine controls. */
public final class ServerDebugAccess {
    private ServerDebugAccess() {}

    public static boolean allowed(Player player) {
        return player != null && player.hasPermissions(2);
    }

    public static boolean require(Player player) {
        if (allowed(player)) return true;
        if (player != null) {
            player.displayClientMessage(Component.literal("Operator permission required for debug controls."), true);
        }
        return false;
    }
}
