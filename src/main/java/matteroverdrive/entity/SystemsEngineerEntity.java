package matteroverdrive.entity;

import matteroverdrive.network.ModNetwork;
import matteroverdrive.quest.ResearchProgression;
import matteroverdrive.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import java.util.List;

/** Village engineering specialist focused on networks, batteries and practical troubleshooting. */
public class SystemsEngineerEntity extends Villager {
    public SystemsEngineerEntity(EntityType<? extends Villager> type,Level level){super(type,level);setCustomName(Component.literal("Systems Engineer"));}
    @Override public InteractionResult mobInteract(Player player,InteractionHand hand){if(level().isClientSide)return InteractionResult.SUCCESS;if(!(player instanceof ServerPlayer sp)||hand!=InteractionHand.MAIN_HAND)return InteractionResult.PASS;ItemStack held=player.getItemInHand(hand);if(held.is(Items.EMERALD)&&held.getCount()>=10){held.shrink(10);give(player,new ItemStack(ModItems.get("battery").get()));player.sendSystemMessage(Component.literal("Systems Engineer: Fresh battery. Don't troubleshoot a dead grid with a dead tool."));return InteractionResult.CONSUME;}if(held.is(Items.EMERALD)&&held.getCount()>=5){held.shrink(5);give(player,new ItemStack(ModItems.get("isolinear_circuit_mk1").get(),2));player.sendSystemMessage(Component.literal("Systems Engineer: Two Mk1 circuits. Label your network this time."));return InteractionResult.CONSUME;}ModNetwork.openDialogue(sp,"Systems Engineer","Village Systems Desk",List.of("Research state: "+ResearchProgression.status(sp),"Heavy Energy Cables move FE. Matter Pipes move matter. Network Cable carries control connectivity. Mixing those concepts is how labs become fires.","Trade: 5 emeralds for two Mk1 circuits; 10 emeralds for a battery.","Wall Terminals can inspect nearby FE-capable nodes without opening every machine."));return InteractionResult.CONSUME;}
    private static void give(Player p,ItemStack s){if(!p.getInventory().add(s))p.drop(s,false);}
}
