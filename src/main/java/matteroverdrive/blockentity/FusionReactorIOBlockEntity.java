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
        if (controller == null) return;
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN) continue;
            BlockEntity receiver = level.getBlockEntity(pos.relative(direction));
            if (receiver == null) continue;
            receiver.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(storage -> {
                int offer = Math.min(FusionReactorControllerBlockEntity.IO_OUTPUT_PER_SIDE,
                        controller.getEnergy().extractEnergy(FusionReactorControllerBlockEntity.IO_OUTPUT_PER_SIDE, true));
                if (offer > 0) {
                    int accepted = storage.receiveEnergy(offer, false);
                    if (accepted > 0) controller.getEnergy().extractEnergy(accepted, false);
                }
            });
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
