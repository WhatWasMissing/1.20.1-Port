package matteroverdrive.menu;

import matteroverdrive.blockentity.WeaponStationBlockEntity;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class WeaponStationMenu extends AbstractContainerMenu {
    private static final int STATION_SLOTS = 7;
    private static final int PLAYER_START = STATION_SLOTS;
    private static final int PLAYER_END = PLAYER_START + 36;
    private final WeaponStationBlockEntity station;
    public WeaponStationMenu(int id, Inventory inv, FriendlyByteBuf buf) { this(id, inv, get(inv, buf.readBlockPos())); }
    public WeaponStationMenu(int id, Inventory inv, WeaponStationBlockEntity station) {
        super(ModMenus.WEAPON_STATION.get(), id); this.station = station;
        for (int i=0;i<STATION_SLOTS;i++) addSlot(new SlotItemHandler(station.getInventory(), i, 44 + (i%4)*18, 36 + (i/4)*18));
        for (int r=0;r<3;r++) for (int c=0;c<9;c++) addSlot(new Slot(inv, c+r*9+9, 8+c*18, 84+r*18));
        for (int c=0;c<9;c++) addSlot(new Slot(inv,c,8+c*18,142));
    }
    private static WeaponStationBlockEntity get(Inventory inv, BlockPos pos) { if (inv.player.level().getBlockEntity(pos) instanceof WeaponStationBlockEntity s) return s; throw new IllegalStateException("Weapon station missing"); }
    @Override public boolean stillValid(Player player) { return stillValid(ContainerLevelAccess.create(station.getLevel(), station.getBlockPos()), player, ModBlocks.get("weapon_station").get()); }
    @Override public ItemStack quickMoveStack(Player player, int index) { Slot slot=slots.get(index); if(!slot.hasItem()) return ItemStack.EMPTY; ItemStack stack=slot.getItem(); ItemStack copy=stack.copy(); if(index<STATION_SLOTS ? !moveItemStackTo(stack,PLAYER_START,PLAYER_END,true) : !moveItemStackTo(stack,0,STATION_SLOTS,false)) return ItemStack.EMPTY; slot.setChanged(); return copy; }
}
