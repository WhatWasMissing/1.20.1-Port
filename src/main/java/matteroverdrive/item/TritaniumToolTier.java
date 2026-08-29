package matteroverdrive.item;

import matteroverdrive.registry.ModItems;
import net.minecraft.world.item.Ingredient;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

/** Material properties for the durable, high-tier tritanium tools. */
public final class TritaniumToolTier {
    public static final Tier TIER = new Tier() {
        @Override public int getUses() { return 2500; }
        @Override public float getSpeed() { return 9.0F; }
        @Override public float getAttackDamageBonus() { return 3.5F; }
        @Override public int getLevel() { return 4; }
        @Override public int getEnchantmentValue() { return 18; }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.get("tritanium_ingot").get());
        }
    };

    private TritaniumToolTier() {}
}
