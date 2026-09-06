package matteroverdrive.blockentity;

import matteroverdrive.block.MatterRecyclerBlock;
import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.item.MatterDustItem;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.item.MachineUpgradeInventory;
import matteroverdrive.machine.MachineRedstoneMode;
import matteroverdrive.menu.MatterRecyclerMenu;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class MatterRecyclerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT=0, ENERGY_SLOT=1, OUTPUT_SLOT=2, SLOT_COUNT=3, UPGRADE_SLOT_COUNT=4;
    public static final int ENERGY_STORAGE=512000, RECYCLE_SPEED_PER_MATTER=80, RECYCLE_ENERGY_PER_MATTER=1000, ENERGY_ITEM_TRANSFER_PER_TICK=16000;
    private final ItemStackHandler items=new ItemStackHandler(SLOT_COUNT){@Override public boolean isItemValid(int slot,ItemStack stack){return switch(slot){case INPUT_SLOT->stack.getItem() instanceof MatterDustItem dust&&!dust.isRefined()&&MatterDustItem.getMatter(stack)>0;case ENERGY_SLOT->stack.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::canExtract).orElse(false);case OUTPUT_SLOT->false;default->false;};}@Override protected void onContentsChanged(int slot){setChanged();}};
    private final MachineEnergyStorage energyStorage=new MachineEnergyStorage(ENERGY_STORAGE,ENERGY_STORAGE,ENERGY_STORAGE,this::setChanged);
    private final MachineUpgradeInventory upgrades=new MachineUpgradeInventory(UPGRADE_SLOT_COUNT,upgrade->upgrade==MachineUpgradeItem.Upgrade.SPEED||upgrade==MachineUpgradeItem.Upgrade.POWER||upgrade==MachineUpgradeItem.Upgrade.POWER_STORAGE||upgrade==MachineUpgradeItem.Upgrade.HYPER_SPEED,this::onUpgradesChanged);
    private LazyOptional<IItemHandler> itemCapability=LazyOptional.of(()->items);private LazyOptional<IEnergyStorage> energyCapability=LazyOptional.of(()->energyStorage);
    private int recycleTime;private boolean running;private int redstoneMode=MachineRedstoneMode.DISABLED;
    private final ContainerData data=new ContainerData(){@Override public int get(int index){return switch(index){case 0->recycleTime;case 1->getSpeed();case 2->lowWord(energyStorage.getEnergyStored());case 3->highWord(energyStorage.getEnergyStored());case 4->lowWord(energyStorage.getMaxEnergyStored());case 5->highWord(energyStorage.getMaxEnergyStored());case 6->getRecycleMatter();case 7->getEnergyDrainPerTick();case 8->redstoneMode;default->0;};}@Override public void set(int index,int value){if(index==0)recycleTime=Math.max(0,value);}@Override public int getCount(){return 9;}};
    public MatterRecyclerBlockEntity(BlockPos pos,BlockState state){super(ModBlockEntities.MATTER_RECYCLER.get(),pos,state);}
    public static void serverTick(Level level,BlockPos pos,BlockState state,MatterRecyclerBlockEntity recycler){recycler.energyStorage.beginUsageTick(level.getGameTime());recycler.chargeFromEnergyItem();if(MachineRedstoneMode.allowsWork(level,pos,recycler.redstoneMode))recycler.manageRecycle();else recycler.running=false;if(state.hasProperty(MatterRecyclerBlock.ACTIVE)&&state.getValue(MatterRecyclerBlock.ACTIVE)!=recycler.running)level.setBlock(pos,state.setValue(MatterRecyclerBlock.ACTIVE,recycler.running),3);}
    private void chargeFromEnergyItem(){ItemStack stack=items.getStackInSlot(ENERGY_SLOT);if(stack.isEmpty()||energyStorage.getEnergyStored()>=energyStorage.getMaxEnergyStored())return;stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(source->{if(!source.canExtract())return;int request=Math.min(ENERGY_ITEM_TRANSFER_PER_TICK,energyStorage.getMaxEnergyStored()-energyStorage.getEnergyStored());int offered=source.extractEnergy(request,true);int accepted=energyStorage.receiveEnergy(offered,false);if(accepted>0)source.extractEnergy(accepted,false);});}
    private void onUpgradesChanged(){energyStorage.setCapacity((int)Math.min(Integer.MAX_VALUE,Math.round(ENERGY_STORAGE*upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerStorage))));setChanged();}
    private void manageRecycle(){if(!canRecycle()){running=false;recycleTime=0;return;}int drain=getEnergyDrainPerTick();if(energyStorage.getEnergyStored()<drain){running=false;return;}running=true;energyStorage.consumeEnergy(drain,level.getGameTime());recycleTime++;if(recycleTime>=getSpeed()){recycleTime=0;recycleItem();}}
    private boolean canRecycle(){return getRecycleMatter()>0&&canPutInOutput();}
    public int getRecycleMatter(){ItemStack input=items.getStackInSlot(INPUT_SLOT);if(input.isEmpty()||!(input.getItem() instanceof MatterDustItem dust)||dust.isRefined())return 0;return MatterDustItem.getMatter(input);}
    public int getSpeed(){int matter=getRecycleMatter();if(matter<=0)return 1;double scaled=Math.log1p(matter);scaled*=scaled;return Math.max(1,(int)Math.round(RECYCLE_SPEED_PER_MATTER*scaled*upgrades.getMultiplier(MachineUpgradeItem.Upgrade::speed)));}
    public int getEnergyDrainMax(){int matter=getRecycleMatter();return matter<=0?0:Math.max(1,(int)Math.round(matter*RECYCLE_ENERGY_PER_MATTER*upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerUsage)));}
    public int getEnergyDrainPerTick(){int max=getEnergyDrainMax();return max<=0?0:Math.max(1,max/getSpeed());}
    private boolean canPutInOutput(){int matter=getRecycleMatter();if(matter<=0)return false;ItemStack output=items.getStackInSlot(OUTPUT_SLOT);if(output.isEmpty())return true;return output.is(ModItems.get("matter_dust_refined").get())&&MatterDustItem.getMatter(output)==matter&&output.getCount()<output.getMaxStackSize();}
    private void recycleItem(){int matter=getRecycleMatter();if(matter<=0||!canPutInOutput())return;ItemStack output=items.getStackInSlot(OUTPUT_SLOT);if(output.isEmpty()){ItemStack refined=new ItemStack(ModItems.get("matter_dust_refined").get());MatterDustItem.setMatter(refined,matter);items.setStackInSlot(OUTPUT_SLOT,refined);}else output.grow(1);ItemStack input=items.getStackInSlot(INPUT_SLOT);input.shrink(1);if(input.isEmpty())items.setStackInSlot(INPUT_SLOT,ItemStack.EMPTY);setChanged();}
    public ItemStackHandler getItemHandler(){return items;}public MachineEnergyStorage getEnergyStorage(){return energyStorage;}public MachineUpgradeInventory getUpgradeInventory(){return upgrades;}public ContainerData getContainerData(){return data;}public int getRedstoneMode(){return redstoneMode;}public int cycleRedstoneMode(){redstoneMode=MachineRedstoneMode.next(redstoneMode);setChanged();return redstoneMode;}
    public void dropContents(){if(level==null||level.isClientSide)return;for(int slot=0;slot<items.getSlots();slot++){ItemStack stack=items.getStackInSlot(slot);if(!stack.isEmpty()){Containers.dropItemStack(level,worldPosition.getX()+0.5,worldPosition.getY()+0.5,worldPosition.getZ()+0.5,stack.copy());items.setStackInSlot(slot,ItemStack.EMPTY);}}for(int slot=0;slot<upgrades.getSlots();slot++){ItemStack stack=upgrades.getStackInSlot(slot);if(!stack.isEmpty()){Containers.dropItemStack(level,worldPosition.getX()+0.5,worldPosition.getY()+0.5,worldPosition.getZ()+0.5,stack.copy());upgrades.setStackInSlot(slot,ItemStack.EMPTY);}}}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.put("Items",items.serializeNBT());tag.put("Upgrades",upgrades.serializeNBT());tag.putInt("Energy",energyStorage.getEnergyStored());tag.putBoolean("InfiniteEnergy",energyStorage.isInfiniteEnergy());tag.putInt("RecycleTime",recycleTime);tag.putBoolean("Running",running);tag.putInt("RedstoneMode",redstoneMode);}
    @Override public void load(CompoundTag tag){super.load(tag);if(tag.contains("Items"))items.deserializeNBT(tag.getCompound("Items"));if(tag.contains("Upgrades"))upgrades.deserializeNBT(tag.getCompound("Upgrades"));onUpgradesChanged();energyStorage.setEnergyStored(tag.getInt("Energy"));energyStorage.setInfiniteEnergy(tag.getBoolean("InfiniteEnergy"));recycleTime=Math.max(0,tag.getInt("RecycleTime"));running=tag.getBoolean("Running");redstoneMode=tag.contains("RedstoneMode")?MachineRedstoneMode.sanitize(tag.getInt("RedstoneMode")):MachineRedstoneMode.DISABLED;}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap,@Nullable Direction side){if(cap==ForgeCapabilities.ITEM_HANDLER)return itemCapability.cast();if(cap==ForgeCapabilities.ENERGY)return energyCapability.cast();return super.getCapability(cap,side);}@Override public void invalidateCaps(){super.invalidateCaps();itemCapability.invalidate();energyCapability.invalidate();}@Override public void reviveCaps(){super.reviveCaps();itemCapability=LazyOptional.of(()->items);energyCapability=LazyOptional.of(()->energyStorage);}
    @Override public Component getDisplayName(){return Component.translatable("block.matteroverdrive.matter_recycler");}@Nullable @Override public AbstractContainerMenu createMenu(int containerId,Inventory playerInventory,Player player){return new MatterRecyclerMenu(containerId,playerInventory,this);}private static int lowWord(int value){return value&0xFFFF;}private static int highWord(int value){return(value>>>16)&0xFFFF;}
}
