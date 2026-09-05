package matteroverdrive.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Modern placement wrapper around the recovered Matter Overdrive structure layouts.
 *
 * Legacy dimensions, machine roles, loot themes and occupants remain authoritative,
 * while placement uses 1.20.1-era terrain checks and entrance cleanup so structures
 * do not blindly stamp themselves into obviously unsuitable terrain.
 */
public final class ModernizedStructureFeature extends Feature<NoneFeatureConfiguration> {
    private final LegacyParityStructureFeature.Kind kind;
    private final LegacyParityStructureFeature delegate;

    public ModernizedStructureFeature(Codec<NoneFeatureConfiguration> codec, LegacyParityStructureFeature.Kind kind) {
        super(codec);
        this.kind = kind;
        this.delegate = new LegacyParityStructureFeature(codec, kind);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!terrainSuitable(context.level(), context.origin())) return false;
        boolean placed = delegate.place(context);
        if (placed) clearEntrances(context.level(), context.origin());
        return placed;
    }

    private boolean terrainSuitable(WorldGenLevel level, BlockPos origin) {
        return switch (kind) {
            case ANDROID_HOUSE -> stableLand(level, origin, 10, 10, 5);
            case MAD_SCIENTIST_HOUSE -> stableLand(level, origin, 4, 4, 4);
            case CRASHED_SHIP -> stableLand(level, origin, 5, 17, 8);
            case CARGO_SHIP -> stableLand(level, origin, 29, 11, 10);
            case UNDERWATER_BASE, SAND_PIT -> true;
        };
    }

    private static boolean stableLand(WorldGenLevel level, BlockPos origin, int halfX, int halfZ, int maxSpread) {
        int[][] samples = {
                {0, 0},
                {-halfX, -halfZ}, {halfX, -halfZ},
                {-halfX, halfZ}, {halfX, halfZ},
                {-halfX, 0}, {halfX, 0},
                {0, -halfZ}, {0, halfZ}
        };
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int solid = 0;
        for (int[] sample : samples) {
            int x = origin.getX() + sample[0];
            int z = origin.getZ() + sample[1];
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            min = Math.min(min, y);
            max = Math.max(max, y);
            BlockPos support = new BlockPos(x, Math.max(level.getMinBuildHeight(), y - 1), z);
            if (!level.getBlockState(support).isAir() && level.getFluidState(support).isEmpty()) solid++;
        }
        return max - min <= maxSpread && solid >= 7;
    }

    private void clearEntrances(WorldGenLevel level, BlockPos origin) {
        switch (kind) {
            case ANDROID_HOUSE -> clearApproach(level, origin.below(2), -10, 2);
            case MAD_SCIENTIST_HOUSE -> clearApproach(level, origin, -4, 2);
            default -> {
            }
        }
    }

    private static void clearApproach(WorldGenLevel level, BlockPos base, int doorZ, int approachDepth) {
        for (int step = 0; step <= approachDepth; step++) {
            int z = doorZ - step;
            for (int x = -1; x <= 1; x++) {
                for (int y = 1; y <= 3; y++) {
                    BlockPos pos = base.offset(x, y, z);
                    if (!level.getBlockState(pos).is(Blocks.BEDROCK)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }
}
