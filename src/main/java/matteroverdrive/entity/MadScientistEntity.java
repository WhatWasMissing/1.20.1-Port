package matteroverdrive.entity;

import matteroverdrive.android.AndroidData;
import matteroverdrive.event.CocktailQuestEvents;
import matteroverdrive.event.ContractInteractionEvents;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.quest.ScientistStoryQuestFlow;
import matteroverdrive.registry.ModEntities;
import matteroverdrive.registry.ModItems;
import matteroverdrive.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import javax.annotation.Nullable;
import java.util.List;

/** Mad Scientist quest host with dialogue-driven legacy and conversion story progression. */
public class MadScientistEntity extends Villager {
    private static final String JUNKIE="Junkie";
    public static final String QUEST_ACTIVE="MatterOverdrivePunyHumansActive",QUEST_DONE="MatterOverdrivePunyHumansDone",QUEST_PARTS_MODE="MatterOverdrivePunyHumansPartsMode";
    private boolean junkie;
    public MadScientistEntity(EntityType<? extends Villager> type,Level level){super(type,level);}
    @Nullable @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level,DifficultyInstance difficulty,MobSpawnType spawnType,@Nullable SpawnGroupData spawnData,@Nullable CompoundTag dataTag){SpawnGroupData data=super.finalizeSpawn(level,difficulty,spawnType,spawnData,dataTag);junkie=getRandom().nextBoolean();updateName();return data;}

    @Override public InteractionResult mobInteract(Player player,InteractionHand hand){
        if(level().isClientSide)return InteractionResult.SUCCESS;if(hand!=InteractionHand.MAIN_HAND)return InteractionResult.PASS;if(!(player instanceof ServerPlayer sp))return InteractionResult.CONSUME;
        ContractInteractionEvents.recordConversation(sp,new ResourceLocation("matteroverdrive","mad_scientist"));CompoundTag persisted=player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);boolean active=persisted.getBoolean(QUEST_ACTIVE),done=persisted.getBoolean(QUEST_DONE);
        if(!done&&active){if(hasAllRogueParts(player)){consumeRogueParts(player);if(!AndroidData.isAndroid(player))AndroidData.activate(player);completePunyHumans(sp,persisted,true);return InteractionResult.CONSUME;}if(!persisted.getBoolean(QUEST_PARTS_MODE)&&AndroidData.isAndroid(player)){completePunyHumans(sp,persisted,false);return InteractionResult.CONSUME;}dialogue(sp,"Puny Humans",List.of("Biology remains inefficient. Bring me one Head, Chest, Arms and Legs Rogue Android part.",partProgress(player),"Once I have a complete mechanical anatomy set, we can begin the conversion."));return InteractionResult.CONSUME;}
        if(!done&&!active&&!AndroidData.isAndroid(player)){persisted.putBoolean(QUEST_ACTIVE,true);persisted.putBoolean(QUEST_PARTS_MODE,true);player.getPersistentData().put(Player.PERSISTED_NBT_TAG,persisted);level().playSound(null,blockPosition(),ModSounds.get("gui.quest_started").get(),net.minecraft.sounds.SoundSource.NEUTRAL,1,1);dialogue(sp,"Puny Humans",List.of("Humanity has had a very respectable trial period. The results are disappointing.","Recover one of each Rogue Android body part and return to me.","Head, Chest, Arms and Legs. Try not to damage the interesting bits."));return InteractionResult.CONSUME;}
        if(done&&ScientistStoryQuestFlow.handle(sp))return InteractionResult.CONSUME;
        if(junkie&&done)return handleCocktail(sp);
        dialogue(sp,"Research Notes",List.of(ScientistStoryQuestFlow.status(sp),AndroidData.isAndroid(player)?"Your synthetic conversion appears stable. Continue field research.":"Bring me enough machine anatomy and perhaps biology can be corrected."));return InteractionResult.CONSUME;
    }

    private void completePunyHumans(ServerPlayer p,CompoundTag d,boolean converted){d.putBoolean(QUEST_DONE,true);d.putBoolean(QUEST_ACTIVE,false);p.getPersistentData().put(Player.PERSISTED_NBT_TAG,d);give(p,new ItemStack(ModItems.get("battery").get()));give(p,new ItemStack(ModItems.get("android_pill_blue").get()));give(p,new ItemStack(ModItems.get("android_pill_yellow").get(),5));p.giveExperiencePoints(256);level().playSound(null,blockPosition(),ModSounds.get("gui.quest_complete").get(),net.minecraft.sounds.SoundSource.NEUTRAL,1,1);dialogue(p,"Puny Humans Complete",List.of(converted?"Conversion sequence complete. You are now considerably less disappointing.":"Your synthetic conversion is already stable. Convenient.","Reward: Battery, Android Pills, 256 XP","Your probationary research clearance is active. Return for field assignments."));}

    private InteractionResult handleCocktail(ServerPlayer p){CompoundTag d=CocktailQuestEvents.persisted(p);if(d.getBoolean(CocktailQuestEvents.DONE)){dialogue(p,"Cocktail of Ascension",List.of("The Cocktail experiment has already run its course. The paperwork remains classified as a biohazard."));return InteractionResult.CONSUME;}if(!d.getBoolean(CocktailQuestEvents.ACTIVE)){d.putBoolean(CocktailQuestEvents.ACTIVE,true);d.putInt(CocktailQuestEvents.CREEPER_KILLS,0);d.putInt(CocktailQuestEvents.GUNPOWDER,0);d.putInt(CocktailQuestEvents.MUSHROOMS,0);level().playSound(null,blockPosition(),ModSounds.get("gui.quest_started").get(),net.minecraft.sounds.SoundSource.NEUTRAL,1,.9F);dialogue(p,"Cocktail of Ascension",List.of("I have a formula. It is either transformative or explosively educational.","Kill 5 Creepers with a shovel, collect 5 gunpowder, and collect 5 red mushrooms in the Nether.","Do not ask why the shovel matters. Methodology matters."));return InteractionResult.CONSUME;}int k=d.getInt(CocktailQuestEvents.CREEPER_KILLS),g=d.getInt(CocktailQuestEvents.GUNPOWDER),m=d.getInt(CocktailQuestEvents.MUSHROOMS);if(k<5||g<5||m<5){dialogue(p,"Cocktail of Ascension",List.of("The mixture is still incomplete.","Shovel Creepers: "+k+"/5   Gunpowder: "+g+"/5   Nether red mushrooms: "+m+"/5"));return InteractionResult.CONSUME;}if(!(level() instanceof ServerLevel sl))return InteractionResult.CONSUME;MutantScientistEntity mutant=ModEntities.MUTANT_SCIENTIST.get().create(sl);if(mutant==null)return InteractionResult.CONSUME;mutant.moveTo(getX(),getY(),getZ(),getYRot(),getXRot());if(!sl.addFreshEntity(mutant)){mutant.discard();return InteractionResult.CONSUME;}d.putBoolean(CocktailQuestEvents.ACTIVE,false);d.putBoolean(CocktailQuestEvents.DONE,true);p.giveExperiencePoints(512);give(p,new ItemStack(ModItems.get("android_pill_blue").get()));give(p,new ItemStack(ModItems.get("android_pill_red").get()));give(p,new ItemStack(ModItems.get("android_pill_yellow").get()));level().playSound(null,blockPosition(),ModSounds.get("failed_animal_die").get(),net.minecraft.sounds.SoundSource.HOSTILE,1,.8F);dialogue(p,"Cocktail of Ascension Complete",List.of("The transformation succeeded according to the broadest possible definition of succeeded.","Reward: Android Pills, 512 XP","You may wish to move away from the experiment."));discard();return InteractionResult.CONSUME;}

    private static void dialogue(ServerPlayer p,String title,List<String> lines){ModNetwork.openDialogue(p,"Mad Scientist",title,lines);}private static boolean hasAllRogueParts(Player p){for(AndroidData.Part part:AndroidData.Part.values())if(p.getInventory().countItem(ModItems.get(part.itemId).get())<1)return false;return true;}private static String partProgress(Player p){StringBuilder t=new StringBuilder("Parts: ");for(int i=0;i<AndroidData.Part.values().length;i++){AndroidData.Part part=AndroidData.Part.values()[i];if(i>0)t.append(", ");t.append(part.name()).append(' ').append(p.getInventory().countItem(ModItems.get(part.itemId).get())>0?"READY":"MISSING");}return t.toString();}private static void consumeRogueParts(Player p){for(AndroidData.Part part:AndroidData.Part.values())removeItems(p,ModItems.get(part.itemId).get(),1);}private static void removeItems(Player p,Item item,int amount){int r=amount;for(int slot=0;slot<p.getInventory().getContainerSize()&&r>0;slot++){ItemStack s=p.getInventory().getItem(slot);if(!s.is(item))continue;int x=Math.min(r,s.getCount());s.shrink(x);r-=x;}p.getInventory().setChanged();}private static void give(Player p,ItemStack s){if(!p.getInventory().add(s))p.drop(s,false);}private void updateName(){setCustomName(Component.literal(junkie?"Mad Scientist (Junkie)":"Mad Scientist").withStyle(junkie?ChatFormatting.LIGHT_PURPLE:ChatFormatting.AQUA));setCustomNameVisible(false);}public boolean isJunkie(){return junkie;}@Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putBoolean(JUNKIE,junkie);}@Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);junkie=tag.getBoolean(JUNKIE);updateName();}@Nullable @Override public Villager getBreedOffspring(ServerLevel level,AgeableMob mate){return ModEntities.MAD_SCIENTIST.get().create(level);}
}
