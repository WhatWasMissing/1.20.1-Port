package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.ContractItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ContractEvents {
    private ContractEvents() {}

    /**
     * Track collect objectives after Forge has completed the pickup. PlayerEvent.ItemPickupEvent#getStack()
     * is the exact amount that actually entered the player's inventory, so partial pickups cannot
     * over-count and cancelled/failed pickups cannot advance a contract.
     */
    @SubscribeEvent
    public static void pickup(PlayerEvent.ItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack picked = event.getStack();
        if (picked.isEmpty() || picked.getCount() <= 0) return;

        for (ItemStack contract : player.getInventory().items) {
            if (contract.getItem() instanceof ContractItem && ContractItem.advancesWithPickup(contract, picked)) {
                ContractItem.advance(contract, picked.getCount());
                syncProgress(player);
                break;
            }
        }
    }

    @SubscribeEvent
    public static void kill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        ResourceLocation type = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
        if (type == null) return;

        for (ItemStack contract : player.getInventory().items) {
            if (contract.getItem() instanceof ContractItem && ContractItem.advancesWithKill(contract, type)) {
                ContractItem.advance(contract, 1);
                syncProgress(player);
                break;
            }
        }
    }

    private static void syncProgress(ServerPlayer player) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) {
            player.containerMenu.broadcastChanges();
        }
    }
}
