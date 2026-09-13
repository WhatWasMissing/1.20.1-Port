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

/** Village research specialist: progression commentary plus a small science-supply exchange. */
public class FieldScientistEntity extends Villager {
    public FieldScientistEntity(EntityType<? extends Villager> type,Level level){super(type,level);setCustomName(Component.literal("Field Scientist"));}
    @Override public InteractionResult mobInteract(Player player,InteractionHand hand){if(level().isClientSide)return InteractionResult.SUCCESS;if(!(player instanceof ServerPlayer sp)||hand!=InteractionHand.MAIN_HAND)return InteractionResult.PASS;ItemStack held=player.getItemInHand(hand);if(held.is(Items.EMERALD)&&held.getCount()>=8){held.shrink(8);give(player,new ItemStack(ModItems.get("dilithium_crystal").get()));player.sendSystemMessage(Component.literal("Field Scientist: Eight emeralds for one calibrated Dilithium sample."));return InteractionResult.CONSUME;}if(player.getInventory().countItem(ModItems.get("data_pad").get())==0&&held.is(Items.EMERALD)&&held.getCount()>=3){held.shrink(3);give(player,new ItemStack(ModItems.get("data_pad").get()));player.sendSystemMessage(Component.literal("Field Scientist: Keep a PDA. Record what you build, not just what you find."));return InteractionResult.CONSUME;}ModNetwork.openDialogue(sp,"Field Scientist","Local Research Exchange",List.of("Current research: "+ResearchProgression.status(sp),"I catalogue reproducible results: fabrication, scans, power behavior and synthetic encounters.","Trade: 3 emeralds for a PDA if you lack one; 8 emeralds for a calibrated Dilithium sample.","Generated ruins are not part of the programme. Your laboratory is the evidence."));return InteractionResult.CONSUME;}
    private static void give(Player p,ItemStack s){if(!p.getInventory().add(s))p.drop(s,false);}
}
