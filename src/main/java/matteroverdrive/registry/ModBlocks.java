package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.block.ChargingStationBlock;
import matteroverdrive.block.AndroidStationBlock;
import matteroverdrive.block.AndroidSpawnerBlock;
import matteroverdrive.block.DecomposerBlock;
import matteroverdrive.block.EnergyPipeBlock;
import matteroverdrive.block.MatterAnalyzerBlock;
import matteroverdrive.block.InscriberBlock;
import matteroverdrive.block.FusionReactorControllerBlock;
import matteroverdrive.block.FusionReactorIOBlock;
import matteroverdrive.block.GravitationalAnomalyBlock;
import matteroverdrive.block.GravitationalStabilizerBlock;
import matteroverdrive.block.TransporterBlock;
import matteroverdrive.block.MatterRecyclerBlock;
import matteroverdrive.block.ReplicatorBlock;
import matteroverdrive.block.SolarPanelBlock;
import matteroverdrive.block.TritaniumCrateBlock;
import matteroverdrive.block.PatternStorageBlock;
import matteroverdrive.block.PatternMonitorBlock;
import matteroverdrive.block.WeaponStationBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MatterOverdrive.MOD_ID);

    private static final Map<String, RegistryObject<Block>> BLOCKS_BY_ID = new LinkedHashMap<>();

    public static final Set<String> NO_BLOCK_ITEM = Set.of(
            "bounding_box",
            "matter_plasma",
            "molten_tritanium"
    );

    private static final List<String> LEGACY_BLOCK_IDS = List.of(
        "android_spawner",
        "android_station",
        "bounding_box",
        "charging_station",
        "contract_market",
        "decomposer",
        "decorative.beams",
        "decorative.carbon_fiber_plate",
        "decorative.clean",
        "decorative.coils",
        "decorative.engine_exhaust_plasma",
        "decorative.floor_noise",
        "decorative.floor_tile_white",
        "decorative.floor_tiles",
        "decorative.floor_tiles_green",
        "decorative.holo_matrix",
        "decorative.matter_tube",
        "decorative.separator",
        "decorative.stripes",
        "decorative.tritanium_lamp",
        "decorative.tritanium_plate",
        "decorative.tritanium_plate_colored",
        "decorative.tritanium_plate_stripe",
        "decorative.vent.bright",
        "decorative.vent.dark",
        "decorative.white_plate",
        "dilithium_ore",
        "debug_matter_block",
        "fusion_reactor_coil",
        "fusion_reactor_controller",
        "fusion_reactor_io",
        "gravitational_anomaly",
        "gravitational_stabilizer",
        "heavy_matter_pipe",
        "holo_sign",
        "industrial_glass",
        "inscriber",
        "machine_hull",
        "matter_analyzer",
        "matter_pipe",
        "matter_plasma",
        "matter_recycler",
        "microwave",
        "molten_tritanium",
        "network_pipe",
        "network_router",
        "network_switch",
        "pattern_monitor",
        "pattern_storage",
        "pylon",
        "replicator",
        "solar_panel",
        "spacetime_accelerator",
        "star_map",
        "transporter",
        "tritanium_block",
        "tritanium_crate",
        "tritanium_crate_black",
        "tritanium_crate_blue",
        "tritanium_crate_brown",
        "tritanium_crate_cyan",
        "tritanium_crate_gray",
        "tritanium_crate_green",
        "tritanium_crate_light_blue",
        "tritanium_crate_lime",
        "tritanium_crate_magenta",
        "tritanium_crate_orange",
        "tritanium_crate_pink",
        "tritanium_crate_purple",
        "tritanium_crate_red",
        "tritanium_crate_silver",
        "tritanium_crate_white",
        "tritanium_crate_yellow",
        "tritanium_ore",
        "weapon_station"
    );

    static {
        LEGACY_BLOCK_IDS.forEach(ModBlocks::registerPlaceholder);
    }

    private ModBlocks() {
    }

    private static void registerPlaceholder(String id) {
        if (id.equals("android_station")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new AndroidStationBlock(propertiesFor(id))));
        } else if (id.equals("android_spawner")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new AndroidSpawnerBlock(propertiesFor(id))));
        } else if (id.equals("heavy_matter_pipe")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new EnergyPipeBlock(propertiesFor(id))));
        } else if (id.equals("charging_station")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new ChargingStationBlock(propertiesFor(id))));
        } else if (id.equals("fusion_reactor_controller")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new FusionReactorControllerBlock(propertiesFor(id))));
        } else if (id.equals("fusion_reactor_io")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new FusionReactorIOBlock(propertiesFor(id))));
        } else if (id.equals("gravitational_anomaly")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new GravitationalAnomalyBlock(propertiesFor(id))));
        } else if (id.equals("gravitational_stabilizer")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new GravitationalStabilizerBlock(propertiesFor(id))));
        } else if (id.equals("decomposer")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new DecomposerBlock(propertiesFor(id))));
        } else if (id.equals("matter_recycler")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new MatterRecyclerBlock(propertiesFor(id))));
        } else if (id.equals("transporter")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new TransporterBlock(propertiesFor(id))));
        } else if (id.equals("inscriber")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new InscriberBlock(propertiesFor(id))));
        } else if (id.equals("matter_analyzer")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new MatterAnalyzerBlock(propertiesFor(id))));
        } else if (id.equals("replicator")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new ReplicatorBlock(propertiesFor(id))));
        } else if (id.equals("pattern_storage")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new PatternStorageBlock(propertiesFor(id))));
        } else if (id.equals("pattern_monitor")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new PatternMonitorBlock(propertiesFor(id))));
        } else if (id.equals("solar_panel")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new SolarPanelBlock(propertiesFor(id))));
        } else if (id.equals("weapon_station")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new WeaponStationBlock(propertiesFor(id))));
        } else if (id.equals("tritanium_crate") || id.startsWith("tritanium_crate_")) {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new TritaniumCrateBlock(propertiesFor(id))));
        } else {
            BLOCKS_BY_ID.put(id, BLOCKS.register(id, () -> new Block(propertiesFor(id))));
        }
    }

    private static BlockBehaviour.Properties propertiesFor(String id) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .strength(isMachineLike(id) ? 4.0F : 2.0F, isMachineLike(id) ? 12.0F : 6.0F);

        if (!id.equals("industrial_glass") && !id.equals("bounding_box")) {
            properties = properties.requiresCorrectToolForDrops();
        }

        if (id.equals("industrial_glass")
                || id.equals("bounding_box")
                || id.equals("matter_plasma")
                || id.equals("molten_tritanium")
                || id.equals("gravitational_anomaly")) {
            properties = properties.noOcclusion();
        }

        // Items and entities must be able to cross the block boundary to reach
        // the anomaly's event horizon and be consumed.
        if (id.equals("gravitational_anomaly")) {
            properties = properties.noCollission();
        }

        if (id.equals("decorative.tritanium_lamp")
                || id.equals("gravitational_anomaly")
                || id.equals("decorative.engine_exhaust_plasma")) {
            properties = properties.lightLevel(state -> 15);
        }

        return properties;
    }

    private static boolean isMachineLike(String id) {
        return id.contains("replicator")
                || id.contains("decomposer")
                || id.contains("analyzer")
                || id.contains("storage")
                || id.contains("station")
                || id.contains("reactor")
                || id.contains("transporter")
                || id.contains("network")
                || id.contains("recycler")
                || id.contains("monitor")
                || id.contains("accelerator")
                || id.contains("inscriber")
                || id.contains("microwave")
                || id.contains("solar")
                || id.contains("crate");
    }

    public static Map<String, RegistryObject<Block>> all() {
        return Collections.unmodifiableMap(BLOCKS_BY_ID);
    }

    public static RegistryObject<Block> get(String id) {
        RegistryObject<Block> block = BLOCKS_BY_ID.get(id);
        if (block == null) {
            throw new IllegalArgumentException("Unknown Matter Overdrive block id: " + id);
        }
        return block;
    }
}
