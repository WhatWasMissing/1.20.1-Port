package matteroverdrive.quest;

import matteroverdrive.item.ContractItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Ordered objective support layered on top of ContractItem's compatibility keys.
 * The active stage is mirrored into Type/Target/Targets/Goal/Progress/ChildrenOnly,
 * so existing HUD, tooltips, redemption and old saves keep using the same contract API.
 */
public final class ContractStageSupport {
    public static final String STAGES = "Stages";
    public static final String ACTIVE_STAGE = "ActiveStage";
    public static final String REQUIRED_NAME = "RequiredName";
    public static final String QUEST_POS = "QuestPos";
    public static final String QUEST_RADIUS = "QuestRadius";

    private ContractStageSupport() {}

    public static ItemStack createStaged(String id, String title, int xp,
                                         List<ContractItem.RewardSpec> rewards,
                                         List<StageSpec> stages) {
        if (stages == null || stages.isEmpty()) throw new IllegalArgumentException("A staged contract requires at least one stage");
        StageSpec first = stages.get(0);
        ItemStack contract = ContractItem.create(id, title, first.type(), first.targets(), first.goal(), xp,
                first.childrenOnly(), rewards);
        CompoundTag root = contract.getOrCreateTag();
        ListTag list = new ListTag();
        for (StageSpec stage : stages) list.add(writeStage(stage));
        root.put(STAGES, list);
        root.putInt(ACTIVE_STAGE, 0);
        mirror(root, list.getCompound(0));
        return contract;
    }

    public static boolean isStaged(ItemStack contract) {
        CompoundTag tag = contract.getTag();
        return tag != null && tag.contains(STAGES, Tag.TAG_LIST) && !tag.getList(STAGES, Tag.TAG_COMPOUND).isEmpty();
    }

    public static int stageIndex(ItemStack contract) {
        if (!isStaged(contract)) return 0;
        ListTag stages = contract.getOrCreateTag().getList(STAGES, Tag.TAG_COMPOUND);
        return Math.max(0, Math.min(contract.getOrCreateTag().getInt(ACTIVE_STAGE), stages.size() - 1));
    }

    public static int stageCount(ItemStack contract) {
        return isStaged(contract) ? contract.getOrCreateTag().getList(STAGES, Tag.TAG_COMPOUND).size() : 1;
    }

    public static String requiredName(ItemStack contract) {
        return contract.getOrCreateTag().getString(REQUIRED_NAME);
    }

    public static boolean requiredNameMatches(ItemStack contract, ItemStack used) {
        String required = requiredName(contract);
        return required.isBlank() || (used.hasCustomHoverName() && required.equals(used.getHoverName().getString()));
    }

    public static void setQuestPosition(ItemStack contract, BlockPos pos, int radius) {
        if (contract == null || contract.isEmpty() || pos == null) return;
        CompoundTag tag = contract.getOrCreateTag();
        tag.putLong(QUEST_POS, pos.asLong());
        tag.putInt(QUEST_RADIUS, Math.max(0, radius));
    }

    public static boolean hasQuestPosition(ItemStack contract) {
        return contract != null && !contract.isEmpty() && contract.hasTag()
                && contract.getTag().contains(QUEST_POS, Tag.TAG_LONG);
    }

    public static boolean questPositionMatches(ItemStack contract, BlockPos eventPos) {
        if (!hasQuestPosition(contract)) return true;
        if (eventPos == null) return false;
        CompoundTag tag = contract.getOrCreateTag();
        BlockPos questPos = BlockPos.of(tag.getLong(QUEST_POS));
        int radius = Math.max(0, tag.getInt(QUEST_RADIUS));
        return questPos.distSqr(eventPos) <= (double) radius * radius;
    }

    public static void copyQuestPosition(ItemStack from, ItemStack to) {
        if (!hasQuestPosition(from) || to == null || to.isEmpty()) return;
        CompoundTag source = from.getOrCreateTag();
        setQuestPosition(to, BlockPos.of(source.getLong(QUEST_POS)), source.getInt(QUEST_RADIUS));
    }

