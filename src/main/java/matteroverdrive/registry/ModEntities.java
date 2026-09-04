package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.entity.FailedChickenEntity;
import matteroverdrive.entity.FailedCowEntity;
import matteroverdrive.entity.FailedPigEntity;
import matteroverdrive.entity.FailedSheepEntity;
import matteroverdrive.entity.MadScientistEntity;
import matteroverdrive.entity.RogueAndroidEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Legacy Matter Overdrive entities restored as real 1.20.1 entity types. */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MatterOverdrive.MOD_ID);

    public static final RegistryObject<EntityType<RogueAndroidEntity>> ROGUE_ANDROID = ENTITY_TYPES.register(
            "rogue_android", () -> EntityType.Builder.of(RogueAndroidEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F).clientTrackingRange(10).build(MatterOverdrive.MOD_ID + ":rogue_android"));

    public static final RegistryObject<EntityType<MadScientistEntity>> MAD_SCIENTIST = ENTITY_TYPES.register(
            "mad_scientist", () -> EntityType.Builder.of(MadScientistEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(MatterOverdrive.MOD_ID + ":mad_scientist"));

    public static final RegistryObject<EntityType<FailedCowEntity>> FAILED_COW = ENTITY_TYPES.register(
            "failed_cow", () -> EntityType.Builder.of(FailedCowEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 1.4F).clientTrackingRange(8).build(MatterOverdrive.MOD_ID + ":failed_cow"));

    public static final RegistryObject<EntityType<FailedPigEntity>> FAILED_PIG = ENTITY_TYPES.register(
            "failed_pig", () -> EntityType.Builder.of(FailedPigEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 0.9F).clientTrackingRange(8).build(MatterOverdrive.MOD_ID + ":failed_pig"));

    public static final RegistryObject<EntityType<FailedSheepEntity>> FAILED_SHEEP = ENTITY_TYPES.register(
            "failed_sheep", () -> EntityType.Builder.of(FailedSheepEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 1.3F).clientTrackingRange(8).build(MatterOverdrive.MOD_ID + ":failed_sheep"));

    public static final RegistryObject<EntityType<FailedChickenEntity>> FAILED_CHICKEN = ENTITY_TYPES.register(
            "failed_chicken", () -> EntityType.Builder.of(FailedChickenEntity::new, MobCategory.CREATURE)
                    .sized(0.4F, 0.7F).clientTrackingRange(8).build(MatterOverdrive.MOD_ID + ":failed_chicken"));

    private ModEntities() {}
}
