package matteroverdrive.worldgen;

import matteroverdrive.block.IndustrialDetailBlock;
import matteroverdrive.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Small, chunk-clipped terrain integration pieces for modern technology facilities.
 *
 * <p>These pieces never query or write outside the active chunk clip. They do not
 * flatten arbitrary terrain; instead they add bounded aprons, retaining skirts and
 * short support piers where a generated facility would otherwise appear to float
 * over a local drop. This keeps the native Structure/StructurePiece generation
 * model intact while making surface approaches read as deliberately engineered.</p>
 */
public final class FacilityTerrainPiece extends StructurePiece {
    public enum Kind {
        ENTRY_APRON_NORTH,
        SALVAGE_FOUNDATION,
        RELAY_MAST_FOUNDATION,
        BUNKER_APPROACH_SUPPORT,
        FUSION_APPROACH_SUPPORT,
        BLACK_HATCH_CROWN,
        BUNKER_ANDROID_LINK_Z
    }

    private final TechnologyFacilityStructure.Kind facility;
    private final Kind kind;
    private final BlockPos origin;

    public FacilityTerrainPiece(TechnologyFacilityStructure.Kind facility, Kind kind, BlockPos origin) {
        super(ModStructures.FACILITY_TERRAIN_PIECE.get(), 0, boxFor(kind, origin));
        this.facility = facility;
        this.kind = kind;
        this.origin = origin;
    }

