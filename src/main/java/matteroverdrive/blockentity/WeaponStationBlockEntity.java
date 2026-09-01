package matteroverdrive.blockentity;

import matteroverdrive.item.weapon.EnergyWeaponItem;
import matteroverdrive.item.weapon.WeaponSystem;
import matteroverdrive.menu.WeaponStationMenu;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

public class WeaponStationBlockEntity extends BlockEntity implements MenuProvider {
    public static final int WEAPON_SLOT = 0;
    public static final int FIRST_MODULE_SLOT = 1;
    public static final int SLOT_COUNT = 1 + WeaponSystem.MODULE_SLOT_COUNT;

    private boolean syncingModules;
    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == WEAPON_SLOT) {
                return stack.getItem() instanceof EnergyWeaponItem;
            }
            if (slot >= FIRST_MODULE_SLOT && slot < SLOT_COUNT) {
                return WeaponSystem.isValidModuleForSlot(stack, slot - FIRST_MODULE_SLOT, getStackInSlot(WEAPON_SLOT));
            }
            return false;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public WeaponStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WEAPON_STATION.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public void unpackModulesFromWeapon() {
        if (syncingModules) {
            return;
        }
        ItemStack weapon = inventory.getStackInSlot(WEAPON_SLOT);
        if (!(weapon.getItem() instanceof EnergyWeaponItem)) {
            return;
        }
        for (int slot = FIRST_MODULE_SLOT; slot < SLOT_COUNT; slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                return;
            }
        }

        syncingModules = true;
        try {
            for (int moduleSlot = 0; moduleSlot < WeaponSystem.MODULE_SLOT_COUNT; moduleSlot++) {
                ItemStack module = WeaponSystem.getModule(weapon, moduleSlot);
                if (!module.isEmpty()) {
                    inventory.setStackInSlot(moduleSlot + FIRST_MODULE_SLOT, module.copyWithCount(1));
                    WeaponSystem.setModule(weapon, moduleSlot, ItemStack.EMPTY);
                }
            }
        } finally {
            syncingModules = false;
        }
        setChanged();
    }

    public void packModulesIntoCurrentWeapon() {
        packModulesInto(inventory.getStackInSlot(WEAPON_SLOT));
    }

    public void packModulesInto(ItemStack weapon) {
        if (syncingModules || !(weapon.getItem() instanceof EnergyWeaponItem)) {
            return;
        }

        syncingModules = true;
        try {
            for (int moduleSlot = 0; moduleSlot < WeaponSystem.MODULE_SLOT_COUNT; moduleSlot++) {
                int stationSlot = moduleSlot + FIRST_MODULE_SLOT;
                ItemStack module = inventory.getStackInSlot(stationSlot);
                WeaponSystem.setModule(weapon, moduleSlot, module);
                inventory.setStackInSlot(stationSlot, ItemStack.EMPTY);
            }
        } finally {
            syncingModules = false;
        }
        setChanged();
    }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        packModulesIntoCurrentWeapon();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, stack.copy());
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Weapon Station");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player, Player owner) {
        return new WeaponStationMenu(id, player, this);
    }
}
