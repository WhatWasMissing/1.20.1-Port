package matteroverdrive.worldgen;

import matteroverdrive.blockentity.TritaniumCrateBlockEntity;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Exact decoder for the legacy MOImageGen PNG format used by the 1.12.2
 * crashed ship, cargo ship and underwater-base generators.
 *
 * The PNG is a row-major atlas of horizontal Y slices. RGB selects the block;
 * unmapped colours leave terrain untouched. Cargo/underwater alpha stores legacy
 * metadata as 255-alpha. Metadata is translated only when 1.20.1 has an equivalent
 * block-state representation.
 */
public final class LegacyImageTemplatePlacer {
    private static final Map<LegacyParityStructureFeature.Kind, Template> CACHE = new HashMap<>();

    private LegacyImageTemplatePlacer() {}

    public static boolean supports(LegacyParityStructureFeature.Kind kind) {
        return kind == LegacyParityStructureFeature.Kind.CRASHED_SHIP
                || kind == LegacyParityStructureFeature.Kind.CARGO_SHIP
                || kind == LegacyParityStructureFeature.Kind.UNDERWATER_BASE;
    }

    public static boolean place(WorldGenLevel level, BlockPos origin, RandomSource random,
                                LegacyParityStructureFeature.Kind kind) {
        if (!supports(kind)) return false;
        Template template = load(kind);
        if (template == null) return false;

        BlockPos base = switch (kind) {
            case CRASHED_SHIP -> origin.below();
            case CARGO_SHIP, UNDERWATER_BASE -> origin.above();
            default -> origin;
        };

        int minX = -(template.layerWidth / 2);
        int minZ = -(template.layerHeight / 2);
        for (int layer = 0; layer < template.layers; layer++) {
            int tileX = layer % template.tilesX;
            int tileZ = layer / template.tilesX;
            for (int x = 0; x < template.layerWidth; x++) {
                for (int z = 0; z < template.layerHeight; z++) {
                    int argb = template.image.getRGB(tileX * template.layerWidth + x,
                            tileZ * template.layerHeight + z);
                    int rgb = argb & 0xFFFFFF;
                    Mapping mapping = mapping(kind, rgb, random);
                    if (mapping == null) continue;
                    int metadata = template.alphaMetadata ? 255 - ((argb >>> 24) & 0xFF) : 0;
                    BlockState state = applyLegacyMetadata(mapping.state, metadata, mapping.metadataKind);
                    BlockPos pos = base.offset(minX + x, layer, minZ + z);
                    level.setBlock(pos, state, 2);
                    if (state.getBlock() instanceof matteroverdrive.block.TritaniumCrateBlock) {
                        fillCrate(level, pos, random, kind);
                    }
                }
            }
        }
        return true;
    }

    private static synchronized Template load(LegacyParityStructureFeature.Kind kind) {
        if (CACHE.containsKey(kind)) return CACHE.get(kind);
        String name;
        int layerWidth;
        int layerHeight;
        boolean alphaMetadata;
        switch (kind) {
            case CRASHED_SHIP -> { name = "crashed_ship.png"; layerWidth = 11; layerHeight = 35; alphaMetadata = false; }
            case CARGO_SHIP -> { name = "cargo_ship.png"; layerWidth = 58; layerHeight = 23; alphaMetadata = true; }
            case UNDERWATER_BASE -> { name = "underwater_base.png"; layerWidth = 43; layerHeight = 43; alphaMetadata = true; }
            default -> { return null; }
        }
        String path = "/assets/matteroverdrive/textures/world/" + name;
        try (InputStream input = LegacyImageTemplatePlacer.class.getResourceAsStream(path)) {
            if (input == null) return null;
            BufferedImage image = ImageIO.read(input);
            if (image == null || image.getWidth() % layerWidth != 0 || image.getHeight() % layerHeight != 0) return null;
            int tilesX = image.getWidth() / layerWidth;
            int tilesZ = image.getHeight() / layerHeight;
            Template template = new Template(image, layerWidth, layerHeight, tilesX, tilesX * tilesZ, alphaMetadata);
            CACHE.put(kind, template);
            return template;
        } catch (IOException ex) {
            return null;
        }
    }

