package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.menu.DecomposerMenu;
import matteroverdrive.menu.MatterAnalyzerMenu;
import matteroverdrive.menu.MatterRecyclerMenu;
import matteroverdrive.menu.ReplicatorMenu;
import matteroverdrive.menu.SolarPanelMenu;
import matteroverdrive.menu.TritaniumCrateMenu;
import matteroverdrive.menu.PatternStorageMenu;
import matteroverdrive.menu.PatternMonitorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MatterOverdrive.MOD_ID);
    public static final RegistryObject<MenuType<DecomposerMenu>> DECOMPOSER = MENUS.register("decomposer", () -> IForgeMenuType.create(DecomposerMenu::new));
    public static final RegistryObject<MenuType<MatterRecyclerMenu>> MATTER_RECYCLER = MENUS.register("matter_recycler", () -> IForgeMenuType.create(MatterRecyclerMenu::new));
    public static final RegistryObject<MenuType<MatterAnalyzerMenu>> MATTER_ANALYZER = MENUS.register("matter_analyzer", () -> IForgeMenuType.create(MatterAnalyzerMenu::new));
    public static final RegistryObject<MenuType<ReplicatorMenu>> REPLICATOR = MENUS.register("replicator", () -> IForgeMenuType.create(ReplicatorMenu::new));
    public static final RegistryObject<MenuType<PatternStorageMenu>> PATTERN_STORAGE = MENUS.register("pattern_storage", () -> IForgeMenuType.create(PatternStorageMenu::new));
    public static final RegistryObject<MenuType<PatternMonitorMenu>> PATTERN_MONITOR = MENUS.register("pattern_monitor", () -> IForgeMenuType.create(PatternMonitorMenu::new));
    public static final RegistryObject<MenuType<SolarPanelMenu>> SOLAR_PANEL = MENUS.register("solar_panel", () -> IForgeMenuType.create(SolarPanelMenu::new));
    public static final RegistryObject<MenuType<TritaniumCrateMenu>> TRITANIUM_CRATE = MENUS.register("tritanium_crate", () -> IForgeMenuType.create(TritaniumCrateMenu::new));
    private ModMenus() {}
}
