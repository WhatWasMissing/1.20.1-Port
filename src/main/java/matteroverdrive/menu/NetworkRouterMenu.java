package matteroverdrive.menu;

import matteroverdrive.blockentity.NetworkRouterBlockEntity;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.network.ItemNetworkUtil;
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
    private static final int MACHINE_SLOTS = 5;
    private static final int PLAYER_START = 5;
    private static final int PLAYER_END = 32;
    private static final int HOTBAR_END = 41;

    private final NetworkRouterBlockEntity router;
    private final ContainerData data;

    public NetworkRouterMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, find(inventory, buffer.readBlockPos()), new SimpleContainerData(NetworkRouterBlockEntity.DATA_COUNT));
    }

    public NetworkRouterMenu(int id, Inventory inventory, NetworkRouterBlockEntity router) {
        this(id, inventory, router, router.getData());
    }

    private NetworkRouterMenu(int id, Inventory inventory, NetworkRouterBlockEntity router, ContainerData data) {
        super(ModMenus.NETWORK_ROUTER.get(), id);
        this.router = router;
        this.data = data;

        addSlot(new SlotItemHandler(router.getFilter(), 0, 45, 49));
        for (int slot = 0; slot < NetworkRouterBlockEntity.UPGRADE_SLOT_COUNT; slot++) {
            addSlot(new SlotItemHandler(router.getUpgrades(), slot, 116 + slot * 18, 49));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 34 + column * 18, 137 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 34 + column * 18, 195));
        addDataSlots(data);
    }

    private static NetworkRouterBlockEntity find(Inventory inventory, BlockPos pos) {
        BlockEntity entity = inventory.player.level().getBlockEntity(pos);
        return entity instanceof NetworkRouterBlockEntity router
                ? router : new NetworkRouterBlockEntity(pos, ModBlocks.get("network_router").get().defaultBlockState());
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), router.getBlockPos()), player, ModBlocks.get("network_router").get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        boolean moved;
        if (index < MACHINE_SLOTS) {
            moved = moveItemStackTo(source, PLAYER_START, HOTBAR_END, true);
        } else {
            moved = false;
            if (source.getItem() instanceof MachineUpgradeItem upgrade
                    && (upgrade.getUpgrade() == MachineUpgradeItem.Upgrade.SPEED
                    || upgrade.getUpgrade() == MachineUpgradeItem.Upgrade.HYPER_SPEED)) {
                moved = moveItemStackTo(source, 1, MACHINE_SLOTS, false);
            }
            if (!moved && router.getFilter().getStackInSlot(0).isEmpty()) moved = moveItemStackTo(source, 0, 1, false);
            if (!moved) moved = index < PLAYER_END
                    ? moveItemStackTo(source, PLAYER_END, HOTBAR_END, false)
                    : moveItemStackTo(source, PLAYER_START, PLAYER_END, false);
        }
        if (!moved) return ItemStack.EMPTY;
        if (source.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, source);
        return copy;
    }

    public int energy() { return data.get(0) | data.get(1) << 16; }
    public int endpoints() { return data.get(2); }
    public int nodes() { return data.get(3); }
    public int pylons() { return data.get(4); }
    public int lastMoved() { return data.get(5); }
    public boolean filtered() { return data.get(6) != 0; }
    public int destinationCount() { return Math.max(0, data.get(7)); }
    public int filterMode() { return data.get(8); }
    public int itemBudget() { return Math.max(1, data.get(9)); }
    public int routerCount() { return Math.max(0, data.get(10)); }
    public ItemNetworkUtil.MoveStatus routeStatus() { return ItemNetworkUtil.MoveStatus.byOrdinal(data.get(11)); }
    public int stalledTicks() { return Math.max(0, data.get(12)); }
    public boolean executing() { return data.get(13) != 0; }
    public int protectedSinks() { return Math.max(0, data.get(14)); }
    public int disabledSwitches() { return Math.max(0, data.get(15)); }
    public int pylonLinks() { return Math.max(0, data.get(16)); }
    public boolean graphTruncated() { return data.get(17) != 0; }
    public int historySize() { return Math.max(0, data.get(18)); }
    public int lastEnergyCost() { return Math.max(0, data.get(19)); }

    public String filterModeLabel() {
        return switch (filterMode()) {
            case 2 -> "DESTINATION DRIVE";
            case 1 -> "ITEM FILTER";
            default -> "NO FILTER";
        };
    }

    public String routeStatusLabel() {
        return switch (routeStatus()) {
            case IDLE -> "IDLE";
            case MOVED -> "ROUTING";
            case NO_ENDPOINTS -> "NEEDS 2 ENDPOINTS";
            case NO_BUDGET -> "NO ITEM BUDGET";
            case NO_SOURCE_ITEMS -> "NO SOURCE ITEMS";
            case FILTER_MISS -> "FILTER MISS";
            case NO_ALLOWED_DESTINATION -> "NO ALLOWED DESTINATION";
            case DESTINATION_FULL -> "DESTINATION FULL";
            case NO_ROUTE -> "NO VALID ROUTE";
            case NO_ENERGY -> "NEEDS FE";
            case SECONDARY_ROUTER -> "STANDBY ROUTER";
            case GRAPH_LIMIT -> "GRAPH LIMIT";
        };
    }
}
