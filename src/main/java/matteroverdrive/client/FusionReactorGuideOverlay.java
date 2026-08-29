package matteroverdrive.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import matteroverdrive.block.FusionReactorControllerBlock;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = matteroverdrive.MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FusionReactorGuideOverlay {
    private static final int[] LATERAL = {0, 1, 2, 3, 4, 5, 5, 5, 5, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -5, -5, -5, -5, -4, -3, -2, -1};
    private static final int[] FORWARD = {5, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 0};
    private static final int[] TYPES = {255, 2, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 2};

    private FusionReactorGuideOverlay() {}

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || (!player.getMainHandItem().is(ModItems.get("reactor_assembly_guide").get())
                && !player.getOffhandItem().is(ModItems.get("reactor_assembly_guide").get()))) return;
        if (!(player.pick(8.0D, event.getPartialTick(), false) instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) return;
        BlockPos controller = hit.getBlockPos();
        if (!minecraft.level.getBlockState(controller).is(ModBlocks.get("fusion_reactor_controller").get())) return;
        Direction forward = minecraft.level.getBlockState(controller).getValue(FusionReactorControllerBlock.FACING);
        Direction right = forward.getClockWise();
        PoseStack pose = event.getPoseStack();
        Vec3Camera camera = new Vec3Camera(event);
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        for (int i = 0; i < LATERAL.length; i++) {
            BlockPos target = controller.relative(forward, FORWARD[i]).relative(right, LATERAL[i]);
            BlockState state = minecraft.level.getBlockState(target);
            float[] color = colour(minecraft.level, target, state, TYPES[i]);
            LevelRenderer.renderLineBox(pose, lines, new AABB(target), color[0], color[1], color[2], 0.9F);
        }
        for (int offset = -3; offset <= 3; offset++) {
            BlockPos anomaly = controller.relative(forward, 5).above(offset);
            LevelRenderer.renderLineBox(pose, lines, new AABB(anomaly), 0.25F, 0.9F, 1.0F, offset == 0 ? 1.0F : 0.35F);
        }
        pose.popPose();
        buffers.endBatch(RenderType.lines());
    }

    private static float[] colour(Level level, BlockPos pos, BlockState state, int type) {
        if (state.isAir()) return new float[]{1.0F, 0.85F, 0.15F};
        Block block = state.getBlock();
        boolean valid = type == 0 && block == ModBlocks.get("machine_hull").get()
                || type == 1 && (block == ModBlocks.get("fusion_reactor_coil").get() || block == ModBlocks.get("fusion_reactor_io").get())
                || type == 2 && (block == ModBlocks.get("machine_hull").get() || block == ModBlocks.get("fusion_reactor_coil").get() || block == ModBlocks.get("fusion_reactor_io").get() || block == ModBlocks.get("decomposer").get());
        return valid ? new float[]{0.25F, 1.0F, 0.35F} : new float[]{1.0F, 0.2F, 0.2F};
    }

    private record Vec3Camera(double x, double y, double z) {
        Vec3Camera(RenderLevelStageEvent event) { this(event.getCamera().getPosition().x, event.getCamera().getPosition().y, event.getCamera().getPosition().z); }
    }
}
