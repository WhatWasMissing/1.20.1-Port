package matteroverdrive.blockentity;

import matteroverdrive.block.NetworkSwitchBlock;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class NetworkSwitchBlockEntity extends BlockEntity {
    private boolean enabled = true;

    public NetworkSwitchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NETWORK_SWITCH.get(), pos, state);
        if (state.hasProperty(NetworkSwitchBlock.ACTIVE)) {
            enabled = state.getValue(NetworkSwitchBlock.ACTIVE);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean toggle() {
        setEnabled(!enabled);
        return enabled;
    }

    private void setEnabled(boolean value) {
        enabled = value;
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            if (state.hasProperty(NetworkSwitchBlock.ACTIVE)
                    && state.getValue(NetworkSwitchBlock.ACTIVE) != enabled) {
                level.setBlock(worldPosition, state.setValue(NetworkSwitchBlock.ACTIVE, enabled), 3);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Enabled", enabled);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        enabled = !tag.contains("Enabled") || tag.getBoolean("Enabled");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            if (state.hasProperty(NetworkSwitchBlock.ACTIVE)
                    && state.getValue(NetworkSwitchBlock.ACTIVE) != enabled) {
                level.setBlock(worldPosition, state.setValue(NetworkSwitchBlock.ACTIVE, enabled), 3);
            }
        }
    }
}
