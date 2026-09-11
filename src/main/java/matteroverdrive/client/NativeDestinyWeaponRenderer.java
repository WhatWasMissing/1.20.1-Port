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
        if (isFirstPerson(displayContext)) {
            applyFirstPersonPlacement(poseStack, weapon.profile());
        } else {
            applyDisplayContextPlacement(poseStack, displayContext);
        }
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

    /** Keeps the native mesh attached to the player in third-person and world displays. */
    private static void applyDisplayContextPlacement(PoseStack poseStack,
                                                      ItemDisplayContext displayContext) {
        switch (displayContext) {
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                // Authored Destiny third-person transform: translation [0.25, -4.75, 1]
                // and scale [0.25, 0.25, 0.25], expressed in model pixels.
                poseStack.scale(0.25F, 0.25F, 0.25F);
                poseStack.translate(0.25D / 16.0D, -4.75D / 16.0D, 1.0D / 16.0D);
            }
            case GROUND -> {
                poseStack.scale(0.30F, 0.30F, 0.30F);
                poseStack.translate(0.0D, -6.0D / 16.0D, 3.5D / 16.0D);
            }
            case FIXED -> {
                poseStack.translate(-5.5D / 16.0D, -10.25D / 16.0D, -1.0D / 16.0D);
                poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                poseStack.scale(0.62F, 0.62F, 0.62F);
            }
            default -> {
                // GUI/icon rendering is supplied by the generated item icon model.
                poseStack.scale(0.37F, 0.37F, 0.37F);
            }
        }
    }

    /**
     * Recreates the transforms that vanilla applies before an item model is rendered.
     * RenderHandEvent is fired before that stage, so omitting this anchor makes the
     * imported Bedrock meshes float high in the camera and appear rotated relative to
     * the hand. The authored Destiny transforms are stored in model pixels and therefore
     * follow the native mesh scale.
     */
    private static void applyFirstPersonPlacement(PoseStack poseStack,
                                                   NativeDestinyWeaponProfile profile) {
        // ItemInHandRenderer.applyItemArmTransform for the right hand.
        poseStack.translate(0.56D, -0.52D, -0.72D);

        // The native geometry is authored at Bedrock pixel scale. A quarter-size mesh
        // keeps the longest rifle/sniper within a normal Minecraft first-person frame.
        poseStack.scale(1.0F / 4.0F, 1.0F / 4.0F, 1.0F / 4.0F);

        FirstPersonTransform transform = switch (profile) {
            case ACE_OF_SPADES, EYASLUNA, HAWKMOON, THE_LAST_WORD, THORN ->
                    new FirstPersonTransform(-1.0D, -19.0D, -12.0D, 2.0F);
            case CHAOS_DOGMA -> new FirstPersonTransform(2.5D, -25.0D, -13.0D, 1.0F);
            case KHVOSTOV_7G02 -> new FirstPersonTransform(1.75D, -26.0D, -5.75D, 2.0F);
            case MARSHAL_A1 -> new FirstPersonTransform(3.5D, -28.5D, -6.5D, 2.0F);
            case MIDA_MULTI_TOOL -> new FirstPersonTransform(2.0D, -25.5D, -20.5D, 1.0F);
            case MONTE_CARLO -> new FirstPersonTransform(2.5D, -28.0D, -5.75D, 2.0F);
            case PROXIMA_CENTAURI_II, TRAX_CALLUM_1 ->
                    new FirstPersonTransform(2.0D, -25.5D, -20.5D, 1.0F);
            case SLEEPER_SIMULANT -> new FirstPersonTransform(2.0D, -27.0D, -1.75D, 1.0F);
            case SUROS_REGIME -> new FirstPersonTransform(3.5D, -28.5D, -7.0D, 2.0F);
        };

        poseStack.translate(transform.x / 16.0D, transform.y / 16.0D, transform.z / 16.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(transform.roll));
    }

    private record FirstPersonTransform(double x, double y, double z, float roll) {}

}
