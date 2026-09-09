package matteroverdrive.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

/** Compact exploration sites themed around the port's new technology systems. */
public final class TechnologySiteFeature extends Feature<NoneFeatureConfiguration> {
    public enum Kind { MATTER_LAB, ANDROID_RELAY, ANOMALY_RESEARCH }

    private final Kind kind;

    public TechnologySiteFeature(Codec<NoneFeatureConfiguration> codec, Kind kind) {
        super(codec);
        this.kind = kind;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        BlockPos base = new BlockPos(origin.getX(), y, origin.getZ());
        if (!level.getFluidState(base.below()).isEmpty()) return false;
        return switch (kind) {
            case MATTER_LAB -> matterLab(level, base);
            case ANDROID_RELAY -> androidRelay(level, base);
            case ANOMALY_RESEARCH -> anomalyResearch(level, base);
        };
    }

    private static boolean matterLab(WorldGenLevel level, BlockPos base) {
        platform(level, base, Blocks.SMOOTH_STONE.defaultBlockState());
        shell(level, base, Blocks.IRON_BLOCK.defaultBlockState(), Blocks.LIGHT_GRAY_STAINED_GLASS.defaultBlockState());
        place(level, base.offset(-1, 1, 0), mod("matter_storage_matrix", Blocks.IRON_BLOCK));
        place(level, base.offset(1, 1, 0), mod("matter_excavator", Blocks.BLAST_FURNACE));
        place(level, base.offset(0, 1, 2), mod("holographic_status_panel", Blocks.SEA_LANTERN));
        brokenRoof(level, base);
        return true;
    }

    private static boolean androidRelay(WorldGenLevel level, BlockPos base) {
        platform(level, base, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        for (int y = 1; y <= 4; y++) place(level, base.offset(0, y, 0), Blocks.IRON_BLOCK.defaultBlockState());
        place(level, base.offset(0, 5, 0), Blocks.SEA_LANTERN.defaultBlockState());
        place(level, base.offset(-1, 1, 0), mod("android_induction_relay", Blocks.LODESTONE));
        place(level, base.offset(1, 1, 0), mod("grid_capacitor", Blocks.IRON_BLOCK));
        place(level, base.offset(0, 1, 2), mod("holographic_status_panel", Blocks.SEA_LANTERN));
        for (int x = -2; x <= 2; x += 4) for (int z = -2; z <= 2; z += 4)
            place(level, base.offset(x, 1, z), Blocks.IRON_BARS.defaultBlockState());
        return true;
    }

    private static boolean anomalyResearch(WorldGenLevel level, BlockPos base) {
        platform(level, base, Blocks.REINFORCED_DEEPSLATE.defaultBlockState());
        for (int x = -3; x <= 3; x++) {
            place(level, base.offset(x, 1, -3), Blocks.IRON_BARS.defaultBlockState());
            place(level, base.offset(x, 1, 3), Blocks.IRON_BARS.defaultBlockState());
        }
        for (int z = -2; z <= 2; z++) {
            place(level, base.offset(-3, 1, z), Blocks.IRON_BARS.defaultBlockState());
            place(level, base.offset(3, 1, z), Blocks.IRON_BARS.defaultBlockState());
        }
        place(level, base.offset(0, 1, 0), mod("anomaly_containment_unit", Blocks.OBSIDIAN));
        place(level, base.offset(-1, 1, 2), mod("facility_network_controller", Blocks.IRON_BLOCK));
        place(level, base.offset(1, 1, 2), mod("holographic_status_panel", Blocks.SEA_LANTERN));
        place(level, base.offset(0, 1, -2), Blocks.CRYING_OBSIDIAN.defaultBlockState());
        return true;
    }

    private static void platform(WorldGenLevel level, BlockPos base, BlockState state) {
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) place(level, base.offset(x, 0, z), state);
    }

    private static void shell(WorldGenLevel level, BlockPos base, BlockState wall, BlockState glass) {
        for (int y = 1; y <= 3; y++) for (int i = -3; i <= 3; i++) {
            place(level, base.offset(i, y, -3), (y == 2 && Math.abs(i) < 2) ? glass : wall);
            place(level, base.offset(i, y, 3), wall);
            place(level, base.offset(-3, y, i), (y == 2 && Math.abs(i) < 2) ? glass : wall);
            place(level, base.offset(3, y, i), wall);
        }
        for (int y = 1; y <= 2; y++) place(level, base.offset(0, y, -3), Blocks.AIR.defaultBlockState());
    }

    private static void brokenRoof(WorldGenLevel level, BlockPos base) {
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++)
            if ((x + z) % 3 != 0) place(level, base.offset(x, 4, z), Blocks.IRON_BLOCK.defaultBlockState());
    }

    private static BlockState mod(String id, Block fallback) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("matteroverdrive", id));
        return block == null || block == Blocks.AIR ? fallback.defaultBlockState() : block.defaultBlockState();
    }

    private static void place(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (!level.getBlockState(pos).is(Blocks.BEDROCK)) level.setBlock(pos, state, 2);
    }
}
