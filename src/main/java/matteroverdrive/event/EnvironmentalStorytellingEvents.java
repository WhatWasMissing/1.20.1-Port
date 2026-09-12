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
 * Interprets existing structure scenery without adding blocks or changing traversal.
 * Observations are sparse action-bar notes while the player explores a valid MO structure.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EnvironmentalStorytellingEvents {
    private static final int SCAN_INTERVAL = 100;
    private static final long REPEAT_DELAY = 20L * 45L;
    private static final Map<UUID, String> LAST_SITE = new HashMap<>();
    private static final Map<UUID, Long> LAST_OBSERVATION = new HashMap<>();

    private EnvironmentalStorytellingEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % SCAN_INTERVAL != 0 || !(player.level() instanceof ServerLevel level)) return;

        String site = null;
        for (StructureLoreCatalog.LoreRecord record : StructureLoreCatalog.records()) {
            StructureStart start = level.structureManager().getStructureWithPieceAt(player.blockPosition(), record.structureKey());
            if (start != null && start.isValid()) {
                site = record.id();
                break;
            }
        }

        UUID id = player.getUUID();
        if (site == null) {
            LAST_SITE.remove(id);
            return;
        }

        String previous = LAST_SITE.put(id, site);
        long now = level.getGameTime();
        long last = LAST_OBSERVATION.getOrDefault(id, Long.MIN_VALUE / 2L);
        boolean entered = !site.equals(previous);
        if (!entered && now - last < REPEAT_DELAY) return;

        String observation = EnvironmentalStorytellingCatalog.observation(site, player.blockPosition());
        if (observation.isBlank()) return;
        LAST_OBSERVATION.put(id, now);
        player.displayClientMessage(Component.literal("FIELD OBSERVATION // " + observation)
                .withStyle(ChatFormatting.DARK_AQUA), true);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        LAST_SITE.remove(id);
        LAST_OBSERVATION.remove(id);
    }
}
