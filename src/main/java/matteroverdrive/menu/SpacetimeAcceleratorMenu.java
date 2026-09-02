package matteroverdrive.menu;

import matteroverdrive.blockentity.SpacetimeAcceleratorBlockEntity;
import matteroverdrive.item.MachineUpgradeItem;
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
import net.minecraftforge.items.SlotItemHandler;

public class SpacetimeAcceleratorMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = 4;
    private static final int PLAYER_INV_START = 4;
    private static final int PLAYER_INV_END = 31;
    private static final int HOTBAR_END = 40;

    private final SpacetimeAcceleratorBlockEntity machine;
    private final ContainerData data;

    public SpacetimeAcceleratorMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, getMachine(inventory, buffer.readBlockPos()), new SimpleContainerData(13));
    }

    public SpacetimeAcceleratorMenu(int id, Inventory inventory, SpacetimeAcceleratorBlockEntity machine) {
        this(id, inventory, machine, machine.getContainerData());
    }

    private SpacetimeAcceleratorMenu(int id, Inventory inventory,
                                     SpacetimeAcceleratorBlockEntity machine, ContainerData data) {
        super(ModMenus.SPACETIME_ACCELERATOR.get(), id);
        this.machine = machine;
        this.data = data;

        for (int slot = 0; slot < SpacetimeAcceleratorBlockEntity.UPGRADE_SLOT_COUNT; slot++) {
            addSlot(new SlotItemHandler(machine.getUpgradeInventory(), slot, 53 + slot * 18, 65));
        }
        addPlayerInventory(inventory);
        addDataSlots(data);
    }

    private static SpacetimeAcceleratorBlockEntity getMachine(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof SpacetimeAcceleratorBlockEntity accelerator) return accelerator;
        throw new IllegalStateException("Space-Time Accelerator missing at " + pos);
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 102 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 160));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        boolean moved;
        if (index < MACHINE_SLOTS) {
            moved = moveItemStackTo(source, PLAYER_INV_START, HOTBAR_END, true);
        } else {
            moved = source.getItem() instanceof MachineUpgradeItem
                    && moveItemStackTo(source, 0, MACHINE_SLOTS, false);
            if (!moved) {
                moved = index < PLAYER_INV_END
                        ? moveItemStackTo(source, PLAYER_INV_END, HOTBAR_END, false)
                        : moveItemStackTo(source, PLAYER_INV_START, PLAYER_INV_END, false);
            }
        }

        if (!moved) return ItemStack.EMPTY;
        if (source.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, source);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(machine.getLevel(), machine.getBlockPos()),
                player, ModBlocks.get("spacetime_accelerator").get());
    }

    public int getEnergy() {
        return combine(data.get(0), data.get(1));
    }

    public int getEnergyCapacity() {
        return combine(data.get(2), data.get(3));
    }

    public int getMatter() {
        return data.get(4);
    }

    public int getMatterCapacity() {
        return data.get(5);
    }

    public int getEnergyPerTick() {
        return data.get(6);
    }

    public int getPulseInterval() {
        return Math.max(1, data.get(7));
    }

    public int getRadius() {
        return data.get(8);
    }

    public boolean isActive() {
        return data.get(9) != 0;
    }

    public int getPulseTimer() {
        return data.get(10);
    }

    public int getLastAcceleratedTargets() {
        return data.get(11);
    }

    public boolean isRedstoneBlocked() {
        return data.get(12) != 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 1) {
            boolean enabled = machine.getEnergyStorage().toggleInfiniteEnergy();
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "[DEBUG] Infinite energy: " + (enabled ? "ON" : "OFF")), true);
            return true;
        }
        if (id == 2) {
            machine.fillMatterForDebug();
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "[DEBUG] Accelerator matter filled"), true);
            return true;
        }
        return false;
    }

    private static int combine(int low, int high) {
        return (low & 0xFFFF) | ((high & 0xFFFF) << 16);
    }
}
