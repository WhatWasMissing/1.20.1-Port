package matteroverdrive.worldgen;

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

/**
 * Playability-first native reconstructions of the classic Matter Overdrive sites.
 *
 * Every structure is deliberately shaped around a readable player route instead of
 * a rectangular shell: entries are explicit, major rooms are spatially distinct,
 * vertical movement uses broad stepped routes, and damage never removes the only
 * required path. All writes remain clipped to the active StructurePiece chunk box.
 */
public final class LegacyNativeStructurePiece extends StructurePiece {
    private final LegacyParityStructureFeature.Kind kind;
    private final BlockPos origin;

    public LegacyNativeStructurePiece(LegacyParityStructureFeature.Kind kind, BlockPos origin) {
        super(ModStructures.LEGACY_NATIVE_PIECE.get(), 0, boxFor(kind, origin));
        this.kind = kind;
        this.origin = origin;
    }

    public LegacyNativeStructurePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.LEGACY_NATIVE_PIECE.get(), tag);
        this.kind = LegacyParityStructureFeature.Kind.valueOf(tag.getString("MOKind"));
        this.origin = new BlockPos(tag.getInt("MOX"), tag.getInt("MOY"), tag.getInt("MOZ"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("MOKind", kind.name());
        tag.putInt("MOX", origin.getX());
        tag.putInt("MOY", origin.getY());
        tag.putInt("MOZ", origin.getZ());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        switch (kind) {
            case CRASHED_SHIP -> crashedShip(level, chunkBox);
            case CARGO_SHIP -> cargoShip(level, chunkBox);
            case UNDERWATER_BASE -> underwaterBase(level, chunkBox);
            case MAD_SCIENTIST_HOUSE -> madScientistHouse(level, chunkBox);
            case ANDROID_HOUSE -> androidHouse(level, chunkBox);
            case SAND_PIT -> sandPit(level, chunkBox);
        }
    }

    /** A torn two-deck survey vessel with a readable nose -> service -> engineering route. */
    private void crashedShip(WorldGenLevel level, BoundingBox clip) {
        BlockPos c = origin.below(2);
        BlockState hull = mod("decorative.tritanium_plate", Blocks.IRON_BLOCK);
        BlockState dark = mod("decorative.carbon_fiber_plate", Blocks.DEEPSLATE_TILES);
        BlockState stripe = mod("decorative.tritanium_plate_stripe", Blocks.YELLOW_CONCRETE);
        BlockState floor = mod("decorative.floor_tiles", Blocks.SMOOTH_STONE);
        BlockState beam = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState glass = mod("industrial_glass", Blocks.TINTED_GLASS);
        BlockState lamp = mod("decorative.tritanium_lamp", Blocks.SEA_LANTERN);

        // Tapered hull. The south side is intentionally split open so the entrance is obvious.
        for (int z = -23; z <= 19; z++) {
            int half = z < -16 ? 2 + (z + 23) / 2 : z > 13 ? Math.max(2, 7 - (z - 13)) : 7;
            half = Math.max(2, Math.min(7, half));
            for (int x = -half; x <= half; x++) {
                set(level, clip, c.offset(x, 0, z), Math.abs(x) == half ? beam : floor);
                for (int y = 1; y <= 6; y++) {
                    boolean side = Math.abs(x) == half;
                    boolean breach = z >= 5 && z <= 12 && x >= half - 1 && y <= 4;
                    boolean southEntry = z <= -19 && Math.abs(x) <= 2 && y <= 3;
                    if (!side || breach || southEntry) set(level, clip, c.offset(x, y, z), Blocks.AIR.defaultBlockState());
                    else if (y == 2 || y == 3) set(level, clip, c.offset(x, y, z), (z % 4 == 0) ? glass : hull);
                    else set(level, clip, c.offset(x, y, z), (z % 5 == 0) ? beam : hull);
                }
                if (Math.abs(x) <= half - 1) set(level, clip, c.offset(x, 7, z), (x == 0 || z % 6 == 0) ? beam : hull);
            }
        }

        // Central illuminated route survives every breach.
        for (int z = -20; z <= 16; z++) {
            set(level, clip, c.offset(0, 0, z), (z % 5 == 0) ? stripe : floor);
            if (z % 5 == 0) set(level, clip, c.offset(0, 6, z), lamp);
        }

        // Cockpit: raised nose with clear glazing and a short 3-wide step ramp.
        for (int z = -22; z <= -16; z++) for (int x = -3; x <= 3; x++) {
            int step = Math.max(0, Math.min(2, (z + 22) / 2));
            set(level, clip, c.offset(x, step, z), dark);
            if (Math.abs(x) == 3 && step + 2 <= 5) set(level, clip, c.offset(x, step + 2, z), glass);
        }
        set(level, clip, c.offset(-2, 3, -19), mod("holographic_status_panel", Blocks.SEA_LANTERN));
        set(level, clip, c.offset(2, 3, -19), mod("network_switch", Blocks.IRON_BLOCK));

        // Midship service bulkheads create rooms without blocking the main aisle.
        for (int z : new int[]{-10, 1}) for (int x = -6; x <= 6; x++) {
            if (Math.abs(x) <= 1) continue;
            for (int y = 1; y <= 5; y++) set(level, clip, c.offset(x, y, z), y == 3 ? glass : dark);
        }
        set(level, clip, c.offset(-4, 1, -5), mod("tritanium_crate", Blocks.BARREL));
        set(level, clip, c.offset(4, 1, -5), mod("grid_capacitor", Blocks.IRON_BLOCK));
        set(level, clip, c.offset(-4, 1, 6), mod("matter_analyzer", Blocks.LECTERN));

        // Engineering tail has a raised ring and visible coils.
        for (int z = 13; z <= 18; z++) for (int x = -5; x <= 5; x++) {
            if (Math.abs(x) >= 4) set(level, clip, c.offset(x, 1, z), beam);
        }
        set(level, clip, c.offset(-3, 1, 15), mod("decorative.coils", Blocks.COPPER_BLOCK));
        set(level, clip, c.offset(3, 1, 15), mod("decorative.coils", Blocks.COPPER_BLOCK));

        // Impact scar and debris on the breached side, never across the centre route.
        for (int z = 6; z <= 13; z++) {
            set(level, clip, c.offset(8, -1, z), Blocks.COARSE_DIRT.defaultBlockState());
            if ((z & 1) == 0) set(level, clip, c.offset(7, 0, z), Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState());
        }
    }

    /** A broad industrial freighter with bridge, two cargo bays, engineering and loading lanes. */
    private void cargoShip(WorldGenLevel level, BoundingBox clip) {
        BlockPos c = origin;
        BlockState hull = mod("decorative.tritanium_plate", Blocks.IRON_BLOCK);
        BlockState dark = mod("decorative.carbon_fiber_plate", Blocks.DEEPSLATE_TILES);
        BlockState stripe = mod("decorative.tritanium_plate_stripe", Blocks.YELLOW_CONCRETE);
        BlockState floor = mod("decorative.floor_tiles", Blocks.SMOOTH_STONE);
        BlockState beam = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState glass = mod("industrial_glass", Blocks.TINTED_GLASS);
        BlockState lamp = mod("decorative.tritanium_lamp", Blocks.SEA_LANTERN);

        // Long stepped hull with chamfered bow/stern instead of a cuboid.
        for (int x = -32; x <= 31; x++) {
            int halfZ = Math.abs(x) > 26 ? 5 : Math.abs(x) > 21 ? 9 : 13;
            for (int z = -halfZ; z <= halfZ; z++) {
                set(level, clip, c.offset(x, 0, z), Math.abs(z) == halfZ ? beam : floor);
                boolean edge = Math.abs(z) == halfZ;
                for (int y = 1; y <= 8; y++) {
                    boolean loader = x >= -4 && x <= 12 && Math.abs(z) == halfZ && y <= 4;
                    if (!edge || loader) set(level, clip, c.offset(x, y, z), Blocks.AIR.defaultBlockState());
                    else if (y >= 3 && y <= 5 && Math.abs(x) < 23 && x % 3 != 0) set(level, clip, c.offset(x, y, z), glass);
                    else set(level, clip, c.offset(x, y, z), ((x + y) % 6 == 0) ? beam : hull);
                }
                if (Math.abs(z) <= halfZ - 1) set(level, clip, c.offset(x, 9, z), ((x + z) & 7) == 0 ? beam : hull);
            }
        }

        // Main longitudinal circulation spine: 5 blocks wide and visually obvious.
        for (int x = -28; x <= 27; x++) for (int z = -2; z <= 2; z++) {
            set(level, clip, c.offset(x, 0, z), z == 0 ? stripe : floor);
            for (int y = 1; y <= 5; y++) set(level, clip, c.offset(x, y, z), Blocks.AIR.defaultBlockState());
            if (x % 6 == 0) set(level, clip, c.offset(x, 6, 0), lamp);
        }

        // Bridge at the bow, raised by broad steps rather than an inaccessible platform.
        for (int x = -31; x <= -24; x++) for (int z = -5; z <= 5; z++) {
            int rise = Math.max(0, Math.min(3, (-24 - x) / 2));
            if (Math.abs(z) <= 5 - rise) set(level, clip, c.offset(x, rise, z), dark);
        }
        set(level, clip, c.offset(-28, 4, -3), mod("holographic_status_panel", Blocks.SEA_LANTERN));
        set(level, clip, c.offset(-28, 4, 0), mod("facility_network_controller", Blocks.IRON_BLOCK));
        set(level, clip, c.offset(-28, 4, 3), mod("network_switch", Blocks.IRON_BLOCK));

        // Two cargo bays with generous aisles between rack rows.
        cargoRacks(level, clip, c.offset(-13, 0, 0), -7, 7);
        cargoRacks(level, clip, c.offset(9, 0, 0), -7, 7);

        // Engineering stern and service machinery.
        for (int x = 22; x <= 29; x++) for (int z = -7; z <= 7; z++) {
            if (Math.abs(z) >= 5) set(level, clip, c.offset(x, 1, z), beam);
        }
        set(level, clip, c.offset(25, 1, -4), mod("grid_capacitor", Blocks.IRON_BLOCK));
        set(level, clip, c.offset(25, 1, 0), mod("transporter", Blocks.RESPAWN_ANCHOR));
        set(level, clip, c.offset(25, 1, 4), mod("network_router", Blocks.IRON_BLOCK));

        // Loading lane ties the open hull doors to the cargo spine.
        for (int z = -12; z <= 12; z++) for (int x = 2; x <= 6; x++) {
            set(level, clip, c.offset(x, 0, z), (x == 4) ? stripe : floor);
            for (int y = 1; y <= 4; y++) set(level, clip, c.offset(x, y, z), Blocks.AIR.defaultBlockState());
        }
    }

    private void cargoRacks(WorldGenLevel level, BoundingBox clip, BlockPos centre, int zMin, int zMax) {
        BlockState rack = mod("decorative.beams", Blocks.IRON_BARS);
        for (int x = -6; x <= 6; x += 4) for (int z = zMin; z <= zMax; z += 4) {
            if (Math.abs(z) <= 1) continue;
            set(level, clip, centre.offset(x, 1, z), rack);
            set(level, clip, centre.offset(x, 2, z), ((x + z) & 4) == 0 ? mod("tritanium_crate_blue", Blocks.BARREL) : mod("tritanium_crate", Blocks.BARREL));
        }
    }

    /** Radial pressure base: airlock -> hub -> four clearly separated research/service pods. */
    private void underwaterBase(WorldGenLevel level, BoundingBox clip) {
        BlockPos c = origin;
        BlockState hull = mod("decorative.tritanium_plate", Blocks.IRON_BLOCK);
        BlockState beam = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState glass = mod("industrial_glass", Blocks.TINTED_GLASS);
        BlockState floor = mod("decorative.floor_tiles", Blocks.SMOOTH_STONE);
        BlockState white = mod("decorative.white_plate", Blocks.QUARTZ_BLOCK);
        BlockState lamp = mod("decorative.tritanium_lamp", Blocks.SEA_LANTERN);

        pressurePod(level, clip, c, 10, 6, hull, floor, beam, glass);
        pressurePod(level, clip, c.offset(-18, 0, 0), 7, 5, white, floor, beam, glass);
        pressurePod(level, clip, c.offset(18, 0, 0), 7, 5, hull, floor, beam, glass);
        pressurePod(level, clip, c.offset(0, 0, 18), 7, 5, hull, floor, beam, glass);
        pressurePod(level, clip, c.offset(0, 0, -18), 7, 5, white, floor, beam, glass);

        tube(level, clip, c.offset(-13, 0, 0), true, 6);
        tube(level, clip, c.offset(13, 0, 0), true, 6);
        tube(level, clip, c.offset(0, 0, 13), false, 6);
        tube(level, clip, c.offset(0, 0, -13), false, 6);

        // South airlock continues beyond the pod and gives the base a readable entrance.
        for (int z = -29; z <= -23; z++) for (int x = -3; x <= 3; x++) {
            set(level, clip, c.offset(x, 0, z), floor);
            for (int y = 1; y <= 4; y++) {
                boolean side = Math.abs(x) == 3;
                set(level, clip, c.offset(x, y, z), side ? (y == 2 ? glass : hull) : Blocks.AIR.defaultBlockState());
            }
            set(level, clip, c.offset(x, 5, z), hull);
        }
        for (int y = 1; y <= 3; y++) for (int x = -1; x <= 1; x++) set(level, clip, c.offset(x, y, -29), Blocks.AIR.defaultBlockState());
        set(level, clip, c.offset(0, 4, -27), mod("holo_sign", Blocks.SEA_LANTERN));

        // Central hub and pod functions.
        for (int a = 0; a < 360; a += 45) {
            double r = Math.toRadians(a);
            int x = (int)Math.round(Math.cos(r) * 6), z = (int)Math.round(Math.sin(r) * 6);
            set(level, clip, c.offset(x, 1, z), lamp);
        }
        set(level, clip, c.offset(0, 1, 0), mod("facility_network_controller", Blocks.IRON_BLOCK));
        set(level, clip, c.offset(-18, 1, 0), mod("matter_analyzer", Blocks.LECTERN));
        set(level, clip, c.offset(-16, 1, 2), mod("pattern_storage", Blocks.CHISELED_BOOKSHELF));
        set(level, clip, c.offset(18, 1, 0), mod("matter_storage_matrix", Blocks.IRON_BLOCK));
        set(level, clip, c.offset(16, 1, 2), mod("decomposer", Blocks.BLAST_FURNACE));
        set(level, clip, c.offset(0, 1, 18), mod("tritanium_crate_blue", Blocks.BARREL));
        set(level, clip, c.offset(0, 1, 16), mod("charging_station", Blocks.LODESTONE));
        set(level, clip, c.offset(0, 1, -18), mod("holographic_status_panel", Blocks.SEA_LANTERN));
    }

    private void pressurePod(WorldGenLevel level, BoundingBox clip, BlockPos c, int r, int h, BlockState wall,
                             BlockState floor, BlockState beam, BlockState glass) {
        for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) {
            double d = Math.sqrt(x * x + z * z);
            if (d <= r - 0.8) {
                set(level, clip, c.offset(x, 0, z), floor);
                for (int y = 1; y <= h; y++) set(level, clip, c.offset(x, y, z), Blocks.AIR.defaultBlockState());
            }
            if (d > r - 1.2 && d <= r + 0.35) {
                for (int y = 1; y <= h; y++) {
                    BlockState state = y == 1 || y == h || ((x + z) & 5) == 0 ? beam : (y >= 2 && y <= h - 1 ? glass : wall);
                    set(level, clip, c.offset(x, y, z), state);
                }
            }
            if (d <= r) set(level, clip, c.offset(x, h + 1, z), ((x + z) & 5) == 0 ? beam : wall);
        }
        // Cardinal doors are always retained for radial routing.
        for (int y = 1; y <= 3; y++) for (int w = -1; w <= 1; w++) {
            set(level, clip, c.offset(w, y, -r), Blocks.AIR.defaultBlockState());
            set(level, clip, c.offset(w, y, r), Blocks.AIR.defaultBlockState());
            set(level, clip, c.offset(-r, y, w), Blocks.AIR.defaultBlockState());
            set(level, clip, c.offset(r, y, w), Blocks.AIR.defaultBlockState());
        }
    }

    private void tube(WorldGenLevel level, BoundingBox clip, BlockPos c, boolean xAxis, int half) {
        BlockState hull = mod("decorative.tritanium_plate", Blocks.IRON_BLOCK);
        BlockState floor = mod("decorative.floor_tile_white", Blocks.SMOOTH_STONE);
        BlockState glass = mod("industrial_glass", Blocks.TINTED_GLASS);
        for (int a = -half; a <= half; a++) for (int b = -2; b <= 2; b++) {
            int x = xAxis ? a : b, z = xAxis ? b : a;
            set(level, clip, c.offset(x, 0, z), floor);
            for (int y = 1; y <= 4; y++) {
                boolean side = Math.abs(b) == 2;
                set(level, clip, c.offset(x, y, z), side ? (y == 2 || y == 3 ? glass : hull) : Blocks.AIR.defaultBlockState());
            }
            set(level, clip, c.offset(x, 5, z), hull);
        }
    }

    /** Small residence above a much larger hidden basement laboratory. */
    private void madScientistHouse(WorldGenLevel level, BoundingBox clip) {
        BlockState white = mod("decorative.white_plate", Blocks.QUARTZ_BLOCK);
        BlockState dark = mod("decorative.carbon_fiber_plate", Blocks.DEEPSLATE_TILES);
        BlockState floor = mod("decorative.floor_tile_white", Blocks.SMOOTH_STONE);
        BlockState labFloor = mod("decorative.floor_tiles", Blocks.SMOOTH_STONE);
        BlockState beam = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState glass = mod("industrial_glass", Blocks.TINTED_GLASS);

        // Asymmetric above-ground residence/pavilion.
        roomShell(level, clip, origin, 6, 5, 5, white, floor, beam, glass, true);
        roomShell(level, clip, origin.offset(7, 0, 2), 4, 3, 4, dark, floor, beam, glass, false);
        for (int y = 1; y <= 3; y++) for (int x = -1; x <= 1; x++) set(level, clip, origin.offset(x, y, -5), Blocks.AIR.defaultBlockState());
        set(level, clip, origin.offset(0, 4, -4), mod("holo_sign", Blocks.SEA_LANTERN));

        // Broad hidden stairwell behind the east annex, one-block descent per two z.
        for (int i = 0; i <= 6; i++) {
            int y = -i;
            int z = 4 + i * 2;
            for (int x = 5; x <= 7; x++) {
                set(level, clip, origin.offset(x, y, z), labFloor);
                for (int h = 1; h <= 3; h++) set(level, clip, origin.offset(x, y + h, z), Blocks.AIR.defaultBlockState());
            }
        }

        BlockPos lab = origin.offset(0, -6, 15);
        roomShell(level, clip, lab, 10, 8, 5, dark, labFloor, beam, glass, true);
        // Divide wet lab / fabrication / specimens, keeping a 3-wide central route.
        for (int x = -9; x <= 9; x++) if (Math.abs(x) > 1) for (int y = 1; y <= 4; y++) {
            set(level, clip, lab.offset(x, y, 0), y == 2 || y == 3 ? glass : white);
        }
        set(level, clip, lab.offset(-6, 1, -4), mod("inscriber", Blocks.ANVIL));
        set(level, clip, lab.offset(-3, 1, -4), mod("decomposer", Blocks.BLAST_FURNACE));
        set(level, clip, lab.offset(4, 1, -4), mod("matter_analyzer", Blocks.LECTERN));
        set(level, clip, lab.offset(7, 1, -4), mod("pattern_storage", Blocks.CHISELED_BOOKSHELF));
        set(level, clip, lab.offset(-6, 1, 4), mod("tritanium_crate", Blocks.BARREL));
        set(level, clip, lab.offset(6, 1, 4), mod("android_spawner", Blocks.IRON_BLOCK));
    }

    /** A disguised low-profile safehouse with public atrium, charging wings and rear service yard. */
    private void androidHouse(WorldGenLevel level, BoundingBox clip) {
        BlockPos c = origin.below();
        BlockState hull = mod("decorative.tritanium_plate", Blocks.IRON_BLOCK);
        BlockState white = mod("decorative.white_plate", Blocks.QUARTZ_BLOCK);
        BlockState dark = mod("decorative.carbon_fiber_plate", Blocks.DEEPSLATE_TILES);
        BlockState floor = mod("decorative.floor_tiles", Blocks.SMOOTH_STONE);
        BlockState beam = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState glass = mod("industrial_glass", Blocks.TINTED_GLASS);
        BlockState stripe = mod("decorative.tritanium_plate_stripe", Blocks.YELLOW_CONCRETE);

        // Central atrium plus offset wings creates an H-like footprint rather than a box.
        roomShell(level, clip, c, 7, 8, 6, white, floor, beam, glass, true);
        roomShell(level, clip, c.offset(-11, 0, 3), 5, 6, 5, hull, floor, beam, glass, false);
        roomShell(level, clip, c.offset(11, 0, 3), 5, 6, 5, hull, floor, beam, glass, false);
        roomShell(level, clip, c.offset(0, 0, 12), 8, 4, 5, dark, floor, beam, glass, true);

        // Open the explicit connectors between volumes and front entrance.
        openDoor(level, clip, c.offset(0, 0, -8), false);
        openDoor(level, clip, c.offset(-7, 0, 3), true);
        openDoor(level, clip, c.offset(7, 0, 3), true);
        openDoor(level, clip, c.offset(0, 0, 8), false);
        for (int z = -12; z <= -8; z++) for (int x = -3; x <= 3; x++) {
            set(level, clip, c.offset(x, 0, z), x == 0 ? stripe : floor);
            if (Math.abs(x) == 3) for (int y = 1; y <= 4; y++) set(level, clip, c.offset(x, y, z), beam);
            set(level, clip, c.offset(x, 5, z), dark);
        }

        // Centre operations; west charging/maintenance; east logistics; rear secure room.
        set(level, clip, c.offset(0, 1, 0), mod("decorative.holo_matrix", Blocks.SEA_LANTERN));
        set(level, clip, c.offset(0, 2, 0), mod("holo_sign", Blocks.SEA_LANTERN));
        set(level, clip, c.offset(-11, 1, 0), mod("android_station", Blocks.SMITHING_TABLE));
        set(level, clip, c.offset(-11, 1, 4), mod("charging_station", Blocks.LODESTONE));
        set(level, clip, c.offset(-11, 1, 7), mod("android_induction_relay", Blocks.LODESTONE));
        set(level, clip, c.offset(11, 1, 0), mod("replicator", Blocks.SMITHING_TABLE));
        set(level, clip, c.offset(11, 1, 4), mod("network_router", Blocks.IRON_BLOCK));
        set(level, clip, c.offset(11, 1, 7), mod("grid_capacitor", Blocks.IRON_BLOCK));
        set(level, clip, c.offset(0, 1, 12), mod("facility_network_controller", Blocks.IRON_BLOCK));
        set(level, clip, c.offset(-4, 1, 12), mod("tritanium_crate_blue", Blocks.BARREL));
        set(level, clip, c.offset(4, 1, 12), mod("tritanium_crate_red", Blocks.BARREL));
    }

    /** Terraced excavation with a walkable ramp, exposed relic and surface operations gantry. */
    private void sandPit(WorldGenLevel level, BoundingBox clip) {
        BlockState sandstone = Blocks.CUT_SANDSTONE.defaultBlockState();
        BlockState floor = mod("decorative.floor_tiles", Blocks.SMOOTH_STONE);
        BlockState beam = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState stripe = mod("decorative.tritanium_plate_stripe", Blocks.YELLOW_CONCRETE);
        BlockPos bottom = origin.below(11);

        // Four broad terraces. The stepped west/south edge leaves a continuous route down.
        for (int x = -17; x <= 17; x++) for (int z = -17; z <= 17; z++) {
            int r = Math.max(Math.abs(x), Math.abs(z));
            int depth = r <= 6 ? 11 : r <= 10 ? 8 : r <= 14 ? 4 : r <= 17 ? 2 : 0;
            if (depth == 0) continue;
            for (int y = 1; y <= depth; y++) set(level, clip, origin.offset(x, -y, z), Blocks.AIR.defaultBlockState());
            set(level, clip, origin.offset(x, -depth - 1, z), sandstone);
        }

        // Safe zig-zag descent ramp, three blocks wide.
        for (int i = 0; i <= 10; i++) {
            int y = -1 - i;
            int x = i < 6 ? -15 + i * 2 : -5;
            int z = i < 6 ? 12 : 12 - (i - 5) * 3;
            for (int w = -1; w <= 1; w++) {
                set(level, clip, origin.offset(x + w, y, z), sandstone);
                set(level, clip, origin.offset(x + w, y + 1, z), Blocks.AIR.defaultBlockState());
                set(level, clip, origin.offset(x + w, y + 2, z), Blocks.AIR.defaultBlockState());
            }
        }

        // Buried relic floor with clear 3-wide aisle.
        for (int x = -7; x <= 7; x++) for (int z = -7; z <= 7; z++) {
            boolean chamfer = Math.abs(x) + Math.abs(z) > 11;
            if (!chamfer) set(level, clip, bottom.offset(x, 0, z), Math.abs(x) <= 1 ? stripe : floor);
        }
        for (int z = -6; z <= 6; z += 6) for (int y = 1; y <= 5; y++) {
            set(level, clip, bottom.offset(-7, y, z), beam);
            set(level, clip, bottom.offset(7, y, z), beam);
        }
        for (int x = -6; x <= 6; x++) set(level, clip, bottom.offset(x, 5, -6), beam);
        set(level, clip, bottom.offset(-4, 1, 0), mod("matter_excavator", Blocks.BLAST_FURNACE));
        set(level, clip, bottom.offset(4, 1, 0), mod("matter_analyzer", Blocks.LECTERN));
        set(level, clip, bottom.offset(0, 1, 4), mod("tritanium_crate", Blocks.BARREL));

        // Surface crane/gantry makes the site readable before descending.
        BlockPos crane = origin.offset(10, 0, -8);
        for (int y = 0; y <= 11; y++) set(level, clip, crane.offset(0, y, 0), beam);
        for (int x = -8; x <= 2; x++) set(level, clip, crane.offset(x, 11, 0), beam);
        for (int y = 7; y <= 10; y++) set(level, clip, crane.offset(-7, y, 0), Blocks.CHAIN.defaultBlockState());
    }

    private void roomShell(WorldGenLevel level, BoundingBox clip, BlockPos c, int hx, int hz, int h,
                           BlockState wall, BlockState floor, BlockState beam, BlockState glass, boolean chamfer) {
        for (int x = -hx; x <= hx; x++) for (int z = -hz; z <= hz; z++) {
            if (chamfer && Math.abs(x) >= hx - 1 && Math.abs(z) >= hz - 1) continue;
            set(level, clip, c.offset(x, 0, z), floor);
            boolean edge = Math.abs(x) == hx || Math.abs(z) == hz;
            boolean innerChamfer = chamfer && (Math.abs(x) == hx - 1 && Math.abs(z) >= hz - 1 || Math.abs(z) == hz - 1 && Math.abs(x) >= hx - 1);
            for (int y = 1; y <= h; y++) {
                if (!edge && !innerChamfer) set(level, clip, c.offset(x, y, z), Blocks.AIR.defaultBlockState());
                else if (y == 1 || y == h || ((x + z) & 5) == 0) set(level, clip, c.offset(x, y, z), beam);
                else if (y == 2 || y == 3) set(level, clip, c.offset(x, y, z), glass);
                else set(level, clip, c.offset(x, y, z), wall);
            }
            set(level, clip, c.offset(x, h + 1, z), ((x + z) & 7) == 0 ? beam : wall);
        }
    }

    private void openDoor(WorldGenLevel level, BoundingBox clip, BlockPos p, boolean xWall) {
        for (int y = 1; y <= 3; y++) for (int w = -1; w <= 1; w++) {
            BlockPos pos = xWall ? p.offset(0, y, w) : p.offset(w, y, 0);
            set(level, clip, pos, Blocks.AIR.defaultBlockState());
        }
    }

    private static BoundingBox boxFor(LegacyParityStructureFeature.Kind kind, BlockPos p) {
        return switch (kind) {
            case CRASHED_SHIP -> new BoundingBox(p.getX() - 9, p.getY() - 3, p.getZ() - 24, p.getX() + 9, p.getY() + 7, p.getZ() + 20);
            case CARGO_SHIP -> new BoundingBox(p.getX() - 32, p.getY(), p.getZ() - 13, p.getX() + 31, p.getY() + 10, p.getZ() + 13);
            case UNDERWATER_BASE -> new BoundingBox(p.getX() - 26, p.getY(), p.getZ() - 30, p.getX() + 26, p.getY() + 8, p.getZ() + 26);
            case MAD_SCIENTIST_HOUSE -> new BoundingBox(p.getX() - 11, p.getY() - 6, p.getZ() - 6, p.getX() + 11, p.getY() + 6, p.getZ() + 23);
            case ANDROID_HOUSE -> new BoundingBox(p.getX() - 16, p.getY() - 1, p.getZ() - 13, p.getX() + 16, p.getY() + 7, p.getZ() + 16);
            case SAND_PIT -> new BoundingBox(p.getX() - 18, p.getY() - 12, p.getZ() - 18, p.getX() + 18, p.getY() + 12, p.getZ() + 18);
        };
    }

    private static BlockState mod(String id, Block fallback) {
        ResourceLocation key = ResourceLocation.tryParse("matteroverdrive:" + id);
        Block block = key == null ? null : ForgeRegistries.BLOCKS.getValue(key);
        return block == null || block == Blocks.AIR ? fallback.defaultBlockState() : block.defaultBlockState();
    }

    private static void set(WorldGenLevel level, BoundingBox clip, BlockPos pos, BlockState state) {
        if (!clip.isInside(pos)) return;
        if (level.getBlockState(pos).is(Blocks.BEDROCK)) return;
        level.setBlock(pos, state, 2);
    }
}
