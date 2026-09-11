package matteroverdrive.menu;

import matteroverdrive.blockentity.MatterAnalyzerBlockEntity;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.matter.MatterValueRegistry;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModMenus;
import matteroverdrive.security.ServerDebugAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class MatterAnalyzerMenu extends AbstractContainerMenu {
 private static final int MACHINE_SLOTS=7,PLAYER_INV_START=7,PLAYER_INV_END=34,HOTBAR_END=43;private final MatterAnalyzerBlockEntity analyzer;private final ContainerData data;
 public MatterAnalyzerMenu(int id,Inventory inv,FriendlyByteBuf buf){this(id,inv,get(inv,buf.readBlockPos()),new SimpleContainerData(13));}public MatterAnalyzerMenu(int id,Inventory inv,MatterAnalyzerBlockEntity a){this(id,inv,a,a.getContainerData());}private MatterAnalyzerMenu(int id,Inventory inv,MatterAnalyzerBlockEntity a,ContainerData d){super(ModMenus.MATTER_ANALYZER.get(),id);analyzer=a;data=d==null?new SimpleContainerData(13):d;addSlot(new SlotItemHandler(a.getItemHandler(),0,26,44));addSlot(new SlotItemHandler(a.getItemHandler(),1,80,44));addSlot(new SlotItemHandler(a.getItemHandler(),2,134,44));for(int s=0;s<4;s++)addSlot(new SlotItemHandler(a.getUpgradeInventory(),s,53+s*18,80));for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inv,c+r*9+9,8+c*18,114+r*18));for(int c=0;c<9;c++)addSlot(new Slot(inv,c,8+c*18,172));addDataSlots(data);}private static MatterAnalyzerBlockEntity get(Inventory inv,BlockPos p){BlockEntity be=inv.player.level().getBlockEntity(p);if(be instanceof MatterAnalyzerBlockEntity a)return a;throw new IllegalStateException("Matter Analyzer missing at "+p);}
 @Override public boolean stillValid(Player p){return stillValid(ContainerLevelAccess.create(analyzer.getLevel(),analyzer.getBlockPos()),p,ModBlocks.get("matter_analyzer").get());}@Override public ItemStack quickMoveStack(Player p,int index){ItemStack empty=ItemStack.EMPTY;Slot slot=slots.get(index);if(!slot.hasItem())return empty;ItemStack source=slot.getItem(),copy=source.copy();if(index<MACHINE_SLOTS){if(!moveItemStackTo(source,PLAYER_INV_START,HOTBAR_END,true))return empty;}else{boolean moved=false;if(source.getItem() instanceof MachineUpgradeItem)moved=moveItemStackTo(source,3,MACHINE_SLOTS,false);if(!moved&&source.getCapability(ForgeCapabilities.ENERGY).map(e->e.canExtract()).orElse(false))moved=moveItemStackTo(source,1,2,false);if(!moved&&MatterValueRegistry.containsMatter(source))moved=moveItemStackTo(source,0,1,false);if(!moved)moved=index<PLAYER_INV_END?moveItemStackTo(source,PLAYER_INV_END,HOTBAR_END,false):moveItemStackTo(source,PLAYER_INV_START,PLAYER_INV_END,false);if(!moved)return empty;}if(source.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,source);return copy;}
 public int getProgress(){return data.get(0);}public int getMaxProgress(){return data.get(1);}public int getEnergy(){return combine(data.get(2),data.get(3));}public int getEnergyCapacity(){return combine(data.get(4),data.get(5));}public int getPatternProgress(){return data.get(6)&0xffff;}public int getInputMatter(){return data.get(7)&0xffff;}public int getEnergyPerTick(){return data.get(8)&0xffff;}public int getRedstoneMode(){return data.get(9);}public int getResearchProgress(){return data.get(10);}public int getResearchMaxProgress(){return data.get(11);}public boolean hasQueuedResearch(){return data.get(12)!=0;}private static int combine(int lo,int hi){return(lo&0xffff)|((hi&0xffff)<<16);}
 @Override public boolean clickMenuButton(Player p,int id){if(id==1){if(!ServerDebugAccess.require(p))return false;boolean enabled=analyzer.getEnergyStorage().toggleInfiniteEnergy();p.displayClientMessage(net.minecraft.network.chat.Component.literal("[DEBUG] Infinite energy: "+(enabled?"ON":"OFF")),true);return true;}if(id==2){var mode=analyzer.cycleRedstoneMode();p.displayClientMessage(net.minecraft.network.chat.Component.literal("Redstone mode: "+mode.getSerializedName().toUpperCase()),true);return true;}return false;}
}
