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

/**
 * Chunk-clipped modular rooms and exterior pieces for the modern Matter Overdrive facilities.
 * Pieces deliberately stay independent: Minecraft may call postProcess once per intersecting chunk.
 */
public final class TechnologyFacilityStructurePiece extends StructurePiece {
    public enum Room {
        MANUFACTURING_CORE, FABRICATION_WING, ASSEMBLY_WING, SHIPPING_WING, PLANT_ENTRANCE,
        REFINERY_CORE, EXCAVATION_WING, STORAGE_WING, PROCESSING_WING, REFINERY_ENTRANCE,
        QUANTUM_CORE, RELAY_WING, POWER_WING, CONTROL_WING, RELAY_ENTRANCE,
        BUNKER_COMMAND, DRONE_BAY, ANDROID_BAY, ARMORY, BUNKER_ENTRANCE,
        FUSION_CORE, STABILIZER_WING, REACTOR_CONTROL, SERVICE_WING, FUSION_ENTRANCE,
        BLACK_CORE, BLACK_LAB, BLACK_CONTAINMENT, BLACK_VAULT, BLACK_SECURITY, BLACK_ENTRANCE,
        CORRIDOR_X, CORRIDOR_Z, SERVICE_GANTRY_X, SERVICE_GANTRY_Z, ROOF_PLANT,
        RELAY_MAST, SECURITY_CHECKPOINT, OBSERVATION_BRIDGE, EXCAVATION_SHAFT, SALVAGE_YARD
    }

    /** Stable room contract shared by structure serialization, audits, and future locator tooling. */
    public record RoomMetadata(String role, boolean required, String connectorAxis) {}

    public static RoomMetadata metadata(Room room) {
        if (room == null) return new RoomMetadata("unknown", false, "none");
        String name = room.name();
        if (name.startsWith("CORRIDOR_")) return new RoomMetadata("connector", true, name.endsWith("X") ? "x" : "z");
        if (name.startsWith("SERVICE_GANTRY_")) return new RoomMetadata("service_connector", false, name.endsWith("X") ? "x" : "z");
        if (name.endsWith("_ENTRANCE")) return new RoomMetadata("entrance", true, "entry");
        if (room == Room.SECURITY_CHECKPOINT || room == Room.BLACK_SECURITY) return new RoomMetadata("security", true, "none");
        if (room == Room.SALVAGE_YARD) return new RoomMetadata("salvage", false, "none");
        if (room == Room.ROOF_PLANT || room == Room.RELAY_MAST || room == Room.OBSERVATION_BRIDGE)
            return new RoomMetadata("exterior", false, "vertical");
        return new RoomMetadata("room", true, "none");
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
        RoomMetadata metadata = metadata(room);
        tag.putString("MORole", metadata.role());
        tag.putBoolean("MORequired", metadata.required());
        tag.putString("MOConnectorAxis", metadata.connectorAxis());
        tag.putInt("MOX", origin.getX());
        tag.putInt("MOY", origin.getY());
        tag.putInt("MOZ", origin.getZ());
    }

    public static void assemble(StructurePiecesBuilder builder, TechnologyFacilityStructure.Kind kind, BlockPos c, int layout) {
        switch (kind) {
            case SYNTHETIC_MANUFACTURING_PLANT -> assemblePlant(builder, kind, c, layout);
            case MATTER_REFINERY -> assembleRefinery(builder, kind, c, layout);
            case QUANTUM_RELAY_STATION -> assembleRelay(builder, kind, c, layout);
            case ANDROID_COMMAND_BUNKER -> assembleBunker(builder, kind, c, layout);
            case FUSION_RESEARCH_COMPLEX -> assembleFusion(builder, kind, c, layout);
            case BLACK_SITE -> assembleBlackSite(builder, kind, c, layout);
        }
        if (layout != 0 && kind != TechnologyFacilityStructure.Kind.BLACK_SITE
                && kind != TechnologyFacilityStructure.Kind.ANDROID_COMMAND_BUNKER) {
            add(builder, kind, Room.SALVAGE_YARD, c.offset(34, 0, -18));
        }
    }

    private static void assemblePlant(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind k, BlockPos c, int v) {
        add(b, k, Room.MANUFACTURING_CORE, c);
        if (v == 0) {
            add(b, k, Room.FABRICATION_WING, c.offset(-18, 0, 0));
            add(b, k, Room.ASSEMBLY_WING, c.offset(18, 0, 0));
            add(b, k, Room.SHIPPING_WING, c.offset(0, 0, 19));
            add(b, k, Room.PLANT_ENTRANCE, c.offset(0, 0, -18));
            connectX(b, k, c.offset(-10, 0, 0)); connectX(b, k, c.offset(10, 0, 0));
            connectZ(b, k, c.offset(0, 0, 11)); connectZ(b, k, c.offset(0, 0, -10));
        } else if (v == 1) {
            add(b, k, Room.FABRICATION_WING, c.offset(-18, 0, 0));
            add(b, k, Room.ASSEMBLY_WING, c.offset(-18, 0, 18));
            add(b, k, Room.SHIPPING_WING, c.offset(0, 0, 18));
            add(b, k, Room.PLANT_ENTRANCE, c.offset(18, 0, 0));
            connectX(b, k, c.offset(-10, 0, 0)); connectZ(b, k, c.offset(-18, 0, 9));
            connectX(b, k, c.offset(-9, 0, 18)); connectX(b, k, c.offset(10, 0, 0));
        } else {
            add(b, k, Room.FABRICATION_WING, c.offset(-19, 0, 13));
            add(b, k, Room.ASSEMBLY_WING, c.offset(19, 0, 13));
            add(b, k, Room.SHIPPING_WING, c.offset(0, 0, 24));
            add(b, k, Room.PLANT_ENTRANCE, c.offset(0, 0, -18));
            connectX(b, k, c.offset(-10, 0, 9)); connectX(b, k, c.offset(10, 0, 9));
            connectZ(b, k, c.offset(0, 0, 16)); connectZ(b, k, c.offset(0, 0, -10));
        }
        add(b, k, Room.ROOF_PLANT, c.offset(0, 7, 0));
        add(b, k, Room.SERVICE_GANTRY_X, c.offset(0, 5, 10));
    }

