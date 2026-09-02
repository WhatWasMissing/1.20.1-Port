package matteroverdrive.menu;

import matteroverdrive.blockentity.MicrowaveBlockEntity;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class MicrowaveMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = 7;
    private static final int PLAYER_INV_START = 7;
    private static final int PLAYER_INV_END = 34;
    private static final int HOTBAR_END = 43;

    private final MicrowaveBlockEntity machine;
    private final ContainerData data;

    public MicrowaveMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, getMachine(inventory, buffer.readBlockPos()), new SimpleContainerData(8));
    }

    public MicrowaveMenu(int id, Inventory inventory, MicrowaveBlockEntity machine) {
        this(id, inventory, machine, machine.getContainerData());
    }

    private MicrowaveMenu(int id, Inventory inventory, MicrowaveBlockEntity machine, ContainerData data) {
        super(ModMenus.MICROWAVE.get(), id);
        this.machine = machine;
        this.data = data;

        addSlot(new SlotItemHandler(machine.getItemHandler(), MicrowaveBlockEntity.INPUT_SLOT, 26, 44));
        addSlot(new SlotItemHandler(machine.getItemHandler(), MicrowaveBlockEntity.ENERGY_SLOT, 80, 44));
        addSlot(new SlotItemHandler(machine.getItemHandler(), MicrowaveBlockEntity.OUTPUT_SLOT, 134, 44) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int slot = 0; slot < MicrowaveBlockEntity.UPGRADE_SLOT_COUNT; slot++) {
            addSlot(new SlotItemHandler(machine.getUpgradeInventory(), slot, 53 + slot * 18, 66));
        }
        addPlayerInventory(inventory);
        addDataSlots(data);
    }

    private static MicrowaveBlockEntity getMachine(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof MicrowaveBlockEntity microwave) return microwave;
        throw new IllegalStateException("Microwave missing at " + pos);
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
            moved = false;
            if (source.getItem() instanceof MachineUpgradeItem) {
                moved = moveItemStackTo(source, 3, MACHINE_SLOTS, false);
            }
            if (!moved && source.getCapability(ForgeCapabilities.ENERGY)
                    .map(storage -> storage.canExtract()).orElse(false)) {
                moved = moveItemStackTo(source, MicrowaveBlockEntity.ENERGY_SLOT,
                        MicrowaveBlockEntity.ENERGY_SLOT + 1, false);
            }
            if (!moved && source.isEdible()) {
                moved = moveItemStackTo(source, MicrowaveBlockEntity.INPUT_SLOT,
                        MicrowaveBlockEntity.INPUT_SLOT + 1, false);
            }
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
                player, ModBlocks.get("microwave").get());
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getMaxProgress() {
        return Math.max(1, data.get(1));
    }

    public int getEnergy() {
        return combine(data.get(2), data.get(3));
    }

    public int getEnergyCapacity() {
        return combine(data.get(4), data.get(5));
    }

    public int getEnergyPerTick() {
        return data.get(6) & 0xFFFF;
    }

    public boolean isRunning() {
        return data.get(7) != 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != 1) return false;
        boolean enabled = machine.getEnergyStorage().toggleInfiniteEnergy();
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "[DEBUG] Infinite energy: " + (enabled ? "ON" : "OFF")), true);
        return true;
    }

    private static int combine(int low, int high) {
        return (low & 0xFFFF) | ((high & 0xFFFF) << 16);
    }
}
