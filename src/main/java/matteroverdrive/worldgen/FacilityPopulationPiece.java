package matteroverdrive.worldgen;

import matteroverdrive.entity.FacilityNpcEntity;
import matteroverdrive.registry.ModEntities;
import matteroverdrive.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
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

import java.util.Locale;

/**
 * Dense, chunk-safe population/dressing overlays for technology facilities.
 *
 * The primary room pieces remain responsible for shells and traversal. These pieces are deliberately
 * small overlays added after the room, infrastructure and terrain pieces: they provide inhabited work
 * areas, role-appropriate MO machinery, and substantially more MO loot caches without returning to
 * synchronous feature stamping. Every block/entity anchor is owned by a bounded StructurePiece.
 */
public final class FacilityPopulationPiece extends StructurePiece {
    public enum Kind {
        RESEARCH_POST,
        ENGINEERING_POST,
        SECURITY_POST,
        LOGISTICS_CACHE,
        ARMORY_CACHE
    }

    private final TechnologyFacilityStructure.Kind facility;
    private final Kind kind;
    private final BlockPos origin;

    public FacilityPopulationPiece(TechnologyFacilityStructure.Kind facility, Kind kind, BlockPos origin) {
        super(ModStructures.FACILITY_POPULATION_PIECE.get(), 0, boxFor(origin));
        this.facility = facility;
        this.kind = kind;
        this.origin = origin;
    }

