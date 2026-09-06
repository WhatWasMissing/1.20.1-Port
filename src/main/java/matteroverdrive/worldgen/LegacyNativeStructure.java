package matteroverdrive.worldgen;

import com.mojang.serialization.Codec;
import matteroverdrive.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public final class LegacyNativeStructure extends Structure {
    private final LegacyParityStructureFeature.Kind kind;

    public LegacyNativeStructure(StructureSettings settings, LegacyParityStructureFeature.Kind kind) {
        super(settings);
        this.kind = kind;
    }

    public static Codec<LegacyNativeStructure> codec(LegacyParityStructureFeature.Kind kind) {
        return simpleCodec(settings -> new LegacyNativeStructure(settings, kind));
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        Heightmap.Types heightmap = kind == LegacyParityStructureFeature.Kind.UNDERWATER_BASE
                ? Heightmap.Types.OCEAN_FLOOR_WG
                : Heightmap.Types.WORLD_SURFACE_WG;
        int surfaceY = context.chunkGenerator().getBaseHeight(x, z, heightmap, context.heightAccessor(), context.randomState());
        int y = switch (kind) {
            case CARGO_SHIP -> Math.max(86, surfaceY + 24);
            case SAND_PIT -> surfaceY;
            default -> surfaceY;
        };
        BlockPos origin = new BlockPos(x, y, z);
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new LegacyNativeStructurePiece(kind, origin))));
    }

    @Override
    public StructureType<?> type() {
        return switch (kind) {
            case CRASHED_SHIP -> ModStructures.CRASHED_SHIP.get();
            case CARGO_SHIP -> ModStructures.CARGO_SHIP.get();
            case UNDERWATER_BASE -> ModStructures.UNDERWATER_BASE.get();
            case MAD_SCIENTIST_HOUSE -> ModStructures.MAD_SCIENTIST_HOUSE.get();
            case ANDROID_HOUSE -> ModStructures.ANDROID_HOUSE.get();
            case SAND_PIT -> ModStructures.SAND_PIT.get();
        };
    }
}
