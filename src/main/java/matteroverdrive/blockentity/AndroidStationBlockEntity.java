package matteroverdrive.blockentity;

import matteroverdrive.android.AndroidChassisData;
import matteroverdrive.android.AndroidData;
import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.menu.AndroidStationMenu;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AndroidStationBlockEntity extends BlockEntity implements MenuProvider {
    public static final int TRANSFER_PER_TICK=2_000,ENERGY_CAPACITY=TRANSFER_PER_TICK;
    private static final double CHARGE_RANGE_SQR=16.0D;
    private final MachineEnergyStorage energy=new MachineEnergyStorage(ENERGY_CAPACITY,TRANSFER_PER_TICK,0,this::setChanged);
    private LazyOptional<IEnergyStorage> energyCapability=LazyOptional.of(()->energy);
    private int lastTransfer;private long chargeSequence;
    public AndroidStationBlockEntity(BlockPos pos,BlockState state){super(ModBlockEntities.ANDROID_STATION.get(),pos,state);}
    public static void serverTick(Level level,BlockPos pos,BlockState state,AndroidStationBlockEntity station){station.pullAdjacentEnergy();station.lastTransfer=station.chargeNearbyAndroids();}

    private void pullAdjacentEnergy(){
        if(level==null)return;int remaining=Math.min(TRANSFER_PER_TICK,energy.getMaxEnergyStored()-energy.getEnergyStored());
        for(Direction direction:Direction.values()){if(remaining<=0)break;BlockEntity neighbor=level.getBlockEntity(worldPosition.relative(direction));if(neighbor==null)continue;IEnergyStorage source=neighbor.getCapability(ForgeCapabilities.ENERGY,direction.getOpposite()).orElse(null);if(source==null||!source.canExtract())continue;int offered=source.extractEnergy(remaining,true),accepted=energy.receiveEnergy(offered,true);if(accepted<=0)continue;int extracted=source.extractEnergy(accepted,false),received=energy.receiveEnergy(extracted,false);if(received<extracted&&source.canReceive())source.receiveEnergy(extracted-received,false);remaining-=received;}
    }
    private int chargeNearbyAndroids(){
        if(level==null||level.getServer()==null||energy.getEnergyStored()<=0)return 0;List<ServerPlayer>candidates=new ArrayList<>();
        for(ServerPlayer player:level.getServer().getPlayerList().getPlayers())if(player.level()==level&&AndroidData.isAndroid(player)&&AndroidData.getEnergy(player)<AndroidData.getEnergyCapacity(player)&&player.distanceToSqr(worldPosition.getX()+.5D,worldPosition.getY()+.5D,worldPosition.getZ()+.5D)<=CHARGE_RANGE_SQR)candidates.add(player);
        if(candidates.isEmpty())return 0;candidates.sort(Comparator.comparing(ServerPlayer::getUUID));int start=(int)Math.floorMod(chargeSequence++,(long)candidates.size()),available=Math.min(TRANSFER_PER_TICK,energy.getEnergyStored()),total=0;
        for(int offset=0;offset<candidates.size()&&available>0;offset++){int remainingPlayers=candidates.size()-offset,fairOffer=(available+remainingPlayers-1)/remainingPlayers;ServerPlayer player=candidates.get((start+offset)%candidates.size());int accepted=AndroidData.receiveEnergy(player,fairOffer);if(accepted<=0)continue;int used=energy.consumeEnergy(accepted,level.getGameTime());available-=used;total+=used;}return total;
    }

    public ContainerData getContainerData(Player viewer){return new ContainerData(){
        @Override public int get(int index){return switch(index){case 0->AndroidData.isAndroid(viewer)?1:0;case 1->AndroidData.getParts(viewer);case 2->low(AndroidData.getEnergy(viewer));case 3->high(AndroidData.getEnergy(viewer));case 4->low(AndroidData.getEnergyCapacity(viewer));case 5->high(AndroidData.getEnergyCapacity(viewer));case 6->low(energy.getEnergyStored());case 7->high(energy.getEnergyStored());case 8->lastTransfer;case 9->AndroidData.getLevel(viewer);case 10->AndroidData.experienceIntoLevel(viewer);case 11->AndroidData.experienceToNextLevel(viewer);case 12->AndroidData.getAvailableSkillPoints(viewer);case 13->AndroidData.getSelectedAbility(viewer).ordinal();case 14->AndroidData.getActiveAbilityFlags(viewer);case 15,16,17,18,19->{AndroidChassisData.Slot slot=AndroidChassisData.Slot.values()[index-15];AndroidChassisData.Module module=AndroidChassisData.get(viewer,slot);yield module==null?0:module.ordinal()+1;}default->0;};}
        @Override public void set(int index,int value){}@Override public int getCount(){return 20;}
    };}
    @Override public Component getDisplayName(){return Component.translatable("block.matteroverdrive.android_station");}
    @Nullable @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return new AndroidStationMenu(id,inventory,this);}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.putInt("Energy",energy.getEnergyStored());tag.putLong("ChargeSequence",chargeSequence);}
    @Override public void load(CompoundTag tag){super.load(tag);energy.setEnergyStored(tag.getInt("Energy"));chargeSequence=tag.getLong("ChargeSequence");}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap,@Nullable Direction side){return cap==ForgeCapabilities.ENERGY?energyCapability.cast():super.getCapability(cap,side);}
    @Override public void invalidateCaps(){super.invalidateCaps();energyCapability.invalidate();}@Override public void reviveCaps(){super.reviveCaps();energyCapability=LazyOptional.of(()->energy);}
    private static int low(int value){return value&0xFFFF;}private static int high(int value){return value>>>16&0xFFFF;}
}
