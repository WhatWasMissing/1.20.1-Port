package matteroverdrive.menu;

import matteroverdrive.blockentity.ChargingStationBlockEntity;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.machine.MachineRedstoneMode;
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

public class ChargingStationMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS=1+ChargingStationBlockEntity.UPGRADE_SLOT_COUNT,PLAYER_START=MACHINE_SLOTS,PLAYER_END=PLAYER_START+27,HOTBAR_END=PLAYER_END+9;private final ChargingStationBlockEntity station;private final ContainerData data;
    public ChargingStationMenu(int id,Inventory inv,FriendlyByteBuf buf){this(id,inv,find(inv,buf.readBlockPos()),new SimpleContainerData(18));}public ChargingStationMenu(int id,Inventory inv,ChargingStationBlockEntity s){this(id,inv,s,s.getContainerData());}
    private ChargingStationMenu(int id,Inventory inv,ChargingStationBlockEntity s,ContainerData d){super(ModMenus.CHARGING_STATION.get(),id);station=s;data=d;addSlot(new SlotItemHandler(s.getBatteryInventory(),0,80,42));for(int slot=0;slot<4;slot++)addSlot(new SlotItemHandler(s.getUpgradeInventory(),slot,53+slot*18,68));for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inv,c+r*9+9,8+c*18,112+r*18));for(int c=0;c<9;c++)addSlot(new Slot(inv,c,8+c*18,170));addDataSlots(d);}
    private static ChargingStationBlockEntity find(Inventory inv,BlockPos pos){BlockEntity e=inv.player.level().getBlockEntity(pos);if(e instanceof ChargingStationBlockEntity s)return s;throw new IllegalStateException("Charging Station missing at "+pos);}
    @Override public ItemStack quickMoveStack(Player p,int index){Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;ItemStack src=slot.getItem(),copy=src.copy();boolean moved;if(index<MACHINE_SLOTS)moved=moveItemStackTo(src,PLAYER_START,HOTBAR_END,true);else{moved=false;if(src.getItem() instanceof MachineUpgradeItem)moved=moveItemStackTo(src,1,MACHINE_SLOTS,false);if(!moved&&src.getCapability(ForgeCapabilities.ENERGY).map(e->e.canReceive()).orElse(false))moved=moveItemStackTo(src,0,1,false);if(!moved)moved=index<PLAYER_END?moveItemStackTo(src,PLAYER_END,HOTBAR_END,false):moveItemStackTo(src,PLAYER_START,PLAYER_END,false);}if(!moved)return ItemStack.EMPTY;if(src.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,src);return copy;}
    @Override public boolean stillValid(Player p){return stillValid(ContainerLevelAccess.create(station.getLevel(),station.getBlockPos()),p,ModBlocks.get("charging_station").get());}
    @Override public boolean clickMenuButton(Player p,int id){if(id==1){if(!ServerDebugAccess.require(p))return false;boolean enabled=station.getEnergyStorage().toggleInfiniteEnergy();p.displayClientMessage(net.minecraft.network.chat.Component.literal("[DEBUG] Infinite energy: "+(enabled?"ON":"OFF")),true);return true;}if(id==2){int mode=station.cycleRedstoneMode();p.displayClientMessage(net.minecraft.network.chat.Component.literal("Redstone mode: "+MachineRedstoneMode.label(mode)),true);return true;}return false;}
    public int batteryEnergy(){return combine(data.get(0),data.get(1));}public int batteryCapacity(){return combine(data.get(2),data.get(3));}public int stationEnergy(){return combine(data.get(4),data.get(5));}public int stationCapacity(){return combine(data.get(6),data.get(7));}public int lastItemTransferred(){return data.get(8);}public int lastAndroidTransferred(){return data.get(9);}public int androidsCharged(){return data.get(10);}public int androidRange(){return data.get(11);}public int maxAndroidCharge(){return data.get(12);}public int redstoneMode(){return data.get(13);}public int lastDroneTransferred(){return data.get(14);}public int dronesCharged(){return data.get(15);}public int lastDroneRepairEnergy(){return data.get(16);}public int dronesRepaired(){return data.get(17);}public String redstoneModeLabel(){return MachineRedstoneMode.label(redstoneMode());}private static int combine(int l,int h){return(l&0xffff)|((h&0xffff)<<16);}
}
