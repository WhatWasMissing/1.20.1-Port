package matteroverdrive.blockentity;

import matteroverdrive.capability.IMatterStorage;
import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.capability.MachineMatterStorage;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.machine.MachineSideConfigurationData;
import matteroverdrive.network.MatterNetworkUtil;
import matteroverdrive.registry.OverhaulContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

/** Stores reactor FE and Matter independently until adjacent networks can use them. */
public final class EnergyBankBlockEntity extends BlockEntity {
    public static final int ENERGY_CAPACITY = 32_000_000;
    public static final int MATTER_CAPACITY = 1_000_000;
    public static final int ENERGY_TRANSFER = 65_536;
    public static final int MATTER_TRANSFER = 4_096;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(
            ENERGY_CAPACITY, ENERGY_TRANSFER, ENERGY_TRANSFER, this::setChanged);
    private final MachineMatterStorage matter = new MachineMatterStorage(
            MATTER_CAPACITY, true, true, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energy);
    private LazyOptional<IMatterStorage> matterCapability = LazyOptional.of(() -> matter);
    private int outputCursor;
    private long matterSequence;
    private int lastEnergyOutput;
    private int lastMatterOutput;

    public EnergyBankBlockEntity(BlockPos pos, BlockState state) {
        super(OverhaulContent.ENERGY_BANK_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EnergyBankBlockEntity bank) {
        bank.lastEnergyOutput = bank.pushEnergy();
        bank.lastMatterOutput = bank.pushMatter();
        if (level.getGameTime() % 20L == 0L) bank.setChanged();
    }

    private int pushEnergy() {
        if (level == null || energy.getEnergyStored() <= 0) return 0;
        Direction[] directions = Direction.values();
        int start = Math.floorMod(outputCursor++, directions.length);
        int moved = 0;
        for (int offset = 0; offset < directions.length && moved < ENERGY_TRANSFER; offset++) {
            Direction direction = directions[(start + offset) % directions.length];
            BlockEntity target = level.getBlockEntity(worldPosition.relative(direction));
            if (target == null || target instanceof EnergyBankBlockEntity
                    || target instanceof FusionReactorIOBlockEntity
                    || target instanceof FusionReactorControllerBlockEntity) continue;

            IEnergyStorage receiver;
            if (target instanceof EnergyPipeBlockEntity pipe) {
                receiver = pipe.getEnergy();
            } else {
                Direction receiverSide = direction.getOpposite();
                if (!MachineSideConfigurationData.allowsInput(level, target.getBlockPos(), receiverSide,
                        MachineSideConfigurationData.Resource.ENERGY)) continue;
                receiver = target.getCapability(ForgeCapabilities.ENERGY, receiverSide).orElse(null);
            }
            if (receiver == null || receiver == energy || !receiver.canReceive()) continue;

            int offered = Math.min(ENERGY_TRANSFER - moved, energy.getEnergyStored());
            int accepted = receiver.receiveEnergy(offered, true);
            if (accepted <= 0) continue;
            int extracted = energy.extractEnergy(accepted, false);
            if (extracted <= 0) continue;
            int received = receiver.receiveEnergy(extracted, false);
            if (received < extracted) energy.setEnergyStored(energy.getEnergyStored() + extracted - received);
            moved += received;
        }
        return moved;
    }

    private int pushMatter() {
        if (level == null || matter.getMatterStored() <= 0) return 0;
        int requested = Math.min(MATTER_TRANSFER, matter.getMatterStored());
        int moved = MatterNetworkUtil.transferMatter(level, worldPosition, requested, matterSequence++);
        if (moved > 0) matter.extractMatter(moved, false);
        return moved;
    }

    public void displayStatus(Player player) {
        player.displayClientMessage(Component.literal(
                "Energy Bank // FE " + energy.getEnergyStored() + "/" + ENERGY_CAPACITY
                        + " // Matter " + matter.getMatterStored() + "/" + MATTER_CAPACITY
                        + " // Output " + lastEnergyOutput + " FE/t, " + lastMatterOutput + " matter/t"
        ).withStyle(ChatFormatting.AQUA), true);
    }

    public int comparatorLevel() {
        long filled = (long) energy.getEnergyStored() * MATTER_CAPACITY
                + (long) matter.getMatterStored() * ENERGY_CAPACITY;
        long capacity = (long) ENERGY_CAPACITY * MATTER_CAPACITY * 2L;
        return capacity <= 0L ? 0 : Math.min(15, (int) (filled * 15L / capacity));
    }

    public MachineEnergyStorage getEnergy() {
        return energy;
    }

    public MachineMatterStorage getMatter() {
        return matter;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Matter", matter.getMatterStored());
        tag.putInt("OutputCursor", outputCursor);
        tag.putLong("MatterSequence", matterSequence);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergyStored(tag.getInt("Energy"));
        matter.setMatterStored(tag.getInt("Matter"));
        outputCursor = tag.getInt("OutputCursor");
        matterSequence = tag.getLong("MatterSequence");
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) return energyCapability.cast();
        if (capability == ModCapabilities.MATTER) return matterCapability.cast();
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
        matterCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyCapability = LazyOptional.of(() -> energy);
        matterCapability = LazyOptional.of(() -> matter);
    }
}
