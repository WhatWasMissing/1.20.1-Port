package matteroverdrive.worldgen;

import com.mojang.serialization.Codec;
import matteroverdrive.blockentity.TritaniumCrateBlockEntity;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModEntities;
import matteroverdrive.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Source-driven translation of the legacy MOImageGen/world building footprints.
 * The original mod generated these from PNG templates; this 1.20.1 translation
 * keeps their recovered logical dimensions, offsets, machine roles and population.
 */
public final class LegacyParityStructureFeature extends Feature<NoneFeatureConfiguration> {
    public enum Kind { CRASHED_SHIP, CARGO_SHIP, UNDERWATER_BASE, MAD_SCIENTIST_HOUSE, ANDROID_HOUSE, SAND_PIT }
    private enum LootProfile { CRASHED_SHIP, CARGO_SHIP, UNDERWATER_BASE, SCIENTIST, ANDROID, SAND_PIT }

    private final Kind kind;

    public LegacyParityStructureFeature(Codec<NoneFeatureConfiguration> codec, Kind kind) {
        super(codec);
        this.kind = kind;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return switch (kind) {
            case CRASHED_SHIP -> crashedShip(context.level(), context.origin(), context.random());
            case CARGO_SHIP -> cargoShip(context.level(), context.origin(), context.random());
            case UNDERWATER_BASE -> underwaterBase(context.level(), context.origin(), context.random());
            case MAD_SCIENTIST_HOUSE -> madScientistHouse(context.level(), context.origin(), context.random());
            case ANDROID_HOUSE -> androidHouse(context.level(), context.origin(), context.random());
            case SAND_PIT -> sandPit(context.level(), context.origin(), context.random());
        };
    }

    /** Legacy template: 11 x 35. */
    private static boolean crashedShip(WorldGenLevel level, BlockPos origin, RandomSource random) {
        if (!level.getFluidState(origin).isEmpty()) return false;
        BlockPos base = origin.below();
        BlockState hull = block("decorative.tritanium_plate");
        BlockState stripe = block("decorative.tritanium_plate_stripe");
        BlockState glass = block("industrial_glass");
        BlockState floor = block("decorative.floor_tiles");

        for (int z = -17; z <= 17; z++) {
            int nose = Math.min(z + 17, 17 - z);
            int half = Math.max(1, Math.min(5, 1 + nose / 3));
            for (int x = -half; x <= half; x++) {
                if (z > 8 && Math.floorMod(x * 31 + z * 17, 13) < 2) continue;
                set(level, base.offset(x, 0, z), floor);
                if (Math.abs(x) == half) {
                    set(level, base.offset(x, 1, z), hull);
                    if (Math.abs(z) < 12 && z % 3 != 0) set(level, base.offset(x, 2, z), hull);
                }
            }
        }
        for (int z = -12; z <= 9; z += 4) {
            set(level, base.offset(-5, 1, z), stripe);
            set(level, base.offset(5, 1, z), stripe);
        }
        for (int x = -2; x <= 2; x++) set(level, base.offset(x, 2, -11), glass);
        placeCrate(level, base.offset(0, 1, 4), "tritanium_crate", random, LootProfile.CRASHED_SHIP);
        set(level, base.offset(2, 1, -4), block("holo_sign"));
        for (int i = 0; i < 7; i++) {
            set(level, base.offset(random.nextInt(11) - 5, -1, random.nextInt(35) - 17), Blocks.COARSE_DIRT.defaultBlockState());
        }
        int defenders = 1 + random.nextInt(2);
        for (int i = 0; i < defenders; i++) spawnAndroid(level, base.offset((i * 3) - 1, 1, -3 + i * 5), random);
        return true;
    }

