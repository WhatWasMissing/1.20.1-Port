package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.entity.*;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class LegacyEntityRenderers {
    private LegacyEntityRenderers() {}
    public static final class RogueAndroidRenderer extends HumanoidMobRenderer<RogueAndroidEntity, ZombieModel<RogueAndroidEntity>> { private static final ResourceLocation T=ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID,"textures/entities/android.png"); public RogueAndroidRenderer(EntityRendererProvider.Context c){super(c,new ZombieModel<>(c.bakeLayer(ModelLayers.ZOMBIE)),0.5F);} @Override public ResourceLocation getTextureLocation(RogueAndroidEntity e){return T;} }
    public static final class RangedRogueAndroidRenderer extends HumanoidMobRenderer<RangedRogueAndroidEntity, ZombieModel<RangedRogueAndroidEntity>> { private static final ResourceLocation T=ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID,"textures/entities/android_ranged.png"); public RangedRogueAndroidRenderer(EntityRendererProvider.Context c){super(c,new ZombieModel<>(c.bakeLayer(ModelLayers.ZOMBIE)),0.5F);} @Override public ResourceLocation getTextureLocation(RangedRogueAndroidEntity e){return T;} }
    public static final class MutantScientistRenderer extends HumanoidMobRenderer<MutantScientistEntity, HumanoidModel<MutantScientistEntity>> { private static final ResourceLocation T=ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID,"textures/entities/hulking_scinetist.png"); public MutantScientistRenderer(EntityRendererProvider.Context c){super(c,new HumanoidModel<>(c.bakeLayer(ModelLayers.PLAYER)),0.9F);} @Override public ResourceLocation getTextureLocation(MutantScientistEntity e){return T;} }
    public static final class DroneRenderer extends MobRenderer<DroneEntity, DroneModel> {
        private static final ResourceLocation T = ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID,"textures/entities/drone_default.png");
        public DroneRenderer(EntityRendererProvider.Context c) { super(c, new DroneModel(c.bakeLayer(LegacyEntityClientEvents.DRONE_LAYER)), 0.5F); }
        @Override public ResourceLocation getTextureLocation(DroneEntity e) { return T; }
    }
    public static final class MadScientistRenderer extends MobRenderer<MadScientistEntity,VillagerModel<MadScientistEntity>> { private static final ResourceLocation T=ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID,"textures/entities/mad_scientist.png"); public MadScientistRenderer(EntityRendererProvider.Context c){super(c,new VillagerModel<>(c.bakeLayer(ModelLayers.VILLAGER)),0.5F);} @Override public ResourceLocation getTextureLocation(MadScientistEntity e){return T;} }
    public static final class FailedCowRenderer extends MobRenderer<FailedCowEntity,CowModel<FailedCowEntity>> { private static final ResourceLocation T=ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID,"textures/entities/failed_cow.png"); public FailedCowRenderer(EntityRendererProvider.Context c){super(c,new CowModel<>(c.bakeLayer(ModelLayers.COW)),0.7F);} @Override public ResourceLocation getTextureLocation(FailedCowEntity e){return T;} }
    public static final class FailedPigRenderer extends MobRenderer<FailedPigEntity,PigModel<FailedPigEntity>> { private static final ResourceLocation T=ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID,"textures/entities/failed_pig.png"); public FailedPigRenderer(EntityRendererProvider.Context c){super(c,new PigModel<>(c.bakeLayer(ModelLayers.PIG)),0.7F);} @Override public ResourceLocation getTextureLocation(FailedPigEntity e){return T;} }
    public static final class FailedSheepRenderer extends MobRenderer<FailedSheepEntity,SheepModel<FailedSheepEntity>> { private static final ResourceLocation T=ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID,"textures/entities/failed_sheep.png"); public FailedSheepRenderer(EntityRendererProvider.Context c){super(c,new SheepModel<>(c.bakeLayer(ModelLayers.SHEEP)),0.7F);} @Override public ResourceLocation getTextureLocation(FailedSheepEntity e){return T;} }
    public static final class FailedChickenRenderer extends MobRenderer<FailedChickenEntity,ChickenModel<FailedChickenEntity>> { private static final ResourceLocation T=ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID,"textures/entities/failed_chicken.png"); public FailedChickenRenderer(EntityRendererProvider.Context c){super(c,new ChickenModel<>(c.bakeLayer(ModelLayers.CHICKEN)),0.3F);} @Override public ResourceLocation getTextureLocation(FailedChickenEntity e){return T;} }
}
