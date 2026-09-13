package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.world.EnvironmentalStorytellingCatalog;
import matteroverdrive.world.StructureLoreCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Interprets recovered evidence without adding blocks or changing traversal.
 * Observations are sparse action-bar notes emitted by player-event discoveries.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EnvironmentalStorytellingEvents {
    private static final int LEGACY_SCAN_INTERVAL = 100;
    private static final long REPEAT_DELAY = 20L * 45L;
    private static final Map<UUID, Long> LAST_OBSERVATION = new HashMap<>();

    private EnvironmentalStorytellingEvents() {}

    @SubscribeEvent
    public static void legacyStructureObservation(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % LEGACY_SCAN_INTERVAL != 0 || !(player.level() instanceof ServerLevel level)) return;
        for (StructureLoreCatalog.LoreRecord record : StructureLoreCatalog.records()) {
            StructureStart start = level.structureManager().getStructureWithPieceAt(player.blockPosition(), record.structureKey());
            if (start != null && start.isValid()) {
                observeFromPlayerEvent(player, record.id());
                return;
            }
        }
    }

    public static void observeFromPlayerEvent(ServerPlayer player, String site) {
        if (player == null || !EnvironmentalStorytellingCatalog.hasSite(site)) return;
        UUID id = player.getUUID();
        long now = player.serverLevel().getGameTime();
        long last = LAST_OBSERVATION.getOrDefault(id, Long.MIN_VALUE / 2L);
        if (now - last < REPEAT_DELAY) return;
        String observation = EnvironmentalStorytellingCatalog.observation(site, player.blockPosition());
        if (observation.isBlank()) return;
        LAST_OBSERVATION.put(id, now);
        player.displayClientMessage(Component.literal("FIELD OBSERVATION // " + observation)
                .withStyle(ChatFormatting.DARK_AQUA), true);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        LAST_OBSERVATION.remove(id);
    }
}
