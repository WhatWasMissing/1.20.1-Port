package matteroverdrive.blockentity;

import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class PylonBlockEntity extends BlockEntity {
    private static final int MAX_LINK_DISTANCE = 64;
    private static final Map<Level, Set<BlockPos>> LOADED = new WeakHashMap<>();
    private int channel;

    public PylonBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.PYLON.get(), pos, state); }
    public int getChannel() { return channel; }
    public void nextChannel() { channel = (channel + 1) & 15; setChanged(); }
    @Override public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) LOADED.computeIfAbsent(level, ignored -> new HashSet<>()).add(worldPosition.immutable());
    }
    @Override public void setRemoved() {
        if (level != null) {
            Set<BlockPos> pylons = LOADED.get(level);
            if (pylons != null) {
                pylons.remove(worldPosition);
                if (pylons.isEmpty()) LOADED.remove(level);
            }
        }
        super.setRemoved();
    }
    public static List<BlockPos> linked(Level level, BlockPos origin, int channel) {
        List<BlockPos> result = new ArrayList<>();
        for (BlockPos candidate : LOADED.getOrDefault(level, Set.of())) {
            if (!candidate.equals(origin) && candidate.distSqr(origin) <= MAX_LINK_DISTANCE * MAX_LINK_DISTANCE
                    && level.getBlockEntity(candidate) instanceof PylonBlockEntity pylon && pylon.channel == channel) {
                result.add(candidate.immutable());
            }
        }
        return result;
    }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.putInt("Channel", channel); }
    @Override public void load(CompoundTag tag) { super.load(tag); channel = tag.getInt("Channel") & 15; }
}
