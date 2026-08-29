package matteroverdrive.menu;

import matteroverdrive.blockentity.InscriberBlockEntity;
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

public class InscriberMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = 8;
    private static final int PLAYER_INV_START = 8;
    private static final int PLAYER_INV_END = 35;
    private static final int HOTBAR_END = 44;

    private final InscriberBlockEntity machine;
    private final ContainerData data;

    public InscriberMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, getMachine(inventory, buffer.readBlockPos()), new SimpleContainerData(11));
    }

    public InscriberMenu(int id, Inventory inventory, InscriberBlockEntity machine) {
        this(id, inventory, machine, machine.getContainerData());
    }

    private InscriberMenu(
            int id, Inventory inventory, InscriberBlockEntity machine, ContainerData data) {
        super(ModMenus.INSCRIBER.get(), id);
        this.machine = machine;
        this.data = data;
        addSlot(new SlotItemHandler(machine.getItemHandler(), InscriberBlockEntity.PRIMARY_SLOT, 27, 44));
        addSlot(new SlotItemHandler(machine.getItemHandler(), InscriberBlockEntity.SECONDARY_SLOT, 79, 44));
        addSlot(new SlotItemHandler(machine.getItemHandler(), InscriberBlockEntity.OUTPUT_SLOT, 133, 44) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addSlot(new SlotItemHandler(machine.getItemHandler(), InscriberBlockEntity.ENERGY_SLOT, 105, 43));
        for (int slot = 0; slot < InscriberBlockEntity.UPGRADE_SLOT_COUNT; slot++) {
            addSlot(new SlotItemHandler(machine.getUpgradeInventory(), slot, 53 + slot * 18, 78));
        }
        addPlayerInventory(inventory);
        addDataSlots(data);
    }

    private static InscriberBlockEntity getMachine(Inventory inventory, BlockPos pos) {
        BlockEntity entity = inventory.player.level().getBlockEntity(pos);
        if (entity instanceof InscriberBlockEntity inscriber) {
            return inscriber;
        }
        throw new IllegalStateException("Inscriber missing at " + pos);
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 145 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 203));
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
            moved = false;
            if (source.getItem() instanceof MachineUpgradeItem) {
                moved = moveItemStackTo(source, 3, MACHINE_SLOTS, false);
            }
            if (!moved && source.getCapability(ForgeCapabilities.ENERGY)
                    .map(energy -> energy.canExtract()).orElse(false)) {
                moved = moveItemStackTo(source, 3, 4, false);
            }
            if (!moved && machine.getItemHandler().isItemValid(InscriberBlockEntity.PRIMARY_SLOT, source)) {
                moved = moveItemStackTo(source, 0, 1, false);
            }
            if (!moved && machine.getItemHandler().isItemValid(InscriberBlockEntity.SECONDARY_SLOT, source)) {
                moved = moveItemStackTo(source, 1, 2, false);
            }
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
                player, ModBlocks.get("inscriber").get());
    }

    public int getProgress() { return data.get(0); }
    public int getCycleTime() { return data.get(1); }
    public int getEnergy() { return combine(data.get(2), data.get(3)); }
    public int getEnergyCapacity() { return combine(data.get(4), data.get(5)); }
    public int getRecipeTier() { return data.get(6); }
    public int getEnergyPerTick() { return data.get(7) & 0xFFFF; }
    public int getTotalEnergy() { return combine(data.get(8), data.get(9)); }
    public boolean isRunning() { return data.get(10) != 0; }

    private static int combine(int low, int high) {
        return (low & 0xFFFF) | ((high & 0xFFFF) << 16);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != 1) return false;
        boolean enabled = machine.getEnergyStorage().toggleInfiniteEnergy();
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "[DEBUG] Infinite energy: " + (enabled ? "ON" : "OFF")), true);
        return true;
    }
}
