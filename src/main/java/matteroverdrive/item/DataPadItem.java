package matteroverdrive.item;

import matteroverdrive.event.ContractEvents;
import matteroverdrive.event.TechnologyLoreEvents;
import matteroverdrive.matter.MatterValueRegistry;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.quest.FieldOperations;
import matteroverdrive.quest.LegacyStoryContracts;
import matteroverdrive.quest.ResearchCampaignQuestFlow;
import matteroverdrive.quest.ResearchProgression;
import matteroverdrive.quest.ScientistStoryQuestFlow;
import matteroverdrive.world.AmbientLoreCatalog;
import matteroverdrive.world.AmbientLoreSavedData;
import matteroverdrive.world.TechnologyLoreCatalog;
import matteroverdrive.world.TechnologyLoreSavedData;
import matteroverdrive.world.TechnologySiteDiscoverySavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Standalone field console: block scanner plus persistent research-campaign journal. */
public class DataPadItem extends Item {
    public static final int HISTORY_CAPACITY = 16;
    private static final String HISTORY_TAG = "DataPadScanHistory";

    public DataPadItem(Properties properties) { super(properties.stacksTo(1)); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack dataPad = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ModNetwork.openDataPad(serverPlayer, journalLines(serverPlayer, dataPad));
        }
        return InteractionResultHolder.sidedSuccess(dataPad, level.isClientSide);
    }

    /** The Data Pad is the lightweight field/research console without requiring the optional GuideME integration. */
    private static List<String> journalLines(ServerPlayer player, ItemStack dataPad) {
        List<String> lines = new ArrayList<>();
        lines.add("=== RESEARCH PROGRAMME ===");
        lines.add("Current: " + ResearchProgression.status(player));
        lines.add("Assignment: " + (ScientistStoryQuestFlow.complete(player)
                ? ResearchCampaignQuestFlow.status(player)
                : ScientistStoryQuestFlow.status(player)));
        TechnologySiteDiscoverySavedData sites = TechnologySiteDiscoverySavedData.get(player.serverLevel());
        lines.add("Field sites discovered: " + sites.count(player.getUUID()));
        for (String site : sites.siteNames(player.getUUID())) lines.add("  - " + site.replace('_', ' '));

        AmbientLoreSavedData ambient = AmbientLoreSavedData.get(player.serverLevel());
        List<AmbientLoreCatalog.Entry> ambientEntries = ambient.entries(player.getUUID());
        lines.add("Optional field logs: " + ambientEntries.size() + "/" + AmbientLoreCatalog.count());
        if (!ambientEntries.isEmpty()) {
            lines.add("Recently authenticated:");
            int shown = 0;
            for (int i = ambientEntries.size() - 1; i >= 0 && shown < 5; i--, shown++) {
                lines.add("  - " + ambientEntries.get(i).title());
            }
        }

        TechnologyLoreSavedData technology = TechnologyLoreSavedData.get(player.serverLevel());
        List<TechnologyLoreCatalog.TechRecord> technologyEntries = technology.entries(player.getUUID());
        lines.add("Technology codex: " + technologyEntries.size() + "/" + TechnologyLoreCatalog.count());
        if (!technologyEntries.isEmpty()) {
            lines.add("Recently indexed technology:");
            int shown = 0;
            for (int i = technologyEntries.size() - 1; i >= 0 && shown < 5; i--, shown++) {
                lines.add("  - " + technologyEntries.get(i).title());
            }
        }

        CompoundTag encounterResearch = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG)
                .getCompound("MatterOverdriveEncounterResearch");
        lines.add("Encounter evidence: " + encounterResearch.getAllKeys().size() + " faction(s) logged");
        encounterResearch.getAllKeys().stream().sorted()
                .forEach(faction -> lines.add("  - " + faction.replace('_', ' ')));
        int investigationStep = sites.chainStage(player.getUUID());
        if (investigationStep >= sites.chainLength()) {
            lines.add("Investigation: COMPLETE - anomaly evidence archived.");
        } else {
            String nextSite = TechnologySiteDiscoverySavedData.nextChainSite(player.getUUID(), investigationStep)
                    .replace('_', ' ');
            lines.add("Investigation " + investigationStep + "/" + sites.chainLength() + ": next lead - " + nextSite);
        }

        lines.add("--- Recovered Logs ---");
        for (AmbientLoreCatalog.Entry entry : ambientEntries) lines.add("@lore:" + entry.id());

        lines.add("--- Technology Codex ---");
        for (TechnologyLoreCatalog.TechRecord entry : technologyEntries) lines.add("@tech:" + entry.id());

        lines.add("--- Field Operations ---");
        lines.add(FieldOperations.status(player));
        lines.add("Completed operations: " + FieldOperations.completions(player));
        lines.add("Assign/change doctrine with /matteroverdrive research field assign <recovery|systems|anomaly>.");
        lines.add("--- Progression ---");
        lines.addAll(ResearchProgression.roadmap(player));
        List<String> history = getHistory(dataPad);
        if (!history.isEmpty()) {
            lines.add("--- Scan History ---");
            lines.addAll(history);
        }
        return lines;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        ItemStack dataPad = context.getItemInHand();
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        // Scanning an intact MO machine counts as genuine first discovery even if the player has
        // not dismantled or crafted it yet. The shared discovery path deduplicates the record.
        if (context.getPlayer() instanceof ServerPlayer scanningPlayer) {
            TechnologyLoreEvents.discoverTechnology(scanningPlayer, blockId);
        }

        if (isLegacyQuestScanner(dataPad) && context.getPlayer() instanceof ServerPlayer serverPlayer && isLegacyScanTarget(blockId)) {
            ItemStack blockItem = new ItemStack(state.getBlock().asItem());
            int matter = MatterValueRegistry.getMatter(blockItem);
            String displayName = blockItem.isEmpty() ? state.getBlock().getName().getString() : blockItem.getHoverName().getString();
            String entry = displayName + " | " + blockId + " | " + matter + " kM";
            record(dataPad, entry);
            ContractEvents.recordScan(serverPlayer, blockId);
            if (state.getDestroySpeed(context.getLevel(), context.getClickedPos()) >= 0.0F) context.getLevel().destroyBlock(context.getClickedPos(), false, serverPlayer);
            serverPlayer.sendSystemMessage(Component.literal("Research sample recorded: " + displayName).withStyle(ChatFormatting.LIGHT_PURPLE));
            serverPlayer.getInventory().setChanged();
            serverPlayer.inventoryMenu.broadcastChanges();
            return InteractionResult.CONSUME;
        }

        ItemStack blockItem = new ItemStack(state.getBlock().asItem());
        int matter = MatterValueRegistry.getMatter(blockItem);
        String displayName = blockItem.isEmpty() ? state.getBlock().getName().getString() : blockItem.getHoverName().getString();
        String entry = displayName + " | " + blockId + " | " + matter + " kM";
        record(dataPad, entry);
        if (context.getPlayer() != null) {
            context.getPlayer().sendSystemMessage(Component.literal("Data Pad recorded: " + entry).withStyle(matter > 0 ? ChatFormatting.AQUA : ChatFormatting.GRAY));
            if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                serverPlayer.getInventory().setChanged();
                serverPlayer.inventoryMenu.broadcastChanges();
            }
        }
        return InteractionResult.CONSUME;
    }

    private static boolean isLegacyScanTarget(ResourceLocation id) {
        if (id == null) return false;
        String value = id.toString();
        return value.equals("minecraft:carrots") || value.equals("minecraft:potatoes") || value.equals("minecraft:wheat");
    }

    public static boolean isLegacyQuestScanner(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.hasTag() && stack.getOrCreateTag().getBoolean(LegacyStoryContracts.LEGACY_SCAN_PAD);
    }

    private static void record(ItemStack dataPad, String entry) {
        ListTag previous = dataPad.getOrCreateTag().getList(HISTORY_TAG, Tag.TAG_STRING);
        ListTag next = new ListTag();
        next.add(StringTag.valueOf(entry));
        for (int i = 0; i < previous.size() && next.size() < HISTORY_CAPACITY; i++) {
            String old = previous.getString(i);
            if (!old.equals(entry)) next.add(StringTag.valueOf(old));
        }
        dataPad.getOrCreateTag().put(HISTORY_TAG, next);
    }

    public static List<String> getHistory(ItemStack dataPad) {
        List<String> history = new ArrayList<>();
        CompoundTag root = dataPad.getOrCreateTag();
        ListTag list = root.getList(HISTORY_TAG, Tag.TAG_STRING);
        for (int i = 0; i < list.size() && i < HISTORY_CAPACITY; i++) history.add(list.getString(i));
        return history;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        List<String> history = getHistory(stack);
        if (isLegacyQuestScanner(stack)) {
            tooltip.add(Component.literal("Mad Scientist research scanner").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.literal("Scans and destroys Wheat, Carrots and Potatoes; advances matching research quests.").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("Standalone field console, research journal and block scan history").withStyle(ChatFormatting.AQUA));
        }
        tooltip.add(Component.literal("Recorded blocks: " + history.size() + "/" + HISTORY_CAPACITY).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Incident archives, technology discoveries, field operations and encounter evidence are shown in the journal.").withStyle(ChatFormatting.DARK_AQUA));
        if (!history.isEmpty()) tooltip.add(Component.literal("Latest: " + history.get(0)).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Use on a block to record it; use in air to open the field console.").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
