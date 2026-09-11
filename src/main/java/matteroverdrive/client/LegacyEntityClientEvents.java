package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.registry.ModEntities;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LegacyEntityClientEvents {
    public static final ModelLayerLocation DRONE_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, "drone"), "main");

    /**
     * Legacy Android textures do not use Minecraft's modern 64x64 zombie skin atlas.
     * Keep the original atlas sizes from Matter Overdrive so UVs line up correctly:
     * melee Android = 64x32, ranged Android = 96x64.
     */
    public static final ModelLayerLocation ROGUE_ANDROID_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, "rogue_android"), "main");
    public static final ModelLayerLocation RANGED_ANDROID_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, "ranged_rogue_android"), "main");

    private LegacyEntityClientEvents() {}

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DRONE_LAYER, DroneModel::createBodyLayer);
        event.registerLayerDefinition(ROGUE_ANDROID_LAYER,
                () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 32));
        event.registerLayerDefinition(RANGED_ANDROID_LAYER,
                () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 96, 64));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ROGUE_ANDROID.get(), LegacyEntityRenderers.RogueAndroidRenderer::new);
        event.registerEntityRenderer(ModEntities.RANGED_ROGUE_ANDROID.get(), LegacyEntityRenderers.RangedRogueAndroidRenderer::new);
        event.registerEntityRenderer(ModEntities.DRONE.get(), LegacyEntityRenderers.DroneRenderer::new);
        event.registerEntityRenderer(ModEntities.MUTANT_SCIENTIST.get(), LegacyEntityRenderers.MutantScientistRenderer::new);
        event.registerEntityRenderer(ModEntities.MAD_SCIENTIST.get(), LegacyEntityRenderers.MadScientistRenderer::new);
        event.registerEntityRenderer(ModEntities.FAILED_COW.get(), LegacyEntityRenderers.FailedCowRenderer::new);
        event.registerEntityRenderer(ModEntities.FAILED_PIG.get(), LegacyEntityRenderers.FailedPigRenderer::new);
        event.registerEntityRenderer(ModEntities.FAILED_SHEEP.get(), LegacyEntityRenderers.FailedSheepRenderer::new);
        event.registerEntityRenderer(ModEntities.FAILED_CHICKEN.get(), LegacyEntityRenderers.FailedChickenRenderer::new);
    }
}
