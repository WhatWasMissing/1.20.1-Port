package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.registry.ModItems;
import matteroverdrive.world.StructureLoreCatalog;
import matteroverdrive.world.StructureLoreCatalog.LoreRecord;
import matteroverdrive.world.StructureLoreCatalog.Reconstruction;
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
            int oldMask = data.mask(player.getUUID());
            if (!data.discover(player.getUUID(), record.mask())) return;
            int newMask = data.mask(player.getUUID());

            ItemStack dossier = createDossier(record, data.count(player.getUUID()));
            if (!player.getInventory().add(dossier)) player.drop(dossier, false);

            int count = data.count(player.getUUID());
            player.sendSystemMessage(Component.literal("RECOVERED RECORD: " + record.title()
                            + " | Archive " + record.archiveIndex() + "/" + StructureLoreCatalog.RECORD_COUNT
                            + " | Reconstruction " + count + "/" + StructureLoreCatalog.RECORD_COUNT)
                    .withStyle(ChatFormatting.AQUA));
            player.sendSystemMessage(Component.literal(record.timestamp() + " // " + record.chapter())
                    .withStyle(ChatFormatting.DARK_AQUA));

            announceNewReconstructions(player, oldMask, newMask);

            if (data.complete(player.getUUID())) {
                player.giveExperiencePoints(500);
                ItemStack reward = createClosedLoopArtifact();
                if (!player.getInventory().add(reward)) player.drop(reward, false);
                player.sendSystemMessage(Component.literal(
                                "THE OVERDRIVE INCIDENT: archive complete. THE CLOSED LOOP reconstruction is now authenticated.")
                        .withStyle(ChatFormatting.GOLD));
                player.sendSystemMessage(Component.literal("Standing instruction retained: DO NOT COMPLETE THE LOOP.")
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }
    }

    private static ItemStack createDossier(LoreRecord record, int progress) {
        ItemStack dossier = new ItemStack(ModItems.get("facility_research").get());
        dossier.setHoverName(Component.literal(record.title()).withStyle(ChatFormatting.AQUA));
        CompoundTag tag = dossier.getOrCreateTag();
        tag.putString("LoreArc", "OVERDRIVE_INCIDENT");
        tag.putString("LoreSite", record.id().toUpperCase(java.util.Locale.ROOT));
        tag.putString("LoreChapter", record.chapter());
        tag.putString("LoreTimestamp", record.timestamp());
        tag.putString("LoreClassification", record.classification());
        tag.putString("LoreFacility", record.facility());
        tag.putString("LoreTitle", record.title());
        tag.putString("LoreAuthor", record.author());
        tag.putString("LoreSitePurpose", record.sitePurpose());
        tag.putString("LoreSummary", record.summary());
        tag.putString("LoreExcerpt", record.excerpt());
        tag.putString("LoreAnalysis", record.analysis());
        tag.putString("LoreImplication", record.implication());
        tag.putString("LoreLink", record.link());
        tag.putInt("ArchiveIndex", record.archiveIndex());
        tag.putInt("LoreProgress", progress);

        CompoundTag display = tag.getCompound("display");
        ListTag lore = new ListTag();
        lore.add(loreLine("Archive Entry " + record.archiveIndex() + "/" + StructureLoreCatalog.RECORD_COUNT,
                ChatFormatting.DARK_AQUA));
        lore.add(loreLine(record.timestamp() + " | " + record.classification(), ChatFormatting.GRAY));
        lore.add(loreLine(record.author(), ChatFormatting.GRAY));
        lore.add(loreLine(record.summary(), ChatFormatting.WHITE));
        lore.add(loreLine("FIELD ANALYSIS: " + record.implication(), ChatFormatting.LIGHT_PURPLE));
        lore.add(loreLine(record.link(), ChatFormatting.DARK_AQUA));
        display.put("Lore", lore);
        tag.put("display", display);
        return dossier;
    }

    private static StringTag loreLine(String text, ChatFormatting style) {
        return StringTag.valueOf(Component.Serializer.toJson(Component.literal(text).withStyle(style)));
    }

    private static void announceNewReconstructions(ServerPlayer player, int oldMask, int newMask) {
        for (Reconstruction reconstruction : StructureLoreCatalog.reconstructions()) {
            boolean wasUnlocked = StructureLoreCatalog.reconstructionUnlocked(oldMask, reconstruction);
            boolean isUnlocked = StructureLoreCatalog.reconstructionUnlocked(newMask, reconstruction);
            if (wasUnlocked || !isUnlocked) continue;
            player.giveExperiencePoints(75);
            player.sendSystemMessage(Component.literal("ARCHIVE RECONSTRUCTION COMPLETE: " + reconstruction.title())
                    .withStyle(ChatFormatting.GOLD));
            player.sendSystemMessage(Component.literal(reconstruction.subtitle()).withStyle(ChatFormatting.YELLOW));
        }
    }

    private static ItemStack createClosedLoopArtifact() {
        ItemStack reward = new ItemStack(ModItems.get("artifact").get());
        reward.setHoverName(Component.literal("Closed Loop Artifact").withStyle(ChatFormatting.GOLD));
        CompoundTag tag = reward.getOrCreateTag();
        tag.putString("LoreArc", "OVERDRIVE_INCIDENT_COMPLETE");
        tag.putString("LoreTitle", "THE CLOSED LOOP");
        tag.putString("LoreOriginTheory",
                "ICARUS debris was displaced into the past and became at least part of SAMPLE M-0, creating a causal bootstrap.");
        tag.putString("LoreWarning", "DO NOT COMPLETE THE LOOP");

        CompoundTag display = tag.getCompound("display");
        ListTag lore = new ListTag();
        lore.add(loreLine("Authenticated reconstruction artifact", ChatFormatting.GOLD));
        lore.add(loreLine("M-0 -> OVERDRIVE -> ICARUS -> LAGRANGE -> M-0", ChatFormatting.LIGHT_PURPLE));
        lore.add(loreLine("No external origin identified.", ChatFormatting.GRAY));
        lore.add(loreLine("DO NOT COMPLETE THE LOOP", ChatFormatting.RED));
        display.put("Lore", lore);
        tag.put("display", display);
        return reward;
    }
}
