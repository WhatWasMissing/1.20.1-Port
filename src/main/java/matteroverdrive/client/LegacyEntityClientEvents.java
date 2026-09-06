package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.registry.ModEntities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LegacyEntityClientEvents {
    public static final ModelLayerLocation DRONE_LAYER = new ModelLayerLocation(
            new ResourceLocation(MatterOverdrive.MOD_ID, "drone"), "main");

    private LegacyEntityClientEvents() {}

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DRONE_LAYER, DroneModel::createBodyLayer);
    }

    @SubscribeEvent public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
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
