package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.ContractItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=MatterOverdrive.MOD_ID,bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class ContractEvents {
    private ContractEvents(){}
    @SubscribeEvent public static void pickup(EntityItemPickupEvent event){
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        for(ItemStack stack:player.getInventory().items)if(stack.getItem() instanceof ContractItem&&ContractItem.advancesWithPickup(stack,event.getItem().getItem()))ContractItem.advance(stack,event.getItem().getItem().getCount());
    }
    @SubscribeEvent public static void kill(LivingDeathEvent event){
        if(!(event.getSource().getEntity() instanceof ServerPlayer player))return;
        ResourceLocation type=net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
        if(type==null)return;
        for(ItemStack stack:player.getInventory().items)if(stack.getItem() instanceof ContractItem&&ContractItem.advancesWithKill(stack,type))ContractItem.advance(stack,1);
    }
}