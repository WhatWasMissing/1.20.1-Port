package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;

/** Adds Matter Overdrive progression to vanilla exploration without requiring MO structures. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class VanillaLootEvents {
    private static final Map<ResourceLocation, List<String>> COMMON_LOOT = Map.of(
            id("chests/simple_dungeon"), List.of("battery", "matter_dust", "tritanium_nugget"),
            id("chests/abandoned_mineshaft"), List.of("matter_dust", "tritanium_plate", "isolinear_circuit_mk1"),
            id("chests/shipwreck_supply"), List.of("battery", "dilithium_crystal", "tritanium_nugget"),
            id("chests/desert_pyramid"), List.of("isolinear_circuit_mk1", "android_pill_blue"),
            id("chests/pillager_outpost"), List.of("battery", "weapon_module_holo_sights", "tritanium_ingot"),
            id("chests/nether_bridge"), List.of("dilithium_crystal", "isolinear_circuit_mk2", "battery"),
            id("chests/stronghold_corridor"), List.of("isolinear_circuit_mk2", "machine_casing", "pattern_drive"),
            id("chests/stronghold_crossing"), List.of("isolinear_circuit_mk2", "integration_matrix", "battery")
    );

    private static final Map<ResourceLocation, String> LEGENDARY_RELICS = Map.of(
            id("chests/stronghold_library"), "OVERCLOCKED_RELAY",
            id("chests/jungle_temple"), "SWARM_BEACON",
            id("chests/bastion_treasure"), "AEGIS_PRISM",
            id("chests/woodland_mansion"), "HUNTER_LENS",
            id("chests/buried_treasure"), "NANITE_CROWN",
            id("chests/end_city_treasure"), "CAPACITOR_HEART",
            id("chests/ancient_city"), "PHASE_ANCHOR"
    );

    private VanillaLootEvents() {}

    private static ResourceLocation id(String path) {
        return new ResourceLocation("minecraft", path);
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        List<String> commonItems = COMMON_LOOT.get(event.getName());
        if (commonItems != null) addCommonPool(event.getTable(), commonItems);
        String relicId = LEGENDARY_RELICS.get(event.getName());
        if (relicId != null) addRelicPool(event.getTable(), relicId);
    }

    private static void addCommonPool(LootTable table, List<String> itemIds) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(EmptyLootItem.emptyItem().setWeight(5));
        for (String itemId : itemIds) {
            pool.add(LootItem.lootTableItem(ModItems.get(itemId).get()).setWeight(1));
        }
        table.addPool(pool.build());
    }

    private static void addRelicPool(LootTable table, String relicId) {
        CompoundTag tag = new CompoundTag();
        tag.putString("RelicId", relicId);
        tag.putInt("CustomModelData", 1001 + switch (relicId) {
            case "OVERCLOCKED_RELAY" -> 0;
            case "SWARM_BEACON" -> 1;
            case "AEGIS_PRISM" -> 2;
            case "HUNTER_LENS" -> 3;
            case "NANITE_CROWN" -> 4;
            case "CAPACITOR_HEART" -> 5;
            case "PHASE_ANCHOR" -> 6;
            default -> 0;
        });
        table.addPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(ModItems.get("artifact").get())
                        .setWeight(1)
                        .apply(SetNbtFunction.setTag(tag)))
                .add(EmptyLootItem.emptyItem().setWeight(49))
                .build());
    }
}
