package matteroverdrive.quest;

import matteroverdrive.item.ContractItem;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Connects the source-backed legacy quests into one scientist-driven campaign. */
public final class ScientistStoryQuestFlow {
    public static final String STORY_INDEX = "MatterOverdriveScientistStoryIndex";
    public static final String STORY_DONE = "MatterOverdriveScientistStoryDone";

    private ScientistStoryQuestFlow() {}

    public static boolean handle(ServerPlayer player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (persisted.getBoolean(STORY_DONE)) return false;

        int index = normalizeStoryIndex(player, persisted);
        ItemStack active = findLegacyContract(player, index);
        if (!active.isEmpty()) {
            if (!ContractItem.complete(active)) {
                showProgress(player, active);
                return true;
            }
            redeem(player, active, persisted);
            return true;
        }

        if (index >= LegacyStoryContracts.QUEST_IDS.length) {
            persisted.putBoolean(STORY_DONE, true);
            save(player, persisted);
            return false;
        }

        String id = LegacyStoryContracts.QUEST_IDS[index];
        ItemStack quest = LegacyStoryContracts.create(id, player.getRandom());
        if (quest.isEmpty()) return false;
        give(player, quest);
        save(player, persisted);
        player.level().playSound(null, player.blockPosition(), ModSounds.get("gui.quest_started").get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        ModNetwork.openDialogue(player, "Mad Scientist", ContractItem.title(quest), List.of(
                storyLine(id), objectiveLine(quest),
                "Keep the contract with you. Return when the work is complete."));
        return true;
    }

    private static void showProgress(ServerPlayer player, ItemStack active) {
        ModNetwork.openDialogue(player, "Mad Scientist", ContractItem.title(active), List.of(
                storyLine(ContractItem.contractId(active)), objectiveLine(active),
                "Progress: " + ContractItem.progress(active) + " / " + ContractItem.goal(active)));
    }

    private static void redeem(ServerPlayer player, ItemStack completed, CompoundTag persisted) {
        String id = ContractItem.contractId(completed);
        int completedIndex = questIndex(id);
        if (completedIndex < 0) return;
        for (ItemStack reward : ContractItem.rewardItems(completed)) give(player, reward.copy());
        for (ItemStack reward : LegacyStoryContracts.specialRewards(completed)) give(player, reward.copy());
        if (ContractItem.xp(completed) > 0) player.giveExperiencePoints(ContractItem.xp(completed));
        LegacyStoryContracts.applyWorldRewards(player, completed);
        removeStack(player, completed);
        removeDuplicateLegacyContracts(player, completedIndex);
        int index = Math.max(Math.max(0, persisted.getInt(STORY_INDEX)), completedIndex + 1);
        persisted.putInt(STORY_INDEX, index);
        if (index >= LegacyStoryContracts.QUEST_IDS.length) persisted.putBoolean(STORY_DONE, true);
        save(player, persisted);
        player.level().playSound(null, player.blockPosition(), ModSounds.get("gui.quest_complete").get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        if (index >= LegacyStoryContracts.QUEST_IDS.length) {
            ModNetwork.openDialogue(player, "Mad Scientist", "Research Chain Complete", List.of(
                    "Against several reasonable predictions, you survived the entire research programme.",
                    "The old field work is complete. Future experiments will be considerably less supervised."));
        } else {
            String next = LegacyStoryContracts.QUEST_IDS[index];
            ModNetwork.openDialogue(player, "Mad Scientist", ContractItem.title(completed) + " Complete", List.of(
                    completionLine(id), "Return to me again when you are ready for " + displayQuest(next) + "."));
        }
    }

    private static int normalizeStoryIndex(ServerPlayer player, CompoundTag persisted) {
        int stored = Math.max(0, persisted.getInt(STORY_INDEX));
        int furthest = -1;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ContractItem) furthest = Math.max(furthest, questIndex(ContractItem.contractId(stack)));
        }
        int normalized = Math.min(Math.max(stored, furthest), LegacyStoryContracts.QUEST_IDS.length);
        if (normalized != stored) {
            persisted.putInt(STORY_INDEX, normalized);
            save(player, persisted);
        }
        return normalized;
    }

    private static ItemStack findLegacyContract(ServerPlayer player, int storyIndex) {
        ItemStack fallback = ItemStack.EMPTY;
        int fallbackIndex = -1;
        for (ItemStack stack : player.getInventory().items) {
            if (!(stack.getItem() instanceof ContractItem)) continue;
            int index = questIndex(ContractItem.contractId(stack));
            if (index < 0) continue;
            if (index == storyIndex) return stack;
            if (index > fallbackIndex) { fallback = stack; fallbackIndex = index; }
        }
        return fallback;
    }

    private static void removeStack(ServerPlayer player, ItemStack target) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i) == target) { player.getInventory().setItem(i, ItemStack.EMPTY); return; }
        }
    }

    private static void removeDuplicateLegacyContracts(ServerPlayer player, int throughIndex) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!(stack.getItem() instanceof ContractItem)) continue;
            int index = questIndex(ContractItem.contractId(stack));
            if (index >= 0 && index <= throughIndex) player.getInventory().setItem(i, ItemStack.EMPTY);
        }
    }

    private static int questIndex(String id) {
        if (id == null || id.isBlank()) return -1;
        for (int i = 0; i < LegacyStoryContracts.QUEST_IDS.length; i++) if (LegacyStoryContracts.QUEST_IDS[i].equals(id)) return i;
        return -1;
    }

    private static void give(ServerPlayer player, ItemStack stack) { if (!player.getInventory().add(stack)) player.drop(stack, false); }
    private static void save(ServerPlayer player, CompoundTag persisted) { player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted); player.getInventory().setChanged(); }
    private static String objectiveLine(ItemStack stack) {
        String stage = ContractStageSupport.stageLabel(stack);
        return stage.isBlank() ? ContractItem.objectiveText(stack) : stage + ": " + ContractItem.objectiveText(stack);
    }
    private static String displayQuest(String id) {
        ItemStack stack = LegacyStoryContracts.create(id, net.minecraft.util.RandomSource.create(0L));
        return stack.isEmpty() ? id : ContractItem.title(stack);
    }
    private static String storyLine(String id) {
        return switch (id) {
            case "crash_landing" -> "A damaged research relay came down nearby. Rebuild the protocol hardware before its data decays.";
            case "we_must_know" -> "The relay is speaking again. I need a field coil placed so we can hear what it was trying to report.";
            case "gmo" -> "Scan the agricultural samples. I want to know what prolonged matter exposure is doing to ordinary crops.";
            case "trade_route" -> "Our suppliers have become unreliable. Follow the agreement trail, inspect the crate, and speak to one of my colleagues.";
            case "stem_bolts" -> "The agreement copy contains the final authentication sequence. Use it and see what the network sends back.";
            case "to_the_power_of" -> "Every laboratory eventually runs out of power. Prove you can establish independent generation.";
            default -> "There is work to do.";
        };
    }
    private static String completionLine(String id) {
        return switch (id) {
            case "crash_landing" -> "Good. The relay survived, which is more than I expected from its previous owner.";
            case "we_must_know" -> "The signal is coherent. Unfortunately, that means we now have to investigate it.";
            case "gmo" -> "Useful samples. Disturbing samples, but useful is the important part.";
            case "trade_route" -> "The route is open again. Ignore anything unusual that followed you back.";
            case "stem_bolts" -> "Authentication accepted. I am choosing to interpret the response as encouraging.";
            case "to_the_power_of" -> "Independent power confirmed. You are becoming inconveniently competent.";
            default -> "The experiment is complete.";
        };
    }
}
