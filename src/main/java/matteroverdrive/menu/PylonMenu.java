package matteroverdrive.menu;

import matteroverdrive.blockentity.PylonBlockEntity;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PylonMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = 13;
    private final PylonBlockEntity pylon;
    private final ContainerData data;

    public PylonMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, find(inventory, buffer.readBlockPos()), new SimpleContainerData(DATA_COUNT));
    }

    public PylonMenu(int id, Inventory inventory, PylonBlockEntity pylon) {
        this(id, inventory, pylon, pylon.getContainerData());
    }

    private PylonMenu(int id, Inventory inventory, PylonBlockEntity pylon, ContainerData data) {
        super(ModMenus.PYLON.get(), id);
        this.pylon = pylon;
        this.data = data;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        8 + column * 18, 112 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 170));
        }
        addDataSlots(data);
    }

    private static PylonBlockEntity find(Inventory inventory, BlockPos pos) {
        BlockEntity entity = inventory.player.level().getBlockEntity(pos);
        if (entity instanceof PylonBlockEntity pylon) return pylon.getRoot();
        throw new IllegalStateException("Dimensional Pylon missing at " + pos);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        int playerEnd = slots.size() - 9;
        boolean moved = index < playerEnd
                ? moveItemStackTo(source, playerEnd, slots.size(), false)
                : moveItemStackTo(source, 0, playerEnd, false);
        if (!moved) return ItemStack.EMPTY;
        if (source.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(
                ContainerLevelAccess.create(pylon.getLevel(), pylon.getBlockPos()),
                player, ModBlocks.get("pylon").get());
    }

    public int energy() { return combine(data.get(0), data.get(1)); }
    public int energyCapacity() { return combine(data.get(2), data.get(3)); }
    public int matter() { return data.get(4); }
    public int matterCapacity() { return data.get(5); }
    public int generatedPerTick() { return data.get(6); }
    public int matterDrainPerSecond() { return data.get(7); }
    public int charge() { return data.get(8); }
    public int maxCharge() { return Math.max(1, data.get(9)); }
    public float dimensionalValue() { return (data.get(10) & 0xffff) / 10000.0F; }
    public boolean formed() { return data.get(11) != 0; }
    public int relayChannel() { return data.get(12); }

    private static int combine(int low, int high) {
        return (low & 0xffff) | ((high & 0xffff) << 16);
    }
}
