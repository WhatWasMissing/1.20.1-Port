package matteroverdrive.menu;

import matteroverdrive.blockentity.AndroidSpawnerBlockEntity;
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

public class AndroidSpawnerMenu extends AbstractContainerMenu {
    private final AndroidSpawnerBlockEntity spawner;
    private final ContainerData data;

    public AndroidSpawnerMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, find(inventory, buffer.readBlockPos()), new SimpleContainerData(7));
    }

    public AndroidSpawnerMenu(int id, Inventory inventory, AndroidSpawnerBlockEntity spawner) {
        this(id, inventory, spawner, spawner.getData());
    }

    private AndroidSpawnerMenu(int id, Inventory inventory,
                               AndroidSpawnerBlockEntity spawner, ContainerData data) {
        super(ModMenus.ANDROID_SPAWNER.get(), id);
        this.spawner = spawner;
        this.data = data;
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

    private static AndroidSpawnerBlockEntity find(Inventory inventory, BlockPos pos) {
        BlockEntity entity = inventory.player.level().getBlockEntity(pos);
        if (entity instanceof AndroidSpawnerBlockEntity spawner) return spawner;
        throw new IllegalStateException("Android Spawner missing");
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), spawner.getBlockPos()),
                player, ModBlocks.get("android_spawner").get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        boolean moved = index < 27
                ? moveItemStackTo(source, 27, 36, false)
                : moveItemStackTo(source, 0, 27, false);
        if (!moved) return ItemStack.EMPTY;
        if (source.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != 1) return false;
        int removed = spawner.removeSpawnedAndroids();
        player.displayClientMessage(Component.literal("Android Spawner removed " + removed + " owned Android(s)"), true);
        return true;
    }

    public int energy() { return combine(0, 1); }
    public int capacity() { return combine(2, 3); }
    public int spawned() { return Math.max(0, data.get(4)); }
    public int maxSpawned() { return Math.max(1, data.get(5)); }
    public int ticksUntilSpawn() { return Math.max(0, data.get(6)); }

    private int combine(int lowIndex, int highIndex) {
        return (data.get(lowIndex) & 0xffff) | ((data.get(highIndex) & 0xffff) << 16);
    }
}
