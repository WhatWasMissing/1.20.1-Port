package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.dialogue.ContactQuestServices;
import matteroverdrive.dialogue.FactionReputation;
import matteroverdrive.quest.FieldOperations;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Awards state-driven mastery advancements that cannot be expressed as inventory triggers. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ImmersiveProgressionAdvancements {
    private ImmersiveProgressionAdvancements() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 100 != 0) return;

        int trusted = 0;
        boolean allChains = true;
        for (FactionReputation.Faction faction : FactionReputation.Faction.values()) {
            if (FactionReputation.tier(player, faction).atLeast(FactionReputation.Tier.TRUSTED)) trusted++;
            if (ContactQuestServices.stage(player, faction) < 3) allChains = false;
        }
        if (trusted >= 3) grant(player, "coalition_builder");
        if (allChains) grant(player, "network_of_trust");
        if (FieldOperations.completions(player) >= 10) grant(player, "field_veteran");
    }

    private static void grant(ServerPlayer player, String path) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(
                ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, "campaign/" + path));
        if (advancement == null || player.getAdvancements().getOrStartProgress(advancement).isDone()) return;
        for (String criterion : advancement.getCriteria().keySet()) player.getAdvancements().award(advancement, criterion);
    }
}
