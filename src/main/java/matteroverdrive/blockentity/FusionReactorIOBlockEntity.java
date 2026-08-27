package matteroverdrive.blockentity;

import matteroverdrive.capability.IMatterStorage;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

/** Energy export point for the compact Fusion Reactor multiblock. */
public class FusionReactorIOBlockEntity extends BlockEntity {
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.empty();

    public FusionReactorIOBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_REACTOR_IO.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FusionReactorIOBlockEntity io) {
        FusionReactorControllerBlockEntity controller = io.controller();
        if (controller == null || controller.getEnergy().getEnergyStored() <= 0) return;

        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        pending.add(pos);
        visited.add(pos);

        while (!pending.isEmpty() && controller.getEnergy().getEnergyStored() > 0) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = current.relative(direction);
                BlockEntity neighbor = level.getBlockEntity(neighborPos);

                if (neighbor instanceof EnergyPipeBlockEntity) {
                    if (visited.add(neighborPos)) {
                        pending.addLast(neighborPos);
                    }
                    continue;
                }

                if (current.equals(pos) && neighbor instanceof FusionReactorControllerBlockEntity) {
                    continue;
                }
                if (neighbor instanceof FusionReactorIOBlockEntity) continue;

                if (neighbor != null) {
                    pushToReceiver(controller, neighbor, direction);
                }
            }
        }
    }

    private static void pushToReceiver(FusionReactorControllerBlockEntity controller,
                                       BlockEntity receiver, Direction direction) {
        IEnergyStorage storage = receiver.getCapability(
                ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
        if (storage == null || !storage.canReceive()) return;

        int offer = Math.min(FusionReactorControllerBlockEntity.IO_OUTPUT_PER_SIDE,
                controller.getEnergy().getEnergyStored());
        int accepted = storage.receiveEnergy(offer, true);
        if (accepted <= 0) return;

        int extracted = controller.getEnergy().extractEnergy(accepted, false);
        if (extracted <= 0) return;

        int received = storage.receiveEnergy(extracted, false);
        if (received < extracted) {
            controller.getEnergy().setEnergyStored(
                    controller.getEnergy().getEnergyStored() + extracted - received);
        }
    }

    @Nullable private FusionReactorControllerBlockEntity controller() {
        if (level == null) return null;
        BlockEntity blockEntity = level.getBlockEntity(worldPosition.below());
        return blockEntity instanceof FusionReactorControllerBlockEntity controller && controller.isIoAt(worldPosition)
                ? controller : null;
    }

    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        FusionReactorControllerBlockEntity controller = controller();
        if (controller == null) {
            return super.getCapability(capability, side);
        }
        if (capability == ForgeCapabilities.ENERGY) {
            return LazyOptional.of(controller::getEnergy).cast();
        }
        if (capability == ModCapabilities.MATTER) {
            return LazyOptional.of(controller::getMatter).cast();
        }
        return super.getCapability(capability, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); energyCap.invalidate(); }
}
