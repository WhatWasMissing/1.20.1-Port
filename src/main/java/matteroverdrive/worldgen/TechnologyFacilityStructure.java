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

/** New worlds generate exploration-native facilities; old piece serializers remain registered for saves. */
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
    public TechnologyFacilityStructure(StructureSettings settings, Kind kind) { super(settings); this.kind = kind; }
    public static Codec<TechnologyFacilityStructure> codec(Kind kind) { return simpleCodec(settings -> new TechnologyFacilityStructure(settings, kind)); }

    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x=context.chunkPos().getMiddleBlockX(), z=context.chunkPos().getMiddleBlockZ();
        int surfaceY=context.chunkGenerator().getBaseHeight(x,z,Heightmap.Types.WORLD_SURFACE_WG,context.heightAccessor(),context.randomState());
        int y=switch(kind){
            case ANDROID_COMMAND_BUNKER -> Math.max(context.heightAccessor().getMinBuildHeight()+16,surfaceY-8);
            case BLACK_SITE -> Math.max(context.heightAccessor().getMinBuildHeight()+20,surfaceY-12);
            default -> surfaceY;
        };
        int layout=Math.floorMod(context.chunkPos().x*73428767 ^ context.chunkPos().z*912931,3);
        BlockPos origin=new BlockPos(x,y,z);
        LOGGER.debug("M2 FACILITY TRACE: exploration-native kind={} chunk={} origin={} surfaceY={} layout={}",kind,context.chunkPos(),origin,surfaceY,layout);
        return Optional.of(new GenerationStub(origin,builder -> builder.addPiece(new ModernExplorationStructurePiece(kind,origin,layout))));
    }

    @Override public StructureType<?> type(){return switch(kind){
        case SYNTHETIC_MANUFACTURING_PLANT -> ModStructures.SYNTHETIC_MANUFACTURING_PLANT.get();
        case MATTER_REFINERY -> ModStructures.MATTER_REFINERY.get();
        case QUANTUM_RELAY_STATION -> ModStructures.QUANTUM_RELAY_STATION.get();
        case ANDROID_COMMAND_BUNKER -> ModStructures.ANDROID_COMMAND_BUNKER.get();
        case FUSION_RESEARCH_COMPLEX -> ModStructures.FUSION_RESEARCH_COMPLEX.get();
        case BLACK_SITE -> ModStructures.BLACK_SITE.get();
    };}
}
