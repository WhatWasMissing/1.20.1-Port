package matteroverdrive.menu;

import matteroverdrive.blockentity.ReplicatorBlockEntity;
import matteroverdrive.item.PatternDriveItem;
import matteroverdrive.item.MachineUpgradeItem;
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

public class ReplicatorMenu extends AbstractContainerMenu{
    private static final int MACHINE_SLOTS=8,PLAYER_INV_START=8,PLAYER_INV_END=35,HOTBAR_END=44;private final ReplicatorBlockEntity machine;private final ContainerData data;
    public ReplicatorMenu(int id,Inventory inv,FriendlyByteBuf buf){this(id,inv,getMachine(inv,buf.readBlockPos()),new SimpleContainerData(13));}
    public ReplicatorMenu(int id,Inventory inv,ReplicatorBlockEntity machine){this(id,inv,machine,machine.getContainerData());}
    private ReplicatorMenu(int id,Inventory inv,ReplicatorBlockEntity machine,ContainerData data){super(ModMenus.REPLICATOR.get(),id);this.machine=machine;this.data=data;addSlot(new SlotItemHandler(machine.getItemHandler(),0,20,44));addSlot(new SlotItemHandler(machine.getItemHandler(),1,62,44));addSlot(new SlotItemHandler(machine.getItemHandler(),2,116,35){@Override public boolean mayPlace(ItemStack s){return false;}});addSlot(new SlotItemHandler(machine.getItemHandler(),3,140,53){@Override public boolean mayPlace(ItemStack s){return false;}});for(int slot=0;slot<ReplicatorBlockEntity.UPGRADE_SLOT_COUNT;slot++)addSlot(new SlotItemHandler(machine.getUpgradeInventory(),slot,53+slot*18,102));addPlayer(inv);addDataSlots(data);}
    private static ReplicatorBlockEntity getMachine(Inventory inv,BlockPos pos){BlockEntity be=inv.player.level().getBlockEntity(pos);if(be instanceof ReplicatorBlockEntity m)return m;throw new IllegalStateException("Replicator missing at "+pos);}
    private void addPlayer(Inventory inv){for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inv,c+r*9+9,8+c*18,136+r*18));for(int c=0;c<9;c++)addSlot(new Slot(inv,c,8+c*18,194));}
    @Override public ItemStack quickMoveStack(Player player,int index){Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;ItemStack src=slot.getItem(),copy=src.copy();boolean moved;if(index<MACHINE_SLOTS)moved=moveItemStackTo(src,PLAYER_INV_START,HOTBAR_END,true);else{moved=false;if(src.getItem() instanceof MachineUpgradeItem)moved=moveItemStackTo(src,4,MACHINE_SLOTS,false);if(!moved&&src.getItem() instanceof PatternDriveItem)moved=moveItemStackTo(src,0,1,false);if(!moved&&src.getCapability(ForgeCapabilities.ENERGY).map(e->e.canExtract()).orElse(false))moved=moveItemStackTo(src,1,2,false);if(!moved)moved=index<PLAYER_INV_END?moveItemStackTo(src,PLAYER_INV_END,HOTBAR_END,false):moveItemStackTo(src,PLAYER_INV_START,PLAYER_INV_END,false);}if(!moved)return ItemStack.EMPTY;if(src.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,src);return copy;}
    @Override public boolean stillValid(Player p){return stillValid(ContainerLevelAccess.create(machine.getLevel(),machine.getBlockPos()),p,ModBlocks.get("replicator").get());}
    public int getProgress(){return data.get(0);}public int getMaxProgress(){return data.get(1);}public int getEnergy(){return combine(data.get(2),data.get(3));}public int getEnergyCapacity(){return combine(data.get(4),data.get(5));}public int getMatter(){return data.get(6)&0xFFFF;}public int getMatterCapacity(){return data.get(7)&0xFFFF;}public int getPatternMatter(){return data.get(8)&0xFFFF;}public int getPatternProgress(){return data.get(9)&0xFFFF;}public int getEnergyPerTick(){return data.get(10)&0xFFFF;}public double getFailChance(){return(data.get(11)&0xFFFF)/10000.0D;}public int getNetworkTaskAmount(){return data.get(12)&0xFFFF;}private static int combine(int l,int h){return(l&0xFFFF)|((h&0xFFFF)<<16);}

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != 1) return false;
        boolean enabled = machine.getEnergyStorage().toggleInfiniteEnergy();
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "[DEBUG] Infinite energy: " + (enabled ? "ON" : "OFF")), true);
        return true;
    }
}
