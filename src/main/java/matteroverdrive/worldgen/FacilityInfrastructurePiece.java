package matteroverdrive.worldgen;

import matteroverdrive.block.IndustrialDetailBlock;
import matteroverdrive.block.SecurityDoorBlock;
import matteroverdrive.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Chunk-clipped infrastructure overlays for native technology facilities.
 *
 * This piece family owns traversal fixes, secure thresholds and reusable industrial
 * detailing without expanding the synchronous footprint of the primary room pieces.
 * Every write is clipped to both the active chunk box and this piece's owning box.
 */
public final class FacilityInfrastructurePiece extends StructurePiece {
    public enum Kind {
        SERVICE_SPINE_X, SERVICE_SPINE_Z, SERVICE_ACCESS,
        SERVICE_BRIDGE_LINK_NORTH, SERVICE_BRIDGE_LINK_SOUTH,
        SERVICE_CORRIDOR_X, SERVICE_CORRIDOR_Z, BLACK_LOWER_CORRIDOR_X,
        SECURE_GATE_X, SECURE_GATE_Z,
        LOWERED_STEP_EAST, LOWERED_STEP_WEST, LOWERED_STEP_SOUTH,
        LADDER_UP_12, LADDER_DOWN_7, LADDER_DOWN_12,
        ENTRANCE_STAIR_NORTH_3, PLANT_EAST_ENTRANCE_FIX, BLACK_SURFACE_HATCH,
        BLACK_VAULT_STAIR, RESTORATION_TERMINAL
    }

    private final TechnologyFacilityStructure.Kind facility;
    private final Kind kind;
    private final BlockPos origin;

    public FacilityInfrastructurePiece(TechnologyFacilityStructure.Kind facility, Kind kind, BlockPos origin) {
        super(ModStructures.FACILITY_INFRASTRUCTURE_PIECE.get(), 0, boxFor(kind, origin));
        this.facility = facility;
        this.kind = kind;
        this.origin = origin;
    }

