package matteroverdrive.menu;

import matteroverdrive.blockentity.MicrowaveBlockEntity;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.machine.MachineRedstoneMode;
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

public class MicrowaveMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS=7,PLAYER_INV_START=7,PLAYER_INV_END=34,HOTBAR_END=43;
    private final MicrowaveBlockEntity machine;private final ContainerData data;
    public MicrowaveMenu(int id,Inventory inv,FriendlyByteBuf buf){this(id,inv,getMachine(inv,buf.readBlockPos()),new SimpleContainerData(9));}
    public MicrowaveMenu(int id,Inventory inv,MicrowaveBlockEntity machine){this(id,inv,machine,machine.getContainerData());}
    private MicrowaveMenu(int id,Inventory inv,MicrowaveBlockEntity machine,ContainerData data){super(ModMenus.MICROWAVE.get(),id);this.machine=machine;this.data=data;addSlot(new SlotItemHandler(machine.getItemHandler(),0,26,44));addSlot(new SlotItemHandler(machine.getItemHandler(),1,80,44));addSlot(new SlotItemHandler(machine.getItemHandler(),2,134,44){@Override public boolean mayPlace(ItemStack stack){return false;}});for(int s=0;s<4;s++)addSlot(new SlotItemHandler(machine.getUpgradeInventory(),s,53+s*18,66));addPlayerInventory(inv);addDataSlots(data);}
    private static MicrowaveBlockEntity getMachine(Inventory inv,BlockPos pos){BlockEntity be=inv.player.level().getBlockEntity(pos);if(be instanceof MicrowaveBlockEntity m)return m;throw new IllegalStateException("Microwave missing at "+pos);}
    private void addPlayerInventory(Inventory inv){for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inv,c+r*9+9,8+c*18,102+r*18));for(int c=0;c<9;c++)addSlot(new Slot(inv,c,8+c*18,160));}
    @Override public ItemStack quickMoveStack(Player player,int index){Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;ItemStack source=slot.getItem(),copy=source.copy();boolean moved;if(index<MACHINE_SLOTS)moved=moveItemStackTo(source,PLAYER_INV_START,HOTBAR_END,true);else{moved=false;if(source.getItem() instanceof MachineUpgradeItem)moved=moveItemStackTo(source,3,MACHINE_SLOTS,false);if(!moved&&source.getCapability(ForgeCapabilities.ENERGY).map(e->e.canExtract()).orElse(false))moved=moveItemStackTo(source,1,2,false);if(!moved&&source.isEdible())moved=moveItemStackTo(source,0,1,false);if(!moved)moved=index<PLAYER_INV_END?moveItemStackTo(source,PLAYER_INV_END,HOTBAR_END,false):moveItemStackTo(source,PLAYER_INV_START,PLAYER_INV_END,false);}if(!moved)return ItemStack.EMPTY;if(source.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,source);return copy;}
    @Override public boolean stillValid(Player player){return stillValid(ContainerLevelAccess.create(machine.getLevel(),machine.getBlockPos()),player,ModBlocks.get("microwave").get());}
    public int getProgress(){return data.get(0);}public int getMaxProgress(){return Math.max(1,data.get(1));}public int getEnergy(){return combine(data.get(2),data.get(3));}public int getEnergyCapacity(){return combine(data.get(4),data.get(5));}public int getEnergyPerTick(){return data.get(6)&0xFFFF;}public boolean isRunning(){return data.get(7)!=0;}public int getRedstoneMode(){return data.get(8);}public String getRedstoneModeLabel(){return MachineRedstoneMode.label(getRedstoneMode());}
    @Override public boolean clickMenuButton(Player player,int id){if(id==1){if(!ServerDebugAccess.require(player))return false;boolean enabled=machine.getEnergyStorage().toggleInfiniteEnergy();player.displayClientMessage(net.minecraft.network.chat.Component.literal("[DEBUG] Infinite energy: "+(enabled?"ON":"OFF")),true);return true;}if(id==2){int mode=machine.cycleRedstoneMode();player.displayClientMessage(net.minecraft.network.chat.Component.literal("Redstone mode: "+MachineRedstoneMode.label(mode)),true);return true;}return false;}
    private static int combine(int low,int high){return(low&0xFFFF)|((high&0xFFFF)<<16);}
}
