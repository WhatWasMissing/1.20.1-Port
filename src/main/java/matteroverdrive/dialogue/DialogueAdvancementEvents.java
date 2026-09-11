package matteroverdrive.dialogue;

import matteroverdrive.MatterOverdrive;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Bridges persistent conversation reputation into data-driven campaign advancements. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DialogueAdvancementEvents {
    private DialogueAdvancementEvents() {}

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 40 != 0) return;

        DialogueStateSavedData state = DialogueStateSavedData.get(player.serverLevel());
        if (state.fieldTrust(player.getUUID()) >= 8) grant(player, "field_liaison");
        if (state.syntheticTrust(player.getUUID()) >= 8) grant(player, "synthetic_liaison");
        if (state.archiveInsight(player.getUUID()) >= 18) grant(player, "incident_analyst");
    }

    private static void grant(ServerPlayer player, String path) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(
                ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, "campaign/" + path));
        if (advancement == null || player.getAdvancements().getOrStartProgress(advancement).isDone()) return;
        for (String criterion : advancement.getCriteria().keySet()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
