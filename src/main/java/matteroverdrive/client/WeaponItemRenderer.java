package matteroverdrive.client;

import matteroverdrive.item.weapon.EnergyWeaponItem;
import matteroverdrive.item.weapon.WeaponModuleItem;
import matteroverdrive.item.weapon.WeaponSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import com.mojang.blaze3d.vertex.PoseStack;

public final class WeaponItemRenderer extends BlockEntityWithoutLevelRenderer {
    public WeaponItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack weapon, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        if (!(weapon.getItem() instanceof EnergyWeaponItem)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        var itemRenderer = minecraft.getItemRenderer();
        BakedModel weaponModel = itemRenderer.getModel(weapon, level, null, 0);
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

        itemRenderer.render(weapon, displayContext, leftHand, poseStack, buffer,
                packedLight, packedOverlay, weaponModel);

        // The original renderer mounted only visual optics. Barrels, colours and utility
        // modules affect weapon behaviour but were never rendered as floating item icons.
        ItemStack module = WeaponSystem.getModule(weapon, WeaponSystem.SIGHTS_SLOT);
        if (module.getItem() instanceof WeaponModuleItem moduleItem
                && (moduleItem.getEffect() == WeaponModuleItem.Effect.HOLO_SIGHTS
                || moduleItem.getEffect() == WeaponModuleItem.Effect.SNIPER_SCOPE)) {
            poseStack.pushPose();
            applyOpticMount(poseStack, weapon, moduleItem.getEffect());
            BakedModel moduleModel = itemRenderer.getModel(module, level, null, 0);
            itemRenderer.render(module, ItemDisplayContext.FIXED, false, poseStack, buffer,
                    packedLight, packedOverlay, moduleModel);
            poseStack.popPose();
        }
    }

    private static void applyOpticMount(PoseStack poseStack, ItemStack weapon,
                                        WeaponModuleItem.Effect effect) {
        double x = 0.0D;
        double y = 0.12D;
        double z = 0.20D;

        if (weapon.getItem() instanceof EnergyWeaponItem energyWeapon) {
            switch (energyWeapon.getWeaponType()) {
                case ION_SNIPER -> {
                    x = effect == WeaponModuleItem.Effect.HOLO_SIGHTS ? 0.18D : 0.0D;
                    y = effect == WeaponModuleItem.Effect.HOLO_SIGHTS ? 0.11D : 0.125D;
                    z = effect == WeaponModuleItem.Effect.HOLO_SIGHTS ? 0.20D : 0.30D;
                }
                case PHASER_RIFLE -> y = 0.123D;
                case PLASMA_SHOTGUN -> {
                    x = 0.21D;
                    y = 0.10D;
                }
                case PHASER -> { }
            }
        }

        poseStack.translate(x, y, z);
        float scale = effect == WeaponModuleItem.Effect.HOLO_SIGHTS ? 0.16F : 0.20F;
        poseStack.scale(scale, scale, scale);
    }
}
