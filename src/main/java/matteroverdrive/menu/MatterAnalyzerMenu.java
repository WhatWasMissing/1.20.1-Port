package matteroverdrive.menu;

import matteroverdrive.blockentity.MatterAnalyzerBlockEntity;
import matteroverdrive.item.PatternDriveItem;
import matteroverdrive.matter.MatterValueRegistry;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class MatterAnalyzerMenu extends AbstractContainerMenu{
    private static final int MACHINE_SLOTS=3,PLAYER_INV_START=3,PLAYER_INV_END=30,HOTBAR_END=39;private final MatterAnalyzerBlockEntity machine;private final ContainerData data;
    public MatterAnalyzerMenu(int id,Inventory inv,FriendlyByteBuf buf){this(id,inv,getMachine(inv,buf.readBlockPos()),new SimpleContainerData(9));}
    public MatterAnalyzerMenu(int id,Inventory inv,MatterAnalyzerBlockEntity machine){this(id,inv,machine,machine.getContainerData());}
    private MatterAnalyzerMenu(int id,Inventory inv,MatterAnalyzerBlockEntity machine,ContainerData data){super(ModMenus.MATTER_ANALYZER.get(),id);this.machine=machine;this.data=data;addSlot(new SlotItemHandler(machine.getItemHandler(),0,26,44));addSlot(new SlotItemHandler(machine.getItemHandler(),1,80,44));addSlot(new SlotItemHandler(machine.getItemHandler(),2,134,44));addPlayer(inv);addDataSlots(data);}
    private static MatterAnalyzerBlockEntity getMachine(Inventory inv,BlockPos pos){BlockEntity be=inv.player.level().getBlockEntity(pos);if(be instanceof MatterAnalyzerBlockEntity m)return m;throw new IllegalStateException("Matter Analyzer missing at "+pos);}
    private void addPlayer(Inventory inv){for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inv,c+r*9+9,8+c*18,84+r*18));for(int c=0;c<9;c++)addSlot(new Slot(inv,c,8+c*18,142));}
    @Override public ItemStack quickMoveStack(Player player,int index){Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;ItemStack src=slot.getItem(),copy=src.copy();boolean moved;if(index<MACHINE_SLOTS)moved=moveItemStackTo(src,PLAYER_INV_START,HOTBAR_END,true);else{moved=false;if(src.getItem() instanceof PatternDriveItem)moved=moveItemStackTo(src,2,3,false);if(!moved&&src.getCapability(ForgeCapabilities.ENERGY).map(e->e.canExtract()).orElse(false))moved=moveItemStackTo(src,1,2,false);if(!moved&&MatterValueRegistry.containsMatter(src))moved=moveItemStackTo(src,0,1,false);if(!moved)moved=index<PLAYER_INV_END?moveItemStackTo(src,PLAYER_INV_END,HOTBAR_END,false):moveItemStackTo(src,PLAYER_INV_START,PLAYER_INV_END,false);}if(!moved)return ItemStack.EMPTY;if(src.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,src);return copy;}
    @Override public boolean stillValid(Player p){return stillValid(ContainerLevelAccess.create(machine.getLevel(),machine.getBlockPos()),p,ModBlocks.get("matter_analyzer").get());}
    public int getProgress(){return data.get(0);}public int getMaxProgress(){return data.get(1);}public int getEnergy(){return combine(data.get(2),data.get(3));}public int getEnergyCapacity(){return combine(data.get(4),data.get(5));}public int getPatternProgress(){return data.get(6)&0xFFFF;}public int getInputMatter(){return data.get(7)&0xFFFF;}public int getEnergyPerTick(){return data.get(8)&0xFFFF;}private static int combine(int l,int h){return(l&0xFFFF)|((h&0xFFFF)<<16);}
}
