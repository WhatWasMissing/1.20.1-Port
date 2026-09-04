package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.client.screen.DecomposerScreen;
import matteroverdrive.client.screen.AndroidStationScreen;
import matteroverdrive.client.screen.NetworkRouterScreen;
import matteroverdrive.client.screen.ContractMarketScreen;
import matteroverdrive.client.screen.StarMapScreen;
import matteroverdrive.client.screen.ChargingStationScreen;
import matteroverdrive.client.screen.EnergyPipeScreen;
import matteroverdrive.client.screen.InscriberScreen;
import matteroverdrive.client.screen.TransporterScreen;
import matteroverdrive.client.screen.FusionReactorScreen;
import matteroverdrive.client.screen.GravitationalStabilizerScreen;
import matteroverdrive.client.screen.MatterAnalyzerScreen;
import matteroverdrive.client.screen.MatterRecyclerScreen;
import matteroverdrive.client.screen.MicrowaveScreen;
import matteroverdrive.client.screen.SpacetimeAcceleratorScreen;
import matteroverdrive.client.screen.ReplicatorScreen;
import matteroverdrive.client.screen.SolarPanelScreen;
import matteroverdrive.client.screen.TritaniumCrateScreen;
import matteroverdrive.client.screen.PatternStorageScreen;
import matteroverdrive.client.screen.PatternMonitorScreen;
import matteroverdrive.client.screen.WeaponStationScreen;
import matteroverdrive.item.MatterContainerItem;
import matteroverdrive.item.PatternDriveItem;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModItems;
import matteroverdrive.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.ANDROID_STATION.get(), AndroidStationScreen::new);
            MenuScreens.register(ModMenus.NETWORK_ROUTER.get(), NetworkRouterScreen::new);
            MenuScreens.register(ModMenus.CONTRACT_MARKET.get(), ContractMarketScreen::new);
            MenuScreens.register(ModMenus.STAR_MAP.get(), StarMapScreen::new);
            MenuScreens.register(ModMenus.ENERGY_PIPE.get(), EnergyPipeScreen::new);
            MenuScreens.register(ModMenus.CHARGING_STATION.get(), ChargingStationScreen::new);
            MenuScreens.register(ModMenus.FUSION_REACTOR_CONTROLLER.get(), FusionReactorScreen::new);
            MenuScreens.register(ModMenus.GRAVITATIONAL_STABILIZER.get(), GravitationalStabilizerScreen::new);
            MenuScreens.register(ModMenus.TRANSPORTER.get(), TransporterScreen::new);
            MenuScreens.register(ModMenus.INSCRIBER.get(), InscriberScreen::new);
            MenuScreens.register(ModMenus.DECOMPOSER.get(), DecomposerScreen::new);
            MenuScreens.register(ModMenus.MATTER_RECYCLER.get(), MatterRecyclerScreen::new);
            MenuScreens.register(ModMenus.MICROWAVE.get(), MicrowaveScreen::new);
            MenuScreens.register(ModMenus.SPACETIME_ACCELERATOR.get(), SpacetimeAcceleratorScreen::new);
            MenuScreens.register(ModMenus.MATTER_ANALYZER.get(), MatterAnalyzerScreen::new);
            MenuScreens.register(ModMenus.REPLICATOR.get(), ReplicatorScreen::new);
            MenuScreens.register(ModMenus.PATTERN_STORAGE.get(), PatternStorageScreen::new);
            MenuScreens.register(ModMenus.PATTERN_MONITOR.get(), PatternMonitorScreen::new);
            MenuScreens.register(ModMenus.SOLAR_PANEL.get(), SolarPanelScreen::new);
            MenuScreens.register(ModMenus.TRITANIUM_CRATE.get(), TritaniumCrateScreen::new);
            MenuScreens.register(ModMenus.WEAPON_STATION.get(), WeaponStationScreen::new);

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.get("industrial_glass").get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.get("bounding_box").get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.get("matter_plasma").get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.get("molten_tritanium").get(), RenderType.translucent());

            ItemProperties.register(
                    ModItems.get("matter_container").get(),
                    new ResourceLocation(MatterOverdrive.MOD_ID, "matter_fill"),
                    (stack, level, entity, seed) -> MatterContainerItem.getFillFraction(stack)
            );

            ItemProperties.register(
                    ModItems.get("pattern_drive").get(),
                    new ResourceLocation(MatterOverdrive.MOD_ID, "pattern_fill"),
                    (stack, level, entity, seed) -> patternFill(stack)
            );
            ItemProperties.register(
                    ModItems.get("creative_pattern_drive").get(),
                    new ResourceLocation(MatterOverdrive.MOD_ID, "pattern_fill"),
                    (stack, level, entity, seed) -> patternFill(stack)
            );
            ItemProperties.register(
                    ModItems.get("matter_scanner").get(),
                    new ResourceLocation(MatterOverdrive.MOD_ID, "scanner_linked"),
                    (stack, level, entity, seed) -> stack.hasTag()
                            && stack.getTag().getBoolean("ScannerLinked") ? 1.0F : 0.0F
            );
        });
    }

    private static float patternFill(net.minecraft.world.item.ItemStack stack) {
        int capacity = PatternDriveItem.getCapacity(stack);
        if (capacity <= 0) return 0.0F;
        return Math.min(1.0F, PatternDriveItem.getPatterns(stack).size() / (float) capacity);
    }
}
