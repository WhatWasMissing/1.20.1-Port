package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.client.screen.DecomposerScreen;
import matteroverdrive.client.screen.InscriberScreen;
import matteroverdrive.client.screen.TransporterScreen;
import matteroverdrive.client.screen.FusionReactorScreen;
import matteroverdrive.client.screen.MatterAnalyzerScreen;
import matteroverdrive.client.screen.MatterRecyclerScreen;
import matteroverdrive.client.screen.ReplicatorScreen;
import matteroverdrive.client.screen.SolarPanelScreen;
import matteroverdrive.client.screen.TritaniumCrateScreen;
import matteroverdrive.client.screen.PatternStorageScreen;
import matteroverdrive.client.screen.PatternMonitorScreen;
import matteroverdrive.item.MatterContainerItem;
import matteroverdrive.registry.ModItems;
import matteroverdrive.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
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
            MenuScreens.register(ModMenus.FUSION_REACTOR_CONTROLLER.get(), FusionReactorScreen::new);
            MenuScreens.register(ModMenus.TRANSPORTER.get(), TransporterScreen::new);
            MenuScreens.register(ModMenus.INSCRIBER.get(), InscriberScreen::new);
            MenuScreens.register(ModMenus.DECOMPOSER.get(), DecomposerScreen::new);
            MenuScreens.register(ModMenus.MATTER_RECYCLER.get(), MatterRecyclerScreen::new);
            MenuScreens.register(ModMenus.MATTER_ANALYZER.get(), MatterAnalyzerScreen::new);
            MenuScreens.register(ModMenus.REPLICATOR.get(), ReplicatorScreen::new);
            MenuScreens.register(ModMenus.PATTERN_STORAGE.get(), PatternStorageScreen::new);
            MenuScreens.register(ModMenus.PATTERN_MONITOR.get(), PatternMonitorScreen::new);
            MenuScreens.register(ModMenus.SOLAR_PANEL.get(), SolarPanelScreen::new);
            MenuScreens.register(ModMenus.TRITANIUM_CRATE.get(), TritaniumCrateScreen::new);

            ItemProperties.register(
                    ModItems.get("matter_container").get(),
                    new ResourceLocation(MatterOverdrive.MOD_ID, "matter_fill"),
                    (stack, level, entity, seed) -> MatterContainerItem.getFillFraction(stack)
            );
        });
    }
}
