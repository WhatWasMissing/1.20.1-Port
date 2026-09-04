package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.entity.FailedChickenEntity;
import matteroverdrive.entity.FailedCowEntity;
import matteroverdrive.entity.FailedPigEntity;
import matteroverdrive.entity.FailedSheepEntity;
import matteroverdrive.entity.MadScientistEntity;
import matteroverdrive.entity.RogueAndroidEntity;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class LegacyEntityRenderers {
    private LegacyEntityRenderers() {}

    public static final class RogueAndroidRenderer
            extends HumanoidMobRenderer<RogueAndroidEntity, ZombieModel<RogueAndroidEntity>> {
        private static final ResourceLocation TEXTURE =
                new ResourceLocation(MatterOverdrive.MOD_ID, "textures/entities/android.png");
        public RogueAndroidRenderer(EntityRendererProvider.Context context) {
            super(context, new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        }
        @Override public ResourceLocation getTextureLocation(RogueAndroidEntity entity) { return TEXTURE; }
    }

    public static final class MadScientistRenderer
            extends MobRenderer<MadScientistEntity, VillagerModel<MadScientistEntity>> {
        private static final ResourceLocation TEXTURE =
                new ResourceLocation(MatterOverdrive.MOD_ID, "textures/entities/mad_scientist.png");
        public MadScientistRenderer(EntityRendererProvider.Context context) {
            super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        }
        @Override public ResourceLocation getTextureLocation(MadScientistEntity entity) { return TEXTURE; }
    }

    public static final class FailedCowRenderer
            extends MobRenderer<FailedCowEntity, CowModel<FailedCowEntity>> {
        private static final ResourceLocation TEXTURE =
                new ResourceLocation(MatterOverdrive.MOD_ID, "textures/entities/failed_cow.png");
        public FailedCowRenderer(EntityRendererProvider.Context context) {
            super(context, new CowModel<>(context.bakeLayer(ModelLayers.COW)), 0.7F);
        }
        @Override public ResourceLocation getTextureLocation(FailedCowEntity entity) { return TEXTURE; }
    }

    public static final class FailedPigRenderer
            extends MobRenderer<FailedPigEntity, PigModel<FailedPigEntity>> {
        private static final ResourceLocation TEXTURE =
                new ResourceLocation(MatterOverdrive.MOD_ID, "textures/entities/failed_pig.png");
        public FailedPigRenderer(EntityRendererProvider.Context context) {
            super(context, new PigModel<>(context.bakeLayer(ModelLayers.PIG)), 0.7F);
        }
        @Override public ResourceLocation getTextureLocation(FailedPigEntity entity) { return TEXTURE; }
    }

    public static final class FailedSheepRenderer
            extends MobRenderer<FailedSheepEntity, SheepModel<FailedSheepEntity>> {
        private static final ResourceLocation TEXTURE =
                new ResourceLocation(MatterOverdrive.MOD_ID, "textures/entities/failed_sheep.png");
        public FailedSheepRenderer(EntityRendererProvider.Context context) {
            super(context, new SheepModel<>(context.bakeLayer(ModelLayers.SHEEP)), 0.7F);
        }
        @Override public ResourceLocation getTextureLocation(FailedSheepEntity entity) { return TEXTURE; }
    }

    public static final class FailedChickenRenderer
            extends MobRenderer<FailedChickenEntity, ChickenModel<FailedChickenEntity>> {
        private static final ResourceLocation TEXTURE =
                new ResourceLocation(MatterOverdrive.MOD_ID, "textures/entities/failed_chicken.png");
        public FailedChickenRenderer(EntityRendererProvider.Context context) {
            super(context, new ChickenModel<>(context.bakeLayer(ModelLayers.CHICKEN)), 0.3F);
        }
        @Override public ResourceLocation getTextureLocation(FailedChickenEntity entity) { return TEXTURE; }
    }
}
