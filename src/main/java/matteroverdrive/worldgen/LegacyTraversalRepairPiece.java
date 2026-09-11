package matteroverdrive.worldgen;

import matteroverdrive.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Small post-layout traversal guarantee for the hidden Mad Scientist laboratory.
 * It is a separate piece so the final walkable stair is authored after the room
 * shell/interior and cannot be cleared by the laboratory volume.
 */
public final class LegacyTraversalRepairPiece extends StructurePiece {
    private final BlockPos origin;

    public LegacyTraversalRepairPiece(BlockPos origin) {
        super(ModStructures.LEGACY_TRAVERSAL_REPAIR_PIECE.get(), 0,
                new BoundingBox(origin.getX()+3, origin.getY()-8, origin.getZ()+3,
                        origin.getX()+7, origin.getY()+4, origin.getZ()+13));
        this.origin = origin;
    }

    public LegacyTraversalRepairPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.LEGACY_TRAVERSAL_REPAIR_PIECE.get(), tag);
        this.origin = new BlockPos(tag.getInt("MOX"), tag.getInt("MOY"), tag.getInt("MOZ"));
    }

    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("MOX", origin.getX()); tag.putInt("MOY", origin.getY()); tag.putInt("MOZ", origin.getZ());
    }

    @Override public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                                     RandomSource random, BoundingBox clip, ChunkPos chunkPos, BlockPos pivot) {
        BlockState stair = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.SOUTH);
        BlockPos start = origin.offset(5, 0, 4);
        for (int i = 0; i <= 7; i++) {
            int y = -i;
            int z = i;
            for (int x = -1; x <= 1; x++) {
                BlockPos p = start.offset(x, y, z);
                set(level, clip, p, stair);
                for (int h = 1; h <= 3; h++) set(level, clip, p.above(h), Blocks.AIR.defaultBlockState());
            }
        }
        // Three-wide lower landing joins the laboratory floor at y-7.
        for (int z = 11; z <= 13; z++) for (int x = 4; x <= 6; x++) {
            BlockPos p = origin.offset(x, -7, z);
            if (z > 11) set(level, clip, p, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            for (int h = 1; h <= 3; h++) set(level, clip, p.above(h), Blocks.AIR.defaultBlockState());
        }
    }

    private void set(WorldGenLevel level, BoundingBox clip, BlockPos pos, BlockState state) {
        if (!clip.isInside(pos) || !getBoundingBox().isInside(pos) || level.getBlockState(pos).is(Blocks.BEDROCK)) return;
        level.setBlock(pos, state, 2);
    }
}
