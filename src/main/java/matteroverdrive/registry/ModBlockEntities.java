package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.blockentity.BalancedGravitationalAnomalyBlockEntity;
import matteroverdrive.blockentity.AndroidStationBlockEntity;
import matteroverdrive.blockentity.AndroidSpawnerBlockEntity;
import matteroverdrive.blockentity.NetworkRouterBlockEntity;
import matteroverdrive.blockentity.NetworkSwitchBlockEntity;
import matteroverdrive.blockentity.PylonBlockEntity;
import matteroverdrive.blockentity.ChargingStationBlockEntity;
import matteroverdrive.blockentity.DecomposerBlockEntity;
import matteroverdrive.blockentity.EnergyPipeBlockEntity;
import matteroverdrive.blockentity.MatterAnalyzerBlockEntity;
import matteroverdrive.blockentity.InscriberBlockEntity;
import matteroverdrive.blockentity.TransporterBlockEntity;
import matteroverdrive.blockentity.FusionReactorControllerBlockEntity;
import matteroverdrive.blockentity.FusionReactorIOBlockEntity;
import matteroverdrive.blockentity.GravitationalAnomalyBlockEntity;
import matteroverdrive.blockentity.GravitationalStabilizerBlockEntity;
import matteroverdrive.blockentity.MatterRecyclerBlockEntity;
import matteroverdrive.blockentity.ReplicatorBlockEntity;
import matteroverdrive.blockentity.SolarPanelBlockEntity;
import matteroverdrive.blockentity.TritaniumCrateBlockEntity;
import matteroverdrive.blockentity.PatternStorageBlockEntity;
import matteroverdrive.blockentity.PatternMonitorBlockEntity;
import matteroverdrive.blockentity.WeaponStationBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MatterOverdrive.MOD_ID);
    public static final RegistryObject<BlockEntityType<NetworkRouterBlockEntity>> NETWORK_ROUTER = BLOCK_ENTITIES.register("network_router", () -> BlockEntityType.Builder.of(NetworkRouterBlockEntity::new, ModBlocks.get("network_router").get()).build(null));
    public static final RegistryObject<BlockEntityType<NetworkSwitchBlockEntity>> NETWORK_SWITCH = BLOCK_ENTITIES.register("network_switch", () -> BlockEntityType.Builder.of(NetworkSwitchBlockEntity::new, ModBlocks.get("network_switch").get()).build(null));
    public static final RegistryObject<BlockEntityType<PylonBlockEntity>> PYLON = BLOCK_ENTITIES.register("pylon", () -> BlockEntityType.Builder.of(PylonBlockEntity::new, ModBlocks.get("pylon").get()).build(null));
    public static final RegistryObject<BlockEntityType<AndroidStationBlockEntity>> ANDROID_STATION = BLOCK_ENTITIES.register("android_station", () -> BlockEntityType.Builder.of(AndroidStationBlockEntity::new, ModBlocks.get("android_station").get()).build(null));
    public static final RegistryObject<BlockEntityType<AndroidSpawnerBlockEntity>> ANDROID_SPAWNER = BLOCK_ENTITIES.register("android_spawner", () -> BlockEntityType.Builder.of(AndroidSpawnerBlockEntity::new, ModBlocks.get("android_spawner").get()).build(null));
    public static final RegistryObject<BlockEntityType<EnergyPipeBlockEntity>> ENERGY_PIPE = BLOCK_ENTITIES.register("heavy_matter_pipe", () -> BlockEntityType.Builder.of(EnergyPipeBlockEntity::new, ModBlocks.get("heavy_matter_pipe").get()).build(null));
    public static final RegistryObject<BlockEntityType<ChargingStationBlockEntity>> CHARGING_STATION = BLOCK_ENTITIES.register("charging_station", () -> BlockEntityType.Builder.of(ChargingStationBlockEntity::new, ModBlocks.get("charging_station").get()).build(null));
    public static final RegistryObject<BlockEntityType<FusionReactorControllerBlockEntity>> FUSION_REACTOR_CONTROLLER = BLOCK_ENTITIES.register("fusion_reactor_controller", () -> BlockEntityType.Builder.of(FusionReactorControllerBlockEntity::new, ModBlocks.get("fusion_reactor_controller").get()).build(null));
    public static final RegistryObject<BlockEntityType<FusionReactorIOBlockEntity>> FUSION_REACTOR_IO = BLOCK_ENTITIES.register("fusion_reactor_io", () -> BlockEntityType.Builder.of(FusionReactorIOBlockEntity::new, ModBlocks.get("fusion_reactor_io").get()).build(null));
    public static final RegistryObject<BlockEntityType<GravitationalAnomalyBlockEntity>> GRAVITATIONAL_ANOMALY = BLOCK_ENTITIES.register("gravitational_anomaly", () -> BlockEntityType.Builder.<GravitationalAnomalyBlockEntity>of(BalancedGravitationalAnomalyBlockEntity::new, ModBlocks.get("gravitational_anomaly").get()).build(null));
    public static final RegistryObject<BlockEntityType<GravitationalStabilizerBlockEntity>> GRAVITATIONAL_STABILIZER = BLOCK_ENTITIES.register("gravitational_stabilizer", () -> BlockEntityType.Builder.of(GravitationalStabilizerBlockEntity::new, ModBlocks.get("gravitational_stabilizer").get()).build(null));
    public static final RegistryObject<BlockEntityType<TransporterBlockEntity>> TRANSPORTER = BLOCK_ENTITIES.register("transporter", () -> BlockEntityType.Builder.of(TransporterBlockEntity::new, ModBlocks.get("transporter").get()).build(null));
    public static final RegistryObject<BlockEntityType<InscriberBlockEntity>> INSCRIBER = BLOCK_ENTITIES.register("inscriber", () -> BlockEntityType.Builder.of(InscriberBlockEntity::new, ModBlocks.get("inscriber").get()).build(null));
    public static final RegistryObject<BlockEntityType<DecomposerBlockEntity>> DECOMPOSER = BLOCK_ENTITIES.register("decomposer", () -> BlockEntityType.Builder.of(DecomposerBlockEntity::new, ModBlocks.get("decomposer").get()).build(null));
    public static final RegistryObject<BlockEntityType<MatterRecyclerBlockEntity>> MATTER_RECYCLER = BLOCK_ENTITIES.register("matter_recycler", () -> BlockEntityType.Builder.of(MatterRecyclerBlockEntity::new, ModBlocks.get("matter_recycler").get()).build(null));
    public static final RegistryObject<BlockEntityType<MatterAnalyzerBlockEntity>> MATTER_ANALYZER = BLOCK_ENTITIES.register("matter_analyzer", () -> BlockEntityType.Builder.of(MatterAnalyzerBlockEntity::new, ModBlocks.get("matter_analyzer").get()).build(null));
    public static final RegistryObject<BlockEntityType<ReplicatorBlockEntity>> REPLICATOR = BLOCK_ENTITIES.register("replicator", () -> BlockEntityType.Builder.of(ReplicatorBlockEntity::new, ModBlocks.get("replicator").get()).build(null));
    public static final RegistryObject<BlockEntityType<PatternStorageBlockEntity>> PATTERN_STORAGE = BLOCK_ENTITIES.register("pattern_storage", () -> BlockEntityType.Builder.of(PatternStorageBlockEntity::new, ModBlocks.get("pattern_storage").get()).build(null));
    public static final RegistryObject<BlockEntityType<PatternMonitorBlockEntity>> PATTERN_MONITOR = BLOCK_ENTITIES.register("pattern_monitor", () -> BlockEntityType.Builder.of(PatternMonitorBlockEntity::new, ModBlocks.get("pattern_monitor").get()).build(null));
    public static final RegistryObject<BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL = BLOCK_ENTITIES.register("solar_panel", () -> BlockEntityType.Builder.of(SolarPanelBlockEntity::new, ModBlocks.get("solar_panel").get()).build(null));
    public static final RegistryObject<BlockEntityType<TritaniumCrateBlockEntity>> TRITANIUM_CRATE = BLOCK_ENTITIES.register("tritanium_crate", () -> BlockEntityType.Builder.of(TritaniumCrateBlockEntity::new,
            ModBlocks.get("tritanium_crate").get(), ModBlocks.get("tritanium_crate_black").get(), ModBlocks.get("tritanium_crate_blue").get(), ModBlocks.get("tritanium_crate_brown").get(),
            ModBlocks.get("tritanium_crate_cyan").get(), ModBlocks.get("tritanium_crate_gray").get(), ModBlocks.get("tritanium_crate_green").get(), ModBlocks.get("tritanium_crate_light_blue").get(),
            ModBlocks.get("tritanium_crate_lime").get(), ModBlocks.get("tritanium_crate_magenta").get(), ModBlocks.get("tritanium_crate_orange").get(), ModBlocks.get("tritanium_crate_pink").get(),
            ModBlocks.get("tritanium_crate_purple").get(), ModBlocks.get("tritanium_crate_red").get(), ModBlocks.get("tritanium_crate_silver").get(), ModBlocks.get("tritanium_crate_white").get(),
            ModBlocks.get("tritanium_crate_yellow").get()).build(null));
    public static final RegistryObject<BlockEntityType<WeaponStationBlockEntity>> WEAPON_STATION = BLOCK_ENTITIES.register("weapon_station", () -> BlockEntityType.Builder.of(WeaponStationBlockEntity::new, ModBlocks.get("weapon_station").get()).build(null));
    private ModBlockEntities() {}
}
