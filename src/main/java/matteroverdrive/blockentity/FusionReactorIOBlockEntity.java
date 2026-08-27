package matteroverdrive.blockentity;

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

/** Energy export point for the compact Fusion Reactor multiblock. */
public class FusionReactorIOBlockEntity extends BlockEntity {
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.empty();

    public FusionReactorIOBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_REACTOR_IO.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FusionReactorIOBlockEntity io) {
        FusionReactorControllerBlockEntity controller = io.controller();
        if (controller == null || controller.getEnergy().getEnergyStored() <= 0) return;
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN || controller.getEnergy().getEnergyStored() <= 0) continue;
            BlockEntity receiver = level.getBlockEntity(pos.relative(direction));
            if (receiver == null) continue;
            IEnergyStorage storage = receiver.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (storage == null || !storage.canReceive()) continue;
            int offer = Math.min(FusionReactorControllerBlockEntity.IO_OUTPUT_PER_SIDE,
                    controller.getEnergy().getEnergyStored());
            int accepted = storage.receiveEnergy(offer, true);
            int extracted = controller.getEnergy().extractEnergy(accepted, false);
            if (extracted > 0) {
                int received = storage.receiveEnergy(extracted, false);
                if (received < extracted) {
                    controller.getEnergy().setEnergyStored(controller.getEnergy().getEnergyStored() + extracted - received);
                }
            }
        }
    }

    @Nullable private FusionReactorControllerBlockEntity controller() {
        if (level == null) return null;
        BlockEntity blockEntity = level.getBlockEntity(worldPosition.below());
        return blockEntity instanceof FusionReactorControllerBlockEntity controller && controller.isIoAt(worldPosition)
                ? controller : null;
    }

    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) {
            FusionReactorControllerBlockEntity controller = controller();
            return controller == null ? LazyOptional.empty() : LazyOptional.of(controller::getEnergy).cast();
        }
        return super.getCapability(capability, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); energyCap.invalidate(); }
}
