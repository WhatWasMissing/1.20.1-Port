package matteroverdrive.item;

import matteroverdrive.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Self-contained contract/quest stack with backward compatibility for the early 1.20.1 format. */
public class ContractItem extends Item {
    public static final String TYPE = "Type";
    public static final String TARGET = "Target";
    public static final String TARGETS = "Targets";
    public static final String GOAL = "Goal";
    public static final String PROGRESS = "Progress";
    public static final String REWARD = "Reward";
    public static final String REWARD_COUNT = "RewardCount";
    public static final String REWARDS = "Rewards";
    public static final String CONTRACT_ID = "ContractId";
    public static final String TITLE = "Title";
    public static final String XP = "Xp";
    public static final String CHILDREN_ONLY = "ChildrenOnly";

    public ContractItem(Properties properties) { super(properties.stacksTo(1)); }

    public static ItemStack collect(String itemId, int goal, String rewardId, int rewardCount) {
        return create("custom_collect", "Collection Contract", "collect", List.of(itemId), goal, 0,
                false, List.of(new RewardSpec(rewardId, rewardCount)));
    }

    public static ItemStack hunt(String entityId, int goal, String rewardId, int rewardCount) {
        return create("custom_hunt", "Hunting Contract", "hunt", List.of(entityId), goal, 0,
                false, List.of(new RewardSpec(rewardId, rewardCount)));
    }

    public static ItemStack create(String id, String title, String type, List<String> targets,
                                   int goal, int xp, boolean childrenOnly, List<RewardSpec> rewards) {
        ItemStack contract = new ItemStack(ModItems.get("contract").get());
        CompoundTag tag = contract.getOrCreateTag();
        tag.putString(CONTRACT_ID, id == null ? "" : id);
        tag.putString(TITLE, title == null ? "Contract" : title);
        tag.putString(TYPE, type == null ? "" : type);
        tag.putInt(GOAL, Math.max(1, goal));
        tag.putInt(PROGRESS, 0);
        tag.putInt(XP, Math.max(0, xp));
        tag.putBoolean(CHILDREN_ONLY, childrenOnly);
        if (targets != null && !targets.isEmpty()) {
            tag.putString(TARGET, targets.get(0));
            ListTag targetTags = new ListTag();
            for (String target : targets) if (target != null && !target.isBlank()) targetTags.add(StringTag.valueOf(target));
            tag.put(TARGETS, targetTags);
        }
        if (rewards != null && !rewards.isEmpty()) {
            RewardSpec first = rewards.get(0);
            tag.putString(REWARD, first.id());
            tag.putInt(REWARD_COUNT, Math.max(1, first.count()));
            ListTag rewardTags = new ListTag();
            for (RewardSpec reward : rewards) {
                if (reward == null || reward.id() == null || reward.id().isBlank()) continue;
                CompoundTag entry = new CompoundTag();
                entry.putString("Id", reward.id());
                entry.putInt("Count", Math.max(1, reward.count()));
                rewardTags.add(entry);
            }
            tag.put(REWARDS, rewardTags);
        }
        return contract;
    }

