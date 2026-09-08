package matteroverdrive.blockentity;

import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.network.MatterNetworkUtil;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FusionReactorIOBlockEntity extends BlockEntity {
    private BlockPos controllerPosition;
    private long matterInputSequence;
    private long matterOutputSequence;
    private long energyOutputSequence;

    public FusionReactorIOBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_REACTOR_IO.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  FusionReactorIOBlockEntity io) {
        FusionReactorControllerBlockEntity controller = io.controller();
        if (controller == null) return;

        int matterRoom = controller.getMatter().getMatterCapacity() - controller.getMatter().getMatterStored();
        if (matterRoom > 0) {
            int receivedMatter = MatterNetworkUtil.pullMatter(
                    level, pos, controller.getMatter(), matterRoom, io.matterInputSequence);
            if (receivedMatter > 0) {
                io.matterInputSequence++;
                io.setChanged();
            }
        }

        if (controller.getMatter().getMatterStored() > 0) {
            int acceptedMatter = MatterNetworkUtil.transferMatter(
                    level, pos, controller.getMatter().getMatterStored(), io.matterOutputSequence);
            if (acceptedMatter > 0) {
                controller.getMatter().extractMatter(acceptedMatter, false);
                io.matterOutputSequence++;
                io.setChanged();
            }
        }

        if (controller.getEnergy().getEnergyStored() <= 0) return;

        List<Receiver> receivers = discoverEnergyReceivers(level, pos, controller);
        if (receivers.isEmpty()) return;
        receivers.sort(Comparator.comparingLong(receiver -> receiver.position().asLong()));

        int start = (int) Math.floorMod(io.energyOutputSequence, (long) receivers.size());
        int available = controller.getEnergy().getEnergyStored();
        int sent = 0;
        for (int offset = 0; offset < receivers.size() && available > 0; offset++) {
            Receiver receiver = receivers.get((start + offset) % receivers.size());
            IEnergyStorage storage = receiver.blockEntity().getCapability(
                    ForgeCapabilities.ENERGY, receiver.side()).orElse(null);
            if (storage == null || !storage.canReceive()) continue;

            int remaining = receivers.size() - offset;
            int fairOffer = (int) Math.min(Integer.MAX_VALUE,
                    ((long) available + remaining - 1L) / remaining);
            int moved = controller.transferEnergyTo(storage, fairOffer);
            available -= moved;
            sent += moved;
        }
        io.energyOutputSequence++;
        if (sent > 0) io.setChanged();
    }

    private static List<Receiver> discoverEnergyReceivers(Level level, BlockPos ioPos,
                                                           FusionReactorControllerBlockEntity controller) {
        List<Receiver> receivers = new ArrayList<>();
        Set<BlockPos> visitedPipes = new HashSet<>();
        Set<BlockPos> visitedReceivers = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = ioPos.relative(direction);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);
            if (neighbor instanceof EnergyPipeBlockEntity) {
                if (visitedPipes.add(neighborPos.immutable())) pending.addLast(neighborPos.immutable());
                continue;
            }
            addReceiver(controller, neighborPos, neighbor, direction.getOpposite(), visitedReceivers, receivers);
        }

        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = current.relative(direction);
                BlockEntity neighbor = level.getBlockEntity(neighborPos);
                if (neighbor instanceof EnergyPipeBlockEntity) {
                    if (visitedPipes.add(neighborPos.immutable())) pending.addLast(neighborPos.immutable());
                    continue;
                }
                addReceiver(controller, neighborPos, neighbor, direction.getOpposite(), visitedReceivers, receivers);
            }
        }
        return receivers;
    }

    private static void addReceiver(FusionReactorControllerBlockEntity controller,
                                    BlockPos position, @Nullable BlockEntity blockEntity, Direction receiverSide,
                                    Set<BlockPos> visitedReceivers, List<Receiver> receivers) {
        if (blockEntity == null
                || blockEntity instanceof FusionReactorControllerBlockEntity
                || blockEntity instanceof FusionReactorIOBlockEntity
                || controller.isInternalPowerAt(position)
                || !visitedReceivers.add(position.immutable())) return;
        IEnergyStorage storage = blockEntity.getCapability(ForgeCapabilities.ENERGY, receiverSide).orElse(null);
        if (storage != null && storage.canReceive()) {
            receivers.add(new Receiver(position.immutable(), blockEntity, receiverSide));
        }
    }

    public void linkController(BlockPos controllerPos) {
        BlockPos immutable = controllerPos.immutable();
        if (!immutable.equals(controllerPosition)) {
            controllerPosition = immutable;
            setChanged();
        }
    }

    public void unlinkController(BlockPos controllerPos) {
        if (controllerPosition != null && controllerPosition.equals(controllerPos)) {
            controllerPosition = null;
            setChanged();
        }
    }

    @Nullable
    private FusionReactorControllerBlockEntity controller() {
        if (level == null || controllerPosition == null) return null;
        BlockEntity blockEntity = level.getBlockEntity(controllerPosition);
        return blockEntity instanceof FusionReactorControllerBlockEntity controller
                && controller.isIoAt(worldPosition) ? controller : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (controllerPosition != null) tag.putLong("ControllerPosition", controllerPosition.asLong());
        tag.putLong("MatterInputSequence", matterInputSequence);
        tag.putLong("MatterOutputSequence", matterOutputSequence);
        tag.putLong("EnergyOutputSequence", energyOutputSequence);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        controllerPosition = tag.contains("ControllerPosition")
                ? BlockPos.of(tag.getLong("ControllerPosition")) : null;
        matterInputSequence = tag.getLong("MatterInputSequence");
        matterOutputSequence = tag.getLong("MatterOutputSequence");
        energyOutputSequence = tag.getLong("EnergyOutputSequence");
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        FusionReactorControllerBlockEntity controller = controller();
        if (controller == null) return super.getCapability(capability, side);
        if (capability == ForgeCapabilities.ENERGY) return LazyOptional.of(controller::getEnergy).cast();
        if (capability == ModCapabilities.MATTER) return LazyOptional.of(controller::getMatter).cast();
        return super.getCapability(capability, side);
    }

    private record Receiver(BlockPos position, BlockEntity blockEntity, Direction side) {}
}
