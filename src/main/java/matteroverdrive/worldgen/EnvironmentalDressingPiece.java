package matteroverdrive.worldgen;

import matteroverdrive.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Optional environmental-storytelling layer for the sixteen canonical facilities.
 *
 * This intentionally copies the safe mechanics used by the working native pieces:
 * persistent StructurePiece serialization, a fixed bounding box, chunk clipping, and
 * local block-state checks. It never creates entrances, stairs, pits, doors, machines,
 * loot or objectives. Every decoration is non-blocking and is only placed into an
 * already-open cell above solid support, so the mandatory route remains owned by the
 * validated structure piece underneath it.
 */
public final class EnvironmentalDressingPiece extends StructurePiece {
    private final String siteId;
    private final BlockPos origin;

    public EnvironmentalDressingPiece(String siteId, BlockPos origin) {
        super(ModStructures.ENVIRONMENTAL_DRESSING_PIECE.get(), 0, box(origin));
        this.siteId = siteId == null ? "" : siteId;
        this.origin = origin;
    }

    public EnvironmentalDressingPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.ENVIRONMENTAL_DRESSING_PIECE.get(), tag);
        this.siteId = tag.getString("MOSite");
        this.origin = new BlockPos(tag.getInt("MOX"), tag.getInt("MOY"), tag.getInt("MOZ"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("MOSite", siteId);
        tag.putInt("MOX", origin.getX());
        tag.putInt("MOY", origin.getY());
        tag.putInt("MOZ", origin.getZ());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox clip, ChunkPos chunkPos, BlockPos pivot) {
        switch (siteId) {
            case "sand_pit" -> warningRing(level, clip, Blocks.YELLOW_CARPET.defaultBlockState());
            case "deep_matter_vault" -> archiveTally(level, clip, Blocks.LIGHT_BLUE_CARPET.defaultBlockState());
            case "matter_refinery" -> localControlRoute(level, clip, Blocks.LIME_CARPET.defaultBlockState());
            case "cargo_ship" -> evacuationRoute(level, clip, Blocks.LIGHT_BLUE_CARPET.defaultBlockState());
            case "synthetic_manufacturing_plant" -> chassisBayMarks(level, clip, Blocks.WHITE_CARPET.defaultBlockState());
            case "underwater_base" -> evacuationRoute(level, clip, Blocks.CYAN_CARPET.defaultBlockState());
            case "quantum_relay_station" -> clockStations(level, clip, Blocks.PURPLE_CARPET.defaultBlockState());
            case "crashed_ship" -> evacuationRoute(level, clip, Blocks.BLUE_CARPET.defaultBlockState());
            case "mad_scientist_house" -> domesticWorkbench(level, clip, Blocks.BLUE_CARPET.defaultBlockState());
            case "anomaly_quarantine_site" -> quarantineMarks(level, clip, Blocks.MAGENTA_CARPET.defaultBlockState());
            case "android_house" -> sharedUtilityMarks(level, clip, Blocks.ORANGE_CARPET.defaultBlockState());
            case "android_command_bunker" -> manualIffRoute(level, clip, Blocks.ORANGE_CARPET.defaultBlockState());
            case "autonomous_drone_foundry" -> humanCorridor(level, clip, Blocks.LIGHT_BLUE_CARPET.defaultBlockState());
            case "fusion_research_complex" -> localScramMarks(level, clip, Blocks.YELLOW_CARPET.defaultBlockState());
            case "black_site" -> recordsProcessMarks(level, clip, Blocks.BLACK_CARPET.defaultBlockState());
            case "orbital_recovery_array" -> quarantineMarks(level, clip, Blocks.YELLOW_CARPET.defaultBlockState());
            default -> { }
        }
    }

    private void warningRing(WorldGenLevel level, BoundingBox clip, BlockState marker) {
        for (BlockPos offset : new BlockPos[]{new BlockPos(5,1,4),new BlockPos(-5,1,4),new BlockPos(5,1,-4),new BlockPos(-5,1,-4)})
            mark(level, clip, offset, marker);
        lamp(level, clip, new BlockPos(6,1,0));
    }

    private void archiveTally(WorldGenLevel level, BoundingBox clip, BlockState marker) {
        stripe(level, clip, -6, 1, -4, -6, 1, 4, marker);
        mark(level, clip, new BlockPos(6,1,4), Blocks.GRAY_CARPET.defaultBlockState());
        mark(level, clip, new BlockPos(6,1,-4), Blocks.GRAY_CARPET.defaultBlockState());
    }

    private void localControlRoute(WorldGenLevel level, BoundingBox clip, BlockState marker) {
        stripe(level, clip, -5,1,5, 5,1,5, marker);
        lamp(level, clip, new BlockPos(-7,1,5));
        lamp(level, clip, new BlockPos(7,1,5));
    }

    private void evacuationRoute(WorldGenLevel level, BoundingBox clip, BlockState marker) {
        stripe(level, clip, -6,1,4, -2,1,4, marker);
        stripe(level, clip, 2,1,4, 6,1,4, marker);
        lamp(level, clip, new BlockPos(6,1,-4));
    }

    private void chassisBayMarks(WorldGenLevel level, BoundingBox clip, BlockState marker) {
        for (int x : new int[]{-6, 6}) {
            mark(level, clip, new BlockPos(x,1,-3), marker);
            mark(level, clip, new BlockPos(x,1,0), marker);
            mark(level, clip, new BlockPos(x,1,3), marker);
        }
    }

    private void clockStations(WorldGenLevel level, BoundingBox clip, BlockState marker) {
        mark(level, clip, new BlockPos(-6,1,4), marker);
        mark(level, clip, new BlockPos(0,1,6), marker);
        mark(level, clip, new BlockPos(6,1,4), marker);
        lamp(level, clip, new BlockPos(-6,1,-4));
        lamp(level, clip, new BlockPos(6,1,-4));
    }

    private void domesticWorkbench(WorldGenLevel level, BoundingBox clip, BlockState marker) {
        mark(level, clip, new BlockPos(5,1,4), marker);
        candle(level, clip, new BlockPos(6,1,4));
        candle(level, clip, new BlockPos(5,1,5));
    }

    private void quarantineMarks(WorldGenLevel level, BoundingBox clip, BlockState marker) {
        stripe(level, clip, -6,1,-5, 6,1,-5, marker);
        lamp(level, clip, new BlockPos(-6,1,5));
        lamp(level, clip, new BlockPos(6,1,5));
    }

    private void sharedUtilityMarks(WorldGenLevel level, BoundingBox clip, BlockState marker) {
        stripe(level, clip, -6,1,5, -2,1,5, marker);
        stripe(level, clip, 2,1,5, 6,1,5, Blocks.LIGHT_BLUE_CARPET.defaultBlockState());
        candle(level, clip, new BlockPos(6,1,-4));
    }

    private void manualIffRoute(WorldGenLevel level, BoundingBox clip, BlockState marker) {
        stripe(level, clip, -7,1,4, -2,1,4, marker);
        stripe(level, clip, 2,1,4, 7,1,4, Blocks.RED_CARPET.defaultBlockState());
        lamp(level, clip, new BlockPos(-7,1,-4));
    }

    private void humanCorridor(WorldGenLevel level, BoundingBox clip, BlockState marker) {
        stripe(level, clip, -7,1,5, 7,1,5, marker);
        lamp(level, clip, new BlockPos(-7,1,-4));
        lamp(level, clip, new BlockPos(7,1,-4));
    }

    private void localScramMarks(WorldGenLevel level, BoundingBox clip, BlockState marker) {
        stripe(level, clip, -7,1,5, -3,1,5, marker);
        stripe(level, clip, 3,1,5, 7,1,5, marker);
        lamp(level, clip, new BlockPos(-8,1,4));
        lamp(level, clip, new BlockPos(8,1,4));
    }

    private void recordsProcessMarks(WorldGenLevel level, BoundingBox clip, BlockState marker) {
        mark(level, clip, new BlockPos(-6,1,5), marker);
        mark(level, clip, new BlockPos(0,1,6), Blocks.GRAY_CARPET.defaultBlockState());
        mark(level, clip, new BlockPos(6,1,5), Blocks.RED_CARPET.defaultBlockState());
        lamp(level, clip, new BlockPos(6,1,-5));
    }

    private void stripe(WorldGenLevel level, BoundingBox clip, int x1, int y1, int z1,
                        int x2, int y2, int z2, BlockState state) {
        int steps = Math.max(Math.abs(x2 - x1), Math.max(Math.abs(y2 - y1), Math.abs(z2 - z1)));
        for (int i = 0; i <= steps; i++) {
            int x = x1 + (x2 - x1) * i / Math.max(1, steps);
            int y = y1 + (y2 - y1) * i / Math.max(1, steps);
            int z = z1 + (z2 - z1) * i / Math.max(1, steps);
            // Never dress the central two-wide traversal axes.
            if (Math.abs(x) <= 1 || Math.abs(z) <= 1) continue;
            mark(level, clip, new BlockPos(x,y,z), state);
        }
    }

    private void lamp(WorldGenLevel level, BoundingBox clip, BlockPos offset) {
        mark(level, clip, offset, Blocks.SOUL_LANTERN.defaultBlockState());
    }

    private void candle(WorldGenLevel level, BoundingBox clip, BlockPos offset) {
        mark(level, clip, offset, Blocks.CANDLE.defaultBlockState());
    }

    private void mark(WorldGenLevel level, BoundingBox clip, BlockPos offset, BlockState state) {
        BlockPos pos = origin.offset(offset);
        if (!clip.isInside(pos) || !getBoundingBox().isInside(pos)) return;
        if (!level.getBlockState(pos).isAir()) return;
        BlockState support = level.getBlockState(pos.below());
        if (support.isAir() || !support.getFluidState().isEmpty()) return;
        level.setBlock(pos, state, 2);
    }

    private static BoundingBox box(BlockPos origin) {
        return new BoundingBox(origin.getX()-40, origin.getY()-16, origin.getZ()-40,
                origin.getX()+40, origin.getY()+24, origin.getZ()+40);
    }
}
