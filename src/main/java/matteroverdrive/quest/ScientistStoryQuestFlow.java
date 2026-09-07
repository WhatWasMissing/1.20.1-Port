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

        ItemStack active = findLegacyContract(player);
        if (!active.isEmpty()) {
            if (!ContractItem.complete(active)) {
                ModNetwork.openDialogue(player, "Mad Scientist", ContractItem.title(active), List.of(
                        storyLine(ContractItem.contractId(active)),
                        ContractItem.objectiveText(active),
                        "Progress: " + ContractItem.progress(active) + " / " + ContractItem.goal(active)));
                return true;
            }
            redeem(player, active, persisted);
            return true;
        }

        int index = Math.max(0, persisted.getInt(STORY_INDEX));
        if (index >= LegacyStoryContracts.QUEST_IDS.length) {
            persisted.putBoolean(STORY_DONE, true);
            save(player, persisted);
            return false;
        }

        String id = LegacyStoryContracts.QUEST_IDS[index];
        ItemStack quest = LegacyStoryContracts.create(id, player.getRandom());
        if (quest.isEmpty()) return false;
        give(player, quest);
        player.level().playSound(null, player.blockPosition(), ModSounds.get("gui.quest_started").get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        ModNetwork.openDialogue(player, "Mad Scientist", ContractItem.title(quest), List.of(
                storyLine(id),
                ContractItem.objectiveText(quest),
                "Keep the contract with you. Return when the work is complete."));
        return true;
    }

    private static void redeem(ServerPlayer player, ItemStack completed, CompoundTag persisted) {
        String id = ContractItem.contractId(completed);
        for (ItemStack reward : ContractItem.rewardItems(completed)) give(player, reward.copy());
        for (ItemStack reward : LegacyStoryContracts.specialRewards(completed)) give(player, reward.copy());
        if (ContractItem.xp(completed) > 0) player.giveExperiencePoints(ContractItem.xp(completed));
        LegacyStoryContracts.applyWorldRewards(player, completed);

        int slot = findStackSlot(player, completed);
        if (slot >= 0) player.getInventory().setItem(slot, ItemStack.EMPTY);
        int index = Math.max(0, persisted.getInt(STORY_INDEX)) + 1;
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
                    completionLine(id),
                    "Return to me again when you are ready for " + displayQuest(next) + "."));
        }
    }

    private static ItemStack findLegacyContract(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (!(stack.getItem() instanceof ContractItem)) continue;
            String id = ContractItem.contractId(stack);
            for (String legacy : LegacyStoryContracts.QUEST_IDS) if (legacy.equals(id)) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static int findStackSlot(ServerPlayer player, ItemStack target) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) if (player.getInventory().getItem(i) == target) return i;
        return -1;
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    private static void save(ServerPlayer player, CompoundTag persisted) {
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        player.getInventory().setChanged();
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
