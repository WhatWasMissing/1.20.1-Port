package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.worldgen.LegacyNativeStructure;
import matteroverdrive.worldgen.LegacyNativeStructurePiece;
import matteroverdrive.worldgen.LegacyParityStructureFeature;
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

    public static final RegistryObject<StructurePieceType> LEGACY_NATIVE_PIECE = STRUCTURE_PIECES.register("legacy_native", () -> LegacyNativeStructurePiece::new);

    private static RegistryObject<StructureType<LegacyNativeStructure>> type(String id, LegacyParityStructureFeature.Kind kind) {
        return STRUCTURE_TYPES.register(id, () -> () -> LegacyNativeStructure.codec(kind));
    }

    private ModStructures() {}
}
