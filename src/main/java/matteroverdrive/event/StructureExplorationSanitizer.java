package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.blockentity.AndroidSpawnerBlockEntity;
import matteroverdrive.blockentity.TritaniumCrateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Enforces the exploration-first structure policy.
 *
 * Generated Matter Overdrive sites must not be a source of fully functional free
 * infrastructure. When a server loads a chunk, only Matter Overdrive functional
 * block entities are considered. If such a block is inside one of our structures it
 * is replaced with inert environmental wreckage or a seeded salvage/research cache.
 *
 * The pass is naturally self-limiting: once the machine block entity is replaced it
 * is no longer a candidate on later chunk loads. No surrounding chunk is queried or
 * force-loaded and ordinary player-placed machines outside structures are untouched.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StructureExplorationSanitizer {
    private static final Set<String> FUNCTIONAL_BLOCKS = Set.of(
            "matter_analyzer", "decomposer", "matter_recycler", "replicator", "inscriber",
            "pattern_storage", "pattern_monitor", "matter_storage_matrix", "matter_excavator",
            "network_switch", "network_router", "grid_capacitor", "quantum_power_relay",
            "transporter", "charging_station", "android_station", "android_induction_relay",
            "drone_fabricator", "facility_network_controller", "fusion_reactor_controller",
            "fusion_reactor_io", "gravitational_stabilizer", "anomaly_containment_unit",
            "spacetime_accelerator", "weapon_station", "matter_network_terminal"
    );

    private static final List<Site> SITES = List.of(
            site("crashed_ship"), site("cargo_ship"), site("underwater_base"),
            site("mad_scientist_house"), site("android_house"), site("sand_pit"),
            site("synthetic_manufacturing_plant"), site("matter_refinery"),
            site("quantum_relay_station"), site("android_command_bunker"),
            site("fusion_research_complex"), site("black_site"),
            site("deep_matter_vault"), site("autonomous_drone_foundry"),
            site("anomaly_quarantine_site"), site("orbital_recovery_array")
    );

    private static final BlockPos[] GUARD_OFFSETS = {
            new BlockPos(3, 0, 0), new BlockPos(-3, 0, 0),
            new BlockPos(0, 0, 3), new BlockPos(0, 0, -3)
    };

    private StructureExplorationSanitizer() {}

    @SubscribeEvent
    public static void chunkLoaded(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        List<BlockPos> candidates = new ArrayList<>();
        for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(entry.getValue().getBlockState().getBlock());
            if (id == null || !MatterOverdrive.MOD_ID.equals(id.getNamespace())) continue;
            if (FUNCTIONAL_BLOCKS.contains(id.getPath())) candidates.add(entry.getKey().immutable());
        }

        for (BlockPos pos : candidates) {
            Site site = containingSite(level, pos);
            if (site == null) continue;
            replaceWithExplorationReward(level, pos, site);
        }
    }

    private static Site containingSite(ServerLevel level, BlockPos pos) {
        for (Site site : SITES) {
            StructureStart start = level.structureManager().getStructureWithPieceAt(pos, site.key());
            if (start != null && start.isValid()) return site;
        }
        return null;
    }

    private static void replaceWithExplorationReward(ServerLevel level, BlockPos pos, Site site) {
        long hash = pos.asLong() ^ level.getSeed() ^ ((long) site.id().hashCode() << 32);
        int variant = Math.floorMod(hash, 7);

        // Roughly one machine position in four becomes a real reward cache. The rest
        // become inert wreckage so rooms still tell the story of what they used to do.
        if (variant <= 1) {
            Block crate = block("tritanium_crate", Blocks.BARREL);
            level.setBlock(pos, crate.defaultBlockState(), 3);
            if (level.getBlockEntity(pos) instanceof TritaniumCrateBlockEntity cache) {
                cache.seedStructureLoot(
                        ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, "chests/facilities/story_cache"),
                        level.getSeed() ^ pos.asLong() ^ site.id().hashCode());
            }
            ensureGuard(level, pos, site, hash);
            return;
        }

        Block replacement = switch (variant) {
            case 2, 3 -> block("decorative.vent.dark", Blocks.CRACKED_DEEPSLATE_BRICKS);
            case 4 -> block("decorative.coils", Blocks.CUT_COPPER);
            case 5 -> block("decorative.beams", Blocks.IRON_BARS);
            default -> Blocks.CRACKED_DEEPSLATE_TILES;
        };
        level.setBlock(pos, replacement.defaultBlockState(), 3);

        BlockPos above = pos.above();
        if ((hash & 3L) == 0L && level.getBlockState(above).isAir()) {
            level.setBlock(above, Blocks.COBWEB.defaultBlockState(), 3);
        }
    }

    /**
     * Place a finite facility-security marker beside a cache only when there is a
     * genuine side alcove: solid floor plus two blocks of clear headroom. We never
     * overwrite a doorway/corridor just to force combat.
     */
    private static void ensureGuard(ServerLevel level, BlockPos cachePos, Site site, long hash) {
        int start = Math.floorMod((int) hash, GUARD_OFFSETS.length);
        for (int i = 0; i < GUARD_OFFSETS.length; i++) {
            BlockPos candidate = cachePos.offset(GUARD_OFFSETS[(start + i) % GUARD_OFFSETS.length]);
            if (!level.hasChunkAt(candidate)) continue;
            if (!level.getBlockState(candidate).isAir() || !level.getBlockState(candidate.above()).isAir()) continue;
            if (!level.getBlockState(candidate.below()).isFaceSturdy(level, candidate.below(), Direction.UP)) continue;

            Block spawner = block("android_spawner", Blocks.IRON_BLOCK);
            level.setBlock(candidate, spawner.defaultBlockState(), 3);
            if (level.getBlockEntity(candidate) instanceof AndroidSpawnerBlockEntity security) {
                int reserve = site.id().equals("black_site") ? 4 : site.id().contains("foundry") ? 3 : 2;
                int ranged = site.id().contains("refinery") ? 35 : site.id().equals("black_site") ? 85 : 60;
                security.configureFacility(site.id(), reserve, ranged);
            }
            return;
        }
    }

    private static Block block(String path, Block fallback) {
        Block value = ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, path));
        return value == null || value == Blocks.AIR ? fallback : value;
    }

    private static Site site(String id) {
        return new Site(id, ResourceKey.create(Registries.STRUCTURE,
                ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, id)));
    }

    private record Site(String id, ResourceKey<Structure> key) {}
}
