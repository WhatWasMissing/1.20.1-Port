package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.registry.ModItems;
import matteroverdrive.world.StructureLoreCatalog;
import matteroverdrive.world.StructureLoreCatalog.LoreRecord;
import matteroverdrive.world.StructureLoreSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * One-time narrative discovery layer for all sixteen Matter Overdrive structures.
 * The records form one connected, non-linear campaign arc: The Overdrive Incident.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StructureLoreEvents {
    private static final Map<UUID, Long> LAST_SCANNED_CHUNK = new HashMap<>();

    private StructureLoreEvents() {}

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
        for (LoreRecord record : StructureLoreCatalog.records()) {
            StructureStart start = level.structureManager().getStructureWithPieceAt(pos, record.structureKey());
            if (start == null || !start.isValid()) continue;

            StructureLoreSavedData data = StructureLoreSavedData.get(level);
            if (!data.discover(player.getUUID(), record.mask())) return;

            ItemStack dossier = new ItemStack(ModItems.get("facility_research").get());
            dossier.setHoverName(Component.literal(record.title()).withStyle(ChatFormatting.AQUA));
            CompoundTag tag = dossier.getOrCreateTag();
            tag.putString("LoreArc", "OVERDRIVE_INCIDENT");
            tag.putString("LoreSite", record.id().toUpperCase(java.util.Locale.ROOT));
            tag.putString("LoreChapter", record.chapter());
            tag.putString("LoreFacility", record.facility());
            tag.putString("LoreTitle", record.title());
            tag.putString("LoreAuthor", record.author());
            tag.putString("LoreSummary", record.summary());
            tag.putString("LoreLink", record.link());
            tag.putInt("ArchiveIndex", record.archiveIndex());
            tag.putInt("LoreProgress", data.count(player.getUUID()));

            CompoundTag display = tag.getCompound("display");
            ListTag lore = new ListTag();
            lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(
                    "Archive Entry " + record.archiveIndex() + "/" + StructureLoreCatalog.RECORD_COUNT)
                    .withStyle(ChatFormatting.DARK_AQUA))));
            lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(record.author()).withStyle(ChatFormatting.GRAY))));
            lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(record.summary()).withStyle(ChatFormatting.WHITE))));
            lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(record.link()).withStyle(ChatFormatting.DARK_AQUA))));
            display.put("Lore", lore);
            tag.put("display", display);

            if (!player.getInventory().add(dossier)) player.drop(dossier, false);
            int count = data.count(player.getUUID());
            player.sendSystemMessage(Component.literal("RECOVERED RECORD: " + record.title()
                            + " | Archive " + record.archiveIndex() + "/" + StructureLoreCatalog.RECORD_COUNT
                            + " | Reconstruction " + count + "/" + StructureLoreCatalog.RECORD_COUNT)
                    .withStyle(ChatFormatting.AQUA));
            if (data.complete(player.getUUID())) {
                player.giveExperiencePoints(500);
                ItemStack reward = new ItemStack(ModItems.get("artifact").get());
                reward.setHoverName(Component.literal("Closed Loop Artifact").withStyle(ChatFormatting.GOLD));
                reward.getOrCreateTag().putString("LoreArc", "OVERDRIVE_INCIDENT_COMPLETE");
                if (!player.getInventory().add(reward)) player.drop(reward, false);
                player.sendSystemMessage(Component.literal("THE OVERDRIVE INCIDENT: archive complete. The ORPHEUS chain of events has been reconstructed.")
                        .withStyle(ChatFormatting.GOLD));
            }
            return;
        }
    }
}
