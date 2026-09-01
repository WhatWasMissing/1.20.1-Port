package matteroverdrive.menu;

import matteroverdrive.blockentity.WeaponStationBlockEntity;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class WeaponStationMenu extends AbstractContainerMenu {
    private static final int STATION_SLOTS = WeaponStationBlockEntity.SLOT_COUNT;
    private static final int PLAYER_START = STATION_SLOTS;
    private static final int PLAYER_END = PLAYER_START + 36;
    private final WeaponStationBlockEntity station;

    public WeaponStationMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, get(inv, buf.readBlockPos()));
    }

    public WeaponStationMenu(int id, Inventory inv, WeaponStationBlockEntity station) {
        super(ModMenus.WEAPON_STATION.get(), id);
        this.station = station;
        station.unpackModulesFromWeapon();

        addSlot(new SlotItemHandler(station.getInventory(), WeaponStationBlockEntity.WEAPON_SLOT, 26, 36) {
            @Override
            public void set(ItemStack stack) {
                super.set(stack);
                station.unpackModulesFromWeapon();
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                station.packModulesInto(stack);
                super.onTake(player, stack);
            }
        });

        addSlot(new SlotItemHandler(station.getInventory(), 1, 62, 36));
        addSlot(new SlotItemHandler(station.getInventory(), 2, 80, 36));
        addSlot(new SlotItemHandler(station.getInventory(), 3, 98, 36));
        addSlot(new SlotItemHandler(station.getInventory(), 4, 62, 54));
        addSlot(new SlotItemHandler(station.getInventory(), 5, 80, 54));
        addSlot(new SlotItemHandler(station.getInventory(), 6, 98, 54));

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, 84 + r * 18));
            }
        }
        for (int c = 0; c < 9; c++) {
            addSlot(new Slot(inv, c, 8 + c * 18, 142));
        }
    }

    private static WeaponStationBlockEntity get(Inventory inv, BlockPos pos) {
        if (inv.player.level().getBlockEntity(pos) instanceof WeaponStationBlockEntity station) {
            return station;
        }
        throw new IllegalStateException("Weapon station missing");
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(station.getLevel(), station.getBlockPos()), player, ModBlocks.get("weapon_station").get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        if (index == WeaponStationBlockEntity.WEAPON_SLOT) {
            station.packModulesInto(stack);
        }
        ItemStack copy = stack.copy();
        if (index < STATION_SLOTS) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, STATION_SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, copy);
        return copy;
    }

    @Override
    public void removed(Player player) {
        station.packModulesIntoCurrentWeapon();
        super.removed(player);
    }
}
