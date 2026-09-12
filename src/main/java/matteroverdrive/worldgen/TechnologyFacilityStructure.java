package matteroverdrive.worldgen;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import matteroverdrive.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Native multi-chunk structures for the post-parity Matter Overdrive technology
 * progression. Facilities are assembled from independent StructurePieces so
 * Minecraft clips generation to the active chunk instead of synchronously
 * stamping blocks into neighbouring chunks.
 */
public final class TechnologyFacilityStructure extends Structure {
    private static final Logger LOGGER = LogUtils.getLogger();

    public enum Kind {
        SYNTHETIC_MANUFACTURING_PLANT,
        MATTER_REFINERY,
        QUANTUM_RELAY_STATION,
        ANDROID_COMMAND_BUNKER,
        FUSION_RESEARCH_COMPLEX,
        BLACK_SITE
    }

    private final Kind kind;

    public TechnologyFacilityStructure(StructureSettings settings, Kind kind) {
        super(settings);
        this.kind = kind;
    }

    public static Codec<TechnologyFacilityStructure> codec(Kind kind) {
        return simpleCodec(settings -> new TechnologyFacilityStructure(settings, kind));
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        int surfaceY = context.chunkGenerator().getBaseHeight(
                x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

        int y = switch (kind) {
            case ANDROID_COMMAND_BUNKER -> Math.max(context.heightAccessor().getMinBuildHeight() + 16, surfaceY - 10);
            case BLACK_SITE -> Math.max(context.heightAccessor().getMinBuildHeight() + 20, surfaceY - 16);
            case FUSION_RESEARCH_COMPLEX -> Math.max(context.heightAccessor().getMinBuildHeight() + 12, surfaceY - 3);
            default -> surfaceY;
        };

        // Stable per-start-chunk variation. Do not consume mutable worldgen random here:
        // every StructurePiece must reconstruct the same layout after save/reload.
        int layout = Math.floorMod(context.chunkPos().x * 73428767 ^ context.chunkPos().z * 912931, 3);
        BlockPos origin = new BlockPos(x, y, z);
        LOGGER.debug("M2 FACILITY TRACE: scheduled kind={} chunk={} origin={} surfaceY={} layout={}",
                kind, context.chunkPos(), origin, surfaceY, layout);

        return Optional.of(new GenerationStub(origin, builder -> {
            TechnologyFacilityStructurePiece.assemble(builder, kind, origin, layout);
            FacilityInfrastructurePiece.assemble(builder, kind, origin, layout);
            FacilityTerrainPiece.assemble(builder, kind, origin, layout);
            // Population/dressing is an independent chunk-clipped pass added last so it cannot
            // compromise room traversal or reintroduce large synchronous structure stamping.
            FacilityPopulationPiece.assemble(builder, kind, origin, layout);
        }));
    }

    @Override
    public StructureType<?> type() {
        return switch (kind) {
            case SYNTHETIC_MANUFACTURING_PLANT -> ModStructures.SYNTHETIC_MANUFACTURING_PLANT.get();
            case MATTER_REFINERY -> ModStructures.MATTER_REFINERY.get();
            case QUANTUM_RELAY_STATION -> ModStructures.QUANTUM_RELAY_STATION.get();
            case ANDROID_COMMAND_BUNKER -> ModStructures.ANDROID_COMMAND_BUNKER.get();
            case FUSION_RESEARCH_COMPLEX -> ModStructures.FUSION_RESEARCH_COMPLEX.get();
            case BLACK_SITE -> ModStructures.BLACK_SITE.get();
        };
    }
}
