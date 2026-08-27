package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.menu.EnergyPipeMenu;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

/** Forge Energy relay used by the existing heavy_matter_pipe block ID. */
public class EnergyPipeBlockEntity extends BlockEntity implements MenuProvider {
    public static final int BUFFER_CAPACITY = 8_192;
    public static final int TRANSFER_PER_SIDE = 1_024;
    private final MachineEnergyStorage energy = new MachineEnergyStorage(BUFFER_CAPACITY, TRANSFER_PER_SIDE, TRANSFER_PER_SIDE, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energy);
    private int lastOutput;
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored() & 0xffff;
                case 1 -> (energy.getEnergyStored() >>> 16) & 0xffff;
                case 2 -> energy.getMaxEnergyStored() & 0xffff;
                case 3 -> (energy.getMaxEnergyStored() >>> 16) & 0xffff;
                case 4 -> lastOutput;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 5; }
    };

    public EnergyPipeBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ENERGY_PIPE.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EnergyPipeBlockEntity pipe) {
        pipe.lastOutput = pipe.pushEnergy();
    }

    private int pushEnergy() {
        if (level == null || energy.getEnergyStored() <= 0) return 0;
        int sent = 0;
        for (Direction direction : Direction.values()) {
            if (energy.getEnergyStored() <= 0) break;
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null) continue;
            IEnergyStorage receiver = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (receiver == null || !receiver.canReceive()) continue;
            int offered = Math.min(TRANSFER_PER_SIDE, energy.getEnergyStored());
            int accepted = receiver.receiveEnergy(offered, true);
            int extracted = energy.extractEnergy(accepted, false);
            if (extracted <= 0) continue;
            int received = receiver.receiveEnergy(extracted, false);
            sent += received;
            if (received < extracted) energy.setEnergyStored(energy.getEnergyStored() + extracted - received);
        }
        return sent;
    }

    public MachineEnergyStorage getEnergy() { return energy; }
    public ContainerData getData() { return data; }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energy.getEnergyStored());
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergyStored(tag.getInt("Energy"));
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) return energyCapability.cast();
        return super.getCapability(capability, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); energyCapability.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); energyCapability = LazyOptional.of(() -> energy); }
    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.heavy_matter_pipe"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new EnergyPipeMenu(id, inventory, this); }
}