    private static void assembleRefinery(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind k, BlockPos c, int v) {
        add(b, k, Room.REFINERY_CORE, c);
        int side = v == 1 ? 1 : -1;
        add(b, k, Room.EXCAVATION_WING, c.offset(19 * side, -2, 0));
        add(b, k, Room.STORAGE_WING, c.offset(-19 * side, 0, 0));
        add(b, k, Room.PROCESSING_WING, c.offset(0, 0, 19));
        add(b, k, Room.REFINERY_ENTRANCE, c.offset(0, 0, -18));
        connectX(b, k, c.offset(10 * side, 0, 0)); connectX(b, k, c.offset(-10 * side, 0, 0));
        connectZ(b, k, c.offset(0, 0, 10)); connectZ(b, k, c.offset(0, 0, -10));
        add(b, k, Room.EXCAVATION_SHAFT, c.offset(26 * side, -7, 0));
        add(b, k, Room.SERVICE_GANTRY_Z, c.offset(0, 5, 10));
        if (v == 2) add(b, k, Room.ROOF_PLANT, c.offset(0, 7, -3));
    }

    private static void assembleRelay(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind k, BlockPos c, int v) {
        add(b, k, Room.QUANTUM_CORE, c);
        int d = v == 1 ? -1 : 1;
        add(b, k, Room.RELAY_WING, c.offset(-16 * d, 0, 10));
        add(b, k, Room.POWER_WING, c.offset(16 * d, 0, 10));
        add(b, k, Room.CONTROL_WING, c.offset(0, 0, -16));
        add(b, k, Room.RELAY_ENTRANCE, c.offset(0, 0, -28));
        connectX(b, k, c.offset(-9 * d, 0, 6)); connectX(b, k, c.offset(9 * d, 0, 6));
        connectZ(b, k, c.offset(0, 0, -9)); connectZ(b, k, c.offset(0, 0, -22));
        add(b, k, Room.RELAY_MAST, c.offset(-18 * d, 5, 11));
        add(b, k, Room.RELAY_MAST, c.offset(18 * d, 5, 11));
        if (v == 2) add(b, k, Room.OBSERVATION_BRIDGE, c.offset(0, 7, 8));
    }

    private static void assembleBunker(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind k, BlockPos c, int v) {
        add(b, k, Room.BUNKER_COMMAND, c);
        add(b, k, Room.BUNKER_ENTRANCE, c.offset(0, 7, -24));
        add(b, k, Room.SECURITY_CHECKPOINT, c.offset(0, 0, -14));
        if (v == 0) {
            add(b, k, Room.DRONE_BAY, c.offset(-18, 0, 3));
            add(b, k, Room.ANDROID_BAY, c.offset(18, 0, 3));
            add(b, k, Room.ARMORY, c.offset(0, 0, 19));
        } else if (v == 1) {
            add(b, k, Room.DRONE_BAY, c.offset(-18, 0, 0));
            add(b, k, Room.ANDROID_BAY, c.offset(-18, 0, 18));
            add(b, k, Room.ARMORY, c.offset(18, 0, 0));
        } else {
            add(b, k, Room.DRONE_BAY, c.offset(18, 0, 0));
            add(b, k, Room.ANDROID_BAY, c.offset(18, 0, 18));
            add(b, k, Room.ARMORY, c.offset(-18, 0, 0));
        }
        connectZ(b, k, c.offset(0, 0, -8));
        connectX(b, k, c.offset(-10, 0, 2)); connectX(b, k, c.offset(10, 0, 2));
        connectZ(b, k, c.offset(0, 0, 11));
    }

    private static void assembleFusion(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind k, BlockPos c, int v) {
        add(b, k, Room.FUSION_CORE, c);
        int turn = v == 1 ? -1 : 1;
        add(b, k, Room.STABILIZER_WING, c.offset(-22 * turn, 0, 0));
        add(b, k, Room.REACTOR_CONTROL, c.offset(22 * turn, 0, 0));
        add(b, k, Room.SERVICE_WING, c.offset(0, -2, 22));
        add(b, k, Room.FUSION_ENTRANCE, c.offset(0, 0, -22));
        connectX(b, k, c.offset(-13 * turn, 0, 0)); connectX(b, k, c.offset(13 * turn, 0, 0));
        connectZ(b, k, c.offset(0, 0, 13)); connectZ(b, k, c.offset(0, 0, -13));
        add(b, k, Room.OBSERVATION_BRIDGE, c.offset(0, 6, v == 2 ? -4 : 4));
        add(b, k, Room.ROOF_PLANT, c.offset(0, 10, 0));
    }

