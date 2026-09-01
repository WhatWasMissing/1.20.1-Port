package matteroverdrive.item;

import matteroverdrive.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.List;

public class ContractItem extends Item {
    public static final String TYPE = "Type", TARGET = "Target", GOAL = "Goal", PROGRESS = "Progress", REWARD = "Reward", REWARD_COUNT = "RewardCount";
    public ContractItem(Properties properties) { super(properties.stacksTo(1)); }
    public static ItemStack collect(String itemId, int goal, String rewardId, int rewardCount) { return create("collect", itemId, goal, rewardId, rewardCount); }
    public static ItemStack hunt(String entityId, int goal, String rewardId, int rewardCount) { return create("hunt", entityId, goal, rewardId, rewardCount); }
    private static ItemStack create(String type,String target,int goal,String reward,int rewardCount) {
        ItemStack contract=new ItemStack(ModItems.get("contract").get()); CompoundTag tag=contract.getOrCreateTag();
        tag.putString(TYPE,type);tag.putString(TARGET,target);tag.putInt(GOAL,goal);tag.putInt(PROGRESS,0);tag.putString(REWARD,reward);tag.putInt(REWARD_COUNT,rewardCount);return contract;
    }
    public static boolean advancesWithPickup(ItemStack contract, ItemStack picked) {
        return "collect".equals(contract.getOrCreateTag().getString(TYPE)) && picked.is(new ResourceLocation(contract.getOrCreateTag().getString(TARGET)));
    }
    public static boolean advancesWithKill(ItemStack contract, ResourceLocation entity) {
        return "hunt".equals(contract.getOrCreateTag().getString(TYPE)) && entity.toString().equals(contract.getOrCreateTag().getString(TARGET));
    }
    public static void advance(ItemStack contract,int amount){CompoundTag tag=contract.getOrCreateTag();tag.putInt(PROGRESS,Math.min(tag.getInt(GOAL),tag.getInt(PROGRESS)+Math.max(0,amount)));}
    public static boolean complete(ItemStack contract){CompoundTag tag=contract.getOrCreateTag();return tag.getInt(GOAL)>0&&tag.getInt(PROGRESS)>=tag.getInt(GOAL);}
    public static ItemStack reward(ItemStack contract){CompoundTag tag=contract.getOrCreateTag();ResourceLocation id=new ResourceLocation(tag.getString(REWARD));return new ItemStack(net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(id),Math.max(1,tag.getInt(REWARD_COUNT)));}
    @Override public boolean isFoil(ItemStack stack){return complete(stack);}
    @Override public void appendHoverText(ItemStack stack,@Nullable Level level,List<Component> tooltip,TooltipFlag flag){
        CompoundTag tag=stack.getOrCreateTag();boolean hunt="hunt".equals(tag.getString(TYPE));String target=tag.getString(TARGET).replace("minecraft:","").replace("matteroverdrive:","");
        tooltip.add(Component.literal(hunt?"Eliminate ":"Collect ").append(Component.literal(target+" x"+tag.getInt(GOAL))).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Progress: "+tag.getInt(PROGRESS)+" / "+tag.getInt(GOAL)).withStyle(complete(stack)?ChatFormatting.GREEN:ChatFormatting.GRAY));
        tooltip.add(Component.literal("Reward: "+tag.getInt(REWARD_COUNT)+"x "+tag.getString(REWARD).replace("matteroverdrive:","")).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal(complete(stack)?"Return to a Contract Market to redeem":"Keep this contract in your inventory.").withStyle(ChatFormatting.DARK_GRAY));
    }
}