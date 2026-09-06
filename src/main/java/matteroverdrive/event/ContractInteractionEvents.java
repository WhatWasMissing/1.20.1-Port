package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.ContractItem;
import matteroverdrive.quest.ContractStageSupport;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** Additional legacy quest objective hooks that map cleanly onto modern Forge events. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ContractInteractionEvents {
    private ContractInteractionEvents() {}

    @SubscribeEvent
    public static void placed(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ResourceLocation block = ForgeRegistries.BLOCKS.getKey(event.getPlacedBlock().getBlock());
        if (block == null) return;
        advanceMatching(player, 1, contract -> "place".equals(ContractItem.type(contract))
                && ContractItem.matchesTarget(contract, block.toString())
                && ContractStageSupport.questPositionMatches(contract, event.getPos()));
    }

    @SubscribeEvent
    public static void blockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ResourceLocation block = ForgeRegistries.BLOCKS.getKey(player.level().getBlockState(event.getPos()).getBlock());
        if (block == null) return;
        advanceMatching(player, 1, contract -> "block_interact".equals(ContractItem.type(contract))
                && ContractItem.matchesTarget(contract, block.toString())
                && ContractStageSupport.questPositionMatches(contract, event.getPos()));
    }

    @SubscribeEvent
    public static void itemInteract(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack used = event.getItemStack();
        if (used.isEmpty()) return;
        ResourceLocation item = ForgeRegistries.ITEMS.getKey(used.getItem());
        if (item == null) return;

        boolean consume = advanceMatching(player, 1, contract ->
                ("item_interact".equals(ContractItem.type(contract))
                        || "item_interact_consume".equals(ContractItem.type(contract)))
                        && ContractItem.matchesTarget(contract, item.toString())
                        && ContractStageSupport.requiredNameMatches(contract, used),
                contract -> "item_interact_consume".equals(ContractItem.type(contract)));
        if (consume && !player.getAbilities().instabuild) used.shrink(1);
    }

    /** Used by server-authoritative NPC interaction handlers to restore legacy conversation objectives. */
    public static void recordConversation(ServerPlayer player, ResourceLocation entityType) {
        if (player == null || entityType == null) return;
        advanceMatching(player, 1, contract -> "conversation".equals(ContractItem.type(contract))
                && ContractItem.matchesTarget(contract, entityType.toString()));
    }

    private static boolean advanceMatching(ServerPlayer player, int amount, ContractPredicate predicate) {
        return advanceMatching(player, amount, predicate, contract -> false);
    }

    /** Returns true when at least one matching objective requested consumption. */
    private static boolean advanceMatching(ServerPlayer player, int amount, ContractPredicate predicate,
                                           ContractPredicate consumePredicate) {
        boolean changed = false;
        boolean completed = false;
        boolean consume = false;
        String latestTitle = null;
        for (ItemStack contract : player.getInventory().items) {
            if (!(contract.getItem() instanceof ContractItem)
                    || ContractItem.complete(contract)
                    || !predicate.test(contract)) continue;
            if (consumePredicate.test(contract)) consume = true;
            if (ContractStageSupport.advanceAndCheck(contract, amount)) {
                completed = true;
                latestTitle = ContractItem.title(contract);
            }
            changed = true;
        }
        if (!changed) return false;
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
        if (completed) notifyCompletion(player, latestTitle == null ? "Contract" : latestTitle);
        return consume;
    }

    private static void notifyCompletion(ServerPlayer player, String title) {
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Contract complete: " + title), true);
        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(MatterOverdrive.MOD_ID, "gui.quest_complete"));
        if (sound != null) player.playNotifySound(sound, SoundSource.PLAYERS, 0.9F, 1.0F);
    }

    @FunctionalInterface
    private interface ContractPredicate { boolean test(ItemStack contract); }
}
