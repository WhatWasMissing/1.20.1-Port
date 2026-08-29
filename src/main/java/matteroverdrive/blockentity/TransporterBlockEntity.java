package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.item.MachineUpgradeInventory;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.item.TransportFlashDriveItem;
import matteroverdrive.menu.TransporterMenu;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

public class TransporterBlockEntity extends BlockEntity implements MenuProvider {
    public static final int DRIVE_SLOT = 0, ENERGY_SLOT = 1, SLOT_COUNT = 2, UPGRADE_SLOT_COUNT = 5;
    public static final int ENERGY_CAPACITY = 1024000, ENERGY_PER_BLOCK = 16;
    public static final int TRANSPORT_TIME = 70, TRANSPORT_DELAY = 80, TRANSPORT_RANGE = 32;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot == DRIVE_SLOT ? stack.getItem() instanceof TransportFlashDriveItem
                    : stack.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::canExtract).orElse(false);
        }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private final MachineEnergyStorage energy = new MachineEnergyStorage(ENERGY_CAPACITY, 256, 32000, this::setChanged);
    private final MachineUpgradeInventory upgrades = new MachineUpgradeInventory(UPGRADE_SLOT_COUNT,
            u -> u == MachineUpgradeItem.Upgrade.SPEED || u == MachineUpgradeItem.Upgrade.POWER
                    || u == MachineUpgradeItem.Upgrade.RANGE || u == MachineUpgradeItem.Upgrade.POWER_STORAGE,
            this::upgradesChanged);
    private LazyOptional<IItemHandler> itemCap = LazyOptional.of(() -> items);
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private int progress, cooldown;
    private boolean running;
    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch(i) {
            case 0 -> progress; case 1 -> cycle(); case 2 -> low(energy.getEnergyStored()); case 3 -> high(energy.getEnergyStored());
            case 4 -> low(energy.getMaxEnergyStored()); case 5 -> high(energy.getMaxEnergyStored()); case 6 -> targetValid() ? 1 : 0;
            case 7 -> distance(); case 8 -> energyCost(); case 9 -> range(); case 10 -> cooldown; case 11 -> running ? 1 : 0; default -> 0;};}
        @Override public void set(int i,int v){if(i==0)progress=Math.max(0,v);} @Override public int getCount(){return 12;}
    };
    public TransporterBlockEntity(BlockPos pos, BlockState state){super(ModBlockEntities.TRANSPORTER.get(),pos,state);}
    public static void serverTick(Level level, BlockPos pos, BlockState state, TransporterBlockEntity t){t.charge();t.tickTransport();}
    private void charge(){ItemStack s=items.getStackInSlot(ENERGY_SLOT);if(s.isEmpty()||energy.getEnergyStored()>=energy.getMaxEnergyStored())return;s.getCapability(ForgeCapabilities.ENERGY).ifPresent(src->{int offer=src.extractEnergy(16000,true);int got=energy.receiveEnergy(offer,false);if(got>0)src.extractEnergy(got,false);});}
    private void tickTransport(){if(cooldown>0){cooldown--;running=false;return;}if(!targetValid()||energy.getEnergyStored()<energyCost()||entities().isEmpty()){progress=0;running=false;return;}running=true;if(++progress>=cycle()){BlockPos target=TransportFlashDriveItem.getTarget(items.getStackInSlot(DRIVE_SLOT));for(Entity e:entities())e.teleportTo(target.getX()+0.5,target.getY()+1,target.getZ()+0.5);energy.extractEnergy(energyCost(),false);progress=0;cooldown=delay();setChanged();}}
    private List<Entity> entities(){return level==null?List.of():level.getEntities((Entity)null,new AABB(worldPosition.above(),worldPosition.above().offset(1,2,1)),e->true);}
    private boolean targetValid(){if(level==null)return false;ItemStack d=items.getStackInSlot(DRIVE_SLOT);if(!TransportFlashDriveItem.hasTarget(d,level))return false;BlockPos p=TransportFlashDriveItem.getTarget(d);return !p.closerThan(worldPosition,4)&&distance()<=range();}
    private int distance(){if(level==null||!TransportFlashDriveItem.hasTarget(items.getStackInSlot(DRIVE_SLOT),level))return 0;return (int)Math.round(Math.sqrt(worldPosition.distSqr(TransportFlashDriveItem.getTarget(items.getStackInSlot(DRIVE_SLOT)))));}
    private int cycle(){return Math.max(1,(int)Math.round(TRANSPORT_TIME*upgrades.getMultiplier(MachineUpgradeItem.Upgrade::speed)));}
    private int delay(){return Math.max(1,(int)Math.round(TRANSPORT_DELAY*upgrades.getMultiplier(MachineUpgradeItem.Upgrade::speed)));}
    private int range(){return Math.max(1,(int)Math.round(TRANSPORT_RANGE*upgrades.getMultiplier(MachineUpgradeItem.Upgrade::range)));}
    private int energyCost(){return Math.max(1,(int)Math.round(distance()*ENERGY_PER_BLOCK*upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerUsage)));}
    private void upgradesChanged(){energy.setCapacity((int)Math.min(Integer.MAX_VALUE,Math.round(ENERGY_CAPACITY*upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerStorage))));setChanged();}
    public ItemStackHandler getItemHandler(){return items;} public MachineEnergyStorage getEnergy(){return energy;} public MachineUpgradeInventory getUpgrades(){return upgrades;} public ContainerData getData(){return data;}
    public void dropContents(){if(level==null||level.isClientSide)return;for(int i=0;i<items.getSlots();i++)drop(items.getStackInSlot(i));for(int i=0;i<upgrades.getSlots();i++)drop(upgrades.getStackInSlot(i));}
    private void drop(ItemStack s){if(!s.isEmpty())Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,s.copy());}
    @Override protected void saveAdditional(CompoundTag t){super.saveAdditional(t);t.put("Items",items.serializeNBT());t.put("Upgrades",upgrades.serializeNBT());t.putInt("Energy", energy.getEnergyStored());
        t.putBoolean("InfiniteEnergy", energy.isInfiniteEnergy());t.putInt("Progress",progress);t.putInt("Cooldown",cooldown);}
    @Override public void load(CompoundTag t){super.load(t);if(t.contains("Items"))items.deserializeNBT(t.getCompound("Items"));if(t.contains("Upgrades"))upgrades.deserializeNBT(t.getCompound("Upgrades"));upgradesChanged();energy.setEnergyStored(t.getInt("Energy"));
        energy.setInfiniteEnergy(t.getBoolean("InfiniteEnergy"));progress=t.getInt("Progress");cooldown=t.getInt("Cooldown");}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> c,@Nullable net.minecraft.core.Direction side){if(c==ForgeCapabilities.ITEM_HANDLER)return itemCap.cast();if(c==ForgeCapabilities.ENERGY)return energyCap.cast();return super.getCapability(c,side);}
    @Override public void invalidateCaps(){super.invalidateCaps();itemCap.invalidate();energyCap.invalidate();}@Override public void reviveCaps(){super.reviveCaps();itemCap=LazyOptional.of(()->items);energyCap=LazyOptional.of(()->energy);}
    @Override public Component getDisplayName(){return Component.translatable("block.matteroverdrive.transporter");}@Nullable @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new TransporterMenu(id,inv,this);}
    private static int low(int v){return v&65535;}private static int high(int v){return v>>>16&65535;}
}
