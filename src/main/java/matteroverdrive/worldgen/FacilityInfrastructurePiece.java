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
 * Chunk-clipped infrastructure overlays for the native technology facilities.
 * This piece family owns traversal fixes, secure thresholds and reusable industrial
 * detailing without increasing the synchronous footprint of the primary room pieces.
 */
public final class FacilityInfrastructurePiece extends StructurePiece {
    public enum Kind {
        SERVICE_SPINE_X, SERVICE_SPINE_Z,
        SECURE_GATE_X, SECURE_GATE_Z,
        LADDER_UP_12, LADDER_DOWN_7, LADDER_DOWN_12,
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
                add(builder, facility, Kind.SERVICE_SPINE_X, c.offset(0, 6, 10));
                add(builder, facility, Kind.SECURE_GATE_X, c.offset(0, 0, 12));
            }
            case MATTER_REFINERY -> {
                int side = layout == 1 ? 1 : -1;
                add(builder, facility, Kind.SERVICE_SPINE_Z, c.offset(-10 * side, 5, 0));
                add(builder, facility, Kind.SECURE_GATE_X, c.offset(0, 0, 12));
                add(builder, facility, Kind.LADDER_UP_12, c.offset(26 * side, -7, 0));
            }
            case QUANTUM_RELAY_STATION -> {
                int d = layout == 1 ? -1 : 1;
                add(builder, facility, Kind.RESTORATION_TERMINAL, c.offset(0, 1, 3));
                add(builder, facility, Kind.SERVICE_SPINE_X, c.offset(0, 6, 8));
                add(builder, facility, Kind.SECURE_GATE_X, c.offset(0, 0, -10));
                add(builder, facility, Kind.SECURE_GATE_Z, c.offset(9 * d, 0, 6));
            }
            case ANDROID_COMMAND_BUNKER -> {
                if (layout == 0) add(builder, facility, Kind.SECURE_GATE_X, c.offset(0, 0, 12));
                else if (layout == 1) add(builder, facility, Kind.SECURE_GATE_Z, c.offset(10, 0, 0));
                else add(builder, facility, Kind.SECURE_GATE_Z, c.offset(-10, 0, 0));
                add(builder, facility, Kind.LADDER_DOWN_7, c.offset(0, 7, -24));
            }
            case FUSION_RESEARCH_COMPLEX -> {
                int turn = layout == 1 ? -1 : 1;
                add(builder, facility, Kind.RESTORATION_TERMINAL, c.offset(0, 1, -6));
                add(builder, facility, Kind.SERVICE_SPINE_X, c.offset(0, 6, 0));
                add(builder, facility, Kind.SECURE_GATE_Z, c.offset(13 * turn, 0, 0));
            }
            case BLACK_SITE -> {
                add(builder, facility, Kind.SECURE_GATE_X, c.offset(0, 0, 11));
                add(builder, facility, Kind.LADDER_DOWN_12, c.offset(0, 12, -27));
                add(builder, facility, Kind.BLACK_VAULT_STAIR, c.offset(0, 0, 13));
            }
        }
    }

    private static void add(StructurePiecesBuilder builder, TechnologyFacilityStructure.Kind facility, Kind kind, BlockPos origin) {
        builder.addPiece(new FacilityInfrastructurePiece(facility, kind, origin));
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        switch (kind) {
            case SERVICE_SPINE_X -> serviceSpine(level, chunkBox, true);
            case SERVICE_SPINE_Z -> serviceSpine(level, chunkBox, false);
            case SECURE_GATE_X -> secureGate(level, chunkBox, true);
            case SECURE_GATE_Z -> secureGate(level, chunkBox, false);
            case LADDER_UP_12 -> ladder(level, chunkBox, 1, 11, 2);
            case LADDER_DOWN_7 -> ladder(level, chunkBox, -6, 0, 5);
            case LADDER_DOWN_12 -> ladder(level, chunkBox, -11, -1, 2);
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

    private void ladder(WorldGenLevel level, BoundingBox clip, int minY, int maxY, int insideZ) {
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH);
        for (int y = minY; y <= maxY; y++) set(level, clip, origin.offset(0, y, insideZ), ladder);
        set(level, clip, origin.offset(1, minY, insideZ), detail("warning_light", Direction.NORTH, Blocks.REDSTONE_LAMP));
    }

    private void blackVaultStair(WorldGenLevel level, BoundingBox clip) {
        BlockState stair = Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
        BlockState wall = mod("decorative.carbon_fiber_plate", Blocks.REINFORCED_DEEPSLATE);
        BlockState lamp = detail("warning_light", Direction.NORTH, Blocks.REDSTONE_LAMP);

        for (int step = 0; step <= 6; step++) {
            int y = -step;
            int z = step;
            for (int x = -1; x <= 1; x++) {
                for (int clear = 1; clear <= 3; clear++) set(level, clip, origin.offset(x, y + clear, z), Blocks.AIR.defaultBlockState());
            }
            set(level, clip, origin.offset(0, y, z), stair);
            set(level, clip, origin.offset(-2, y + 1, z), wall);
            set(level, clip, origin.offset(2, y + 1, z), wall);
            if ((step & 1) == 0) set(level, clip, origin.offset(2, y + 2, z), lamp);
        }
    }

    private BlockState detail(String id, Direction facing, Block fallback) {
        BlockState state = mod(id, fallback);
        if (state.getBlock() instanceof IndustrialDetailBlock) return state.setValue(IndustrialDetailBlock.FACING, facing);
        return state;
    }

    private BlockState securityDoor(Direction facing) {
        BlockState state = mod("security_door", Blocks.IRON_BLOCK);
        if (state.getBlock() instanceof SecurityDoorBlock) {
            return state.setValue(SecurityDoorBlock.FACING, facing).setValue(SecurityDoorBlock.OPEN, false);
        }
        return state;
    }

    private static BoundingBox boxFor(Kind kind, BlockPos p) {
        return switch (kind) {
            case SERVICE_SPINE_X -> new BoundingBox(p.getX() - 8, p.getY(), p.getZ() - 1, p.getX() + 8, p.getY() + 4, p.getZ() + 1);
            case SERVICE_SPINE_Z -> new BoundingBox(p.getX() - 1, p.getY(), p.getZ() - 8, p.getX() + 1, p.getY() + 4, p.getZ() + 8);
            case SECURE_GATE_X -> new BoundingBox(p.getX() - 2, p.getY(), p.getZ(), p.getX() + 2, p.getY() + 4, p.getZ());
            case SECURE_GATE_Z -> new BoundingBox(p.getX(), p.getY(), p.getZ() - 2, p.getX(), p.getY() + 4, p.getZ() + 2);
            case LADDER_UP_12 -> new BoundingBox(p.getX() - 1, p.getY(), p.getZ() + 2, p.getX() + 1, p.getY() + 11, p.getZ() + 2);
            case LADDER_DOWN_7 -> new BoundingBox(p.getX() - 1, p.getY() - 6, p.getZ() + 5, p.getX() + 1, p.getY(), p.getZ() + 5);
            case LADDER_DOWN_12 -> new BoundingBox(p.getX() - 1, p.getY() - 11, p.getZ() + 2, p.getX() + 1, p.getY() - 1, p.getZ() + 2);
            case BLACK_VAULT_STAIR -> new BoundingBox(p.getX() - 2, p.getY() - 6, p.getZ(), p.getX() + 2, p.getY() + 3, p.getZ() + 6);
            case RESTORATION_TERMINAL -> new BoundingBox(p.getX(), p.getY(), p.getZ(), p.getX(), p.getY() + 1, p.getZ());
        };
    }

    private static BlockState mod(String id, Block fallback) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("matteroverdrive", id));
        return block == null || block == Blocks.AIR ? fallback.defaultBlockState() : block.defaultBlockState();
    }

    private void set(WorldGenLevel level, BoundingBox clip, BlockPos pos, BlockState state) {
        if (!clip.isInside(pos) || !getBoundingBox().isInside(pos)) return;
        if (level.getBlockState(pos).is(Blocks.BEDROCK)) return;
        level.setBlock(pos, state, 2);
    }
}
