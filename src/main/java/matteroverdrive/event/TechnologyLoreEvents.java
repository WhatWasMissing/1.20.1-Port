package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.world.TechnologyLoreCatalog;
import matteroverdrive.world.TechnologyLoreSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Authenticates major Matter Overdrive technology into the PDA on first real acquisition/use.
 * Crafting, pickup and placement are immediate; Data Pad inspection can authenticate intact world
 * technology, and a conservative periodic inventory audit catches container transfers, machine
 * outputs, commands and creative acquisition without adding another network packet.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TechnologyLoreEvents {
    private static final int INVENTORY_SCAN_INTERVAL = 40;

    private TechnologyLoreEvents() {}

    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) discoverStack(player, event.getCrafting());
    }

    @SubscribeEvent
    public static void onPickup(EntityItemPickupEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) discoverStack(player, event.getItem().getItem());
    }

    @SubscribeEvent
    public static void onPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(event.getPlacedBlock().getBlock());
        discoverTechnology(player, id);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % INVENTORY_SCAN_INTERVAL != 0) return;

        // Only emit one new discovery per audit. This avoids a returning player with a mature
        // inventory dumping dozens of voice messages into the serialized PDA queue at once.
        for (ItemStack stack : player.getInventory().items) if (discoverStack(player, stack)) return;
        for (ItemStack stack : player.getInventory().offhand) if (discoverStack(player, stack)) return;
        for (ItemStack stack : player.getInventory().armor) if (discoverStack(player, stack)) return;
    }

    private static boolean discoverStack(ServerPlayer player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return discoverTechnology(player, id);
    }

    /** Shared entry point for explicit inspection paths such as Data Pad block scanning. */
    public static boolean discoverTechnology(ServerPlayer player, ResourceLocation id) {
        if (player == null || id == null || !MatterOverdrive.MOD_ID.equals(id.getNamespace())) return false;
        return discoverTechnology(player, id.getPath());
    }

    public static boolean discoverTechnology(ServerPlayer player, String itemId) {
        if (player == null || itemId == null || itemId.isBlank()) return false;
        TechnologyLoreCatalog.TechRecord record = TechnologyLoreCatalog.byItemId(itemId);
        if (record == null) return false;
        TechnologyLoreSavedData data = TechnologyLoreSavedData.get(player.serverLevel());
        boolean firstTechnology = data.discover(player.getUUID(), itemId);
        boolean firstArchiveRecord = StructureLoreEvents.discoverFromTechnology(player, itemId);
        if (!firstTechnology) return firstArchiveRecord;

        int recovered = data.count(player.getUUID());
        player.sendSystemMessage(Component.literal("PDA // TECHNOLOGY INDEXED: " + record.title())
                .withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal(record.function()).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("ARCHIVE CONTEXT: " + record.lore())
                .withStyle(ChatFormatting.DARK_AQUA));
        player.sendSystemMessage(Component.literal("Technology Codex " + recovered + "/" + TechnologyLoreCatalog.count())
                .withStyle(ChatFormatting.GREEN));
        ModNetwork.sendPdaVoice(player, record.voiceId());
        return true;
    }
}