    private static void assembleBlackSite(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind k, BlockPos c, int v) {
        add(b, k, Room.BLACK_CORE, c);
        add(b, k, Room.BLACK_SECURITY, c.offset(0, 0, -16));
        add(b, k, Room.BLACK_ENTRANCE, c.offset(0, 12, -27));
        add(b, k, Room.SECURITY_CHECKPOINT, c.offset(0, 0, -10));
        int d = v == 1 ? -1 : 1;
        add(b, k, Room.BLACK_LAB, c.offset(-18 * d, 0, 0));
        add(b, k, Room.BLACK_CONTAINMENT, c.offset(18 * d, 0, 0));
        add(b, k, Room.BLACK_VAULT, c.offset(0, -6, 19));
        connectX(b, k, c.offset(-10 * d, 0, 0)); connectX(b, k, c.offset(10 * d, 0, 0));
        connectZ(b, k, c.offset(0, 0, 10)); connectZ(b, k, c.offset(0, 0, -9));
        if (v == 2) add(b, k, Room.BLACK_LAB, c.offset(-18, -6, 19));
    }

    private static void connectX(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind k, BlockPos p) { add(b, k, Room.CORRIDOR_X, p); }
    private static void connectZ(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind k, BlockPos p) { add(b, k, Room.CORRIDOR_Z, p); }
    private static void add(StructurePiecesBuilder builder, TechnologyFacilityStructure.Kind kind, Room room, BlockPos pos) {
        builder.addPiece(new TechnologyFacilityStructurePiece(kind, room, pos));
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        switch (room) {
            case MANUFACTURING_CORE -> modernRoom(level, chunkBox, 7, 7, 5, paletteWall(), paletteFloor(), true, true,
                    p(0,1,0,"facility_network_controller",Blocks.IRON_BLOCK), p(0,1,-2,"matter_network_terminal",Blocks.IRON_BLOCK), p(-3,1,2,"inscriber",Blocks.BLAST_FURNACE), p(3,1,2,"replicator",Blocks.SMITHING_TABLE));
            case FABRICATION_WING -> modernRoom(level, chunkBox, 7, 6, 4, whiteWall(), paletteFloor(), false, true,
                    p(-3,1,0,"inscriber",Blocks.BLAST_FURNACE), p(3,1,0,"replicator",Blocks.SMITHING_TABLE), p(0,1,3,"holographic_status_panel",Blocks.SEA_LANTERN));
            case ASSEMBLY_WING -> modernRoom(level, chunkBox, 7, 6, 4, paletteWall(), paletteFloor(), false, true,
                    p(-3,1,0,"android_station",Blocks.SMITHING_TABLE), p(3,1,0,"charging_station",Blocks.LODESTONE));
            case SHIPPING_WING -> modernRoom(level, chunkBox, 6, 7, 4, paletteWall(), paletteFloor(), false, false,
                    p(-3,1,1,"tritanium_crate",Blocks.BARREL), p(0,1,1,"tritanium_crate_blue",Blocks.BARREL), p(3,1,1,"tritanium_crate_lime",Blocks.BARREL));
            case PLANT_ENTRANCE -> entrance(level, chunkBox, paletteWall(), true);

            case REFINERY_CORE -> modernRoom(level, chunkBox, 8, 8, 6, paletteWall(), greenFloor(), true, false,
                    p(0,1,0,"facility_network_controller",Blocks.IRON_BLOCK), p(-3,1,2,"matter_storage_matrix",Blocks.IRON_BLOCK), p(3,1,2,"holographic_status_panel",Blocks.SEA_LANTERN));
            case EXCAVATION_WING -> modernRoom(level, chunkBox, 7, 7, 5, paletteWall(), mod("decorative.floor_tiles_green",Blocks.DEEPSLATE_TILES), false, false,
                    p(0,1,0,"matter_excavator",Blocks.BLAST_FURNACE));
            case STORAGE_WING -> modernRoom(level, chunkBox, 7, 7, 5, paletteWall(), greenFloor(), false, false,
                    p(-3,1,0,"matter_storage_matrix",Blocks.IRON_BLOCK), p(3,1,0,"matter_storage_matrix",Blocks.IRON_BLOCK));
            case PROCESSING_WING -> modernRoom(level, chunkBox, 7, 7, 5, whiteWall(), greenFloor(), false, true,
                    p(-3,1,0,"decomposer",Blocks.BLAST_FURNACE), p(3,1,0,"matter_analyzer",Blocks.LECTERN));
            case REFINERY_ENTRANCE -> entrance(level, chunkBox, paletteWall(), false);

            case QUANTUM_CORE -> quantumTower(level, chunkBox);
            case RELAY_WING -> modernRoom(level, chunkBox, 6, 6, 5, paletteWall(), paletteFloor(), true, true,
                    p(0,1,0,"quantum_power_relay",Blocks.RESPAWN_ANCHOR));
            case POWER_WING -> modernRoom(level, chunkBox, 6, 6, 5, paletteWall(), paletteFloor(), false, true,
                    p(0,1,0,"grid_capacitor",Blocks.IRON_BLOCK));
            case CONTROL_WING -> modernRoom(level, chunkBox, 6, 6, 5, whiteWall(), paletteFloor(), true, true,
                    p(-2,1,0,"facility_network_controller",Blocks.IRON_BLOCK), p(2,1,0,"holographic_status_panel",Blocks.SEA_LANTERN));
            case RELAY_ENTRANCE -> entrance(level, chunkBox, paletteWall(), true);

            case BUNKER_COMMAND -> modernRoom(level, chunkBox, 8, 8, 5, paletteWall(), darkFloor(), true, false,
                    p(0,1,0,"facility_network_controller",Blocks.IRON_BLOCK), p(-3,1,2,"holographic_status_panel",Blocks.SEA_LANTERN));
            case DRONE_BAY -> modernRoom(level, chunkBox, 7, 7, 5, paletteWall(), darkFloor(), true, false,
                    p(-3,1,0,"android_spawner",Blocks.IRON_BLOCK), p(3,1,0,"charging_station",Blocks.LODESTONE));
            case ANDROID_BAY -> modernRoom(level, chunkBox, 7, 7, 5, paletteWall(), darkFloor(), true, true,
                    p(-3,1,0,"android_station",Blocks.SMITHING_TABLE), p(3,1,0,"android_induction_relay",Blocks.LODESTONE));
            case ARMORY -> modernRoom(level, chunkBox, 7, 7, 5, paletteWall(), darkFloor(), false, false,
                    p(-3,1,1,"tritanium_crate_red",Blocks.BARREL), p(3,1,1,"tritanium_crate",Blocks.BARREL));
            case BUNKER_ENTRANCE -> bunkerEntrance(level, chunkBox);

            case FUSION_CORE -> fusionCore(level, chunkBox);
            case STABILIZER_WING -> modernRoom(level, chunkBox, 8, 7, 5, paletteWall(), darkFloor(), true, true,
                    p(-3,1,0,"gravitational_stabilizer",Blocks.OBSIDIAN), p(3,1,0,"gravitational_stabilizer",Blocks.OBSIDIAN));
            case REACTOR_CONTROL -> modernRoom(level, chunkBox, 8, 7, 5, whiteWall(), paletteFloor(), true, true,
                    p(-3,1,0,"fusion_reactor_controller",Blocks.IRON_BLOCK), p(0,1,0,"facility_network_controller",Blocks.IRON_BLOCK), p(3,1,0,"holographic_status_panel",Blocks.SEA_LANTERN));
            case SERVICE_WING -> modernRoom(level, chunkBox, 7, 8, 5, paletteWall(), darkFloor(), false, false,
                    p(-3,1,0,"grid_capacitor",Blocks.IRON_BLOCK), p(3,1,0,"anomaly_containment_unit",Blocks.OBSIDIAN));
            case FUSION_ENTRANCE -> entrance(level, chunkBox, paletteWall(), true);

            case BLACK_CORE -> modernRoom(level, chunkBox, 8, 8, 5, blackWall(), darkFloor(), true, false,
                    p(0,1,0,"facility_network_controller",Blocks.IRON_BLOCK), p(0,1,3,"holographic_status_panel",Blocks.SEA_LANTERN));
            case BLACK_LAB -> modernRoom(level, chunkBox, 7, 7, 5, blackWall(), darkFloor(), true, false,
                    p(-3,1,0,"matter_analyzer",Blocks.LECTERN), p(3,1,0,"android_station",Blocks.SMITHING_TABLE));
            case BLACK_CONTAINMENT -> modernRoom(level, chunkBox, 7, 7, 6, blackWall(), darkFloor(), true, false,
                    p(0,1,0,"anomaly_containment_unit",Blocks.OBSIDIAN));
            case BLACK_VAULT -> modernRoom(level, chunkBox, 7, 7, 5, blackWall(), darkFloor(), false, false,
                    p(-3,1,1,"tritanium_crate_red",Blocks.BARREL), p(0,1,1,"tritanium_crate_blue",Blocks.BARREL), p(3,1,1,"tritanium_crate_lime",Blocks.BARREL));
            case BLACK_SECURITY -> modernRoom(level, chunkBox, 7, 7, 5, blackWall(), darkFloor(), true, false,
                    p(-3,1,0,"android_spawner",Blocks.IRON_BLOCK), p(3,1,0,"grid_capacitor",Blocks.IRON_BLOCK));
            case BLACK_ENTRANCE -> blackEntrance(level, chunkBox);

            case CORRIDOR_X -> corridor(level, chunkBox, true);
            case CORRIDOR_Z -> corridor(level, chunkBox, false);
            case SERVICE_GANTRY_X -> gantry(level, chunkBox, true);
            case SERVICE_GANTRY_Z -> gantry(level, chunkBox, false);
            case ROOF_PLANT -> roofPlant(level, chunkBox);
            case RELAY_MAST -> relayMast(level, chunkBox);
            case SECURITY_CHECKPOINT -> securityCheckpoint(level, chunkBox);
            case OBSERVATION_BRIDGE -> observationBridge(level, chunkBox);
            case EXCAVATION_SHAFT -> excavationShaft(level, chunkBox);
            case SALVAGE_YARD -> salvageYard(level, chunkBox);
        }
        // Room-local cache/defence markers run after the room shell, never outside this chunk.
        switch (room) {
            case PROCESSING_WING, CONTROL_WING, REACTOR_CONTROL, BLACK_LAB ->
                    set(level, chunkBox, origin.offset(3,1,3), mod("tritanium_crate_blue", Blocks.BARREL));
            default -> { }
        }
        switch (room) {
            case ASSEMBLY_WING, EXCAVATION_WING, POWER_WING, STABILIZER_WING, BLACK_CONTAINMENT ->
                    set(level, chunkBox, origin.offset(-3,1,3), mod("android_spawner", Blocks.IRON_BLOCK));
            default -> { }
        }
    }

