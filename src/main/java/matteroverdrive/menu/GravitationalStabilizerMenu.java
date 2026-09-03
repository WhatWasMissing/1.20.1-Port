package matteroverdrive.menu;

import matteroverdrive.blockentity.GravitationalStabilizerBlockEntity;
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

public class GravitationalStabilizerMenu extends AbstractContainerMenu {
    private static final int UPGRADE_SLOTS = 4;

    private final GravitationalStabilizerBlockEntity stabilizer;
    private final ContainerData data;

    public GravitationalStabilizerMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, find(inventory, buffer.readBlockPos()), new SimpleContainerData(12));
    }

    public GravitationalStabilizerMenu(int id, Inventory inventory,
                                       GravitationalStabilizerBlockEntity stabilizer) {
        this(id, inventory, stabilizer, stabilizer.getContainerData());
    }

    private GravitationalStabilizerMenu(int id, Inventory inventory,
                                        GravitationalStabilizerBlockEntity stabilizer,
                                        ContainerData data) {
        super(ModMenus.GRAVITATIONAL_STABILIZER.get(), id);
        this.stabilizer = stabilizer;
        this.data = data;

        for (int slot = 0; slot < UPGRADE_SLOTS; slot++) {
            addSlot(new SlotItemHandler(stabilizer.getUpgrades(), slot, 53 + slot * 18, 37));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        8 + column * 18, 101 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 159));
        }
        addDataSlots(data);
    }

    private static GravitationalStabilizerBlockEntity find(Inventory inventory, BlockPos pos) {
        BlockEntity entity = inventory.player.level().getBlockEntity(pos);
        if (entity instanceof GravitationalStabilizerBlockEntity stabilizer) return stabilizer;
        throw new IllegalStateException("Gravitational Stabilizer missing at " + pos);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        boolean moved = index < UPGRADE_SLOTS
                ? moveItemStackTo(source, UPGRADE_SLOTS, slots.size(), true)
                : moveItemStackTo(source, 0, UPGRADE_SLOTS, false);
        if (!moved) return ItemStack.EMPTY;
        if (source.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, source);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(stabilizer.getLevel(), stabilizer.getBlockPos()),
                player, ModBlocks.get("gravitational_stabilizer").get());
    }

    public int energy() { return combine(data.get(0), data.get(1)); }
    public int energyCapacity() { return combine(data.get(2), data.get(3)); }
    public int requiredPower() { return data.get(4); }
    public int powerUsed() { return data.get(5); }
    public boolean isPowered() { return data.get(6) != 0; }
    public int anomalyDistance() { return data.get(7) - 1; }
    public boolean isBeamBlocked() { return data.get(8) != 0; }
    public int beamBlockedDistance() { return data.get(9) - 1; }
    public int redstoneMode() { return data.get(10); }
    public boolean redstoneAllowsOperation() { return data.get(11) != 0; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != 1) return false;
        int mode = stabilizer.cycleRedstoneMode();
        String label = switch (mode) {
            case 1 -> "HIGH (signal runs)";
            case 2 -> "LOW (signal stops)";
            default -> "IGNORED";
        };
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "Stabilizer redstone: " + label), true);
        return true;
    }

    private static int combine(int low, int high) {
        return (low & 0xFFFF) | ((high & 0xFFFF) << 16);
    }
}
