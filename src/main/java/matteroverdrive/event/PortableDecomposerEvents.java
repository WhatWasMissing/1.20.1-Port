package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.PortableDecomposerItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PortableDecomposerEvents {
    private PortableDecomposerEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemEntity itemEntity = event.getItem();
        ItemStack pickup = itemEntity.getItem();
        if (pickup.isEmpty()) {
            return;
        }

        for (ItemStack inventoryStack : player.getInventory().items) {
            if (!(inventoryStack.getItem() instanceof PortableDecomposerItem decomposer)) {
                continue;
            }

            int consumed = decomposer.decomposePickup(inventoryStack, pickup);
            if (consumed <= 0) {
                continue;
            }

            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
            if (player.containerMenu != player.inventoryMenu) {
                player.containerMenu.broadcastChanges();
            }

            if (pickup.isEmpty()) {
                itemEntity.discard();
                event.setCanceled(true);
            } else {
                itemEntity.setItem(pickup);
            }
            break;
        }
    }
}
