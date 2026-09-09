package matteroverdrive.blockentity;

import matteroverdrive.capability.IMatterStorage;
import matteroverdrive.capability.MachineMatterStorage;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.item.MatterStorageCellItem;
import matteroverdrive.registry.ModExtraBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/** Four-cell high-density Matter bank. Cells are physical capacity modules and can be moved between facilities. */
public class MatterStorageMatrixBlockEntity extends BlockEntity {
    public static final int BASE_CAPACITY = 64_000;
    public static final int CELL_SLOTS = 4;
    private final ItemStackHandler cells = new ItemStackHandler(CELL_SLOTS) {
        @Override public boolean isItemValid(int slot, ItemStack stack) { return stack.getItem() instanceof MatterStorageCellItem; }
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override protected void onContentsChanged(int slot) { updateCapacity(); setChanged(); }
    };
    private final MachineMatterStorage matter = new MachineMatterStorage(BASE_CAPACITY, true, true, this::setChanged);
    private LazyOptional<IMatterStorage> matterCap = LazyOptional.of(() -> matter);

    public MatterStorageMatrixBlockEntity(BlockPos pos, BlockState state) { super(ModExtraBlockEntities.MATTER_STORAGE_MATRIX.get(), pos, state); }

    private void updateCapacity() {
        long capacity = BASE_CAPACITY;
        for (int i = 0; i < cells.getSlots(); i++) {
            if (cells.getStackInSlot(i).getItem() instanceof MatterStorageCellItem cell) capacity += cell.capacity();
        }
        matter.setCapacity((int)Math.min(Integer.MAX_VALUE, capacity));
    }

    public InteractionResult onUse(ServerPlayer player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof MatterStorageCellItem) {
            for (int i = 0; i < cells.getSlots(); i++) {
                if (!cells.getStackInSlot(i).isEmpty()) continue;
                ItemStack one = held.copy(); one.setCount(1);
                cells.setStackInSlot(i, one);
                if (!player.getAbilities().instabuild) held.shrink(1);
                player.sendSystemMessage(Component.literal("Installed Matter cell in slot " + (i + 1) + "; capacity " + matter.getMatterCapacity()).withStyle(ChatFormatting.LIGHT_PURPLE));
                return InteractionResult.CONSUME;
            }
            player.sendSystemMessage(Component.literal("Matter Storage Matrix has no free cell slots").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }

        if (player.isCrouching() && held.isEmpty()) {
            for (int i = cells.getSlots() - 1; i >= 0; i--) {
                ItemStack cell = cells.getStackInSlot(i);
                if (cell.isEmpty()) continue;
                int removedCapacity = cell.getItem() instanceof MatterStorageCellItem storageCell ? storageCell.capacity() : 0;
                int nextCapacity = Math.max(BASE_CAPACITY, matter.getMatterCapacity() - removedCapacity);
                if (matter.getMatterStored() > nextCapacity) {
                    player.sendSystemMessage(Component.literal("Cannot remove cell: stored Matter exceeds remaining capacity (" + matter.getMatterStored() + " > " + nextCapacity + ")").withStyle(ChatFormatting.RED));
                    return InteractionResult.CONSUME;
                }
                ItemStack removed = cells.extractItem(i, 1, false);
                if (!player.getInventory().add(removed)) player.drop(removed, false);
                player.sendSystemMessage(Component.literal("Removed Matter cell; capacity " + matter.getMatterCapacity()).withStyle(ChatFormatting.YELLOW));
                return InteractionResult.CONSUME;
            }
        }

        player.sendSystemMessage(Component.literal("MATTER STORAGE MATRIX  " + matter.getMatterStored() + " / " + matter.getMatterCapacity() + " Matter").withStyle(ChatFormatting.LIGHT_PURPLE));
        int installed = 0; for (int i = 0; i < cells.getSlots(); i++) if (!cells.getStackInSlot(i).isEmpty()) installed++;
        player.sendSystemMessage(Component.literal("Cells: " + installed + "/" + CELL_SLOTS + " | use a Matter Storage Cell to install | sneak + empty hand to eject").withStyle(ChatFormatting.GRAY));
        return InteractionResult.CONSUME;
    }

    public int comparatorLevel() { return matter.getMatterCapacity() <= 0 ? 0 : (int)((long)matter.getMatterStored() * 15L / matter.getMatterCapacity()); }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        for (int i = 0; i < cells.getSlots(); i++) {
            ItemStack stack = cells.getStackInSlot(i);
            if (!stack.isEmpty()) Containers.dropItemStack(level, worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5, stack.copy());
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.put("Cells", cells.serializeNBT()); tag.putInt("Matter", matter.getMatterStored()); }
    @Override public void load(CompoundTag tag) { super.load(tag); if (tag.contains("Cells")) cells.deserializeNBT(tag.getCompound("Cells")); updateCapacity(); matter.setMatterStored(tag.getInt("Matter")); }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) { return cap == ModCapabilities.MATTER ? matterCap.cast() : super.getCapability(cap, side); }
    @Override public void invalidateCaps() { super.invalidateCaps(); matterCap.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); matterCap = LazyOptional.of(() -> matter); }
}