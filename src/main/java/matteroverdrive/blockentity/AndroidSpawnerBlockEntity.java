package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.event.AndroidEvents;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
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
    private static final int CAPACITY = 100_000;
    private static final int TRANSFER = 2_000;
    private static final int SPAWN_COST = 20_000;
    private static final int SPAWN_INTERVAL = 200;
    private static final int FAILED_RETRY_DELAY = 20;
    private static final int[][] SPAWN_OFFSETS = {
            {0, 1, 0}, {2, 1, 0}, {-2, 1, 0}, {0, 1, 2}, {0, 1, -2},
            {2, 1, 2}, {2, 1, -2}, {-2, 1, 2}, {-2, 1, -2}
    };

    private final MachineEnergyStorage energy =
            new MachineEnergyStorage(CAPACITY, TRANSFER, 0, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energy);
    private long lastSpawn;

    public AndroidSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANDROID_SPAWNER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  AndroidSpawnerBlockEntity spawner) {
        spawner.pullAdjacentEnergy();
        long gameTime = level.getGameTime();
        if (gameTime < spawner.lastSpawn) {
            spawner.lastSpawn = gameTime - SPAWN_INTERVAL;
        }
        if (gameTime - spawner.lastSpawn < SPAWN_INTERVAL
                || spawner.energy.getEnergyStored() < SPAWN_COST) {
            return;
        }

        AABB area = new AABB(pos).inflate(12.0D);
        if (!level.getEntitiesOfClass(Husk.class, area,
                entity -> entity.getPersistentData().getBoolean(AndroidEvents.ROGUE_ANDROID_TAG)).isEmpty()) {
            return;
        }

        if (trySpawn((ServerLevel) level, pos)) {
            spawner.energy.consumeEnergy(SPAWN_COST, gameTime);
            spawner.lastSpawn = gameTime;
        } else {
            spawner.lastSpawn = gameTime - (SPAWN_INTERVAL - FAILED_RETRY_DELAY);
        }
        spawner.setChanged();
    }

    private static boolean trySpawn(ServerLevel level, BlockPos spawnerPos) {
        for (int[] offset : SPAWN_OFFSETS) {
            BlockPos candidate = spawnerPos.offset(offset[0], offset[1], offset[2]);
            if (!level.getWorldBorder().isWithinBounds(candidate)) {
                continue;
            }

            Husk android = new Husk(EntityType.HUSK, level);
            android.moveTo(candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D,
                    level.random.nextFloat() * 360.0F, 0.0F);
            if (!level.noCollision(android)) {
                continue;
            }

            android.finalizeSpawn(level, level.getCurrentDifficultyAt(candidate),
                    MobSpawnType.SPAWNER, null, null);
            android.setCustomName(Component.literal("Rogue Android"));
            android.setCustomNameVisible(true);
            android.getPersistentData().putBoolean(AndroidEvents.ROGUE_ANDROID_TAG, true);
            if (level.addFreshEntity(android)) {
                return true;
            }
        }
        return false;
    }

    private void pullAdjacentEnergy() {
        if (level == null) {
            return;
        }
        int remaining = Math.min(TRANSFER, energy.getMaxEnergyStored() - energy.getEnergyStored());
        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null || remaining <= 0) {
                continue;
            }
            IEnergyStorage source =
                    neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (source == null || !source.canExtract()) {
                continue;
            }
            int amount = Math.min(source.extractEnergy(remaining, true),
                    energy.receiveEnergy(remaining, true));
            if (amount > 0) {
                remaining -= energy.receiveEnergy(source.extractEnergy(amount, false), false);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putLong("LastSpawn", lastSpawn);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergyStored(tag.getInt("Energy"));
        lastSpawn = tag.getLong("LastSpawn");
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        return cap == ForgeCapabilities.ENERGY
                ? energyCapability.cast()
                : super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyCapability = LazyOptional.of(() -> energy);
    }
}
