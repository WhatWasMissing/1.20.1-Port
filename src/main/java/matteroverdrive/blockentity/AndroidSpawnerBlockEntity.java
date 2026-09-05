package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.entity.RogueAndroidEntity;
import matteroverdrive.event.AndroidEvents;
import matteroverdrive.menu.AndroidSpawnerMenu;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class AndroidSpawnerBlockEntity extends BlockEntity implements MenuProvider {
    private static final int CAPACITY = 100_000;
    private static final int TRANSFER = 2_000;
    private static final int SPAWN_COST = 20_000;
    private static final int SPAWN_INTERVAL = 200;
    private static final int FAILED_RETRY_DELAY = 20;
    private static final int MAX_SPAWN_AMOUNT = 6;
    private static final int SPAWN_CHECK_RANGE = 128;
    private static final int[][] SPAWN_OFFSETS = {
            {0, 1, 0}, {2, 1, 0}, {-2, 1, 0}, {0, 1, 2}, {0, 1, -2},
            {2, 1, 2}, {2, 1, -2}, {-2, 1, 2}, {-2, 1, -2},
            {4, 1, 0}, {-4, 1, 0}, {0, 1, 4}, {0, 1, -4}
    };

    private final MachineEnergyStorage energy =
            new MachineEnergyStorage(CAPACITY, TRANSFER, 0, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energy);
    private long lastSpawn;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored() & 0xffff;
                case 1 -> energy.getEnergyStored() >>> 16 & 0xffff;
                case 2 -> energy.getMaxEnergyStored() & 0xffff;
                case 3 -> energy.getMaxEnergyStored() >>> 16 & 0xffff;
                case 4 -> ownedSpawnCount();
                case 5 -> MAX_SPAWN_AMOUNT;
                case 6 -> ticksUntilNextSpawn();
                default -> 0;
            };
        }

        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 7; }
    };

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

        if (spawner.ownedSpawnCount() >= MAX_SPAWN_AMOUNT) {
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
        int start = level.random.nextInt(SPAWN_OFFSETS.length);
        for (int attempt = 0; attempt < SPAWN_OFFSETS.length; attempt++) {
            int[] offset = SPAWN_OFFSETS[(start + attempt) % SPAWN_OFFSETS.length];
            BlockPos candidate = spawnerPos.offset(offset[0], offset[1], offset[2]);
            if (!level.getWorldBorder().isWithinBounds(candidate)) {
                continue;
            }

            RogueAndroidEntity android = level.random.nextInt(10) < 3
                    ? ModEntities.ROGUE_ANDROID.get().create(level)
                    : ModEntities.RANGED_ROGUE_ANDROID.get().create(level);
            if (android == null) {
                return false;
            }
            android.moveTo(candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D,
                    level.random.nextFloat() * 360.0F, 0.0F);
            if (!level.noCollision(android)) {
                android.discard();
                continue;
            }

            android.finalizeSpawn(level, level.getCurrentDifficultyAt(candidate),
                    MobSpawnType.SPAWNER, null, null);
            android.setSpawnerPosition(spawnerPos);
            if (level.addFreshEntity(android)) {
                return true;
            }
        }
        return false;
    }

    private int ownedSpawnCount() {
        if (level == null) return 0;
        AABB area = new AABB(worldPosition).inflate(SPAWN_CHECK_RANGE);
        int modern = level.getEntitiesOfClass(RogueAndroidEntity.class, area,
                android -> android.wasSpawnedFrom(worldPosition)).size();
        int legacy = level.getEntitiesOfClass(Husk.class, area,
                entity -> entity.getPersistentData().getBoolean(AndroidEvents.ROGUE_ANDROID_TAG)
                        && entity.getPersistentData().contains("SpawnerPosition")
                        && BlockPos.of(entity.getPersistentData().getLong("SpawnerPosition")).equals(worldPosition)).size();
        return modern + legacy;
    }

    public int removeSpawnedAndroids() {
        if (level == null || level.isClientSide) return 0;
        AABB area = new AABB(worldPosition).inflate(SPAWN_CHECK_RANGE);
        int removed = 0;
        for (RogueAndroidEntity android : level.getEntitiesOfClass(RogueAndroidEntity.class, area,
                candidate -> candidate.wasSpawnedFrom(worldPosition))) {
            android.discard();
            removed++;
        }
        if (removed > 0) setChanged();
        return removed;
    }

    private int ticksUntilNextSpawn() {
        if (level == null) return SPAWN_INTERVAL;
        if (ownedSpawnCount() >= MAX_SPAWN_AMOUNT) return 0;
        long elapsed = Math.max(0L, level.getGameTime() - lastSpawn);
        return (int) Math.max(0L, SPAWN_INTERVAL - elapsed);
    }

    private void pullAdjacentEnergy() {
        if (level == null) return;
        int remaining = Math.min(TRANSFER, energy.getMaxEnergyStored() - energy.getEnergyStored());
        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null || remaining <= 0) continue;
            IEnergyStorage source =
                    neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (source == null || !source.canExtract()) continue;
            int amount = Math.min(source.extractEnergy(remaining, true),
                    energy.receiveEnergy(remaining, true));
            if (amount > 0) {
                remaining -= energy.receiveEnergy(source.extractEnergy(amount, false), false);
            }
        }
    }

    public ContainerData getData() { return data; }

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

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.matteroverdrive.android_spawner");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AndroidSpawnerMenu(id, inventory, this);
    }
}
