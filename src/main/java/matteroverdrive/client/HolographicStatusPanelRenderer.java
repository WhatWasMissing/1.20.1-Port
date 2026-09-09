package matteroverdrive.client;

import com.mojang.blaze3d.vertex.PoseStack;
import matteroverdrive.blockentity.HolographicStatusPanelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** Floating full-bright facility telemetry above the status panel. */
public class HolographicStatusPanelRenderer implements BlockEntityRenderer<HolographicStatusPanelBlockEntity> {
    private final Font font;

    public HolographicStatusPanelRenderer(BlockEntityRendererProvider.Context context) { this.font = context.getFont(); }

    @Override
    public void render(HolographicStatusPanelBlockEntity panel, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        String text = panel.displayLine();
        poseStack.pushPose();
        poseStack.translate(0.5D, 1.35D, 0.5D);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        float x = -font.width(text) / 2.0F;
        int color = panel.severity() == 2 ? 0xFFFF5555 : panel.severity() == 1 ? 0xFFFFDD55 : 0xFF73D9E6;
        font.drawInBatch(text, x, 0.0F, color, false, poseStack.last().pose(), buffers,
                Font.DisplayMode.SEE_THROUGH, 0x60000000, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }
}