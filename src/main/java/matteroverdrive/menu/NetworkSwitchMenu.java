package matteroverdrive.menu;

import matteroverdrive.blockentity.NetworkSwitchBlockEntity;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NetworkSwitchMenu extends AbstractContainerMenu {
    public static final int BUTTON_TOGGLE = 0;

    private final NetworkSwitchBlockEntity networkSwitch;
    private final DataSlot enabled;
    private final DataSlot connections;

    public NetworkSwitchMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, find(inventory, buf.readBlockPos()));
    }

    public NetworkSwitchMenu(int id, Inventory inventory, NetworkSwitchBlockEntity networkSwitch) {
        super(ModMenus.NETWORK_SWITCH.get(), id);
        this.networkSwitch = networkSwitch;

        int playerX = 34;
        int playerY = 104;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, playerX + col * 18, playerY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, playerX + col * 18, playerY + 58));
        }

        enabled = addDataSlot(new DataSlot() {
            private int clientValue;
            @Override public int get() {
                return inventory.player.level().isClientSide ? clientValue : (networkSwitch.isEnabled() ? 1 : 0);
            }
            @Override public void set(int value) { clientValue = value; }
        });
        connections = addDataSlot(new DataSlot() {
            private int clientValue;
            @Override public int get() {
                return inventory.player.level().isClientSide ? clientValue : networkSwitch.connectionMask();
            }
            @Override public void set(int value) { clientValue = value; }
        });
    }

    private static NetworkSwitchBlockEntity find(Inventory inventory, BlockPos pos) {
        BlockEntity entity = inventory.player.level().getBlockEntity(pos);
        return entity instanceof NetworkSwitchBlockEntity networkSwitch
                ? networkSwitch
                : new NetworkSwitchBlockEntity(pos, ModBlocks.get("network_switch").get().defaultBlockState());
    }

    public boolean isEnabled() {
        return enabled.get() != 0;
    }

    public int getConnectionMask() {
        return connections.get();
    }

    public boolean isConnected(Direction direction) {
        return (getConnectionMask() & (1 << direction.get3DDataValue())) != 0;
    }

    public int getConnectionCount() {
        return Integer.bitCount(getConnectionMask() & 0x3F);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_TOGGLE) return false;
        if (!player.level().isClientSide) {
            networkSwitch.toggle();
        }
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), networkSwitch.getBlockPos()), player,
                ModBlocks.get("network_switch").get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // The switch has no machine inventory. Preserve normal player inventory/hotbar shift movement.
        Slot slot = getSlot(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        if (index < 27) {
            if (!moveItemStackTo(source, 27, 36, false)) return ItemStack.EMPTY;
        } else if (index < 36) {
            if (!moveItemStackTo(source, 0, 27, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (source.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }
}
