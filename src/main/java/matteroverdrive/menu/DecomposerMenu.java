package matteroverdrive.menu;

import matteroverdrive.blockentity.DecomposerBlockEntity;
import matteroverdrive.machine.MachineRedstoneMode;
import matteroverdrive.matter.MatterValueRegistry;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModMenus;
import matteroverdrive.security.ServerDebugAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class DecomposerMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS=7,PLAYER_INV_START=7,PLAYER_INV_END=34,HOTBAR_END=43;
    private final DecomposerBlockEntity decomposer;private final ContainerData data;
    public DecomposerMenu(int id,Inventory inv,FriendlyByteBuf buf){this(id,inv,getDecomposer(inv,buf.readBlockPos()),new SimpleContainerData(13));}
    public DecomposerMenu(int id,Inventory inv,DecomposerBlockEntity decomposer){this(id,inv,decomposer,decomposer.getContainerData());}
    private DecomposerMenu(int id,Inventory inv,DecomposerBlockEntity decomposer,ContainerData data){super(ModMenus.DECOMPOSER.get(),id);this.decomposer=decomposer;this.data=data==null?new SimpleContainerData(13):data;addSlot(new SlotItemHandler(decomposer.getItemHandler(),0,26,44));addSlot(new SlotItemHandler(decomposer.getItemHandler(),1,80,44));addSlot(new SlotItemHandler(decomposer.getItemHandler(),2,134,44){@Override public boolean mayPlace(ItemStack stack){return false;}});for(int s=0;s<4;s++)addSlot(new SlotItemHandler(decomposer.getUpgradeInventory(),s,53+s*18,80));addPlayerInventory(inv);addDataSlots(this.data);}
    private static DecomposerBlockEntity getDecomposer(Inventory inv,BlockPos pos){BlockEntity be=inv.player.level().getBlockEntity(pos);if(be instanceof DecomposerBlockEntity d)return d;throw new IllegalStateException("Matter Overdrive decomposer block entity missing at "+pos);}
    private void addPlayerInventory(Inventory inv){for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inv,c+r*9+9,8+c*18,114+r*18));for(int c=0;c<9;c++)addSlot(new Slot(inv,c,8+c*18,172));}
    @Override public ItemStack quickMoveStack(Player player,int index){Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;ItemStack source=slot.getItem(),copy=source.copy();boolean moved;if(index<MACHINE_SLOTS)moved=moveItemStackTo(source,PLAYER_INV_START,HOTBAR_END,true);else{moved=false;if(source.getItem() instanceof MachineUpgradeItem)moved=moveItemStackTo(source,3,MACHINE_SLOTS,false);if(!moved&&source.getCapability(ForgeCapabilities.ENERGY).map(e->e.canExtract()).orElse(false))moved=moveItemStackTo(source,1,2,false);if(!moved&&MatterValueRegistry.containsMatter(source))moved=moveItemStackTo(source,0,1,false);if(!moved)moved=index<PLAYER_INV_END?moveItemStackTo(source,PLAYER_INV_END,HOTBAR_END,false):moveItemStackTo(source,PLAYER_INV_START,PLAYER_INV_END,false);}if(!moved)return ItemStack.EMPTY;if(source.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,source);return copy;}
    @Override public boolean stillValid(Player player){return stillValid(ContainerLevelAccess.create(decomposer.getLevel(),decomposer.getBlockPos()),player,ModBlocks.get("decomposer").get());}
    public int getProgress(){return data.get(0);}public int getMaxProgress(){return data.get(1);}public int getEnergy(){return combineWords(data.get(2),data.get(3));}public int getEnergyCapacity(){return combineWords(data.get(4),data.get(5));}public int getMatter(){return data.get(6)&0xFFFF;}public int getMatterCapacity(){return data.get(7)&0xFFFF;}public int getInputMatterValue(){return data.get(8)&0xFFFF;}public int getEnergyPerTick(){return data.get(9)&0xFFFF;}public double getFailureChancePercent(){return combineWords(data.get(10),data.get(11))/10000.0D;}public int getRedstoneMode(){return data.get(12);}public String getRedstoneModeLabel(){return MachineRedstoneMode.label(getRedstoneMode());}
    private static int combineWords(int low,int high){return(low&0xFFFF)|((high&0xFFFF)<<16);}
    @Override public boolean clickMenuButton(Player player,int id){if(id==1){if(!ServerDebugAccess.require(player))return false;boolean enabled=decomposer.getEnergyStorage().toggleInfiniteEnergy();player.displayClientMessage(net.minecraft.network.chat.Component.literal("[DEBUG] Infinite energy: "+(enabled?"ON":"OFF")),true);return true;}if(id==2){int mode=decomposer.cycleRedstoneMode();player.displayClientMessage(net.minecraft.network.chat.Component.literal("Redstone mode: "+MachineRedstoneMode.label(mode)),true);return true;}return false;}
}
