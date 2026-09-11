package matteroverdrive.worldgen;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.blockentity.AndroidSpawnerBlockEntity;
import matteroverdrive.blockentity.TritaniumCrateBlockEntity;
import matteroverdrive.registry.ModStructures;
import net.minecraft.core.BlockPos;
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
import net.minecraftforge.registries.ForgeRegistries;

/** Optional rare-layout archive mezzanine placed inside the main facility volume. */
public final class RareArchiveMezzaninePiece extends StructurePiece {
    private final BlockPos origin;
    private final String profile;

    public RareArchiveMezzaninePiece(BlockPos origin, String profile) {
        super(ModStructures.RARE_ARCHIVE_MEZZANINE_PIECE.get(), 0,
                new BoundingBox(origin.getX() - 10, origin.getY(), origin.getZ() - 10,
                        origin.getX() + 10, origin.getY() + 9, origin.getZ() + 10));
        this.origin = origin;
        this.profile = profile;
    }

    public RareArchiveMezzaninePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.RARE_ARCHIVE_MEZZANINE_PIECE.get(), tag);
        this.origin = new BlockPos(tag.getInt("MOX"), tag.getInt("MOY"), tag.getInt("MOZ"));
        this.profile = tag.getString("MOProfile");
    }

    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("MOX", origin.getX()); tag.putInt("MOY", origin.getY()); tag.putInt("MOZ", origin.getZ());
        tag.putString("MOProfile", profile);
    }

    @Override public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                                     RandomSource random, BoundingBox clip, ChunkPos chunkPos, BlockPos pivot) {
        BlockState floor = mod("decorative.floor_tiles", Blocks.SMOOTH_STONE);
        BlockState beam = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState rail = Blocks.IRON_BARS.defaultBlockState();

        // Raised side platform leaves the ground-level centre lane untouched.
        for (int x = 3; x <= 9; x++) for (int z = -5; z <= 5; z++) {
            set(level, clip, origin.offset(x, 4, z), floor);
            if (x == 3 || x == 9 || Math.abs(z) == 5) set(level, clip, origin.offset(x, 5, z), rail);
        }
        // Broad 3-wide stair from the main floor to the archive.
        for (int i = 0; i <= 4; i++) for (int w = -1; w <= 1; w++) {
            BlockPos p = origin.offset(2 + i, i, -6 + w);
            set(level, clip, p, floor);
            for (int h = 1; h <= 3; h++) set(level, clip, p.above(h), Blocks.AIR.defaultBlockState());
        }
        for (int y = 1; y <= 4; y++) set(level, clip, origin.offset(9, y, 0), beam);
        set(level, clip, origin.offset(6, 5, -2), Blocks.LECTERN.defaultBlockState());
        set(level, clip, origin.offset(7, 5, 2), Blocks.CHISELED_BOOKSHELF.defaultBlockState());

        Block crate = block("tritanium_crate", Blocks.BARREL);
        BlockPos cache = origin.offset(5, 5, 2);
        set(level, clip, cache, crate.defaultBlockState());
        if (level.getBlockEntity(cache) instanceof TritaniumCrateBlockEntity be) {
            be.seedStructureLoot(ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID,
                    "chests/facilities/story_cache"), level.getSeed() ^ cache.asLong() ^ profile.hashCode());
        }

        BlockPos guard = origin.offset(8, 5, -2);
        if (clip.isInside(guard) && level.getBlockState(guard).isAir() && level.getBlockState(guard.above()).isAir()
                && !level.getBlockState(guard.below()).isAir()) {
            Block spawner = block("android_spawner", Blocks.IRON_BLOCK);
            set(level, clip, guard, spawner.defaultBlockState());
            if (level.getBlockEntity(guard) instanceof AndroidSpawnerBlockEntity security) {
                security.configureFacility(profile, profile.equals("black_site") ? 4 : 2,
                        profile.equals("black_site") ? 90 : 70);
            }
        }
    }

    private static Block block(String id, Block fallback) {
        Block value = ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, id));
        return value == null || value == Blocks.AIR ? fallback : value;
    }
    private static BlockState mod(String id, Block fallback) { return block(id, fallback).defaultBlockState(); }
    private void set(WorldGenLevel level, BoundingBox clip, BlockPos pos, BlockState state) {
        if (!clip.isInside(pos) || !getBoundingBox().isInside(pos) || level.getBlockState(pos).is(Blocks.BEDROCK)) return;
        level.setBlock(pos, state, 2);
    }
}
