package matteroverdrive.worldgen;

import com.mojang.serialization.Codec;
import matteroverdrive.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Restores the legacy naturally generated gravitational anomaly event. */
public final class GravitationalAnomalyFeature extends Feature<NoneFeatureConfiguration> {
    private static final int LEGACY_MIN_OFFSET = 4;
    private static final int LEGACY_VERTICAL_SPAN = 60;

    public GravitationalAnomalyFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        int y = Math.min(level.getMaxBuildHeight() - 2,
                level.getSeaLevel() + LEGACY_MIN_OFFSET + context.random().nextInt(LEGACY_VERTICAL_SPAN));
        BlockPos pos = new BlockPos(context.origin().getX(), y, context.origin().getZ());
        if (!level.getWorldBorder().isWithinBounds(pos)) return false;
        if (!level.getFluidState(pos).isEmpty() || !level.getBlockState(pos).isAir()) return false;
        return level.setBlock(pos, ModBlocks.get("gravitational_anomaly").get().defaultBlockState(), 2);
    }
}
