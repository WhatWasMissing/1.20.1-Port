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

/** Modular rooms for the Frontier Expedition structure family. */
public final class FrontierSitePiece extends StructurePiece {
    public enum Room {
        VAULT_ENTRY, VAULT_SECURITY, VAULT_ARCHIVE, VAULT_REFINERY, VAULT_CORE,
        FOUNDRY_ENTRY, FOUNDRY_CONTROL, FOUNDRY_FABRICATION, FOUNDRY_HANGAR, FOUNDRY_SALVAGE,
        QUARANTINE_ENTRY, QUARANTINE_DECON, QUARANTINE_OBSERVATION, QUARANTINE_CONTAINMENT, QUARANTINE_SECURITY,
        RECOVERY_ENTRY, RECOVERY_CONTROL, RECOVERY_PROCESSING, RECOVERY_STORAGE, RECOVERY_ARRAY,
        CORRIDOR_X, CORRIDOR_Z, SHAFT, CATWALK
    }

    private final FrontierSiteStructure.Kind site;
    private final Room room;
    private final BlockPos origin;
    private final int layout;

    public FrontierSitePiece(FrontierSiteStructure.Kind site, Room room, BlockPos origin, int layout) {
        super(ModStructures.FRONTIER_SITE_PIECE.get(), 0, boxFor(room, origin));
        this.site = site;
        this.room = room;
        this.origin = origin;
        this.layout = layout;
    }

