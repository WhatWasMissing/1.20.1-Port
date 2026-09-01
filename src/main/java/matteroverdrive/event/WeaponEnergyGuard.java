package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.weapon.EnergyWeaponItem;
import matteroverdrive.item.weapon.WeaponBatteryItem;
import matteroverdrive.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WeaponEnergyGuard {
    private WeaponEnergyGuard() {
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof EnergyWeaponItem) || canSupplyShot(event.getEntity(), stack)) {
            return;
        }

        event.setCancellationResult(InteractionResult.FAIL);
        event.setCanceled(true);
        if (!event.getLevel().isClientSide) {
            event.getEntity().displayClientMessage(Component.literal("Weapon has no energy"), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide || !event.player.isUsingItem()) {
            return;
        }
        ItemStack stack = event.player.getUseItem();
        if (stack.getItem() instanceof EnergyWeaponItem && !canSupplyShot(event.player, stack)) {
            event.player.stopUsingItem();
        }
    }

    private static boolean canSupplyShot(Player player, ItemStack weapon) {
        int stored = weapon.getCapability(ForgeCapabilities.ENERGY)
                .map(storage -> storage.getEnergyStored())
                .orElse(0);
        if (stored > 0) {
            return true;
        }

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.is(ModItems.get("energy_pack").get()) || isChargedBattery(candidate)) {
                return true;
            }
        }
        return isChargedBattery(player.getOffhandItem());
    }

    private static boolean isChargedBattery(ItemStack stack) {
        if (!(stack.getItem() instanceof WeaponBatteryItem)) {
            return false;
        }
        return stack.getCapability(ForgeCapabilities.ENERGY)
                .map(storage -> storage.getEnergyStored() > 0)
                .orElse(false);
    }
}
