package matteroverdrive.blockentity;

import matteroverdrive.menu.TritaniumCrateMenu;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class TritaniumCrateBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity
        implements MenuProvider {
    public static final int SLOT_COUNT = 54;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private net.minecraft.resources.ResourceLocation structureLoot;
    private long structureLootSeed;
    private boolean structureLootAssigned;

    /** Generation only assigns metadata; loot resolves on the server during actual access. */
    public void seedStructureLoot(net.minecraft.resources.ResourceLocation table, long seed) {
        if (structureLootAssigned || getUsedSlots() != 0) return;
        structureLootAssigned = true;
        structureLoot = table;
        structureLootSeed = seed;
        setChanged();
    }

    private void unpackStructureLoot() {
        if (structureLoot == null || !(level instanceof net.minecraft.server.level.ServerLevel server)) return;
        var table = server.getServer().getLootData().getLootTable(structureLoot);
        var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(server)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,
                        net.minecraft.world.phys.Vec3.atCenterOf(worldPosition))
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
        // Clear before invoking loot modifiers, which may themselves query capabilities.
        structureLoot = null;
        var stacks = table.getRandomItems(params, structureLootSeed);
        for (ItemStack stack : stacks) {
            for (int slot = 0; slot < inventory.getSlots() && !stack.isEmpty(); slot++) {
                stack = inventory.insertItem(slot, stack, false);
            }
        }
        setChanged();
    }

    private LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> inventory);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> getUsedSlots();
                case 1 -> getTotalItemCount();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public TritaniumCrateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRITANIUM_CRATE.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        unpackStructureLoot();
        return inventory;
    }

    public ContainerData getContainerData() {
        return data;
    }

    public int getUsedSlots() {
        int used = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                used++;
            }
        }
        return used;
    }

    public int getTotalItemCount() {
        int total = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            total += inventory.getStackInSlot(slot).getCount();
        }
        return total;
    }

    public void writeInventoryToItem(ItemStack stack) {
        unpackStructureLoot();
        if (!stack.isEmpty()) {
            stack.getOrCreateTag().put("TritaniumCrateInventory", inventory.serializeNBT());
        }
    }

    public void readInventoryFromItem(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("TritaniumCrateInventory")) {
            inventory.deserializeNBT(tag.getCompound("TritaniumCrateInventory"));
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putBoolean("StructureLootAssigned", structureLootAssigned);
        if (structureLoot != null) tag.putString("StructureLoot", structureLoot.toString());
        tag.putLong("StructureLootSeed", structureLootSeed);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        structureLoot = tag.contains("StructureLoot") ? net.minecraft.resources.ResourceLocation.tryParse(tag.getString("StructureLoot")) : null;
        structureLootAssigned = tag.getBoolean("StructureLootAssigned") || structureLoot != null;
        structureLootSeed = tag.getLong("StructureLootSeed");
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            unpackStructureLoot();
            return itemCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemCapability = LazyOptional.of(() -> inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.matteroverdrive.tritanium_crate");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        unpackStructureLoot();
        return new TritaniumCrateMenu(containerId, playerInventory, this);
    }
}

