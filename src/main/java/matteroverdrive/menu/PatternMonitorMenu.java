package matteroverdrive.menu;

import matteroverdrive.blockentity.PatternMonitorBlockEntity;
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

public class PatternMonitorMenu extends AbstractContainerMenu {
    private static final int GHOST_SLOTS = PatternMonitorBlockEntity.DISPLAY_SLOTS;
    private static final int PLAYER_INV_START = GHOST_SLOTS;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_END = PLAYER_INV_END + 9;

    private final PatternMonitorBlockEntity machine;
    private final ContainerData data;

    public PatternMonitorMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, getMachine(inv, buf.readBlockPos()), new SimpleContainerData(2 + GHOST_SLOTS));
    }

    public PatternMonitorMenu(int id, Inventory inv, PatternMonitorBlockEntity machine) {
        this(id, inv, machine, machine.getContainerData());
    }

    private PatternMonitorMenu(int id, Inventory inv, PatternMonitorBlockEntity machine, ContainerData data) {
        super(ModMenus.PATTERN_MONITOR.get(), id);
        this.machine = machine;
        this.data = data;

        for (int i = 0; i < GHOST_SLOTS; i++) {
            int x = 44 + (i % 4) * 22;
            int y = 28 + (i / 4) * 20;
            addSlot(new GhostSlot(machine, i, x, y));
        }
        addPlayer(inv);
        addDataSlots(data);
    }

    private static PatternMonitorBlockEntity getMachine(Inventory inv, BlockPos pos) {
        BlockEntity blockEntity = inv.player.level().getBlockEntity(pos);
        if (blockEntity instanceof PatternMonitorBlockEntity monitor) {
            return monitor;
        }
        throw new IllegalStateException("Pattern Monitor missing at " + pos);
    }

    private void addPlayer(Inventory inv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 96 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 154));
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return id >= 0 && id < GHOST_SLOTS && machine.requestReplication(id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < GHOST_SLOTS) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        boolean moved = index < PLAYER_INV_END
                ? moveItemStackTo(source, PLAYER_INV_END, HOTBAR_END, false)
                : moveItemStackTo(source, PLAYER_INV_START, PLAYER_INV_END, false);
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(machine.getLevel(), machine.getBlockPos()), player,
                ModBlocks.get("pattern_monitor").get());
    }

    public int getPatternCount() {
        return data.get(0) & 0xFFFF;
    }

    public int getQueueSize() {
        return data.get(1) & 0xFFFF;
    }

    public int getPatternProgress(int slot) {
        if (slot < 0 || slot >= GHOST_SLOTS) {
            return 0;
        }
        return data.get(2 + slot) & 0xFFFF;
    }

    private static final class GhostSlot extends SlotItemHandler {
        GhostSlot(PatternMonitorBlockEntity monitor, int index, int x, int y) {
            super(monitor.getDisplayItems(), index, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
