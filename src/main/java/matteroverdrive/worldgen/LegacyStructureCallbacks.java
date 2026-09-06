package matteroverdrive.worldgen;

import matteroverdrive.blockentity.HoloSignBlockEntity;
import matteroverdrive.blockentity.TritaniumCrateBlockEntity;
import matteroverdrive.blockentity.WeaponStationBlockEntity;
import matteroverdrive.item.ContractItem;
import matteroverdrive.quest.ContractStageSupport;
import matteroverdrive.quest.LegacyStoryContracts;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Source-backed post-generation callbacks that the old MOImageGen workers applied after PNG placement. */
public final class LegacyStructureCallbacks {
    private LegacyStructureCallbacks() {}

    public static void apply(WorldGenLevel level, BlockPos origin, RandomSource random,
                             LegacyParityStructureFeature.Kind kind) {
        switch (kind) {
            case CRASHED_SHIP -> crashedShip(level, origin, random);
            case CARGO_SHIP -> cargoShip(level, origin, random);
            case UNDERWATER_BASE -> underwaterBase(level, origin);
            default -> {
            }
        }
    }

    private static void crashedShip(WorldGenLevel level, BlockPos origin, RandomSource random) {
        Bounds bounds = bounds(origin, LegacyParityStructureFeature.Kind.CRASHED_SHIP);
        List<TritaniumCrateBlockEntity> crates = crates(level, bounds);

        // Legacy loot table: one roll, nugget weight 80, battery weight 10, empty weight 10.
        // The crash_landing quest stack was additionally placed into the crashed-ship crate.
        if (!crates.isEmpty()) {
            TritaniumCrateBlockEntity crate = crates.get(0);
            clear(crate);
            ItemStack quest = LegacyStoryContracts.create("crash_landing", random);
            ContractStageSupport.setQuestPosition(quest, origin, 0);
            putFirstFree(crate, quest);
            int roll = random.nextInt(100);
            if (roll < 80) putFirstFree(crate, new ItemStack(ModItems.get("tritanium_nugget").get()));
            else if (roll < 90) putFirstFree(crate, new ItemStack(ModItems.get("battery").get()));
            crate.setChanged();
        }

        // Two independent 30% checks in the original callback: 30% insurance,
        // then 30% of the remaining 70% keep-calm, otherwise blank.
        for (HoloSignBlockEntity sign : signs(level, bounds)) {
            if (random.nextFloat() < 0.30F) sign.setText("I hope my insurance covers this.");
            else if (random.nextFloat() < 0.30F) sign.setText("Keep calm and respawn.");
            else sign.setText("");
        }

        // Legacy callback: 10/200 (5%) chance for a decorated level-3 energy weapon.
        // Preserve the source weapon-type weights with the modern registered equivalents.
        for (WeaponStationBlockEntity station : weaponStations(level, bounds)) {
            if (random.nextInt(200) < 10 && station.getInventory().getStackInSlot(WeaponStationBlockEntity.WEAPON_SLOT).isEmpty()) {
                station.getInventory().setStackInSlot(WeaponStationBlockEntity.WEAPON_SLOT, sourceWeightedWeapon(random));
                station.setChanged();
            }
        }
    }

    private static void cargoShip(WorldGenLevel level, BlockPos origin, RandomSource random) {
        Bounds bounds = bounds(origin, LegacyParityStructureFeature.Kind.CARGO_SHIP);
        List<TritaniumCrateBlockEntity> red = cratesOf(level, bounds, "tritanium_crate_red");
        List<TritaniumCrateBlockEntity> lime = cratesOf(level, bounds, "tritanium_crate_lime");

        // MOWorldGenCargoShip marks the red crate as the quest position and inserts
        // the named Isolinear Circuit agreement there.
        BlockPos questPos = red.isEmpty() ? origin : red.get(0).getBlockPos();
        if (!red.isEmpty()) {
            ItemStack agreement = new ItemStack(ModItems.get("isolinear_circuit_mk1").get());
            agreement.setHoverName(Component.literal("Trade Route Agreement"));
            putFirstFree(red.get(0), agreement);
            red.get(0).setChanged();
        }

        // The worker remembers the first lime crate and inserts the generated trade_route contract onGeneration.
        if (!lime.isEmpty()) {
            ItemStack contract = LegacyStoryContracts.create("trade_route", random);
            ContractStageSupport.setQuestPosition(contract, questPos, 0);
            putFirstFree(lime.get(0), contract);
            lime.get(0).setChanged();
        }

        String destination = level.getLevel().dimension().location().getPath().replace('_', ' ');
        destination = destination.isBlank() ? "Unknown" : title(destination);
        String text = "<-- [Cargo Unit] -->\nCode: 02 - Corn\nStatus: Confirmed\nDestination:\nPlanet - " + destination;
        for (HoloSignBlockEntity sign : signs(level, bounds)) sign.setText(text);
    }

