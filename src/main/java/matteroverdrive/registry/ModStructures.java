package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.worldgen.FacilityInfrastructurePiece;
import matteroverdrive.worldgen.FacilityTerrainPiece;
import matteroverdrive.worldgen.FrontierSitePiece;
import matteroverdrive.worldgen.FrontierSiteStructure;
import matteroverdrive.worldgen.LegacyNativeStructure;
import matteroverdrive.worldgen.LegacyNativeStructurePiece;
import matteroverdrive.worldgen.LegacyParityStructureFeature;
import matteroverdrive.worldgen.TechnologyFacilityStructure;
import matteroverdrive.worldgen.TechnologyFacilityStructurePiece;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, MatterOverdrive.MOD_ID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, MatterOverdrive.MOD_ID);

    public static final RegistryObject<StructureType<LegacyNativeStructure>> CRASHED_SHIP = type("crashed_ship", LegacyParityStructureFeature.Kind.CRASHED_SHIP);
    public static final RegistryObject<StructureType<LegacyNativeStructure>> CARGO_SHIP = type("cargo_ship", LegacyParityStructureFeature.Kind.CARGO_SHIP);
    public static final RegistryObject<StructureType<LegacyNativeStructure>> UNDERWATER_BASE = type("underwater_base", LegacyParityStructureFeature.Kind.UNDERWATER_BASE);
    public static final RegistryObject<StructureType<LegacyNativeStructure>> MAD_SCIENTIST_HOUSE = type("mad_scientist_house", LegacyParityStructureFeature.Kind.MAD_SCIENTIST_HOUSE);
    public static final RegistryObject<StructureType<LegacyNativeStructure>> ANDROID_HOUSE = type("android_house", LegacyParityStructureFeature.Kind.ANDROID_HOUSE);
    public static final RegistryObject<StructureType<LegacyNativeStructure>> SAND_PIT = type("sand_pit", LegacyParityStructureFeature.Kind.SAND_PIT);

    public static final RegistryObject<StructureType<TechnologyFacilityStructure>> SYNTHETIC_MANUFACTURING_PLANT = facilityType("synthetic_manufacturing_plant", TechnologyFacilityStructure.Kind.SYNTHETIC_MANUFACTURING_PLANT);
    public static final RegistryObject<StructureType<TechnologyFacilityStructure>> MATTER_REFINERY = facilityType("matter_refinery", TechnologyFacilityStructure.Kind.MATTER_REFINERY);
    public static final RegistryObject<StructureType<TechnologyFacilityStructure>> QUANTUM_RELAY_STATION = facilityType("quantum_relay_station", TechnologyFacilityStructure.Kind.QUANTUM_RELAY_STATION);
    public static final RegistryObject<StructureType<TechnologyFacilityStructure>> ANDROID_COMMAND_BUNKER = facilityType("android_command_bunker", TechnologyFacilityStructure.Kind.ANDROID_COMMAND_BUNKER);
    public static final RegistryObject<StructureType<TechnologyFacilityStructure>> FUSION_RESEARCH_COMPLEX = facilityType("fusion_research_complex", TechnologyFacilityStructure.Kind.FUSION_RESEARCH_COMPLEX);
    public static final RegistryObject<StructureType<TechnologyFacilityStructure>> BLACK_SITE = facilityType("black_site", TechnologyFacilityStructure.Kind.BLACK_SITE);

    public static final RegistryObject<StructureType<FrontierSiteStructure>> DEEP_MATTER_VAULT = frontierType("deep_matter_vault", FrontierSiteStructure.Kind.DEEP_MATTER_VAULT);
    public static final RegistryObject<StructureType<FrontierSiteStructure>> AUTONOMOUS_DRONE_FOUNDRY = frontierType("autonomous_drone_foundry", FrontierSiteStructure.Kind.AUTONOMOUS_DRONE_FOUNDRY);
    public static final RegistryObject<StructureType<FrontierSiteStructure>> ANOMALY_QUARANTINE_SITE = frontierType("anomaly_quarantine_site", FrontierSiteStructure.Kind.ANOMALY_QUARANTINE_SITE);
    public static final RegistryObject<StructureType<FrontierSiteStructure>> ORBITAL_RECOVERY_ARRAY = frontierType("orbital_recovery_array", FrontierSiteStructure.Kind.ORBITAL_RECOVERY_ARRAY);

    public static final RegistryObject<StructurePieceType> LEGACY_NATIVE_PIECE = STRUCTURE_PIECES.register("legacy_native", () -> LegacyNativeStructurePiece::new);
    public static final RegistryObject<StructurePieceType> TECHNOLOGY_FACILITY_PIECE = STRUCTURE_PIECES.register("technology_facility", () -> TechnologyFacilityStructurePiece::new);
    public static final RegistryObject<StructurePieceType> FACILITY_INFRASTRUCTURE_PIECE = STRUCTURE_PIECES.register("facility_infrastructure", () -> FacilityInfrastructurePiece::new);
    public static final RegistryObject<StructurePieceType> FACILITY_TERRAIN_PIECE = STRUCTURE_PIECES.register("facility_terrain", () -> FacilityTerrainPiece::new);
    public static final RegistryObject<StructurePieceType> FRONTIER_SITE_PIECE = STRUCTURE_PIECES.register("frontier_site", () -> FrontierSitePiece::new);

    private static RegistryObject<StructureType<LegacyNativeStructure>> type(String id, LegacyParityStructureFeature.Kind kind) {
        return STRUCTURE_TYPES.register(id, () -> () -> LegacyNativeStructure.codec(kind));
    }

    private static RegistryObject<StructureType<TechnologyFacilityStructure>> facilityType(String id, TechnologyFacilityStructure.Kind kind) {
        return STRUCTURE_TYPES.register(id, () -> () -> TechnologyFacilityStructure.codec(kind));
    }

    private static RegistryObject<StructureType<FrontierSiteStructure>> frontierType(String id, FrontierSiteStructure.Kind kind) {
        return STRUCTURE_TYPES.register(id, () -> () -> FrontierSiteStructure.codec(kind));
    }

    private ModStructures() {}
}
