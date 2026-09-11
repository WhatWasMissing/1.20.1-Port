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
 * Native, chunk-safe expedition structures added by the lead-development expansion.
 * Each start is assembled from independent room pieces; no piece force-loads or
 * writes outside Minecraft's current chunk clipping box.
 */
public final class FrontierSiteStructure extends Structure {
    private static final Logger LOGGER = LogUtils.getLogger();

    public enum Kind {
        DEEP_MATTER_VAULT,
        AUTONOMOUS_DRONE_FOUNDRY,
        ANOMALY_QUARANTINE_SITE,
        ORBITAL_RECOVERY_ARRAY
    }

    private final Kind kind;

    public FrontierSiteStructure(StructureSettings settings, Kind kind) {
        super(settings);
        this.kind = kind;
    }

    public static Codec<FrontierSiteStructure> codec(Kind kind) {
        return simpleCodec(settings -> new FrontierSiteStructure(settings, kind));
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        int surfaceY = context.chunkGenerator().getBaseHeight(
                x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int minimum = context.heightAccessor().getMinBuildHeight() + 18;
        int y = switch (kind) {
            case DEEP_MATTER_VAULT -> Math.max(minimum, surfaceY - 22);
            case ANOMALY_QUARANTINE_SITE -> Math.max(minimum, surfaceY - 8);
            case AUTONOMOUS_DRONE_FOUNDRY, ORBITAL_RECOVERY_ARRAY -> surfaceY;
        };
        int layout = Math.floorMod(context.chunkPos().x * 1103515245 ^ context.chunkPos().z * 12345 ^ kind.ordinal() * 7919, 3);
        BlockPos origin = new BlockPos(x, y, z);
        LOGGER.debug("M2 FRONTIER TRACE: scheduled kind={} chunk={} origin={} layout={}", kind, context.chunkPos(), origin, layout);
        return Optional.of(new GenerationStub(origin,
                builder -> FrontierSitePiece.assemble(builder, kind, origin, layout)));
    }

    @Override
    public StructureType<?> type() {
        return switch (kind) {
            case DEEP_MATTER_VAULT -> ModStructures.DEEP_MATTER_VAULT.get();
            case AUTONOMOUS_DRONE_FOUNDRY -> ModStructures.AUTONOMOUS_DRONE_FOUNDRY.get();
            case ANOMALY_QUARANTINE_SITE -> ModStructures.ANOMALY_QUARANTINE_SITE.get();
            case ORBITAL_RECOVERY_ARRAY -> ModStructures.ORBITAL_RECOVERY_ARRAY.get();
        };
    }
}
