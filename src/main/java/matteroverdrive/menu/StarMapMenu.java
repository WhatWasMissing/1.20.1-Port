package matteroverdrive.menu;

import matteroverdrive.blockentity.StarMapBlockEntity;
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

public class StarMapMenu extends AbstractContainerMenu {
    private final StarMapBlockEntity map;
    private final ContainerData data;

    public StarMapMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, find(inventory, buffer.readBlockPos()), new SimpleContainerData(2));
    }

    public StarMapMenu(int id, Inventory inventory, StarMapBlockEntity map) {
        this(id, inventory, map, map.dataFor(inventory.player));
    }

    private StarMapMenu(int id, Inventory inventory, StarMapBlockEntity map, ContainerData data) {
        super(ModMenus.STAR_MAP.get(), id);
        this.map = map;
        this.data = data;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 78 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 136));
        addDataSlots(data);
    }

    private static StarMapBlockEntity find(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        return blockEntity instanceof StarMapBlockEntity map
                ? map : new StarMapBlockEntity(pos, ModBlocks.get("star_map").get().defaultBlockState());
    }

    @Override public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), map.getBlockPos()), player, ModBlocks.get("star_map").get());
    }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    public int active() { return data.get(0); }
    public int complete() { return data.get(1); }
}
