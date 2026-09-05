package matteroverdrive.menu;

import matteroverdrive.blockentity.TransporterBlockEntity;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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

public class TransporterMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = 7;
    private static final int PLAYER_START = 7;
    private static final int PLAYER_END = 34;
    private static final int HOTBAR_END = 43;

    private final TransporterBlockEntity transporter;
    private final ContainerData data;

    public TransporterMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, get(inventory, buffer.readBlockPos()), new SimpleContainerData(15));
    }

    public TransporterMenu(int id, Inventory inventory, TransporterBlockEntity transporter) {
        this(id, inventory, transporter, transporter.getData());
    }

    private TransporterMenu(int id, Inventory inventory,
                            TransporterBlockEntity transporter, ContainerData data) {
        super(ModMenus.TRANSPORTER.get(), id);
        this.transporter = transporter;
        this.data = data;

        addSlot(new SlotItemHandler(transporter.getItemHandler(), 0, 44, 44));
        addSlot(new SlotItemHandler(transporter.getItemHandler(), 1, 112, 44));
        for (int slot = 0; slot < 5; slot++) {
            addSlot(new SlotItemHandler(transporter.getUpgrades(), slot, 43 + slot * 18, 78));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        8 + column * 18, 145 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 203));
        }
        addDataSlots(data);
    }

    private static TransporterBlockEntity get(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof TransporterBlockEntity transporter) return transporter;
        throw new IllegalStateException("Transporter missing");
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        boolean moved;

        if (index < MACHINE_SLOTS) {
            moved = moveItemStackTo(source, PLAYER_START, HOTBAR_END, true);
        } else {
            moved = false;
            if (source.getItem() instanceof MachineUpgradeItem) {
                moved = moveItemStackTo(source, 2, MACHINE_SLOTS, false);
            }
            if (!moved && transporter.getItemHandler().isItemValid(0, source)) {
                moved = moveItemStackTo(source, 0, 1, false);
            }
            if (!moved && source.getCapability(ForgeCapabilities.ENERGY)
                    .map(energy -> energy.canExtract()).orElse(false)) {
                moved = moveItemStackTo(source, 1, 2, false);
            }
            if (!moved) {
                moved = index < PLAYER_END
                        ? moveItemStackTo(source, PLAYER_END, HOTBAR_END, false)
                        : moveItemStackTo(source, PLAYER_START, PLAYER_END, false);
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
        return stillValid(ContainerLevelAccess.create(transporter.getLevel(), transporter.getBlockPos()),
                player, ModBlocks.get("transporter").get());
    }

    public int progress() { return data.get(0); }
    public int cycle() { return data.get(1); }
    public int e() { return combine(2, 3); }
    public int cap() { return combine(4, 5); }
    public boolean target() { return data.get(6) != 0; }
    public int dist() { return data.get(7); }
    public int cost() { return data.get(8); }
    public int range() { return data.get(9); }
    public int cooldown() { return data.get(10); }
    public boolean running() { return data.get(11) != 0; }
    public int destinationCount() { return Math.max(0, data.get(12)); }
    public int selectedDestination() { return data.get(13); }
    public int lastTransportedEntities() { return Math.max(0, data.get(14)); }

    private int combine(int lowIndex, int highIndex) {
        return (data.get(lowIndex) & 65_535) | ((data.get(highIndex) & 65_535) << 16);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 1) {
            boolean enabled = transporter.getEnergy().toggleInfiniteEnergy();
            player.displayClientMessage(Component.literal(
                    "[DEBUG] Infinite energy: " + (enabled ? "ON" : "OFF")), true);
            return true;
        }
        if (id == 2) {
            boolean imported = transporter.importDriveDestination();
            player.displayClientMessage(Component.literal(imported
                    ? "Transport destination imported"
                    : "Insert a bound Transport Flash Drive first"), true);
            return true;
        }
        if (id == 3) return transporter.cycleDestination(-1);
        if (id == 4) return transporter.cycleDestination(1);
        if (id == 5) {
            boolean removed = transporter.removeSelectedDestination();
            if (removed) player.displayClientMessage(Component.literal("Transport destination removed"), true);
            return true;
        }
        return false;
    }
}
