package matteroverdrive.item;

import matteroverdrive.event.ContractEvents;
import matteroverdrive.matter.MatterValueRegistry;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.progression.PlayerDiscoveryLog;
import matteroverdrive.quest.LegacyStoryContracts;
import matteroverdrive.quest.ResearchCampaignQuestFlow;
import matteroverdrive.quest.ResearchProgression;
import matteroverdrive.quest.ScientistStoryQuestFlow;
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

/** Compact PDA: player-driven research log, scanner and contract status. */
public class DataPadItem extends Item {
    public static final int HISTORY_CAPACITY = 12;
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

    private static List<String> journalLines(ServerPlayer player, ItemStack dataPad) {
        List<String> lines = new ArrayList<>();
        lines.add("STATUS // " + ResearchProgression.status(player));
        lines.add("ASSIGNMENT // " + (ScientistStoryQuestFlow.complete(player)
                ? ResearchCampaignQuestFlow.status(player)
                : ScientistStoryQuestFlow.status(player)));
        List<String> discoveries = PlayerDiscoveryLog.lines(player);
        if (discoveries.isEmpty()) {
            lines.add("PDA // No experimental discoveries yet. Craft, scan, dismantle and test Matter Overdrive technology.");
        } else {
            lines.add("--- PERSONAL DISCOVERIES ---");
            lines.addAll(discoveries);
        }
        List<String> scans = getHistory(dataPad);
        if (!scans.isEmpty()) {
            lines.add("--- RECENT SCANS ---");
            lines.addAll(scans);
        }
        return lines;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        ItemStack dataPad = context.getItemInHand();
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        ItemStack blockItem = new ItemStack(state.getBlock().asItem());
        int matter = MatterValueRegistry.getMatter(blockItem);
        String displayName = blockItem.isEmpty() ? state.getBlock().getName().getString() : blockItem.getHoverName().getString();
        String entry = displayName + " | " + blockId + " | " + matter + " kM";
        record(dataPad, entry);

        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            ContractEvents.recordScan(serverPlayer, blockId);
            PlayerDiscoveryLog.record(serverPlayer, "scan:" + blockId,
                    "ANALYSIS // " + displayName + " scanned. Matter value measured at " + matter + " kM.");
            if (isLegacyQuestScanner(dataPad) && isLegacyScanTarget(blockId)) {
                if (state.getDestroySpeed(context.getLevel(), context.getClickedPos()) >= 0.0F) context.getLevel().destroyBlock(context.getClickedPos(), false, serverPlayer);
                serverPlayer.sendSystemMessage(Component.literal("Research sample recorded: " + displayName).withStyle(ChatFormatting.LIGHT_PURPLE));
            } else {
                serverPlayer.sendSystemMessage(Component.literal("PDA recorded: " + entry).withStyle(matter > 0 ? ChatFormatting.AQUA : ChatFormatting.GRAY));
            }
            serverPlayer.getInventory().setChanged();
            serverPlayer.inventoryMenu.broadcastChanges();
        }
        return InteractionResult.CONSUME;
    }

    private static boolean isLegacyScanTarget(ResourceLocation id) {
        if (id == null) return false;
        String value = id.toString();
        return value.equals("minecraft:carrots") || value.equals("minecraft:potatoes") || value.equals("minecraft:wheat");
    }

    public static boolean isLegacyQuestScanner(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.hasTag() && stack.getTag().getBoolean(LegacyStoryContracts.LEGACY_SCAN_PAD);
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
        CompoundTag root = dataPad.getTag();
        if (root == null) return history;
        ListTag list = root.getList(HISTORY_TAG, Tag.TAG_STRING);
        for (int i = 0; i < list.size() && i < HISTORY_CAPACITY; i++) history.add(list.getString(i));
        return history;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (isLegacyQuestScanner(stack)) tooltip.add(Component.literal("Scientist research scanner").withStyle(ChatFormatting.LIGHT_PURPLE));
        else tooltip.add(Component.literal("Personal research log and matter scanner").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Use on blocks to analyse them; use in air to open the PDA.").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
