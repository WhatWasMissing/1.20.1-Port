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
 * Playability-first modular technology facilities.
 *
 * The layout contract is entrance -> security/operations -> specialised wings ->
 * core/high-value spaces. Rooms use chamfered shells, service alcoves, raised work
 * platforms and distinct silhouettes instead of single rectangular boxes. Vertical
 * transitions are explicit pieces so every required level has a walkable connection.
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

    public record RoomMetadata(String role, boolean required, String connectorAxis) {}

    public static RoomMetadata metadata(Room room) {
        if (room == null) return new RoomMetadata("unknown", false, "none");
        String n = room.name();
        if (n.startsWith("CORRIDOR_")) return new RoomMetadata("connector", true, n.endsWith("X") ? "x" : "z");
        if (n.startsWith("SERVICE_GANTRY_")) return new RoomMetadata("service_connector", false, n.endsWith("X") ? "x" : "z");
        if (n.endsWith("_ENTRANCE")) return new RoomMetadata("entrance", true, "entry");
        if (room == Room.SECURITY_CHECKPOINT || room == Room.BLACK_SECURITY) return new RoomMetadata("security", true, "none");
        if (room == Room.EXCAVATION_SHAFT) return new RoomMetadata("vertical_connector", true, "y");
        if (room == Room.SALVAGE_YARD) return new RoomMetadata("salvage", false, "none");
        if (room == Room.ROOF_PLANT || room == Room.RELAY_MAST || room == Room.OBSERVATION_BRIDGE)
            return new RoomMetadata("landmark", false, "vertical");
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
        RoomMetadata m = metadata(room);
        tag.putString("MORole", m.role());
        tag.putBoolean("MORequired", m.required());
        tag.putString("MOConnectorAxis", m.connectorAxis());
        tag.putInt("MOX", origin.getX());
        tag.putInt("MOY", origin.getY());
        tag.putInt("MOZ", origin.getZ());
    }

    public static void assemble(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind kind, BlockPos c, int layout) {
        switch (kind) {
            case SYNTHETIC_MANUFACTURING_PLANT -> assemblePlant(b, kind, c, layout);
            case MATTER_REFINERY -> assembleRefinery(b, kind, c, layout);
            case QUANTUM_RELAY_STATION -> assembleRelay(b, kind, c, layout);
            case ANDROID_COMMAND_BUNKER -> assembleBunker(b, kind, c, layout);
            case FUSION_RESEARCH_COMPLEX -> assembleFusion(b, kind, c, layout);
            case BLACK_SITE -> assembleBlackSite(b, kind, c, layout);
        }
    }

    private static void assemblePlant(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind k, BlockPos c, int v) {
        // Front gate -> manufacturing hall -> fabrication/assembly -> shipping/loading.
        add(b,k,Room.PLANT_ENTRANCE,c.offset(0,0,-30));
        add(b,k,Room.SECURITY_CHECKPOINT,c.offset(0,0,-20));
        connectZ(b,k,c.offset(0,0,-13));
        add(b,k,Room.MANUFACTURING_CORE,c);
        int side = v == 1 ? -1 : 1;
        add(b,k,Room.FABRICATION_WING,c.offset(-20*side,0,2));
        add(b,k,Room.ASSEMBLY_WING,c.offset(20*side,0,2));
        connectX(b,k,c.offset(-11*side,0,1));
        connectX(b,k,c.offset(11*side,0,1));
        add(b,k,Room.SHIPPING_WING,c.offset(0,0,23));
        connectZ(b,k,c.offset(0,0,13));
        add(b,k,Room.ROOF_PLANT,c.offset(0,9,0));
        add(b,k,Room.SERVICE_GANTRY_X,c.offset(0,6,8));
        if (v != 0) add(b,k,Room.SALVAGE_YARD,c.offset(28*side,0,22));
    }

    private static void assembleRefinery(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind k, BlockPos c, int v) {
        // Intake/excavation -> core processing -> clean processing -> storage/output.
        add(b,k,Room.REFINERY_ENTRANCE,c.offset(0,0,-28));
        add(b,k,Room.SECURITY_CHECKPOINT,c.offset(0,0,-18));
        connectZ(b,k,c.offset(0,0,-11));
        add(b,k,Room.REFINERY_CORE,c);
        int side = v == 1 ? 1 : -1;
        add(b,k,Room.EXCAVATION_WING,c.offset(20*side,-2,0));
        connectX(b,k,c.offset(11*side,0,0));
        add(b,k,Room.EXCAVATION_SHAFT,c.offset(28*side,-10,0));
        add(b,k,Room.PROCESSING_WING,c.offset(0,0,21));
        connectZ(b,k,c.offset(0,0,12));
        add(b,k,Room.STORAGE_WING,c.offset(-20*side,0,12));
        connectX(b,k,c.offset(-11*side,0,12));
        add(b,k,Room.SERVICE_GANTRY_Z,c.offset(0,6,10));
        if (v == 2) add(b,k,Room.SALVAGE_YARD,c.offset(-30*side,0,-4));
    }

    private static void assembleRelay(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind k, BlockPos c, int v) {
        // Public/control wing protects a central tower; power and diagnostics flank it.
        add(b,k,Room.RELAY_ENTRANCE,c.offset(0,0,-31));
        add(b,k,Room.CONTROL_WING,c.offset(0,0,-19));
        connectZ(b,k,c.offset(0,0,-11));
        add(b,k,Room.QUANTUM_CORE,c);
        int side = v == 1 ? -1 : 1;
        add(b,k,Room.RELAY_WING,c.offset(-19*side,0,5));
        add(b,k,Room.POWER_WING,c.offset(19*side,0,5));
        connectX(b,k,c.offset(-10*side,0,3));
        connectX(b,k,c.offset(10*side,0,3));
        add(b,k,Room.RELAY_MAST,c.offset(-22*side,5,16));
        add(b,k,Room.RELAY_MAST,c.offset(22*side,5,16));
        add(b,k,Room.OBSERVATION_BRIDGE,c.offset(0,8,10));
        if (v == 2) add(b,k,Room.SALVAGE_YARD,c.offset(31,0,-2));
    }

    private static void assembleBunker(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind k, BlockPos c, int v) {
        // Surface vestibule includes its own stepped descent into security.
        add(b,k,Room.BUNKER_ENTRANCE,c.offset(0,8,-28));
        add(b,k,Room.SECURITY_CHECKPOINT,c.offset(0,0,-17));
        connectZ(b,k,c.offset(0,0,-9));
        add(b,k,Room.BUNKER_COMMAND,c);
        int side = v == 1 ? -1 : 1;
        add(b,k,Room.DRONE_BAY,c.offset(-20*side,0,3));
        add(b,k,Room.ANDROID_BAY,c.offset(20*side,0,3));
        connectX(b,k,c.offset(-11*side,0,2));
        connectX(b,k,c.offset(11*side,0,2));
        add(b,k,Room.ARMORY,c.offset(0,0,21));
        connectZ(b,k,c.offset(0,0,12));
        if (v == 2) add(b,k,Room.SERVICE_GANTRY_X,c.offset(0,6,9));
    }

    private static void assembleFusion(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind k, BlockPos c, int v) {
        // Reception/control leads to the circular chamber; dangerous support wings are side-accessed.
        add(b,k,Room.FUSION_ENTRANCE,c.offset(0,0,-31));
        add(b,k,Room.REACTOR_CONTROL,c.offset(0,0,-19));
        connectZ(b,k,c.offset(0,0,-11));
        add(b,k,Room.FUSION_CORE,c);
        int side = v == 1 ? -1 : 1;
        add(b,k,Room.STABILIZER_WING,c.offset(-23*side,0,2));
        add(b,k,Room.SERVICE_WING,c.offset(23*side,0,2));
        connectX(b,k,c.offset(-13*side,0,1));
        connectX(b,k,c.offset(13*side,0,1));
        add(b,k,Room.OBSERVATION_BRIDGE,c.offset(0,8,10));
        add(b,k,Room.ROOF_PLANT,c.offset(0,11,0));
        if (v == 2) add(b,k,Room.SALVAGE_YARD,c.offset(31*side,0,19));
    }

    private static void assembleBlackSite(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind k, BlockPos c, int v) {
        // Concealed surface shaft -> security -> command -> labs/containment -> descended vault.
        add(b,k,Room.BLACK_ENTRANCE,c.offset(0,12,-31));
        add(b,k,Room.BLACK_SECURITY,c.offset(0,0,-19));
        add(b,k,Room.SECURITY_CHECKPOINT,c.offset(0,0,-11));
        connectZ(b,k,c.offset(0,0,-6));
        add(b,k,Room.BLACK_CORE,c);
        int side = v == 1 ? -1 : 1;
        add(b,k,Room.BLACK_LAB,c.offset(-20*side,0,2));
        add(b,k,Room.BLACK_CONTAINMENT,c.offset(20*side,0,2));
        connectX(b,k,c.offset(-11*side,0,1));
        connectX(b,k,c.offset(11*side,0,1));
        // Guaranteed vertical route to the deep vault; this fixes the previous disconnected lower level.
        add(b,k,Room.EXCAVATION_SHAFT,c.offset(0,-8,14));
        add(b,k,Room.CORRIDOR_Z,c.offset(0,-8,22));
        add(b,k,Room.BLACK_VAULT,c.offset(0,-8,31));
        if (v == 2) add(b,k,Room.BLACK_LAB,c.offset(-20,-8,31));
    }

    private static void connectX(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind k, BlockPos p) { add(b,k,Room.CORRIDOR_X,p); }
    private static void connectZ(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind k, BlockPos p) { add(b,k,Room.CORRIDOR_Z,p); }
    private static void add(StructurePiecesBuilder b, TechnologyFacilityStructure.Kind k, Room r, BlockPos p) { b.addPiece(new TechnologyFacilityStructurePiece(k,r,p)); }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox clip, ChunkPos chunkPos, BlockPos pivot) {
        switch (room) {
            case MANUFACTURING_CORE -> manufacturingHall(level,clip);
            case FABRICATION_WING -> industrialRoom(level,clip,9,8,6,whiteWall(),darkFloor(),true,
                    p(-4,1,2,"inscriber",Blocks.ANVIL),p(0,1,2,"drone_fabricator",Blocks.SMITHING_TABLE),p(4,1,2,"replicator",Blocks.SMITHING_TABLE));
            case ASSEMBLY_WING -> industrialRoom(level,clip,9,8,6,paletteWall(),darkFloor(),true,
                    p(-4,1,2,"android_station",Blocks.SMITHING_TABLE),p(0,1,2,"charging_station",Blocks.LODESTONE),p(4,1,2,"android_induction_relay",Blocks.LODESTONE));
            case SHIPPING_WING -> loadingHall(level,clip);
            case PLANT_ENTRANCE -> entrance(level,clip,paletteWall(),false);

            case REFINERY_CORE -> refineryHall(level,clip);
            case EXCAVATION_WING -> industrialRoom(level,clip,9,8,6,greenWall(),greenFloor(),true,p(0,1,2,"matter_excavator",Blocks.BLAST_FURNACE));
            case STORAGE_WING -> storageHall(level,clip,true);
            case PROCESSING_WING -> industrialRoom(level,clip,9,8,6,whiteWall(),greenFloor(),true,
                    p(-4,1,2,"decomposer",Blocks.BLAST_FURNACE),p(0,1,2,"matter_analyzer",Blocks.LECTERN),p(4,1,2,"matter_storage_matrix",Blocks.IRON_BLOCK));
            case REFINERY_ENTRANCE -> entrance(level,clip,greenWall(),false);

            case QUANTUM_CORE -> quantumTower(level,clip);
            case RELAY_WING -> industrialRoom(level,clip,8,8,6,paletteWall(),paletteFloor(),true,p(0,1,2,"quantum_power_relay",Blocks.RESPAWN_ANCHOR));
            case POWER_WING -> industrialRoom(level,clip,8,8,6,darkWall(),darkFloor(),true,p(-3,1,2,"grid_capacitor",Blocks.IRON_BLOCK),p(3,1,2,"network_router",Blocks.IRON_BLOCK));
            case CONTROL_WING -> operationsRoom(level,clip,false);
            case RELAY_ENTRANCE -> entrance(level,clip,paletteWall(),true);

            case BUNKER_COMMAND -> operationsRoom(level,clip,true);
            case DRONE_BAY -> hangar(level,clip,true);
            case ANDROID_BAY -> industrialRoom(level,clip,9,8,6,darkWall(),darkFloor(),true,
                    p(-4,1,2,"android_station",Blocks.SMITHING_TABLE),p(0,1,2,"charging_station",Blocks.LODESTONE),p(4,1,2,"android_induction_relay",Blocks.LODESTONE));
            case ARMORY -> armory(level,clip);
            case BUNKER_ENTRANCE -> bunkerEntrance(level,clip);

            case FUSION_CORE -> fusionCore(level,clip);
            case STABILIZER_WING -> industrialRoom(level,clip,9,8,6,darkWall(),darkFloor(),true,
                    p(-4,1,2,"gravitational_stabilizer",Blocks.OBSIDIAN),p(4,1,2,"gravitational_stabilizer",Blocks.OBSIDIAN));
            case REACTOR_CONTROL -> reactorControl(level,clip);
            case SERVICE_WING -> industrialRoom(level,clip,9,8,6,paletteWall(),darkFloor(),true,
                    p(-4,1,2,"grid_capacitor",Blocks.IRON_BLOCK),p(0,1,2,"anomaly_containment_unit",Blocks.OBSIDIAN),p(4,1,2,"network_switch",Blocks.IRON_BLOCK));
            case FUSION_ENTRANCE -> entrance(level,clip,paletteWall(),true);

            case BLACK_CORE -> blackCommand(level,clip);
            case BLACK_LAB -> industrialRoom(level,clip,9,8,6,blackWall(),darkFloor(),true,
                    p(-4,1,2,"matter_analyzer",Blocks.LECTERN),p(0,1,2,"android_station",Blocks.SMITHING_TABLE),p(4,1,2,"pattern_storage",Blocks.CHISELED_BOOKSHELF));
            case BLACK_CONTAINMENT -> containmentRoom(level,clip);
            case BLACK_VAULT -> vault(level,clip);
            case BLACK_SECURITY -> securityCheckpoint(level,clip);
            case BLACK_ENTRANCE -> blackEntrance(level,clip);

            case CORRIDOR_X -> corridor(level,clip,true);
            case CORRIDOR_Z -> corridor(level,clip,false);
            case SERVICE_GANTRY_X -> gantry(level,clip,true);
            case SERVICE_GANTRY_Z -> gantry(level,clip,false);
            case ROOF_PLANT -> roofPlant(level,clip);
            case RELAY_MAST -> relayMast(level,clip);
            case SECURITY_CHECKPOINT -> securityCheckpoint(level,clip);
            case OBSERVATION_BRIDGE -> observationBridge(level,clip);
            case EXCAVATION_SHAFT -> steppedShaft(level,clip);
            case SALVAGE_YARD -> salvageYard(level,clip);
        }
    }

    private void industrialRoom(WorldGenLevel level, BoundingBox clip, int hx, int hz, int h, BlockState wall, BlockState floor,
                                boolean throughRoute, Placement... machines) {
        BlockState beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE), glass=mod("industrial_glass",Blocks.TINTED_GLASS), lamp=mod("decorative.tritanium_lamp",Blocks.SEA_LANTERN);
        boolean damaged=damageVariant();
        for(int x=-hx;x<=hx;x++) for(int z=-hz;z<=hz;z++) {
            boolean cut=Math.abs(x)>=hx-1&&Math.abs(z)>=hz-1;
            if(cut) continue;
            set(level,clip,origin.offset(x,0,z),floor);
            boolean edge=Math.abs(x)==hx||Math.abs(z)==hz||Math.abs(x)==hx-1&&Math.abs(z)>=hz-2||Math.abs(z)==hz-1&&Math.abs(x)>=hx-2;
            for(int y=1;y<=h;y++) {
                boolean nsDoor=Math.abs(x)<=1&&Math.abs(z)>=hz-1&&y<=3;
                boolean ewDoor=throughRoute&&Math.abs(z)<=1&&Math.abs(x)>=hx-1&&y<=3;
                if(!edge||nsDoor||ewDoor) set(level,clip,origin.offset(x,y,z),Blocks.AIR.defaultBlockState());
                else if(y==1||y==h||((x+z)&5)==0) set(level,clip,origin.offset(x,y,z),beam);
                else if(y==2||y==3) set(level,clip,origin.offset(x,y,z),glass);
                else set(level,clip,origin.offset(x,y,z),wall);
            }
            set(level,clip,origin.offset(x,h+1,z),((x+z)&7)==0?beam:wall);
        }
        // Recessed service trenches and raised side work platforms leave the centre aisle clear.
        for(int z=-hz+3;z<=hz-3;z++) {
            set(level,clip,origin.offset(-hx+3,1,z),darkFloor());
            set(level,clip,origin.offset(hx-3,1,z),darkFloor());
        }
        for(int z=-hz+3;z<=hz-3;z+=4) set(level,clip,origin.offset(0,h,z),lamp);
        for(Placement p:machines) set(level,clip,origin.offset(p.x,p.y,p.z),damaged&&p.x>0&&!p.id.startsWith("tritanium_crate")?mod("decorative.vent.dark",Blocks.IRON_BLOCK):mod(p.id,p.fallback));
        if(damaged) damageCorner(level,clip,hx,hz,h);
        if(occupiedRoom()) set(level,clip,origin.offset(-hx+3,1,hz-3),mod("android_spawner",Blocks.IRON_BLOCK));
    }

    private void manufacturingHall(WorldGenLevel level, BoundingBox clip) {
        industrialRoom(level,clip,11,10,8,paletteWall(),darkFloor(),true,p(0,1,-5,"facility_network_controller",Blocks.IRON_BLOCK));
        BlockState stripe=mod("decorative.tritanium_plate_stripe",Blocks.YELLOW_CONCRETE),beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE);
        for(int z=-6;z<=7;z++) {
            for(int x=-2;x<=2;x++) set(level,clip,origin.offset(x,1,z),x==0?stripe:darkFloor());
            if(z%4==0){set(level,clip,origin.offset(-5,1,z),beam);set(level,clip,origin.offset(5,1,z),beam);}
        }
        set(level,clip,origin.offset(-6,1,4),mod("drone_fabricator",Blocks.SMITHING_TABLE));
        set(level,clip,origin.offset(6,1,4),mod("android_station",Blocks.SMITHING_TABLE));
    }

    private void loadingHall(WorldGenLevel level, BoundingBox clip) {
        industrialRoom(level,clip,10,9,7,paletteWall(),darkFloor(),true);
        BlockState stripe=mod("decorative.tritanium_plate_stripe",Blocks.YELLOW_CONCRETE),rack=mod("decorative.beams",Blocks.IRON_BARS);
        for(int z=-6;z<=6;z++) for(int x=-2;x<=2;x++) set(level,clip,origin.offset(x,1,z),x==0?stripe:darkFloor());
        for(int x:new int[]{-7,7}) for(int z=-5;z<=5;z+=3){set(level,clip,origin.offset(x,1,z),rack);set(level,clip,origin.offset(x,2,z),mod("tritanium_crate",Blocks.BARREL));}
    }

    private void refineryHall(WorldGenLevel level, BoundingBox clip) {
        industrialRoom(level,clip,11,10,8,greenWall(),greenFloor(),true,p(0,1,-5,"facility_network_controller",Blocks.IRON_BLOCK));
        for(int x=-6;x<=6;x+=4){set(level,clip,origin.offset(x,1,3),mod("matter_storage_matrix",Blocks.IRON_BLOCK));set(level,clip,origin.offset(x,1,6),mod("decorative.coils",Blocks.COPPER_BLOCK));}
        for(int z=-4;z<=7;z++) set(level,clip,origin.offset(0,1,z),mod("decorative.tritanium_plate_stripe",Blocks.YELLOW_CONCRETE));
    }

    private void storageHall(WorldGenLevel level, BoundingBox clip, boolean matter) {
        industrialRoom(level,clip,9,8,6,paletteWall(),darkFloor(),true);
        for(int x=-6;x<=6;x+=4) for(int z=-4;z<=4;z+=4) {
            set(level,clip,origin.offset(x,1,z),matter?mod("matter_storage_matrix",Blocks.IRON_BLOCK):mod("tritanium_crate",Blocks.BARREL));
        }
    }

    private void operationsRoom(WorldGenLevel level, BoundingBox clip, boolean fortified) {
        industrialRoom(level,clip,10,9,7,fortified?darkWall():whiteWall(),darkFloor(),true,
                p(0,1,-4,"facility_network_controller",Blocks.IRON_BLOCK),p(-4,1,2,"holographic_status_panel",Blocks.SEA_LANTERN),p(4,1,2,"network_switch",Blocks.IRON_BLOCK));
        // Tiered command dais is reachable by two broad steps.
        for(int z=3;z<=6;z++) for(int x=-5;x<=5;x++) set(level,clip,origin.offset(x,1,z),darkFloor());
        for(int z=5;z<=6;z++) for(int x=-4;x<=4;x++) set(level,clip,origin.offset(x,2,z),darkFloor());
    }

    private void hangar(WorldGenLevel level, BoundingBox clip, boolean drones) {
        industrialRoom(level,clip,12,10,8,paletteWall(),darkFloor(),true);
        BlockState stripe=mod("decorative.tritanium_plate_stripe",Blocks.YELLOW_CONCRETE);
        for(int x=-8;x<=8;x++) for(int z=-6;z<=6;z++) if(Math.abs(x)%6==0||Math.abs(z)==6) set(level,clip,origin.offset(x,1,z),stripe);
        set(level,clip,origin.offset(-7,1,0),mod("charging_station",Blocks.LODESTONE));
        set(level,clip,origin.offset(7,1,0),mod("charging_station",Blocks.LODESTONE));
        if(drones) set(level,clip,origin.offset(0,1,5),mod("android_spawner",Blocks.IRON_BLOCK));
    }

    private void armory(WorldGenLevel level, BoundingBox clip) {
        industrialRoom(level,clip,9,8,6,darkWall(),darkFloor(),false);
        for(int x=-6;x<=6;x+=3){set(level,clip,origin.offset(x,1,3),mod("tritanium_crate_red",Blocks.BARREL));set(level,clip,origin.offset(x,2,3),Blocks.IRON_BARS.defaultBlockState());}
        set(level,clip,origin.offset(0,1,-3),mod("holographic_status_panel",Blocks.SEA_LANTERN));
    }

    private void reactorControl(WorldGenLevel level, BoundingBox clip) {
        industrialRoom(level,clip,10,8,7,whiteWall(),darkFloor(),true,
                p(-4,1,2,"fusion_reactor_controller",Blocks.IRON_BLOCK),p(0,1,2,"facility_network_controller",Blocks.IRON_BLOCK),p(4,1,2,"holographic_status_panel",Blocks.SEA_LANTERN));
        for(int x=-7;x<=7;x++) set(level,clip,origin.offset(x,1,5),mod("industrial_glass",Blocks.TINTED_GLASS));
    }

    private void blackCommand(WorldGenLevel level, BoundingBox clip) {
        operationsRoom(level,clip,true);
        for(int x=-6;x<=6;x+=3) set(level,clip,origin.offset(x,3,6),mod("decorative.tritanium_lamp",Blocks.REDSTONE_LAMP));
    }

    private void containmentRoom(WorldGenLevel level, BoundingBox clip) {
        industrialRoom(level,clip,10,9,8,blackWall(),darkFloor(),true,p(0,1,0,"anomaly_containment_unit",Blocks.CRYING_OBSIDIAN));
        BlockState glass=mod("industrial_glass",Blocks.TINTED_GLASS),beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE);
        for(int x=-5;x<=5;x++) for(int z=-5;z<=5;z++) if(Math.abs(x)==5||Math.abs(z)==5) for(int y=1;y<=5;y++) set(level,clip,origin.offset(x,y,z),y==1||y==5?beam:glass);
        for(int y=1;y<=3;y++) for(int x=-1;x<=1;x++) set(level,clip,origin.offset(x,y,-5),Blocks.AIR.defaultBlockState());
    }

    private void vault(WorldGenLevel level, BoundingBox clip) {
        industrialRoom(level,clip,9,8,6,blackWall(),darkFloor(),false);
        for(int x=-6;x<=6;x+=3){set(level,clip,origin.offset(x,1,3),mod(x==0?"tritanium_crate_red":"tritanium_crate_blue",Blocks.BARREL));}
        set(level,clip,origin.offset(0,1,-3),mod("matter_storage_matrix",Blocks.IRON_BLOCK));
    }

    private void quantumTower(WorldGenLevel level, BoundingBox clip) {
        // Octagonal base and tall open-frame transmission crown.
        industrialRoom(level,clip,9,9,7,paletteWall(),paletteFloor(),true,p(0,1,0,"quantum_power_relay",Blocks.RESPAWN_ANCHOR));
        BlockState beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE),glow=mod("decorative.holo_matrix",Blocks.SEA_LANTERN);
        for(int y=8;y<=22;y++) {
            int r=y<15?4:3;
            for(int x=-r;x<=r;x++) for(int z=-r;z<=r;z++) if(Math.abs(x)==r||Math.abs(z)==r) set(level,clip,origin.offset(x,y,z),((x+z+y)&3)==0?glow:beam);
        }
        for(int d=-7;d<=7;d++){set(level,clip,origin.offset(d,14,0),beam);set(level,clip,origin.offset(0,14,d),beam);}
        set(level,clip,origin.offset(0,23,0),glow);
    }

    private void fusionCore(WorldGenLevel level, BoundingBox clip) {
        BlockState hull=paletteWall(),floor=darkFloor(),beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE),glass=mod("industrial_glass",Blocks.TINTED_GLASS),lamp=mod("decorative.tritanium_lamp",Blocks.SEA_LANTERN);
        int r=12;
        for(int x=-r;x<=r;x++) for(int z=-r;z<=r;z++) {
            double d=Math.sqrt(x*x+z*z);
            if(d<=r-1){set(level,clip,origin.offset(x,0,z),floor);for(int y=1;y<=10;y++)set(level,clip,origin.offset(x,y,z),Blocks.AIR.defaultBlockState());}
            if(d>r-1.4&&d<=r+.2)for(int y=1;y<=10;y++)set(level,clip,origin.offset(x,y,z),y==1||y==10||((x+z)&4)==0?beam:y>=3&&y<=7?glass:hull);
            if(d<=r)set(level,clip,origin.offset(x,11,z),((x+z)&7)==0?beam:hull);
        }
        // Cardinal 3-wide doors guarantee connection to all attached wings.
        for(int y=1;y<=4;y++)for(int w=-1;w<=1;w++){set(level,clip,origin.offset(w,y,-r),Blocks.AIR.defaultBlockState());set(level,clip,origin.offset(w,y,r),Blocks.AIR.defaultBlockState());set(level,clip,origin.offset(-r,y,w),Blocks.AIR.defaultBlockState());set(level,clip,origin.offset(r,y,w),Blocks.AIR.defaultBlockState());}
        // Ring catwalk with rails around the containment centre.
        for(int a=0;a<360;a+=15){double rad=Math.toRadians(a);int x=(int)Math.round(Math.cos(rad)*7),z=(int)Math.round(Math.sin(rad)*7);set(level,clip,origin.offset(x,2,z),beam);if(a%45==0)set(level,clip,origin.offset(x,3,z),lamp);}
        set(level,clip,origin.offset(0,1,0),mod("anomaly_containment_unit",Blocks.OBSIDIAN));
        set(level,clip,origin.offset(-5,1,0),mod("gravitational_stabilizer",Blocks.OBSIDIAN));
        set(level,clip,origin.offset(5,1,0),mod("gravitational_stabilizer",Blocks.OBSIDIAN));
    }

    private void corridor(WorldGenLevel level, BoundingBox clip, boolean xAxis) {
        BlockState wall=facility==TechnologyFacilityStructure.Kind.BLACK_SITE?blackWall():paletteWall(),floor=facility==TechnologyFacilityStructure.Kind.MATTER_REFINERY?greenFloor():darkFloor(),beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE),lamp=mod("decorative.tritanium_lamp",Blocks.SEA_LANTERN);
        for(int a=-7;a<=7;a++)for(int b=-2;b<=2;b++){int x=xAxis?a:b,z=xAxis?b:a;set(level,clip,origin.offset(x,0,z),floor);for(int y=1;y<=4;y++)set(level,clip,origin.offset(x,y,z),Math.abs(b)==2?(a%4==0?beam:wall):Blocks.AIR.defaultBlockState());set(level,clip,origin.offset(x,5,z),a%4==0?beam:wall);}
        for(int a=-4;a<=4;a+=4){int x=xAxis?a:0,z=xAxis?0:a;set(level,clip,origin.offset(x,4,z),lamp);}
    }

    private void entrance(WorldGenLevel level, BoundingBox clip, BlockState wall, boolean glassFront) {
        // Angled vestibule + canopy, not a plain rectangular mouth.
        industrialRoom(level,clip,6,8,5,wall,paletteFloor(),false,p(0,1,3,"holo_sign",Blocks.SEA_LANTERN));
        BlockState beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE),glass=mod("industrial_glass",Blocks.TINTED_GLASS);
        for(int z=-12;z<=-8;z++)for(int x=-4;x<=4;x++){if(Math.abs(x)<=4-(Math.abs(z+10)/2)){set(level,clip,origin.offset(x,5,z),beam);if(Math.abs(x)>=3)for(int y=1;y<=4;y++)set(level,clip,origin.offset(x,y,z),glass);}}
        for(int y=1;y<=3;y++)for(int x=-1;x<=1;x++)set(level,clip,origin.offset(x,y,-8),Blocks.AIR.defaultBlockState());
        if(glassFront) for(int x=-4;x<=4;x+=4) set(level,clip,origin.offset(x,3,-9),mod("decorative.tritanium_lamp",Blocks.SEA_LANTERN));
    }

    private void bunkerEntrance(WorldGenLevel level, BoundingBox clip) {
        entrance(level,clip,paletteWall(),false);
        // Two-flight stairwell descends eight blocks while keeping 3-wide clearance.
        for(int i=0;i<=8;i++){int y=-i;int z=1+i*2;for(int x=-1;x<=1;x++){set(level,clip,origin.offset(x,y,z),darkFloor());for(int h=1;h<=3;h++)set(level,clip,origin.offset(x,y+h,z),Blocks.AIR.defaultBlockState());}}
    }

    private void blackEntrance(WorldGenLevel level, BoundingBox clip) {
        BlockState wall=blackWall(),beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE),floor=darkFloor();
        // Concealed descending switchback rather than an empty vertical shaft.
        for(int i=0;i<=12;i++){int y=-i;int z=i;int x=i<6?-2:2;for(int w=-1;w<=1;w++){set(level,clip,origin.offset(x+w,y,z),floor);for(int h=1;h<=3;h++)set(level,clip,origin.offset(x+w,y+h,z),Blocks.AIR.defaultBlockState());}set(level,clip,origin.offset(x-2,y+2,z),beam);set(level,clip,origin.offset(x+2,y+2,z),beam);}
        for(int x=-4;x<=4;x++)for(int z=-4;z<=2;z++)if(Math.abs(x)==4||z==-4)for(int y=0;y<=5;y++)set(level,clip,origin.offset(x,y,z),y==0||y==5?beam:wall);
        set(level,clip,origin.offset(0,3,-3),mod("holo_sign",Blocks.REDSTONE_LAMP));
    }

    private void steppedShaft(WorldGenLevel level, BoundingBox clip) {
        BlockState wall=facility==TechnologyFacilityStructure.Kind.BLACK_SITE?blackWall():paletteWall(),beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE),floor=darkFloor();
        // A real walkable helical/switchback descent replaces ladder-only vertical traps.
        for(int y=0;y<=15;y++)for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){boolean edge=Math.abs(x)==4||Math.abs(z)==4;set(level,clip,origin.offset(x,y,z),edge?(y%4==0?beam:wall):Blocks.AIR.defaultBlockState());}
        for(int i=0;i<=8;i++){int y=i;int x=i<4?-2:2;int z=-3+i;for(int w=-1;w<=1;w++){set(level,clip,origin.offset(x+w,y,z),floor);set(level,clip,origin.offset(x+w,y+1,z),Blocks.AIR.defaultBlockState());set(level,clip,origin.offset(x+w,y+2,z),Blocks.AIR.defaultBlockState());}}
    }

    private void securityCheckpoint(WorldGenLevel level, BoundingBox clip) {
        industrialRoom(level,clip,6,6,5,facility==TechnologyFacilityStructure.Kind.BLACK_SITE?blackWall():darkWall(),darkFloor(),true,
                p(-3,1,2,"android_spawner",Blocks.IRON_BLOCK),p(3,1,2,"holographic_status_panel",Blocks.SEA_LANTERN));
        // Security lane leaves the centre passable; side barriers make the purpose clear.
        for(int z=-3;z<=3;z++)for(int y=1;y<=3;y++){set(level,clip,origin.offset(-2,y,z),Blocks.IRON_BARS.defaultBlockState());set(level,clip,origin.offset(2,y,z),Blocks.IRON_BARS.defaultBlockState());}
        for(int y=1;y<=3;y++){set(level,clip,origin.offset(-2,y,0),Blocks.AIR.defaultBlockState());set(level,clip,origin.offset(2,y,0),Blocks.AIR.defaultBlockState());}
    }

    private void observationBridge(WorldGenLevel level, BoundingBox clip) {
        BlockState floor=darkFloor(),beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE),glass=mod("industrial_glass",Blocks.TINTED_GLASS);
        for(int x=-9;x<=9;x++)for(int z=-2;z<=2;z++){set(level,clip,origin.offset(x,0,z),floor);if(Math.abs(z)==2)for(int y=1;y<=3;y++)set(level,clip,origin.offset(x,y,z),y==1?beam:glass);set(level,clip,origin.offset(x,4,z),x%4==0?beam:glass);}
        set(level,clip,origin.offset(0,1,0),mod("holographic_status_panel",Blocks.SEA_LANTERN));
    }

    private void gantry(WorldGenLevel level, BoundingBox clip, boolean xAxis) {
        BlockState floor=mod("decorative.floor_tile_white",Blocks.IRON_BLOCK),beam=mod("decorative.beams",Blocks.IRON_BARS);
        for(int a=-9;a<=9;a++)for(int b=-1;b<=1;b++){int x=xAxis?a:b,z=xAxis?b:a;set(level,clip,origin.offset(x,0,z),floor);if(Math.abs(b)==1)set(level,clip,origin.offset(x,1,z),Blocks.IRON_BARS.defaultBlockState());if(a%4==0)set(level,clip,origin.offset(x,-1,z),beam);}
    }

    private void roofPlant(WorldGenLevel level, BoundingBox clip) {
        BlockState beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE),vent=mod("decorative.vent.dark",Blocks.IRON_BLOCK),coil=mod("decorative.coils",Blocks.COPPER_BLOCK);
        for(int x=-5;x<=5;x++)for(int z=-4;z<=4;z++)if(Math.abs(x)==5||Math.abs(z)==4)set(level,clip,origin.offset(x,0,z),beam);
        for(int x=-3;x<=3;x+=3){set(level,clip,origin.offset(x,1,0),vent);set(level,clip,origin.offset(x,2,0),coil);}
        for(int y=1;y<=5;y++)set(level,clip,origin.offset(0,y,4),beam);
    }

    private void relayMast(WorldGenLevel level, BoundingBox clip) {
        BlockState beam=mod("decorative.beams",Blocks.IRON_BARS),glow=mod("decorative.holo_matrix",Blocks.SEA_LANTERN);
        for(int y=0;y<=17;y++){set(level,clip,origin.offset(0,y,0),beam);if(y==5||y==10||y==15)for(int d=-3;d<=3;d++){set(level,clip,origin.offset(d,y,0),beam);set(level,clip,origin.offset(0,y,d),beam);}}
        set(level,clip,origin.offset(0,18,0),glow);
    }

    private void salvageYard(WorldGenLevel level, BoundingBox clip) {
        BlockState beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE),wreck=mod("decorative.vent.dark",Blocks.IRON_BLOCK);
        for(int x=-7;x<=7;x++)for(int z=-6;z<=6;z++)if(Math.abs(x)+Math.abs(z)<=12)set(level,clip,origin.offset(x,0,z),Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState());
        // Keep a clean 3-wide recovery aisle through the middle.
        for(int z=-5;z<=5;z++)for(int x=-1;x<=1;x++)set(level,clip,origin.offset(x,1,z),Blocks.AIR.defaultBlockState());
        for(int x=-6;x<=6;x+=3){set(level,clip,origin.offset(x,1,4),wreck);set(level,clip,origin.offset(x,2,4),beam);}
        set(level,clip,origin.offset(-5,1,-3),mod("tritanium_crate",Blocks.BARREL));
    }

    private void damageCorner(WorldGenLevel level, BoundingBox clip, int hx, int hz, int h) {
        // Damage is deliberately off-route: never erase the centre/cardi nal doors.
        for(int y=h-2;y<=h+1;y++)for(int z=hz-4;z<=hz-2;z++)set(level,clip,origin.offset(hx-1,y,z),Blocks.AIR.defaultBlockState());
        for(int x=hx-4;x<=hx-2;x++)for(int z=hz-4;z<=hz-2;z++)if(((x+z)&1)==0)set(level,clip,origin.offset(x,1,z),Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState());
        set(level,clip,origin.offset(-hx+3,h-1,hz-3),Blocks.COBWEB.defaultBlockState());
    }

    private boolean damageVariant(){long h=origin.asLong()^((long)facility.ordinal()*0x9E3779B97F4A7C15L)^((long)room.ordinal()*0xC2B2AE3D27D4EB4FL);return Math.floorMod(h,5L)==0L;}
    private boolean occupiedRoom(){return Math.floorMod(origin.getX()*17+origin.getZ()*31+room.ordinal()*7+facility.ordinal()*13,5)!=0;}

    private BlockState paletteWall(){return mod("decorative.tritanium_plate",Blocks.IRON_BLOCK);}    private BlockState whiteWall(){return mod("decorative.white_plate",Blocks.QUARTZ_BLOCK);}    private BlockState blackWall(){return mod("decorative.carbon_fiber_plate",Blocks.POLISHED_BLACKSTONE);}    private BlockState greenWall(){return mod("decorative.tritanium_plate_green",Blocks.OXIDIZED_COPPER);}    private BlockState darkWall(){return mod("decorative.vent.dark",Blocks.DEEPSLATE_BRICKS);}    private BlockState paletteFloor(){return mod("decorative.floor_tiles",Blocks.SMOOTH_STONE);}    private BlockState greenFloor(){return mod("decorative.floor_tiles_green",Blocks.OXIDIZED_COPPER);}    private BlockState darkFloor(){return mod("decorative.floor_tiles_dark",Blocks.DEEPSLATE_TILES);}

    private static Placement p(int x,int y,int z,String id,Block fallback){return new Placement(x,y,z,id,fallback);}

    private static BoundingBox boxFor(Room room, BlockPos p) {
        return switch(room) {
            case MANUFACTURING_CORE,REFINERY_CORE,BUNKER_COMMAND,BLACK_CORE -> box(p,12,11,10);
            case QUANTUM_CORE -> new BoundingBox(p.getX()-10,p.getY(),p.getZ()-10,p.getX()+10,p.getY()+24,p.getZ()+10);
            case FUSION_CORE -> box(p,13,13,13);
            case DRONE_BAY -> box(p,13,11,10);
            case PLANT_ENTRANCE,REFINERY_ENTRANCE,RELAY_ENTRANCE,FUSION_ENTRANCE -> new BoundingBox(p.getX()-7,p.getY(),p.getZ()-13,p.getX()+7,p.getY()+7,p.getZ()+9);
            case BUNKER_ENTRANCE -> new BoundingBox(p.getX()-7,p.getY()-9,p.getZ()-13,p.getX()+7,p.getY()+7,p.getZ()+19);
            case BLACK_ENTRANCE -> new BoundingBox(p.getX()-6,p.getY()-13,p.getZ()-5,p.getX()+6,p.getY()+6,p.getZ()+14);
            case CORRIDOR_X -> new BoundingBox(p.getX()-7,p.getY(),p.getZ()-2,p.getX()+7,p.getY()+5,p.getZ()+2);
            case CORRIDOR_Z -> new BoundingBox(p.getX()-2,p.getY(),p.getZ()-7,p.getX()+2,p.getY()+5,p.getZ()+7);
            case SERVICE_GANTRY_X -> new BoundingBox(p.getX()-9,p.getY()-1,p.getZ()-2,p.getX()+9,p.getY()+2,p.getZ()+2);
            case SERVICE_GANTRY_Z -> new BoundingBox(p.getX()-2,p.getY()-1,p.getZ()-9,p.getX()+2,p.getY()+2,p.getZ()+9);
            case ROOF_PLANT -> new BoundingBox(p.getX()-6,p.getY(),p.getZ()-5,p.getX()+6,p.getY()+6,p.getZ()+5);
            case RELAY_MAST -> new BoundingBox(p.getX()-4,p.getY(),p.getZ()-4,p.getX()+4,p.getY()+19,p.getZ()+4);
            case SECURITY_CHECKPOINT -> box(p,7,7,7);
            case OBSERVATION_BRIDGE -> new BoundingBox(p.getX()-10,p.getY(),p.getZ()-3,p.getX()+10,p.getY()+5,p.getZ()+3);
            case EXCAVATION_SHAFT -> new BoundingBox(p.getX()-5,p.getY(),p.getZ()-5,p.getX()+5,p.getY()+16,p.getZ()+5);
            case SALVAGE_YARD -> box(p,8,7,5);
            default -> box(p,10,9,9);
        };
    }

    private static BoundingBox box(BlockPos p,int hx,int hz,int h){return new BoundingBox(p.getX()-hx,p.getY(),p.getZ()-hz,p.getX()+hx,p.getY()+h,p.getZ()+hz);}

    private static BlockState mod(String id,Block fallback){Block block=ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("matteroverdrive",id));return block==null||block==Blocks.AIR?fallback.defaultBlockState():block.defaultBlockState();}

    private void set(WorldGenLevel level, BoundingBox clip, BlockPos pos, BlockState state) {
        if(!clip.isInside(pos)||!getBoundingBox().isInside(pos))return;
        if(level.getBlockState(pos).is(Blocks.BEDROCK))return;
        level.setBlock(pos,state,2);
        if(state.getBlock() instanceof matteroverdrive.block.TritaniumCrateBlock&&level.getBlockEntity(pos) instanceof matteroverdrive.blockentity.TritaniumCrateBlockEntity crate){String profile=room==Room.SALVAGE_YARD?"salvage":facility.name().toLowerCase(java.util.Locale.ROOT);crate.seedStructureLoot(ResourceLocation.fromNamespaceAndPath("matteroverdrive","chests/facilities/"+profile),level.getSeed()^pos.asLong()^((long)room.ordinal()*73428767L));}
        if(state.getBlock() instanceof matteroverdrive.block.AndroidSpawnerBlock&&level.getBlockEntity(pos) instanceof matteroverdrive.blockentity.AndroidSpawnerBlockEntity spawner){int reserve=switch(facility){case BLACK_SITE->4;case ANDROID_COMMAND_BUNKER->3;default->2;};int ranged=switch(facility){case MATTER_REFINERY,SYNTHETIC_MANUFACTURING_PLANT->25;case BLACK_SITE,QUANTUM_RELAY_STATION->85;default->60;};spawner.configureFacility(facility.name().toLowerCase(java.util.Locale.ROOT),damageVariant()?Math.max(1,reserve-1):reserve,ranged);}
    }

    private record Placement(int x,int y,int z,String id,Block fallback){}
}
