package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.CreativeBatteryItem;
import matteroverdrive.item.ContractItem;
import matteroverdrive.item.AndroidPillItem;
import matteroverdrive.item.AndroidPartItem;
import matteroverdrive.android.AndroidData;
import matteroverdrive.item.DebugMatterContainerItem;
import matteroverdrive.item.DataPadItem;
import matteroverdrive.item.DocumentationItem;
import matteroverdrive.item.MatterScannerItem;
import matteroverdrive.item.PortableDecomposerItem;
import matteroverdrive.item.MatterContainerItem;
import matteroverdrive.item.MatterDustItem;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.item.PatternDriveItem;
import matteroverdrive.item.ReactorRemoteItem;
import matteroverdrive.item.ReactorAssemblyGuideItem;
import matteroverdrive.item.SpacetimeEqualizerItem;
import matteroverdrive.item.TransportFlashDriveItem;
import matteroverdrive.item.TritaniumArmorItem;
import matteroverdrive.item.TritaniumArmorMaterial;
import matteroverdrive.item.TritaniumToolTier;
import matteroverdrive.item.TritaniumWrenchItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import matteroverdrive.item.weapon.EnergyPackItem;
import matteroverdrive.item.weapon.EnergyWeaponItem;
import matteroverdrive.item.weapon.WeaponBatteryItem;
import matteroverdrive.item.weapon.WeaponModuleItem;

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
        "debug_matter_container",
        "creative_pattern_drive",
        "data_pad",
        "m2_testing_checklist",
        "current_features_reference",
        "system_guide",
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
        if (id.equals("contract")) return new ContractItem(propertiesFor(id));
        if (id.equals("android_pill_blue")) return new AndroidPillItem(propertiesFor(id), AndroidPillItem.Type.BLUE);
        if (id.equals("android_pill_red")) return new AndroidPillItem(propertiesFor(id), AndroidPillItem.Type.RED);
        if (id.equals("android_pill_yellow")) return new AndroidPillItem(propertiesFor(id), AndroidPillItem.Type.YELLOW);
        if (id.equals("rogue_android_part_head")) return new AndroidPartItem(propertiesFor(id), AndroidData.Part.HEAD);
        if (id.equals("rogue_android_part_chest")) return new AndroidPartItem(propertiesFor(id), AndroidData.Part.CHEST);
        if (id.equals("rogue_android_part_arms")) return new AndroidPartItem(propertiesFor(id), AndroidData.Part.ARMS);
        if (id.equals("rogue_android_part_legs")) return new AndroidPartItem(propertiesFor(id), AndroidData.Part.LEGS);
        if (id.equals("battery")) return new WeaponBatteryItem(propertiesFor(id), 524288, 400, 800);
        if (id.equals("hc_battery")) return new WeaponBatteryItem(propertiesFor(id), 1048576, 4096, 4096);
        if (id.equals("energy_pack")) return new EnergyPackItem(propertiesFor(id));
        if (id.equals("phaser")) return new EnergyWeaponItem(propertiesFor(id), EnergyWeaponItem.WeaponType.PHASER);
        if (id.equals("phaser_rifle")) return new EnergyWeaponItem(propertiesFor(id), EnergyWeaponItem.WeaponType.PHASER_RIFLE);
        if (id.equals("ion_sniper")) return new EnergyWeaponItem(propertiesFor(id), EnergyWeaponItem.WeaponType.ION_SNIPER);
        if (id.equals("plasma_shotgun")) return new EnergyWeaponItem(propertiesFor(id), EnergyWeaponItem.WeaponType.PLASMA_SHOTGUN);
        if (id.startsWith("weapon_module_barrel_")) return new WeaponModuleItem(propertiesFor(id), WeaponModuleItem.SlotType.BARREL, WeaponModuleItem.Effect.valueOf(id.substring(21).toUpperCase()));
        if (id.equals("weapon_module_holo_sights")) return new WeaponModuleItem(propertiesFor(id), WeaponModuleItem.SlotType.SIGHTS, WeaponModuleItem.Effect.HOLO_SIGHTS);
        if (id.equals("sniper_scope")) return new WeaponModuleItem(propertiesFor(id), WeaponModuleItem.SlotType.SIGHTS, WeaponModuleItem.Effect.SNIPER_SCOPE);
        if (id.equals("weapon_module_ricochet")) return new WeaponModuleItem(propertiesFor(id), WeaponModuleItem.SlotType.OTHER, WeaponModuleItem.Effect.RICOCHET);
        if (id.equals("weapon_module_color") || id.startsWith("weapon_module_color_")) return new WeaponModuleItem(propertiesFor(id), WeaponModuleItem.SlotType.COLOR, WeaponModuleItem.Effect.COLOR, colorFor(id));
        if (id.equals("creative_battery")) {
            return new CreativeBatteryItem(propertiesFor(id));
        }
        if (id.equals("debug_matter_container")) {
            return new DebugMatterContainerItem(propertiesFor(id));
        }
        if (id.equals("matter_container")) {
            return new MatterContainerItem(propertiesFor(id));
        }
        if (id.equals("matter_scanner")) {
            return new MatterScannerItem(propertiesFor(id));
        }
        if (id.equals("portable_decomposer")) {
            return new PortableDecomposerItem(propertiesFor(id));
        }
        if (id.equals("data_pad")) {
            return new DataPadItem(propertiesFor(id));
        }
        if (id.equals("m2_testing_checklist")) {
            return new DocumentationItem(propertiesFor(id), DocumentationItem.Document.TESTING_CHECKLIST);
        }
        if (id.equals("current_features_reference")) {
            return new DocumentationItem(propertiesFor(id), DocumentationItem.Document.FEATURE_REFERENCE);
        }
        if (id.equals("system_guide")) {
            return new DocumentationItem(propertiesFor(id), DocumentationItem.Document.SYSTEM_GUIDE);
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
        if (id.equals("spacetime_equalizer")) {
            return new SpacetimeEqualizerItem(propertiesFor(id));
        }
        if (id.equals("pattern_drive") || id.equals("creative_pattern_drive")) {
            return new PatternDriveItem(propertiesFor(id), id.equals("creative_pattern_drive"));
        }
        if (id.equals("tritanium_pickaxe")) return new PickaxeItem(TritaniumToolTier.TIER, 1, -2.8F, propertiesFor(id));
        if (id.equals("tritanium_axe")) return new AxeItem(TritaniumToolTier.TIER, 5.0F, -3.1F, propertiesFor(id));
        if (id.equals("tritanium_shovel")) return new ShovelItem(TritaniumToolTier.TIER, 1.5F, -3.0F, propertiesFor(id));
        if (id.equals("tritanium_hoe")) return new HoeItem(TritaniumToolTier.TIER, -2, 0.0F, propertiesFor(id));
        if (id.equals("tritanium_sword")) return new SwordItem(TritaniumToolTier.TIER, 3, -2.4F, propertiesFor(id));
        if (id.equals("tritanium_wrench")) return new TritaniumWrenchItem(propertiesFor(id).durability(2500));
        if (id.equals("tritanium_helmet")) return new TritaniumArmorItem(TritaniumArmorMaterial.MATERIAL, ArmorItem.Type.HELMET, propertiesFor(id));
        if (id.equals("tritanium_chestplate")) return new TritaniumArmorItem(TritaniumArmorMaterial.MATERIAL, ArmorItem.Type.CHESTPLATE, propertiesFor(id));
        if (id.equals("tritanium_leggings")) return new TritaniumArmorItem(TritaniumArmorMaterial.MATERIAL, ArmorItem.Type.LEGGINGS, propertiesFor(id));
        if (id.equals("tritanium_boots")) return new TritaniumArmorItem(TritaniumArmorMaterial.MATERIAL, ArmorItem.Type.BOOTS, propertiesFor(id));
        MachineUpgradeItem.Upgrade upgrade = MachineUpgradeItem.Upgrade.fromItemId(id);
        if (upgrade != null) {
            return new MachineUpgradeItem(propertiesFor(id), upgrade);
        }
        return new Item(propertiesFor(id));
    }

    private static int colorFor(String id) {
        return switch (id.substring("weapon_module_color".length()).replace("_", "")) {
            case "red" -> 0xff3333; case "green" -> 0x33cc66; case "limegreen" -> 0x66ff33; case "blue" -> 0x3366ff;
            case "skyblue" -> 0x33ccff; case "purple" -> 0xaa55ff; case "pink" -> 0xff66cc; case "gold" -> 0xffcc33;
            case "brown" -> 0x996633; case "gray" -> 0x999999; case "black" -> 0x222222; default -> 0x33ccff;
        };
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
                || id.equals("m2_testing_checklist")
                || id.equals("current_features_reference")
                || id.equals("system_guide")
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

