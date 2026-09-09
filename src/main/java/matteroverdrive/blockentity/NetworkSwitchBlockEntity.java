package matteroverdrive.blockentity;

import matteroverdrive.block.NetworkSwitchBlock;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.menu.NetworkSwitchMenu;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.Nullable;

public class NetworkSwitchBlockEntity extends BlockEntity implements MenuProvider {
    private boolean enabled = true;
    private int channel;

    public NetworkSwitchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NETWORK_SWITCH.get(), pos, state);
        if (state.hasProperty(NetworkSwitchBlock.ACTIVE)) enabled = state.getValue(NetworkSwitchBlock.ACTIVE);
    }

    public boolean isEnabled() { return enabled; }
    public boolean toggle() { setEnabled(!enabled); return enabled; }
    public int getChannel() { return channel; }
    public int cycleChannel() { setChannel(channel + 1); return channel; }
    public void setChannel(int value) { channel = value & 15; setChanged(); }

    private void setEnabled(boolean value) {
        enabled = value; setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            if (state.hasProperty(NetworkSwitchBlock.ACTIVE) && state.getValue(NetworkSwitchBlock.ACTIVE) != enabled)
                level.setBlock(worldPosition, state.setValue(NetworkSwitchBlock.ACTIVE, enabled), 3);
        }
    }

    public int connectionMask() {
        if (level == null) return 0;
        int mask = 0;
        for (Direction direction : Direction.values()) if (isNetworkConnection(direction)) mask |= 1 << direction.get3DDataValue();
        return mask;
    }

    private boolean isNetworkConnection(Direction direction) {
        if (level == null) return false;
        BlockPos adjacent = worldPosition.relative(direction);
        if (!level.hasChunkAt(adjacent)) return false;
        BlockState state = level.getBlockState(adjacent);
        if (state.is(ModBlocks.get("network_pipe").get()) || state.is(ModBlocks.get("network_router").get())
                || state.is(ModBlocks.get("network_switch").get()) || state.is(ModBlocks.get("pylon").get())
                || state.is(ModBlocks.get("matter_pipe").get())) return true;
        BlockEntity entity = level.getBlockEntity(adjacent);
        if (entity == null) return false;
        Direction exposedSide = direction.getOpposite();
        return entity.getCapability(ForgeCapabilities.ITEM_HANDLER, exposedSide).isPresent()
                || entity.getCapability(ModCapabilities.MATTER, exposedSide).isPresent();
    }

    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.network_switch"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new NetworkSwitchMenu(id, inventory, this); }

    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.putBoolean("Enabled", enabled); tag.putInt("Channel", channel); }
    @Override public void load(CompoundTag tag) { super.load(tag); enabled = !tag.contains("Enabled") || tag.getBoolean("Enabled"); channel = tag.getInt("Channel") & 15; }
    @Override public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            if (state.hasProperty(NetworkSwitchBlock.ACTIVE) && state.getValue(NetworkSwitchBlock.ACTIVE) != enabled)
                level.setBlock(worldPosition, state.setValue(NetworkSwitchBlock.ACTIVE, enabled), 3);
        }
    }
}