    /** Legacy template: 58 x 23. */
    private static boolean cargoShip(WorldGenLevel level, BlockPos origin, RandomSource random) {
        if (!level.getFluidState(origin).isEmpty()) return false;
        BlockPos base = origin.above();
        BlockState hull = block("decorative.tritanium_plate");
        BlockState stripe = block("decorative.tritanium_plate_stripe");
        BlockState floor = block("decorative.floor_tiles");
        BlockState glass = block("industrial_glass");

        for (int x = -29; x <= 28; x++) {
            int end = Math.min(x + 29, 28 - x);
            int halfZ = Math.max(2, Math.min(11, 2 + end / 4));
            for (int z = -halfZ; z <= halfZ; z++) {
                set(level, base.offset(x, 0, z), floor);
                if (Math.abs(z) == halfZ) {
                    set(level, base.offset(x, 1, z), hull);
                    if ((x & 1) == 0) set(level, base.offset(x, 2, z), hull);
                }
            }
        }
        for (int x = -20; x <= 18; x++) {
            set(level, base.offset(x, 3, -4), hull);
            set(level, base.offset(x, 3, 4), hull);
            if ((x & 1) == 0) {
                set(level, base.offset(x, 2, -4), glass);
                set(level, base.offset(x, 2, 4), glass);
            }
        }
        for (int x = -22; x <= 20; x += 7) {
            set(level, base.offset(x, 1, 0), block("decorative.tritanium_lamp"));
            if (random.nextBoolean()) placeCrate(level, base.offset(x, 1, 3), "tritanium_crate", random, LootProfile.CARGO_SHIP);
        }
        for (int z = -6; z <= 6; z++) {
            set(level, base.offset(-23, 1, z), stripe);
            if ((z & 1) == 0) set(level, base.offset(20, 1, z), stripe);
        }
        set(level, base.offset(-25, 1, 0), block("holo_sign"));
        set(level, base.offset(10, 1, 0), block("transporter"));
        set(level, base.offset(12, 1, 0), block("network_switch"));
        set(level, base.offset(14, 1, 0), block("network_pipe"));

        int defenders = 2 + random.nextInt(3);
        for (int i = 0; i < defenders; i++) spawnAndroid(level, base.offset(-10 + i * 7, 1, i % 2 == 0 ? -3 : 3), random);
        if (random.nextInt(3) == 0) spawnMob(level, base.offset(4, 2, 0), ModEntities.DRONE.get(), random);
        return true;
    }

    /** Legacy template: 43 x 43, deep-ocean installation. */
    private static boolean underwaterBase(WorldGenLevel level, BlockPos origin, RandomSource random) {
        if (level.getFluidState(origin.above(8)).isEmpty()) return false;
        BlockPos c = origin.above();
        final int r = 21;
        BlockState hull = block("decorative.tritanium_plate");
        BlockState glass = block("industrial_glass");
        BlockState floor = block("decorative.floor_tiles");

        for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) {
            double d = Math.sqrt(x * x + z * z);
            if (d <= 19.5D) {
                set(level, c.offset(x, 0, z), floor);
                for (int y = 1; y <= 5; y++) set(level, c.offset(x, y, z), Blocks.AIR.defaultBlockState());
            }
            if (d >= 19.3D && d <= 21.1D) {
                for (int y = 1; y <= 5; y++) set(level, c.offset(x, y, z), y == 2 || y == 3 ? glass : hull);
            }
            if (d <= 18.5D) set(level, c.offset(x, 6, z), d > 14.0D ? glass : hull);
        }
        // Four legacy-like radial rooms/corridors inside the 43x43 envelope.
        for (int x = -15; x <= 15; x++) {
            for (int z = -2; z <= 2; z++) set(level, c.offset(x, 1, z), Blocks.AIR.defaultBlockState());
        }
        for (int z = -15; z <= 15; z++) {
            for (int x = -2; x <= 2; x++) set(level, c.offset(x, 1, z), Blocks.AIR.defaultBlockState());
        }
        set(level, c.offset(0, 1, 0), block("matter_analyzer"));
        set(level, c.offset(5, 1, 0), block("pattern_storage"));
        set(level, c.offset(-5, 1, 0), block("pattern_monitor"));
        placeCrate(level, c.offset(8, 1, 5), "tritanium_crate_blue", random, LootProfile.UNDERWATER_BASE);
        placeCrate(level, c.offset(-8, 1, -5), "tritanium_crate", random, LootProfile.UNDERWATER_BASE);

