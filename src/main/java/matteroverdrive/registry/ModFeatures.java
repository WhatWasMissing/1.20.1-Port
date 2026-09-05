package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.worldgen.GravitationalAnomalyFeature;
import matteroverdrive.worldgen.LegacyStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, MatterOverdrive.MOD_ID);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CRASHED_SHIP = FEATURES.register("crashed_ship", () -> new LegacyStructureFeature(NoneFeatureConfiguration.CODEC, LegacyStructureFeature.Kind.CRASHED_SHIP));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CARGO_SHIP = FEATURES.register("cargo_ship", () -> new LegacyStructureFeature(NoneFeatureConfiguration.CODEC, LegacyStructureFeature.Kind.CARGO_SHIP));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> UNDERWATER_BASE = FEATURES.register("underwater_base", () -> new LegacyStructureFeature(NoneFeatureConfiguration.CODEC, LegacyStructureFeature.Kind.UNDERWATER_BASE));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> MAD_SCIENTIST_HOUSE = FEATURES.register("mad_scientist_house", () -> new LegacyStructureFeature(NoneFeatureConfiguration.CODEC, LegacyStructureFeature.Kind.MAD_SCIENTIST_HOUSE));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ANDROID_HOUSE = FEATURES.register("android_house", () -> new LegacyStructureFeature(NoneFeatureConfiguration.CODEC, LegacyStructureFeature.Kind.ANDROID_HOUSE));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SAND_PIT = FEATURES.register("sand_pit", () -> new LegacyStructureFeature(NoneFeatureConfiguration.CODEC, LegacyStructureFeature.Kind.SAND_PIT));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GRAVITATIONAL_ANOMALY = FEATURES.register("gravitational_anomaly_worldgen", () -> new GravitationalAnomalyFeature(NoneFeatureConfiguration.CODEC));
    private ModFeatures() {}
}
