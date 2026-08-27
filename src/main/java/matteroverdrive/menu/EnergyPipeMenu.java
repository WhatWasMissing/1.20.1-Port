package matteroverdrive.menu;

import matteroverdrive.blockentity.EnergyPipeBlockEntity;
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

public class EnergyPipeMenu extends AbstractContainerMenu {
    private final EnergyPipeBlockEntity pipe;
    private final ContainerData data;
    public EnergyPipeMenu(int id, Inventory inventory, FriendlyByteBuf buffer) { this(id, inventory, find(inventory, buffer.readBlockPos()), new SimpleContainerData(5)); }
    public EnergyPipeMenu(int id, Inventory inventory, EnergyPipeBlockEntity pipe) { this(id, inventory, pipe, pipe.getData()); }
    private EnergyPipeMenu(int id, Inventory inventory, EnergyPipeBlockEntity pipe, ContainerData data) {
        super(ModMenus.ENERGY_PIPE.get(), id); this.pipe = pipe; this.data = data;
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        addDataSlots(data);
    }
    private static EnergyPipeBlockEntity find(Inventory inventory, BlockPos pos) {
        BlockEntity entity = inventory.player.level().getBlockEntity(pos);
        if (entity instanceof EnergyPipeBlockEntity pipe) return pipe;
        throw new IllegalStateException("Energy Pipe missing");
    }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) { return stillValid(ContainerLevelAccess.create(pipe.getLevel(), pipe.getBlockPos()), player, ModBlocks.get("heavy_matter_pipe").get()); }
    public int stored() { return values(0, 1); } public int capacity() { return values(2, 3); } public int output() { return data.get(4); }
    private int values(int low, int high) { return (data.get(low) & 0xffff) | ((data.get(high) & 0xffff) << 16); }
}