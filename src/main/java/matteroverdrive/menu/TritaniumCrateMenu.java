package matteroverdrive.menu;

import matteroverdrive.block.TritaniumCrateBlock;
import matteroverdrive.blockentity.TritaniumCrateBlockEntity;
import matteroverdrive.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class TritaniumCrateMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = TritaniumCrateBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INV_START = MACHINE_SLOTS;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_END = PLAYER_INV_END + 9;

    private final TritaniumCrateBlockEntity crate;
    private final ContainerData data;

    public TritaniumCrateMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, getCrate(inventory, buffer.readBlockPos()), new SimpleContainerData(2));
    }

    public TritaniumCrateMenu(int id, Inventory inventory, TritaniumCrateBlockEntity crate) {
        this(id, inventory, crate, crate.getContainerData());
    }

    private TritaniumCrateMenu(
            int id, Inventory inventory, TritaniumCrateBlockEntity crate, ContainerData data) {
        super(ModMenus.TRITANIUM_CRATE.get(), id);
        this.crate = crate;
        this.data = data;

        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new SlotItemHandler(
                        crate.getInventory(), column + row * 9,
                        8 + column * 18, 18 + row * 18));
            }
        }
        addPlayerInventory(inventory);
        addDataSlots(data);
    }

    private static TritaniumCrateBlockEntity getCrate(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof TritaniumCrateBlockEntity crate) {
            return crate;
        }
        throw new IllegalStateException("Tritanium Crate missing at " + pos);
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 157 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 215));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        boolean moved;
        if (index < MACHINE_SLOTS) {
            moved = moveItemStackTo(source, PLAYER_INV_START, HOTBAR_END, true);
        } else {
            moved = moveItemStackTo(source, 0, MACHINE_SLOTS, false);
        }
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, source);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return crate.getLevel() != null
                && crate.getLevel().getBlockState(crate.getBlockPos()).getBlock()
                        instanceof TritaniumCrateBlock
                && player.distanceToSqr(
                        crate.getBlockPos().getX() + 0.5,
                        crate.getBlockPos().getY() + 0.5,
                        crate.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    public int getUsedSlots() {
        return data.get(0);
    }

    public int getTotalItemCount() {
        return data.get(1);
    }
}
