package matteroverdrive.item;

import matteroverdrive.MatterOverdrive;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class SpacetimeEqualizerItem extends ArmorItem {
    public SpacetimeEqualizerItem(Properties properties) {
        super(TritaniumArmorMaterial.MATERIAL, Type.CHESTPLATE, properties);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return MatterOverdrive.MOD_ID + ":textures/armor/spacetime_equalizer_1.png";
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flags) {
        tooltip.add(Component.translatable("item.matteroverdrive.spacetime_equalizer.details"));
    }
}
