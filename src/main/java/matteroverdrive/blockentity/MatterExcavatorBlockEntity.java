package matteroverdrive.blockentity;

import matteroverdrive.capability.IMatterStorage;
import matteroverdrive.capability.MachineEnergyStorage;
import matteroverdrive.capability.MachineMatterStorage;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.matter.MatterValueRegistry;
import matteroverdrive.network.MatterNetworkUtil;
import matteroverdrive.registry.ModExtraBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/** Industrial remote decomposer: consumes FE, removes filtered blocks and stores their Matter value. */
public class MatterExcavatorBlockEntity extends BlockEntity {
    public static final int ENERGY_CAPACITY = 2_000_000;
    public static final int ENERGY_PER_BLOCK = 4_000;
    public static final int MATTER_CAPACITY = 128_000;
    public static final int OUTPUT_PER_TICK = 2_048;
    private static final int SEARCH_BUDGET = 128;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(ENERGY_CAPACITY, 32_768, 0, this::setChanged);
    private final MachineMatterStorage matter = new MachineMatterStorage(MATTER_CAPACITY, false, true, this::setChanged);
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private LazyOptional<IMatterStorage> matterCap = LazyOptional.of(() -> matter);
    private ResourceLocation filter = BuiltInRegistries.BLOCK.getKey(Blocks.IRON_ORE);
    private int radius = 8;
    private long scanCursor;
    private long outputCursor;
    private int lastMatter;
    private BlockPos lastTarget;

    public MatterExcavatorBlockEntity(BlockPos pos, BlockState state) { super(ModExtraBlockEntities.MATTER_EXCAVATOR.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MatterExcavatorBlockEntity node) {
        node.pullEnergy();
        node.outputMatter();
        if (level.getGameTime() % 10L == 0L && !level.hasNeighborSignal(pos)) node.excavate();
    }

    private void pullEnergy() {
        if (level == null) return;
        int remaining = Math.min(32_768, energy.getMaxEnergyStored() - energy.getEnergyStored());
        for (Direction direction : Direction.values()) {
            if (remaining <= 0) break;
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null) continue;
            IEnergyStorage source = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (source == null || !source.canExtract()) continue;
            int accepted = Math.min(source.extractEnergy(remaining, true), energy.receiveEnergy(remaining, true));
            if (accepted > 0) remaining -= energy.receiveEnergy(source.extractEnergy(accepted, false), false);
        }
    }

    private void outputMatter() {
        if (level == null || matter.getMatterStored() <= 0) return;
        int moved = MatterNetworkUtil.transferMatter(level, worldPosition, Math.min(OUTPUT_PER_TICK, matter.getMatterStored()), outputCursor++);
        if (moved > 0) matter.extractMatter(moved, false);
    }

    private void excavate() {
        if (level == null || energy.getEnergyStored() < ENERGY_PER_BLOCK || matter.getMatterStored() >= matter.getMatterCapacity()) return;
        int diameter = radius * 2 + 1;
        long volume = (long)diameter * diameter * diameter;
        for (int attempt = 0; attempt < SEARCH_BUDGET; attempt++) {
            long index = Math.floorMod(scanCursor++, volume);
            int x = (int)(index % diameter) - radius;
            int z = (int)((index / diameter) % diameter) - radius;
            int y = (int)(index / ((long)diameter * diameter)) - radius;
            BlockPos target = worldPosition.offset(x, y, z);
            if (target.equals(worldPosition) || !level.hasChunkAt(target)) continue;
            BlockState targetState = level.getBlockState(target);
            if (!BuiltInRegistries.BLOCK.getKey(targetState.getBlock()).equals(filter)) continue;
            float hardness = targetState.getDestroySpeed(level, target);
            if (hardness < 0.0F || hardness > 50.0F) continue;
            ItemStack representation = new ItemStack(targetState.getBlock().asItem());
            int value = Math.max(1, MatterValueRegistry.getMatter(level, representation));
            int accepted = matter.receiveMatter(value, true);
            if (accepted < value) return;
            if (energy.extractEnergy(ENERGY_PER_BLOCK, false) < ENERGY_PER_BLOCK) return;
            if (level.destroyBlock(target, false)) {
                matter.receiveMatter(value, false);
                lastMatter = value;
                lastTarget = target.immutable();
                setChanged();
            }
            return;
        }
    }

    public InteractionResult onUse(ServerPlayer player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof BlockItem blockItem) {
            filter = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
            scanCursor = 0L;
            setChanged();
            player.sendSystemMessage(Component.literal("Excavator filter -> " + filter).withStyle(ChatFormatting.AQUA));
            return InteractionResult.CONSUME;
        }
        if (player.isCrouching()) {
            radius = radius == 8 ? 16 : radius == 16 ? 24 : 8;
            scanCursor = 0L;
            setChanged();
            player.sendSystemMessage(Component.literal("Excavator radius -> " + radius + " blocks (redstone signal pauses work)").withStyle(ChatFormatting.AQUA));
            return InteractionResult.CONSUME;
        }
        player.sendSystemMessage(Component.literal("MATTER EXCAVATOR  filter " + filter + " | radius " + radius).withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("FE " + energy.getEnergyStored() + "/" + ENERGY_CAPACITY + " | Matter " + matter.getMatterStored() + "/" + matter.getMatterCapacity()).withStyle(ChatFormatting.GRAY));
        if (lastTarget != null) player.sendSystemMessage(Component.literal("Last extraction: " + lastMatter + " Matter at " + lastTarget.toShortString()).withStyle(ChatFormatting.LIGHT_PURPLE));
        return InteractionResult.CONSUME;
    }

    public int comparatorLevel() { return matter.getMatterCapacity() <= 0 ? 0 : (int)((long)matter.getMatterStored() * 15L / matter.getMatterCapacity()); }
    public int getRadius() { return radius; }
    public ResourceLocation getFilter() { return filter; }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); tag.putInt("Energy", energy.getEnergyStored()); tag.putInt("Matter", matter.getMatterStored()); tag.putString("Filter", filter.toString()); tag.putInt("Radius", radius); tag.putLong("ScanCursor", scanCursor); tag.putLong("OutputCursor", outputCursor); tag.putInt("LastMatter", lastMatter); if (lastTarget != null) tag.putLong("LastTarget", lastTarget.asLong());
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); energy.setEnergyStored(tag.getInt("Energy")); matter.setMatterStored(tag.getInt("Matter")); ResourceLocation parsed = ResourceLocation.tryParse(tag.getString("Filter")); if (parsed != null && BuiltInRegistries.BLOCK.containsKey(parsed)) filter = parsed; radius = tag.getInt("Radius"); if (radius != 8 && radius != 16 && radius != 24) radius = 8; scanCursor = tag.getLong("ScanCursor"); outputCursor = tag.getLong("OutputCursor"); lastMatter = tag.getInt("LastMatter"); lastTarget = tag.contains("LastTarget") ? BlockPos.of(tag.getLong("LastTarget")) : null;
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) { if (cap == ForgeCapabilities.ENERGY) return energyCap.cast(); if (cap == ModCapabilities.MATTER) return matterCap.cast(); return super.getCapability(cap, side); }
    @Override public void invalidateCaps() { super.invalidateCaps(); energyCap.invalidate(); matterCap.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); energyCap = LazyOptional.of(() -> energy); matterCap = LazyOptional.of(() -> matter); }
}