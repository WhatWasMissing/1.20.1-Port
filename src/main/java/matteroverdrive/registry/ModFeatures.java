package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.worldgen.GravitationalAnomalyFeature;
import matteroverdrive.worldgen.LegacyParityStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, MatterOverdrive.MOD_ID);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CRASHED_SHIP = FEATURES.register("crashed_ship", () -> new LegacyParityStructureFeature(NoneFeatureConfiguration.CODEC, LegacyParityStructureFeature.Kind.CRASHED_SHIP));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CARGO_SHIP = FEATURES.register("cargo_ship", () -> new LegacyParityStructureFeature(NoneFeatureConfiguration.CODEC, LegacyParityStructureFeature.Kind.CARGO_SHIP));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> UNDERWATER_BASE = FEATURES.register("underwater_base", () -> new LegacyParityStructureFeature(NoneFeatureConfiguration.CODEC, LegacyParityStructureFeature.Kind.UNDERWATER_BASE));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> MAD_SCIENTIST_HOUSE = FEATURES.register("mad_scientist_house", () -> new LegacyParityStructureFeature(NoneFeatureConfiguration.CODEC, LegacyParityStructureFeature.Kind.MAD_SCIENTIST_HOUSE));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ANDROID_HOUSE = FEATURES.register("android_house", () -> new LegacyParityStructureFeature(NoneFeatureConfiguration.CODEC, LegacyParityStructureFeature.Kind.ANDROID_HOUSE));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SAND_PIT = FEATURES.register("sand_pit", () -> new LegacyParityStructureFeature(NoneFeatureConfiguration.CODEC, LegacyParityStructureFeature.Kind.SAND_PIT));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GRAVITATIONAL_ANOMALY = FEATURES.register("gravitational_anomaly_worldgen", () -> new GravitationalAnomalyFeature(NoneFeatureConfiguration.CODEC));
    private ModFeatures() {}
}
