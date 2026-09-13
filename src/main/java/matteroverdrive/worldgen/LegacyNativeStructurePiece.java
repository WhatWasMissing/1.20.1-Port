package matteroverdrive.worldgen;

import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.ChunkPos;

/**
 * Native, chunk-clipped reconstructions of the classic Matter Overdrive sites.
 *
 * The silhouettes and roles remain recognisable, but the actual builds now use a
 * more contemporary sci-fi language: layered hulls, structural ribs, framed
 * glazing, recessed lights, service bays, consoles and intentional interior
 * zoning rather than flat single-material walls.
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

    private void crashedShip(WorldGenLevel level, BoundingBox clip) {
        BlockPos base = origin.below();
        BlockState hull = block("decorative.tritanium_plate");
        BlockState accent = block("decorative.tritanium_plate_stripe");
        BlockState floor = block("decorative.floor_tiles");
        BlockState beam = block("decorative.beams");
        BlockState lamp = block("decorative.tritanium_lamp");
        for (int z = -17; z <= 17; z++) {
            int nose = Math.min(z + 17, 17 - z);
            int half = Math.max(1, Math.min(5, 1 + nose / 3));
            for (int x = -half; x <= half; x++) {
                set(level, clip, base.offset(x, 0, z), ((z & 5) == 0) ? accent : floor);
                if (Math.abs(x) == half) {
                    set(level, clip, base.offset(x, 1, z), ((z & 3) == 0) ? beam : hull);
                    if (Math.abs(z) < 12 && z % 3 != 0) set(level, clip, base.offset(x, 2, z), hull);
                }
            }
            if (z % 6 == 0 && Math.abs(z) < 14) {
                set(level, clip, base.offset(-half, 3, z), beam);
                set(level, clip, base.offset(half, 3, z), beam);
                set(level, clip, base.offset(0, 3, z), lamp);
            }
        }
        // A torn-open service section makes the wreck read as damaged rather than
        // simply as an intact ship sitting on the terrain.
        for (int z = 3; z <= 8; z++) for (int y = 1; y <= 3; y++)
            set(level, clip, base.offset(4, y, z), Blocks.AIR.defaultBlockState());
        set(level, clip, base.offset(0, 1, 4), block("tritanium_crate"));
        set(level, clip, base.offset(2, 1, -4), block("holo_sign"));
        set(level, clip, base.offset(-2, 1, -6), block("grid_capacitor"));
    }

    private void cargoShip(WorldGenLevel level, BoundingBox clip) {
        BlockPos base = origin.above();
        BlockState hull = block("decorative.tritanium_plate");
        BlockState stripe = block("decorative.tritanium_plate_stripe");
        BlockState floor = block("decorative.floor_tiles");
        BlockState glass = block("industrial_glass");
        BlockState beam = block("decorative.beams");
        BlockState lamp = block("decorative.tritanium_lamp");
        for (int x = -29; x <= 28; x++) {
            int end = Math.min(x + 29, 28 - x);
            int halfZ = Math.max(2, Math.min(11, 2 + end / 4));
            for (int z = -halfZ; z <= halfZ; z++) {
                set(level, clip, base.offset(x, 0, z), ((x & 7) == 0) ? stripe : floor);
                if (Math.abs(z) == halfZ) {
                    set(level, clip, base.offset(x, 1, z), ((x & 3) == 0) ? beam : hull);
                    set(level, clip, base.offset(x, 2, z), (Math.abs(x) < 20 && (x & 1) == 0) ? glass : hull);
                }
            }
            if (Math.abs(x) < 23 && x % 5 == 0) {
                set(level, clip, base.offset(x, 3, -4), beam);
                set(level, clip, base.offset(x, 3, 4), beam);
                set(level, clip, base.offset(x, 4, 0), lamp);
            }
        }
        // Raised central spine / cargo tram corridor.
        for (int x = -22; x <= 20; x++) {
            for (int z = -3; z <= 3; z++) set(level, clip, base.offset(x, 3, z), z == 0 ? stripe : hull);
            if ((x & 3) == 0) set(level, clip, base.offset(x, 4, 0), lamp);
        }
        set(level, clip, base.offset(-25, 1, 0), block("holo_sign"));
        set(level, clip, base.offset(10, 1, 0), block("transporter"));
        set(level, clip, base.offset(12, 1, 0), block("network_switch"));
        set(level, clip, base.offset(14, 1, 0), block("network_pipe"));
        set(level, clip, base.offset(-10, 1, 3), block("tritanium_crate_red"));
        set(level, clip, base.offset(-8, 1, 3), block("tritanium_crate_cyan"));
        set(level, clip, base.offset(10, 1, 3), block("tritanium_crate_lime"));
        set(level, clip, base.offset(12, 1, 3), block("tritanium_crate_blue"));
    }

    private void underwaterBase(WorldGenLevel level, BoundingBox clip) {
        BlockPos c = origin.above();
        BlockState hull = block("decorative.tritanium_plate");
        BlockState beam = block("decorative.beams");
        BlockState glass = block("industrial_glass");
        BlockState floor = block("decorative.floor_tiles");
        BlockState lamp = block("decorative.tritanium_lamp");
        for (int x = -21; x <= 21; x++) for (int z = -21; z <= 21; z++) {
            double d = Math.sqrt(x * x + z * z);
            if (d <= 19.5D) {
                set(level, clip, c.offset(x, 0, z), floor);
                for (int y = 1; y <= 5; y++) set(level, clip, c.offset(x, y, z), Blocks.AIR.defaultBlockState());
            }
            if (d >= 19.3D && d <= 21.1D) {
                for (int y = 1; y <= 5; y++) {
                    boolean rib = ((Math.abs(x) + Math.abs(z)) % 7) == 0;
                    set(level, clip, c.offset(x, y, z), rib ? beam : (y == 2 || y == 3) ? glass : hull);
                }
            }
            if (d <= 18.5D) set(level, clip, c.offset(x, 6, z), d > 14.0D ? glass : hull);
        }
        // Lit radial service spokes break up the huge circular interior.
        for (int i = -15; i <= 15; i++) {
            set(level, clip, c.offset(i, 1, 0), (i % 5 == 0) ? lamp : block("decorative.floor_tile_white"));
            set(level, clip, c.offset(0, 1, i), (i % 5 == 0) ? lamp : block("decorative.floor_tile_white"));
        }
        set(level, clip, c.offset(0, 1, 0), block("matter_analyzer"));
        set(level, clip, c.offset(5, 1, 0), block("pattern_storage"));
        set(level, clip, c.offset(-5, 1, 0), block("pattern_monitor"));
        set(level, clip, c.offset(8, 1, 5), block("tritanium_crate_blue"));
        set(level, clip, c.offset(-8, 1, 5), block("matter_storage_matrix"));
        set(level, clip, c.offset(0, 1, -8), block("holographic_status_panel"));
    }

    private void madScientistHouse(WorldGenLevel level, BoundingBox clip) {
        BlockState wall = block("decorative.white_plate");
        BlockState dark = block("decorative.carbon_fiber_plate");
        BlockState beam = block("decorative.beams");
        BlockState floor = block("decorative.floor_tile_white");
        BlockState glass = block("industrial_glass");
        BlockState lamp = block("decorative.tritanium_lamp");

        // Keep the classic small footprint, but turn it into a compact research
        // pavilion with a dark structural frame, glass curtain walls and overhang.
        for (int x = -5; x <= 5; x++) for (int z = -5; z <= 5; z++) {
            if (Math.abs(x) <= 4 && Math.abs(z) <= 4) set(level, clip, origin.offset(x, 0, z), floor);
            boolean frame = (Math.abs(x) == 4 && Math.abs(z) <= 4) || (Math.abs(z) == 4 && Math.abs(x) <= 4);
            for (int y = 1; y <= 4; y++) {
                if (!frame) continue;
                boolean door = z == -4 && Math.abs(x) <= 1 && y <= 3;
                boolean corner = Math.abs(x) == 4 && Math.abs(z) == 4;
                boolean glazing = !corner && y >= 2 && y <= 3;
                set(level, clip, origin.offset(x, y, z), door ? Blocks.AIR.defaultBlockState() : corner ? beam : glazing ? glass : wall);
            }
            if (Math.abs(x) <= 5 && Math.abs(z) <= 5) set(level, clip, origin.offset(x, 5, z), (Math.abs(x) == 5 || Math.abs(z) == 5) ? dark : wall);
        }
        for (int x : new int[]{-3, 0, 3}) set(level, clip, origin.offset(x, 4, 0), lamp);
        // Split wet-lab / fabrication zones with a low service divider.
        for (int x = -3; x <= 3; x++) if (Math.abs(x) > 1) set(level, clip, origin.offset(x, 1, 1), dark);
        set(level, clip, origin.offset(-2, 1, 2), block("inscriber"));
        set(level, clip, origin.offset(0, 1, 2), block("matter_analyzer"));
        set(level, clip, origin.offset(2, 1, 2), block("decomposer"));
        set(level, clip, origin.offset(-2, 1, -2), block("tritanium_crate"));
        set(level, clip, origin.offset(2, 1, -2), block("holographic_status_panel"));
    }

    private void androidHouse(WorldGenLevel level, BoundingBox clip) {
        BlockPos base = origin.below(2);
        BlockState hull = block("decorative.tritanium_plate");
        BlockState dark = block("decorative.carbon_fiber_plate");
        BlockState wall = block("decorative.white_plate");
        BlockState beam = block("decorative.beams");
        BlockState floor = block("decorative.floor_tiles");
        BlockState accentFloor = block("decorative.floor_tile_white");
        BlockState glass = block("industrial_glass");
        BlockState lamp = block("decorative.tritanium_lamp");

        // Modern synthetic safehouse: broad low profile, framed glass facade,
        // recessed entrance, central illuminated spine and two equipment wings.
        for (int x = -10; x <= 10; x++) for (int z = -10; z <= 10; z++) {
            boolean inside = Math.abs(x) <= 10 && Math.abs(z) <= 10;
            if (!inside) continue;
            set(level, clip, base.offset(x, 0, z), (x == 0 || z == 0) ? accentFloor : floor);
            boolean edge = Math.abs(x) == 10 || Math.abs(z) == 10;
            for (int y = 1; y <= 5; y++) {
                if (!edge) {
                    set(level, clip, base.offset(x, y, z), Blocks.AIR.defaultBlockState());
                    continue;
                }
                boolean door = z == -10 && Math.abs(x) <= 2 && y <= 3;
                boolean cornerRib = (Math.abs(x) >= 9 && Math.abs(z) == 10) || (Math.abs(z) >= 9 && Math.abs(x) == 10);
                boolean windowBand = y >= 2 && y <= 3 && !cornerRib && ((Math.abs(x) == 10 && Math.abs(z) <= 7) || (Math.abs(z) == 10 && Math.abs(x) >= 3));
                BlockState state = door ? Blocks.AIR.defaultBlockState() : cornerRib ? beam : windowBand ? glass : ((x + z + y & 1) == 0 ? hull : wall);
                set(level, clip, base.offset(x, y, z), state);
            }
            set(level, clip, base.offset(x, 6, z), (Math.abs(x) >= 8 || Math.abs(z) >= 8) ? dark : hull);
        }

        // Exterior entry canopy + light rails.
        for (int z = -13; z <= -9; z++) for (int x = -3; x <= 3; x++) {
            set(level, clip, base.offset(x, 4, z), dark);
            if (Math.abs(x) == 3) set(level, clip, base.offset(x, 3, z), beam);
        }
        for (int z = -8; z <= 8; z += 4) {
            set(level, clip, base.offset(0, 5, z), lamp);
            set(level, clip, base.offset(-6, 5, z), lamp);
            set(level, clip, base.offset(6, 5, z), lamp);
        }

        // Equipment wings use only active progression blocks.
        set(level, clip, base.offset(-6, 1, -6), block("android_station"));
        set(level, clip, base.offset(-3, 1, -6), block("replicator"));
        set(level, clip, base.offset(0, 1, -6), block("facility_network_controller"));
        set(level, clip, base.offset(3, 1, -6), block("charging_station"));
        set(level, clip, base.offset(6, 1, -6), block("android_induction_relay"));

        set(level, clip, base.offset(-6, 1, 6), block("network_router"));
        set(level, clip, base.offset(-3, 1, 6), block("tritanium_crate_blue"));
        set(level, clip, base.offset(0, 1, 6), block("holographic_status_panel"));
        set(level, clip, base.offset(3, 1, 6), block("tritanium_crate"));
        set(level, clip, base.offset(6, 1, 6), block("grid_capacitor"));

        // Central holo table / social operations space.
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++)
            if (Math.abs(x) == 2 || Math.abs(z) == 2) set(level, clip, base.offset(x, 1, z), dark);
        set(level, clip, base.offset(0, 1, 0), block("decorative.holo_matrix"));
        set(level, clip, base.offset(0, 2, 0), block("holo_sign"));
    }

    private void sandPit(WorldGenLevel level, BoundingBox clip) {
        BlockPos floor = origin.below(9);
        for (int x = -12; x <= 11; x++) for (int z = -12; z <= 11; z++) {
            double nx = (x + 0.5D) / 12.0D;
            double nz = (z + 0.5D) / 12.0D;
            double d = Math.sqrt(nx * nx + nz * nz);
            int depth = d < .30D ? 9 : d < .50D ? 7 : d < .70D ? 5 : d < .88D ? 3 : d <= 1.0D ? 1 : 0;
            if (depth == 0) continue;
            BlockPos surface = origin.offset(x, -1, z);
            for (int y = 0; y < depth; y++) set(level, clip, surface.below(y), Blocks.AIR.defaultBlockState());
            set(level, clip, surface.below(depth), d < .55D ? Blocks.CUT_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState());
        }
        // Reframe the old pit as a buried industrial excavation rather than an
        // unexplained hole: exposed hull, gantry ribs, lighting and equipment.
        for (int z = -5; z <= 5; z++) {
            int half = Math.max(1, 4 - Math.abs(z) / 2);
            for (int x = -half; x <= half; x++) set(level, clip, floor.offset(x, 0, z), block("decorative.tritanium_plate"));
        }
        for (int z = -5; z <= 5; z += 5) for (int y = 1; y <= 4; y++) {
            set(level, clip, floor.offset(-4, y, z), block("decorative.beams"));
            set(level, clip, floor.offset(4, y, z), block("decorative.beams"));
        }
        for (int z = -5; z <= 5; z += 2) set(level, clip, floor.offset(0, 4, z), block("decorative.tritanium_lamp"));
        set(level, clip, floor.offset(-3, 1, 0), block("decorative.coils"));
        set(level, clip, floor.offset(3, 1, 0), block("decorative.coils"));
        set(level, clip, floor.offset(0, 1, 2), block("tritanium_crate"));
        set(level, clip, floor.offset(0, 1, -2), block("matter_excavator"));
    }

    private static BoundingBox boxFor(LegacyParityStructureFeature.Kind kind, BlockPos p) {
        return switch (kind) {
            case CRASHED_SHIP -> new BoundingBox(p.getX() - 5, p.getY() - 1, p.getZ() - 17, p.getX() + 5, p.getY() + 4, p.getZ() + 17);
            case CARGO_SHIP -> new BoundingBox(p.getX() - 29, p.getY(), p.getZ() - 11, p.getX() + 28, p.getY() + 6, p.getZ() + 11);
            case UNDERWATER_BASE -> new BoundingBox(p.getX() - 21, p.getY(), p.getZ() - 21, p.getX() + 21, p.getY() + 7, p.getZ() + 21);
            case MAD_SCIENTIST_HOUSE -> new BoundingBox(p.getX() - 5, p.getY(), p.getZ() - 5, p.getX() + 5, p.getY() + 5, p.getZ() + 5);
            case ANDROID_HOUSE -> new BoundingBox(p.getX() - 10, p.getY() - 2, p.getZ() - 13, p.getX() + 10, p.getY() + 4, p.getZ() + 10);
            case SAND_PIT -> new BoundingBox(p.getX() - 12, p.getY() - 9, p.getZ() - 12, p.getX() + 11, p.getY(), p.getZ() + 11);
        };
    }

    private static BlockState block(String id) {
        return ModBlocks.get(id).get().defaultBlockState();
    }

    private static void set(WorldGenLevel level, BoundingBox clip, BlockPos pos, BlockState state) {
        if (clip.isInside(pos)) level.setBlock(pos, state, 2);
    }
}