    private static void underwaterBase(WorldGenLevel level, BlockPos origin) {
        // The 1.12.2 population hook is empty. Image metadata already restores the
        // machine/door/ladder/crop state represented by the template. Keep this hook
        // explicit so future recovered worker callbacks have one source-backed home.
    }

    private static ItemStack sourceWeightedWeapon(RandomSource random) {
        // WeaponFactory.initWeapons weights in 1.12.2: Phaser Rifle 70, Omni Tool 30,
        // Plasma Shotgun 10, Ion Sniper 5. The station can physically retain any stack;
        // module packing remains active only for EnergyWeaponItem instances.
        int roll = random.nextInt(115);
        String id = roll < 70 ? "phaser_rifle" : roll < 100 ? "omni_tool" : roll < 110 ? "plasma_shotgun" : "ion_sniper";
        ItemStack weapon = new ItemStack(ModItems.get(id).get());
        weapon.getOrCreateTag().putInt("MatterOverdriveLootLevel", 3);
        weapon.getOrCreateTag().putBoolean("MatterOverdriveLegacyGenerated", true);
        return weapon;
    }

    private static List<TritaniumCrateBlockEntity> crates(WorldGenLevel level, Bounds bounds) {
        List<TritaniumCrateBlockEntity> result = new ArrayList<>();
        forEach(bounds, pos -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TritaniumCrateBlockEntity crate) result.add(crate);
        });
        result.sort(Comparator.comparingLong(crate -> crate.getBlockPos().asLong()));
        return result;
    }

    private static List<TritaniumCrateBlockEntity> cratesOf(WorldGenLevel level, Bounds bounds, String id) {
        List<TritaniumCrateBlockEntity> result = new ArrayList<>();
        var block = ModBlocks.get(id).get();
        forEach(bounds, pos -> {
            if (!level.getBlockState(pos).is(block)) return;
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TritaniumCrateBlockEntity crate) result.add(crate);
        });
        result.sort(Comparator.comparingLong(crate -> crate.getBlockPos().asLong()));
        return result;
    }

    private static List<HoloSignBlockEntity> signs(WorldGenLevel level, Bounds bounds) {
        List<HoloSignBlockEntity> result = new ArrayList<>();
        forEach(bounds, pos -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof HoloSignBlockEntity sign) result.add(sign);
        });
        return result;
    }

    private static List<WeaponStationBlockEntity> weaponStations(WorldGenLevel level, Bounds bounds) {
        List<WeaponStationBlockEntity> result = new ArrayList<>();
        forEach(bounds, pos -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof WeaponStationBlockEntity station) result.add(station);
        });
        return result;
    }

    private static void clear(TritaniumCrateBlockEntity crate) {
        for (int slot = 0; slot < crate.getInventory().getSlots(); slot++) {
            crate.getInventory().setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    private static void putFirstFree(TritaniumCrateBlockEntity crate, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        for (int slot = 0; slot < crate.getInventory().getSlots(); slot++) {
            if (crate.getInventory().getStackInSlot(slot).isEmpty()) {
                crate.getInventory().setStackInSlot(slot, stack.copy());
                return;
            }
        }
    }

    private static Bounds bounds(BlockPos origin, LegacyParityStructureFeature.Kind kind) {
        return switch (kind) {
            case CRASHED_SHIP -> new Bounds(origin.offset(-6, -2, -18), origin.offset(6, 16, 18));
            case CARGO_SHIP -> new Bounds(origin.offset(-30, -2, -12), origin.offset(30, 24, 12));
            case UNDERWATER_BASE -> new Bounds(origin.offset(-22, -2, -22), origin.offset(22, 24, 22));
            default -> new Bounds(origin, origin);
        };
    }

    private static void forEach(Bounds bounds, java.util.function.Consumer<BlockPos> consumer) {
        BlockPos.betweenClosedStream(bounds.min, bounds.max).forEach(p -> consumer.accept(p.immutable()));
    }

    private static String title(String value) {
        StringBuilder out = new StringBuilder();
        boolean upper = true;
        for (char c : value.toCharArray()) {
            if (c == ' ') { out.append(c); upper = true; }
            else { out.append(upper ? Character.toUpperCase(c) : c); upper = false; }
        }
        return out.toString();
    }

    private record Bounds(BlockPos min, BlockPos max) {}
}
