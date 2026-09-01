package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.ContractItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ContractEvents {
    private ContractEvents() {}

    @SubscribeEvent
    public static void pickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack picked = event.getItem().getItem();
        int amount = amountThatFits(player.getInventory(), picked);
        if (amount <= 0) return;

        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ContractItem && ContractItem.advancesWithPickup(stack, picked)) {
                ContractItem.advance(stack, amount);
                break;
            }
        }
    }

    @SubscribeEvent
    public static void kill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        ResourceLocation type = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
        if (type == null) return;

        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ContractItem && ContractItem.advancesWithKill(stack, type)) {
                ContractItem.advance(stack, 1);
                break;
            }
        }
    }

    private static int amountThatFits(Inventory inventory, ItemStack picked) {
        int remaining = picked.getCount();
        int accepted = 0;
        for (ItemStack slot : inventory.items) {
            if (remaining <= 0) break;
            int room;
            if (slot.isEmpty()) {
                room = Math.min(picked.getMaxStackSize(), inventory.getMaxStackSize());
            } else if (ItemStack.isSameItemSameTags(slot, picked)) {
                room = Math.max(0, Math.min(slot.getMaxStackSize(), inventory.getMaxStackSize()) - slot.getCount());
            } else {
                continue;
            }
            int moved = Math.min(remaining, room);
            accepted += moved;
            remaining -= moved;
        }
        return accepted;
    }
}
