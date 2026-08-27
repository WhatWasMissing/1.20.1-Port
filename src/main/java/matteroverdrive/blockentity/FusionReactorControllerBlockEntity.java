package matteroverdrive.blockentity;

import matteroverdrive.capability.IMatterStorage;
import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.capability.MachineMatterStorage;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.item.MachineUpgradeInventory;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.menu.FusionReactorMenu;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

/**
 * First functional 1.20.1 Fusion Reactor controller.  The legacy output/storage/drain
 * values are retained; the structure is intentionally compact while the original large
 * multiblock model is still pending its rendering port.
 */
public class FusionReactorControllerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int ENERGY_CAPACITY = 100_000_000;
    public static final int MATTER_CAPACITY = 2_048;
    public static final int BASE_OUTPUT = 2_048;
    public static final int IO_OUTPUT_PER_SIDE = 512;
    public static final int STRUCTURE_CHECK_DELAY = 40;
    public static final int BASE_ANOMALY_RANGE = 3;
    public static final int MAX_ANOMALY_SCAN_RANGE = 16;
    private static final int REQUIRED_COILS = 9;
    private static final int REQUIRED_HULLS = 16;
    private static final int REQUIRED_DECOMPOSERS = 2;
    private static final int REQUIRED_STABILIZERS = 4;
    private static final int STRUCTURE_SCAN_RADIUS = 4;
    private static final double BASE_MATTER_DRAIN = 1.0D / 80.0D;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(ENERGY_CAPACITY, 0, ENERGY_CAPACITY, this::setChanged);
    private final MachineMatterStorage matter = new MachineMatterStorage(MATTER_CAPACITY, true, false, this::setChanged);
    private final MachineUpgradeInventory upgrades = new MachineUpgradeInventory(4,
            upgrade -> upgrade == MachineUpgradeItem.Upgrade.SPEED
                    || upgrade == MachineUpgradeItem.Upgrade.RANGE
                    || upgrade == MachineUpgradeItem.Upgrade.POWER_STORAGE
                    || upgrade == MachineUpgradeItem.Upgrade.MATTER_STORAGE,
            this::upgradesChanged);
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private LazyOptional<IMatterStorage> matterCap = LazyOptional.of(() -> matter);

    private boolean structureValid;
    private int anomalyDistance = -1;
    private int generatedLastTick;
    private int tickCounter;
    private double matterDrainRemainder;
    private String fault = "Checking structure";

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> low(energy.getEnergyStored());
                case 1 -> high(energy.getEnergyStored());
                case 2 -> low(energy.getMaxEnergyStored());
                case 3 -> high(energy.getMaxEnergyStored());
                case 4 -> matter.getMatterStored();
                case 5 -> matter.getMatterCapacity();
                case 6 -> structureValid ? 1 : 0;
                case 7 -> anomalyDistance;
                case 8 -> generatedLastTick;
                case 9 -> (int) Math.round(efficiency() * 1000.0D);
                case 10 -> (int) Math.round(matterDrain() * 10_000.0D);
                case 11 -> faultCode();
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 12; }
    };

    public FusionReactorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_REACTOR_CONTROLLER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FusionReactorControllerBlockEntity reactor) {
        reactor.tickCounter++;
        if (reactor.tickCounter % STRUCTURE_CHECK_DELAY == 0) {
            reactor.validateStructure();
        }
        reactor.generate();
    }

    private void generate() {
        generatedLastTick = 0;
        if (!structureValid || energy.getEnergyStored() >= energy.getMaxEnergyStored()) {
            return;
        }
        double drain = matterDrain();
        if (matter.getMatterStored() <= 0) {
            fault = "No matter";
            return;
        }
        int output = Math.max(1, (int) Math.round(BASE_OUTPUT * efficiency() / upgrades.getMultiplier(MachineUpgradeItem.Upgrade::speed)));
        int accepted = Math.min(output, energy.getMaxEnergyStored() - energy.getEnergyStored());
        if (accepted <= 0) {
            return;
        }
        matterDrainRemainder += drain;
        int wholeMatter = (int) matterDrainRemainder;
        if (wholeMatter > 0) {
            if (matter.getMatterStored() < wholeMatter) {
                fault = "No matter";
                return;
            }
            matter.setMatterStored(matter.getMatterStored() - wholeMatter);
            matterDrainRemainder -= wholeMatter;
        }
        energy.setEnergyStored(energy.getEnergyStored() + accepted);
        generatedLastTick = accepted;
        fault = "Running";
        setChanged();
    }

    private void validateStructure() {
        structureValid = false;
        anomalyDistance = -1;
        if (level == null) {
            fault = "Checking structure";
            return;
        }

        int coils = 0;
        int hulls = 0;
        int decomposers = 0;
        int stabilizers = 0;
        boolean hasIo = false;
        Block coil = ModBlocks.get("fusion_reactor_coil").get();
        Block hull = ModBlocks.get("machine_hull").get();
        Block decomposer = ModBlocks.get("decomposer").get();
        Block stabilizer = ModBlocks.get("gravitational_stabilizer").get();
        Block io = ModBlocks.get("fusion_reactor_io").get();

        for (BlockPos candidate : BlockPos.betweenClosed(
                worldPosition.offset(-STRUCTURE_SCAN_RADIUS, -STRUCTURE_SCAN_RADIUS, -STRUCTURE_SCAN_RADIUS),
                worldPosition.offset(STRUCTURE_SCAN_RADIUS, STRUCTURE_SCAN_RADIUS, STRUCTURE_SCAN_RADIUS))) {
            if (candidate.equals(worldPosition)) {
                continue;
            }
            Block block = level.getBlockState(candidate).getBlock();
            if (block == coil) coils++;
            else if (block == hull) hulls++;
            else if (block == decomposer) decomposers++;
            else if (block == stabilizer) stabilizers++;
            else if (block == io) hasIo = true;
        }

        if (coils < REQUIRED_COILS) {
            fault = "Missing coils (" + coils + "/" + REQUIRED_COILS + ")";
            return;
        }
        if (hulls < REQUIRED_HULLS) {
            fault = "Missing hulls (" + hulls + "/" + REQUIRED_HULLS + ")";
            return;
        }
        if (decomposers < REQUIRED_DECOMPOSERS) {
            fault = "Missing decomposers (" + decomposers + "/" + REQUIRED_DECOMPOSERS + ")";
            return;
        }
        if (stabilizers < REQUIRED_STABILIZERS) {
            fault = "Missing stabilizers (" + stabilizers + "/" + REQUIRED_STABILIZERS + ")";
            return;
        }
        if (!hasIo) {
            fault = "Missing reactor IO";
            return;
        }

        int foundDistance = findAnomalyDistance();
        if (foundDistance < 0) {
            fault = "No anomaly in range";
            return;
        }

        structureValid = true;
        anomalyDistance = foundDistance;
        fault = "Ready";
    }

    private int findAnomalyDistance() {
        int range = anomalySearchRange();
        int closest = -1;
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos candidate = worldPosition.offset(x, y, z);
                    if (!level.getBlockState(candidate).is(ModBlocks.get("gravitational_anomaly").get())) {
                        continue;
                    }
                    int distance = (int) Math.ceil(Math.sqrt(worldPosition.distSqr(candidate)));
                    if (distance <= range && (closest < 0 || distance < closest)) {
                        closest = distance;
                    }
                }
            }
        }
        return closest;
    }

    private int anomalySearchRange() {
        return Math.min(MAX_ANOMALY_SCAN_RANGE, Math.max(1, (int) Math.round(BASE_ANOMALY_RANGE
                * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::range))));
    }

    private double efficiency() {
        if (anomalyDistance < 0) {
            return 0.0D;
        }
        int range = anomalySearchRange();
        return Math.max(0.25D, 1.0D - ((anomalyDistance - 1) / (double) Math.max(1, range + 1)));
    }

    private double matterDrain() {
        return BASE_MATTER_DRAIN * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::matterUsage);
    }

    private void upgradesChanged() {
        energy.setCapacity((int) Math.min(Integer.MAX_VALUE, Math.round(ENERGY_CAPACITY
                * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerStorage))));
        matter.setCapacity((int) Math.min(Integer.MAX_VALUE, Math.round(MATTER_CAPACITY
                * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::matterStorage))));
        if (level != null && !level.isClientSide) {
            validateStructure();
        }
        setChanged();
    }

    public void outputTo(Direction side, int limit) {
        if (level == null || limit <= 0) {
            return;
        }
        BlockEntity receiver = level.getBlockEntity(worldPosition.relative(side));
        if (receiver == null) {
            return;
        }
        receiver.getCapability(ForgeCapabilities.ENERGY, side.getOpposite()).ifPresent(storage -> {
            int offer = Math.min(limit, energy.extractEnergy(limit, true));
            if (offer > 0) {
                int accepted = storage.receiveEnergy(offer, false);
                if (accepted > 0) {
                    energy.extractEnergy(accepted, false);
                }
            }
        });
    }

    public boolean isIoAt(BlockPos position) {
        return worldPosition.above().equals(position);
    }

    public MachineEnergyStorage getEnergy() { return energy; }
    public MachineMatterStorage getMatter() { return matter; }
    public MachineUpgradeInventory getUpgrades() { return upgrades; }
    public ContainerData getData() { return data; }

    public void dropUpgrades() {
        if (level == null || level.isClientSide) return;
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            if (!upgrades.getStackInSlot(slot).isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + .5D, worldPosition.getY() + .5D, worldPosition.getZ() + .5D, upgrades.getStackInSlot(slot).copy());
            }
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Matter", matter.getMatterStored());
        tag.put("Upgrades", upgrades.serializeNBT());
        tag.putDouble("MatterRemainder", matterDrainRemainder);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Upgrades")) upgrades.deserializeNBT(tag.getCompound("Upgrades"));
        upgradesChanged();
        energy.setEnergyStored(tag.getInt("Energy"));
        matter.setMatterStored(tag.getInt("Matter"));
        matterDrainRemainder = tag.getDouble("MatterRemainder");
    }

    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) return energyCap.cast();
        if (capability == ModCapabilities.MATTER) return matterCap.cast();
        return super.getCapability(capability, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); energyCap.invalidate(); matterCap.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); energyCap = LazyOptional.of(() -> energy); matterCap = LazyOptional.of(() -> matter); }
    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.fusion_reactor_controller"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new FusionReactorMenu(id, inventory, this); }

    private int faultCode() {
        return switch (fault) {
            case "Running" -> 1; case "Ready" -> 2; case "Missing coil" -> 3; case "Missing reactor IO" -> 4;
            case "No anomaly in range" -> 5; case "No matter" -> 6; default -> fault.startsWith("Missing") ? 3 : 0;
        };
    }
    private static int low(int value) { return value & 0xFFFF; }
    private static int high(int value) { return (value >>> 16) & 0xFFFF; }
}
