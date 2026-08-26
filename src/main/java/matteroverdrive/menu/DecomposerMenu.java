package matteroverdrive.menu;

import matteroverdrive.blockentity.DecomposerBlockEntity;
import matteroverdrive.matter.MatterValueRegistry;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class DecomposerMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = 3;
    private static final int PLAYER_INV_START = MACHINE_SLOTS;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_END = PLAYER_INV_END + 9;

    private final DecomposerBlockEntity decomposer;
    private final ContainerData data;

    public DecomposerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, getDecomposer(playerInventory, buffer.readBlockPos()), new SimpleContainerData(10));
    }

    public DecomposerMenu(int containerId, Inventory playerInventory, DecomposerBlockEntity decomposer) {
        this(containerId, playerInventory, decomposer, decomposer.getContainerData());
    }

    private DecomposerMenu(int containerId, Inventory playerInventory, DecomposerBlockEntity decomposer, ContainerData data) {
        super(ModMenus.DECOMPOSER.get(), containerId);
        this.decomposer = decomposer;
        this.data = data == null ? new SimpleContainerData(10) : data;

        addSlot(new SlotItemHandler(decomposer.getItemHandler(), DecomposerBlockEntity.INPUT_SLOT, 26, 44));
        addSlot(new SlotItemHandler(decomposer.getItemHandler(), DecomposerBlockEntity.ENERGY_SLOT, 80, 44));
        addSlot(new SlotItemHandler(decomposer.getItemHandler(), DecomposerBlockEntity.OUTPUT_SLOT, 134, 44) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(playerInventory);
        addDataSlots(this.data);
    }

    private static DecomposerBlockEntity getDecomposer(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof DecomposerBlockEntity decomposer) {
            return decomposer;
        }
        throw new IllegalStateException("Matter Overdrive decomposer block entity missing at " + pos);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack empty = ItemStack.EMPTY;
        Slot sourceSlot = slots.get(index);
        if (!sourceSlot.hasItem()) {
            return empty;
        }

        ItemStack source = sourceSlot.getItem();
        ItemStack copy = source.copy();

        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(source, PLAYER_INV_START, HOTBAR_END, true)) {
                return empty;
            }
        } else {
            boolean moved = false;
            if (source.getCapability(ForgeCapabilities.ENERGY).map(storage -> storage.canExtract()).orElse(false)) {
                moved = moveItemStackTo(source, DecomposerBlockEntity.ENERGY_SLOT, DecomposerBlockEntity.ENERGY_SLOT + 1, false);
            }
            if (!moved && MatterValueRegistry.containsMatter(source)) {
                moved = moveItemStackTo(source, DecomposerBlockEntity.INPUT_SLOT, DecomposerBlockEntity.INPUT_SLOT + 1, false);
            }
            if (!moved) {
                if (index < PLAYER_INV_END) {
                    moved = moveItemStackTo(source, PLAYER_INV_END, HOTBAR_END, false);
                } else {
                    moved = moveItemStackTo(source, PLAYER_INV_START, PLAYER_INV_END, false);
                }
            }
            if (!moved) {
                return empty;
            }
        }

        if (source.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(player, source);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(
                ContainerLevelAccess.create(decomposer.getLevel(), decomposer.getBlockPos()),
                player,
                ModBlocks.get("decomposer").get()
        );
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getMaxProgress() {
        return data.get(1);
    }

    public int getEnergy() {
        return combineWords(data.get(2), data.get(3));
    }

    public int getEnergyCapacity() {
        return combineWords(data.get(4), data.get(5));
    }

    public int getMatter() {
        return data.get(6) & 0xFFFF;
    }

    public int getMatterCapacity() {
        return data.get(7) & 0xFFFF;
    }

    public int getInputMatterValue() {
        return data.get(8) & 0xFFFF;
    }

    public int getEnergyPerTick() {
        return data.get(9) & 0xFFFF;
    }

    private static int combineWords(int low, int high) {
        return (low & 0xFFFF) | ((high & 0xFFFF) << 16);
    }
}