    /** Advances the active objective and rotates to the next stage when appropriate. */
    public static boolean advanceAndCheck(ItemStack contract, int amount) {
        if (!isStaged(contract)) return ContractItem.advanceAndCheck(contract, amount);
        CompoundTag root = contract.getOrCreateTag();
        ListTag stages = root.getList(STAGES, Tag.TAG_COMPOUND);
        int index = stageIndex(contract);

        ContractItem.advance(contract, amount);
        CompoundTag current = stages.getCompound(index);
        current.putInt(ContractItem.PROGRESS, ContractItem.progress(contract));
        stages.set(index, current);
        root.put(STAGES, stages);

        if (!ContractItem.complete(contract)) return false;
        if (index >= stages.size() - 1) return true;

        int next = index + 1;
        root.putInt(ACTIVE_STAGE, next);
        mirror(root, stages.getCompound(next));
        return false;
    }

    public static String stageLabel(ItemStack contract) {
        return isStaged(contract) ? "Stage " + (stageIndex(contract) + 1) + "/" + stageCount(contract) : "";
    }

    private static CompoundTag writeStage(StageSpec stage) {
        CompoundTag tag = new CompoundTag();
        tag.putString(ContractItem.TYPE, stage.type());
        tag.putInt(ContractItem.GOAL, Math.max(1, stage.goal()));
        tag.putInt(ContractItem.PROGRESS, 0);
        tag.putBoolean(ContractItem.CHILDREN_ONLY, stage.childrenOnly());
        if (stage.requiredName() != null && !stage.requiredName().isBlank()) tag.putString(REQUIRED_NAME, stage.requiredName());
        if (stage.targets() != null && !stage.targets().isEmpty()) {
            tag.putString(ContractItem.TARGET, stage.targets().get(0));
            ListTag targets = new ListTag();
            for (String target : stage.targets()) if (target != null && !target.isBlank()) targets.add(StringTag.valueOf(target));
            tag.put(ContractItem.TARGETS, targets);
        }
        return tag;
    }

    private static void mirror(CompoundTag root, CompoundTag stage) {
        root.putString(ContractItem.TYPE, stage.getString(ContractItem.TYPE));
        root.putInt(ContractItem.GOAL, Math.max(1, stage.getInt(ContractItem.GOAL)));
        root.putInt(ContractItem.PROGRESS, Math.max(0, stage.getInt(ContractItem.PROGRESS)));
        root.putBoolean(ContractItem.CHILDREN_ONLY, stage.getBoolean(ContractItem.CHILDREN_ONLY));
        if (stage.contains(REQUIRED_NAME, Tag.TAG_STRING)) root.putString(REQUIRED_NAME, stage.getString(REQUIRED_NAME));
        else root.remove(REQUIRED_NAME);
        if (stage.contains(ContractItem.TARGET, Tag.TAG_STRING)) root.putString(ContractItem.TARGET, stage.getString(ContractItem.TARGET));
        else root.remove(ContractItem.TARGET);
        if (stage.contains(ContractItem.TARGETS, Tag.TAG_LIST)) root.put(ContractItem.TARGETS, stage.getList(ContractItem.TARGETS, Tag.TAG_STRING).copy());
        else root.remove(ContractItem.TARGETS);
    }

    public record StageSpec(String type, List<String> targets, int goal, boolean childrenOnly, String requiredName) {
        public StageSpec(String type, List<String> targets, int goal) { this(type, targets, goal, false, ""); }
        public StageSpec(String type, List<String> targets, int goal, boolean childrenOnly) { this(type, targets, goal, childrenOnly, ""); }
        public StageSpec(String type, List<String> targets, int goal, String requiredName) { this(type, targets, goal, false, requiredName); }
    }
}