    public FacilityPopulationPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.FACILITY_POPULATION_PIECE.get(), tag);
        this.facility = TechnologyFacilityStructure.Kind.valueOf(tag.getString("MOFacility"));
        this.kind = Kind.valueOf(tag.getString("MOPopulation"));
        this.origin = new BlockPos(tag.getInt("MOX"), tag.getInt("MOY"), tag.getInt("MOZ"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("MOFacility", facility.name());
        tag.putString("MOPopulation", kind.name());
        tag.putInt("MOX", origin.getX());
        tag.putInt("MOY", origin.getY());
        tag.putInt("MOZ", origin.getZ());
    }

    public static void assemble(StructurePiecesBuilder builder, TechnologyFacilityStructure.Kind facility,
                                BlockPos c, int layout) {
        switch (facility) {
            case SYNTHETIC_MANUFACTURING_PLANT -> {
                BlockPos fabrication = switch (layout) {
                    case 1 -> c.offset(-18, 0, 0);
                    case 2 -> c.offset(-19, 0, 13);
                    default -> c.offset(-18, 0, 0);
                };
                BlockPos assembly = switch (layout) {
                    case 1 -> c.offset(-18, 0, 18);
                    case 2 -> c.offset(19, 0, 13);
                    default -> c.offset(18, 0, 0);
                };
                BlockPos shipping = layout == 0 ? c.offset(0, 0, 19)
                        : layout == 1 ? c.offset(0, 0, 18) : c.offset(0, 0, 24);
                BlockPos entrance = layout == 1 ? c.offset(18, 0, 0) : c.offset(0, 0, -18);
                add(builder, facility, Kind.RESEARCH_POST, fabrication);
                add(builder, facility, Kind.ENGINEERING_POST, assembly);
                add(builder, facility, Kind.LOGISTICS_CACHE, shipping);
                add(builder, facility, Kind.SECURITY_POST, entrance);
            }
            case MATTER_REFINERY -> {
                int side = layout == 1 ? 1 : -1;
                add(builder, facility, Kind.ENGINEERING_POST, c.offset(19 * side, -2, 0));
                add(builder, facility, Kind.LOGISTICS_CACHE, c.offset(-19 * side, 0, 0));
                add(builder, facility, Kind.RESEARCH_POST, c.offset(0, 0, 19));
                add(builder, facility, Kind.SECURITY_POST, c.offset(0, 0, -18));
            }
            case QUANTUM_RELAY_STATION -> {
                int d = layout == 1 ? -1 : 1;
                add(builder, facility, Kind.LOGISTICS_CACHE, c.offset(-16 * d, 0, 10));
                add(builder, facility, Kind.ENGINEERING_POST, c.offset(16 * d, 0, 10));
                add(builder, facility, Kind.RESEARCH_POST, c.offset(0, 0, -16));
                add(builder, facility, Kind.SECURITY_POST, c.offset(0, 0, -28));
            }
            case ANDROID_COMMAND_BUNKER -> {
                BlockPos drone = layout == 0 ? c.offset(-18, 0, 3)
                        : layout == 1 ? c.offset(-18, 0, 0) : c.offset(18, 0, 0);
                BlockPos armory = layout == 0 ? c.offset(0, 0, 19)
                        : layout == 1 ? c.offset(18, 0, 0) : c.offset(-18, 0, 0);
                add(builder, facility, Kind.SECURITY_POST, c.offset(0, 0, -14));
                add(builder, facility, Kind.ENGINEERING_POST, drone);
                add(builder, facility, Kind.ARMORY_CACHE, armory);
            }
            case FUSION_RESEARCH_COMPLEX -> {
                int turn = layout == 1 ? -1 : 1;
                add(builder, facility, Kind.LOGISTICS_CACHE, c.offset(-22 * turn, 0, 0));
                add(builder, facility, Kind.RESEARCH_POST, c.offset(22 * turn, 0, 0));
                add(builder, facility, Kind.ENGINEERING_POST, c.offset(0, -2, 22));
                add(builder, facility, Kind.SECURITY_POST, c.offset(0, 0, -22));
            }
            case BLACK_SITE -> {
                int d = layout == 1 ? -1 : 1;
                // The black site is intentionally abandoned/hostile. Populate it with evidence and
                // high-value caches, not friendly staff who would contradict the active containment threat.
                add(builder, facility, Kind.RESEARCH_POST, c.offset(-18 * d, 0, 0));
                add(builder, facility, Kind.ARMORY_CACHE, c.offset(0, -6, 19));
                add(builder, facility, Kind.SECURITY_POST, c.offset(0, 0, -16));
            }
        }
    }

    private static void add(StructurePiecesBuilder builder, TechnologyFacilityStructure.Kind facility,
                            Kind kind, BlockPos origin) {
        builder.addPiece(new FacilityPopulationPiece(facility, kind, origin));
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        switch (kind) {
            case RESEARCH_POST -> researchPost(level, chunkBox, random);
            case ENGINEERING_POST -> engineeringPost(level, chunkBox, random);
            case SECURITY_POST -> securityPost(level, chunkBox, random);
            case LOGISTICS_CACHE -> logisticsCache(level, chunkBox);
            case ARMORY_CACHE -> armoryCache(level, chunkBox);
        }
    }

    private void researchPost(WorldGenLevel level, BoundingBox clip, RandomSource random) {
        BlockState desk = mod("decorative.vent.dark", Blocks.POLISHED_DEEPSLATE);
        set(level, clip, origin.offset(-3, 1, 3), mod("matter_analyzer", Blocks.LECTERN));
        set(level, clip, origin.offset(-2, 1, 3), desk);
        set(level, clip, origin.offset(-1, 1, 3), mod("holographic_status_panel", Blocks.SEA_LANTERN));
        set(level, clip, origin.offset(3, 1, -3), mod("tritanium_crate_blue", Blocks.BARREL));
        set(level, clip, origin.offset(2, 1, -3), mod("decorative.holo_matrix", Blocks.SEA_LANTERN));
        if (facility == TechnologyFacilityStructure.Kind.BLACK_SITE) {
            set(level, clip, origin.offset(0, 1, -3), Blocks.COBWEB.defaultBlockState());
            set(level, clip, origin.offset(1, 1, 3), Blocks.CRYING_OBSIDIAN.defaultBlockState());
        } else {
            spawnNpc(level, clip, random, FacilityNpcEntity.Role.RESEARCHER, origin.offset(0, 1, -3));
        }
    }

    private void engineeringPost(WorldGenLevel level, BoundingBox clip, RandomSource random) {
        set(level, clip, origin.offset(-3, 1, 3), mod("charging_station", Blocks.LODESTONE));
        set(level, clip, origin.offset(-2, 1, 3), mod("decorative.coils", Blocks.COPPER_BLOCK));
        set(level, clip, origin.offset(3, 1, 3), mod("grid_capacitor", Blocks.IRON_BLOCK));
        set(level, clip, origin.offset(3, 1, -3), mod("tritanium_crate_lime", Blocks.BARREL));
        set(level, clip, origin.offset(2, 1, -3), mod("decorative.vent.dark", Blocks.IRON_BLOCK));
        spawnNpc(level, clip, random, FacilityNpcEntity.Role.ENGINEER, origin.offset(0, 1, -3));
    }

    private void securityPost(WorldGenLevel level, BoundingBox clip, RandomSource random) {
        set(level, clip, origin.offset(-3, 1, 2), mod("tritanium_crate_red", Blocks.BARREL));
        set(level, clip, origin.offset(3, 1, 2), mod("decorative.vent.dark", Blocks.IRON_BLOCK));
        set(level, clip, origin.offset(3, 2, 2), mod("holographic_status_panel", Blocks.SEA_LANTERN));
        set(level, clip, origin.offset(-3, 2, 2), mod("holo_sign", Blocks.REDSTONE_LAMP));
        if (facility == TechnologyFacilityStructure.Kind.BLACK_SITE) {
            set(level, clip, origin.offset(0, 1, 2), Blocks.IRON_BARS.defaultBlockState());
            set(level, clip, origin.offset(0, 2, 2), Blocks.IRON_BARS.defaultBlockState());
        } else {
            spawnNpc(level, clip, random, FacilityNpcEntity.Role.SECURITY, origin.offset(0, 1, 0));
        }
    }

    private void logisticsCache(WorldGenLevel level, BoundingBox clip) {
        set(level, clip, origin.offset(-3, 1, 3), mod("tritanium_crate", Blocks.BARREL));
        set(level, clip, origin.offset(0, 1, 3), mod("tritanium_crate_blue", Blocks.BARREL));
        set(level, clip, origin.offset(3, 1, 3), mod("tritanium_crate_lime", Blocks.BARREL));
        set(level, clip, origin.offset(3, 1, -3), mod("tritanium_crate", Blocks.BARREL));
        set(level, clip, origin.offset(-3, 1, -3), mod("decorative.vent.dark", Blocks.IRON_BLOCK));
        set(level, clip, origin.offset(-3, 2, -3), mod("decorative.beams", Blocks.POLISHED_DEEPSLATE));
    }

    private void armoryCache(WorldGenLevel level, BoundingBox clip) {
        set(level, clip, origin.offset(-3, 1, 3), mod("tritanium_crate_red", Blocks.BARREL));
        set(level, clip, origin.offset(0, 1, 3), mod("tritanium_crate_red", Blocks.BARREL));
        set(level, clip, origin.offset(3, 1, 3), mod("tritanium_crate_blue", Blocks.BARREL));
        set(level, clip, origin.offset(-3, 1, -3), mod("tritanium_crate", Blocks.BARREL));
        set(level, clip, origin.offset(3, 1, -3), mod("android_station", Blocks.SMITHING_TABLE));
        set(level, clip, origin.offset(2, 2, -3), mod("holo_sign", Blocks.REDSTONE_LAMP));
    }

    private void spawnNpc(WorldGenLevel level, BoundingBox clip, RandomSource random,
                          FacilityNpcEntity.Role role, BlockPos pos) {
        if (!clip.isInside(pos) || !getBoundingBox().isInside(pos)) return;
        if (!level.getBlockState(pos).isAir()) return;

        EntityType<FacilityNpcEntity> type = switch (role) {
            case RESEARCHER -> ModEntities.FACILITY_RESEARCHER.get();
            case ENGINEER -> ModEntities.FACILITY_ENGINEER.get();
            case SECURITY -> ModEntities.FACILITY_SECURITY.get();
        };
        FacilityNpcEntity npc = type.create(level.getLevel());
        if (npc == null) return;
        npc.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
        npc.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
        npc.getPersistentData().putString("MOFacility", facility.name());
        npc.getPersistentData().putLong("MOFacilityAnchor", origin.asLong());
        level.addFreshEntityWithPassengers(npc);
    }

    private void set(WorldGenLevel level, BoundingBox clip, BlockPos pos, BlockState state) {
        if (!clip.isInside(pos) || !getBoundingBox().isInside(pos)) return;
        if (level.getBlockState(pos).is(Blocks.BEDROCK)) return;
        level.setBlock(pos, state, 2);
        if (state.getBlock() instanceof matteroverdrive.block.TritaniumCrateBlock
                && level.getBlockEntity(pos) instanceof matteroverdrive.blockentity.TritaniumCrateBlockEntity crate) {
            String profile = facility.name().toLowerCase(Locale.ROOT);
            crate.seedStructureLoot(new ResourceLocation("matteroverdrive", "chests/facilities/" + profile),
                    level.getSeed() ^ pos.asLong() ^ ((long) kind.ordinal() * 982451653L));
        }
    }

    private static BlockState mod(String id, Block fallback) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("matteroverdrive", id));
        return block == null || block == Blocks.AIR ? fallback.defaultBlockState() : block.defaultBlockState();
    }

    private static BoundingBox boxFor(BlockPos p) {
        return new BoundingBox(p.getX() - 5, p.getY(), p.getZ() - 5,
                p.getX() + 5, p.getY() + 4, p.getZ() + 5);
    }
}
