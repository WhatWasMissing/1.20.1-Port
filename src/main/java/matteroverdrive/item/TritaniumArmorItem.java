package matteroverdrive.item;

import matteroverdrive.registry.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Tritanium armour with a small defensive bonus when the complete set is worn. */
public class TritaniumArmorItem extends ArmorItem {
    public TritaniumArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && entity instanceof Player player && wearingFullSet(player)) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, 40, 0, true, false, false));
        }
    }

    private static boolean wearingFullSet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.get("tritanium_helmet").get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.get("tritanium_chestplate").get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.get("tritanium_leggings").get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.get("tritanium_boots").get());
    }
}
