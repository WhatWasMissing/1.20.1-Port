package matteroverdrive.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import matteroverdrive.block.HoloSignBlock;
import matteroverdrive.blockentity.HoloSignBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;

/** Holographic text renderer for the legacy Holo Sign. */
public class HoloSignRenderer implements BlockEntityRenderer<HoloSignBlockEntity> {
    private static final float BASE_SCALE = 0.0125F;
    private static final int MAX_PIXEL_WIDTH = 120;
    private static final int LINE_HEIGHT = 9;

    public HoloSignRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HoloSignBlockEntity sign, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        String text = sign.getText();
        if (text == null || text.isBlank()) return;

        Font font = Minecraft.getInstance().font;
        String[] lines = text.split("\\n", -1);
        int widest = 1;
        for (String line : lines) widest = Math.max(widest, font.width(line));
        float autoScale = widest > MAX_PIXEL_WIDTH ? (float) MAX_PIXEL_WIDTH / (float) widest : 1.0F;
        float scale = BASE_SCALE * autoScale;
        Direction facing = sign.getBlockState().hasProperty(HoloSignBlock.FACING)
                ? sign.getBlockState().getValue(HoloSignBlock.FACING) : Direction.NORTH;

        poseStack.pushPose();
        switch (facing) {
            case NORTH -> poseStack.translate(0.5D, 0.52D, 0.866D);
            case SOUTH -> poseStack.translate(0.5D, 0.52D, 0.134D);
            case WEST -> poseStack.translate(0.866D, 0.52D, 0.5D);
            case EAST -> poseStack.translate(0.134D, 0.52D, 0.5D);
            default -> poseStack.translate(0.5D, 0.52D, 0.5D);
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        poseStack.scale(-scale, -scale, scale);
        Matrix4f matrix = poseStack.last().pose();
        float top = -((lines.length - 1) * LINE_HEIGHT) / 2.0F - 4.0F;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            float x = -font.width(line) / 2.0F;
            font.drawInBatch(line, x, top + i * LINE_HEIGHT, 0x66E8FF, false, matrix, bufferSource,
                    Font.DisplayMode.SEE_THROUGH, 0x40000000, LightTexture.FULL_BRIGHT);
        }
        poseStack.popPose();
    }
}
