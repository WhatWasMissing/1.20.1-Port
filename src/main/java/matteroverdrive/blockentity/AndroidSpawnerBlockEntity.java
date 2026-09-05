package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.entity.RogueAndroidEntity;
import matteroverdrive.item.TransportFlashDriveItem;
import matteroverdrive.menu.AndroidSpawnerMenu;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AndroidSpawnerBlockEntity extends BlockEntity implements MenuProvider {
    private static final int CAPACITY = 100_000;
    private static final int TRANSFER = 2_000;
    private static final int SPAWN_COST = 20_000;
    private static final int SPAWN_INTERVAL = 200;
    private static final int FAILED_RETRY_DELAY = 20;
    private static final int MAX_SPAWN_AMOUNT = 6;
    private static final int OWNERSHIP_MIGRATION_RANGE = 128;
    public static final int PATROL_SLOT_COUNT = 6;
    private static final int[][] SPAWN_OFFSETS = {
            {0, 1, 0}, {2, 1, 0}, {-2, 1, 0}, {0, 1, 2}, {0, 1, -2},
            {2, 1, 2}, {2, 1, -2}, {-2, 1, 2}, {-2, 1, -2},
            {4, 1, 0}, {-4, 1, 0}, {0, 1, 4}, {0, 1, -4}
    };

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, TRANSFER, 0, this::setChanged);
    private final ItemStackHandler patrolDrives = new ItemStackHandler(PATROL_SLOT_COUNT) {
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return stack.getItem() instanceof TransportFlashDriveItem; }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private final Set<UUID> ownedAndroids = new LinkedHashSet<>();
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energy);
    private LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> patrolDrives);
    private long lastSpawn;
    private long lastOwnershipMigration;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored() & 0xffff;
                case 1 -> energy.getEnergyStored() >>> 16 & 0xffff;
                case 2 -> energy.getMaxEnergyStored() & 0xffff;
                case 3 -> energy.getMaxEnergyStored() >>> 16 & 0xffff;
                case 4 -> ownedSpawnCount();
                case 5 -> MAX_SPAWN_AMOUNT;
                case 6 -> ticksUntilNextSpawn();
                case 7 -> patrolTargets().size();
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 8; }
    };

    public AndroidSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANDROID_SPAWNER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AndroidSpawnerBlockEntity spawner) {
        spawner.pullAdjacentEnergy();
        long gameTime = level.getGameTime();
        if (gameTime < spawner.lastSpawn) spawner.lastSpawn = gameTime - SPAWN_INTERVAL;
        if (spawner.ownedAndroids.isEmpty() || gameTime - spawner.lastOwnershipMigration >= 100) {
            spawner.discoverNearbyOwnedAndroids();
            spawner.lastOwnershipMigration = gameTime;
        }
        if (gameTime - spawner.lastSpawn < SPAWN_INTERVAL
                || spawner.energy.getEnergyStored() < SPAWN_COST
                || spawner.ownedSpawnCount() >= MAX_SPAWN_AMOUNT) return;

        if (spawner.trySpawn((ServerLevel) level, pos)) {
            spawner.energy.consumeEnergy(SPAWN_COST, gameTime);
            spawner.lastSpawn = gameTime;
        } else {
            spawner.lastSpawn = gameTime - (SPAWN_INTERVAL - FAILED_RETRY_DELAY);
        }
        spawner.setChanged();
    }

    private boolean trySpawn(ServerLevel level, BlockPos spawnerPos) {
        int start = level.random.nextInt(SPAWN_OFFSETS.length);
        List<BlockPos> patrol = patrolTargets();
        for (int attempt = 0; attempt < SPAWN_OFFSETS.length; attempt++) {
            int[] offset = SPAWN_OFFSETS[(start + attempt) % SPAWN_OFFSETS.length];
            BlockPos candidate = spawnerPos.offset(offset[0], offset[1], offset[2]);
            if (!level.getWorldBorder().isWithinBounds(candidate)) continue;

            RogueAndroidEntity android = level.random.nextInt(10) < 3
                    ? ModEntities.ROGUE_ANDROID.get().create(level)
                    : ModEntities.RANGED_ROGUE_ANDROID.get().create(level);
            if (android == null) return false;
            android.moveTo(candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D,
                    level.random.nextFloat() * 360.0F, 0.0F);
            if (!level.noCollision(android)) {
                android.discard();
                continue;
            }

            android.finalizeSpawn(level, level.getCurrentDifficultyAt(candidate), MobSpawnType.SPAWNER, null, null);
            android.setSpawnerPosition(spawnerPos);
            android.setPatrolPoints(patrol);
            if (level.addFreshEntity(android)) {
                registerOwnedAndroid(android.getUUID());
                return true;
            }
        }
        return false;
    }

    private List<BlockPos> patrolTargets() {
        if (level == null) return List.of();
        List<BlockPos> result = new ArrayList<>();
        for (int slot = 0; slot < patrolDrives.getSlots(); slot++) {
            ItemStack drive = patrolDrives.getStackInSlot(slot);
            if (TransportFlashDriveItem.hasTarget(drive, level)) {
                BlockPos target = TransportFlashDriveItem.getTarget(drive);
                if (!result.contains(target)) result.add(target.immutable());
            }
        }
        return result;
    }

    private void discoverNearbyOwnedAndroids() {
        if (level == null) return;
        AABB area = new AABB(worldPosition).inflate(OWNERSHIP_MIGRATION_RANGE);
        boolean changed = false;
        for (RogueAndroidEntity android : level.getEntitiesOfClass(RogueAndroidEntity.class, area,
                candidate -> candidate.wasSpawnedFrom(worldPosition))) {
            changed |= ownedAndroids.add(android.getUUID());
        }
        if (changed) setChanged();
    }

    private int ownedSpawnCount() {
        return ownedAndroids.size();
    }

    public void registerOwnedAndroid(UUID uuid) {
        if (ownedAndroids.add(uuid)) setChanged();
    }

    public void unregisterOwnedAndroid(UUID uuid) {
        if (ownedAndroids.remove(uuid)) setChanged();
    }

    public int removeSpawnedAndroids() {
        if (!(level instanceof ServerLevel serverLevel)) return 0;
        discoverNearbyOwnedAndroids();
        int removed = 0;
        for (UUID uuid : List.copyOf(ownedAndroids)) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof RogueAndroidEntity android && android.wasSpawnedFrom(worldPosition)) {
                android.discard();
                removed++;
            }
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
            IEnergyStorage source = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (source == null || !source.canExtract()) continue;
            int amount = Math.min(source.extractEnergy(remaining, true), energy.receiveEnergy(remaining, true));
            if (amount > 0) remaining -= energy.receiveEnergy(source.extractEnergy(amount, false), false);
        }
    }

    public ItemStackHandler getPatrolDrives() { return patrolDrives; }
    public ContainerData getData() { return data; }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        for (int slot = 0; slot < patrolDrives.getSlots(); slot++) {
            ItemStack stack = patrolDrives.extractItem(slot, 1, false);
            if (!stack.isEmpty()) Containers.dropItemStack(level, worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, stack);
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putLong("LastSpawn", lastSpawn);
        tag.put("PatrolDrives", patrolDrives.serializeNBT());
        ListTag owners = new ListTag();
        for (UUID uuid : ownedAndroids) owners.add(NbtUtils.createUUID(uuid));
        tag.put("OwnedAndroids", owners);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergyStored(tag.getInt("Energy"));
        lastSpawn = tag.getLong("LastSpawn");
        if (tag.contains("PatrolDrives")) patrolDrives.deserializeNBT(tag.getCompound("PatrolDrives"));
        ownedAndroids.clear();
        ListTag owners = tag.getList("OwnedAndroids", Tag.TAG_INT_ARRAY);
        for (Tag owner : owners) {
            try { ownedAndroids.add(NbtUtils.loadUUID(owner)); }
            catch (IllegalArgumentException ignored) {}
        }
    }

    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyCapability.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemCapability.cast();
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
        itemCapability.invalidate();
    }

    @Override public void reviveCaps() {
        super.reviveCaps();
        energyCapability = LazyOptional.of(() -> energy);
        itemCapability = LazyOptional.of(() -> patrolDrives);
    }

    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.android_spawner"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AndroidSpawnerMenu(id, inventory, this);
    }
}
