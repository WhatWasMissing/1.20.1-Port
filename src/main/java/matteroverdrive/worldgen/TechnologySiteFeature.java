package matteroverdrive.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import matteroverdrive.blockentity.TritaniumCrateBlockEntity;
import matteroverdrive.registry.ModEntities;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.registries.ForgeRegistries;

/** Compact exploration sites themed around the port's new technology systems. */
public final class TechnologySiteFeature extends Feature<NoneFeatureConfiguration> {
    public enum Kind { MATTER_LAB, ANDROID_RELAY, ANOMALY_RESEARCH, MATTER_OBSERVATORY, FIELD_LOGISTICS_DEPOT }

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
        if (!suitableSite(level, base)) return false;
        return switch (kind) {
            case MATTER_LAB -> matterLab(level, base);
            case ANDROID_RELAY -> androidRelay(level, base);
            case ANOMALY_RESEARCH -> anomalyResearch(level, base);
            case MATTER_OBSERVATORY -> matterObservatory(level, base);
            case FIELD_LOGISTICS_DEPOT -> fieldLogisticsDepot(level, base, context.random());
        };
    }

    private static boolean suitableSite(WorldGenLevel level, BlockPos base) {
        // Compact features must have a stable footprint; this prevents floating rooms,
        // buried entrances and accidental construction across lakes or ravines.
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int x = -4; x <= 4; x++) for (int z = -4; z <= 4; z++) {
            BlockPos floor = base.offset(x, -1, z);
            if (!level.getFluidState(floor).isEmpty()) return false;
            int h = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, floor.getX(), floor.getZ());
            min = Math.min(min, h); max = Math.max(max, h);
        }
        return max - min <= 3;
    }

    private static boolean matterLab(WorldGenLevel level, BlockPos base) {
        platform(level, base, Blocks.SMOOTH_STONE.defaultBlockState());
        shell(level, base, Blocks.IRON_BLOCK.defaultBlockState(), Blocks.LIGHT_GRAY_STAINED_GLASS.defaultBlockState());
        place(level, base.offset(-1, 1, 0), mod("matter_storage_matrix", Blocks.IRON_BLOCK));
        place(level, base.offset(1, 1, 0), mod("matter_excavator", Blocks.BLAST_FURNACE));
        place(level, base.offset(0, 1, -2), mod("matter_network_terminal", Blocks.IRON_BLOCK));
        place(level, base.offset(0, 1, 2), mod("holographic_status_panel", Blocks.SEA_LANTERN));
        BlockPos cache = base.offset(2, 1, 2);
        place(level, cache, mod("tritanium_crate_blue", Blocks.CHEST));
        seedSiteCache(level, cache, "salvage", 0x4C41424CL);
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
        BlockPos cache = base.offset(-2, 1, 2);
        place(level, cache, mod("tritanium_crate_cyan", Blocks.CHEST));
        seedSiteCache(level, cache, "salvage", 0x52454C4159L);
        spawnDrone(level, base.offset(0, 2, -2));
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
        BlockPos cache = base.offset(2, 1, -2);
        place(level, cache, mod("tritanium_crate_purple", Blocks.CHEST));
        seedSiteCache(level, cache, "salvage", 0x414E4F4D414C59L);
        spawnDrone(level, base.offset(0, 2, 2));
        return true;
    }

    private static boolean matterObservatory(WorldGenLevel level, BlockPos base) {
        platform(level, base, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        // A ring, central sensor, and service console give the site a readable purpose.
        for (int x = -4; x <= 4; x++) for (int z = -4; z <= 4; z++)
            if (Math.abs(x) == 4 || Math.abs(z) == 4) place(level, base.offset(x, 1, z), Blocks.IRON_BARS.defaultBlockState());
        for (int y = 1; y <= 5; y++) place(level, base.offset(0, y, 0), Blocks.IRON_BLOCK.defaultBlockState());
        place(level, base.offset(0, 6, 0), Blocks.SEA_LANTERN.defaultBlockState());
        place(level, base.offset(-2, 1, 0), mod("matter_analyzer", Blocks.BLAST_FURNACE));
        place(level, base.offset(2, 1, 0), mod("facility_network_controller", Blocks.IRON_BLOCK));
        place(level, base.offset(0, 1, 2), mod("holographic_status_panel", Blocks.SEA_LANTERN));
        place(level, base.offset(0, 1, -4), Blocks.AIR.defaultBlockState());
        BlockPos cache = base.offset(-2, 1, 2);
        place(level, cache, mod("tritanium_crate_blue", Blocks.CHEST));
        seedSiteCache(level, cache, "salvage", 0x4F42534552564154L);
        // The observatory's exposed sensor mast is protected by a single autonomous guard.
        spawnDrone(level, base.offset(2, 2, -2));
        return true;
    }

    private static boolean fieldLogisticsDepot(WorldGenLevel level, BlockPos base, net.minecraft.util.RandomSource random) {
        platform(level, base, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        // A depot has a readable approach, power/service spine and two separated caches.
        for (int x = -4; x <= 4; x++) {
            place(level, base.offset(x, 1, -4), Blocks.IRON_BLOCK.defaultBlockState());
            if (Math.abs(x) < 2) place(level, base.offset(x, 1, -4), Blocks.AIR.defaultBlockState());
        }
        for (int z = -2; z <= 2; z++) place(level, base.offset(0, 1, z), Blocks.IRON_BLOCK.defaultBlockState());
        place(level, base.offset(0, 2, 0), mod("charging_station", Blocks.LIGHTNING_ROD));
        place(level, base.offset(0, 1, 2), mod("network_switch", Blocks.IRON_BLOCK));
        place(level, base.offset(1, 1, 2), mod("network_pipe", Blocks.IRON_BARS));
        BlockPos leftCache = base.offset(-2, 1, 1);
        BlockPos rightCache = base.offset(2, 1, 1);
        place(level, leftCache, mod("tritanium_crate_blue", Blocks.CHEST));
        place(level, rightCache, mod("tritanium_crate", Blocks.CHEST));
        seedDepotCache(level, leftCache, "field_logistics_depot", 0x4D4F444550L);
        seedDepotCache(level, rightCache, "field_logistics_depot", 0x4D4F444551L);
        place(level, base.offset(0, 1, -2), mod("holographic_status_panel", Blocks.SEA_LANTERN));
        if (random.nextBoolean()) place(level, base.offset(-3, 1, 3), mod("matter_storage_matrix", Blocks.IRON_BLOCK));
        return true;
    }

    private static void seedDepotCache(WorldGenLevel level, BlockPos pos, String table, long salt) {
        seedSiteCache(level, pos, table, salt);
    }

    private static void seedSiteCache(WorldGenLevel level, BlockPos pos, String table, long salt) {
        if (level.getBlockEntity(pos) instanceof TritaniumCrateBlockEntity crate) {
            long seed = level.getSeed() ^ pos.asLong() ^ salt;
            crate.seedStructureLoot(ResourceLocation.fromNamespaceAndPath("matteroverdrive", "chests/facilities/" + table), seed);
        }
    }

    private static void spawnDrone(WorldGenLevel level, BlockPos pos) {
        if (!level.getWorldBorder().isWithinBounds(pos)) return;
        EntityType<? extends Mob> type = ModEntities.DRONE.get();
        Mob drone = type.create(level.getLevel());
        if (drone == null) return;
        drone.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
        if (!level.noCollision(drone)) { drone.discard(); return; }
        drone.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
        drone.setPersistenceRequired();
        level.addFreshEntity(drone);
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
        Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("matteroverdrive", id));
        return block == null || block == Blocks.AIR ? fallback.defaultBlockState() : block.defaultBlockState();
    }

    private static void place(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (!level.getBlockState(pos).is(Blocks.BEDROCK)) level.setBlock(pos, state, 2);
    }
}
