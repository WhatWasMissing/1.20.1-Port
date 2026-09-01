package matteroverdrive.blockentity;

import matteroverdrive.block.GravitationalStabilizerBlock;
import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.item.MachineUpgradeInventory;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.menu.GravitationalStabilizerMenu;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class GravitationalStabilizerBlockEntity extends BlockEntity implements MenuProvider {
    private static final int MAX_DISTANCE = 63;
    private static final int BASE_POWER_PER_TICK = 64;
    private static final double BASE_SUPPRESSION = 0.7D;

    private final MachineEnergyStorage energy =
            new MachineEnergyStorage(100_000, 4_096, 0, this::setChanged);
    private final MachineUpgradeInventory upgrades = new MachineUpgradeInventory(
            4,
            upgrade -> upgrade == MachineUpgradeItem.Upgrade.POWER
                    || upgrade == MachineUpgradeItem.Upgrade.POWER_STORAGE,
            this::upgradesChanged);
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energy);

    private int anomalyDistance = -1;
    private int beamBlockedDistance = -1;
    private boolean beamBlocked;
    private int powerUsed;
    private boolean powered;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> low(energy.getEnergyStored());
                case 1 -> high(energy.getEnergyStored());
                case 2 -> low(energy.getMaxEnergyStored());
                case 3 -> high(energy.getMaxEnergyStored());
                case 4 -> requiredPower();
                case 5 -> powerUsed;
                case 6 -> powered ? 1 : 0;
                case 7 -> anomalyDistance + 1;
                case 8 -> beamBlocked ? 1 : 0;
                case 9 -> beamBlockedDistance + 1;
                default -> 0;
            };
        }

        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 10; }
    };

    public GravitationalStabilizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRAVITATIONAL_STABILIZER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  GravitationalStabilizerBlockEntity stabilizer) {
        stabilizer.anomalyDistance = -1;
        stabilizer.beamBlockedDistance = -1;
        stabilizer.beamBlocked = false;
        stabilizer.powerUsed = 0;
        stabilizer.powered = false;

        int required = stabilizer.requiredPower();
        if (stabilizer.energy.getEnergyStored() < required
                || stabilizer.energy.consumeEnergy(required, level.getGameTime()) < required) {
            return;
        }
        stabilizer.powerUsed = required;
        stabilizer.powered = true;

        Direction facing = state.getValue(GravitationalStabilizerBlock.FACING);
        for (int distance = 1; distance <= MAX_DISTANCE; distance++) {
            BlockPos targetPos = pos.relative(facing, distance);
            if (!level.hasChunkAt(targetPos)) return;
            BlockState targetState = level.getBlockState(targetPos);
            if (level.getBlockEntity(targetPos) instanceof GravitationalAnomalyBlockEntity anomaly) {
                anomaly.suppress(pos, 20, stabilizer.suppressionAmount());
                stabilizer.anomalyDistance = distance;
                return;
            }
            if (!targetState.isAir()
                    && !targetState.getCollisionShape(level, targetPos).isEmpty()) {
                stabilizer.beamBlocked = true;
                stabilizer.beamBlockedDistance = distance;
                return;
            }
        }
    }

    private int requiredPower() {
        return (int) Math.max(1, Math.round(BASE_POWER_PER_TICK
                * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerUsage)));
    }

    private double suppressionAmount() {
        int powerUpgrades = 0;
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            ItemStack stack = upgrades.getStackInSlot(slot);
            if (stack.getItem() instanceof MachineUpgradeItem item
                    && item.getUpgrade() == MachineUpgradeItem.Upgrade.POWER) {
                powerUpgrades++;
            }
        }
        return Math.min(0.95D, BASE_SUPPRESSION + powerUpgrades * 0.05D);
    }

    private void upgradesChanged() {
        energy.setCapacity((int) Math.min(Integer.MAX_VALUE, Math.round(
                100_000D * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerStorage))));
        setChanged();
    }

    public MachineUpgradeInventory getUpgrades() { return upgrades; }
    public ContainerData getContainerData() { return data; }
    public int requiredPowerForDisplay() { return requiredPower(); }
    public int getPowerUsed() { return powerUsed; }
    public boolean isPowered() { return powered; }
    public int getAnomalyDistance() { return anomalyDistance; }
    public boolean isBeamBlocked() { return beamBlocked; }
    public int getBeamBlockedDistance() { return beamBlockedDistance; }

    public boolean insertUpgrade(ItemStack stack) {
        if (!(stack.getItem() instanceof MachineUpgradeItem)) return false;
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            if (upgrades.getStackInSlot(slot).isEmpty()
                    && upgrades.isItemValid(slot, stack)) {
                ItemStack inserted = stack.copy();
                inserted.setCount(1);
                upgrades.setStackInSlot(slot, inserted);
                return true;
            }
        }
        return false;
    }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            ItemStack stack = upgrades.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, stack.copy());
            }
        }
        upgrades.deserializeNBT(new CompoundTag());
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.put("Upgrades", upgrades.serializeNBT());
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Upgrades")) upgrades.deserializeNBT(tag.getCompound("Upgrades"));
        upgradesChanged();
        energy.setEnergyStored(tag.getInt("Energy"));
    }

    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyCapability.cast();
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
    }

    @Override public void reviveCaps() {
        super.reviveCaps();
        energyCapability = LazyOptional.of(() -> energy);
    }

    @Override public Component getDisplayName() {
        return Component.translatable("block.matteroverdrive.gravitational_stabilizer");
    }

    @Nullable
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GravitationalStabilizerMenu(id, inventory, this);
    }

    private static int low(int value) { return value & 0xFFFF; }
    private static int high(int value) { return (value >>> 16) & 0xFFFF; }
}
