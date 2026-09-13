package matteroverdrive.quest;

import matteroverdrive.event.StructureLoreEvents;
import matteroverdrive.item.ContractItem;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.progression.PlayerDiscoveryLog;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Small adapter that lets specialist NPCs hand out the existing research
 * contracts without creating a second quest database or completion format.
 */
public final class NpcAssignmentFlow {
    private static final String OFFERED_PREFIX = "MatterOverdriveNpcAssignment:";

    private NpcAssignmentFlow() {
    }

    public static void offer(ServerPlayer player, String contactId, String speaker,
                             String title, List<String> briefing, String... assignmentIds) {
        for (String assignmentId : assignmentIds) {
            ItemStack active = find(player, assignmentId);
            if (!active.isEmpty()) {
                ModNetwork.openDialogue(player, speaker, ContractItem.title(active), List.of(
                        "Your field assignment is still registered here.",
                        ContractItem.objectiveText(active),
                        "Progress: " + ContractItem.progress(active) + " / " + ContractItem.goal(active),
                        ContractItem.complete(active)
                                ? "It is complete. Redeem it at the Contract Market, then return for the next brief."
                                : "Keep the contract in your inventory while working.")
                );
                return;
            }

            CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            String offeredKey = OFFERED_PREFIX + contactId + ":" + assignmentId;
            if (persisted.getBoolean(offeredKey)) continue;

            ItemStack assignment = ResearchCampaignContracts.create(assignmentId, player.getRandom());
            if (assignment.isEmpty()) continue;
            if (!player.getInventory().add(assignment)) player.drop(assignment, false);
            persisted.putBoolean(offeredKey, true);
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
            PlayerDiscoveryLog.record(player, "assignment:" + contactId + ":" + assignmentId,
                    "ASSIGNMENT // " + speaker + " issued " + ContractItem.title(assignment) + ".");
            StructureLoreEvents.discoverFromAssignment(player, contactId);

            java.util.ArrayList<String> lines = new java.util.ArrayList<>(briefing);
            lines.add(ContractItem.title(assignment));
            lines.add(ContractItem.objectiveText(assignment));
            lines.add("Reward contract issued. Keep it with you to track progress.");
            ModNetwork.openDialogue(player, speaker, title, lines);
            return;
        }

        ModNetwork.openDialogue(player, speaker, title, List.of(
                "No new assignment is available from this desk.",
                "Review your PDA field log and redeem completed contracts at a Contract Market."));
    }

    private static ItemStack find(ServerPlayer player, String assignmentId) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ContractItem
                    && assignmentId.equals(ContractItem.contractId(stack))) return stack;
        }
        return ItemStack.EMPTY;
    }
}
