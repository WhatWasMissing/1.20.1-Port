package matteroverdrive.blockentity;

import com.mojang.logging.LogUtils;
import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.capability.MachineMatterStorage;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.item.MachineUpgradeInventory;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.machine.MachineRedstoneMode;
import matteroverdrive.menu.SpacetimeAcceleratorMenu;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.slf4j.Logger;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class SpacetimeAcceleratorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int UPGRADE_SLOT_COUNT=4,BASE_MATTER_STORAGE=1024,BASE_ENERGY_STORAGE=512000,BASE_ENERGY_USAGE=64,BASE_PULSE_INTERVAL=40,BASE_RADIUS=2,MIN_PULSE_INTERVAL=8,MAX_RADIUS=12;public static final double BASE_MATTER_USAGE=0.2D;
    private static final Logger LOGGER=LogUtils.getLogger();
    private final MachineEnergyStorage energyStorage=new MachineEnergyStorage(BASE_ENERGY_STORAGE,BASE_ENERGY_STORAGE,0,this::setChanged);private final MachineMatterStorage matterStorage=new MachineMatterStorage(BASE_MATTER_STORAGE,true,false,this::setChanged);
    private final MachineUpgradeInventory upgrades=new MachineUpgradeInventory(UPGRADE_SLOT_COUNT,u->u==MachineUpgradeItem.Upgrade.SPEED||u==MachineUpgradeItem.Upgrade.HYPER_SPEED||u==MachineUpgradeItem.Upgrade.POWER||u==MachineUpgradeItem.Upgrade.POWER_STORAGE||u==MachineUpgradeItem.Upgrade.MATTER_STORAGE||u==MachineUpgradeItem.Upgrade.RANGE,this::onUpgradesChanged);
    private LazyOptional<IEnergyStorage> energyCapability=LazyOptional.of(()->energyStorage);private LazyOptional<matteroverdrive.capability.IMatterStorage> matterCapability=LazyOptional.of(()->matterStorage);private final Set<String> failedTickerTypes=new HashSet<>();private int pulseTimer,lastAcceleratedTargets;private double matterUseRemainder;private boolean active;private int redstoneMode=MachineRedstoneMode.LOW.id();
    private final ContainerData data=new ContainerData(){@Override public int get(int i){return switch(i){case 0->lowWord(energyStorage.getEnergyStored());case 1->highWord(energyStorage.getEnergyStored());case 2->lowWord(energyStorage.getMaxEnergyStored());case 3->highWord(energyStorage.getMaxEnergyStored());case 4->matterStorage.getMatterStored();case 5->matterStorage.getMatterCapacity();case 6->getEnergyUsagePerTick();case 7->getPulseInterval();case 8->getRadius();case 9->active?1:0;case 10->pulseTimer;case 11->lastAcceleratedTargets;case 12->isRedstoneBlocked()?1:0;case 13->redstoneMode;default->0;};}@Override public void set(int i,int v){if(i==10)pulseTimer=Math.max(0,v);}@Override public int getCount(){return 14;}};
    public SpacetimeAcceleratorBlockEntity(BlockPos pos,BlockState state){super(ModBlockEntities.SPACETIME_ACCELERATOR.get(),pos,state);}
    public static void serverTick(Level level,BlockPos pos,BlockState state,SpacetimeAcceleratorBlockEntity a){a.energyStorage.beginUsageTick(level.getGameTime());a.active=a.canRun();if(!a.active)return;int usage=a.getEnergyUsagePerTick();if(a.energyStorage.consumeEnergy(usage,level.getGameTime())<usage){a.active=false;return;}a.pulseTimer++;int interval=a.getPulseInterval();if(a.pulseTimer>=interval){a.pulseTimer=0;a.consumePulseMatter();a.lastAcceleratedTargets=a.manageAccelerations();a.setChanged();}}
    private boolean canRun(){if(level==null||level.isClientSide||!MachineRedstoneMode.allowsWork(level,worldPosition,redstoneMode))return false;int usage=getEnergyUsagePerTick();return energyStorage.getEnergyStored()>=usage&&matterStorage.getMatterStored()>=matterRequiredForNextPulse();}private int matterRequiredForNextPulse(){int whole=(int)Math.floor(matterUseRemainder+getMatterUsagePerPulse());return Math.max(1,whole);}private void consumePulseMatter(){matterUseRemainder+=getMatterUsagePerPulse();int whole=(int)Math.floor(matterUseRemainder);if(whole<=0)return;int consumed=Math.min(whole,matterStorage.getMatterStored());matterStorage.setMatterStored(matterStorage.getMatterStored()-consumed);matterUseRemainder-=consumed;}
    private int manageAccelerations(){if(!(level instanceof ServerLevel sl))return 0;int radius=getRadius(),accelerated=0;for(int x=-radius;x<radius;x++)for(int z=-radius;z<radius;z++){BlockPos p=worldPosition.offset(x,0,z);if(!sl.hasChunkAt(p))continue;BlockState state=sl.getBlockState(p);if(state.isRandomlyTicking()){state.randomTick(sl,p,sl.random);accelerated++;}BlockState ts=sl.getBlockState(p);BlockEntity target=sl.getBlockEntity(p);if(target!=null&&tickBlockEntitySafely(sl,p,ts,target))accelerated++;}return accelerated;}
    @SuppressWarnings({"rawtypes","unchecked"})private boolean tickBlockEntitySafely(ServerLevel sl,BlockPos p,BlockState state,BlockEntity target){if(target==this||target instanceof SpacetimeAcceleratorBlockEntity||target.isRemoved())return false;String type=target.getType().toString();if(failedTickerTypes.contains(type))return false;Block block=state.getBlock();if(!(block instanceof EntityBlock eb))return false;BlockEntityTicker ticker=eb.getTicker(sl,state,target.getType());if(ticker==null)return false;try{ticker.tick(sl,p,state,target);return true;}catch(RuntimeException ex){failedTickerTypes.add(type);LOGGER.warn("Space-Time Accelerator at {} disabled extra ticking for {} after target {} failed",worldPosition,type,p,ex);return false;}}
    private void onUpgradesChanged(){energyStorage.setCapacity((int)Math.min(Integer.MAX_VALUE,Math.round(BASE_ENERGY_STORAGE*upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerStorage))));matterStorage.setCapacity((int)Math.min(Integer.MAX_VALUE,Math.round(BASE_MATTER_STORAGE*upgrades.getMultiplier(MachineUpgradeItem.Upgrade::matterStorage))));setChanged();}
    public int getEnergyUsagePerTick(){return Math.max(1,(int)Math.round(BASE_ENERGY_USAGE*upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerUsage)));}public double getMatterUsagePerPulse(){return Math.max(.01D,BASE_MATTER_USAGE*upgrades.getMultiplier(MachineUpgradeItem.Upgrade::matterUsage));}public int getPulseInterval(){return Math.max(MIN_PULSE_INTERVAL,(int)Math.round(BASE_PULSE_INTERVAL*upgrades.getMultiplier(MachineUpgradeItem.Upgrade::speed)));}public int getRadius(){double m=Math.min(6D,Math.max(1D,upgrades.getMultiplier(MachineUpgradeItem.Upgrade::range)));return Math.min(MAX_RADIUS,Math.max(1,(int)Math.round(BASE_RADIUS*m)));}public boolean isRedstoneBlocked(){return level!=null&&!MachineRedstoneMode.allowsWork(level,worldPosition,redstoneMode);}public int getRedstoneMode(){return redstoneMode;}public int cycleRedstoneMode(){redstoneMode=MachineRedstoneMode.next(redstoneMode);setChanged();return redstoneMode;}
    public MachineEnergyStorage getEnergyStorage(){return energyStorage;}public MachineMatterStorage getMatterStorage(){return matterStorage;}public MachineUpgradeInventory getUpgradeInventory(){return upgrades;}public ContainerData getContainerData(){return data;}public void fillMatterForDebug(){matterStorage.setMatterStored(matterStorage.getMatterCapacity());}
    public void dropContents(){if(level==null||level.isClientSide)return;for(int s=0;s<upgrades.getSlots();s++){ItemStack stack=upgrades.getStackInSlot(s);if(!stack.isEmpty()){Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,stack.copy());upgrades.setStackInSlot(s,ItemStack.EMPTY);}}}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.put("Upgrades",upgrades.serializeNBT());tag.putInt("Energy",energyStorage.getEnergyStored());tag.putBoolean("InfiniteEnergy",energyStorage.isInfiniteEnergy());tag.putInt("Matter",matterStorage.getMatterStored());tag.putInt("PulseTimer",pulseTimer);tag.putInt("LastAcceleratedTargets",lastAcceleratedTargets);tag.putDouble("MatterUseRemainder",matterUseRemainder);tag.putInt("RedstoneMode",redstoneMode);}@Override public void load(CompoundTag tag){super.load(tag);if(tag.contains("Upgrades"))upgrades.deserializeNBT(tag.getCompound("Upgrades"));onUpgradesChanged();energyStorage.setEnergyStored(tag.getInt("Energy"));energyStorage.setInfiniteEnergy(tag.getBoolean("InfiniteEnergy"));matterStorage.setMatterStored(tag.getInt("Matter"));pulseTimer=Math.max(0,tag.getInt("PulseTimer"));lastAcceleratedTargets=Math.max(0,tag.getInt("LastAcceleratedTargets"));matterUseRemainder=Math.max(0D,tag.getDouble("MatterUseRemainder"));redstoneMode=tag.contains("RedstoneMode")?MachineRedstoneMode.sanitize(tag.getInt("RedstoneMode")):MachineRedstoneMode.LOW.id();}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap,@Nullable Direction side){if(cap==ForgeCapabilities.ENERGY)return energyCapability.cast();if(cap==ModCapabilities.MATTER)return matterCapability.cast();return super.getCapability(cap,side);}@Override public void invalidateCaps(){super.invalidateCaps();energyCapability.invalidate();matterCapability.invalidate();}@Override public void reviveCaps(){super.reviveCaps();energyCapability=LazyOptional.of(()->energyStorage);matterCapability=LazyOptional.of(()->matterStorage);}@Override public Component getDisplayName(){return Component.translatable("block.matteroverdrive.spacetime_accelerator");}@Nullable @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player player){return new SpacetimeAcceleratorMenu(id,inv,this);}private static int lowWord(int v){return v&0xffff;}private static int highWord(int v){return(v>>>16)&0xffff;}
}