    public static boolean advancesWithPickup(ItemStack contract, ItemStack picked) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(picked.getItem());
        return "collect".equals(type(contract)) && id != null && matchesTarget(contract, id.toString());
    }

    public static boolean advancesWithKill(ItemStack contract, ResourceLocation entity, boolean baby) {
        return "hunt".equals(type(contract)) && entity != null
                && (!childrenOnly(contract) || baby) && matchesTarget(contract, entity.toString());
    }

    public static boolean advancesWithCraft(ItemStack contract, ItemStack crafted) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(crafted.getItem());
        return "craft".equals(type(contract)) && id != null && matchesTarget(contract, id.toString());
    }

    public static boolean advancesWithMine(ItemStack contract, ResourceLocation block) {
        return "mine".equals(type(contract)) && block != null && matchesTarget(contract, block.toString());
    }

    public static boolean advancesWithScan(ItemStack contract, ResourceLocation block) {
        return "scan".equals(type(contract)) && block != null && matchesTarget(contract, block.toString());
    }

    public static boolean advancesWithScanName(ItemStack contract, String scannedName) {
        if (!"scan".equals(type(contract)) || scannedName == null || scannedName.isBlank()) return false;
        CompoundTag tag = contract.getOrCreateTag();
        if (tag.contains(TARGETS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TARGETS, Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) if (scannedName.equals(displayNameForId(list.getString(i)))) return true;
        }
        return scannedName.equals(displayNameForId(tag.getString(TARGET)));
    }

    public static boolean advancesWithTransport(ItemStack contract) { return "transport".equals(type(contract)); }
    public static boolean advancesWithAnomaly(ItemStack contract) { return "anomaly".equals(type(contract)); }

    public static boolean matchesTarget(ItemStack contract, String id) {
        if (id == null || id.isBlank()) return false;
        CompoundTag tag = contract.getOrCreateTag();
        if (tag.contains(TARGETS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TARGETS, Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) if (id.equals(list.getString(i))) return true;
        }
        return id.equals(tag.getString(TARGET));
    }

    public static boolean advanceAndCheck(ItemStack contract, int amount) {
        boolean before = complete(contract);
        advance(contract, amount);
        return !before && complete(contract);
    }

    public static void advance(ItemStack contract, int amount) {
        CompoundTag tag = contract.getOrCreateTag();
        int goal = Math.max(0, tag.getInt(GOAL));
        tag.putInt(PROGRESS, Math.min(goal, Math.max(0, tag.getInt(PROGRESS)) + Math.max(0, amount)));
    }

    public static boolean complete(ItemStack contract) {
        CompoundTag tag = contract.getOrCreateTag();
        return tag.getInt(GOAL) > 0 && tag.getInt(PROGRESS) >= tag.getInt(GOAL);
    }

    public static int progress(ItemStack contract) { return Math.max(0, contract.getOrCreateTag().getInt(PROGRESS)); }
    public static int goal(ItemStack contract) { return Math.max(1, contract.getOrCreateTag().getInt(GOAL)); }
    public static int xp(ItemStack contract) { return Math.max(0, contract.getOrCreateTag().getInt(XP)); }
    public static String type(ItemStack contract) { return contract.getOrCreateTag().getString(TYPE); }
    public static String contractId(ItemStack contract) { return contract.getOrCreateTag().getString(CONTRACT_ID); }
    public static boolean childrenOnly(ItemStack contract) { return contract.getOrCreateTag().getBoolean(CHILDREN_ONLY); }
    public static String title(ItemStack contract) {
        String title = contract.getOrCreateTag().getString(TITLE);
        return title.isBlank() ? "Contract" : title;
    }

    public static ItemStack reward(ItemStack contract) {
        List<ItemStack> rewards = rewardItems(contract);
        return rewards.isEmpty() ? ItemStack.EMPTY : rewards.get(0);
    }

    public static List<ItemStack> rewardItems(ItemStack contract) {
        CompoundTag tag = contract.getOrCreateTag();
        List<ItemStack> result = new ArrayList<>();
        if (tag.contains(REWARDS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(REWARDS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                ItemStack stack = stack(entry.getString("Id"), entry.getInt("Count"));
                if (!stack.isEmpty()) result.add(stack);
            }
        }
        if (result.isEmpty() && tag.contains(REWARD, Tag.TAG_STRING)) {
            ItemStack stack = stack(tag.getString(REWARD), tag.getInt(REWARD_COUNT));
            if (!stack.isEmpty()) result.add(stack);
        }
        return Collections.unmodifiableList(result);
    }

    private static ItemStack stack(String id, int count) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        if (key == null) return ItemStack.EMPTY;
        Item item = ForgeRegistries.ITEMS.getValue(key);
        return item == null ? ItemStack.EMPTY : new ItemStack(item, Math.max(1, count));
    }

    @Override public boolean isFoil(ItemStack stack) { return complete(stack); }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(title(stack)).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal(objectiveText(stack)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Progress: " + progress(stack) + " / " + goal(stack))
                .withStyle(complete(stack) ? ChatFormatting.GREEN : ChatFormatting.WHITE));
        List<ItemStack> rewards = rewardItems(stack);
        if (!rewards.isEmpty()) {
            StringBuilder rewardLine = new StringBuilder("Rewards: ");
            for (int i = 0; i < rewards.size(); i++) {
                if (i > 0) rewardLine.append(", ");
                ItemStack reward = rewards.get(i);
                rewardLine.append(reward.getCount()).append('x').append(' ').append(reward.getHoverName().getString());
            }
            tooltip.add(Component.literal(rewardLine.toString()).withStyle(ChatFormatting.GOLD));
        }
        if (xp(stack) > 0) tooltip.add(Component.literal("Experience: " + xp(stack) + " XP").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal(complete(stack)
                ? "Return to a Contract Market to redeem."
                : "Keep this contract in your inventory to track progress.")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    public static String objectiveText(ItemStack stack) {
        String target = primaryTargetName(stack);
        return switch (type(stack)) {
            case "hunt" -> childrenOnly(stack) ? "Eliminate juvenile targets" : "Eliminate " + target;
            case "collect" -> "Collect " + target;
            case "craft" -> "Craft " + target;
            case "mine" -> "Mine " + target;
            case "scan" -> "Scan " + target;
            case "transport" -> "Use a Matter Overdrive Transporter";
            case "anomaly" -> "Enter a gravitational anomaly event horizon";
            case "place" -> "Place " + target;
            case "block_interact" -> "Interact with " + target;
            case "item_interact" -> "Use " + target;
            case "item_interact_consume" -> "Read/use " + target;
            case "conversation" -> "Talk to " + target;
            default -> "Complete the assigned objective";
        };
    }

    private static String primaryTargetName(ItemStack stack) { return displayNameForId(stack.getOrCreateTag().getString(TARGET)); }

    private static String displayNameForId(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        if (key == null) return id;
        Item item = ForgeRegistries.ITEMS.getValue(key);
        if (item != null && item != net.minecraft.world.item.Items.AIR) return new ItemStack(item).getHoverName().getString();
        var block = ForgeRegistries.BLOCKS.getValue(key);
        if (block != null && block != net.minecraft.world.level.block.Blocks.AIR) return new ItemStack(block.asItem()).getHoverName().getString();
        return key.getPath().replace('_', ' ');
    }

    public record RewardSpec(String id, int count) {}
}
