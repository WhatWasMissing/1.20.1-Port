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
 * Server-authoritative discovery loop for Frontier Expedition sites.
 * Detection queries Minecraft's native structure manager only when the player
 * crosses into another chunk, avoiding block-radius scans and false positives
 * against older Matter Overdrive facilities.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FrontierExpeditionEvents {
    private static final Map<UUID, Long> LAST_SCANNED_CHUNK = new HashMap<>();
    private static final ResourceKey<Structure> DEEP_MATTER_VAULT = key("deep_matter_vault");
    private static final ResourceKey<Structure> AUTONOMOUS_DRONE_FOUNDRY = key("autonomous_drone_foundry");
    private static final ResourceKey<Structure> ANOMALY_QUARANTINE_SITE = key("anomaly_quarantine_site");
    private static final ResourceKey<Structure> ORBITAL_RECOVERY_ARRAY = key("orbital_recovery_array");

    private FrontierExpeditionEvents() {}

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;
        long chunk = player.chunkPosition().toLong();
        Long previous = LAST_SCANNED_CHUNK.put(player.getUUID(), chunk);
        if (previous != null && previous == chunk) return;
        scan(player);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_SCANNED_CHUNK.remove(event.getEntity().getUUID());
    }

    private static void scan(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        BlockPos pos = player.blockPosition();
        SiteMatch match = match(level, pos, DEEP_MATTER_VAULT, "deep_matter_vault", 1);
        if (match == null) match = match(level, pos, AUTONOMOUS_DRONE_FOUNDRY, "autonomous_drone_foundry", 2);
        if (match == null) match = match(level, pos, ANOMALY_QUARANTINE_SITE, "anomaly_quarantine_site", 4);
        if (match == null) match = match(level, pos, ORBITAL_RECOVERY_ARRAY, "orbital_recovery_array", 8);
        if (match == null) return;

        FrontierExpeditionSavedData ledger = FrontierExpeditionSavedData.get(level);
        FrontierExpeditionSavedData.DiscoveryResult result = ledger.discover(
                level, player.getUUID(), match.site(), match.bit(), match.startChunk());
        if (result == FrontierExpeditionSavedData.DiscoveryResult.REJECTED
                || result == FrontierExpeditionSavedData.DiscoveryResult.ALREADY_LOGGED) return;

        int unique = ledger.uniqueSiteCount(player.getUUID());
        player.giveExperiencePoints(result == FrontierExpeditionSavedData.DiscoveryResult.NEW_LOCATION ? 15 : 40);
        ItemStack dossier = new ItemStack(ModItems.get("facility_research").get());
        dossier.getOrCreateTag().putString("FrontierArchive", match.site().toUpperCase(java.util.Locale.ROOT));
        dossier.getOrCreateTag().putInt("FrontierProgress", unique);
        if (!player.getInventory().add(dossier)) player.drop(dossier, false);

        player.sendSystemMessage(Component.literal("FRONTIER EXPEDITION: " + readable(match.site()) + " logged (" + unique + "/4 unique sites).")
                .withStyle(ChatFormatting.AQUA));
        if (result == FrontierExpeditionSavedData.DiscoveryResult.NEW_LOCATION) {
            player.sendSystemMessage(Component.literal("Additional site coordinates added to the expedition archive.")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (result == FrontierExpeditionSavedData.DiscoveryResult.EXPEDITION_COMPLETE) {
            ItemStack reward = new ItemStack(ModItems.get("upgrade_parallel_processing").get());
            if (!player.getInventory().add(reward)) player.drop(reward, false);
            player.giveExperiencePoints(120);
            player.sendSystemMessage(Component.literal("FRONTIER EXPEDITION COMPLETE: all four facility classes catalogued. Parallel Processing Upgrade recovered.")
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    private static SiteMatch match(ServerLevel level, BlockPos pos, ResourceKey<Structure> key, String site, int bit) {
        StructureStart start = level.structureManager().getStructureWithPieceAt(pos, key);
        if (start == null || !start.isValid()) return null;
        return new SiteMatch(site, bit, start.getChunkPos().toLong());
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

    private record SiteMatch(String site, int bit, long startChunk) {}
}
