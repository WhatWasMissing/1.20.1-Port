package matteroverdrive.menu;

import matteroverdrive.blockentity.PatternStorageBlockEntity;
import matteroverdrive.item.PatternDriveItem;
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

public class PatternStorageMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = 7;
    private static final int PLAYER_INV_START = 7;
    private static final int PLAYER_INV_END = 34;
    private static final int HOTBAR_END = 43;

    private final PatternStorageBlockEntity machine;
    private final ContainerData data;

    public PatternStorageMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, getMachine(inv, buf.readBlockPos()), new SimpleContainerData(5));
    }

    public PatternStorageMenu(int id, Inventory inv, PatternStorageBlockEntity machine) {
        this(id, inv, machine, machine.getContainerData());
    }

    private PatternStorageMenu(int id, Inventory inv, PatternStorageBlockEntity machine, ContainerData data) {
        super(ModMenus.PATTERN_STORAGE.get(), id);
        this.machine = machine;
        this.data = data;

        addSlot(new SlotItemHandler(machine.getItemHandler(), PatternStorageBlockEntity.ENERGY_SLOT, 26, 44));
        for (int i = 0; i < PatternStorageBlockEntity.DRIVE_COUNT; i++) {
            addSlot(new SlotItemHandler(machine.getItemHandler(), PatternStorageBlockEntity.FIRST_DRIVE_SLOT + i,
                    62 + (i % 3) * 24, 32 + (i / 3) * 24));
        }
        addPlayer(inv);
        addDataSlots(data);
    }

    private static PatternStorageBlockEntity getMachine(Inventory inv, BlockPos pos) {
        BlockEntity blockEntity = inv.player.level().getBlockEntity(pos);
        if (blockEntity instanceof PatternStorageBlockEntity storage) {
            return storage;
        }
        throw new IllegalStateException("Pattern Storage missing at " + pos);
    }

    private void addPlayer(Inventory inv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
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
            if (source.getItem() instanceof PatternDriveItem) {
                moved = moveItemStackTo(source, 1, 7, false);
            }
            if (!moved && source.getCapability(ForgeCapabilities.ENERGY).map(e -> e.canExtract()).orElse(false)) {
                moved = moveItemStackTo(source, 0, 1, false);
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
        return stillValid(ContainerLevelAccess.create(machine.getLevel(), machine.getBlockPos()), player,
                ModBlocks.get("pattern_storage").get());
    }

    public int getEnergy() {
        return combine(data.get(0), data.get(1));
    }

    public int getEnergyCapacity() {
        return combine(data.get(2), data.get(3));
    }

    public int getPatternCount() {
        return data.get(4) & 0xFFFF;
    }

    private static int combine(int low, int high) {
        return (low & 0xFFFF) | ((high & 0xFFFF) << 16);
    }
}
