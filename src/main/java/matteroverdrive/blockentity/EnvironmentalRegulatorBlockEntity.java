package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.registry.OverhaulContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import javax.annotation.Nullable;

/** Powered safety field that suppresses Matter Overdrive environmental hazards around a laboratory. */
public class EnvironmentalRegulatorBlockEntity extends BlockEntity {
    public static final int CAPACITY=250_000, DRAIN_PER_TICK=40, RADIUS=12;
    public static final String PROTECTED_UNTIL="MatterOverdriveEnvironmentProtectedUntil";
    private final MachineEnergyStorage energy=new MachineEnergyStorage(CAPACITY,4096,0,this::setChanged);
    private LazyOptional<IEnergyStorage> energyCap=LazyOptional.of(()->energy);
    private boolean active;
    public EnvironmentalRegulatorBlockEntity(BlockPos pos,BlockState state){super(OverhaulContent.ENVIRONMENTAL_REGULATOR_BE.get(),pos,state);}
    public static void serverTick(Level level,BlockPos pos,BlockState state,EnvironmentalRegulatorBlockEntity r){r.energy.beginUsageTick(level.getGameTime());r.active=r.energy.getEnergyStored()>=DRAIN_PER_TICK;if(r.active){r.energy.consumeEnergy(DRAIN_PER_TICK,level.getGameTime());long until=level.getGameTime()+30L;for(Player p:level.getEntitiesOfClass(Player.class,new AABB(pos).inflate(RADIUS),Player::isAlive))p.getPersistentData().putLong(PROTECTED_UNTIL,until);}if(level.getGameTime()%20L==0L)r.setChanged();}
    public int getEnergyStored(){return energy.getEnergyStored();} public boolean isActive(){return active;}
    public static boolean protects(Player player){return player!=null&&player.getPersistentData().getLong(PROTECTED_UNTIL)>=player.level().getGameTime();}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.putInt("Energy",energy.getEnergyStored());}
    @Override public void load(CompoundTag tag){super.load(tag);energy.setEnergyStored(tag.getInt("Energy"));}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap,@Nullable Direction side){if(cap==ForgeCapabilities.ENERGY)return energyCap.cast();return super.getCapability(cap,side);}
    @Override public void invalidateCaps(){super.invalidateCaps();energyCap.invalidate();}
    @Override public void reviveCaps(){super.reviveCaps();energyCap=LazyOptional.of(()->energy);}
}