    private static Mapping mapping(LegacyParityStructureFeature.Kind kind, int rgb, RandomSource random) {
        Mapping decorative = decorative(rgb);
        if (decorative != null && kind != LegacyParityStructureFeature.Kind.CRASHED_SHIP) return decorative;
        return switch (kind) {
            case CRASHED_SHIP -> crashed(rgb);
            case CARGO_SHIP -> cargo(rgb, random);
            case UNDERWATER_BASE -> underwater(rgb);
            default -> null;
        };
    }

    private static Mapping decorative(int rgb) {
        return switch (rgb) {
            case 0xD4B108 -> mo("decorative.stripes");
            case 0xB6621E -> mo("decorative.coils");
            case 0x3B484B -> mo("decorative.clean");
            case 0x32393C -> mo("decorative.vent.dark");
            case 0x3F4B4E -> mo("decorative.vent.bright");
            case 0x323B3A -> mo("decorative.holo_matrix");
            case 0x475459 -> mo("decorative.tritanium_plate");
            case 0x576468 -> mo("decorative.tritanium_plate_stripe");
            case 0x1C1F20 -> mo("decorative.carbon_fiber_plate");
            case 0x5088A5 -> mo("decorative.matter_tube");
            case 0x1E2220 -> mo("decorative.beams");
            case 0x958D7C -> mo("decorative.floor_tiles");
            case 0xA3A49C -> mo("decorative.floor_tile_white");
            case 0x53593F -> mo("decorative.floor_tiles_green");
            case 0x7F7E7B -> mo("decorative.floor_noise");
            case 0xE3E3E3 -> mo("decorative.white_plate");
            case 0x303837 -> mo("decorative.separator");
            case 0xD4F8F5 -> mo("decorative.tritanium_lamp");
            case 0x505050 -> mo("decorative.tritanium_plate_colored");
            case 0x387C9E -> mo("decorative.engine_exhaust_plasma");
            default -> null;
        };
    }

    private static Mapping crashed(int rgb) {
        return switch (rgb) {
            case 0x38C8DF -> mo("decorative.clean");
            case 0x187B8B -> mo("decorative.vent.bright");
            case 0x00FF78 -> vanilla(Blocks.GRASS_BLOCK);
            case 0xD8FF00, 0xACCB00 -> mo("holo_sign");
            case 0x3896DF -> mo("decorative.tritanium_plate");
            case 0xDFD938 -> mo("decorative.tritanium_plate_stripe");
            case 0x5D89AB -> mo("decorative.holo_matrix");
            case 0x77147D -> mo("weapon_station");
            case 0xB04A90 -> mo("tritanium_crate");
            case 0x94DEEA -> mo("decorative.separator");
            case 0xFF9C00 -> mo("decorative.coils");
            case 0xACA847 -> mo("decorative.matter_tube");
            case 0x0C3B60 -> mo("decorative.carbon_fiber_plate");
            case 0xC5CED0 -> vanilla(Blocks.AIR);
            default -> null;
        };
    }

