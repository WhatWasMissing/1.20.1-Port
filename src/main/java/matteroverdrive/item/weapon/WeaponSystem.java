package matteroverdrive.item.weapon;

import matteroverdrive.item.CreativeBatteryItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class WeaponSystem {
    public static final int BATTERY_SLOT = 0;
    public static final int COLOR_SLOT = 1;
    public static final int BARREL_SLOT = 2;
    public static final int SIGHTS_SLOT = 3;
    public static final int OTHER_SLOT_ONE = 4;
    public static final int OTHER_SLOT_TWO = 5;
    public static final int MODULE_SLOT_COUNT = 6;

    private static final String MODULES_TAG = "MatterOverdriveWeaponModules";

    private WeaponSystem() {
    }

    public static ItemStack getModule(ItemStack weapon, int slot) {
        if (slot < 0 || slot >= MODULE_SLOT_COUNT || weapon.isEmpty()) {
            return ItemStack.EMPTY;
        }
        CompoundTag root = weapon.getTagElement(MODULES_TAG);
        if (root == null || !root.contains(key(slot))) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(root.getCompound(key(slot)));
    }

    public static void setModule(ItemStack weapon, int slot, ItemStack module) {
        if (slot < 0 || slot >= MODULE_SLOT_COUNT || weapon.isEmpty()) {
            return;
        }
        CompoundTag root = weapon.getOrCreateTagElement(MODULES_TAG);
        if (module.isEmpty()) {
            root.remove(key(slot));
        } else {
            root.put(key(slot), module.copyWithCount(1).save(new CompoundTag()));
        }
    }

    public static boolean isValidModuleForSlot(ItemStack module, int slot, ItemStack weapon) {
        if (module.isEmpty()) {
            return true;
        }
        if (!(weapon.getItem() instanceof EnergyWeaponItem gun)) {
            return false;
        }
        if (slot == BATTERY_SLOT) {
            return module.getItem() instanceof WeaponBatteryItem
                    || module.getItem() instanceof CreativeBatteryItem;
        }
        if (!(module.getItem() instanceof WeaponModuleItem weaponModule)) {
            return false;
        }
        boolean slotMatches = switch (slot) {
            case COLOR_SLOT -> weaponModule.getSlotType() == WeaponModuleItem.SlotType.COLOR;
            case BARREL_SLOT -> weaponModule.getSlotType() == WeaponModuleItem.SlotType.BARREL;
            case SIGHTS_SLOT -> weaponModule.getSlotType() == WeaponModuleItem.SlotType.SIGHTS;
            case OTHER_SLOT_ONE, OTHER_SLOT_TWO -> weaponModule.getSlotType() == WeaponModuleItem.SlotType.OTHER;
            default -> false;
        };
        return slotMatches && gun.supportsModule(weaponModule);
    }

    public static int installedModuleCount(ItemStack weapon) {
        int count = 0;
        for (int slot = 0; slot < MODULE_SLOT_COUNT; slot++) {
            if (!getModule(weapon, slot).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public static WeaponModuleItem.Effect getBarrelEffect(ItemStack weapon) {
        ItemStack module = getModule(weapon, BARREL_SLOT);
        if (module.getItem() instanceof WeaponModuleItem weaponModule) {
            return weaponModule.getEffect();
        }
        return null;
    }

    public static boolean hasEffect(ItemStack weapon, WeaponModuleItem.Effect effect) {
        for (int slot = 0; slot < MODULE_SLOT_COUNT; slot++) {
            ItemStack module = getModule(weapon, slot);
            if (module.getItem() instanceof WeaponModuleItem weaponModule
                    && weaponModule.getEffect() == effect) {
                return true;
            }
        }
        return false;
    }

    public static int getColor(ItemStack weapon) {
        ItemStack module = getModule(weapon, COLOR_SLOT);
        if (module.getItem() instanceof WeaponModuleItem weaponModule
                && weaponModule.getEffect() == WeaponModuleItem.Effect.COLOR) {
            return weaponModule.getColor();
        }
        return 0x66CCFF;
    }

    public static int getCapacity(ItemStack weapon, int fallback) {
        ItemStack battery = getModule(weapon, BATTERY_SLOT);
        if (battery.getItem() instanceof CreativeBatteryItem) {
            return Integer.MAX_VALUE;
        }
        if (battery.getItem() instanceof WeaponBatteryItem batteryItem) {
            return batteryItem.getCapacity();
        }
        return fallback;
    }

    public static float damageMultiplier(ItemStack weapon) {
        WeaponModuleItem.Effect effect = getBarrelEffect(weapon);
        if (effect == WeaponModuleItem.Effect.DAMAGE) {
            return 1.5F;
        }
        if (effect == WeaponModuleItem.Effect.FIRE) {
            return 0.75F;
        }
        if (effect == WeaponModuleItem.Effect.HEAL || effect == WeaponModuleItem.Effect.BLOCK) {
            return 0.0F;
        }
        return 1.0F;
    }

    public static float energyMultiplier(ItemStack weapon) {
        WeaponModuleItem.Effect effect = getBarrelEffect(weapon);
        if (effect == null) {
            return 1.0F;
        }
        return switch (effect) {
            case EXPLOSION, DOOMSDAY -> 0.2F;
            case DAMAGE, FIRE, HEAL, BLOCK -> 0.5F;
            default -> 1.0F;
        };
    }

    public static float cooldownMultiplier(ItemStack weapon) {
        WeaponModuleItem.Effect effect = getBarrelEffect(weapon);
        if (effect == WeaponModuleItem.Effect.EXPLOSION) {
            return 0.15F;
        }
        if (effect == WeaponModuleItem.Effect.DOOMSDAY) {
            return 0.1F;
        }
        return 1.0F;
    }

    public static float rangeMultiplier(ItemStack weapon) {
        return hasEffect(weapon, WeaponModuleItem.Effect.SNIPER_SCOPE) ? 1.5F : 1.0F;
    }

    public static float accuracyMultiplier(ItemStack weapon, boolean aimed) {
        if (hasEffect(weapon, WeaponModuleItem.Effect.SNIPER_SCOPE)) {
            return aimed ? 0.4F : 1.8F;
        }
        if (hasEffect(weapon, WeaponModuleItem.Effect.HOLO_SIGHTS)) {
            return aimed ? 0.6F : 0.8F;
        }
        return 1.0F;
    }

    private static String key(int slot) {
        return "Slot" + slot;
    }
}
