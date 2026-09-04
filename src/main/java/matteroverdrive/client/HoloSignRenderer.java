package matteroverdrive.client;

import com.mojang.blaze3d.vertex.PoseStack;
import matteroverdrive.blockentity.HoloSignBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix4f;

/** Holographic text renderer for the legacy Holo Sign. */
public class HoloSignRenderer implements BlockEntityRenderer<HoloSignBlockEntity> {
    private static final float BASE_SCALE = 0.025F;
    private static final int MAX_PIXEL_WIDTH = 180;

    public HoloSignRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HoloSignBlockEntity sign, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        String text = sign.getText();
        if (text == null || text.isBlank()) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        int width = Math.max(1, font.width(text));
        float autoScale = width > MAX_PIXEL_WIDTH ? (float) MAX_PIXEL_WIDTH / (float) width : 1.0F;
        float scale = BASE_SCALE * autoScale;

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.18D, 0.5D);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-scale, -scale, scale);
        Matrix4f matrix = poseStack.last().pose();
        float x = -font.width(text) / 2.0F;
        font.drawInBatch(text, x, 0.0F, 0x66E8FF, false, matrix, bufferSource,
                Font.DisplayMode.SEE_THROUGH, 0x40000000, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }
}
