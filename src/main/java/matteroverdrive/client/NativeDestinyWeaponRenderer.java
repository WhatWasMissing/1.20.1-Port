package matteroverdrive.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import matteroverdrive.item.weapon.NativeDestinyWeaponItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Native Matter Overdrive renderer for the imported skeletal Destiny weapon models. */
public final class NativeDestinyWeaponRenderer extends BlockEntityWithoutLevelRenderer {
    public NativeDestinyWeaponRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof NativeDestinyWeaponItem weapon)) return;
        NativeDestinyVisualLibrary.WeaponVisual visual = NativeDestinyVisualLibrary.get(weapon.profile().id());
        if (visual == null) return;

        poseStack.pushPose();
        applyDisplayTransform(poseStack, displayContext, visual.displayTransform());
        visual.apply(stack, Minecraft.getInstance().getFrameTime());
        VertexConsumer vertices = buffer.getBuffer(RenderType.entityCutoutNoCull(visual.texture()));
        visual.root().render(poseStack, vertices, packedLight, packedOverlay);
        poseStack.popPose();
    }

    /**
     * The supplied Destiny models are Bedrock/Blockbench pixel geometry. Point Blank's
     * separate-transforms model applies these values before rendering the geometry; the
     * native vanilla renderer must do the same because it owns the complete draw call.
     */
    private static void applyDisplayTransform(PoseStack poseStack, ItemDisplayContext context,
                                              NativeDestinyVisualLibrary.DisplayTransform displayTransform) {
        NativeDestinyVisualLibrary.DisplayTransform.Entry transform = displayTransform.forContext(context);
        poseStack.translate(transform.x() / 16.0D, transform.y() / 16.0D, transform.z() / 16.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(transform.xRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(transform.yRot()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(transform.zRot()));
        poseStack.scale(transform.scale(), transform.scale(), transform.scale());
    }
}
