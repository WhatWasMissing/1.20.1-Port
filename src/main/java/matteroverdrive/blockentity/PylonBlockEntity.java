package matteroverdrive.blockentity;

import matteroverdrive.capability.IMatterStorage;
import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.capability.MachineMatterStorage;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.menu.PylonMenu;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class PylonBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MATTER_CAPACITY = 2048;
    public static final int MATTER_RECEIVE = 128;
    public static final int ENERGY_CAPACITY = 1_000_000;
    public static final int ENERGY_OUTPUT = 2048;
    public static final int MAX_CHARGE = 2048;
    public static final int CHARGE_DECREASE_ON_HIT = 16;
    public static final int CHARGE_INCREASE_RATE = 64;
    public static final int CHARGE_ENERGY_INCREASE = 128;
    public static final int CHARGE_MATTER_INCREASE = 5;
    public static final int MAX_POWER_GEN_PER_TICK = 256;

    private static final int MAX_LINK_DISTANCE = 64;
    private static final Map<Level, Set<BlockPos>> LOADED = new WeakHashMap<>();

    private final MachineEnergyStorage energyStorage =
            new MachineEnergyStorage(ENERGY_CAPACITY, 0, ENERGY_OUTPUT, this::setChanged);
    private final MachineMatterStorage matterStorage =
            new MachineMatterStorage(MATTER_CAPACITY, true, false, this::setChanged);
    private final IMatterStorage matterInput = new IMatterStorage() {
        @Override public int receiveMatter(int maxReceive, boolean simulate) {
            return matterStorage.receiveMatter(Math.min(MATTER_RECEIVE, maxReceive), simulate);
        }
        @Override public int extractMatter(int maxExtract, boolean simulate) { return 0; }
        @Override public int getMatterStored() { return matterStorage.getMatterStored(); }
        @Override public int getMatterCapacity() { return matterStorage.getMatterCapacity(); }
        @Override public boolean canReceive() { return true; }
        @Override public boolean canExtract() { return false; }
    };
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energyStorage);
    private LazyOptional<IMatterStorage> matterCapability = LazyOptional.of(() -> matterInput);

    private int channel;
    private boolean formed;
    @Nullable private BlockPos mainBlock;
    private int charge;
    private int generatedLastTick;
    private int matterDrainPerSecond;
    private int validationTimer;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            PylonBlockEntity root = getRoot();
            return switch (index) {
                case 0 -> low(root.energyStorage.getEnergyStored());
                case 1 -> high(root.energyStorage.getEnergyStored());
                case 2 -> low(root.energyStorage.getMaxEnergyStored());
                case 3 -> high(root.energyStorage.getMaxEnergyStored());
                case 4 -> root.matterStorage.getMatterStored();
                case 5 -> root.matterStorage.getMatterCapacity();
                case 6 -> root.generatedLastTick;
                case 7 -> root.matterDrainPerSecond;
                case 8 -> root.charge;
                case 9 -> MAX_CHARGE;
                case 10 -> Math.round(root.getDimensionalValue() * 10_000.0F);
                case 11 -> root.formed ? 1 : 0;
                case 12 -> root.channel;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 13; }
    };

    public PylonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PYLON.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PylonBlockEntity pylon) {
        if (!pylon.formed || !pylon.isMainStructureBlock()) return;
        pylon.energyStorage.beginUsageTick(level.getGameTime());
        if (++pylon.validationTimer >= 20) {
            pylon.validationTimer = 0;
            if (!pylon.validateStructure()) {
                pylon.invalidateStructure();
                return;
            }
        }
        pylon.manageCharge();
        pylon.managePowerGeneration();
        pylon.pushEnergy();
        if (level.getGameTime() % 80L == 0L && pylon.generatedLastTick > 0) {
            float volume = 0.05F + 0.25F * pylon.getDimensionalValue();
            level.playSound(null, pos, ModSounds.get("blocks.pylon").get(), SoundSource.BLOCKS, volume, 1.0F);
        }
    }

    private void managePowerGeneration() {
        generatedLastTick = 0;
        float dimensionalValue = getDimensionalValue();
        float chargeRatio = charge / (float) MAX_CHARGE;
        int energyGenPerTick = (int) (dimensionalValue * MAX_POWER_GEN_PER_TICK)
                + (int) (CHARGE_ENERGY_INCREASE * chargeRatio);
        matterDrainPerSecond = Mth.ceil(dimensionalValue * 20.0F)
                + (int) (CHARGE_MATTER_INCREASE * chargeRatio);
        if (energyGenPerTick <= 0 || energyStorage.getEnergyStored() >= energyStorage.getMaxEnergyStored()) return;
        if (matterDrainPerSecond > 0 && matterStorage.getMatterStored() < matterDrainPerSecond) return;
        int before = energyStorage.getEnergyStored();
        energyStorage.setEnergyStored(Math.min(energyStorage.getMaxEnergyStored(), before + energyGenPerTick));
        generatedLastTick = energyStorage.getEnergyStored() - before;
        if (generatedLastTick > 0 && matterDrainPerSecond > 0 && level != null && level.getGameTime() % 20L == 0L) {
            matterStorage.setMatterStored(matterStorage.getMatterStored() - matterDrainPerSecond);
        }
    }

    private void manageCharge() {
        if (level == null) return;
        if (charge > 0 && level.getGameTime() % 40L == 0L) {
            charge = Math.max(0, charge - (1 + Math.round(charge * 0.005F)));
            setChanged();
        }
    }

    private void pushEnergy() {
        if (level == null || energyStorage.getEnergyStored() <= 0) return;
        int remaining = Math.min(ENERGY_OUTPUT, energyStorage.getEnergyStored());
        for (BlockPos member : structureMembers()) {
            for (Direction direction : Direction.values()) {
                if (remaining <= 0) return;
                BlockPos outside = member.relative(direction);
                if (isStructureMember(outside)) continue;
                BlockEntity neighbor = level.getBlockEntity(outside);
                if (neighbor == null) continue;
                IEnergyStorage target = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
                if (target == null || !target.canReceive()) continue;
                int accepted = target.receiveEnergy(remaining, false);
                if (accepted > 0) {
                    energyStorage.extractEnergy(accepted, false);
                    remaining -= accepted;
                }
            }
        }
    }

    public float getDimensionalValue() {
        PylonBlockEntity root = getRoot();
        if (root != this) return root.getDimensionalValue();
        if (!(level instanceof ServerLevel serverLevel)) return 0.0F;
        BlockPos sample = mainBlock == null ? worldPosition : mainBlock;
        long seed = serverLevel.getSeed();
        double seedX = ((seed >>> 16) & 0xffffL) * 0.00031D;
        double seedZ = ((seed >>> 32) & 0xffffL) * 0.00037D;
        double x = sample.getX() * 0.018D + seedX;
        double z = sample.getZ() * 0.018D + seedZ;
        double raw = 0.50D
                + Math.sin(x) * 0.22D
                + Math.cos(z * 0.93D) * 0.18D
                + Math.sin((x + z) * 0.61D) * 0.12D;
        raw = Mth.clamp(raw, 0.0D, 1.0D);
        double value = Math.pow(Math.max(0.0D, raw - 0.45D), 5.0D) * 180.0D;
        return (float) Mth.clamp(value, 0.0D, 1.0D);
    }

    public boolean tryFormStructure() {
        if (level == null || level.isClientSide || formed) return false;
        for (int ox = -1; ox <= 0; ox++) {
            for (int oy = -2; oy <= 0; oy++) {
                for (int oz = -1; oz <= 0; oz++) {
                    BlockPos min = worldPosition.offset(ox, oy, oz);
                    if (canFormAt(min)) {
                        formAt(min);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean canFormAt(BlockPos min) {
        if (level == null) return false;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 2; z++) {
                    BlockPos pos = min.offset(x, y, z);
                    if (!level.getBlockState(pos).is(ModBlocks.get("pylon").get())) return false;
                    if (!(level.getBlockEntity(pos) instanceof PylonBlockEntity pylon) || pylon.formed) return false;
                }
            }
        }
        return true;
    }

    private void formAt(BlockPos min) {
        if (level == null) return;
        BlockPos main = min.offset(1, 0, 1).immutable();
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 2; z++) {
                    BlockPos pos = min.offset(x, y, z);
                    if (level.getBlockEntity(pos) instanceof PylonBlockEntity pylon) {
                        pylon.formed = true;
                        pylon.mainBlock = main;
                        pylon.removeFromRelayRegistry();
                        pylon.setChanged();
                    }
                }
            }
        }
    }

    public void invalidateStructure() {
        PylonBlockEntity root = getRoot();
        if (root != this) {
            root.invalidateStructure();
            return;
        }
        if (!formed || level == null) return;
        BlockPos expectedMain = mainBlock == null ? worldPosition : mainBlock;
        for (BlockPos member : structureMembers()) {
            if (level.getBlockEntity(member) instanceof PylonBlockEntity pylon
                    && pylon.formed && expectedMain.equals(pylon.mainBlock)) {
                pylon.formed = false;
                pylon.mainBlock = null;
                pylon.generatedLastTick = 0;
                pylon.matterDrainPerSecond = 0;
                pylon.addToRelayRegistry();
                pylon.setChanged();
            }
        }
    }

    private boolean validateStructure() {
        if (!formed || level == null || mainBlock == null) return false;
        for (BlockPos pos : structureMembers()) {
            if (!level.getBlockState(pos).is(ModBlocks.get("pylon").get())) return false;
            if (!(level.getBlockEntity(pos) instanceof PylonBlockEntity pylon)
                    || !pylon.formed || !mainBlock.equals(pylon.mainBlock)) return false;
        }
        return true;
    }

    private List<BlockPos> structureMembers() {
        PylonBlockEntity root = getRoot();
        BlockPos main = root.mainBlock == null ? root.worldPosition : root.mainBlock;
        BlockPos min = main.offset(-1, 0, -1);
        List<BlockPos> result = new ArrayList<>(12);
        for (int x = 0; x < 2; x++) for (int y = 0; y < 3; y++) for (int z = 0; z < 2; z++)
            result.add(min.offset(x, y, z).immutable());
        return result;
    }

    private boolean isStructureMember(BlockPos pos) {
        if (!formed || mainBlock == null) return false;
        BlockPos min = mainBlock.offset(-1, 0, -1);
        return pos.getX() >= min.getX() && pos.getX() <= min.getX() + 1
                && pos.getY() >= min.getY() && pos.getY() <= min.getY() + 2
                && pos.getZ() >= min.getZ() && pos.getZ() <= min.getZ() + 1;
    }

    public PylonBlockEntity getRoot() {
        if (!formed || mainBlock == null || mainBlock.equals(worldPosition) || level == null) return this;
        BlockEntity blockEntity = level.getBlockEntity(mainBlock);
        return blockEntity instanceof PylonBlockEntity pylon ? pylon : this;
    }

    public boolean isFormed() { return formed; }
    public boolean isMainStructureBlock() { return formed && mainBlock != null && mainBlock.equals(worldPosition); }
    public boolean isRelayMode() { return !formed; }
    @Nullable public BlockPos getMainBlock() { return mainBlock; }

    public int getChannel() { return channel; }
    public void nextChannel() {
        if (formed) return;
        channel = (channel + 1) & 15;
        setChanged();
    }

    public int getCharge() { return getRoot().charge; }
    public int addCharge(int amount) {
        PylonBlockEntity root = getRoot();
        int accepted = Math.min(Math.max(0, amount), MAX_CHARGE - root.charge);
        if (accepted > 0) { root.charge += accepted; root.setChanged(); }
        return accepted;
    }
    public int removeCharge(int amount) {
        PylonBlockEntity root = getRoot();
        int removed = Math.min(Math.max(0, amount), root.charge);
        if (removed > 0) { root.charge -= removed; root.setChanged(); }
        return removed;
    }

    public MachineEnergyStorage getEnergyStorage() { return getRoot().energyStorage; }
    public MachineMatterStorage getMatterStorage() { return getRoot().matterStorage; }
    public ContainerData getContainerData() { return data; }
    public int getGeneratedLastTick() { return getRoot().generatedLastTick; }
    public int getMatterDrainPerSecond() { return getRoot().matterDrainPerSecond; }

    @Override public void onLoad() {
        super.onLoad();
        if (!formed) addToRelayRegistry();
    }

    private void addToRelayRegistry() {
        if (level != null && !level.isClientSide && !formed) {
            LOADED.computeIfAbsent(level, ignored -> new HashSet<>()).add(worldPosition.immutable());
        }
    }

    private void removeFromRelayRegistry() {
        if (level == null) return;
        Set<BlockPos> pylons = LOADED.get(level);
        if (pylons != null) {
            pylons.remove(worldPosition);
            if (pylons.isEmpty()) LOADED.remove(level);
        }
    }

    @Override public void setRemoved() {
        removeFromRelayRegistry();
        super.setRemoved();
    }

    public static List<BlockPos> linked(Level level, BlockPos origin, int channel) {
        List<BlockPos> result = new ArrayList<>();
        if (!(level.getBlockEntity(origin) instanceof PylonBlockEntity source) || !source.isRelayMode()) return result;
        for (BlockPos candidate : LOADED.getOrDefault(level, Set.of())) {
            if (!candidate.equals(origin)
                    && candidate.distSqr(origin) <= MAX_LINK_DISTANCE * MAX_LINK_DISTANCE
                    && level.getBlockEntity(candidate) instanceof PylonBlockEntity pylon
                    && pylon.isRelayMode() && pylon.channel == channel) {
                result.add(candidate.immutable());
            }
        }
        return result;
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Channel", channel);
        tag.putBoolean("Formed", formed);
        if (mainBlock != null) tag.putLong("MainBlock", mainBlock.asLong());
        if (isMainStructureBlock()) {
            tag.putInt("Energy", energyStorage.getEnergyStored());
            tag.putInt("Matter", matterStorage.getMatterStored());
            tag.putInt("Charge", charge);
        }
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        channel = tag.getInt("Channel") & 15;
        formed = tag.getBoolean("Formed");
        mainBlock = tag.contains("MainBlock") ? BlockPos.of(tag.getLong("MainBlock")) : null;
        energyStorage.setEnergyStored(tag.getInt("Energy"));
        matterStorage.setMatterStored(tag.getInt("Matter"));
        charge = Mth.clamp(tag.getInt("Charge"), 0, MAX_CHARGE);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (formed) {
            PylonBlockEntity root = getRoot();
            if (root != this) return root.getCapability(cap, side);
            if (cap == ForgeCapabilities.ENERGY) return energyCapability.cast();
            if (cap == ModCapabilities.MATTER && (side == null || side == Direction.DOWN)) return matterCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
        matterCapability.invalidate();
    }

    @Override public void reviveCaps() {
        super.reviveCaps();
        energyCapability = LazyOptional.of(() -> energyStorage);
        matterCapability = LazyOptional.of(() -> matterInput);
    }

    @Override public Component getDisplayName() {
        return Component.literal(formed ? "Dimensional Pylon" : "Pylon");
    }

    @Nullable
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new PylonMenu(id, inventory, getRoot());
    }

    private static int low(int value) { return value & 0xffff; }
    private static int high(int value) { return (value >>> 16) & 0xffff; }
}
