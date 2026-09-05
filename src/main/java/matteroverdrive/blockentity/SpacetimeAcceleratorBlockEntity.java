package matteroverdrive.blockentity;

import com.mojang.logging.LogUtils;
import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.capability.MachineMatterStorage;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.item.MachineUpgradeInventory;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.menu.SpacetimeAcceleratorMenu;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class SpacetimeAcceleratorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int UPGRADE_SLOT_COUNT = 4;
    public static final int BASE_MATTER_STORAGE = 1024;
    public static final int BASE_ENERGY_STORAGE = 512000;
    public static final int BASE_ENERGY_USAGE = 64;
    public static final double BASE_MATTER_USAGE = 0.2D;
    public static final int BASE_PULSE_INTERVAL = 40;
    public static final int BASE_RADIUS = 2;
    public static final int MIN_PULSE_INTERVAL = 8;
    public static final int MAX_RADIUS = 12;

    private static final Logger LOGGER = LogUtils.getLogger();

    private final MachineEnergyStorage energyStorage = new MachineEnergyStorage(
            BASE_ENERGY_STORAGE, BASE_ENERGY_STORAGE, 0, this::setChanged);
    private final MachineMatterStorage matterStorage = new MachineMatterStorage(
            BASE_MATTER_STORAGE, true, false, this::setChanged);
    private final MachineUpgradeInventory upgrades = new MachineUpgradeInventory(
            UPGRADE_SLOT_COUNT,
            upgrade -> upgrade == MachineUpgradeItem.Upgrade.SPEED
                    || upgrade == MachineUpgradeItem.Upgrade.HYPER_SPEED
                    || upgrade == MachineUpgradeItem.Upgrade.POWER
                    || upgrade == MachineUpgradeItem.Upgrade.POWER_STORAGE
                    || upgrade == MachineUpgradeItem.Upgrade.MATTER_STORAGE
                    || upgrade == MachineUpgradeItem.Upgrade.RANGE,
            this::onUpgradesChanged
    );

    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energyStorage);
    private LazyOptional<matteroverdrive.capability.IMatterStorage> matterCapability =
            LazyOptional.of(() -> matterStorage);

    private final Set<String> failedTickerTypes = new HashSet<>();
    private int pulseTimer;
    private int lastAcceleratedTargets;
    private double matterUseRemainder;
    private boolean active;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> lowWord(energyStorage.getEnergyStored());
                case 1 -> highWord(energyStorage.getEnergyStored());
                case 2 -> lowWord(energyStorage.getMaxEnergyStored());
                case 3 -> highWord(energyStorage.getMaxEnergyStored());
                case 4 -> matterStorage.getMatterStored();
                case 5 -> matterStorage.getMatterCapacity();
                case 6 -> getEnergyUsagePerTick();
                case 7 -> getPulseInterval();
                case 8 -> getRadius();
                case 9 -> active ? 1 : 0;
                case 10 -> pulseTimer;
                case 11 -> lastAcceleratedTargets;
                case 12 -> isRedstoneBlocked() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 10) pulseTimer = Math.max(0, value);
        }

        @Override
        public int getCount() {
            return 13;
        }
    };

    public SpacetimeAcceleratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPACETIME_ACCELERATOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  SpacetimeAcceleratorBlockEntity accelerator) {
        accelerator.energyStorage.beginUsageTick(level.getGameTime());
        accelerator.active = accelerator.canRun();
        if (!accelerator.active) return;

        int energyUsage = accelerator.getEnergyUsagePerTick();
        if (accelerator.energyStorage.consumeEnergy(energyUsage, level.getGameTime()) < energyUsage) {
            accelerator.active = false;
            return;
        }

        accelerator.pulseTimer++;
        int interval = accelerator.getPulseInterval();
        if (accelerator.pulseTimer >= interval) {
            accelerator.pulseTimer = 0;
            accelerator.consumePulseMatter();
            accelerator.lastAcceleratedTargets = accelerator.manageAccelerations();
            accelerator.setChanged();
        }
    }

    private boolean canRun() {
        if (level == null || level.isClientSide || isRedstoneBlocked()) return false;
        int energyUsage = getEnergyUsagePerTick();
        if (energyStorage.getEnergyStored() < energyUsage) return false;
        return matterStorage.getMatterStored() >= matterRequiredForNextPulse();
    }

    private int matterRequiredForNextPulse() {
        int whole = (int) Math.floor(matterUseRemainder + getMatterUsagePerPulse());
        return Math.max(1, whole);
    }

    private void consumePulseMatter() {
        matterUseRemainder += getMatterUsagePerPulse();
        int whole = (int) Math.floor(matterUseRemainder);
        if (whole <= 0) return;
        int consumed = Math.min(whole, matterStorage.getMatterStored());
        matterStorage.setMatterStored(matterStorage.getMatterStored() - consumed);
        matterUseRemainder -= consumed;
    }

    private int manageAccelerations() {
        if (!(level instanceof ServerLevel serverLevel)) return 0;
        int radius = getRadius();
        int accelerated = 0;
        // 1.12 iterated [-radius, radius), giving a 2r x 2r footprint rather
        // than the (2r+1)^2 footprint produced by inclusive upper bounds.
        for (int x = -radius; x < radius; x++) {
            for (int z = -radius; z < radius; z++) {
                BlockPos targetPos = worldPosition.offset(x, 0, z);
                if (!serverLevel.hasChunkAt(targetPos)) continue;
                BlockState targetState = serverLevel.getBlockState(targetPos);

                if (targetState.isRandomlyTicking()) {
                    targetState.randomTick(serverLevel, targetPos, serverLevel.random);
                    accelerated++;
                }

                BlockState tickerState = serverLevel.getBlockState(targetPos);
                BlockEntity target = serverLevel.getBlockEntity(targetPos);
                if (target != null && tickBlockEntitySafely(serverLevel, targetPos, tickerState, target)) {
                    accelerated++;
                }
            }
        }
        return accelerated;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean tickBlockEntitySafely(ServerLevel serverLevel, BlockPos targetPos,
                                          BlockState targetState, BlockEntity target) {
        if (target == this || target instanceof SpacetimeAcceleratorBlockEntity || target.isRemoved()) return false;
        String tickerType = target.getType().toString();
        if (failedTickerTypes.contains(tickerType)) return false;

        Block block = targetState.getBlock();
        if (!(block instanceof EntityBlock entityBlock)) return false;
        BlockEntityTicker ticker = entityBlock.getTicker(serverLevel, targetState, target.getType());
        if (ticker == null) return false;

        try {
            ticker.tick(serverLevel, targetPos, targetState, target);
            return true;
        } catch (RuntimeException exception) {
            failedTickerTypes.add(tickerType);
            LOGGER.warn("Space-Time Accelerator at {} disabled extra ticking for {} after target {} failed",
                    worldPosition, tickerType, targetPos, exception);
            return false;
        }
    }

    private void onUpgradesChanged() {
        energyStorage.setCapacity((int) Math.min(Integer.MAX_VALUE,
                Math.round(BASE_ENERGY_STORAGE
                        * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerStorage))));
        matterStorage.setCapacity((int) Math.min(Integer.MAX_VALUE,
                Math.round(BASE_MATTER_STORAGE
                        * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::matterStorage))));
        setChanged();
    }

    public int getEnergyUsagePerTick() {
        return Math.max(1, (int) Math.round(BASE_ENERGY_USAGE
                * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerUsage)));
    }

    public double getMatterUsagePerPulse() {
        return Math.max(0.01D, BASE_MATTER_USAGE
                * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::matterUsage));
    }

    public int getPulseInterval() {
        return Math.max(MIN_PULSE_INTERVAL, (int) Math.round(BASE_PULSE_INTERVAL
                * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::speed)));
    }

    public int getRadius() {
        double multiplier = upgrades.getMultiplier(MachineUpgradeItem.Upgrade::range);
        multiplier = Math.min(6.0D, Math.max(1.0D, multiplier));
        return Math.min(MAX_RADIUS, Math.max(1, (int) Math.round(BASE_RADIUS * multiplier)));
    }

    public boolean isRedstoneBlocked() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    public MachineEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public MachineMatterStorage getMatterStorage() {
        return matterStorage;
    }

    public MachineUpgradeInventory getUpgradeInventory() {
        return upgrades;
    }

    public ContainerData getContainerData() {
        return data;
    }

    public void fillMatterForDebug() {
        matterStorage.setMatterStored(matterStorage.getMatterCapacity());
    }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            ItemStack stack = upgrades.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D, stack.copy());
                upgrades.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Upgrades", upgrades.serializeNBT());
        tag.putInt("Energy", energyStorage.getEnergyStored());
        tag.putBoolean("InfiniteEnergy", energyStorage.isInfiniteEnergy());
        tag.putInt("Matter", matterStorage.getMatterStored());
        tag.putInt("PulseTimer", pulseTimer);
        tag.putInt("LastAcceleratedTargets", lastAcceleratedTargets);
        tag.putDouble("MatterUseRemainder", matterUseRemainder);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Upgrades")) upgrades.deserializeNBT(tag.getCompound("Upgrades"));
        onUpgradesChanged();
        energyStorage.setEnergyStored(tag.getInt("Energy"));
        energyStorage.setInfiniteEnergy(tag.getBoolean("InfiniteEnergy"));
        matterStorage.setMatterStored(tag.getInt("Matter"));
        pulseTimer = Math.max(0, tag.getInt("PulseTimer"));
        lastAcceleratedTargets = Math.max(0, tag.getInt("LastAcceleratedTargets"));
        matterUseRemainder = Math.max(0.0D, tag.getDouble("MatterUseRemainder"));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyCapability.cast();
        if (cap == ModCapabilities.MATTER) return matterCapability.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
        matterCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyCapability = LazyOptional.of(() -> energyStorage);
        matterCapability = LazyOptional.of(() -> matterStorage);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.matteroverdrive.spacetime_accelerator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SpacetimeAcceleratorMenu(containerId, playerInventory, this);
    }

    private static int lowWord(int value) {
        return value & 0xFFFF;
    }

    private static int highWord(int value) {
        return (value >>> 16) & 0xFFFF;
    }
}
