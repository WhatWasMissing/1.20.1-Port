package matteroverdrive.menu;

import matteroverdrive.android.AndroidChassisData;
import matteroverdrive.android.AndroidData;
import matteroverdrive.blockentity.AndroidStationBlockEntity;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModItems;
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
    public static final int PART_BUTTON_BASE = 10;
    public static final int CHASSIS_BUTTON_BASE = 100;
    private final AndroidStationBlockEntity station;
    private final ContainerData data;

    public AndroidStationMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, find(inventory, buffer.readBlockPos()), new SimpleContainerData(20));
    }
    public AndroidStationMenu(int id, Inventory inventory, AndroidStationBlockEntity station) {
        this(id, inventory, station, station.getContainerData(inventory.player));
    }
    private AndroidStationMenu(int id, Inventory inventory, AndroidStationBlockEntity station, ContainerData data) {
        super(ModMenus.ANDROID_STATION.get(), id);
        this.station = station;
        this.data = data;
        int invX = 55;
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column + row * 9 + 9, invX + column * 18, 157 + row * 18));
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, invX + column * 18, 215));
        addDataSlots(data);
    }
    private static AndroidStationBlockEntity find(Inventory inventory, BlockPos pos) {
        BlockEntity entity = inventory.player.level().getBlockEntity(pos);
        if (entity instanceof AndroidStationBlockEntity station) return station;
        return new AndroidStationBlockEntity(pos, ModBlocks.get("android_station").get().defaultBlockState());
    }
    @Override public boolean stillValid(Player player) { return stillValid(ContainerLevelAccess.create(player.level(), station.getBlockPos()), player, ModBlocks.get("android_station").get()); }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!AndroidData.isAndroid(player)) return false;
        if (id == 1) {
            AndroidData.cycleAbility(player);
            return true;
        }
        if (id >= PART_BUTTON_BASE && id < PART_BUTTON_BASE + AndroidData.Part.values().length) {
            return togglePart(player, AndroidData.Part.values()[id - PART_BUTTON_BASE]);
        }
        if (id >= CHASSIS_BUTTON_BASE && id < CHASSIS_BUTTON_BASE + AndroidChassisData.Module.values().length) {
            return equipModule(player, AndroidChassisData.Module.values()[id - CHASSIS_BUTTON_BASE]);
        }
        return false;
    }

    private boolean togglePart(Player player, AndroidData.Part part) {
        if (AndroidData.hasPart(player, part)) {
            if (!AndroidData.removePart(player, part)) return false;
            giveOrDrop(player, AndroidData.partStack(part));
            return true;
        }
        int slot = findInventoryItem(player, ModItems.get(part.itemId).get());
        if (slot < 0 || !AndroidData.installPart(player, part)) return false;
        player.getInventory().getItem(slot).shrink(1);
        return true;
    }

    private boolean equipModule(Player player, AndroidChassisData.Module module) {
        AndroidChassisData.Module installed = AndroidChassisData.get(player, module.slot);
        if (installed == module) {
            AndroidChassisData.Module removed = AndroidChassisData.remove(player, module.slot);
            if (removed != null) giveOrDrop(player, AndroidChassisData.stack(removed));
            return removed != null;
        }
        int slot = findInventoryItem(player, ModItems.get(module.itemId).get());
        if (slot < 0) return false;
        player.getInventory().getItem(slot).shrink(1);
        AndroidChassisData.Module old = AndroidChassisData.install(player, module);
        if (old != null) giveOrDrop(player, AndroidChassisData.stack(old));
        return true;
    }

    private static int findInventoryItem(Player player, net.minecraft.world.item.Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(item)) return i;
        }
        return -1;
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (stack.isEmpty()) return;
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

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
    public int chassisModuleOrdinal(AndroidChassisData.Slot slot) { return data.get(15 + slot.ordinal()) - 1; }
    public boolean chassisEquipped(AndroidChassisData.Module module) { return chassisModuleOrdinal(module.slot) == module.ordinal(); }
}
