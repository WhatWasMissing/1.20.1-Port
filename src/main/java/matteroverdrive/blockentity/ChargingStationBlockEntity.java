package matteroverdrive.blockentity;

import matteroverdrive.android.AndroidData;
import matteroverdrive.entity.DroneEntity;
import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.compat.AutomationItemHandler;
import matteroverdrive.item.MachineUpgradeInventory;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.machine.MachineRedstoneMode;
import matteroverdrive.menu.ChargingStationMenu;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ChargingStationBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_TRANSFER_PER_TICK=4096,ENERGY_CAPACITY=100000,BASE_ANDROID_RANGE=8,BASE_ANDROID_CHARGE_PER_TICK=512,UPGRADE_SLOT_COUNT=4;
    private static final int DRONE_SCAN_INTERVAL_TICKS=20,MAX_CACHED_DRONES=32,DRONE_MAINTENANCE_INTERVAL_TICKS=20,MAX_DRONES_REPAIRED_PER_CYCLE=2,DRONE_REPAIR_FE_PER_HEALTH=400;
    private final MachineEnergyStorage internalEnergy=new MachineEnergyStorage(ENERGY_CAPACITY,MAX_TRANSFER_PER_TICK,0,this::setChanged);
    private final ItemStackHandler battery=new ItemStackHandler(1){@Override public boolean isItemValid(int slot,ItemStack stack){IEnergyStorage e=stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);return e!=null&&e.canReceive();}@Override public int getSlotLimit(int slot){return 1;}@Override protected void onContentsChanged(int slot){setChanged();}};
    /** AE2/Forge automation view: only the public rechargeable-battery slot is exposed. */
    private final IItemHandler automationBattery=new AutomationItemHandler(battery,slot->slot==0,slot->slot==0);
    private final MachineUpgradeInventory upgrades=new MachineUpgradeInventory(UPGRADE_SLOT_COUNT,u->u==MachineUpgradeItem.Upgrade.RANGE||u==MachineUpgradeItem.Upgrade.POWER||u==MachineUpgradeItem.Upgrade.POWER_STORAGE,this::onUpgradesChanged);
    private LazyOptional<IItemHandler> itemCapability=LazyOptional.of(()->automationBattery);
    private LazyOptional<IEnergyStorage> energyCapability=LazyOptional.of(()->internalEnergy);
    private int lastItemTransferred,lastAndroidTransferred,androidsCharged,lastDroneTransferred,dronesCharged,lastDroneRepairEnergy,dronesRepaired;private int redstoneMode=MachineRedstoneMode.DISABLED;
    private final List<UUID> nearbyDroneIds=new ArrayList<>();private long nextDroneScanTick;private int droneRoundRobinCursor;
    private final ContainerData data=new ContainerData(){@Override public int get(int i){ItemStack stack=battery.getStackInSlot(0);IEnergyStorage target=stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);return switch(i){case 0->target==null?0:low(target.getEnergyStored());case 1->target==null?0:high(target.getEnergyStored());case 2->target==null?0:low(target.getMaxEnergyStored());case 3->target==null?0:high(target.getMaxEnergyStored());case 4->low(internalEnergy.getEnergyStored());case 5->high(internalEnergy.getEnergyStored());case 6->low(internalEnergy.getMaxEnergyStored());case 7->high(internalEnergy.getMaxEnergyStored());case 8->lastItemTransferred;case 9->lastAndroidTransferred;case 10->androidsCharged;case 11->getAndroidRange();case 12->getMaxAndroidChargePerTick();case 13->redstoneMode;case 14->lastDroneTransferred;case 15->dronesCharged;case 16->lastDroneRepairEnergy;case 17->dronesRepaired;default->0;};}@Override public void set(int i,int v){}@Override public int getCount(){return 18;}};
    public ChargingStationBlockEntity(BlockPos pos,BlockState state){super(ModBlockEntities.CHARGING_STATION.get(),pos,state);}
    public static void serverTick(Level level,BlockPos pos,BlockState state,ChargingStationBlockEntity s){s.internalEnergy.beginUsageTick(level.getGameTime());s.pullAdjacentEnergy();if(MachineRedstoneMode.allowsWork(level,pos,s.redstoneMode)){s.chargeAndroids();s.chargeDrones();s.maintainDrones();s.lastItemTransferred=s.chargeBattery();}else{s.lastAndroidTransferred=0;s.androidsCharged=0;s.lastDroneTransferred=0;s.dronesCharged=0;s.lastDroneRepairEnergy=0;s.dronesRepaired=0;s.lastItemTransferred=0;}}
    private void chargeAndroids(){lastAndroidTransferred=0;androidsCharged=0;if(level==null||level.isClientSide||internalEnergy.getEnergyStored()<=0)return;int range=getAndroidRange();Vec3 centre=Vec3.atCenterOf(worldPosition);AABB area=new AABB(worldPosition).inflate(range);for(Player player:level.getEntitiesOfClass(Player.class,area,AndroidData::isAndroid)){if(internalEnergy.getEnergyStored()<=0)break;double distance=Math.sqrt(player.distanceToSqr(centre));if(distance>range)continue;double falloff=1.0D-Mth.clamp(distance/Math.max(1.0D,range),0.0D,1.0D);int required=(int)Math.floor(BASE_ANDROID_CHARGE_PER_TICK*falloff);int offered=Math.min(Math.min(required,getMaxAndroidChargePerTick()),internalEnergy.getEnergyStored());if(offered<=0)continue;int accepted=AndroidData.receiveEnergy(player,offered);if(accepted<=0)continue;int consumed=internalEnergy.consumeEnergy(accepted,level.getGameTime());lastAndroidTransferred+=consumed;androidsCharged++;}}
    private void chargeDrones(){
        lastDroneTransferred=0;dronesCharged=0;
        if(!(level instanceof ServerLevel server)||internalEnergy.getEnergyStored()<=0)return;
        int range=getAndroidRange();long gameTime=level.getGameTime();
        if(gameTime>=nextDroneScanTick)refreshDroneCache(server,range,gameTime);
        int cached=nearbyDroneIds.size();if(cached==0)return;
        Vec3 centre=Vec3.atCenterOf(worldPosition);int remaining=MAX_TRANSFER_PER_TICK;
        int perDrone=Math.max(1,getMaxAndroidChargePerTick()/2);int start=Math.floorMod(droneRoundRobinCursor,cached);
        for(int offset=0;offset<cached&&remaining>0&&internalEnergy.getEnergyStored()>0;offset++){
            var entity=server.getEntity(nearbyDroneIds.get((start+offset)%cached));
            if(!(entity instanceof DroneEntity drone)||!drone.isAlive()||drone.getOwnerUuid()==null)continue;
            double distance=Math.sqrt(drone.distanceToSqr(centre));if(distance>range)continue;
            double falloff=1.0D-Mth.clamp(distance/Math.max(1.0D,range),0.0D,1.0D);
            int offered=Math.min(remaining,Math.min((int)Math.floor(perDrone*falloff),internalEnergy.getEnergyStored()));
            if(offered<=0)continue;int accepted=drone.receiveDroneEnergy(offered);
            if(accepted>0){int consumed=internalEnergy.consumeEnergy(accepted,gameTime);remaining-=consumed;lastDroneTransferred+=consumed;dronesCharged++;}
        }
        droneRoundRobinCursor=(start+1)%cached;
    }
    private void refreshDroneCache(ServerLevel server,int range,long gameTime){
        Vec3 centre=Vec3.atCenterOf(worldPosition);AABB area=new AABB(worldPosition).inflate(range);
        List<DroneEntity> candidates=server.getEntitiesOfClass(DroneEntity.class,area,d->d.isAlive()&&d.getOwnerUuid()!=null);
        candidates.sort(Comparator.comparingDouble(drone->drone.distanceToSqr(centre)));
        nearbyDroneIds.clear();
        for(int i=0;i<Math.min(MAX_CACHED_DRONES,candidates.size());i++)nearbyDroneIds.add(candidates.get(i).getUUID());
        if(droneRoundRobinCursor>=nearbyDroneIds.size())droneRoundRobinCursor=0;
        nextDroneScanTick=gameTime+DRONE_SCAN_INTERVAL_TICKS;
    }
    /** Bounded fleet maintenance: repairs at most two cached nearby drones each second using real station FE. */
    private void maintainDrones(){
        lastDroneRepairEnergy=0;dronesRepaired=0;
        if(!(level instanceof ServerLevel server)||level.getGameTime()%DRONE_MAINTENANCE_INTERVAL_TICKS!=0||internalEnergy.getEnergyStored()<=0)return;
        int range=getAndroidRange();long gameTime=level.getGameTime();
        if(gameTime>=nextDroneScanTick)refreshDroneCache(server,range,gameTime);
        Vec3 centre=Vec3.atCenterOf(worldPosition);int repaired=0;
        for(UUID id:nearbyDroneIds){
            if(repaired>=MAX_DRONES_REPAIRED_PER_CYCLE||internalEnergy.getEnergyStored()<=0)break;
            var entity=server.getEntity(id);
            if(!(entity instanceof DroneEntity drone)||!drone.isAlive()||drone.getOwnerUuid()==null||Math.sqrt(drone.distanceToSqr(centre))>range)continue;
            float healed=Math.min(2.0F,drone.getMaxHealth()-drone.getHealth());
            int cost=(int)Math.ceil(healed*DRONE_REPAIR_FE_PER_HEALTH);
            if(healed<=0.0F||cost<=0||internalEnergy.getEnergyStored()<cost)continue;
            int consumed=internalEnergy.consumeEnergy(cost,gameTime);
            if(consumed<cost){internalEnergy.receiveEnergy(consumed,false);continue;}
            drone.heal(healed);lastDroneRepairEnergy+=consumed;dronesRepaired++;repaired++;
        }
    }
    private void pullAdjacentEnergy(){if(level==null||level.isClientSide)return;int remaining=Math.min(MAX_TRANSFER_PER_TICK,internalEnergy.getMaxEnergyStored()-internalEnergy.getEnergyStored());for(Direction direction:Direction.values()){if(remaining<=0)break;BlockEntity neighbor=level.getBlockEntity(worldPosition.relative(direction));if(neighbor==null)continue;IEnergyStorage source=neighbor.getCapability(ForgeCapabilities.ENERGY,direction.getOpposite()).orElse(null);if(source==null||!source.canExtract())continue;int available=source.extractEnergy(remaining,true),accepted=internalEnergy.receiveEnergy(available,true);if(accepted<=0)continue;int extracted=source.extractEnergy(accepted,false),received=internalEnergy.receiveEnergy(extracted,false);if(received<extracted&&source.canReceive())source.receiveEnergy(extracted-received,false);remaining-=received;}}
    private int chargeBattery(){if(level==null||level.isClientSide)return 0;ItemStack stack=battery.getStackInSlot(0);IEnergyStorage target=stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);if(target==null||!target.canReceive())return 0;int offered=Math.min(MAX_TRANSFER_PER_TICK,internalEnergy.getEnergyStored());if(offered<=0)return 0;int accepted=target.receiveEnergy(offered,false);return accepted<=0?0:internalEnergy.consumeEnergy(accepted,level.getGameTime());}
    public int getAndroidRange(){double m=Math.min(8.0D,upgrades.getMultiplier(MachineUpgradeItem.Upgrade::range));return Math.max(1,(int)Math.floor(BASE_ANDROID_RANGE*m));}public int getMaxAndroidChargePerTick(){double p=Math.max(0.05D,upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerUsage));return Math.max(1,(int)Math.floor(BASE_ANDROID_CHARGE_PER_TICK/p));}private void onUpgradesChanged(){int cap=(int)Math.min(Integer.MAX_VALUE,Math.round(ENERGY_CAPACITY*upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerStorage)));internalEnergy.setCapacity(cap);nextDroneScanTick=0;setChanged();}
    public ItemStack getBattery(){return battery.getStackInSlot(0);}public boolean hasBattery(){return!getBattery().isEmpty();}public boolean insertBattery(ItemStack stack){if(hasBattery()||stack.isEmpty()||!battery.isItemValid(0,stack))return false;ItemStack inserted=stack.copy();inserted.setCount(1);battery.setStackInSlot(0,inserted);return true;}public ItemStack removeBattery(){return battery.extractItem(0,1,false);}public int getRedstoneMode(){return redstoneMode;}public int cycleRedstoneMode(){redstoneMode=MachineRedstoneMode.next(redstoneMode);setChanged();return redstoneMode;}
    public void dropContents(){if(level==null||level.isClientSide)return;ItemStack stack=removeBattery();if(!stack.isEmpty())drop(stack);for(int slot=0;slot<upgrades.getSlots();slot++){ItemStack u=upgrades.getStackInSlot(slot);if(!u.isEmpty()){drop(u.copy());upgrades.setStackInSlot(slot,ItemStack.EMPTY);}}}private void drop(ItemStack stack){Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,stack);}public ItemStackHandler getBatteryInventory(){return battery;}public MachineUpgradeInventory getUpgradeInventory(){return upgrades;}public MachineEnergyStorage getEnergyStorage(){return internalEnergy;}public ContainerData getContainerData(){return data;}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.put("Battery",battery.serializeNBT());tag.put("Upgrades",upgrades.serializeNBT());tag.putInt("InternalEnergy",internalEnergy.getEnergyStored());tag.putBoolean("InfiniteEnergy",internalEnergy.isInfiniteEnergy());tag.putInt("RedstoneMode",redstoneMode);}@Override public void load(CompoundTag tag){super.load(tag);if(tag.contains("Battery"))battery.deserializeNBT(tag.getCompound("Battery"));if(tag.contains("Upgrades"))upgrades.deserializeNBT(tag.getCompound("Upgrades"));onUpgradesChanged();internalEnergy.setEnergyStored(tag.getInt("InternalEnergy"));internalEnergy.setInfiniteEnergy(tag.getBoolean("InfiniteEnergy"));redstoneMode=tag.contains("RedstoneMode")?MachineRedstoneMode.sanitize(tag.getInt("RedstoneMode")):MachineRedstoneMode.DISABLED;}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap,@Nullable Direction side){if(cap==ForgeCapabilities.ITEM_HANDLER)return itemCapability.cast();if(cap==ForgeCapabilities.ENERGY)return energyCapability.cast();return super.getCapability(cap,side);}@Override public void invalidateCaps(){super.invalidateCaps();itemCapability.invalidate();energyCapability.invalidate();}@Override public void reviveCaps(){super.reviveCaps();itemCapability=LazyOptional.of(()->automationBattery);energyCapability=LazyOptional.of(()->internalEnergy);}@Override public Component getDisplayName(){return Component.translatable("block.matteroverdrive.charging_station");}@Nullable @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return new ChargingStationMenu(id,inventory,this);}private static int low(int v){return v&0xffff;}private static int high(int v){return(v>>>16)&0xffff;}
}
