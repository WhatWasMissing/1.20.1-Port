package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.event.AndroidEvents;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import javax.annotation.Nullable;

public class AndroidSpawnerBlockEntity extends BlockEntity {
    private static final int CAPACITY = 100_000, TRANSFER = 2_000, SPAWN_COST = 20_000;
    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, TRANSFER, 0, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energy);
    private long lastSpawn;

    public AndroidSpawnerBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ANDROID_SPAWNER.get(), pos, state); }
    public static void serverTick(Level level, BlockPos pos, BlockState state, AndroidSpawnerBlockEntity spawner) {
        spawner.pullAdjacentEnergy();
        if (level.getGameTime() - spawner.lastSpawn < 200 || spawner.energy.getEnergyStored() < SPAWN_COST) return;
        AABB area = new AABB(pos).inflate(12.0D);
        if (!level.getEntitiesOfClass(Husk.class, area, e -> e.getPersistentData().getBoolean(AndroidEvents.ROGUE_ANDROID_TAG)).isEmpty()) return;
        Husk android = new Husk(EntityType.HUSK, level);
        android.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0);
        android.finalizeSpawn((ServerLevel) level, level.getCurrentDifficultyAt(pos), MobSpawnType.SPAWNER, null, null);
        android.setCustomName(Component.literal("Rogue Android"));
        android.setCustomNameVisible(true);
        android.getPersistentData().putBoolean(AndroidEvents.ROGUE_ANDROID_TAG, true);
        level.addFreshEntity(android);
        spawner.energy.consumeEnergy(SPAWN_COST, level.getGameTime());
        spawner.lastSpawn = level.getGameTime();
    }
    private void pullAdjacentEnergy() {
        if (level == null) return;
        int remaining = Math.min(TRANSFER, energy.getMaxEnergyStored() - energy.getEnergyStored());
        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null || remaining <= 0) continue;
            IEnergyStorage source = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (source == null || !source.canExtract()) continue;
            int amount = Math.min(source.extractEnergy(remaining, true), energy.receiveEnergy(remaining, true));
            if (amount > 0) remaining -= energy.receiveEnergy(source.extractEnergy(amount, false), false);
        }
    }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.putInt("Energy", energy.getEnergyStored()); tag.putLong("LastSpawn", lastSpawn); }
    @Override public void load(CompoundTag tag) { super.load(tag); energy.setEnergyStored(tag.getInt("Energy")); lastSpawn = tag.getLong("LastSpawn"); }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) { return cap == ForgeCapabilities.ENERGY ? energyCapability.cast() : super.getCapability(cap, side); }
    @Override public void invalidateCaps() { super.invalidateCaps(); energyCapability.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); energyCapability = LazyOptional.of(() -> energy); }
}