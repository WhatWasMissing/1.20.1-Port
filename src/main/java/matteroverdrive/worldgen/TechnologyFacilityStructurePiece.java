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
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraftforge.registries.ForgeRegistries;

/** Room-sized pieces used by {@link TechnologyFacilityStructure}. */
public final class TechnologyFacilityStructurePiece extends StructurePiece {
    public enum Room {
        MANUFACTURING_CORE, FABRICATION_WING, ASSEMBLY_WING, SHIPPING_WING, PLANT_ENTRANCE,
        REFINERY_CORE, EXCAVATION_WING, STORAGE_WING, PROCESSING_WING, REFINERY_ENTRANCE,
        QUANTUM_CORE, RELAY_WING, POWER_WING, CONTROL_WING, RELAY_ENTRANCE,
        BUNKER_COMMAND, DRONE_BAY, ANDROID_BAY, ARMORY, BUNKER_ENTRANCE,
        FUSION_CORE, STABILIZER_WING, REACTOR_CONTROL, SERVICE_WING, FUSION_ENTRANCE,
        BLACK_CORE, BLACK_LAB, BLACK_CONTAINMENT, BLACK_VAULT, BLACK_SECURITY, BLACK_ENTRANCE
    }

    private final TechnologyFacilityStructure.Kind facility;
    private final Room room;
    private final BlockPos origin;

    public TechnologyFacilityStructurePiece(TechnologyFacilityStructure.Kind facility, Room room, BlockPos origin) {
        super(ModStructures.TECHNOLOGY_FACILITY_PIECE.get(), 0, boxFor(room, origin));
        this.facility = facility;
        this.room = room;
        this.origin = origin;
    }

    public TechnologyFacilityStructurePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.TECHNOLOGY_FACILITY_PIECE.get(), tag);
        this.facility = TechnologyFacilityStructure.Kind.valueOf(tag.getString("MOFacility"));
        this.room = Room.valueOf(tag.getString("MORoom"));
        this.origin = new BlockPos(tag.getInt("MOX"), tag.getInt("MOY"), tag.getInt("MOZ"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("MOFacility", facility.name());
        tag.putString("MORoom", room.name());
        tag.putInt("MOX", origin.getX());
        tag.putInt("MOY", origin.getY());
        tag.putInt("MOZ", origin.getZ());
    }

    public static void assemble(StructurePiecesBuilder builder, TechnologyFacilityStructure.Kind kind, BlockPos c) {
        switch (kind) {
            case SYNTHETIC_MANUFACTURING_PLANT -> {
                add(builder, kind, Room.MANUFACTURING_CORE, c);
                add(builder, kind, Room.FABRICATION_WING, c.offset(-14, 0, 0));
                add(builder, kind, Room.ASSEMBLY_WING, c.offset(14, 0, 0));
                add(builder, kind, Room.SHIPPING_WING, c.offset(0, 0, 14));
                add(builder, kind, Room.PLANT_ENTRANCE, c.offset(0, 0, -14));
            }
            case MATTER_REFINERY -> {
                add(builder, kind, Room.REFINERY_CORE, c);
                add(builder, kind, Room.EXCAVATION_WING, c.offset(-15, 0, 0));
                add(builder, kind, Room.STORAGE_WING, c.offset(15, 0, 0));
                add(builder, kind, Room.PROCESSING_WING, c.offset(0, 0, 15));
                add(builder, kind, Room.REFINERY_ENTRANCE, c.offset(0, 0, -15));
            }
            case QUANTUM_RELAY_STATION -> {
                add(builder, kind, Room.QUANTUM_CORE, c);
                add(builder, kind, Room.RELAY_WING, c.offset(-13, 0, 0));
                add(builder, kind, Room.POWER_WING, c.offset(13, 0, 0));
                add(builder, kind, Room.CONTROL_WING, c.offset(0, 0, 13));
                add(builder, kind, Room.RELAY_ENTRANCE, c.offset(0, 0, -13));
            }
            case ANDROID_COMMAND_BUNKER -> {
                add(builder, kind, Room.BUNKER_COMMAND, c);
                add(builder, kind, Room.DRONE_BAY, c.offset(-15, 0, 0));
                add(builder, kind, Room.ANDROID_BAY, c.offset(15, 0, 0));
                add(builder, kind, Room.ARMORY, c.offset(0, 0, 15));
                add(builder, kind, Room.BUNKER_ENTRANCE, c.offset(0, 0, -15));
            }
            case FUSION_RESEARCH_COMPLEX -> {
                add(builder, kind, Room.FUSION_CORE, c);
                add(builder, kind, Room.STABILIZER_WING, c.offset(-17, 0, 0));
                add(builder, kind, Room.REACTOR_CONTROL, c.offset(17, 0, 0));
                add(builder, kind, Room.SERVICE_WING, c.offset(0, 0, 17));
                add(builder, kind, Room.FUSION_ENTRANCE, c.offset(0, 0, -17));
            }
            case BLACK_SITE -> {
                add(builder, kind, Room.BLACK_CORE, c);
                add(builder, kind, Room.BLACK_LAB, c.offset(-15, 0, 0));
                add(builder, kind, Room.BLACK_CONTAINMENT, c.offset(15, 0, 0));
                add(builder, kind, Room.BLACK_VAULT, c.offset(0, 0, 15));
                add(builder, kind, Room.BLACK_SECURITY, c.offset(0, 0, -15));
                add(builder, kind, Room.BLACK_ENTRANCE, c.offset(0, 7, -25));
            }
        }
    }

    private static void add(StructurePiecesBuilder builder, TechnologyFacilityStructure.Kind kind, Room room, BlockPos pos) {
        builder.addPiece(new TechnologyFacilityStructurePiece(kind, room, pos));
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        switch (room) {
            case MANUFACTURING_CORE -> room(level, chunkBox, 6, 6, 5, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK), mod("decorative.floor_tiles", Blocks.SMOOTH_STONE), true,
                    new Placement(0, 1, 0, "facility_network_controller", Blocks.IRON_BLOCK),
                    new Placement(-2, 1, 1, "inscriber", Blocks.BLAST_FURNACE),
                    new Placement(2, 1, 1, "replicator", Blocks.SMITHING_TABLE));
            case FABRICATION_WING -> room(level, chunkBox, 6, 5, 4, mod("decorative.white_plate", Blocks.QUARTZ_BLOCK), mod("decorative.floor_tiles", Blocks.SMOOTH_STONE), false,
                    new Placement(-2, 1, 0, "inscriber", Blocks.BLAST_FURNACE), new Placement(2, 1, 0, "replicator", Blocks.SMITHING_TABLE));
            case ASSEMBLY_WING -> room(level, chunkBox, 6, 5, 4, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK), mod("decorative.floor_tiles", Blocks.SMOOTH_STONE), false,
                    new Placement(-2, 1, 0, "android_station", Blocks.SMITHING_TABLE), new Placement(2, 1, 0, "charging_station", Blocks.LODESTONE));
            case SHIPPING_WING -> room(level, chunkBox, 5, 6, 4, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK), mod("decorative.floor_tiles", Blocks.SMOOTH_STONE), false,
                    new Placement(-2, 1, 0, "tritanium_crate", Blocks.BARREL), new Placement(0, 1, 0, "tritanium_crate_blue", Blocks.BARREL), new Placement(2, 1, 0, "tritanium_crate_lime", Blocks.BARREL));
            case PLANT_ENTRANCE -> entrance(level, chunkBox, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK));

            case REFINERY_CORE -> room(level, chunkBox, 7, 7, 5, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK), mod("decorative.floor_tiles", Blocks.SMOOTH_STONE), true,
                    new Placement(0, 1, 0, "facility_network_controller", Blocks.IRON_BLOCK), new Placement(-2, 1, 1, "matter_storage_matrix", Blocks.IRON_BLOCK), new Placement(2, 1, 1, "holographic_status_panel", Blocks.SEA_LANTERN));
            case EXCAVATION_WING -> room(level, chunkBox, 6, 6, 4, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK), mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES), false,
                    new Placement(0, 1, 0, "matter_excavator", Blocks.BLAST_FURNACE));
            case STORAGE_WING -> room(level, chunkBox, 6, 6, 4, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK), mod("decorative.floor_tiles", Blocks.SMOOTH_STONE), false,
                    new Placement(-2, 1, 0, "matter_storage_matrix", Blocks.IRON_BLOCK), new Placement(2, 1, 0, "matter_storage_matrix", Blocks.IRON_BLOCK));
            case PROCESSING_WING -> room(level, chunkBox, 6, 6, 4, mod("decorative.white_plate", Blocks.QUARTZ_BLOCK), mod("decorative.floor_tiles", Blocks.SMOOTH_STONE), false,
                    new Placement(-2, 1, 0, "decomposer", Blocks.BLAST_FURNACE), new Placement(2, 1, 0, "matter_analyzer", Blocks.LECTERN));
            case REFINERY_ENTRANCE -> entrance(level, chunkBox, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK));

            case QUANTUM_CORE -> towerRoom(level, chunkBox, 6, 6, 8, new Placement(0, 1, 0, "quantum_power_relay", Blocks.RESPAWN_ANCHOR));
            case RELAY_WING -> room(level, chunkBox, 5, 5, 4, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK), mod("decorative.floor_tiles", Blocks.SMOOTH_STONE), false,
                    new Placement(0, 1, 0, "quantum_power_relay", Blocks.RESPAWN_ANCHOR));
            case POWER_WING -> room(level, chunkBox, 5, 5, 4, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK), mod("decorative.floor_tiles", Blocks.SMOOTH_STONE), false,
                    new Placement(0, 1, 0, "grid_capacitor", Blocks.IRON_BLOCK));
            case CONTROL_WING -> room(level, chunkBox, 5, 5, 4, mod("decorative.white_plate", Blocks.QUARTZ_BLOCK), mod("decorative.floor_tiles", Blocks.SMOOTH_STONE), false,
                    new Placement(-1, 1, 0, "facility_network_controller", Blocks.IRON_BLOCK), new Placement(1, 1, 0, "holographic_status_panel", Blocks.SEA_LANTERN));
            case RELAY_ENTRANCE -> entrance(level, chunkBox, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK));

            case BUNKER_COMMAND -> room(level, chunkBox, 7, 7, 5, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK), mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES), true,
                    new Placement(0, 1, 0, "facility_network_controller", Blocks.IRON_BLOCK), new Placement(-2, 1, 1, "holographic_status_panel", Blocks.SEA_LANTERN));
            case DRONE_BAY -> room(level, chunkBox, 6, 6, 5, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK), mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES), false,
                    new Placement(-2, 1, 0, "android_spawner", Blocks.IRON_BLOCK), new Placement(2, 1, 0, "charging_station", Blocks.LODESTONE));
            case ANDROID_BAY -> room(level, chunkBox, 6, 6, 5, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK), mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES), false,
                    new Placement(-2, 1, 0, "android_station", Blocks.SMITHING_TABLE), new Placement(2, 1, 0, "android_induction_relay", Blocks.LODESTONE));
            case ARMORY -> room(level, chunkBox, 6, 6, 5, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK), mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES), false,
                    new Placement(-2, 1, 0, "tritanium_crate_red", Blocks.BARREL), new Placement(2, 1, 0, "tritanium_crate", Blocks.BARREL));
            case BUNKER_ENTRANCE -> bunkerEntrance(level, chunkBox);

            case FUSION_CORE -> fusionCore(level, chunkBox);
            case STABILIZER_WING -> room(level, chunkBox, 7, 6, 5, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK), mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES), false,
                    new Placement(-2, 1, 0, "gravitational_stabilizer", Blocks.OBSIDIAN), new Placement(2, 1, 0, "gravitational_stabilizer", Blocks.OBSIDIAN));
            case REACTOR_CONTROL -> room(level, chunkBox, 7, 6, 5, mod("decorative.white_plate", Blocks.QUARTZ_BLOCK), mod("decorative.floor_tiles", Blocks.SMOOTH_STONE), false,
                    new Placement(-2, 1, 0, "fusion_reactor_controller", Blocks.IRON_BLOCK), new Placement(0, 1, 0, "facility_network_controller", Blocks.IRON_BLOCK), new Placement(2, 1, 0, "holographic_status_panel", Blocks.SEA_LANTERN));
            case SERVICE_WING -> room(level, chunkBox, 6, 7, 5, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK), mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES), false,
                    new Placement(-2, 1, 0, "grid_capacitor", Blocks.IRON_BLOCK), new Placement(2, 1, 0, "anomaly_containment_unit", Blocks.OBSIDIAN));
            case FUSION_ENTRANCE -> entrance(level, chunkBox, mod("decorative.tritanium_plate", Blocks.IRON_BLOCK));

            case BLACK_CORE -> room(level, chunkBox, 7, 7, 5, Blocks.REINFORCED_DEEPSLATE.defaultBlockState(), mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES), true,
                    new Placement(0, 1, 0, "facility_network_controller", Blocks.IRON_BLOCK), new Placement(0, 1, 2, "holographic_status_panel", Blocks.SEA_LANTERN));
            case BLACK_LAB -> room(level, chunkBox, 6, 6, 5, Blocks.REINFORCED_DEEPSLATE.defaultBlockState(), mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES), false,
                    new Placement(-2, 1, 0, "matter_analyzer", Blocks.LECTERN), new Placement(2, 1, 0, "android_station", Blocks.SMITHING_TABLE));
            case BLACK_CONTAINMENT -> room(level, chunkBox, 6, 6, 5, Blocks.REINFORCED_DEEPSLATE.defaultBlockState(), mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES), false,
                    new Placement(0, 1, 0, "anomaly_containment_unit", Blocks.OBSIDIAN));
            case BLACK_VAULT -> room(level, chunkBox, 6, 6, 5, Blocks.REINFORCED_DEEPSLATE.defaultBlockState(), mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES), false,
                    new Placement(-2, 1, 0, "tritanium_crate_red", Blocks.BARREL), new Placement(0, 1, 0, "tritanium_crate_blue", Blocks.BARREL), new Placement(2, 1, 0, "tritanium_crate_lime", Blocks.BARREL));
            case BLACK_SECURITY -> room(level, chunkBox, 6, 6, 5, Blocks.REINFORCED_DEEPSLATE.defaultBlockState(), mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES), false,
                    new Placement(-2, 1, 0, "android_spawner", Blocks.IRON_BLOCK), new Placement(2, 1, 0, "grid_capacitor", Blocks.IRON_BLOCK));
            case BLACK_ENTRANCE -> blackEntrance(level, chunkBox);
        }
    }

    private void room(WorldGenLevel level, BoundingBox clip, int hx, int hz, int height, BlockState wall, BlockState floor, boolean fourDoors, Placement... machines) {
        for (int x = -hx; x <= hx; x++) for (int z = -hz; z <= hz; z++) {
            set(level, clip, origin.offset(x, 0, z), floor);
            boolean edge = Math.abs(x) == hx || Math.abs(z) == hz;
            for (int y = 1; y <= height; y++) {
                boolean nsDoor = Math.abs(x) <= 1 && (z == -hz || z == hz) && y <= 3;
                boolean ewDoor = Math.abs(z) <= 1 && (x == -hx || x == hx) && y <= 3;
                boolean door = fourDoors ? nsDoor || ewDoor : nsDoor;
                set(level, clip, origin.offset(x, y, z), edge ? (door ? Blocks.AIR.defaultBlockState() : wall) : Blocks.AIR.defaultBlockState());
            }
            set(level, clip, origin.offset(x, height + 1, z), wall);
        }
        for (Placement p : machines) set(level, clip, origin.offset(p.x, p.y, p.z), mod(p.id, p.fallback));
    }

    private void towerRoom(WorldGenLevel level, BoundingBox clip, int hx, int hz, int height, Placement machine) {
        BlockState wall = mod("decorative.tritanium_plate", Blocks.IRON_BLOCK);
        room(level, clip, hx, hz, 5, wall, mod("decorative.floor_tiles", Blocks.SMOOTH_STONE), true, machine);
        for (int y = 6; y <= height + 5; y++) {
            set(level, clip, origin.offset(0, y, 0), wall);
            if ((y & 1) == 0) {
                set(level, clip, origin.offset(1, y, 0), Blocks.SEA_LANTERN.defaultBlockState());
                set(level, clip, origin.offset(-1, y, 0), Blocks.SEA_LANTERN.defaultBlockState());
                set(level, clip, origin.offset(0, y, 1), Blocks.SEA_LANTERN.defaultBlockState());
                set(level, clip, origin.offset(0, y, -1), Blocks.SEA_LANTERN.defaultBlockState());
            }
        }
    }

    private void fusionCore(WorldGenLevel level, BoundingBox clip) {
        BlockState hull = mod("decorative.tritanium_plate", Blocks.IRON_BLOCK);
        BlockState floor = mod("decorative.floor_tiles", Blocks.DEEPSLATE_TILES);
        int r = 8;
        for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) {
            double d = Math.sqrt(x * x + z * z);
            if (d <= r) set(level, clip, origin.offset(x, 0, z), floor);
            if (d >= r - 1 && d <= r + .35) for (int y = 1; y <= 7; y++) set(level, clip, origin.offset(x, y, z), hull);
            if (d < r - 1) for (int y = 1; y <= 7; y++) set(level, clip, origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
            if (d <= r) set(level, clip, origin.offset(x, 8, z), hull);
        }
        for (int y = 1; y <= 3; y++) {
            set(level, clip, origin.offset(0, y, -r), Blocks.AIR.defaultBlockState());
            set(level, clip, origin.offset(0, y, r), Blocks.AIR.defaultBlockState());
            set(level, clip, origin.offset(-r, y, 0), Blocks.AIR.defaultBlockState());
            set(level, clip, origin.offset(r, y, 0), Blocks.AIR.defaultBlockState());
        }
        set(level, clip, origin.offset(0, 1, 0), mod("anomaly_containment_unit", Blocks.OBSIDIAN));
        set(level, clip, origin.offset(-3, 1, 0), mod("gravitational_stabilizer", Blocks.OBSIDIAN));
        set(level, clip, origin.offset(3, 1, 0), mod("gravitational_stabilizer", Blocks.OBSIDIAN));
    }

    private void entrance(WorldGenLevel level, BoundingBox clip, BlockState wall) {
        BlockState floor = mod("decorative.floor_tiles", Blocks.SMOOTH_STONE);
        for (int z = -5; z <= 5; z++) for (int x = -3; x <= 3; x++) {
            set(level, clip, origin.offset(x, 0, z), floor);
            if (Math.abs(x) == 3) for (int y = 1; y <= 4; y++) set(level, clip, origin.offset(x, y, z), wall);
            set(level, clip, origin.offset(x, 5, z), wall);
        }
        for (int y = 1; y <= 3; y++) for (int x = -1; x <= 1; x++) {
            set(level, clip, origin.offset(x, y, -5), Blocks.AIR.defaultBlockState());
            set(level, clip, origin.offset(x, y, 5), Blocks.AIR.defaultBlockState());
        }
    }

    private void bunkerEntrance(WorldGenLevel level, BoundingBox clip) {
        BlockState wall = mod("decorative.tritanium_plate", Blocks.IRON_BLOCK);
        entrance(level, clip, wall);
        for (int y = 1; y <= 10; y++) {
            set(level, clip, origin.offset(-2, y, -5), wall);
            set(level, clip, origin.offset(2, y, -5), wall);
        }
    }

    private void blackEntrance(WorldGenLevel level, BoundingBox clip) {
        BlockState wall = Blocks.REINFORCED_DEEPSLATE.defaultBlockState();
        for (int y = -7; y <= 0; y++) for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            boolean edge = Math.abs(x) == 2 || Math.abs(z) == 2;
            set(level, clip, origin.offset(x, y, z), edge ? wall : Blocks.AIR.defaultBlockState());
        }
        set(level, clip, origin.offset(0, 0, 0), mod("holo_sign", Blocks.SEA_LANTERN));
    }

    private static BoundingBox boxFor(Room room, BlockPos p) {
        return switch (room) {
            case MANUFACTURING_CORE, REFINERY_CORE, BUNKER_COMMAND, BLACK_CORE -> box(p, 7, 7, 7);
            case QUANTUM_CORE -> new BoundingBox(p.getX() - 6, p.getY(), p.getZ() - 6, p.getX() + 6, p.getY() + 13, p.getZ() + 6);
            case FUSION_CORE -> box(p, 8, 8, 9);
            case PLANT_ENTRANCE, REFINERY_ENTRANCE, RELAY_ENTRANCE, FUSION_ENTRANCE -> new BoundingBox(p.getX() - 3, p.getY(), p.getZ() - 5, p.getX() + 3, p.getY() + 5, p.getZ() + 5);
            case BUNKER_ENTRANCE -> new BoundingBox(p.getX() - 3, p.getY(), p.getZ() - 5, p.getX() + 3, p.getY() + 10, p.getZ() + 5);
            case BLACK_ENTRANCE -> new BoundingBox(p.getX() - 2, p.getY() - 7, p.getZ() - 2, p.getX() + 2, p.getY(), p.getZ() + 2);
            default -> box(p, 7, 7, 7);
        };
    }

    private static BoundingBox box(BlockPos p, int hx, int hz, int h) {
        return new BoundingBox(p.getX() - hx, p.getY(), p.getZ() - hz, p.getX() + hx, p.getY() + h, p.getZ() + hz);
    }

    private static BlockState mod(String id, Block fallback) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("matteroverdrive", id));
        return block == null || block == Blocks.AIR ? fallback.defaultBlockState() : block.defaultBlockState();
    }

    private static void set(WorldGenLevel level, BoundingBox clip, BlockPos pos, BlockState state) {
        if (clip.isInside(pos) && !level.getBlockState(pos).is(Blocks.BEDROCK)) level.setBlock(pos, state, 2);
    }

    private record Placement(int x, int y, int z, String id, Block fallback) {}
}
