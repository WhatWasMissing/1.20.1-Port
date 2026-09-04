package matteroverdrive.menu;

import matteroverdrive.blockentity.AndroidStationBlockEntity;
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

public class AndroidStationMenu extends AbstractContainerMenu {
    private final AndroidStationBlockEntity station;
    private final ContainerData data;

    public AndroidStationMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, find(inventory, buffer.readBlockPos()), new SimpleContainerData(15));
    }
    public AndroidStationMenu(int id, Inventory inventory, AndroidStationBlockEntity station) {
        this(id, inventory, station, station.getContainerData(inventory.player));
    }
    private AndroidStationMenu(int id, Inventory inventory, AndroidStationBlockEntity station, ContainerData data) {
        super(ModMenus.ANDROID_STATION.get(), id);
        this.station = station;
        this.data = data;
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 132 + row * 18));
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 190));
        addDataSlots(data);
    }
    private static AndroidStationBlockEntity find(Inventory inventory, BlockPos pos) {
        BlockEntity entity = inventory.player.level().getBlockEntity(pos);
        if (entity instanceof AndroidStationBlockEntity station) return station;
        return new AndroidStationBlockEntity(pos, ModBlocks.get("android_station").get().defaultBlockState());
    }
    @Override public boolean stillValid(Player player) { return stillValid(ContainerLevelAccess.create(player.level(), station.getBlockPos()), player, ModBlocks.get("android_station").get()); }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    public boolean androidActive() { return data.get(0) != 0; }
    public int parts() { return data.get(1); }
    public int androidEnergy() { return data.get(2) | data.get(3) << 16; }
    public int androidCapacity() { return data.get(4) | data.get(5) << 16; }
    public int stationEnergy() { return data.get(6) | data.get(7) << 16; }
    public int lastTransfer() { return data.get(8); }
    public int androidLevel() { return data.get(9); }
    public int experienceIntoLevel() { return data.get(10); }
    public int experienceToNextLevel() { return data.get(11); }
    public int availableSkillPoints() { return data.get(12); }
    public int selectedAbilityOrdinal() { return data.get(13); }
    public int activeAbilityFlags() { return data.get(14); }
}
