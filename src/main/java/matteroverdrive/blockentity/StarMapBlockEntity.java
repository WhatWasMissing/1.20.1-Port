package matteroverdrive.blockentity;

import matteroverdrive.item.ContractItem;
import matteroverdrive.menu.StarMapMenu;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

public class StarMapBlockEntity extends BlockEntity implements MenuProvider {
    public StarMapBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.STAR_MAP.get(), pos, state); }

    public ContainerData dataFor(Player viewer) {
        return new ContainerData() {
            @Override public int get(int index) {
                int active = 0;
                int done = 0;
                for (var stack : viewer.getInventory().items) {
                    if (stack.getItem() instanceof ContractItem) {
                        active++;
                        if (ContractItem.complete(stack)) done++;
                    }
                }
                return index == 0 ? active : index == 1 ? done : 0;
            }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return 2; }
        };
    }

    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.star_map"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new StarMapMenu(id, inventory, this);
    }
}