    public FacilityInfrastructurePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.FACILITY_INFRASTRUCTURE_PIECE.get(), tag);
        this.facility = TechnologyFacilityStructure.Kind.valueOf(tag.getString("MOFacility"));
        this.kind = Kind.valueOf(tag.getString("MOInfrastructure"));
        this.origin = new BlockPos(tag.getInt("MOX"), tag.getInt("MOY"), tag.getInt("MOZ"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("MOFacility", facility.name());
        tag.putString("MOInfrastructure", kind.name());
        tag.putInt("MOX", origin.getX());
        tag.putInt("MOY", origin.getY());
        tag.putInt("MOZ", origin.getZ());
    }

    public static void assemble(StructurePiecesBuilder builder, TechnologyFacilityStructure.Kind facility,
                                BlockPos c, int layout) {
        switch (facility) {
            case SYNTHETIC_MANUFACTURING_PLANT -> {
                // Overlay the existing gantry at one deck height instead of creating a second deck.
                add(builder, facility, Kind.SERVICE_SPINE_X, c.offset(0, 5, 10));
                add(builder, facility, Kind.SERVICE_ACCESS, c.offset(-8, 0, 7));
                if (layout == 0) {
                    add(builder, facility, Kind.SECURE_GATE_X, c.offset(0, 0, 12));
                } else if (layout == 1) {
                    // Shipping is approached from the west in layout 1.
                    add(builder, facility, Kind.SECURE_GATE_Z, c.offset(-6, 0, 18));
                    add(builder, facility, Kind.PLANT_EAST_ENTRANCE_FIX, c.offset(18, 0, 0));
                } else {
                    // Layout 2's original offset corridors did not actually meet the core.
                    add(builder, facility, Kind.SERVICE_CORRIDOR_X, c.offset(-12, 0, 0));
                    add(builder, facility, Kind.SERVICE_CORRIDOR_Z, c.offset(-18, 0, 6));
                    add(builder, facility, Kind.SERVICE_CORRIDOR_X, c.offset(12, 0, 0));
                    add(builder, facility, Kind.SERVICE_CORRIDOR_Z, c.offset(18, 0, 6));
                    add(builder, facility, Kind.SERVICE_CORRIDOR_Z, c.offset(0, 0, 11));
                    add(builder, facility, Kind.SECURE_GATE_X, c.offset(0, 0, 17));
                }
            }
            case MATTER_REFINERY -> {
                int side = layout == 1 ? 1 : -1;
                add(builder, facility, Kind.SERVICE_SPINE_Z, c.offset(0, 5, 10));
                add(builder, facility, Kind.SERVICE_ACCESS, c.offset(-3, 0, 18));
                add(builder, facility, Kind.SECURE_GATE_X, c.offset(0, 0, 12));
                add(builder, facility, side > 0 ? Kind.LOWERED_STEP_EAST : Kind.LOWERED_STEP_WEST,
                        c.offset(12 * side, 0, 0));
                add(builder, facility, Kind.LADDER_UP_12, c.offset(26 * side, -7, 0));
            }
            case QUANTUM_RELAY_STATION -> {
                int d = layout == 1 ? -1 : 1;
                add(builder, facility, Kind.RESTORATION_TERMINAL, c.offset(0, 1, 3));
                // Layout 2 already owns a y=7 observation deck: share that level for all variants.
                add(builder, facility, Kind.SERVICE_SPINE_X, c.offset(0, 7, 8));
                add(builder, facility, Kind.SERVICE_ACCESS, c.offset(-8, 0, 5));
                add(builder, facility, Kind.SECURE_GATE_X, c.offset(0, 0, -10));
                add(builder, facility, Kind.SECURE_GATE_Z, c.offset(9 * d, 0, 6));
            }
            case ANDROID_COMMAND_BUNKER -> {
                if (layout == 0) add(builder, facility, Kind.SECURE_GATE_X, c.offset(0, 0, 12));
                else if (layout == 1) add(builder, facility, Kind.SECURE_GATE_Z, c.offset(10, 0, 0));
                else add(builder, facility, Kind.SECURE_GATE_Z, c.offset(-10, 0, 0));
                add(builder, facility, Kind.LADDER_DOWN_7, c.offset(0, 7, -24));
                add(builder, facility, Kind.ENTRANCE_STAIR_NORTH_3, c.offset(0, 7, -30));
            }
            case FUSION_RESEARCH_COMPLEX -> {
                int turn = layout == 1 ? -1 : 1;
                add(builder, facility, Kind.RESTORATION_TERMINAL, c.offset(0, 1, -6));
                add(builder, facility, Kind.SERVICE_SPINE_X, c.offset(0, 6, 0));
                add(builder, facility, Kind.SERVICE_ACCESS, c.offset(8, 0, -3));
                add(builder, facility,
                        layout == 2 ? Kind.SERVICE_BRIDGE_LINK_NORTH : Kind.SERVICE_BRIDGE_LINK_SOUTH,
                        c.offset(0, 6, 0));
                add(builder, facility, Kind.SECURE_GATE_Z, c.offset(13 * turn, 0, 0));
                add(builder, facility, Kind.LOWERED_STEP_SOUTH, c.offset(0, 0, 14));
                add(builder, facility, Kind.ENTRANCE_STAIR_NORTH_3, c.offset(0, 0, -28));
            }
            case BLACK_SITE -> {
                add(builder, facility, Kind.SECURE_GATE_X, c.offset(0, 0, 11));
                add(builder, facility, Kind.LADDER_DOWN_12, c.offset(0, 12, -27));
                add(builder, facility, Kind.BLACK_SURFACE_HATCH, c.offset(0, 12, -27));
                add(builder, facility, Kind.BLACK_VAULT_STAIR, c.offset(0, 0, 13));
                if (layout == 2) add(builder, facility, Kind.BLACK_LOWER_CORRIDOR_X, c.offset(-9, -6, 19));
            }
        }
    }

    private static void add(StructurePiecesBuilder builder, TechnologyFacilityStructure.Kind facility,
                            Kind kind, BlockPos origin) {
        builder.addPiece(new FacilityInfrastructurePiece(facility, kind, origin));
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        switch (kind) {
            case SERVICE_SPINE_X -> serviceSpine(level, chunkBox, true);
            case SERVICE_SPINE_Z -> serviceSpine(level, chunkBox, false);
            case SERVICE_ACCESS -> serviceAccess(level, chunkBox);
            case SERVICE_BRIDGE_LINK_NORTH -> serviceBridgeLink(level, chunkBox, Direction.NORTH);
            case SERVICE_BRIDGE_LINK_SOUTH -> serviceBridgeLink(level, chunkBox, Direction.SOUTH);
            case SERVICE_CORRIDOR_X -> serviceCorridor(level, chunkBox, true, false);
            case SERVICE_CORRIDOR_Z -> serviceCorridor(level, chunkBox, false, false);
            case BLACK_LOWER_CORRIDOR_X -> serviceCorridor(level, chunkBox, true, true);
            case SECURE_GATE_X -> secureGate(level, chunkBox, true);
            case SECURE_GATE_Z -> secureGate(level, chunkBox, false);
            case LOWERED_STEP_EAST -> loweredTransition(level, chunkBox, Direction.EAST);
            case LOWERED_STEP_WEST -> loweredTransition(level, chunkBox, Direction.WEST);
            case LOWERED_STEP_SOUTH -> loweredTransition(level, chunkBox, Direction.SOUTH);
            case LADDER_UP_12 -> ladder(level, chunkBox, 1, 11, 2, false);
            case LADDER_DOWN_7 -> ladder(level, chunkBox, -6, 0, 5, true);
            case LADDER_DOWN_12 -> ladder(level, chunkBox, -11, -1, 2, true);
            case ENTRANCE_STAIR_NORTH_3 -> entranceStairNorth3(level, chunkBox);
            case PLANT_EAST_ENTRANCE_FIX -> plantEastEntranceFix(level, chunkBox);
            case BLACK_SURFACE_HATCH -> blackSurfaceHatch(level, chunkBox);
            case BLACK_VAULT_STAIR -> blackVaultStair(level, chunkBox);
            case RESTORATION_TERMINAL -> restorationTerminal(level, chunkBox);
        }
    }

    private void restorationTerminal(WorldGenLevel level, BoundingBox clip) {
        set(level, clip, origin, mod("facility_network_controller", Blocks.IRON_BLOCK));
        set(level, clip, origin.above(), detail("warning_light", Direction.NORTH, Blocks.REDSTONE_LAMP));
    }

    private void serviceSpine(WorldGenLevel level, BoundingBox clip, boolean xAxis) {
        BlockState catwalk = detail("industrial_catwalk", xAxis ? Direction.EAST : Direction.SOUTH, Blocks.IRON_BLOCK);
        BlockState railA = detail("industrial_railing", xAxis ? Direction.NORTH : Direction.EAST, Blocks.IRON_BARS);
        BlockState railB = detail("industrial_railing", xAxis ? Direction.SOUTH : Direction.WEST, Blocks.IRON_BARS);
        BlockState tray = detail("cable_tray", xAxis ? Direction.EAST : Direction.SOUTH, Blocks.IRON_BARS);
        BlockState warning = detail("warning_light", xAxis ? Direction.NORTH : Direction.EAST, Blocks.REDSTONE_LAMP);
        BlockState panel = detail("damaged_panel", xAxis ? Direction.SOUTH : Direction.WEST, Blocks.IRON_BLOCK);

        for (int a = -8; a <= 8; a++) {
            int x = xAxis ? a : 0;
            int z = xAxis ? 0 : a;
            set(level, clip, origin.offset(x, 0, z), catwalk);

            int ax = xAxis ? a : -1;
            int az = xAxis ? -1 : a;
            int bx = xAxis ? a : 1;
            int bz = xAxis ? 1 : a;
            set(level, clip, origin.offset(ax, 1, az), railA);
            set(level, clip, origin.offset(bx, 1, bz), railB);

            if ((a & 1) == 0) set(level, clip, origin.offset(x, 3, z), tray);
            if (a % 4 == 0) set(level, clip, origin.offset(ax, 2, az), warning);
        }
        int broken = Math.floorMod(origin.getX() * 31 + origin.getZ() * 17 + facility.ordinal(), 11) - 5;
        int px = xAxis ? broken : 1;
        int pz = xAxis ? 1 : broken;
        set(level, clip, origin.offset(px, 2, pz), panel);
    }

    private void serviceAccess(WorldGenLevel level, BoundingBox clip) {
        int deckY;
        Direction towardDeck;
        switch (facility) {
            case QUANTUM_RELAY_STATION -> { deckY = 7; towardDeck = Direction.SOUTH; }
            case FUSION_RESEARCH_COMPLEX -> { deckY = 6; towardDeck = Direction.SOUTH; }
            case MATTER_REFINERY -> { deckY = 5; towardDeck = Direction.EAST; }
            default -> { deckY = 5; towardDeck = Direction.SOUTH; }
        }

        Direction supportSide = towardDeck.getOpposite();
        BlockState support = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, towardDeck);
        BlockState catwalk = detail("industrial_catwalk", towardDeck, Blocks.IRON_BLOCK);
        BlockState warning = detail("warning_light", towardDeck, Blocks.REDSTONE_LAMP);

        set(level, clip, origin, mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES));
        for (int y = 1; y <= deckY + 1; y++) {
            set(level, clip, origin.relative(supportSide).above(y), support);
            set(level, clip, origin.above(y), ladder);
        }
        for (int distance = 1; distance <= 3; distance++) {
            BlockPos landing = origin.relative(towardDeck, distance).above(deckY);
            set(level, clip, landing, catwalk);
            set(level, clip, landing.above(), Blocks.AIR.defaultBlockState());
            set(level, clip, landing.above(2), Blocks.AIR.defaultBlockState());
        }
        set(level, clip, origin.relative(towardDeck.getClockWise()).above(Math.max(2, deckY - 2)), warning);
    }

    private void serviceBridgeLink(WorldGenLevel level, BoundingBox clip, Direction direction) {
        BlockState catwalk = detail("industrial_catwalk", Direction.SOUTH, Blocks.IRON_BLOCK);
        BlockState railWest = detail("industrial_railing", Direction.WEST, Blocks.IRON_BARS);
        BlockState railEast = detail("industrial_railing", Direction.EAST, Blocks.IRON_BARS);
        for (int distance = 0; distance <= 2; distance++) {
            BlockPos center = origin.relative(direction, distance);
            set(level, clip, center, catwalk);
            set(level, clip, center.west().above(), railWest);
            set(level, clip, center.east().above(), railEast);
            set(level, clip, center.above(), Blocks.AIR.defaultBlockState());
            set(level, clip, center.above(2), Blocks.AIR.defaultBlockState());
        }
    }

    private void serviceCorridor(WorldGenLevel level, BoundingBox clip, boolean xAxis, boolean black) {
        BlockState wall = black ? mod("decorative.carbon_fiber_plate", Blocks.REINFORCED_DEEPSLATE)
                : mod("decorative.tritanium_plate", Blocks.IRON_BLOCK);
        BlockState floor = black ? mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES)
                : mod("decorative.floor_tiles", Blocks.SMOOTH_STONE);
        BlockState frame = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState lamp = detail("warning_light", xAxis ? Direction.NORTH : Direction.EAST, Blocks.REDSTONE_LAMP);

        for (int a = -5; a <= 5; a++) for (int b = -2; b <= 2; b++) {
            int x = xAxis ? a : b;
            int z = xAxis ? b : a;
            set(level, clip, origin.offset(x, 0, z), floor);
            for (int y = 1; y <= 4; y++) {
                boolean side = Math.abs(b) == 2;
                set(level, clip, origin.offset(x, y, z), side ? ((a % 4 == 0) ? frame : wall)
                        : Blocks.AIR.defaultBlockState());
            }
            set(level, clip, origin.offset(x, 5, z), (a % 4 == 0) ? frame : wall);
        }
        for (int a = -4; a <= 4; a += 4) {
            int x = xAxis ? a : 0;
            int z = xAxis ? 0 : a;
            set(level, clip, origin.offset(x, 4, z), lamp);
        }
    }

    private void secureGate(WorldGenLevel level, BoundingBox clip, boolean xAxis) {
        BlockState frame = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        Direction doorFacing = xAxis ? Direction.NORTH : Direction.EAST;
        BlockState door = securityDoor(doorFacing);
        BlockState warning = detail("warning_light", doorFacing, Blocks.REDSTONE_LAMP);

        for (int lateral = -2; lateral <= 2; lateral++) {
            for (int y = 1; y <= 4; y++) {
                int x = xAxis ? lateral : 0;
                int z = xAxis ? 0 : lateral;
                boolean center = lateral == 0 && y <= 3;
                set(level, clip, origin.offset(x, y, z), center ? door : frame);
            }
        }
        int wx = xAxis ? 2 : 0;
        int wz = xAxis ? 0 : 2;
        set(level, clip, origin.offset(wx, 3, wz), warning);
    }

    private void loweredTransition(WorldGenLevel level, BoundingBox clip, Direction towardLowerRoom) {
        BlockState floor = facility == TechnologyFacilityStructure.Kind.MATTER_REFINERY
                ? mod("decorative.floor_tiles_green", Blocks.OXIDIZED_COPPER)
                : mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES);
        Direction lateral = towardLowerRoom.getAxis() == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;

        for (int lane = -1; lane <= 1; lane++) {
            BlockPos approach = origin.relative(towardLowerRoom.getOpposite()).relative(lateral, lane);
            BlockPos middle = origin.relative(lateral, lane);
            BlockPos lower = origin.relative(towardLowerRoom).relative(lateral, lane);
            set(level, clip, approach, floor);
            set(level, clip, middle.below(), floor);
            set(level, clip, lower.below(2), floor);
            clearHeadroom(level, clip, approach, 1);
            clearHeadroom(level, clip, middle.below(), 1);
            clearHeadroom(level, clip, lower.below(2), 1);
        }
    }

    private void ladder(WorldGenLevel level, BoundingBox clip, int minY, int maxY, int insideZ, boolean sideExit) {
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH);
        BlockState support = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState floor = mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES);
        BlockState warning = detail("warning_light", Direction.NORTH, Blocks.REDSTONE_LAMP);

        for (int y = minY; y <= maxY; y++) {
            BlockPos backing = origin.offset(0, y, insideZ + 1);
            if (level.getBlockState(backing).isAir()) set(level, clip, backing, support);
            set(level, clip, origin.offset(0, y, insideZ), ladder);
        }

        // Explicit bottom landing: the old shaft pieces clear their interior floor.
        set(level, clip, origin.offset(0, minY - 1, insideZ), floor);
        set(level, clip, origin.offset(1, minY - 1, insideZ), floor);
        set(level, clip, origin.offset(1, minY - 1, insideZ + 1), floor);
        set(level, clip, origin.offset(1, minY + 1, insideZ), warning);

        if (sideExit) {
            // Preserve the backing block directly behind the ladder, but cut a two-block-wide
            // lower landing beside it into the adjoining checkpoint/security room.
            for (int y = minY; y <= minY + 2; y++) {
                set(level, clip, origin.offset(1, y, insideZ), Blocks.AIR.defaultBlockState());
                set(level, clip, origin.offset(1, y, insideZ + 1), Blocks.AIR.defaultBlockState());
            }
        }
    }

    private void entranceStairNorth3(WorldGenLevel level, BoundingBox clip) {
        BlockState stair = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
        BlockState support = mod("decorative.tritanium_plate", Blocks.POLISHED_DEEPSLATE);
        for (int step = 1; step <= 3; step++) {
            int y = step - 1;
            int z = -step;
            for (int x = -2; x <= 2; x++) {
                for (int fillY = 0; fillY < y; fillY++) set(level, clip, origin.offset(x, fillY, z), support);
                set(level, clip, origin.offset(x, y, z), stair);
                for (int clear = 1; clear <= 3; clear++) {
                    set(level, clip, origin.offset(x, y + clear, z), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private void plantEastEntranceFix(WorldGenLevel level, BoundingBox clip) {
        BlockState floor = mod("decorative.floor_tiles", Blocks.SMOOTH_STONE);
        for (int z = -1; z <= 1; z++) {
            set(level, clip, origin.offset(-4, 0, z), floor);
            for (int y = 1; y <= 3; y++) {
                set(level, clip, origin.offset(-4, y, z), Blocks.AIR.defaultBlockState());
                set(level, clip, origin.offset(-3, y, z), Blocks.AIR.defaultBlockState());
            }
        }
    }

    private void blackSurfaceHatch(WorldGenLevel level, BoundingBox clip) {
        BlockState wall = mod("decorative.carbon_fiber_plate", Blocks.REINFORCED_DEEPSLATE);
        BlockState frame = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH);
        BlockState warning = detail("warning_light", Direction.NORTH, Blocks.REDSTONE_LAMP);

        // Black Site starts at surfaceY-16 and its main entrance top is surfaceY-4.
        // Continue the same ladder/support line four more blocks to a visible surface hatch.
        for (int y = 0; y <= 4; y++) {
            set(level, clip, origin.offset(0, y, 3), wall);
            set(level, clip, origin.offset(0, y, 2), ladder);
            for (int x = -1; x <= 1; x++) for (int z = 0; z <= 1; z++) {
                set(level, clip, origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
            }
        }
        for (int x = -2; x <= 2; x++) for (int z = -1; z <= 3; z++) {
            boolean edge = Math.abs(x) == 2 || z == -1 || z == 3;
            if (edge) set(level, clip, origin.offset(x, 4, z), frame);
        }
        set(level, clip, origin.offset(2, 5, 1), warning);
    }

    private void blackVaultStair(WorldGenLevel level, BoundingBox clip) {
        BlockState stair = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
        BlockState wall = mod("decorative.carbon_fiber_plate", Blocks.REINFORCED_DEEPSLATE);
        BlockState lamp = detail("warning_light", Direction.NORTH, Blocks.REDSTONE_LAMP);

        for (int step = 0; step <= 6; step++) {
            int y = -step;
            int z = step;
            for (int x = -1; x <= 1; x++) {
                for (int clear = 1; clear <= 3; clear++) {
                    set(level, clip, origin.offset(x, y + clear, z), Blocks.AIR.defaultBlockState());
                }
                set(level, clip, origin.offset(x, y, z), stair);
            }
            set(level, clip, origin.offset(-2, y + 1, z), wall);
            set(level, clip, origin.offset(2, y + 1, z), wall);
            if ((step & 1) == 0) set(level, clip, origin.offset(2, y + 2, z), lamp);
        }
    }

    private void clearHeadroom(WorldGenLevel level, BoundingBox clip, BlockPos floor, int extra) {
        for (int y = 1; y <= 3 + extra; y++) set(level, clip, floor.above(y), Blocks.AIR.defaultBlockState());
    }

    private BlockState detail(String id, Direction facing, Block fallback) {
        BlockState state = mod(id, fallback);
        if (state.getBlock() instanceof IndustrialDetailBlock) return state.setValue(IndustrialDetailBlock.FACING, facing);
        return state;
    }

    private BlockState securityDoor(Direction facing) {
        BlockState state = mod("security_door", Blocks.IRON_BLOCK);
        if (state.getBlock() instanceof SecurityDoorBlock) {
            return state.setValue(SecurityDoorBlock.FACING, facing)
                    .setValue(SecurityDoorBlock.OPEN, false)
                    .setValue(SecurityDoorBlock.POWERED, false);
        }
        return state;
    }

    private static BoundingBox boxFor(Kind kind, BlockPos p) {
        return switch (kind) {
            case SERVICE_SPINE_X -> new BoundingBox(p.getX() - 8, p.getY(), p.getZ() - 1,
                    p.getX() + 8, p.getY() + 4, p.getZ() + 1);
            case SERVICE_SPINE_Z -> new BoundingBox(p.getX() - 1, p.getY(), p.getZ() - 8,
                    p.getX() + 1, p.getY() + 4, p.getZ() + 8);
            case SERVICE_ACCESS -> new BoundingBox(p.getX() - 4, p.getY(), p.getZ() - 4,
                    p.getX() + 4, p.getY() + 8, p.getZ() + 4);
            case SERVICE_BRIDGE_LINK_NORTH, SERVICE_BRIDGE_LINK_SOUTH ->
                    new BoundingBox(p.getX() - 1, p.getY(), p.getZ() - 2,
                            p.getX() + 1, p.getY() + 3, p.getZ() + 2);
            case SERVICE_CORRIDOR_X, BLACK_LOWER_CORRIDOR_X ->
                    new BoundingBox(p.getX() - 5, p.getY(), p.getZ() - 2,
                            p.getX() + 5, p.getY() + 5, p.getZ() + 2);
            case SERVICE_CORRIDOR_Z -> new BoundingBox(p.getX() - 2, p.getY(), p.getZ() - 5,
                    p.getX() + 2, p.getY() + 5, p.getZ() + 5);
            case SECURE_GATE_X -> new BoundingBox(p.getX() - 2, p.getY(), p.getZ(),
                    p.getX() + 2, p.getY() + 4, p.getZ());
            case SECURE_GATE_Z -> new BoundingBox(p.getX(), p.getY(), p.getZ() - 2,
                    p.getX(), p.getY() + 4, p.getZ() + 2);
            case LOWERED_STEP_EAST, LOWERED_STEP_WEST, LOWERED_STEP_SOUTH ->
                    new BoundingBox(p.getX() - 2, p.getY() - 2, p.getZ() - 2,
                            p.getX() + 2, p.getY() + 4, p.getZ() + 2);
            case LADDER_UP_12 -> new BoundingBox(p.getX() - 2, p.getY(), p.getZ() + 1,
                    p.getX() + 2, p.getY() + 12, p.getZ() + 3);
            case LADDER_DOWN_7 -> new BoundingBox(p.getX() - 2, p.getY() - 7, p.getZ() + 4,
                    p.getX() + 2, p.getY() + 1, p.getZ() + 6);
            case LADDER_DOWN_12 -> new BoundingBox(p.getX() - 2, p.getY() - 12, p.getZ() + 1,
                    p.getX() + 2, p.getY(), p.getZ() + 3);
            case ENTRANCE_STAIR_NORTH_3 -> new BoundingBox(p.getX() - 2, p.getY(), p.getZ() - 3,
                    p.getX() + 2, p.getY() + 5, p.getZ());
            case PLANT_EAST_ENTRANCE_FIX -> new BoundingBox(p.getX() - 4, p.getY(), p.getZ() - 1,
                    p.getX() - 3, p.getY() + 3, p.getZ() + 1);
            case BLACK_SURFACE_HATCH -> new BoundingBox(p.getX() - 2, p.getY(), p.getZ() - 1,
                    p.getX() + 2, p.getY() + 5, p.getZ() + 3);
            case BLACK_VAULT_STAIR -> new BoundingBox(p.getX() - 2, p.getY() - 6, p.getZ(),
                    p.getX() + 2, p.getY() + 3, p.getZ() + 6);
            case RESTORATION_TERMINAL -> new BoundingBox(p.getX(), p.getY(), p.getZ(),
                    p.getX(), p.getY() + 1, p.getZ());
        };
    }

    private static BlockState mod(String id, Block fallback) {
        Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("matteroverdrive", id));
        return block == null || block == Blocks.AIR ? fallback.defaultBlockState() : block.defaultBlockState();
    }

    private void set(WorldGenLevel level, BoundingBox clip, BlockPos pos, BlockState state) {
        if (!clip.isInside(pos) || !getBoundingBox().isInside(pos)) return;
        if (level.getBlockState(pos).is(Blocks.BEDROCK)) return;
        level.setBlock(pos, state, 2);
    }
}