    public FacilityTerrainPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.FACILITY_TERRAIN_PIECE.get(), tag);
        this.facility = TechnologyFacilityStructure.Kind.valueOf(tag.getString("MOFacility"));
        this.kind = Kind.valueOf(tag.getString("MOTerrain"));
        this.origin = new BlockPos(tag.getInt("MOX"), tag.getInt("MOY"), tag.getInt("MOZ"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("MOFacility", facility.name());
        tag.putString("MOTerrain", kind.name());
        tag.putInt("MOX", origin.getX());
        tag.putInt("MOY", origin.getY());
        tag.putInt("MOZ", origin.getZ());
    }

    public static void assemble(StructurePiecesBuilder builder, TechnologyFacilityStructure.Kind facility,
                                BlockPos c, int layout) {
        switch (facility) {
            case SYNTHETIC_MANUFACTURING_PLANT -> {
                add(builder, facility, Kind.ENTRY_APRON_NORTH, c.offset(0, 0, -25));
                if (layout != 0) add(builder, facility, Kind.SALVAGE_FOUNDATION, c.offset(34, 0, -18));
            }
            case MATTER_REFINERY -> {
                add(builder, facility, Kind.ENTRY_APRON_NORTH, c.offset(0, 0, -25));
                if (layout != 0) add(builder, facility, Kind.SALVAGE_FOUNDATION, c.offset(34, 0, -18));
            }
            case QUANTUM_RELAY_STATION -> {
                add(builder, facility, Kind.ENTRY_APRON_NORTH, c.offset(0, 0, -35));
                int d = layout == 1 ? -1 : 1;
                add(builder, facility, Kind.RELAY_MAST_FOUNDATION, c.offset(-18 * d, 5, 11));
                add(builder, facility, Kind.RELAY_MAST_FOUNDATION, c.offset(18 * d, 5, 11));
                if (layout != 0) add(builder, facility, Kind.SALVAGE_FOUNDATION, c.offset(34, 0, -18));
            }
            case ANDROID_COMMAND_BUNKER -> {
                add(builder, facility, Kind.BUNKER_APPROACH_SUPPORT, c.offset(0, 9, -33));
                if (layout == 1) add(builder, facility, Kind.BUNKER_ANDROID_LINK_Z, c.offset(-18, 0, 9));
                else if (layout == 2) add(builder, facility, Kind.BUNKER_ANDROID_LINK_Z, c.offset(18, 0, 9));
            }
            case FUSION_RESEARCH_COMPLEX -> {
                add(builder, facility, Kind.FUSION_APPROACH_SUPPORT, c.offset(0, 2, -31));
                if (layout != 0) add(builder, facility, Kind.SALVAGE_FOUNDATION, c.offset(34, 0, -18));
            }
            case BLACK_SITE -> add(builder, facility, Kind.BLACK_HATCH_CROWN, c.offset(0, 16, -27));
        }
    }

    private static void add(StructurePiecesBuilder builder, TechnologyFacilityStructure.Kind facility,
                            Kind kind, BlockPos origin) {
        builder.addPiece(new FacilityTerrainPiece(facility, kind, origin));
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        switch (kind) {
            case ENTRY_APRON_NORTH -> entryApron(level, chunkBox);
            case SALVAGE_FOUNDATION -> salvageFoundation(level, chunkBox);
            case RELAY_MAST_FOUNDATION -> relayMastFoundation(level, chunkBox);
            case BUNKER_APPROACH_SUPPORT -> compactApproach(level, chunkBox, 3);
            case FUSION_APPROACH_SUPPORT -> compactApproach(level, chunkBox, 4);
            case BLACK_HATCH_CROWN -> blackHatchCrown(level, chunkBox);
            case BUNKER_ANDROID_LINK_Z -> bunkerAndroidLink(level, chunkBox);
        }
    }

    private void entryApron(WorldGenLevel level, BoundingBox clip) {
        BlockState floor = facility == TechnologyFacilityStructure.Kind.MATTER_REFINERY
                ? mod("decorative.floor_tiles_green", Blocks.POLISHED_ANDESITE)
                : mod("decorative.floor_tiles", Blocks.SMOOTH_STONE);
        BlockState stripe = mod("decorative.tritanium_plate_stripe", Blocks.YELLOW_CONCRETE);
        BlockState support = mod("decorative.tritanium_plate", Blocks.IRON_BLOCK);
        BlockState rail = detail("industrial_railing", Direction.NORTH, Blocks.IRON_BARS);
        BlockState warning = detail("warning_light", Direction.NORTH, Blocks.REDSTONE_LAMP);

        for (int z = -4; z <= 4; z++) {
            for (int x = -4; x <= 4; x++) {
                BlockPos top = origin.offset(x, 0, z);
                BlockState surface = (Math.abs(x) == 4 || z == -4 || ((x + z) & 7) == 0) ? stripe : floor;
                supportedFloor(level, clip, top, surface, support, 7);
                if (Math.abs(x) == 4 && (z & 1) == 0) set(level, clip, top.above(), rail);
            }
        }
        set(level, clip, origin.offset(-4, 2, -3), warning);
        set(level, clip, origin.offset(4, 2, -3), warning);
    }

    private void salvageFoundation(WorldGenLevel level, BoundingBox clip) {
        BlockState surface = mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES);
        BlockState support = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        for (int x = -7; x <= 7; x++) {
            for (int z = -5; z <= 5; z++) {
                boolean aisle = Math.abs(x) <= 2;
                boolean edge = Math.abs(x) == 7 || Math.abs(z) == 5;
                if (aisle || edge || ((x * 13 + z * 7) & 3) == 0) {
                    supportedFloor(level, clip, origin.offset(x, 0, z), surface, support, 8);
                }
            }
        }
    }

    private void relayMastFoundation(WorldGenLevel level, BoundingBox clip) {
        BlockState pad = mod("decorative.tritanium_plate", Blocks.IRON_BLOCK);
        BlockState support = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        // Mast origins are five blocks above the main floor. Build a compact engineered plinth
        // down toward the facility level rather than leaving the tower visually unsupported.
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            set(level, clip, origin.offset(x, -1, z), pad);
            if (Math.abs(x) == 2 || Math.abs(z) == 2 || (x == 0 && z == 0)) {
                for (int y = -2; y >= -5; y--) set(level, clip, origin.offset(x, y, z), support);
            }
        }
    }

    private void compactApproach(WorldGenLevel level, BoundingBox clip, int halfWidth) {
        BlockState floor = mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES);
        BlockState support = facility == TechnologyFacilityStructure.Kind.BLACK_SITE
                ? mod("decorative.carbon_fiber_plate", Blocks.REINFORCED_DEEPSLATE)
                : mod("decorative.tritanium_plate", Blocks.POLISHED_DEEPSLATE);
        BlockState warning = detail("warning_light", Direction.NORTH, Blocks.REDSTONE_LAMP);

        for (int x = -halfWidth; x <= halfWidth; x++) for (int z = -2; z <= 2; z++) {
            supportedFloor(level, clip, origin.offset(x, 0, z), floor, support, 8);
        }
        set(level, clip, origin.offset(-halfWidth, 2, -1), warning);
        set(level, clip, origin.offset(halfWidth, 2, -1), warning);
    }

    private void blackHatchCrown(WorldGenLevel level, BoundingBox clip) {
        BlockState frame = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState dark = mod("decorative.carbon_fiber_plate", Blocks.REINFORCED_DEEPSLATE);
        BlockState warning = detail("warning_light", Direction.NORTH, Blocks.REDSTONE_LAMP);

        for (int x = -3; x <= 3; x++) for (int z = -2; z <= 4; z++) {
            boolean edge = Math.abs(x) == 3 || z == -2 || z == 4;
            if (edge) supportedFloor(level, clip, origin.offset(x, 0, z), dark, frame, 6);
        }
        for (int x : new int[]{-3, 3}) for (int z : new int[]{-2, 4}) {
            set(level, clip, origin.offset(x, 1, z), frame);
            set(level, clip, origin.offset(x, 2, z), frame);
        }
        set(level, clip, origin.offset(3, 2, 1), warning);
    }

    private void bunkerAndroidLink(WorldGenLevel level, BoundingBox clip) {
        BlockState wall = mod("decorative.tritanium_plate", Blocks.IRON_BLOCK);
        BlockState floor = mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES);
        BlockState frame = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState lamp = detail("warning_light", Direction.EAST, Blocks.REDSTONE_LAMP);
        for (int z = -5; z <= 5; z++) for (int x = -2; x <= 2; x++) {
            set(level, clip, origin.offset(x, 0, z), floor);
            for (int y = 1; y <= 4; y++) {
                boolean side = Math.abs(x) == 2;
                set(level, clip, origin.offset(x, y, z), side ? ((z % 4 == 0) ? frame : wall)
                        : Blocks.AIR.defaultBlockState());
            }
            set(level, clip, origin.offset(x, 5, z), (z % 4 == 0) ? frame : wall);
        }
        for (int z = -4; z <= 4; z += 4) set(level, clip, origin.offset(0, 4, z), lamp);
    }

    private void supportedFloor(WorldGenLevel level, BoundingBox clip, BlockPos top, BlockState surface,
                                BlockState support, int maxDepth) {
        set(level, clip, top, surface);
        for (int depth = 1; depth <= maxDepth; depth++) {
            BlockPos below = top.below(depth);
            if (!clip.isInside(below) || !getBoundingBox().isInside(below)) continue;
            BlockState existing = level.getBlockState(below);
            if (!existing.isAir() && existing.getFluidState().isEmpty()) break;
            set(level, clip, below, support);
        }
    }

    private BlockState detail(String id, Direction facing, Block fallback) {
        BlockState state = mod(id, fallback);
        if (state.getBlock() instanceof IndustrialDetailBlock) return state.setValue(IndustrialDetailBlock.FACING, facing);
        return state;
    }

    private static BoundingBox boxFor(Kind kind, BlockPos p) {
        return switch (kind) {
            case ENTRY_APRON_NORTH -> new BoundingBox(p.getX() - 4, p.getY() - 7, p.getZ() - 4,
                    p.getX() + 4, p.getY() + 2, p.getZ() + 4);
            case SALVAGE_FOUNDATION -> new BoundingBox(p.getX() - 7, p.getY() - 8, p.getZ() - 5,
                    p.getX() + 7, p.getY(), p.getZ() + 5);
            case RELAY_MAST_FOUNDATION -> new BoundingBox(p.getX() - 2, p.getY() - 5, p.getZ() - 2,
                    p.getX() + 2, p.getY() - 1, p.getZ() + 2);
            case BUNKER_APPROACH_SUPPORT -> new BoundingBox(p.getX() - 3, p.getY() - 8, p.getZ() - 2,
                    p.getX() + 3, p.getY() + 2, p.getZ() + 2);
            case FUSION_APPROACH_SUPPORT -> new BoundingBox(p.getX() - 4, p.getY() - 8, p.getZ() - 2,
                    p.getX() + 4, p.getY() + 2, p.getZ() + 2);
            case BLACK_HATCH_CROWN -> new BoundingBox(p.getX() - 3, p.getY() - 6, p.getZ() - 2,
                    p.getX() + 3, p.getY() + 2, p.getZ() + 4);
            case BUNKER_ANDROID_LINK_Z -> new BoundingBox(p.getX() - 2, p.getY(), p.getZ() - 5,
                    p.getX() + 2, p.getY() + 5, p.getZ() + 5);
        };
    }

    private static BlockState mod(String id, Block fallback) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("matteroverdrive", id));
        return block == null || block == Blocks.AIR ? fallback.defaultBlockState() : block.defaultBlockState();
    }

    private void set(WorldGenLevel level, BoundingBox clip, BlockPos pos, BlockState state) {
        if (!clip.isInside(pos) || !getBoundingBox().isInside(pos)) return;
        if (level.getBlockState(pos).is(Blocks.BEDROCK)) return;
        level.setBlock(pos, state, 2);
    }
}