    private void modernRoom(WorldGenLevel level, BoundingBox clip, int hx, int hz, int h, BlockState wall, BlockState floor,
                            boolean fourDoors, boolean windows, Placement... machines) {
        BlockState frame = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState glass = mod("industrial_glass", Blocks.TINTED_GLASS);
        BlockState lamp = mod("decorative.tritanium_lamp", Blocks.SEA_LANTERN);
        BlockState trim = mod("decorative.tritanium_plate_stripe", Blocks.YELLOW_CONCRETE);
        boolean damaged = damageVariant();

        for (int x = -hx; x <= hx; x++) for (int z = -hz; z <= hz; z++) {
            set(level, clip, origin.offset(x, 0, z), floor);
            boolean edge = Math.abs(x) == hx || Math.abs(z) == hz;
            boolean corner = Math.abs(x) >= hx - 1 && Math.abs(z) >= hz - 1;
            for (int y = 1; y <= h; y++) {
                boolean nsDoor = Math.abs(x) <= 1 && Math.abs(z) == hz && y <= 3;
                boolean ewDoor = Math.abs(z) <= 1 && Math.abs(x) == hx && y <= 3;
                boolean door = fourDoors ? nsDoor || ewDoor : nsDoor;
                if (!edge) {
                    set(level, clip, origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
                } else if (door) {
                    set(level, clip, origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
                } else if (corner || y == 1 || y == h) {
                    set(level, clip, origin.offset(x, y, z), frame);
                } else if (windows && y >= 2 && y <= h - 1 && ((Math.abs(x) + Math.abs(z)) % 3 != 0)) {
                    set(level, clip, origin.offset(x, y, z), glass);
                } else {
                    set(level, clip, origin.offset(x, y, z), wall);
                }
            }
            BlockState roof = ((Math.abs(x) + Math.abs(z)) % 6 == 0) ? frame : wall;
            set(level, clip, origin.offset(x, h + 1, z), roof);
        }
        for (int x = -hx + 2; x <= hx - 2; x += 4) {
            set(level, clip, origin.offset(x, h, 0), lamp);
        }
        for (int x = -hx + 1; x <= hx - 1; x++) {
            if ((x & 1) == 0) set(level, clip, origin.offset(x, 0, -hz + 1), trim);
        }
        if (damaged) {
            for (int y = h - 1; y <= h + 1; y++) set(level, clip, origin.offset(hx, y, hz - 2), Blocks.AIR.defaultBlockState());
            set(level, clip, origin.offset(hx - 1, h + 1, hz - 2), Blocks.AIR.defaultBlockState());
        }
        for (Placement p : machines) {
            // Ruined support machinery becomes inert salvage; caches and defence markers remain usable.
            boolean wreck = damaged && p.x > 0 && !p.id.startsWith("tritanium_crate") && !p.id.equals("android_spawner");
            set(level, clip, origin.offset(p.x, p.y, p.z), wreck ? mod("decorative.vent.dark", Blocks.IRON_BLOCK) : mod(p.id, p.fallback));
        }
        if (damaged) {
            BlockState debris = switch (facility) {
                case MATTER_REFINERY -> Blocks.TUFF.defaultBlockState();
                case FUSION_RESEARCH_COMPLEX, BLACK_SITE -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
                case QUANTUM_RELAY_STATION -> Blocks.OXIDIZED_COPPER.defaultBlockState();
                default -> Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
            };
            for (int x = hx-3; x <= hx; x++) for (int z = hz-3; z <= hz; z++) {
                if ((x+z)%3 != 0) set(level,clip,origin.offset(x,h+1,z),Blocks.AIR.defaultBlockState());
                if ((x+z)%2 == 0) set(level,clip,origin.offset(x,1,z),debris);
            }
            for (int y=2;y<h;y++) set(level,clip,origin.offset(hx,y,hz-2),Blocks.AIR.defaultBlockState());
            set(level,clip,origin.offset(-hx+1,h-1,hz-1),Blocks.COBWEB.defaultBlockState());
            set(level,clip,origin.offset(hx-2,h,0),mod("decorative.vent.dark",Blocks.IRON_BLOCK));
        }
    }

    private void corridor(WorldGenLevel level, BoundingBox clip, boolean xAxis) {
        BlockState wall = facility == TechnologyFacilityStructure.Kind.BLACK_SITE ? blackWall() : paletteWall();
        BlockState floor = facility == TechnologyFacilityStructure.Kind.MATTER_REFINERY ? greenFloor() : darkFloor();
        BlockState frame = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState lamp = mod("decorative.tritanium_lamp", Blocks.SEA_LANTERN);
        for (int a = -5; a <= 5; a++) for (int b = -2; b <= 2; b++) {
            int x = xAxis ? a : b, z = xAxis ? b : a;
            set(level, clip, origin.offset(x, 0, z), floor);
            for (int y = 1; y <= 4; y++) {
                boolean side = Math.abs(b) == 2;
                set(level, clip, origin.offset(x, y, z), side ? ((a % 4 == 0) ? frame : wall) : Blocks.AIR.defaultBlockState());
            }
            set(level, clip, origin.offset(x, 5, z), (a % 4 == 0) ? frame : wall);
        }
        for (int a = -4; a <= 4; a += 4) {
            int x = xAxis ? a : 0, z = xAxis ? 0 : a;
            set(level, clip, origin.offset(x, 4, z), lamp);
        }
    }

    private void entrance(WorldGenLevel level, BoundingBox clip, BlockState wall, boolean glassFront) {
        BlockState frame = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState glass = mod("industrial_glass", Blocks.TINTED_GLASS);
        BlockState floor = paletteFloor();
        for (int z = -6; z <= 6; z++) for (int x = -4; x <= 4; x++) {
            set(level, clip, origin.offset(x, 0, z), floor);
            for (int y = 1; y <= 4; y++) {
                boolean side = Math.abs(x) == 4;
                if (side) set(level, clip, origin.offset(x, y, z), (z % 4 == 0) ? frame : wall);
                else set(level, clip, origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
            }
            set(level, clip, origin.offset(x, 5, z), (Math.abs(x) == 4 || (x & 1) == 0) ? frame : wall);
        }
        if (glassFront) for (int x = -3; x <= 3; x++) for (int y = 2; y <= 4; y++) {
            if (Math.abs(x) > 1) set(level, clip, origin.offset(x, y, -6), glass);
        }
        set(level, clip, origin.offset(0, 4, -5), mod("holo_sign", Blocks.SEA_LANTERN));
    }

    private void quantumTower(WorldGenLevel level, BoundingBox clip) {
        modernRoom(level, clip, 7, 7, 6, paletteWall(), paletteFloor(), true, true,
                p(0,1,0,"quantum_power_relay",Blocks.RESPAWN_ANCHOR));
        BlockState frame = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState glow = mod("decorative.holo_matrix", Blocks.SEA_LANTERN);
        for (int y = 7; y <= 17; y++) {
            int r = y < 13 ? 3 : 2;
            for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) {
                boolean edge = Math.abs(x) == r || Math.abs(z) == r;
                if (edge) set(level, clip, origin.offset(x,y,z), ((x + z + y) & 3) == 0 ? glow : frame);
            }
        }
        set(level, clip, origin.offset(0,18,0), glow);
        set(level, clip, origin.offset(0,19,0), glow);
    }

    private void fusionCore(WorldGenLevel level, BoundingBox clip) {
        BlockState hull = paletteWall(), floor = darkFloor();
        BlockState frame = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState glass = mod("industrial_glass", Blocks.TINTED_GLASS);
        BlockState lamp = mod("decorative.tritanium_lamp", Blocks.SEA_LANTERN);
        int r = 10;
        for (int x=-r;x<=r;x++) for (int z=-r;z<=r;z++) {
            double d = Math.sqrt(x*x + z*z);
            if (d <= r) set(level,clip,origin.offset(x,0,z),floor);
            if (d < r-1) for (int y=1;y<=8;y++) set(level,clip,origin.offset(x,y,z),Blocks.AIR.defaultBlockState());
            if (d >= r-1 && d <= r+.35) for (int y=1;y<=8;y++) {
                BlockState s = (y==1 || y==8 || ((x+z)&3)==0) ? frame : (y>=3 && y<=6 ? glass : hull);
                set(level,clip,origin.offset(x,y,z),s);
            }
            if (d <= r) set(level,clip,origin.offset(x,9,z), ((x+z)&5)==0 ? frame : hull);
        }
        for (int y=1;y<=4;y++) for (int w=-1;w<=1;w++) {
            set(level,clip,origin.offset(w,y,-r),Blocks.AIR.defaultBlockState());
            set(level,clip,origin.offset(w,y,r),Blocks.AIR.defaultBlockState());
            set(level,clip,origin.offset(-r,y,w),Blocks.AIR.defaultBlockState());
            set(level,clip,origin.offset(r,y,w),Blocks.AIR.defaultBlockState());
        }
        for (int a=0;a<360;a+=45) {
            double rad=Math.toRadians(a);
            int x=(int)Math.round(Math.cos(rad)*6), z=(int)Math.round(Math.sin(rad)*6);
            set(level,clip,origin.offset(x,1,z),frame);
            set(level,clip,origin.offset(x,2,z),lamp);
        }
        set(level,clip,origin.offset(0,1,0),mod("anomaly_containment_unit",Blocks.OBSIDIAN));
        set(level,clip,origin.offset(-4,1,0),mod("gravitational_stabilizer",Blocks.OBSIDIAN));
        set(level,clip,origin.offset(4,1,0),mod("gravitational_stabilizer",Blocks.OBSIDIAN));
    }

    private void gantry(WorldGenLevel level, BoundingBox clip, boolean xAxis) {
        BlockState frame=mod("decorative.beams",Blocks.IRON_BARS), floor=mod("decorative.floor_tile_white",Blocks.IRON_BLOCK);
        for(int a=-8;a<=8;a++) for(int b=-1;b<=1;b++) {
            int x=xAxis?a:b,z=xAxis?b:a;
            set(level,clip,origin.offset(x,0,z),floor);
            if(Math.abs(b)==1) set(level,clip,origin.offset(x,1,z),Blocks.IRON_BARS.defaultBlockState());
            if(a%4==0) set(level,clip,origin.offset(x,-1,z),frame);
        }
    }

    private void roofPlant(WorldGenLevel level, BoundingBox clip) {
        BlockState frame=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE), vent=mod("decorative.vent.dark",Blocks.IRON_BLOCK), coil=mod("decorative.coils",Blocks.COPPER_BLOCK);
        for(int x=-4;x<=4;x++) for(int z=-3;z<=3;z++) if(Math.abs(x)==4||Math.abs(z)==3) set(level,clip,origin.offset(x,0,z),frame);
        for(int x=-2;x<=2;x+=2){ set(level,clip,origin.offset(x,1,0),vent); set(level,clip,origin.offset(x,2,0),coil); }
        for(int y=1;y<=4;y++) set(level,clip,origin.offset(0,y,3),frame);
        set(level,clip,origin.offset(0,5,3),mod("holo_sign",Blocks.REDSTONE_LAMP));
    }

    private void relayMast(WorldGenLevel level, BoundingBox clip) {
        BlockState frame=mod("decorative.beams",Blocks.IRON_BARS), glow=mod("decorative.holo_matrix",Blocks.SEA_LANTERN);
        for(int y=0;y<=13;y++) {
            set(level,clip,origin.offset(0,y,0),frame);
            if(y==5||y==9||y==13) for(int d=-2;d<=2;d++) { set(level,clip,origin.offset(d,y,0),frame); set(level,clip,origin.offset(0,y,d),frame); }
        }
        set(level,clip,origin.offset(0,14,0),glow);
    }

    private void securityCheckpoint(WorldGenLevel level, BoundingBox clip) {
        BlockState wall=facility==TechnologyFacilityStructure.Kind.BLACK_SITE?blackWall():paletteWall(), frame=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE);
        for(int z=-4;z<=4;z++) for(int x=-4;x<=4;x++) {
            set(level,clip,origin.offset(x,0,z),darkFloor());
            if(Math.abs(x)==4) for(int y=1;y<=4;y++) set(level,clip,origin.offset(x,y,z),(z%3==0)?frame:wall);
            set(level,clip,origin.offset(x,5,z),wall);
        }
        for(int y=1;y<=3;y++) { set(level,clip,origin.offset(-1,y,0),Blocks.IRON_BARS.defaultBlockState()); set(level,clip,origin.offset(1,y,0),Blocks.IRON_BARS.defaultBlockState()); }
        set(level,clip,origin.offset(-3,1,0),mod("android_spawner",Blocks.IRON_BLOCK));
        set(level,clip,origin.offset(3,2,0),mod("holographic_status_panel",Blocks.SEA_LANTERN));
    }

