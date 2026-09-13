package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.registry.ModItems;
import matteroverdrive.world.FrontierExpeditionSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-authoritative archive for Frontier Expedition site classes.
 * New entries are authenticated by player-event evidence; native structure
 * serializers remain available only for compatibility with existing saves. The
 * legacy structure scan is deliberately a fallback for those saves, not a new
 * placement or progression route.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FrontierExpeditionEvents {
    private static final Map<UUID, Long> LAST_LEGACY_SCAN = new HashMap<>();
    private static final ResourceKey<Structure> DEEP_MATTER_VAULT = key("deep_matter_vault");
    private static final ResourceKey<Structure> AUTONOMOUS_DRONE_FOUNDRY = key("autonomous_drone_foundry");
    private static final ResourceKey<Structure> ANOMALY_QUARANTINE_SITE = key("anomaly_quarantine_site");
    private static final ResourceKey<Structure> ORBITAL_RECOVERY_ARRAY = key("orbital_recovery_array");

    private FrontierExpeditionEvents() {}

    @SubscribeEvent
    public static void legacyPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;
        long chunk = player.chunkPosition().toLong();
        Long previous = LAST_LEGACY_SCAN.put(player.getUUID(), chunk);
        if (previous != null && previous == chunk) return;
        legacyStructureScan(player);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_LEGACY_SCAN.remove(event.getEntity().getUUID());
    }

    public static boolean discoverFromPlayerEvent(ServerPlayer player, String site) {
        int bit = bitFor(site);
        if (player == null || bit == 0 || !(player.level() instanceof ServerLevel level)) return false;

        // A deterministic per-player/site anchor keeps the existing ledger format while
        // ensuring an event route records one class once, without inventing another store.
        long anchor = player.getUUID().getMostSignificantBits() ^ player.getUUID().getLeastSignificantBits()
                ^ site.hashCode();
        FrontierExpeditionSavedData ledger = FrontierExpeditionSavedData.get(level);
        FrontierExpeditionSavedData.DiscoveryResult result = ledger.discover(
                level, player.getUUID(), site, bit, anchor);
        if (result == FrontierExpeditionSavedData.DiscoveryResult.REJECTED
                || result == FrontierExpeditionSavedData.DiscoveryResult.ALREADY_LOGGED) return false;

        int unique = ledger.uniqueSiteCount(player.getUUID());
        player.giveExperiencePoints(result == FrontierExpeditionSavedData.DiscoveryResult.NEW_LOCATION ? 15 : 40);
        ItemStack dossier = new ItemStack(ModItems.get("facility_research").get());
        dossier.getOrCreateTag().putString("FrontierArchive", site.toUpperCase(java.util.Locale.ROOT));
        dossier.getOrCreateTag().putInt("FrontierProgress", unique);
        if (!player.getInventory().add(dossier)) player.drop(dossier, false);

        player.sendSystemMessage(Component.literal("FRONTIER ARCHIVE: " + readable(site) + " logged (" + unique + "/4 site classes).")
                .withStyle(ChatFormatting.AQUA));
        if (result == FrontierExpeditionSavedData.DiscoveryResult.NEW_LOCATION) {
            player.sendSystemMessage(Component.literal("Additional field evidence added to the expedition archive.")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (result == FrontierExpeditionSavedData.DiscoveryResult.EXPEDITION_COMPLETE) {
            ItemStack reward = new ItemStack(ModItems.get("upgrade_parallel_processing").get());
            if (!player.getInventory().add(reward)) player.drop(reward, false);
            player.giveExperiencePoints(120);
            player.sendSystemMessage(Component.literal("FRONTIER ARCHIVE COMPLETE: all four facility classes authenticated. Parallel Processing Upgrade recovered.")
                    .withStyle(ChatFormatting.GOLD));
        }
        return true;
    }

    private static int bitFor(String site) {
        return switch (site == null ? "" : site) {
            case "deep_matter_vault" -> 1;
            case "autonomous_drone_foundry" -> 2;
            case "anomaly_quarantine_site" -> 4;
            case "orbital_recovery_array" -> 8;
            default -> 0;
        };
    }

    /** Existing generated structures only; new worlds have no placement data for these keys. */
    private static void legacyStructureScan(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        BlockPos pos = player.blockPosition();
        if (legacyMatch(level, pos, DEEP_MATTER_VAULT)) {
            discoverFromPlayerEvent(player, "deep_matter_vault");
        } else if (legacyMatch(level, pos, AUTONOMOUS_DRONE_FOUNDRY)) {
            discoverFromPlayerEvent(player, "autonomous_drone_foundry");
        } else if (legacyMatch(level, pos, ANOMALY_QUARANTINE_SITE)) {
            discoverFromPlayerEvent(player, "anomaly_quarantine_site");
        } else if (legacyMatch(level, pos, ORBITAL_RECOVERY_ARRAY)) {
            discoverFromPlayerEvent(player, "orbital_recovery_array");
        }
    }

    private static boolean legacyMatch(ServerLevel level, BlockPos pos, ResourceKey<Structure> key) {
        StructureStart start = level.structureManager().getStructureWithPieceAt(pos, key);
        return start != null && start.isValid();
    }

    private static ResourceKey<Structure> key(String path) {
        return ResourceKey.create(Registries.STRUCTURE,
                ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, path));
    }

    private static String readable(String id) {
        String[] words = id.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (result.length() > 0) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
}
