package matteroverdrive.menu;

import matteroverdrive.blockentity.FusionReactorControllerBlockEntity;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class FusionReactorMenu extends AbstractContainerMenu {
    private static final int UPGRADE_SLOTS = 4;
    private static final int PLAYER_START = 4;
    private static final int PLAYER_END = 31;
    private static final int HOTBAR_START = 31;
    private static final int HOTBAR_END = 40;
    private static final int DATA_COUNT = 22;

    private final FusionReactorControllerBlockEntity reactor;
    private final ContainerData data;
    private final boolean remote;

    public FusionReactorMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, getReactor(inventory, buffer.readBlockPos()),
                new SimpleContainerData(DATA_COUNT), true);
    }

    public FusionReactorMenu(int id, Inventory inventory,
                             FusionReactorControllerBlockEntity reactor) {
        this(id, inventory, reactor, reactor.getData(), false);
    }

    public FusionReactorMenu(int id, Inventory inventory,
                             FusionReactorControllerBlockEntity reactor, boolean remote) {
        this(id, inventory, reactor, reactor.getData(), remote);
    }

    private FusionReactorMenu(int id, Inventory inventory,
                              FusionReactorControllerBlockEntity reactor,
                              ContainerData data, boolean remote) {
        super(ModMenus.FUSION_REACTOR_CONTROLLER.get(), id);
        this.reactor = reactor;
        this.data = data;
        this.remote = remote;
        for (int slot = 0; slot < UPGRADE_SLOTS; slot++) {
            addSlot(new SlotItemHandler(reactor.getUpgrades(), slot, 52 + slot * 18, 52));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        8 + column * 18, 156 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 214));
        }
        addDataSlots(data);
    }

    private static FusionReactorControllerBlockEntity getReactor(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof FusionReactorControllerBlockEntity reactor) {
            return reactor;
        }
        throw new IllegalStateException("Fusion Reactor controller missing");
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        boolean moved;
        if (index < UPGRADE_SLOTS) {
            moved = moveItemStackTo(stack, PLAYER_START, HOTBAR_END, true);
        } else {
            moved = false;
            if (stack.getItem() instanceof MachineUpgradeItem) {
                moved = moveItemStackTo(stack, 0, UPGRADE_SLOTS, false);
            }
            if (!moved) {
                moved = index < HOTBAR_START
                        ? moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)
                        : moveItemStackTo(stack, PLAYER_START, HOTBAR_START, false);
            }
        }
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return remote || stillValid(ContainerLevelAccess.create(
                reactor.getLevel(), reactor.getBlockPos()), player,
                ModBlocks.get("fusion_reactor_controller").get());
    }

    public int energy() {
        return value(0, 1);
    }

    public int capacity() {
        return value(2, 3);
    }

    public int matter() {
        return data.get(4);
    }

    public int matterCapacity() {
        return data.get(5);
    }

    public boolean valid() {
        return data.get(6) != 0;
    }

    public int anomalyDistance() {
        return data.get(7);
    }

    public int output() {
        return data.get(8);
    }

    public double efficiency() {
        return data.get(9) / 1_000.0D;
    }

    public double matterDrain() {
        return data.get(10) / 10_000.0D;
    }

    public int fault() {
        return data.get(11);
    }

    public double unsuppressedMass() {
        return data.get(12) / 1_000.0D;
    }

    public double suppressedMass() {
        return data.get(13) / 1_000.0D;
    }

    public int ioCount() {
        return data.get(14);
    }

    public Direction ringDirection() {
        return Direction.from2DDataValue(data.get(15));
    }

    public int stabilizerCount() {
        return data.get(16);
    }

    public int affectedEntityCount() {
        return data.get(17);
    }

    public double anomalyRange() {
        return data.get(18) / 100.0D;
    }

    public double blockHazardRange() {
        return data.get(19) / 100.0D;
    }

    public int connectedUsage() {
        return data.get(21);
    }

    public double eventHorizon() {
        return data.get(20) / 100.0D;
    }

    private int value(int low, int high) {
        return (data.get(low) & 0xffff) | ((data.get(high) & 0xffff) << 16);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != 1) return false;
        boolean enabled = reactor.getEnergy().toggleInfiniteEnergy();
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "[DEBUG] Infinite energy: " + (enabled ? "ON" : "OFF")), true);
        return true;
    }
}
