package matteroverdrive.menu;

import matteroverdrive.blockentity.ChargingStationBlockEntity;
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

public class ChargingStationMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = 1 + ChargingStationBlockEntity.UPGRADE_SLOT_COUNT;
    private static final int PLAYER_START = MACHINE_SLOTS;
    private static final int PLAYER_END = PLAYER_START + 27;
    private static final int HOTBAR_END = PLAYER_END + 9;

    private final ChargingStationBlockEntity station;
    private final ContainerData data;

    public ChargingStationMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, find(inventory, buffer.readBlockPos()), new SimpleContainerData(13));
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
        for (int slot = 0; slot < ChargingStationBlockEntity.UPGRADE_SLOT_COUNT; slot++) {
            addSlot(new SlotItemHandler(station.getUpgradeInventory(), slot, 53 + slot * 18, 68));
        }
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

    private static ChargingStationBlockEntity find(Inventory inventory, BlockPos pos) {
        BlockEntity entity = inventory.player.level().getBlockEntity(pos);
        if (entity instanceof ChargingStationBlockEntity station) return station;
        throw new IllegalStateException("Charging Station missing at " + pos);
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
                moved = moveItemStackTo(source, 1, MACHINE_SLOTS, false);
            }
            if (!moved && source.getCapability(ForgeCapabilities.ENERGY)
                    .map(energy -> energy.canReceive()).orElse(false)) {
                moved = moveItemStackTo(source, 0, 1, false);
            }
            if (!moved) {
                moved = index < PLAYER_END
                        ? moveItemStackTo(source, PLAYER_END, HOTBAR_END, false)
                        : moveItemStackTo(source, PLAYER_START, PLAYER_END, false);
            }
        }
        if (!moved) return ItemStack.EMPTY;
        if (source.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, source);
        return copy;
    }

    @Override public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(station.getLevel(), station.getBlockPos()),
                player, ModBlocks.get("charging_station").get());
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != 1) return false;
        boolean enabled = station.getEnergyStorage().toggleInfiniteEnergy();
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "[DEBUG] Infinite energy: " + (enabled ? "ON" : "OFF")), true);
        return true;
    }

    public int batteryEnergy() { return combine(data.get(0), data.get(1)); }
    public int batteryCapacity() { return combine(data.get(2), data.get(3)); }
    public int stationEnergy() { return combine(data.get(4), data.get(5)); }
    public int stationCapacity() { return combine(data.get(6), data.get(7)); }
    public int lastItemTransferred() { return data.get(8); }
    public int lastAndroidTransferred() { return data.get(9); }
    public int androidsCharged() { return data.get(10); }
    public int androidRange() { return data.get(11); }
    public int maxAndroidCharge() { return data.get(12); }

    private static int combine(int low, int high) {
        return (low & 0xFFFF) | ((high & 0xFFFF) << 16);
    }
}
