package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.CreativeBatteryItem;
import matteroverdrive.item.MatterContainerItem;
import matteroverdrive.item.MatterDustItem;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.item.PatternDriveItem;
import matteroverdrive.item.ReactorRemoteItem;
import matteroverdrive.item.ReactorAssemblyGuideItem;
import matteroverdrive.item.TransportFlashDriveItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MatterOverdrive.MOD_ID);

    private static final Map<String, RegistryObject<Item>> BLOCK_ITEMS_MUTABLE = new LinkedHashMap<>();
    private static final Map<String, RegistryObject<Item>> STANDALONE_ITEMS_MUTABLE = new LinkedHashMap<>();

    public static final Map<String, RegistryObject<Item>> BLOCK_ITEMS =
            Collections.unmodifiableMap(BLOCK_ITEMS_MUTABLE);
    public static final Map<String, RegistryObject<Item>> STANDALONE_ITEMS =
            Collections.unmodifiableMap(STANDALONE_ITEMS_MUTABLE);

    private static final List<String> STANDALONE_ITEM_IDS = List.of(
        "android_pill_blue",
        "android_pill_red",
        "android_pill_yellow",
        "artifact",
        "battery",
        "contract",
        "creative_battery",
        "creative_pattern_drive",
        "data_pad",
        "dilithium_crystal",
        "earl_gray_tea",
        "emergency_ration",
        "energy_pack",
        "flash_drive",
        "forcefield_emitter",
        "h_compensator",
        "hc_battery",
        "integration_matrix",
        "ion_sniper",
        "isolinear_circuit_mk1",
        "isolinear_circuit_mk2",
        "isolinear_circuit_mk3",
        "isolinear_circuit_mk4",
        "machine_casing",
        "matter_container",
        "matter_dust",
        "matter_dust_refined",
        "matter_scanner",
        "me_conversion_matrix",
        "network_flash_drive",
        "omni_tool",
        "pattern_drive",
        "phaser",
        "phaser_rifle",
        "plasma_core",
        "plasma_shotgun",
        "portable_decomposer",
        "quantum_fold_manipulator",
        "record_transformation",
        "reactor_remote",
        "reactor_assembly_guide",
        "rogue_android_part_arms",
        "rogue_android_part_chest",
        "rogue_android_part_head",
        "rogue_android_part_legs",
        "romulan_ale",
        "s_magnet",
        "security_protocol_access",
        "security_protocol_claim",
        "security_protocol_empty",
        "security_protocol_remove",
        "sniper_scope",
        "spacetime_equalizer",
        "transport_flash_drive",
        "trilithium_crystal",
        "tritanium_axe",
        "tritanium_boots",
        "tritanium_chestplate",
        "tritanium_dust",
        "tritanium_helmet",
        "tritanium_hoe",
        "tritanium_ingot",
        "tritanium_leggings",
        "tritanium_nugget",
        "tritanium_pickaxe",
        "tritanium_plate",
        "tritanium_shovel",
        "tritanium_spine",
        "tritanium_sword",
        "tritanium_wrench",
        "upgrade_base",
        "upgrade_failsafe",
        "upgrade_hyper_speed",
        "upgrade_matter_storage",
        "upgrade_power",
        "upgrade_power_storage",
        "upgrade_range",
        "upgrade_speed",
        "weapon_handle",
        "weapon_module_barrel_block",
        "weapon_module_barrel_damage",
        "weapon_module_barrel_doomsday",
        "weapon_module_barrel_explosion",
        "weapon_module_barrel_fire",
        "weapon_module_barrel_heal",
        "weapon_module_color",
        "weapon_module_color_black",
        "weapon_module_color_blue",
        "weapon_module_color_brown",
        "weapon_module_color_gold",
        "weapon_module_color_gray",
        "weapon_module_color_green",
        "weapon_module_color_lime_green",
        "weapon_module_color_pink",
        "weapon_module_color_red",
        "weapon_module_color_sky_blue",
        "weapon_module_holo_sights",
        "weapon_module_ricochet",
        "weapon_receiver"
    );

    static {
        ModBlocks.all().forEach((id, block) -> {
            if (!ModBlocks.NO_BLOCK_ITEM.contains(id)) {
                BLOCK_ITEMS_MUTABLE.put(id,
                        ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties())));
            }
        });

        STANDALONE_ITEM_IDS.forEach(id ->
                STANDALONE_ITEMS_MUTABLE.put(id,
                        ITEMS.register(id, () -> createStandaloneItem(id))));
    }

    private ModItems() {
    }

    private static Item createStandaloneItem(String id) {
        if (id.equals("creative_battery")) {
            return new CreativeBatteryItem(propertiesFor(id));
        }
        if (id.equals("matter_container")) {
            return new MatterContainerItem(propertiesFor(id));
        }
        if (id.equals("matter_dust")) {
            return new MatterDustItem(propertiesFor(id), false);
        }
        if (id.equals("matter_dust_refined")) {
            return new MatterDustItem(propertiesFor(id), true);
        }
        if (id.equals("transport_flash_drive")) {
            return new TransportFlashDriveItem(propertiesFor(id));
        }
        if (id.equals("reactor_remote")) {
            return new ReactorRemoteItem(propertiesFor(id));
        }
        if (id.equals("reactor_assembly_guide")) {
            return new ReactorAssemblyGuideItem(propertiesFor(id));
        }
        if (id.equals("pattern_drive") || id.equals("creative_pattern_drive")) {
            return new PatternDriveItem(propertiesFor(id), id.equals("creative_pattern_drive"));
        }
        MachineUpgradeItem.Upgrade upgrade = MachineUpgradeItem.Upgrade.fromItemId(id);
        if (upgrade != null) {
            return new MachineUpgradeItem(propertiesFor(id), upgrade);
        }
        return new Item(propertiesFor(id));
    }

    private static Item.Properties propertiesFor(String id) {
        Item.Properties properties = new Item.Properties();
        if (isNonStacking(id)) {
            properties = properties.stacksTo(1);
        }
        return properties;
    }

    private static boolean isNonStacking(String id) {
        return id.contains("phaser")
                || id.contains("sniper")
                || id.contains("shotgun")
                || id.equals("omni_tool")
                || id.equals("matter_scanner")
                || id.equals("portable_decomposer")
                || id.contains("wrench")
                || id.contains("pickaxe")
                || id.contains("axe")
                || id.contains("shovel")
                || id.contains("hoe")
                || id.contains("sword")
                || id.contains("helmet")
                || id.contains("chestplate")
                || id.contains("leggings")
                || id.contains("boots")
                || id.contains("battery")
                || id.contains("pattern_drive")
                || id.contains("flash_drive")
                || id.equals("data_pad")
                || id.equals("spacetime_equalizer")
                || id.equals("energy_pack")
                || id.equals("reactor_remote")
                || id.equals("reactor_assembly_guide");
    }

    public static RegistryObject<Item> get(String id) {
        RegistryObject<Item> item = STANDALONE_ITEMS_MUTABLE.get(id);
        if (item == null) {
            item = BLOCK_ITEMS_MUTABLE.get(id);
        }
        if (item == null) {
            throw new IllegalArgumentException("Unknown Matter Overdrive item id: " + id);
        }
        return item;
    }
}