    private static Mapping cargo(int rgb, RandomSource random) {
        return switch (rgb) {
            case 0xDB9C3A -> mo("holo_sign");
            case 0x5FFFBE -> mo("transporter");
            case 0xD2FB50 -> mo("industrial_glass");
            case 0xDC01D8 -> vanilla(Blocks.OAK_PRESSURE_PLATE);
            case 0xFC6B34 -> vanilla(switch (random.nextInt(4)) {
                case 0 -> Blocks.GOLD_ORE;
                case 1 -> Blocks.IRON_ORE;
                case 2 -> Blocks.COAL_ORE;
                default -> ModBlocks.get("tritanium_ore").get();
            });
            case 0x0D1626 -> mo("fusion_reactor_io");
            case 0x1B2FF7 -> mo("network_pipe");
            case 0x1F2312 -> mo("tritanium_crate_lime");
            case 0xAB4824 -> vanilla(Blocks.OAK_FENCE);
            case 0x68D738 -> new Mapping(Blocks.WHITE_CARPET.defaultBlockState(), MetadataKind.DYE_CARPET);
            case 0xBDEA8F -> new Mapping(Blocks.LADDER.defaultBlockState(), MetadataKind.HORIZONTAL);
            case 0xEFF73D -> mo("network_switch");
            case 0xA8ED1C -> mo("heavy_matter_pipe");
            case 0x4B285D -> new Mapping(Blocks.OAK_STAIRS.defaultBlockState(), MetadataKind.STAIRS);
            case 0xCFD752 -> mo("network_router");
            case 0x4D8DD3 -> mo("pattern_monitor");
            case 0x6B3534 -> vanilla(Blocks.RED_BED);
            case 0xFF00FF -> vanilla(Blocks.AIR);
            case 0x69960C -> mo("tritanium_crate_red");
            default -> null;
        };
    }

    private static Mapping underwater(int rgb) {
        return switch (rgb) {
            case 0xDC979C -> new Mapping(Blocks.SHORT_GRASS.defaultBlockState(), MetadataKind.TALL_GRASS);
            case 0x77D1B6 -> new Mapping(Blocks.POPPY.defaultBlockState(), MetadataKind.FLOWER);
            case 0x0C1E4E -> vanilla(Blocks.FARMLAND);
            case 0xA7AC65 -> mo("tritanium_crate_orange");
            case 0xD6A714 -> new Mapping(Blocks.WHITE_STAINED_GLASS.defaultBlockState(), MetadataKind.DYE_GLASS);
            case 0x2C5AE9 -> mo("weapon_station");
            case 0x0ACD8C -> mo("android_station");
            case 0x7018F9 -> mo("tritanium_crate_light_blue");
            case 0x4657CC -> mo("tritanium_crate_lime");
            case 0x1F2312 -> mo("tritanium_crate_white");
            case 0xD3371D -> mo("machine_hull");
            case 0x3640F9 -> new Mapping(Blocks.STONE_BUTTON.defaultBlockState(), MetadataKind.HORIZONTAL);
            case 0xEFF73D -> mo("network_switch");
            case 0x5A6388 -> mo("bounding_box");
            case 0xBF19A9 -> vanilla(Blocks.GRASS_BLOCK);
            case 0xC05E5E -> vanilla(Blocks.FLOWER_POT);
            case 0x4D8DD3 -> mo("pattern_monitor");
            case 0xDB9C3A -> mo("holo_sign");
            case 0x68B68C -> mo("matter_analyzer");
            case 0x2CB0C7 -> mo("star_map");
            case 0x1B2FF7 -> mo("network_pipe");
            case 0x05EAAB -> mo("tritanium_crate");
            case 0x11003E -> mo("charging_station");
            case 0xB31E83 -> new Mapping(Blocks.CARROTS.defaultBlockState(), MetadataKind.CROP);
            case 0xC78E77 -> mo("replicator");
            case 0x338A42 -> new Mapping(Blocks.POTATOES.defaultBlockState(), MetadataKind.CROP);
            case 0xBDEA8F -> new Mapping(Blocks.LADDER.defaultBlockState(), MetadataKind.HORIZONTAL);
            case 0x4D12F4 -> mo("pattern_storage");
            case 0xF7D20B -> new Mapping(Blocks.OAK_SAPLING.defaultBlockState(), MetadataKind.SAPLING);
            case 0x854B38 -> new Mapping(Blocks.IRON_DOOR.defaultBlockState(), MetadataKind.DOOR);
            case 0xFF00FF -> vanilla(Blocks.AIR);
            default -> null;
        };
    }