    private void observationBridge(WorldGenLevel level, BoundingBox clip) {
        BlockState frame=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE), glass=mod("industrial_glass",Blocks.TINTED_GLASS);
        for(int x=-7;x<=7;x++) for(int z=-2;z<=2;z++) {
            set(level,clip,origin.offset(x,0,z),darkFloor());
            for(int y=1;y<=3;y++) if(Math.abs(z)==2) set(level,clip,origin.offset(x,y,z), y==1?frame:glass);
            set(level,clip,origin.offset(x,4,z),(x%4==0)?frame:glass);
        }
        set(level,clip,origin.offset(0,1,0),mod("holographic_status_panel",Blocks.SEA_LANTERN));
    }

    private void excavationShaft(WorldGenLevel level, BoundingBox clip) {
        BlockState frame=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE), lamp=mod("decorative.tritanium_lamp",Blocks.SEA_LANTERN);
        for(int y=0;y<=12;y++) for(int x=-3;x<=3;x++) for(int z=-3;z<=3;z++) {
            boolean edge=Math.abs(x)==3||Math.abs(z)==3;
            set(level,clip,origin.offset(x,y,z),edge?((y%4==0)?frame:Blocks.DEEPSLATE_TILES.defaultBlockState()):Blocks.AIR.defaultBlockState());
        }
        for(int y=1;y<=11;y+=3) set(level,clip,origin.offset(2,y,2),lamp);
        set(level,clip,origin.offset(0,0,0),mod("matter_excavator",Blocks.BLAST_FURNACE));
    }

    private void bunkerEntrance(WorldGenLevel level, BoundingBox clip) {
        entrance(level,clip,paletteWall(),false);
        BlockState frame=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE);
        for(int y=-7;y<=0;y++) for(int x=-2;x<=2;x++) for(int z=3;z<=6;z++) {
            boolean edge=Math.abs(x)==2||z==6;
            set(level,clip,origin.offset(x,y,z),edge?frame:Blocks.AIR.defaultBlockState());
        }
    }

    private void blackEntrance(WorldGenLevel level, BoundingBox clip) {
        BlockState wall=blackWall(), frame=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE);
        for(int y=-12;y<=0;y++) for(int x=-3;x<=3;x++) for(int z=-3;z<=3;z++) {
            boolean edge=Math.abs(x)==3||Math.abs(z)==3;
            set(level,clip,origin.offset(x,y,z),edge?((y%4==0)?frame:wall):Blocks.AIR.defaultBlockState());
        }
        for(int y=-10;y<=-1;y+=3) set(level,clip,origin.offset(2,y,2),mod("decorative.tritanium_lamp",Blocks.REDSTONE_LAMP));
        set(level,clip,origin.offset(0,0,0),mod("holo_sign",Blocks.SEA_LANTERN));
    }

    private void salvageYard(WorldGenLevel level, BoundingBox clip) {
        BlockState beam = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState wreck = switch (facility) {
            case QUANTUM_RELAY_STATION -> mod("decorative.coils", Blocks.OXIDIZED_COPPER);
            case MATTER_REFINERY -> Blocks.TUFF.defaultBlockState();
            case FUSION_RESEARCH_COMPLEX -> Blocks.OBSIDIAN.defaultBlockState();
            default -> mod("decorative.vent.dark", Blocks.IRON_BLOCK);
        };
        for (int x=-5;x<=5;x++) for (int z=-4;z<=4;z++) {
            set(level,clip,origin.offset(x,0,z),darkFloor());
            for(int y=1;y<=4;y++) set(level,clip,origin.offset(x,y,z),Blocks.AIR.defaultBlockState());
            if (Math.abs(x)==5 && z%3==0) for(int y=1;y<=3;y++) set(level,clip,origin.offset(x,y,z),beam);
            if (x > 1 && z > 0 && (x+z)%2==0) set(level,clip,origin.offset(x,1,z),wreck);
        }
        // Broken service frame and a clear two-block recovery aisle.
        for(int x=-5;x<=1;x++) set(level,clip,origin.offset(x,4,3),beam);
        set(level,clip,origin.offset(-3,1,1),mod("tritanium_crate",Blocks.BARREL));
    }

    private boolean damageVariant() {
        long h=origin.asLong() ^ ((long)facility.ordinal()*0x9E3779B97F4A7C15L) ^ ((long)room.ordinal()*0xC2B2AE3D27D4EB4FL);
        return Math.floorMod(h, 4L)==0L;
    }

    private BlockState paletteWall(){ return mod("decorative.tritanium_plate",Blocks.IRON_BLOCK); }
    private BlockState whiteWall(){ return mod("decorative.white_plate",Blocks.QUARTZ_BLOCK); }
    private BlockState blackWall(){ return mod("decorative.carbon_fiber_plate",Blocks.REINFORCED_DEEPSLATE); }
    private BlockState paletteFloor(){ return mod("decorative.floor_tiles",Blocks.SMOOTH_STONE); }
    private BlockState greenFloor(){ return mod("decorative.floor_tiles_green",Blocks.OXIDIZED_COPPER); }
    private BlockState darkFloor(){ return mod("decorative.floor_tiles",Blocks.DEEPSLATE_TILES); }

    private static Placement p(int x,int y,int z,String id,Block fallback){ return new Placement(x,y,z,id,fallback); }

    private static BoundingBox boxFor(Room room, BlockPos p) {
        return switch(room) {
            case SALVAGE_YARD -> box(p,5,4,4);
            case MANUFACTURING_CORE -> box(p,8,8,8);
            case REFINERY_CORE,BUNKER_COMMAND,BLACK_CORE -> box(p,9,9,8);
            case QUANTUM_CORE -> new BoundingBox(p.getX()-7,p.getY(),p.getZ()-7,p.getX()+7,p.getY()+19,p.getZ()+7);
            case FUSION_CORE -> box(p,11,11,11);
            case PLANT_ENTRANCE,REFINERY_ENTRANCE,RELAY_ENTRANCE,FUSION_ENTRANCE -> new BoundingBox(p.getX()-4,p.getY(),p.getZ()-6,p.getX()+4,p.getY()+5,p.getZ()+6);
            case BUNKER_ENTRANCE -> new BoundingBox(p.getX()-4,p.getY()-7,p.getZ()-6,p.getX()+4,p.getY()+5,p.getZ()+6);
            case BLACK_ENTRANCE -> new BoundingBox(p.getX()-3,p.getY()-12,p.getZ()-3,p.getX()+3,p.getY(),p.getZ()+3);
            case CORRIDOR_X -> new BoundingBox(p.getX()-5,p.getY(),p.getZ()-2,p.getX()+5,p.getY()+5,p.getZ()+2);
            case CORRIDOR_Z -> new BoundingBox(p.getX()-2,p.getY(),p.getZ()-5,p.getX()+2,p.getY()+5,p.getZ()+5);
            case SERVICE_GANTRY_X -> new BoundingBox(p.getX()-8,p.getY()-1,p.getZ()-1,p.getX()+8,p.getY()+2,p.getZ()+1);
            case SERVICE_GANTRY_Z -> new BoundingBox(p.getX()-1,p.getY()-1,p.getZ()-8,p.getX()+1,p.getY()+2,p.getZ()+8);
            case ROOF_PLANT -> new BoundingBox(p.getX()-4,p.getY(),p.getZ()-3,p.getX()+4,p.getY()+5,p.getZ()+3);
            case RELAY_MAST -> new BoundingBox(p.getX()-2,p.getY(),p.getZ()-2,p.getX()+2,p.getY()+14,p.getZ()+2);
            case SECURITY_CHECKPOINT -> new BoundingBox(p.getX()-4,p.getY(),p.getZ()-4,p.getX()+4,p.getY()+5,p.getZ()+4);
            case OBSERVATION_BRIDGE -> new BoundingBox(p.getX()-7,p.getY(),p.getZ()-2,p.getX()+7,p.getY()+4,p.getZ()+2);
            case EXCAVATION_SHAFT -> new BoundingBox(p.getX()-3,p.getY(),p.getZ()-3,p.getX()+3,p.getY()+12,p.getZ()+3);
            default -> box(p,8,8,8);
        };
    }

    private static BoundingBox box(BlockPos p,int hx,int hz,int h){ return new BoundingBox(p.getX()-hx,p.getY(),p.getZ()-hz,p.getX()+hx,p.getY()+h,p.getZ()+hz); }

    private static BlockState mod(String id,Block fallback){
        Block block=ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("matteroverdrive",id));
        return block==null||block==Blocks.AIR?fallback.defaultBlockState():block.defaultBlockState();
    }

    private void set(WorldGenLevel level,BoundingBox clip,BlockPos pos,BlockState state){
        if (!clip.isInside(pos) || !getBoundingBox().isInside(pos)) return;
        if (level.getBlockState(pos).is(Blocks.BEDROCK)) return;
        level.setBlock(pos,state,2);
        // All reads, metadata assignment and writes use the same clipped position.
        if (state.getBlock() instanceof matteroverdrive.block.TritaniumCrateBlock
                && level.getBlockEntity(pos) instanceof matteroverdrive.blockentity.TritaniumCrateBlockEntity crate) {
            String profile = room == Room.SALVAGE_YARD ? "salvage" : facility.name().toLowerCase(java.util.Locale.ROOT);
            crate.seedStructureLoot(ResourceLocation.fromNamespaceAndPath("matteroverdrive", "chests/facilities/" + profile),
                    level.getSeed() ^ pos.asLong() ^ ((long) room.ordinal() * 73428767L));
        }
        if (state.getBlock() instanceof matteroverdrive.block.AndroidSpawnerBlock
                && level.getBlockEntity(pos) instanceof matteroverdrive.blockentity.AndroidSpawnerBlockEntity spawner) {
            int reserve = switch (facility) {
                case BLACK_SITE -> 4;
                case ANDROID_COMMAND_BUNKER -> 3;
                default -> 2;
            };
            int ranged = switch (facility) {
                case MATTER_REFINERY, SYNTHETIC_MANUFACTURING_PLANT -> 25;
                case BLACK_SITE, QUANTUM_RELAY_STATION -> 85;
                default -> 60;
            };
            spawner.configureFacility(facility.name().toLowerCase(java.util.Locale.ROOT),
                    damageVariant() ? Math.max(1,reserve-1) : reserve, ranged);
        }
    }

    private record Placement(int x,int y,int z,String id,Block fallback){}
}

