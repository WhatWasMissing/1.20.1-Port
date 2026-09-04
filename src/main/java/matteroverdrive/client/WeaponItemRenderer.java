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

        for (int slot = WeaponSystem.COLOR_SLOT; slot < WeaponSystem.MODULE_SLOT_COUNT; slot++) {
            ItemStack module = WeaponSystem.getModule(weapon, slot);
            if (module.isEmpty() || !(module.getItem() instanceof WeaponModuleItem moduleItem)
                    || moduleItem.getEffect() == WeaponModuleItem.Effect.COLOR) {
                continue;
            }

            poseStack.pushPose();
            applyMountTransform(poseStack, displayContext, slot);
            BakedModel moduleModel = itemRenderer.getModel(module, level, null, 0);
            itemRenderer.render(module, displayContext, leftHand, poseStack, buffer,
                    packedLight, packedOverlay, moduleModel);
            poseStack.popPose();
        }
    }

    private static void applyMountTransform(PoseStack poseStack,
                                             ItemDisplayContext context, int slot) {
        boolean firstPerson = context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        float scale = firstPerson ? 0.28F : 0.20F;

        if (slot == WeaponSystem.SIGHTS_SLOT) {
            poseStack.translate(0.0D, 0.0D, 0.24D);
            poseStack.scale(scale, scale, scale);
        } else if (slot == WeaponSystem.BARREL_SLOT) {
            poseStack.translate(0.0D, 0.0D, 0.16D);
            poseStack.scale(scale, scale, scale);
        } else {
            poseStack.translate(0.0D, 0.0D, 0.08D);
            poseStack.scale(scale, scale, scale);
        }
    }
}