    private static BlockState applyLegacyMetadata(BlockState state, int meta, MetadataKind kind) {
        if (meta == 0 || kind == MetadataKind.NONE) return state;
        return switch (kind) {
            case DYE_GLASS -> dyeGlass(meta).defaultBlockState();
            case DYE_CARPET -> dyeCarpet(meta).defaultBlockState();
            case TALL_GRASS -> (meta == 2 ? Blocks.FERN : Blocks.SHORT_GRASS).defaultBlockState();
            case FLOWER -> flower(meta).defaultBlockState();
            case SAPLING -> sapling(meta).defaultBlockState();
            case CROP -> state.hasProperty(CropBlock.AGE) ? state.setValue(CropBlock.AGE, Math.min(7, meta & 7)) : state;
            case HORIZONTAL -> horizontal(state, meta);
            case STAIRS -> stairs(state, meta);
            case DOOR -> door(state, meta);
            default -> state;
        };
    }

    private static BlockState horizontal(BlockState state, int meta) {
        if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return state;
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, legacyFacing(meta & 3));
    }

    private static BlockState stairs(BlockState state, int meta) {
        state = horizontal(state, meta);
        if (state.hasProperty(BlockStateProperties.HALF)) {
            state = state.setValue(BlockStateProperties.HALF, (meta & 4) != 0 ? Half.TOP : Half.BOTTOM);
        }
        return state;
    }

    private static BlockState door(BlockState state, int meta) {
        if ((meta & 8) != 0) return Blocks.IRON_DOOR.defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER);
        state = horizontal(state, meta);
        if (state.hasProperty(BlockStateProperties.OPEN)) state = state.setValue(BlockStateProperties.OPEN, (meta & 4) != 0);
        return state;
    }

    private static Direction legacyFacing(int meta) {
        return switch (meta & 3) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.WEST;
            case 2 -> Direction.SOUTH;
            default -> Direction.NORTH;
        };
    }

    private static Block dyeGlass(int meta) {
        return switch (meta & 15) {
            case 1 -> Blocks.ORANGE_STAINED_GLASS; case 2 -> Blocks.MAGENTA_STAINED_GLASS;
            case 3 -> Blocks.LIGHT_BLUE_STAINED_GLASS; case 4 -> Blocks.YELLOW_STAINED_GLASS;
            case 5 -> Blocks.LIME_STAINED_GLASS; case 6 -> Blocks.PINK_STAINED_GLASS;
            case 7 -> Blocks.GRAY_STAINED_GLASS; case 8 -> Blocks.LIGHT_GRAY_STAINED_GLASS;
            case 9 -> Blocks.CYAN_STAINED_GLASS; case 10 -> Blocks.PURPLE_STAINED_GLASS;
            case 11 -> Blocks.BLUE_STAINED_GLASS; case 12 -> Blocks.BROWN_STAINED_GLASS;
            case 13 -> Blocks.GREEN_STAINED_GLASS; case 14 -> Blocks.RED_STAINED_GLASS;
            case 15 -> Blocks.BLACK_STAINED_GLASS; default -> Blocks.WHITE_STAINED_GLASS;
        };
    }

    private static Block dyeCarpet(int meta) {
        return switch (meta & 15) {
            case 1 -> Blocks.ORANGE_CARPET; case 2 -> Blocks.MAGENTA_CARPET; case 3 -> Blocks.LIGHT_BLUE_CARPET;
            case 4 -> Blocks.YELLOW_CARPET; case 5 -> Blocks.LIME_CARPET; case 6 -> Blocks.PINK_CARPET;
            case 7 -> Blocks.GRAY_CARPET; case 8 -> Blocks.LIGHT_GRAY_CARPET; case 9 -> Blocks.CYAN_CARPET;
            case 10 -> Blocks.PURPLE_CARPET; case 11 -> Blocks.BLUE_CARPET; case 12 -> Blocks.BROWN_CARPET;
            case 13 -> Blocks.GREEN_CARPET; case 14 -> Blocks.RED_CARPET; case 15 -> Blocks.BLACK_CARPET;
            default -> Blocks.WHITE_CARPET;
        };
    }

    private static Block flower(int meta) {
        return switch (meta & 15) {
            case 1 -> Blocks.BLUE_ORCHID; case 2 -> Blocks.ALLIUM; case 3 -> Blocks.AZURE_BLUET;
            case 4 -> Blocks.RED_TULIP; case 5 -> Blocks.ORANGE_TULIP; case 6 -> Blocks.WHITE_TULIP;
            case 7 -> Blocks.PINK_TULIP; case 8 -> Blocks.OXEYE_DAISY; default -> Blocks.POPPY;
        };
    }

    private static Block sapling(int meta) {
        return switch (meta & 7) {
            case 1 -> Blocks.SPRUCE_SAPLING; case 2 -> Blocks.BIRCH_SAPLING; case 3 -> Blocks.JUNGLE_SAPLING;
            case 4 -> Blocks.ACACIA_SAPLING; case 5 -> Blocks.DARK_OAK_SAPLING; default -> Blocks.OAK_SAPLING;
        };
    }

    private static Mapping mo(String id) { return new Mapping(ModBlocks.get(id).get().defaultBlockState(), MetadataKind.NONE); }
    private static Mapping vanilla(Block block) { return new Mapping(block.defaultBlockState(), MetadataKind.NONE); }

    private static void fillCrate(WorldGenLevel level, BlockPos pos, RandomSource random,
                                  LegacyParityStructureFeature.Kind kind) {
        if (!(level.getBlockEntity(pos) instanceof TritaniumCrateBlockEntity crate)) return;
        switch (kind) {
            case CRASHED_SHIP -> {
                addLoot(crate, random, "tritanium_plate", 2, 6);
                addLoot(crate, random, "matter_dust", 2, 8);
                if (random.nextBoolean()) addLoot(crate, random, "battery", 1, 1);
                if (random.nextInt(4) == 0) addLoot(crate, random, "isolinear_circuit_mk1", 1, 2);
            }
            case CARGO_SHIP -> {
                addLoot(crate, random, "tritanium_ingot", 3, 10);
                addLoot(crate, random, "tritanium_plate", 2, 7);
                addLoot(crate, random, "dilithium_crystal", 1, 4);
                if (random.nextBoolean()) addLoot(crate, random, "machine_casing", 1, 2);
                if (random.nextInt(3) == 0) addLoot(crate, random, "upgrade_base", 1, 1);
            }
            case UNDERWATER_BASE -> {
                addLoot(crate, random, "matter_dust_refined", 3, 9);
                addLoot(crate, random, "dilithium_crystal", 2, 5);
                addLoot(crate, random, "isolinear_circuit_mk2", 1, 2);
                if (random.nextInt(3) == 0) addLoot(crate, random, "integration_matrix", 1, 1);
                if (random.nextInt(4) == 0) addLoot(crate, random, "pattern_drive", 1, 1);
            }
            default -> { return; }
        }
        crate.setChanged();
    }

    private static void addLoot(TritaniumCrateBlockEntity crate, RandomSource random, String id, int min, int max) {
        var item = ModItems.STANDALONE_ITEMS.get(id);
        if (item == null) return;
        ItemStack stack = new ItemStack(item.get(), min + (max > min ? random.nextInt(max - min + 1) : 0));
        for (int attempt = 0; attempt < TritaniumCrateBlockEntity.SLOT_COUNT; attempt++) {
            int slot = random.nextInt(TritaniumCrateBlockEntity.SLOT_COUNT);
            if (crate.getInventory().getStackInSlot(slot).isEmpty()) {
                crate.getInventory().setStackInSlot(slot, stack);
                return;
            }
        }
    }

    private enum MetadataKind { NONE, HORIZONTAL, STAIRS, DYE_GLASS, DYE_CARPET, TALL_GRASS, FLOWER, SAPLING, CROP, DOOR }
    private record Mapping(BlockState state, MetadataKind metadataKind) {}
    private record Template(BufferedImage image, int layerWidth, int layerHeight, int tilesX, int layers, boolean alphaMetadata) {}
}