        spawnMob(level, c.offset(-4, 1, 0), ModEntities.DRONE.get(), random);
        if (random.nextBoolean()) spawnMob(level, c.offset(0, 1, 6), ModEntities.RANGED_ROGUE_ANDROID.get(), random);
        return true;
    }

    /** Legacy village piece is approximately 9 x 9 x 6. */
    private static boolean madScientistHouse(WorldGenLevel level, BlockPos origin, RandomSource random) {
        if (!level.getFluidState(origin).isEmpty()) return false;
        BlockState plate = block("decorative.white_plate");
        BlockState floor = block("decorative.floor_tile_white");
        BlockState glass = block("industrial_glass");
        BlockState beam = block("decorative.beams");
        for (int x = -4; x <= 4; x++) for (int z = -4; z <= 4; z++) {
            set(level, origin.offset(x, 0, z), floor);
            if (Math.abs(x) == 4 || Math.abs(z) == 4) {
                for (int y = 1; y <= 4; y++) {
                    boolean door = z == -4 && Math.abs(x) <= 1 && y <= 2;
                    boolean window = y >= 2 && y <= 3 && !door && ((Math.abs(x) == 4 && Math.abs(z) <= 1) || (Math.abs(z) == 4 && Math.abs(x) <= 1));
                    set(level, origin.offset(x, y, z), door ? Blocks.AIR.defaultBlockState() : window ? glass : plate);
                }
            } else {
                for (int y = 1; y <= 4; y++) set(level, origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
            }
            set(level, origin.offset(x, 5, z), plate);
        }
        for (int y = 1; y <= 4; y++) for (int sx : new int[]{-3, 3}) for (int sz : new int[]{-3, 3}) set(level, origin.offset(sx, y, sz), beam);
        set(level, origin.offset(0, 1, 2), block("inscriber"));
        set(level, origin.offset(2, 1, 2), block("decomposer"));
        placeCrate(level, origin.offset(-2, 1, 2), "tritanium_crate", random, LootProfile.SCIENTIST);
        spawnMob(level, origin.offset(0, 1, -1), ModEntities.MAD_SCIENTIST.get(), random);
        EntityType<? extends Mob> failed = switch (random.nextInt(4)) {
            case 0 -> ModEntities.FAILED_COW.get();
            case 1 -> ModEntities.FAILED_PIG.get();
            case 2 -> ModEntities.FAILED_SHEEP.get();
            default -> ModEntities.FAILED_CHICKEN.get();
        };
        if (random.nextBoolean()) spawnMob(level, origin.offset(3, 1, -2), failed, random);
        return true;
    }

    /** Legacy MOAndroidHouseBuilding: 21 x 21 with yOffset -2. */
    private static boolean androidHouse(WorldGenLevel level, BlockPos origin, RandomSource random) {
        if (!level.getFluidState(origin).isEmpty() || level.getBlockState(origin.below()).isAir()) return false;
        BlockPos base = origin.below(2);
        BlockState hull = block("decorative.tritanium_plate");
        BlockState white = block("decorative.white_plate");
        BlockState floor = block("decorative.floor_tiles");
        BlockState green = block("decorative.floor_tiles_green");
        BlockState glass = block("industrial_glass");
        BlockState beam = block("decorative.beams");
        BlockState vent = block("decorative.vent.dark");
        BlockState holo = block("decorative.holo_matrix");

        for (int x = -10; x <= 10; x++) for (int z = -10; z <= 10; z++) {
            boolean edge = Math.abs(x) == 10 || Math.abs(z) == 10;
            boolean inner = Math.abs(x) <= 8 && Math.abs(z) <= 8;
            set(level, base.offset(x, 0, z), inner && ((x + z) & 3) == 0 ? green : floor);
            if (edge) {
                for (int y = 1; y <= 5; y++) {
                    boolean door = z == -10 && Math.abs(x) <= 1 && y <= 3;
                    boolean window = y >= 2 && y <= 3 && ((Math.abs(x) == 10 && Math.abs(z) <= 5) || (Math.abs(z) == 10 && Math.abs(x) >= 3 && Math.abs(x) <= 7));
                    set(level, base.offset(x, y, z), door ? Blocks.AIR.defaultBlockState() : window ? glass : (((x + z + y) & 1) == 0 ? hull : white));
                }
            } else {
                for (int y = 1; y <= 5; y++) set(level, base.offset(x, y, z), Blocks.AIR.defaultBlockState());
            }
            set(level, base.offset(x, 6, z), ((Math.abs(x) + Math.abs(z)) % 6 == 0) ? white : hull);
        }
        for (int y = 1; y <= 5; y++) for (int sx : new int[]{-9, 9}) for (int sz : new int[]{-9, 9}) set(level, base.offset(sx, y, sz), beam);
        for (int x = -5; x <= 5; x++) {
            set(level, base.offset(x, 1, 4), white);
            if (Math.abs(x) > 1) set(level, base.offset(x, 2, 4), glass);
        }
        for (int z = -6; z <= 6; z += 3) set(level, base.offset(0, 5, z), z % 6 == 0 ? holo : vent);

        // Recovered legacy machine palette/roles from MOAndroidHouseBuilding.
        set(level, base.offset(-6, 1, -6), block("star_map"));
        set(level, base.offset(-3, 1, -6), block("replicator"));
        set(level, base.offset(0, 1, -6), block("network_switch"));
        set(level, base.offset(1, 1, -6), block("network_pipe"));
        set(level, base.offset(3, 1, -6), block("charging_station"));
        set(level, base.offset(6, 1, -6), block("pattern_monitor"));
        set(level, base.offset(-6, 1, 6), random.nextBoolean() ? block("android_station") : block("weapon_station"));
        placeCrate(level, base.offset(-3, 1, 6), "tritanium_crate_blue", random, LootProfile.ANDROID);
        placeCrate(level, base.offset(3, 1, 6), "tritanium_crate", random, LootProfile.ANDROID);
        set(level, base.offset(6, 1, 6), block("holo_sign"));

        int defenders = 3 + random.nextInt(2);
        BlockPos[] positions = {base.offset(-5, 1, 0), base.offset(5, 1, 0), base.offset(-2, 1, -4), base.offset(2, 1, -4)};
        for (int i = 0; i < defenders; i++) spawnAndroid(level, positions[i], random);
        if (random.nextInt(4) == 0) spawnMob(level, base.offset(0, 2, 2), ModEntities.DRONE.get(), random);
        return true;
    }

    /** Legacy MOSandPit: 24 x 24, yOffset -9, airLeeway 3. */
    private static boolean sandPit(WorldGenLevel level, BlockPos origin, RandomSource random) {
        if (!level.getFluidState(origin).isEmpty()) return false;
        if (!sandSupportNear(level, origin.offset(-12, -1, -12), 3)
                || !sandSupportNear(level, origin.offset(11, -1, -12), 3)
                || !sandSupportNear(level, origin.offset(-12, -1, 11), 3)
                || !sandSupportNear(level, origin.offset(11, -1, 11), 3)) return false;

        BlockPos floor = origin.below(9);
        BlockState sand = Blocks.SAND.defaultBlockState();
        BlockState sandstone = Blocks.SANDSTONE.defaultBlockState();
        BlockState cut = Blocks.CUT_SANDSTONE.defaultBlockState();
        BlockState wreck = block("decorative.tritanium_plate");
        BlockState coils = block("decorative.coils");

        for (int x = -12; x <= 11; x++) for (int z = -12; z <= 11; z++) {
            double nx = (x + 0.5D) / 12.0D;
            double nz = (z + 0.5D) / 12.0D;
            double d = Math.sqrt(nx * nx + nz * nz);
            int depth = d < .30D ? 9 : d < .50D ? 7 : d < .70D ? 5 : d < .88D ? 3 : d <= 1.0D ? 1 : 0;
            BlockPos surface = origin.offset(x, -1, z);
            if (depth == 0) continue;
            for (int y = 0; y < depth; y++) set(level, surface.below(y), Blocks.AIR.defaultBlockState());
            set(level, surface.below(depth), d < .55D ? cut : sandstone);
            if (d > .70D && random.nextInt(6) == 0) set(level, surface.below(Math.max(0, depth - 1)), sand);
        }

        // Buried machine wreck at the legacy -9 offset.
        for (int z = -5; z <= 5; z++) {
            int half = Math.max(1, 4 - Math.abs(z) / 2);
            for (int x = -half; x <= half; x++) if (random.nextInt(7) != 0) set(level, floor.offset(x, 0, z), wreck);
        }
        set(level, floor.offset(-3, 1, 0), coils);
        set(level, floor.offset(3, 1, 0), coils);
        set(level, floor.offset(0, 1, -3), Blocks.WATER.defaultBlockState());
        placeCrate(level, floor.offset(0, 1, 2), "tritanium_crate", random, LootProfile.SAND_PIT);
        spawnAndroid(level, floor.offset(4, 1, 2), random);
        if (random.nextInt(5) == 0) spawnMob(level, floor.offset(-4, 2, -1), ModEntities.DRONE.get(), random);
        return true;
    }

    private static boolean sandSupportNear(WorldGenLevel level, BlockPos pos, int leeway) {
        for (int dy = -leeway; dy <= leeway; dy++) if (level.getBlockState(pos.offset(0, dy, 0)).is(Blocks.SAND)) return true;
        return false;
    }

    private static void spawnAndroid(WorldGenLevel level, BlockPos pos, RandomSource random) {
        EntityType<? extends Mob> type = random.nextInt(10) < 4 ? ModEntities.ROGUE_ANDROID.get() : ModEntities.RANGED_ROGUE_ANDROID.get();
        spawnMob(level, pos, type, random);
    }

    private static void spawnMob(WorldGenLevel level, BlockPos pos, EntityType<? extends Mob> type, RandomSource random) {
        if (!level.getWorldBorder().isWithinBounds(pos)) return;
        Mob mob = type.create(level.getLevel());
        if (mob == null) return;
        mob.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
        if (!level.noCollision(mob)) {
            mob.discard();
            return;
        }
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
        mob.setPersistenceRequired();
        level.addFreshEntity(mob);
    }

    private static void placeCrate(WorldGenLevel level, BlockPos pos, String blockId, RandomSource random, LootProfile profile) {
        set(level, pos, block(blockId));
        if (!(level.getBlockEntity(pos) instanceof TritaniumCrateBlockEntity crate)) return;
        switch (profile) {
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
            case SCIENTIST -> {
                addLoot(crate, random, "isolinear_circuit_mk1", 1, 3);
                addLoot(crate, random, "machine_casing", 1, 3);
                addLoot(crate, random, "matter_dust_refined", 2, 6);
                if (random.nextBoolean()) addLoot(crate, random, "integration_matrix", 1, 1);
                if (random.nextInt(5) == 0) addLoot(crate, random, "artifact", 1, 1);
            }
            case ANDROID -> {
                addLoot(crate, random, "battery", 1, 2);
                addLoot(crate, random, "isolinear_circuit_mk2", 1, 3);
                String part = switch (random.nextInt(4)) {
                    case 0 -> "rogue_android_part_head";
                    case 1 -> "rogue_android_part_chest";
                    case 2 -> "rogue_android_part_arms";
                    default -> "rogue_android_part_legs";
                };
                addLoot(crate, random, part, 1, 1);
                if (random.nextInt(3) == 0) addLoot(crate, random, "android_pill_blue", 1, 1);
                if (random.nextInt(5) == 0) addLoot(crate, random, "network_flash_drive", 1, 1);
            }
            case SAND_PIT -> {
                addLoot(crate, random, "tritanium_nugget", 4, 14);
                addLoot(crate, random, "tritanium_ingot", 1, 4);
                addLoot(crate, random, "matter_dust", 2, 7);
                if (random.nextInt(4) == 0) addLoot(crate, random, "dilithium_crystal", 1, 2);
                if (random.nextInt(12) == 0) addLoot(crate, random, "artifact", 1, 1);
            }
        }
        crate.setChanged();
    }

    private static void addLoot(TritaniumCrateBlockEntity crate, RandomSource random, String itemId, int min, int max) {
        var registered = ModItems.STANDALONE_ITEMS.get(itemId);
        if (registered == null) return;
        int count = min + (max > min ? random.nextInt(max - min + 1) : 0);
        ItemStack stack = new ItemStack(registered.get(), count);
        for (int attempt = 0; attempt < TritaniumCrateBlockEntity.SLOT_COUNT; attempt++) {
            int slot = random.nextInt(TritaniumCrateBlockEntity.SLOT_COUNT);
            if (crate.getInventory().getStackInSlot(slot).isEmpty()) {
                crate.getInventory().setStackInSlot(slot, stack);
                return;
            }
        }
    }

    private static BlockState block(String id) { return ModBlocks.get(id).get().defaultBlockState(); }
    private static void set(WorldGenLevel level, BlockPos pos, BlockState state) { level.setBlock(pos, state, 2); }
}
