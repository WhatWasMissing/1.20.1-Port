package matteroverdrive.item;

import matteroverdrive.registry.ModItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Ingredient;

/** Protection and durability values for the tritanium armour set. */
public final class TritaniumArmorMaterial {
    public static final ArmorMaterial MATERIAL = new ArmorMaterial() {
        private final int[] protection = {3, 8, 6, 3};

        @Override public int getDurabilityForType(ArmorItem.Type type) {
            return new int[] {13, 15, 16, 11}[type.getSlot().getIndex()] * 37;
        }
        @Override public int getDefenseForType(ArmorItem.Type type) {
            return protection[type.getSlot().getIndex()];
        }
        @Override public int getEnchantmentValue() { return 18; }
        @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_NETHERITE; }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.get("tritanium_ingot").get());
        }
        @Override public String getName() { return "matteroverdrive:tritanium"; }
        @Override public float getToughness() { return 3.0F; }
        @Override public float getKnockbackResistance() { return 0.1F; }
    };

    private TritaniumArmorMaterial() {}
}
