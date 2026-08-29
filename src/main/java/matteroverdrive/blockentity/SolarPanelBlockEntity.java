package matteroverdrive.blockentity;

import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.item.MachineUpgradeInventory;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.menu.SolarPanelMenu;
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
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class SolarPanelBlockEntity extends BlockEntity implements MenuProvider {
    public static final int UPGRADE_SLOT_COUNT = 2;
    public static final int PEAK_GENERATION = 8;
    public static final int ENERGY_CAPACITY = 64000;
    public static final int MAX_OUTPUT_PER_SIDE = 512;

    private final MachineEnergyStorage energyStorage =
            new MachineEnergyStorage(ENERGY_CAPACITY, 0, MAX_OUTPUT_PER_SIDE, this::setChanged);
    private final MachineUpgradeInventory upgrades = new MachineUpgradeInventory(
            UPGRADE_SLOT_COUNT,
            upgrade -> upgrade == MachineUpgradeItem.Upgrade.POWER_STORAGE,
            this::onUpgradesChanged
    );
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energyStorage);

    private int currentGeneration;
    private int lastOutput;
    private boolean dimensionHasSky;
    private boolean canSeeSky;
    private int effectiveSkyLight;
    private int daylightMilli;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> lowWord(energyStorage.getEnergyStored());
                case 1 -> highWord(energyStorage.getEnergyStored());
                case 2 -> lowWord(energyStorage.getMaxEnergyStored());
                case 3 -> highWord(energyStorage.getMaxEnergyStored());
                case 4 -> currentGeneration;
                case 5 -> lastOutput;
                case 6 -> dimensionHasSky ? 1 : 0;
                case 7 -> canSeeSky ? 1 : 0;
                case 8 -> effectiveSkyLight;
                case 9 -> daylightMilli;
                case 10 -> MAX_OUTPUT_PER_SIDE;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 11;
        }
    };

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_PANEL.get(), pos, state);
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, SolarPanelBlockEntity panel) {
        panel.lastOutput = panel.pushEnergy();
        panel.updateEnvironment();
        panel.currentGeneration = panel.generateEnergy();
    }

    private void updateEnvironment() {
        if (level == null) {
            dimensionHasSky = false;
            canSeeSky = false;
            effectiveSkyLight = 0;
            daylightMilli = 0;
            return;
        }

        BlockPos skyPos = worldPosition.above();
        dimensionHasSky = level.dimensionType().hasSkyLight();
        canSeeSky = dimensionHasSky && level.canSeeSky(skyPos);
        effectiveSkyLight = dimensionHasSky
                ? Math.max(0, level.getBrightness(LightLayer.SKY, skyPos) - level.getSkyDarken())
                : 0;
        daylightMilli = Math.max(-1000, Math.min(1000, Math.round(getLegacyDaylightFactor() * 1000.0F)));
    }

    private float getLegacyDaylightFactor() {
        if (level == null) {
            return 0.0F;
        }
        float angle = level.getSunAngle(1.0F);
        if (angle < (float) Math.PI) {
            angle += (0.0F - angle) * 0.2F;
        } else {
            angle += (((float) Math.PI * 2.0F) - angle) * 0.2F;
        }
        return (float) Math.cos(angle);
    }

    private int generateEnergy() {
        if (!dimensionHasSky || !canSeeSky || effectiveSkyLight < 15 || daylightMilli <= 500) {
            return 0;
        }

        int requested = Math.max(0, Math.round(PEAK_GENERATION * daylightMilli / 1000.0F));
        int generated = Math.min(requested,
                energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
        if (generated > 0) {
            energyStorage.setEnergyStored(energyStorage.getEnergyStored() + generated);
        }
        return generated;
    }

    private int pushEnergy() {
        if (level == null || energyStorage.getEnergyStored() <= 0) {
            return 0;
        }

        int sent = 0;
        for (Direction direction : Direction.values()) {
            if (energyStorage.getEnergyStored() <= 0) {
                break;
            }
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null) {
                continue;
            }
            IEnergyStorage receiver = neighbor
                    .getCapability(ForgeCapabilities.ENERGY, direction.getOpposite())
                    .orElse(null);
            if (receiver == null || !receiver.canReceive()) {
                continue;
            }

            int offered = Math.min(MAX_OUTPUT_PER_SIDE, energyStorage.getEnergyStored());
            int accepted = receiver.receiveEnergy(offered, true);
            int extracted = energyStorage.extractEnergy(accepted, false);
            if (extracted > 0) {
                int received = receiver.receiveEnergy(extracted, false);
                sent += received;
                if (received < extracted) {
                    energyStorage.setEnergyStored(
                            energyStorage.getEnergyStored() + extracted - received);
                }
            }
        }
        return sent;
    }

    private void onUpgradesChanged() {
        energyStorage.setCapacity((int) Math.min(Integer.MAX_VALUE,
                Math.round(ENERGY_CAPACITY
                        * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerStorage))));
        setChanged();
    }

    public MachineEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public MachineUpgradeInventory getUpgradeInventory() {
        return upgrades;
    }

    public ContainerData getContainerData() {
        return data;
    }

    public void dropUpgrades() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            ItemStack stack = upgrades.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(
                        level,
                        worldPosition.getX() + 0.5,
                        worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5,
                        stack.copy());
                upgrades.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energyStorage.getEnergyStored());
        tag.putBoolean("InfiniteEnergy", energyStorage.isInfiniteEnergy());
        tag.put("Upgrades", upgrades.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Upgrades")) {
            upgrades.deserializeNBT(tag.getCompound("Upgrades"));
        }
        onUpgradesChanged();
        energyStorage.setEnergyStored(tag.getInt("Energy"));
        energyStorage.setInfiniteEnergy(tag.getBoolean("InfiniteEnergy"));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyCapability = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.matteroverdrive.solar_panel");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new SolarPanelMenu(containerId, playerInventory, this);
    }

    private static int lowWord(int value) {
        return value & 0xFFFF;
    }

    private static int highWord(int value) {
        return (value >>> 16) & 0xFFFF;
    }
}
