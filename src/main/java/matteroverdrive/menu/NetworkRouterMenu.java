package matteroverdrive.menu;

import matteroverdrive.blockentity.NetworkRouterBlockEntity;
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

public class NetworkRouterMenu extends AbstractContainerMenu {
    private final NetworkRouterBlockEntity router;
    private final ContainerData data;

    public NetworkRouterMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, find(inventory, buffer.readBlockPos()), new SimpleContainerData(9));
    }

    public NetworkRouterMenu(int id, Inventory inventory, NetworkRouterBlockEntity router) {
        this(id, inventory, router, router.getData());
    }

    private NetworkRouterMenu(int id, Inventory inventory,
                              NetworkRouterBlockEntity router, ContainerData data) {
        super(ModMenus.NETWORK_ROUTER.get(), id);
        this.router = router;
        this.data = data;
        addSlot(new SlotItemHandler(router.getFilter(), 0, 80, 41));
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

    private static NetworkRouterBlockEntity find(Inventory inventory, BlockPos pos) {
        BlockEntity entity = inventory.player.level().getBlockEntity(pos);
        return entity instanceof NetworkRouterBlockEntity router
                ? router
                : new NetworkRouterBlockEntity(pos, ModBlocks.get("network_router").get().defaultBlockState());
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), router.getBlockPos()),
                player, ModBlocks.get("network_router").get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public int energy() { return data.get(0) | data.get(1) << 16; }
    public int endpoints() { return data.get(2); }
    public int nodes() { return data.get(3); }
    public int pylons() { return data.get(4); }
    public int lastMoved() { return data.get(5); }
    public boolean filtered() { return data.get(6) != 0; }
    public int destinationCount() { return Math.max(0, data.get(7)); }
    public int filterMode() { return data.get(8); }

    public String filterModeLabel() {
        return switch (filterMode()) {
            case 2 -> "DESTINATION DRIVE";
            case 1 -> "ITEM FILTER";
            default -> "NO FILTER";
        };
    }
}