    public FrontierSitePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.FRONTIER_SITE_PIECE.get(), tag);
        this.site = FrontierSiteStructure.Kind.valueOf(tag.getString("MOFrontierSite"));
        this.room = Room.valueOf(tag.getString("MOFrontierRoom"));
        this.origin = new BlockPos(tag.getInt("MOX"), tag.getInt("MOY"), tag.getInt("MOZ"));
        this.layout = Math.floorMod(tag.getInt("MOLayout"), 3);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("MOFrontierSite", site.name());
        tag.putString("MOFrontierRoom", room.name());
        tag.putInt("MOX", origin.getX());
        tag.putInt("MOY", origin.getY());
        tag.putInt("MOZ", origin.getZ());
        tag.putInt("MOLayout", layout);
    }

    public static void assemble(StructurePiecesBuilder b, FrontierSiteStructure.Kind site, BlockPos c, int layout) {
        switch (site) {
            case DEEP_MATTER_VAULT -> {
                add(b, site, Room.VAULT_CORE, c, layout);
                add(b, site, Room.VAULT_SECURITY, c.offset(0, 0, -18), layout);
                add(b, site, Room.VAULT_ENTRY, c.offset(0, 15, -30), layout);
                add(b, site, Room.VAULT_ARCHIVE, c.offset(layout == 1 ? 20 : -20, 0, 0), layout);
                add(b, site, Room.VAULT_REFINERY, c.offset(layout == 1 ? -20 : 20, 0, 0), layout);
                add(b, site, Room.CORRIDOR_Z, c.offset(0, 0, -10), layout);
                add(b, site, Room.CORRIDOR_X, c.offset(-10, 0, 0), layout);
                add(b, site, Room.CORRIDOR_X, c.offset(10, 0, 0), layout);
                add(b, site, Room.SHAFT, c.offset(0, 7, -25), layout);
            }
            case AUTONOMOUS_DRONE_FOUNDRY -> {
                add(b, site, Room.FOUNDRY_CONTROL, c, layout);
                add(b, site, Room.FOUNDRY_ENTRY, c.offset(0, 0, -20), layout);
                add(b, site, Room.FOUNDRY_FABRICATION, c.offset(-20, 0, layout == 2 ? 12 : 0), layout);
                add(b, site, Room.FOUNDRY_HANGAR, c.offset(20, 0, layout == 2 ? 12 : 0), layout);
                add(b, site, Room.FOUNDRY_SALVAGE, c.offset(0, 0, 22), layout);
                add(b, site, Room.CORRIDOR_Z, c.offset(0, 0, -11), layout);
                add(b, site, Room.CORRIDOR_X, c.offset(-11, 0, 0), layout);
                add(b, site, Room.CORRIDOR_X, c.offset(11, 0, 0), layout);
                add(b, site, Room.CATWALK, c.offset(0, 6, 12), layout);
            }
            case ANOMALY_QUARANTINE_SITE -> {
                add(b, site, Room.QUARANTINE_CONTAINMENT, c, layout);
                add(b, site, Room.QUARANTINE_SECURITY, c.offset(0, 0, -20), layout);
                add(b, site, Room.QUARANTINE_ENTRY, c.offset(0, 8, -32), layout);
                add(b, site, Room.QUARANTINE_DECON, c.offset(-20, 0, 0), layout);
                add(b, site, Room.QUARANTINE_OBSERVATION, c.offset(20, 0, 0), layout);
                add(b, site, Room.CORRIDOR_Z, c.offset(0, 0, -11), layout);
                add(b, site, Room.CORRIDOR_X, c.offset(-11, 0, 0), layout);
                add(b, site, Room.CORRIDOR_X, c.offset(11, 0, 0), layout);
                add(b, site, Room.SHAFT, c.offset(0, 4, -27), layout);
            }
            case ORBITAL_RECOVERY_ARRAY -> {
                add(b, site, Room.RECOVERY_CONTROL, c, layout);
                add(b, site, Room.RECOVERY_ENTRY, c.offset(0, 0, -18), layout);
                add(b, site, Room.RECOVERY_PROCESSING, c.offset(-20, 0, 0), layout);
                add(b, site, Room.RECOVERY_STORAGE, c.offset(20, 0, 0), layout);
                add(b, site, Room.RECOVERY_ARRAY, c.offset(0, 0, 22), layout);
                add(b, site, Room.CORRIDOR_Z, c.offset(0, 0, -10), layout);
                add(b, site, Room.CORRIDOR_X, c.offset(-11, 0, 0), layout);
                add(b, site, Room.CORRIDOR_X, c.offset(11, 0, 0), layout);
                add(b, site, Room.CATWALK, c.offset(0, 6, 10), layout);
            }
        }
    }

    private static void add(StructurePiecesBuilder b, FrontierSiteStructure.Kind site, Room room, BlockPos pos, int layout) {
        b.addPiece(new FrontierSitePiece(site, room, pos, layout));
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        switch (room) {
            case VAULT_ENTRY -> entry(level, chunkBox, true);
            case VAULT_SECURITY -> room(level, chunkBox, 7, 7, 5, darkWall(), darkFloor(), true,
                    p(-3,1,0,"android_spawner", Blocks.IRON_BLOCK), p(3,1,0,"grid_capacitor", Blocks.IRON_BLOCK));
            case VAULT_ARCHIVE -> room(level, chunkBox, 8, 7, 5, whiteWall(), darkFloor(), false,
                    p(-3,1,0,"pattern_storage", Blocks.CHISELED_BOOKSHELF), p(3,1,0,"matter_analyzer", Blocks.LECTERN), p(0,1,3,"tritanium_crate_blue", Blocks.BARREL));
            case VAULT_REFINERY -> room(level, chunkBox, 8, 7, 5, greenWall(), greenFloor(), false,
                    p(-3,1,0,"matter_storage_matrix", Blocks.IRON_BLOCK), p(0,1,0,"decomposer", Blocks.BLAST_FURNACE), p(3,1,0,"matter_excavator", Blocks.BLAST_FURNACE));
            case VAULT_CORE -> room(level, chunkBox, 9, 9, 6, darkWall(), darkFloor(), true,
                    p(0,1,0,"facility_network_controller", Blocks.IRON_BLOCK), p(-4,1,3,"matter_storage_matrix", Blocks.IRON_BLOCK), p(4,1,3,"holographic_status_panel", Blocks.SEA_LANTERN), p(0,1,-4,"tritanium_crate_red", Blocks.BARREL));

            case FOUNDRY_ENTRY -> entry(level, chunkBox, false);
            case FOUNDRY_CONTROL -> room(level, chunkBox, 8, 8, 5, whiteWall(), darkFloor(), true,
                    p(0,1,0,"facility_network_controller", Blocks.IRON_BLOCK), p(-3,1,2,"charging_station", Blocks.LODESTONE), p(3,1,2,"holographic_status_panel", Blocks.SEA_LANTERN));
            case FOUNDRY_FABRICATION -> room(level, chunkBox, 9, 8, 6, paletteWall(), darkFloor(), false,
                    p(-4,1,0,"drone_fabricator", Blocks.SMITHING_TABLE), p(0,1,0,"inscriber", Blocks.ANVIL), p(4,1,0,"android_station", Blocks.SMITHING_TABLE));
            case FOUNDRY_HANGAR -> hangar(level, chunkBox);
            case FOUNDRY_SALVAGE -> salvage(level, chunkBox);

            case QUARANTINE_ENTRY -> entry(level, chunkBox, true);
            case QUARANTINE_SECURITY -> room(level, chunkBox, 8, 7, 5, blackWall(), darkFloor(), true,
                    p(-3,1,0,"android_spawner", Blocks.IRON_BLOCK), p(3,1,0,"grid_capacitor", Blocks.IRON_BLOCK));
            case QUARANTINE_DECON -> room(level, chunkBox, 8, 7, 5, whiteWall(), paletteFloor(), false,
                    p(-3,1,0,"matter_analyzer", Blocks.LECTERN), p(3,1,0,"charging_station", Blocks.LODESTONE));
            case QUARANTINE_OBSERVATION -> room(level, chunkBox, 8, 7, 5, blackWall(), paletteFloor(), false,
                    p(0,1,0,"holographic_status_panel", Blocks.SEA_LANTERN), p(3,1,2,"tritanium_crate_blue", Blocks.BARREL));
            case QUARANTINE_CONTAINMENT -> containment(level, chunkBox);

            case RECOVERY_ENTRY -> entry(level, chunkBox, false);
            case RECOVERY_CONTROL -> room(level, chunkBox, 8, 8, 5, paletteWall(), paletteFloor(), true,
                    p(0,1,0,"facility_network_controller", Blocks.IRON_BLOCK), p(-3,1,2,"network_switch", Blocks.IRON_BLOCK), p(3,1,2,"holographic_status_panel", Blocks.SEA_LANTERN));
            case RECOVERY_PROCESSING -> room(level, chunkBox, 8, 7, 5, whiteWall(), paletteFloor(), false,
                    p(-3,1,0,"matter_analyzer", Blocks.LECTERN), p(3,1,0,"decomposer", Blocks.BLAST_FURNACE));
            case RECOVERY_STORAGE -> room(level, chunkBox, 8, 7, 5, paletteWall(), darkFloor(), false,
                    p(-4,1,2,"tritanium_crate", Blocks.BARREL), p(0,1,2,"tritanium_crate_cyan", Blocks.BARREL), p(4,1,2,"tritanium_crate_lime", Blocks.BARREL));
            case RECOVERY_ARRAY -> recoveryArray(level, chunkBox);

            case CORRIDOR_X -> corridor(level, chunkBox, true);
            case CORRIDOR_Z -> corridor(level, chunkBox, false);
            case SHAFT -> shaft(level, chunkBox);
            case CATWALK -> catwalk(level, chunkBox);
        }
    }

    private void room(WorldGenLevel level, BoundingBox clip, int hx, int hz, int h, BlockState wall, BlockState floor,
                      boolean fourDoors, Placement... placements) {
        BlockState frame = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState glass = mod("industrial_glass", Blocks.TINTED_GLASS);
        BlockState lamp = mod("decorative.tritanium_lamp", Blocks.SEA_LANTERN);
        boolean damaged = damaged();
        for (int x = -hx; x <= hx; x++) for (int z = -hz; z <= hz; z++) {
            set(level, clip, origin.offset(x, 0, z), floor);
            boolean edge = Math.abs(x) == hx || Math.abs(z) == hz;
            for (int y = 1; y <= h; y++) {
                boolean doorNS = Math.abs(x) <= 1 && Math.abs(z) == hz && y <= 3;
                boolean doorEW = Math.abs(z) <= 1 && Math.abs(x) == hx && y <= 3;
                if (!edge || doorNS || (fourDoors && doorEW)) set(level, clip, origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
                else if ((Math.abs(x) >= hx - 1 && Math.abs(z) >= hz - 1) || y == 1 || y == h) set(level, clip, origin.offset(x, y, z), frame);
                else if (y == 3 && ((x + z) & 1) == 0) set(level, clip, origin.offset(x, y, z), glass);
                else set(level, clip, origin.offset(x, y, z), wall);
            }
            set(level, clip, origin.offset(x, h + 1, z), ((x + z) % 6 == 0) ? frame : wall);
        }
        for (int x = -hx + 2; x <= hx - 2; x += 4) set(level, clip, origin.offset(x, h, 0), lamp);
        for (Placement p : placements) {
            boolean wreck = damaged && p.x > 0 && !p.id.startsWith("tritanium_crate") && !p.id.equals("android_spawner");
            set(level, clip, origin.offset(p.x, p.y, p.z), wreck ? mod("decorative.vent.dark", Blocks.IRON_BLOCK) : mod(p.id, p.fallback));
        }
        if (damaged) damageRoom(level, clip, hx, hz, h);
        if (occupied()) {
            set(level, clip, origin.offset(-hx + 2, 1, hz - 2), mod("android_spawner", Blocks.IRON_BLOCK));
            set(level, clip, origin.offset(hx - 2, 1, -hz + 2), Blocks.REDSTONE_TORCH.defaultBlockState());
        }
    }

    private void entry(WorldGenLevel level, BoundingBox clip, boolean reinforced) {
        room(level, clip, 5, 7, 5, reinforced ? blackWall() : paletteWall(), paletteFloor(), false,
                p(0,1,2,"holo_sign", Blocks.SEA_LANTERN));
        for (int z = -10; z <= -7; z++) for (int x = -3; x <= 3; x++) {
            set(level, clip, origin.offset(x, 4, z), darkWall());
            if (Math.abs(x) == 3) set(level, clip, origin.offset(x, 1, z), mod("decorative.beams", Blocks.POLISHED_DEEPSLATE));
        }
    }

    private void hangar(WorldGenLevel level, BoundingBox clip) {
        room(level, clip, 10, 9, 7, paletteWall(), darkFloor(), true,
                p(-5,1,0,"charging_station", Blocks.LODESTONE), p(0,1,0,"android_spawner", Blocks.IRON_BLOCK), p(5,1,0,"charging_station", Blocks.LODESTONE));
        for (int x = -7; x <= 7; x += 7) for (int z = -5; z <= 5; z += 5)
            set(level, clip, origin.offset(x, 1, z), mod("decorative.tritanium_plate_stripe", Blocks.YELLOW_CONCRETE));
    }

    private void salvage(WorldGenLevel level, BoundingBox clip) {
        BlockState floor = Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState();
        for (int x = -9; x <= 9; x++) for (int z = -9; z <= 9; z++) {
            if (x * x + z * z <= 80) set(level, clip, origin.offset(x, 0, z), floor);
        }
        for (int i = -6; i <= 6; i += 3) {
            set(level, clip, origin.offset(i, 1, 2), mod(i % 2 == 0 ? "tritanium_crate" : "decorative.vent.dark", Blocks.IRON_BLOCK));
            set(level, clip, origin.offset(-2, 1, i), mod("decorative.beams", Blocks.POLISHED_DEEPSLATE));
        }
        set(level, clip, origin.offset(5,1,-4), mod("android_spawner", Blocks.IRON_BLOCK));
    }

    private void containment(WorldGenLevel level, BoundingBox clip) {
        room(level, clip, 10, 10, 7, blackWall(), darkFloor(), true,
                p(0,1,0,"anomaly_containment_unit", Blocks.CRYING_OBSIDIAN),
                p(-5,1,0,"gravitational_stabilizer", Blocks.OBSIDIAN),
                p(5,1,0,"gravitational_stabilizer", Blocks.OBSIDIAN),
                p(0,1,5,"facility_network_controller", Blocks.IRON_BLOCK));
        for (int x = -4; x <= 4; x++) for (int z = -4; z <= 4; z++) {
            if (Math.abs(x) == 4 || Math.abs(z) == 4) set(level, clip, origin.offset(x, 2, z), mod("industrial_glass", Blocks.TINTED_GLASS));
        }
    }

    private void recoveryArray(WorldGenLevel level, BoundingBox clip) {
        BlockState frame = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        BlockState floor = paletteFloor();
        for (int x = -11; x <= 11; x++) for (int z = -11; z <= 11; z++) {
            if (x * x + z * z <= 121) set(level, clip, origin.offset(x, 0, z), floor);
        }
        for (int y = 1; y <= 13; y++) set(level, clip, origin.offset(0, y, 0), frame);
        for (int r = 2; r <= 8; r += 2) for (int x = -r; x <= r; x++) {
            int z = r - Math.abs(x);
            set(level, clip, origin.offset(x, 8 + r / 2, z), mod("industrial_glass", Blocks.TINTED_GLASS));
        }
        set(level, clip, origin.offset(0,1,-5), mod("quantum_power_relay", Blocks.RESPAWN_ANCHOR));
        set(level, clip, origin.offset(4,1,4), mod("android_spawner", Blocks.IRON_BLOCK));
    }

    private void corridor(WorldGenLevel level, BoundingBox clip, boolean xAxis) {
        BlockState wall = site == FrontierSiteStructure.Kind.ANOMALY_QUARANTINE_SITE ? blackWall() : paletteWall();
        BlockState floor = site == FrontierSiteStructure.Kind.DEEP_MATTER_VAULT ? greenFloor() : darkFloor();
        BlockState frame = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        for (int a = -6; a <= 6; a++) for (int b = -2; b <= 2; b++) {
            int x = xAxis ? a : b;
            int z = xAxis ? b : a;
            set(level, clip, origin.offset(x, 0, z), floor);
            for (int y = 1; y <= 4; y++) set(level, clip, origin.offset(x, y, z), Math.abs(b) == 2 ? ((a & 3) == 0 ? frame : wall) : Blocks.AIR.defaultBlockState());
            set(level, clip, origin.offset(x, 5, z), (a & 3) == 0 ? frame : wall);
        }
    }

    private void shaft(WorldGenLevel level, BoundingBox clip) {
        BlockState frame = mod("decorative.beams", Blocks.POLISHED_DEEPSLATE);
        for (int y = -7; y <= 8; y++) for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            boolean edge = Math.abs(x) == 3 || Math.abs(z) == 3;
            set(level, clip, origin.offset(x, y, z), edge ? frame : Blocks.AIR.defaultBlockState());
        }
        for (int y = -6; y <= 7; y += 2) set(level, clip, origin.offset(2, y, 0), Blocks.LADDER.defaultBlockState());
    }

    private void catwalk(WorldGenLevel level, BoundingBox clip) {
        BlockState floor = mod("decorative.floor_tiles", Blocks.IRON_BLOCK);
        BlockState rail = mod("decorative.beams", Blocks.IRON_BARS);
        for (int x = -12; x <= 12; x++) {
            for (int z = -1; z <= 1; z++) set(level, clip, origin.offset(x, 0, z), floor);
            set(level, clip, origin.offset(x, 1, -2), rail);
            set(level, clip, origin.offset(x, 1, 2), rail);
        }
    }

    private void damageRoom(WorldGenLevel level, BoundingBox clip, int hx, int hz, int h) {
        for (int y = h - 1; y <= h + 1; y++) set(level, clip, origin.offset(hx, y, hz - 2), Blocks.AIR.defaultBlockState());
        for (int x = hx - 3; x <= hx - 1; x++) for (int z = hz - 3; z <= hz - 1; z++) {
            if (((x + z) & 1) == 0) set(level, clip, origin.offset(x, 1, z), Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState());
        }
        set(level, clip, origin.offset(-hx + 2, h - 1, hz - 2), Blocks.COBWEB.defaultBlockState());
    }

    private boolean damaged() {
        return Math.floorMod(origin.getX() * 31 + origin.getZ() * 17 + room.ordinal() * 13 + layout, 5) <= 1;
    }

    private boolean occupied() {
        return Math.floorMod(origin.getX() * 7 + origin.getZ() * 11 + site.ordinal() * 19 + layout, 4) != 0;
    }

    private static BoundingBox boxFor(Room room, BlockPos p) {
        int hx = switch (room) {
            case VAULT_CORE, FOUNDRY_FABRICATION, FOUNDRY_SALVAGE -> 11;
            case FOUNDRY_HANGAR, QUARANTINE_CONTAINMENT, RECOVERY_ARRAY -> 13;
            case CORRIDOR_X -> 7;
            case CORRIDOR_Z, SHAFT -> 4;
            case CATWALK -> 13;
            default -> 10;
        };
        int hz = switch (room) {
            case VAULT_CORE, FOUNDRY_FABRICATION, FOUNDRY_SALVAGE -> 11;
            case FOUNDRY_HANGAR, QUARANTINE_CONTAINMENT, RECOVERY_ARRAY -> 13;
            case CORRIDOR_Z -> 7;
            case CORRIDOR_X, SHAFT -> 4;
            case CATWALK -> 4;
            default -> 10;
        };
        int down = room == Room.SHAFT ? 8 : 2;
        int up = switch (room) { case RECOVERY_ARRAY -> 16; case SHAFT -> 10; default -> 10; };
        return new BoundingBox(p.getX() - hx, p.getY() - down, p.getZ() - hz, p.getX() + hx, p.getY() + up, p.getZ() + hz);
    }

    private static BlockState mod(String id, Block fallback) {
        ResourceLocation key = ResourceLocation.tryParse("matteroverdrive:" + id);
        Block block = key == null ? null : ForgeRegistries.BLOCKS.getValue(key);
        return block == null || block == Blocks.AIR ? fallback.defaultBlockState() : block.defaultBlockState();
    }

    private static BlockState paletteWall() { return mod("decorative.tritanium_plate", Blocks.IRON_BLOCK); }
    private static BlockState whiteWall() { return mod("decorative.white_plate", Blocks.QUARTZ_BLOCK); }
    private static BlockState blackWall() { return mod("decorative.carbon_fiber_plate", Blocks.POLISHED_BLACKSTONE); }
    private static BlockState greenWall() { return mod("decorative.tritanium_plate_green", Blocks.OXIDIZED_COPPER); }
    private static BlockState darkWall() { return mod("decorative.vent.dark", Blocks.DEEPSLATE_BRICKS); }
    private static BlockState paletteFloor() { return mod("decorative.floor_tiles", Blocks.SMOOTH_STONE); }
    private static BlockState darkFloor() { return mod("decorative.floor_tiles_dark", Blocks.DEEPSLATE_TILES); }
    private static BlockState greenFloor() { return mod("decorative.floor_tiles_green", Blocks.OXIDIZED_COPPER); }

    private static void set(WorldGenLevel level, BoundingBox clip, BlockPos pos, BlockState state) {
        if (clip.isInside(pos)) level.setBlock(pos, state, 2);
    }

    private static Placement p(int x, int y, int z, String id, Block fallback) {
        return new Placement(x, y, z, id, fallback);
    }

    private record Placement(int x, int y, int z, String id, Block fallback) {}
}
