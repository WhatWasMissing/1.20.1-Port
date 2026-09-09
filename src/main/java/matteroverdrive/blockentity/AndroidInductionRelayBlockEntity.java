package matteroverdrive.blockentity;

import matteroverdrive.android.AndroidChassisData;
import matteroverdrive.android.AndroidData;
import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.registry.ModExtraBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Wireless same-dimension Android charging relay inspired by player-transmitter style tech. */
public class AndroidInductionRelayBlockEntity extends BlockEntity {
    public static final int CAPACITY = 2_000_000, INPUT = 16_384, OUTPUT = 8_192, BASE_RANGE = 32;
    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, INPUT, 0, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private int range = BASE_RANGE, lastTransfer;
    private long sequence;

    public AndroidInductionRelayBlockEntity(BlockPos pos, BlockState state) { super(ModExtraBlockEntities.ANDROID_INDUCTION_RELAY.get(), pos, state); }
    public static void serverTick(Level level, BlockPos pos, BlockState state, AndroidInductionRelayBlockEntity relay) { relay.pullEnergy(); relay.lastTransfer = relay.chargeAndroids(); }

    private void pullEnergy() {
        if (level == null) return; int remaining = Math.min(INPUT, energy.getMaxEnergyStored() - energy.getEnergyStored());
        for (Direction direction : Direction.values()) {
            if (remaining <= 0) break; BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null || neighbor instanceof AndroidInductionRelayBlockEntity) continue;
            IEnergyStorage source = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (source == null || !source.canExtract()) continue;
            int offered = source.extractEnergy(remaining, true), accepted = energy.receiveEnergy(offered, true);
            if (accepted <= 0) continue; int moved = energy.receiveEnergy(source.extractEnergy(accepted, false), false); remaining -= moved;
        }
    }

    private int chargeAndroids() {
        if (level == null || level.getServer() == null || energy.getEnergyStored() <= 0) return 0;
        double rangeSq = (double)range * range; List<ServerPlayer> players = new ArrayList<>();
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() != level || !AndroidData.isAndroid(player)) continue;
            int capacity = AndroidChassisData.energyCapacity(player); if (AndroidData.getEnergy(player) >= capacity) continue;
            if (player.distanceToSqr(worldPosition.getX()+0.5D, worldPosition.getY()+0.5D, worldPosition.getZ()+0.5D) <= rangeSq) players.add(player);
        }
        if (players.isEmpty()) return 0; players.sort(Comparator.comparing(ServerPlayer::getUUID));
        int start=(int)Math.floorMod(sequence++,(long)players.size()),budget=Math.min(OUTPUT,energy.getEnergyStored()),total=0;
        for(int i=0;i<players.size()&&budget>0;i++){
            ServerPlayer player=players.get((start+i)%players.size()); int offer=Math.max(1,budget/(players.size()-i));
            int accepted=AndroidData.receiveEnergy(player,offer); if(accepted<=0)continue; int used=energy.extractEnergy(accepted,false); total+=used; budget-=used;
        }
        return total;
    }

    public InteractionResult onUse(ServerPlayer player, InteractionHand hand) {
        if(player.isCrouching()){range=switch(range){case 32->64;case 64->96;default->32;};setChanged();player.sendSystemMessage(Component.literal("Induction range set to "+range+" blocks").withStyle(ChatFormatting.AQUA));}
        else {player.sendSystemMessage(Component.literal("ANDROID INDUCTION RELAY  "+energy.getEnergyStored()+" / "+CAPACITY+" FE").withStyle(ChatFormatting.AQUA));player.sendSystemMessage(Component.literal("Range "+range+" | last wireless transfer "+lastTransfer+" FE/t | same dimension only").withStyle(ChatFormatting.GRAY));}
        return InteractionResult.CONSUME;
    }

    public int getRange(){return range;}
    public int comparatorLevel(){return(int)((long)energy.getEnergyStored()*15L/CAPACITY);}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.putInt("Energy",energy.getEnergyStored());tag.putInt("Range",range);tag.putLong("Sequence",sequence);}
    @Override public void load(CompoundTag tag){super.load(tag);energy.setEnergyStored(tag.getInt("Energy"));range=tag.contains("Range")?Math.max(16,Math.min(96,tag.getInt("Range"))):BASE_RANGE;sequence=tag.getLong("Sequence");}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap,@Nullable Direction side){return cap==ForgeCapabilities.ENERGY?energyCap.cast():super.getCapability(cap,side);}
    @Override public void invalidateCaps(){super.invalidateCaps();energyCap.invalidate();}
    @Override public void reviveCaps(){super.reviveCaps();energyCap=LazyOptional.of(()->energy);}
}
