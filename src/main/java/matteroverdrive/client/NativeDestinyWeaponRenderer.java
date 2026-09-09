package matteroverdrive.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import matteroverdrive.item.weapon.NativeDestinyWeaponItem;
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

    public NativeDestinyWeaponRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        renderNative(stack, poseStack, buffer, packedLight, packedOverlay);
    }

    /** Renderer 2.0 entry point. The caller owns camera/ADS/recoil transforms. */
    public boolean renderNative(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer,
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
        visual.apply(stack, Minecraft.getInstance().getFrameTime());
        VertexConsumer vertices = buffer.getBuffer(RenderType.entityCutoutNoCull(visual.texture()));
        visual.root().render(poseStack, vertices, packedLight, packedOverlay);
        poseStack.popPose();
        return true;
    }
}
