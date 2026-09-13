package matteroverdrive.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.logging.LogUtils;
import matteroverdrive.item.weapon.NativeDestinyWeaponItem;
import matteroverdrive.item.weapon.NativeDestinyWeaponProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

/** Native Matter Overdrive renderer for the supplied skeletal Destiny weapon models. */
public final class NativeDestinyWeaponRenderer extends BlockEntityWithoutLevelRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> WARNED_MISSING = new HashSet<>();
    private static final Set<String> WARNED_RENDER = new HashSet<>();

    public NativeDestinyWeaponRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        renderNative(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
    }

    /** Renderer 2.0 entry point. The caller owns camera/ADS/recoil transforms. */
    public boolean renderNative(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer,
                                int packedLight, int packedOverlay) {
        return renderNative(stack, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, poseStack, buffer,
                packedLight, packedOverlay);
    }

    private boolean renderNative(ItemStack stack, ItemDisplayContext displayContext,
                                 PoseStack poseStack, MultiBufferSource buffer,
                                 int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof NativeDestinyWeaponItem weapon)) return false;
        NativeDestinyVisualLibrary.WeaponVisual visual = NativeDestinyVisualLibrary.get(weapon.profile().id());
        if (visual == null) {
            if (WARNED_MISSING.add(weapon.profile().id())) {
                LOGGER.error("Native Destiny visual {} is missing; check native Destiny visual data and textures",
                        weapon.profile().id());
            }
            return false;
        }

        poseStack.pushPose();
        if (isFirstPerson(displayContext)) applyFirstPersonPlacement(poseStack);
        applyDisplayTransform(poseStack, displayContext, visual.displayTransform());
        try {
            visual.apply(stack, Minecraft.getInstance().getFrameTime());
            VertexConsumer vertices = buffer.getBuffer(RenderType.entityCutoutNoCull(visual.texture()));
            visual.root().render(poseStack, vertices, packedLight, packedOverlay);
            if (WARNED_RENDER.add(weapon.profile().id())) {
                LOGGER.info("Native Destiny weapon rendered: {} texture={} ",
                        weapon.profile().id(), visual.texture());
            }
            return true;
        } catch (RuntimeException ex) {
            LOGGER.error("Native Destiny render failed for {}", weapon.profile().id(), ex);
            return false;
        } finally {
            poseStack.popPose();
        }
    }

    private static boolean isFirstPerson(ItemDisplayContext displayContext) {
        return displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }

    /**
     * Recreates the transforms that vanilla applies before an item model is rendered.
     * RenderHandEvent is fired before that stage, so omitting this anchor makes the
     * imported Bedrock meshes float high in the camera and appear rotated relative to
     * the hand. The authored Destiny transforms are stored in model pixels and therefore
     * follow the native mesh scale.
     */
    private static void applyFirstPersonPlacement(PoseStack poseStack) {
        // ItemInHandRenderer.applyItemArmTransform for the right hand.
        poseStack.translate(0.56D, -0.52D, -0.72D);

        // The native geometry is authored at Bedrock pixel scale. A quarter-size mesh
        // keeps the longest rifle/sniper within a normal Minecraft first-person frame.
        poseStack.scale(1.0F / 4.0F, 1.0F / 4.0F, 1.0F / 4.0F);

        // Per-weapon source translations/rotations are applied by applyDisplayTransform
        // immediately after this camera anchor. This keeps all imported weapons on the
        // same renderer path and avoids a second hand-authored transform table.
    }

    private static void applyDisplayTransform(PoseStack poseStack, ItemDisplayContext displayContext,
                                              NativeDestinyVisualLibrary.DisplayTransform displayTransform) {
        NativeDestinyVisualLibrary.DisplayTransform.Entry transform = displayTransform.forContext(displayContext);
        poseStack.translate(transform.x() / 16.0D, transform.y() / 16.0D, transform.z() / 16.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(transform.xRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(transform.yRot()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(transform.zRot()));
        poseStack.scale(transform.scale(), transform.scale(), transform.scale());
    }

}
