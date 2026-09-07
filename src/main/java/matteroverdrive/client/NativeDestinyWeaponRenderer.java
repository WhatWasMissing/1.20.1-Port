package matteroverdrive.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
        visual.apply(stack, Minecraft.getInstance().getFrameTime());
        VertexConsumer vertices = buffer.getBuffer(RenderType.entityCutoutNoCull(visual.texture()));
        visual.root().render(poseStack, vertices, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
