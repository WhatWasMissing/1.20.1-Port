package matteroverdrive.dialogue;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.network.ModNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Ephemeral server-side conversation sessions used to validate client choices. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DialogueSessionManager {
    private static final int SESSION_LIFETIME_TICKS = 20 * 90;
    private static final Map<UUID, Session> ACTIVE = new HashMap<>();

    private record Session(String dialogueId, String speaker, String nodeId, long expiresAt) { }

    private DialogueSessionManager() {}

    public static void open(ServerPlayer player, String dialogueId, String speaker) {
        if (!DialogueCatalog.contains(dialogueId)) return;
        DialogueStateSavedData state = DialogueStateSavedData.get(player.serverLevel());
        state.markVisited(player.getUUID(), dialogueId);
        DialogueCatalog.DialogueView view = DialogueCatalog.root(player, dialogueId, speaker);
        if (view == null) return;
        ACTIVE.put(player.getUUID(), new Session(dialogueId, speaker, view.nodeId(),
                player.level().getGameTime() + SESSION_LIFETIME_TICKS));
        ModNetwork.sendDialogueView(player, dialogueId, view);
    }

    public static void choose(ServerPlayer player, String dialogueId, String nodeId, String choiceId) {
        Session session = ACTIVE.get(player.getUUID());
        long now = player.level().getGameTime();
        if (session == null || session.expiresAt() < now) {
            ACTIVE.remove(player.getUUID());
            return;
        }
        if (!session.dialogueId().equals(dialogueId) || !session.nodeId().equals(nodeId)) return;

        DialogueCatalog.DialogueView view = DialogueCatalog.choose(player, dialogueId, session.speaker(), nodeId, choiceId);
        if (view == null) return;
        ACTIVE.put(player.getUUID(), new Session(dialogueId, session.speaker(), view.nodeId(), now + SESSION_LIFETIME_TICKS));
        ModNetwork.sendDialogueView(player, dialogueId, view);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE.remove(event.getEntity().getUUID());
    }
}
