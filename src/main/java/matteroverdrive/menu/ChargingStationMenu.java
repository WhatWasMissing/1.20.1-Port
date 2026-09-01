package matteroverdrive.menu;

import matteroverdrive.blockentity.ChargingStationBlockEntity;
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

public class ChargingStationMenu extends AbstractContainerMenu {
    private final ChargingStationBlockEntity station;
    private final ContainerData data;

    public ChargingStationMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, find(inventory, buffer.readBlockPos()), new SimpleContainerData(9));
    }

    public ChargingStationMenu(int id, Inventory inventory, ChargingStationBlockEntity station) {
        this(id, inventory, station, station.getContainerData());
    }

    private ChargingStationMenu(int id, Inventory inventory,
                                ChargingStationBlockEntity station, ContainerData data) {
        super(ModMenus.CHARGING_STATION.get(), id);
        this.station = station;
        this.data = data;
        addSlot(new SlotItemHandler(station.getBatteryInventory(), 0, 80, 42));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        8 + column * 18, 96 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 154));
        }
        addDataSlots(data);
    }

    private static ChargingStationBlockEntity find(Inventory inventory, BlockPos pos) {
        BlockEntity entity = inventory.player.level().getBlockEntity(pos);
        if (entity instanceof ChargingStationBlockEntity station) return station;
        throw new IllegalStateException("Charging Station missing at " + pos);
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        boolean moved = index == 0
                ? moveItemStackTo(source, 1, slots.size(), true)
                : moveItemStackTo(source, 0, 1, false);
        if (!moved) return ItemStack.EMPTY;
        if (source.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, source);
        return copy;
    }

    @Override public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(station.getLevel(), station.getBlockPos()),
                player, ModBlocks.get("charging_station").get());
    }

    public int batteryEnergy() { return combine(data.get(0), data.get(1)); }
    public int batteryCapacity() { return combine(data.get(2), data.get(3)); }
    public int stationEnergy() { return combine(data.get(4), data.get(5)); }
    public int stationCapacity() { return combine(data.get(6), data.get(7)); }
    public int lastTransferred() { return data.get(8); }

    private static int combine(int low, int high) {
        return (low & 0xFFFF) | ((high & 0xFFFF) << 16);
    }
}
