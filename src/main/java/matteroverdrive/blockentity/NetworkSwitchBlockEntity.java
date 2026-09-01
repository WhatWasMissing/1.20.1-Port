package matteroverdrive.blockentity;

import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class NetworkSwitchBlockEntity extends BlockEntity {
    private boolean enabled = true;
    public NetworkSwitchBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.NETWORK_SWITCH.get(), pos, state); }
    public boolean isEnabled() { return enabled; }
    public boolean toggle() { enabled = !enabled; setChanged(); return enabled; }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.putBoolean("Enabled", enabled); }
    @Override public void load(CompoundTag tag) { super.load(tag); enabled = !tag.contains("Enabled") || tag.getBoolean("Enabled"); }
}