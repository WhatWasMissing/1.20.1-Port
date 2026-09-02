package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.menu.DecomposerMenu;
import matteroverdrive.menu.AndroidStationMenu;
import matteroverdrive.menu.NetworkRouterMenu;
import matteroverdrive.menu.ContractMarketMenu;
import matteroverdrive.menu.StarMapMenu;
import matteroverdrive.menu.ChargingStationMenu;
import matteroverdrive.menu.EnergyPipeMenu;
import matteroverdrive.menu.InscriberMenu;
import matteroverdrive.menu.TransporterMenu;
import matteroverdrive.menu.FusionReactorMenu;
import matteroverdrive.menu.GravitationalStabilizerMenu;
import matteroverdrive.menu.MatterAnalyzerMenu;
import matteroverdrive.menu.MatterRecyclerMenu;
import matteroverdrive.menu.MicrowaveMenu;
import matteroverdrive.menu.SpacetimeAcceleratorMenu;
import matteroverdrive.menu.ReplicatorMenu;
import matteroverdrive.menu.SolarPanelMenu;
import matteroverdrive.menu.TritaniumCrateMenu;
import matteroverdrive.menu.PatternStorageMenu;
import matteroverdrive.menu.PatternMonitorMenu;
import matteroverdrive.menu.WeaponStationMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MatterOverdrive.MOD_ID);
    public static final RegistryObject<MenuType<ContractMarketMenu>> CONTRACT_MARKET = MENUS.register("contract_market", () -> IForgeMenuType.create(ContractMarketMenu::new));
    public static final RegistryObject<MenuType<StarMapMenu>> STAR_MAP = MENUS.register("star_map", () -> IForgeMenuType.create(StarMapMenu::new));
    public static final RegistryObject<MenuType<NetworkRouterMenu>> NETWORK_ROUTER = MENUS.register("network_router", () -> IForgeMenuType.create(NetworkRouterMenu::new));
    public static final RegistryObject<MenuType<AndroidStationMenu>> ANDROID_STATION = MENUS.register("android_station", () -> IForgeMenuType.create(AndroidStationMenu::new));
    public static final RegistryObject<MenuType<ChargingStationMenu>> CHARGING_STATION = MENUS.register("charging_station", () -> IForgeMenuType.create(ChargingStationMenu::new));
    public static final RegistryObject<MenuType<EnergyPipeMenu>> ENERGY_PIPE = MENUS.register("heavy_matter_pipe", () -> IForgeMenuType.create(EnergyPipeMenu::new));
    public static final RegistryObject<MenuType<FusionReactorMenu>> FUSION_REACTOR_CONTROLLER = MENUS.register("fusion_reactor_controller", () -> IForgeMenuType.create(FusionReactorMenu::new));
    public static final RegistryObject<MenuType<GravitationalStabilizerMenu>> GRAVITATIONAL_STABILIZER = MENUS.register("gravitational_stabilizer", () -> IForgeMenuType.create(GravitationalStabilizerMenu::new));
    public static final RegistryObject<MenuType<TransporterMenu>> TRANSPORTER = MENUS.register("transporter", () -> IForgeMenuType.create(TransporterMenu::new));
    public static final RegistryObject<MenuType<InscriberMenu>> INSCRIBER = MENUS.register("inscriber", () -> IForgeMenuType.create(InscriberMenu::new));
    public static final RegistryObject<MenuType<DecomposerMenu>> DECOMPOSER = MENUS.register("decomposer", () -> IForgeMenuType.create(DecomposerMenu::new));
    public static final RegistryObject<MenuType<MatterRecyclerMenu>> MATTER_RECYCLER = MENUS.register("matter_recycler", () -> IForgeMenuType.create(MatterRecyclerMenu::new));
    public static final RegistryObject<MenuType<MicrowaveMenu>> MICROWAVE = MENUS.register("microwave", () -> IForgeMenuType.create(MicrowaveMenu::new));
    public static final RegistryObject<MenuType<SpacetimeAcceleratorMenu>> SPACETIME_ACCELERATOR = MENUS.register("spacetime_accelerator", () -> IForgeMenuType.create(SpacetimeAcceleratorMenu::new));
    public static final RegistryObject<MenuType<MatterAnalyzerMenu>> MATTER_ANALYZER = MENUS.register("matter_analyzer", () -> IForgeMenuType.create(MatterAnalyzerMenu::new));
    public static final RegistryObject<MenuType<ReplicatorMenu>> REPLICATOR = MENUS.register("replicator", () -> IForgeMenuType.create(ReplicatorMenu::new));
    public static final RegistryObject<MenuType<PatternStorageMenu>> PATTERN_STORAGE = MENUS.register("pattern_storage", () -> IForgeMenuType.create(PatternStorageMenu::new));
    public static final RegistryObject<MenuType<PatternMonitorMenu>> PATTERN_MONITOR = MENUS.register("pattern_monitor", () -> IForgeMenuType.create(PatternMonitorMenu::new));
    public static final RegistryObject<MenuType<SolarPanelMenu>> SOLAR_PANEL = MENUS.register("solar_panel", () -> IForgeMenuType.create(SolarPanelMenu::new));
    public static final RegistryObject<MenuType<TritaniumCrateMenu>> TRITANIUM_CRATE = MENUS.register("tritanium_crate", () -> IForgeMenuType.create(TritaniumCrateMenu::new));
    public static final RegistryObject<MenuType<WeaponStationMenu>> WEAPON_STATION = MENUS.register("weapon_station", () -> IForgeMenuType.create(WeaponStationMenu::new));
    private ModMenus() {}
}
