package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.registry.ModItems;
import matteroverdrive.world.FrontierExpeditionSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bounded, server-authoritative discovery loop for Frontier Expedition sites.
 * Detection is signature-based so damaged variants remain discoverable even if
 * individual decorative blocks are missing.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FrontierExpeditionEvents {
    private static final Map<UUID, Long> LAST_SCANNED_CHUNK = new HashMap<>();

    private FrontierExpeditionEvents() {}

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;
        long chunk = ((long) player.chunkPosition().x << 32) ^ (player.chunkPosition().z & 0xffffffffL);
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
        boolean controller = false;
        boolean matrix = false;
        boolean redCrate = false;
        boolean charger = false;
        boolean containment = false;
        boolean stabilizer = false;
        boolean quantumRelay = false;
        boolean networkSwitch = false;
        BlockPos anchor = null;

        BlockPos centre = player.blockPosition();
        for (BlockPos mutable : BlockPos.betweenClosed(centre.offset(-18, -8, -18), centre.offset(18, 8, 18))) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(level.getBlockState(mutable).getBlock());
            if (id == null || !MatterOverdrive.MOD_ID.equals(id.getNamespace())) continue;
            String path = id.getPath();
            switch (path) {
                case "facility_network_controller" -> { controller = true; if (anchor == null) anchor = mutable.immutable(); }
                case "matter_storage_matrix" -> matrix = true;
                case "tritanium_crate_red" -> redCrate = true;
                case "charging_station" -> charger = true;
                case "anomaly_containment_unit" -> { containment = true; if (anchor == null) anchor = mutable.immutable(); }
                case "gravitational_stabilizer" -> stabilizer = true;
                case "quantum_power_relay" -> { quantumRelay = true; if (anchor == null) anchor = mutable.immutable(); }
                case "network_switch" -> networkSwitch = true;
                default -> { }
            }
        }

        String site = null;
        int bit = 0;
        // Signatures are deliberately concentrated around each site's control/core room.
        // This keeps discovery bounded while avoiding dependence on decorative blocks.
        if (controller && matrix && redCrate) { site = "deep_matter_vault"; bit = 1; }
        else if (controller && charger && !containment) { site = "autonomous_drone_foundry"; bit = 2; }
        else if (containment && stabilizer && controller) { site = "anomaly_quarantine_site"; bit = 4; }
        else if (quantumRelay && networkSwitch && controller) { site = "orbital_recovery_array"; bit = 8; }
        if (site == null || anchor == null) return;

        FrontierExpeditionSavedData ledger = FrontierExpeditionSavedData.get(level);
        FrontierExpeditionSavedData.DiscoveryResult result = ledger.discover(level, player.getUUID(), site, bit, anchor.asLong());
        if (result == FrontierExpeditionSavedData.DiscoveryResult.REJECTED
                || result == FrontierExpeditionSavedData.DiscoveryResult.ALREADY_LOGGED) return;

        int unique = ledger.uniqueSiteCount(player.getUUID());
        player.giveExperiencePoints(result == FrontierExpeditionSavedData.DiscoveryResult.NEW_LOCATION ? 15 : 40);
        ItemStack dossier = new ItemStack(ModItems.get("facility_research").get());
        dossier.getOrCreateTag().putString("FrontierArchive", site.toUpperCase(java.util.Locale.ROOT));
        dossier.getOrCreateTag().putInt("FrontierProgress", unique);
        if (!player.getInventory().add(dossier)) player.drop(dossier, false);

        player.sendSystemMessage(Component.literal("FRONTIER EXPEDITION: " + readable(site) + " logged (" + unique + "/4 unique sites).")
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
