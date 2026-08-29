package matteroverdrive.blockentity;

import matteroverdrive.block.FusionReactorControllerBlock;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.Connection;
import net.minecraftforge.fml.DistExecutor;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class FusionReactorControllerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int ENERGY_CAPACITY = 100_000_000;
    public static final int MATTER_CAPACITY = 2_048;
    public static final int BASE_OUTPUT = 2_048;
    public static final int IO_OUTPUT_PER_SIDE = 512;
    public static final int STRUCTURE_CHECK_DELAY = 40;
    public static final int MAX_GRAVITATIONAL_ANOMALY_DISTANCE = 3;
    private static final double BASE_MATTER_DRAIN = 1.0D / 80.0D;

    private static final int ANOMALY_SLOT = 255;
    private static final int HULL_SLOT = 0;
    private static final int COIL_SLOT = 1;
    private static final int CONTROLLER_SIDE_SLOT = 2;

    private static final int[] LATERAL_OFFSETS = {
            0, 1, 2, 3, 4, 5, 5, 5, 5, 5, 4, 3, 2, 1,
            0, -1, -2, -3, -4, -5, -5, -5, -5, -5, -4, -3, -2, -1
    };
    private static final int[] FORWARD_OFFSETS = {
            5, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10,
            10, 10, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 0
    };
    private static final int[] SLOT_TYPES = {
            ANOMALY_SLOT, CONTROLLER_SIDE_SLOT,
            HULL_SLOT, HULL_SLOT, HULL_SLOT, HULL_SLOT,
            COIL_SLOT, COIL_SLOT, COIL_SLOT,
            HULL_SLOT, HULL_SLOT, HULL_SLOT, HULL_SLOT,
            COIL_SLOT, COIL_SLOT, COIL_SLOT,
            HULL_SLOT, HULL_SLOT, HULL_SLOT, HULL_SLOT,
            COIL_SLOT, COIL_SLOT, COIL_SLOT,
            HULL_SLOT, HULL_SLOT, HULL_SLOT, HULL_SLOT,
            CONTROLLER_SIDE_SLOT
    };

    private final MachineEnergyStorage energy = new MachineEnergyStorage(
            ENERGY_CAPACITY, 0, ENERGY_CAPACITY, this::setChanged);
    private final MachineMatterStorage matter = new MachineMatterStorage(
            MATTER_CAPACITY, true, true, this::setChanged);
    private final MachineUpgradeInventory upgrades = new MachineUpgradeInventory(4,
            upgrade -> upgrade == MachineUpgradeItem.Upgrade.SPEED
                    || upgrade == MachineUpgradeItem.Upgrade.RANGE
                    || upgrade == MachineUpgradeItem.Upgrade.POWER_STORAGE
                    || upgrade == MachineUpgradeItem.Upgrade.MATTER_STORAGE,
            this::upgradesChanged);
    private final Set<BlockPos> ioPositions = new HashSet<>();

    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private LazyOptional<IMatterStorage> matterCap = LazyOptional.of(() -> matter);

    private boolean structureValid;
    private boolean overlayEnabled;
    private BlockPos anomalyPosition;
    private int anomalyDistance = -1;
    private int generatedLastTick;
    private int connectedUsage;
    private int tickCounter;
    private double matterDrainRemainder;
    private String fault = "Checking structure";

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            GravitationalAnomalyBlockEntity anomaly = getAnomaly();
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
                case 9 -> (int) Math.round(efficiency() * 1_000.0D);
                case 10 -> (int) Math.round(matterDrain() * 10_000.0D);
                case 11 -> faultCode();
                case 12 -> scaledMass(anomaly == null ? 0.0D : anomaly.getRealMassUnsuppressed());
                case 13 -> scaledMass(anomaly == null ? 0.0D : anomaly.getRealMass());
                case 14 -> ioPositions.size();
                case 15 -> getForward().get2DDataValue();
                case 16 -> anomaly == null ? 0 : anomaly.getActiveSuppressorCount();
                case 17 -> anomaly == null ? 0 : anomaly.getAffectedEntityCount();
                case 18 -> anomaly == null ? 0 : scaledDistance(
                        Math.min(32.0D, anomaly.getMaxRange()));
                case 19 -> anomaly == null ? 0 : scaledDistance(anomaly.getBlockBreakRange());
                case 20 -> anomaly == null ? 0 : scaledDistance(anomaly.getEventHorizon());
                case 21 -> connectedUsage;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 22;
        }
    };

    public FusionReactorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_REACTOR_CONTROLLER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  FusionReactorControllerBlockEntity reactor) {
        reactor.tickCounter++;
        if (reactor.tickCounter == 1 || reactor.tickCounter % STRUCTURE_CHECK_DELAY == 0) {
            reactor.validateStructure();
        }
        reactor.generate();
        reactor.refreshConnectedUsage();
    }

    private void generate() {
        generatedLastTick = 0;
        if (!structureValid || energy.getEnergyStored() >= energy.getMaxEnergyStored()) {
            return;
        }

        GravitationalAnomalyBlockEntity anomaly = getAnomaly();
        if (anomaly == null) {
            structureValid = false;
            fault = "Anomaly data unavailable";
            invalidateStructureLinks();
            return;
        }
        if (matter.getMatterStored() <= 0) {
            fault = "No matter";
            return;
        }

        double unsuppressedMass = anomaly.getRealMassUnsuppressed();
        double rawOutput = BASE_OUTPUT * efficiency() * unsuppressedMass;
        int requested = (int) Math.min(Integer.MAX_VALUE,
                Math.max(1L, Math.round(rawOutput)));
        int accepted = Math.min(requested,
                energy.getMaxEnergyStored() - energy.getEnergyStored());
        if (accepted <= 0) {
            return;
        }

        double proportionalDrain = BASE_MATTER_DRAIN * unsuppressedMass
                * (accepted / (double) requested);
        double pendingDrain = matterDrainRemainder + proportionalDrain;
        int wholeMatter = (int) Math.floor(pendingDrain);
        if (wholeMatter > matter.getMatterStored()) {
            fault = "No matter";
            return;
        }

        matterDrainRemainder = pendingDrain - wholeMatter;
        if (wholeMatter > 0) {
            matter.setMatterStored(matter.getMatterStored() - wholeMatter);
        }
        energy.setEnergyStored(energy.getEnergyStored() + accepted);
        generatedLastTick = accepted;
        fault = "Running";
        setChanged();
    }

    private void validateStructure() {
        invalidateStructureLinks();
        structureValid = false;
        anomalyPosition = null;
        anomalyDistance = -1;
        if (level == null) {
            fault = "Checking structure";
            return;
        }

        Block coil = ModBlocks.get("fusion_reactor_coil").get();
        Block hull = ModBlocks.get("machine_hull").get();
        Block decomposer = ModBlocks.get("decomposer").get();
        Block io = ModBlocks.get("fusion_reactor_io").get();
        Set<BlockPos> foundIoPositions = new HashSet<>();

        for (int index = 1; index < SLOT_TYPES.length; index++) {
            BlockPos structurePos = structurePosition(
                    LATERAL_OFFSETS[index], FORWARD_OFFSETS[index]);
            if (!level.hasChunkAt(structurePos)) {
                fault = "Structure area unloaded";
                return;
            }

            Block block = level.getBlockState(structurePos).getBlock();
            int slotType = SLOT_TYPES[index];
            if (slotType == HULL_SLOT) {
                if (block != hull) {
                    fault = "Incorrect hull position";
                    return;
                }
            } else if (slotType == COIL_SLOT) {
                if (block != coil && block != io) {
                    fault = "Incorrect coil position";
                    return;
                }
            } else if (slotType == CONTROLLER_SIDE_SLOT) {
                if (block != hull && block != coil && block != decomposer && block != io) {
                    fault = "Incorrect controller-side position";
                    return;
                }
            }

            if (block == io) {
                foundIoPositions.add(structurePos.immutable());
            }
        }

        BlockPos anomalyCentre = structurePosition(0, 5);
        int closestDistance = Integer.MAX_VALUE;
        BlockPos closestAnomaly = null;
        for (int verticalOffset = -MAX_GRAVITATIONAL_ANOMALY_DISTANCE;
             verticalOffset <= MAX_GRAVITATIONAL_ANOMALY_DISTANCE; verticalOffset++) {
            BlockPos candidate = anomalyCentre.above(verticalOffset);
            if (anomalyAt(candidate) != null
                    && Math.abs(verticalOffset) < closestDistance) {
                closestDistance = Math.abs(verticalOffset);
                closestAnomaly = candidate.immutable();
            }
        }
        if (closestAnomaly == null) {
            fault = "No anomaly at ring centre";
            return;
        }

        anomalyPosition = closestAnomaly;
        anomalyDistance = closestDistance;
        structureValid = true;
        ioPositions.addAll(foundIoPositions);
        for (BlockPos ioPosition : ioPositions) {
            if (level.getBlockEntity(ioPosition) instanceof FusionReactorIOBlockEntity ioBlockEntity) {
                ioBlockEntity.linkController(worldPosition);
            }
        }
        fault = matter.getMatterStored() > 0 ? "Ready" : "No matter";
        setChanged();
    }

    @Nullable
    private GravitationalAnomalyBlockEntity anomalyAt(BlockPos position) {
        if (level == null
                || !level.getBlockState(position).is(ModBlocks.get("gravitational_anomaly").get())) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(position);
        if (blockEntity instanceof GravitationalAnomalyBlockEntity anomaly) {
            return anomaly;
        }
        if (blockEntity == null && !level.isClientSide) {
            GravitationalAnomalyBlockEntity migrated = new GravitationalAnomalyBlockEntity(
                    position, level.getBlockState(position));
            level.setBlockEntity(migrated);
            return migrated;
        }
        return null;
    }

    private BlockPos structurePosition(int lateralOffset, int forwardOffset) {
        Direction forward = getForward();
        Direction right = forward.getClockWise();
        return worldPosition.relative(forward, forwardOffset).relative(right, lateralOffset);
    }

    private Direction getForward() {
        BlockState state = getBlockState();
        if (state.hasProperty(FusionReactorControllerBlock.FACING)) {
            return state.getValue(FusionReactorControllerBlock.FACING);
        }
        return Direction.NORTH;
    }

    @Nullable
    private GravitationalAnomalyBlockEntity getAnomaly() {
        if (level == null || anomalyPosition == null) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(anomalyPosition);
        return blockEntity instanceof GravitationalAnomalyBlockEntity anomaly ? anomaly : null;
    }

    private double efficiency() {
        if (anomalyDistance < 0) {
            return 0.0D;
        }
        return Math.max(0.0D, Math.min(1.0D,
                1.0D - anomalyDistance / (double) (MAX_GRAVITATIONAL_ANOMALY_DISTANCE + 1)));
    }

    private double matterDrain() {
        GravitationalAnomalyBlockEntity anomaly = getAnomaly();
        return anomaly == null ? 0.0D : BASE_MATTER_DRAIN * anomaly.getRealMassUnsuppressed();
    }

    private void upgradesChanged() {
        energy.setCapacity((int) Math.min(Integer.MAX_VALUE, Math.round(ENERGY_CAPACITY
                * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::powerStorage))));
        matter.setCapacity((int) Math.min(Integer.MAX_VALUE, Math.round(MATTER_CAPACITY
                * upgrades.getMultiplier(MachineUpgradeItem.Upgrade::matterStorage))));
        setChanged();
    }

    private void refreshConnectedUsage() {
        if (level == null || level.isClientSide || ioPositions.isEmpty()) {
            connectedUsage = 0;
            return;
        }

        long usage = 0;
        Deque<BlockPos> pending = new ArrayDeque<>();
        Set<BlockPos> visitedPipes = new HashSet<>();
        Set<BlockPos> visitedReceivers = new HashSet<>();
        for (BlockPos ioPosition : ioPositions) {
            for (Direction direction : Direction.values()) {
                BlockPos adjacent = ioPosition.relative(direction);
                if (level.getBlockEntity(adjacent) instanceof EnergyPipeBlockEntity
                        && visitedPipes.add(adjacent.immutable())) {
                    pending.addLast(adjacent.immutable());
                } else if (!adjacent.equals(worldPosition)) {
                    usage += receiverUsage(adjacent, direction, visitedReceivers);
                }
            }
        }

        while (!pending.isEmpty()) {
            BlockPos pipePosition = pending.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos adjacent = pipePosition.relative(direction);
                if (level.getBlockEntity(adjacent) instanceof EnergyPipeBlockEntity) {
                    if (visitedPipes.add(adjacent.immutable())) {
                        pending.addLast(adjacent.immutable());
                    }
                } else if (!adjacent.equals(worldPosition)
                        && !ioPositions.contains(adjacent)) {
                    usage += receiverUsage(adjacent, direction, visitedReceivers);
                }
            }
        }
        connectedUsage = (int) Math.min(Integer.MAX_VALUE, usage);
    }

    private int receiverUsage(BlockPos position, Direction pipeSide,
                               Set<BlockPos> visitedReceivers) {
        if (!visitedReceivers.add(position.immutable())) {
            return 0;
        }
        BlockEntity receiver = level.getBlockEntity(position);
        if (receiver == null) {
            return 0;
        }
        IEnergyStorage storage = receiver.getCapability(
                ForgeCapabilities.ENERGY, pipeSide.getOpposite()).orElse(null);
        if (!(storage instanceof MachineEnergyStorage machineStorage)
                || !storage.canReceive()) {
            return 0;
        }
        return machineStorage.getRecentEnergyUsage(level.getGameTime());
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
        return structureValid && ioPositions.contains(position);
    }

    public void invalidateStructureLinks() {
        if (level != null) {
            for (BlockPos ioPosition : ioPositions) {
                if (level.getBlockEntity(ioPosition) instanceof FusionReactorIOBlockEntity ioBlockEntity) {
                    ioBlockEntity.unlinkController(worldPosition);
                }
            }
        }
        ioPositions.clear();
    }

    public MachineEnergyStorage getEnergy() {
        return energy;
    }

    public MachineMatterStorage getMatter() {
        return matter;
    }

    public MachineUpgradeInventory getUpgrades() {
        return upgrades;
    }

    public ContainerData getData() {
        return data;
    }

    public boolean isOverlayEnabled() {
        return overlayEnabled;
    }

    public boolean toggleOverlay() {
        overlayEnabled = !overlayEnabled;
        setChanged();
        return overlayEnabled;
    }

    public void dropUpgrades() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            if (!upgrades.getStackInSlot(slot).isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D,
                        upgrades.getStackInSlot(slot).copy());
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putBoolean("InfiniteEnergy", energy.isInfiniteEnergy());
        tag.putInt("Matter", matter.getMatterStored());
        tag.put("Upgrades", upgrades.serializeNBT());
        tag.putDouble("MatterRemainder", matterDrainRemainder);
        tag.putBoolean("OverlayEnabled", overlayEnabled);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Upgrades")) {
            upgrades.deserializeNBT(tag.getCompound("Upgrades"));
        }
        upgradesChanged();
        energy.setEnergyStored(tag.getInt("Energy"));
        energy.setInfiniteEnergy(tag.getBoolean("InfiniteEnergy"));
        matter.setMatterStored(tag.getInt("Matter"));
        matterDrainRemainder = tag.getDouble("MatterRemainder");
        overlayEnabled = tag.getBoolean("OverlayEnabled");
        updateClientOverlayCache();
    }

    private void updateClientOverlayCache() {
        if (level != null && level.isClientSide && overlayEnabled) {
            DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                    () -> () -> matteroverdrive.client.FusionReactorGuideOverlay.rememberController(worldPosition));
        }
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        super.onDataPacket(connection, packet);
        updateClientOverlayCache();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) {
            return energyCap.cast();
        }
        if (capability == ModCapabilities.MATTER) {
            return matterCap.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
        matterCap.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyCap = LazyOptional.of(() -> energy);
        matterCap = LazyOptional.of(() -> matter);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.matteroverdrive.fusion_reactor_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FusionReactorMenu(id, inventory, this);
    }

    private int faultCode() {
        return switch (fault) {
            case "Running" -> 1;
            case "Ready" -> 2;
            case "Incorrect coil position" -> 3;
            case "Incorrect hull position" -> 4;
            case "No anomaly at ring centre" -> 5;
            case "No matter" -> 6;
            case "Incorrect controller-side position" -> 7;
            case "Structure area unloaded" -> 8;
            case "Anomaly data unavailable" -> 9;
            default -> 0;
        };
    }

    private static int scaledMass(double mass) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, Math.round(mass * 1_000.0D)));
    }

    private static int scaledDistance(double distance) {
        return (int) Math.min(Integer.MAX_VALUE,
                Math.max(0L, Math.round(distance * 100.0D)));
    }

    private static int low(int value) {
        return value & 0xFFFF;
    }

    private static int high(int value) {
        return (value >>> 16) & 0xFFFF;
    }
}
