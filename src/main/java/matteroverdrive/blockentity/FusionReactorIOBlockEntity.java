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
import java.util.HashSet;
import java.util.Set;

public class FusionReactorIOBlockEntity extends BlockEntity {
    private BlockPos controllerPosition;
    private long matterInputSequence;
    private long matterOutputSequence;

    public FusionReactorIOBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_REACTOR_IO.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  FusionReactorIOBlockEntity io) {
        FusionReactorControllerBlockEntity controller = io.controller();
        if (controller == null) {
            return;
        }

        int matterRoom = controller.getMatter().getMatterCapacity()
                - controller.getMatter().getMatterStored();
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

        if (controller.getEnergy().getEnergyStored() <= 0) {
            return;
        }

        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> visitedReceivers = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        visited.add(pos);
        enqueueAdjacentCables(level, pos, pending, visited);

        // A machine touching a valid Reactor IO is a legitimate zero-cable output.
        // Internal-ring receivers are handled by the controller's fair internal bus.
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);
            if (neighbor == null
                    || neighbor instanceof EnergyPipeBlockEntity
                    || neighbor instanceof FusionReactorControllerBlockEntity
                    || neighbor instanceof FusionReactorIOBlockEntity
                    || controller.isInternalPowerAt(neighborPos)) {
                continue;
            }
            if (pushToReceiver(controller, neighbor, direction)) {
                visitedReceivers.add(neighborPos.immutable());
            }
        }

        while (!pending.isEmpty() && controller.getEnergy().getEnergyStored() > 0) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = current.relative(direction);
                BlockEntity neighbor = level.getBlockEntity(neighborPos);

                if (neighbor instanceof EnergyPipeBlockEntity) {
                    if (visited.add(neighborPos.immutable())) {
                        pending.addLast(neighborPos.immutable());
                    }
                    continue;
                }
                if (neighbor instanceof FusionReactorControllerBlockEntity
                        || neighbor instanceof FusionReactorIOBlockEntity
                        || controller.isInternalPowerAt(neighborPos)) {
                    continue;
                }
                if (neighbor != null && !visitedReceivers.contains(neighborPos)
                        && pushToReceiver(controller, neighbor, direction)) {
                    visitedReceivers.add(neighborPos.immutable());
                }
            }
        }
    }

    private static void enqueueAdjacentCables(Level level, BlockPos ioPosition,
                                              ArrayDeque<BlockPos> pending,
                                              Set<BlockPos> visited) {
        for (Direction direction : Direction.values()) {
            BlockPos adjacent = ioPosition.relative(direction);
            if (level.getBlockEntity(adjacent) instanceof EnergyPipeBlockEntity
                    && visited.add(adjacent.immutable())) {
                pending.addLast(adjacent.immutable());
            }
        }
    }

    private static boolean pushToReceiver(FusionReactorControllerBlockEntity controller,
                                          BlockEntity receiver, Direction direction) {
        IEnergyStorage storage = receiver.getCapability(
                ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
        if (storage == null || !storage.canReceive()) {
            return false;
        }

        controller.transferEnergyTo(storage, controller.getEnergy().getEnergyStored());
        return true;
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
        if (level == null || controllerPosition == null) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(controllerPosition);
        return blockEntity instanceof FusionReactorControllerBlockEntity controller
                && controller.isIoAt(worldPosition) ? controller : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (controllerPosition != null) {
            tag.putLong("ControllerPosition", controllerPosition.asLong());
        }
        tag.putLong("MatterInputSequence", matterInputSequence);
        tag.putLong("MatterOutputSequence", matterOutputSequence);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        controllerPosition = tag.contains("ControllerPosition")
                ? BlockPos.of(tag.getLong("ControllerPosition")) : null;
        matterInputSequence = tag.getLong("MatterInputSequence");
        matterOutputSequence = tag.getLong("MatterOutputSequence");
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        FusionReactorControllerBlockEntity controller = controller();
        if (controller == null) {
            return super.getCapability(capability, side);
        }
        if (capability == ForgeCapabilities.ENERGY) {
            // A formed IO is the reactor's sided FE face. Exposing it to every
            // face restores direct machine, cable, and capability-based extraction.
            return LazyOptional.of(controller::getEnergy).cast();
        }
        if (capability == ModCapabilities.MATTER) {
            return LazyOptional.of(controller::getMatter).cast();
        }
        return super.getCapability(capability, side);
    }
}
