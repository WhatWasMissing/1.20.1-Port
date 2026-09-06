package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.item.MachineUpgradeInventory;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.machine.MachineRedstoneMode;
import matteroverdrive.menu.MicrowaveMenu;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
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
import java.util.Optional;

public class MicrowaveBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT=0,ENERGY_SLOT=1,OUTPUT_SLOT=2,SLOT_COUNT=3,UPGRADE_SLOT_COUNT=4;
    public static final int ENERGY_STORAGE=512000,ENERGY_COST=1000,COOK_SPEED=10,ENERGY_ITEM_TRANSFER_PER_TICK=16000;
    private final ItemStackHandler items=new ItemStackHandler(SLOT_COUNT){@Override public boolean isItemValid(int slot,ItemStack stack){return switch(slot){case INPUT_SLOT->stack.isEdible();case ENERGY_SLOT->stack.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::canExtract).orElse(false);case OUTPUT_SLOT->false;default->false;};}@Override protected void onContentsChanged(int slot){setChanged();}};
    private final MachineEnergyStorage energyStorage=new MachineEnergyStorage(ENERGY_STORAGE,ENERGY_STORAGE,ENERGY_STORAGE,this::setChanged);
    private final MachineUpgradeInventory upgrades=new MachineUpgradeInventory(UPGRADE_SLOT_COUNT,upgrade->upgrade==MachineUpgradeItem.Upgrade.SPEED||upgrade==MachineUpgradeItem.Upgrade.POWER||upgrade==MachineUpgradeItem.Upgrade.POWER_STORAGE||upgrade==MachineUpgradeItem.Upgrade.HYPER_SPEED,this::onUpgradesChanged);
    private LazyOptional<IItemHandler> itemCapability=LazyOptional.of(()->items);private LazyOptional<IEnergyStorage> energyCapability=LazyOptional.of(()->energyStorage);
    private int cookTime;private boolean running;private int redstoneMode=MachineRedstoneMode.DISABLED;
    private final ContainerData data=new ContainerData(){@Override public int get(int index){return switch(index){case 0->cookTime;case 1->getCookSpeed();case 2->lowWord(energyStorage.getEnergyStored());case 3->highWord(energyStorage.getEnergyStored());case 4->lowWord(energyStorage.getMaxEnergyStored());case 5->highWord(energyStorage.getMaxEnergyStored());case 6->getEnergyDrainPerTick();case 7->running?1:0;case 8->redstoneMode;default->0;};}@Override public void set(int index,int value){if(index==0)cookTime=Math.max(0,value);}@Override public int getCount(){return 9;}};
    public MicrowaveBlockEntity(BlockPos pos,BlockState state){super(ModBlockEntities.MICROWAVE.get(),pos,state);}
    public static void serverTick(Level level,BlockPos pos,BlockState state,MicrowaveBlockEntity microwave){microwave.energyStorage.beginUsageTick(level.getGameTime());microwave.chargeFromEnergyItem();if(MachineRedstoneMode.allowsWork(level,pos,microwave.redstoneMode))microwave.manageCooking();else microwave.running=false;}
    private void chargeFromEnergyItem(){ItemStack stack=items.getStackInSlot(ENERGY_SLOT);if(stack.isEmpty()||energyStorage.getEnergyStored()>=energyStorage.getMaxEnergyStored())return;stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(source->{if(!source.canExtract())return;int request=Math.min(ENERGY_ITEM_TRANSFER_PER_TICK,energyStorage.getMaxEnergyStored()-energyStorage.getEnergyStored());int offered=source.extractEnergy(request,true);int accepted=energyStorage.receiveEnergy(offered,false);if(accepted>0)source.extractEnergy(accepted,false);});}
    private void onUpgradesChanged(){energyStorage.setCapacity((int)Math.min(Integer.MAX_VALUE,Math.round(ENERGY_STORAGE*upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerStorage))));setChanged();}
    private void manageCooking(){if(!canCook()){running=false;cookTime=0;return;}int drain=getEnergyDrainPerTick();if(drain<=0||energyStorage.getEnergyStored()<drain){running=false;return;}running=true;energyStorage.consumeEnergy(drain,level.getGameTime());cookTime++;if(cookTime>=getCookSpeed()){cookTime=0;cookItem();}}
    public boolean canCook(){ItemStack result=getCookingResult();if(result.isEmpty())return false;ItemStack output=items.getStackInSlot(OUTPUT_SLOT);if(output.isEmpty())return true;if(!ItemStack.isSameItemSameTags(output,result))return false;return output.getCount()+result.getCount()<=output.getMaxStackSize();}
    public int getCookSpeed(){if(getCookingResult().isEmpty())return COOK_SPEED;return Math.max(1,(int)Math.round(COOK_SPEED*upgrades.getMultiplier(MachineUpgradeItem.Upgrade::speed)));}
    public int getEnergyDrainMax(){if(getCookingResult().isEmpty())return 0;return Math.max(1,(int)Math.round(ENERGY_COST*upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerUsage)));}
    public int getEnergyDrainPerTick(){int total=getEnergyDrainMax();return total<=0?0:Math.max(1,total/getCookSpeed());}
    private ItemStack getCookingResult(){if(level==null)return ItemStack.EMPTY;ItemStack input=items.getStackInSlot(INPUT_SLOT);if(input.isEmpty()||!input.isEdible())return ItemStack.EMPTY;SimpleContainer container=new SimpleContainer(1);container.setItem(0,input);Optional<SmeltingRecipe> recipe=level.getRecipeManager().getRecipeFor(RecipeType.SMELTING,container,level);return recipe.map(value->value.getResultItem(level.registryAccess()).copy()).orElse(ItemStack.EMPTY);}
    private void cookItem(){ItemStack result=getCookingResult();if(result.isEmpty()||!canCook())return;ItemStack input=items.getStackInSlot(INPUT_SLOT),output=items.getStackInSlot(OUTPUT_SLOT);input.shrink(1);if(input.isEmpty())items.setStackInSlot(INPUT_SLOT,ItemStack.EMPTY);if(output.isEmpty())items.setStackInSlot(OUTPUT_SLOT,result.copy());else output.grow(result.getCount());setChanged();}
    public ItemStackHandler getItemHandler(){return items;}public MachineEnergyStorage getEnergyStorage(){return energyStorage;}public MachineUpgradeInventory getUpgradeInventory(){return upgrades;}public ContainerData getContainerData(){return data;}public boolean isRunning(){return running;}public int getRedstoneMode(){return redstoneMode;}public int cycleRedstoneMode(){redstoneMode=MachineRedstoneMode.next(redstoneMode);setChanged();return redstoneMode;}
    public void dropContents(){if(level==null||level.isClientSide)return;for(int slot=0;slot<items.getSlots();slot++){ItemStack stack=items.getStackInSlot(slot);if(!stack.isEmpty()){Containers.dropItemStack(level,worldPosition.getX()+0.5,worldPosition.getY()+0.5,worldPosition.getZ()+0.5,stack.copy());items.setStackInSlot(slot,ItemStack.EMPTY);}}for(int slot=0;slot<upgrades.getSlots();slot++){ItemStack stack=upgrades.getStackInSlot(slot);if(!stack.isEmpty()){Containers.dropItemStack(level,worldPosition.getX()+0.5,worldPosition.getY()+0.5,worldPosition.getZ()+0.5,stack.copy());upgrades.setStackInSlot(slot,ItemStack.EMPTY);}}}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.put("Items",items.serializeNBT());tag.put("Upgrades",upgrades.serializeNBT());tag.putInt("Energy",energyStorage.getEnergyStored());tag.putBoolean("InfiniteEnergy",energyStorage.isInfiniteEnergy());tag.putInt("CookTime",cookTime);tag.putBoolean("Running",running);tag.putInt("RedstoneMode",redstoneMode);}
    @Override public void load(CompoundTag tag){super.load(tag);if(tag.contains("Items"))items.deserializeNBT(tag.getCompound("Items"));if(tag.contains("Upgrades"))upgrades.deserializeNBT(tag.getCompound("Upgrades"));onUpgradesChanged();energyStorage.setEnergyStored(tag.getInt("Energy"));energyStorage.setInfiniteEnergy(tag.getBoolean("InfiniteEnergy"));cookTime=Math.max(0,tag.getInt("CookTime"));running=tag.getBoolean("Running");redstoneMode=tag.contains("RedstoneMode")?MachineRedstoneMode.sanitize(tag.getInt("RedstoneMode")):MachineRedstoneMode.DISABLED;}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap,@Nullable Direction side){if(cap==ForgeCapabilities.ITEM_HANDLER)return itemCapability.cast();if(cap==ForgeCapabilities.ENERGY)return energyCapability.cast();return super.getCapability(cap,side);}@Override public void invalidateCaps(){super.invalidateCaps();itemCapability.invalidate();energyCapability.invalidate();}@Override public void reviveCaps(){super.reviveCaps();itemCapability=LazyOptional.of(()->items);energyCapability=LazyOptional.of(()->energyStorage);}@Override public Component getDisplayName(){return Component.translatable("block.matteroverdrive.microwave");}@Nullable @Override public AbstractContainerMenu createMenu(int containerId,Inventory playerInventory,Player player){return new MicrowaveMenu(containerId,playerInventory,this);}private static int lowWord(int value){return value&0xFFFF;}private static int highWord(int value){return(value>>>16)&0xFFFF;}
}
