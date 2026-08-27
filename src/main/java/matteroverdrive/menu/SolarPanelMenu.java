package matteroverdrive.menu;

import matteroverdrive.blockentity.SolarPanelBlockEntity;
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

public class SolarPanelMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = 2;
    private static final int PLAYER_INV_START = 2;
    private static final int PLAYER_INV_END = 29;
    private static final int HOTBAR_END = 38;

    private final SolarPanelBlockEntity machine;
    private final ContainerData data;

    public SolarPanelMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, getMachine(inventory, buffer.readBlockPos()), new SimpleContainerData(11));
    }

    public SolarPanelMenu(int id, Inventory inventory, SolarPanelBlockEntity machine) {
        this(id, inventory, machine, machine.getContainerData());
    }

    private SolarPanelMenu(
            int id, Inventory inventory, SolarPanelBlockEntity machine, ContainerData data) {
        super(ModMenus.SOLAR_PANEL.get(), id);
        this.machine = machine;
        this.data = data;

        for (int slot = 0; slot < SolarPanelBlockEntity.UPGRADE_SLOT_COUNT; slot++) {
            addSlot(new SlotItemHandler(machine.getUpgradeInventory(), slot, 71 + slot * 18, 44));
        }
        addPlayerInventory(inventory);
        addDataSlots(data);
    }

    private static SolarPanelBlockEntity getMachine(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof SolarPanelBlockEntity panel) {
            return panel;
        }
        throw new IllegalStateException("Solar Panel missing at " + pos);
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        inventory, column + row * 9 + 9,
                        8 + column * 18, 122 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 180));
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
            moved = source.getItem() instanceof MachineUpgradeItem
                    && moveItemStackTo(source, 0, MACHINE_SLOTS, false);
            if (!moved) {
                moved = index < PLAYER_INV_END
                        ? moveItemStackTo(source, PLAYER_INV_END, HOTBAR_END, false)
                        : moveItemStackTo(source, PLAYER_INV_START, PLAYER_INV_END, false);
            }
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
        return stillValid(
                ContainerLevelAccess.create(machine.getLevel(), machine.getBlockPos()),
                player,
                ModBlocks.get("solar_panel").get());
    }

    public int getEnergy() {
        return combine(data.get(0), data.get(1));
    }

    public int getEnergyCapacity() {
        return combine(data.get(2), data.get(3));
    }

    public int getCurrentGeneration() {
        return data.get(4) & 0xFFFF;
    }

    public int getLastOutput() {
        return data.get(5) & 0xFFFF;
    }

    public boolean dimensionHasSky() {
        return data.get(6) != 0;
    }

    public boolean canSeeSky() {
        return data.get(7) != 0;
    }

    public int getEffectiveSkyLight() {
        return data.get(8) & 0xFFFF;
    }

    public double getDaylightFactor() {
        return ((short) data.get(9)) / 1000.0D;
    }

    public int getMaxOutputPerSide() {
        return data.get(10) & 0xFFFF;
    }

    private static int combine(int low, int high) {
        return (low & 0xFFFF) | ((high & 0xFFFF) << 16);
    }
}
