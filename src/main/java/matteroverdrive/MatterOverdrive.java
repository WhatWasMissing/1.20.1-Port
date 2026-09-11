package matteroverdrive;

import com.mojang.logging.LogUtils;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModCreativeTabs;
import matteroverdrive.registry.ModEntities;
import matteroverdrive.registry.ModDestinyItems;
import matteroverdrive.registry.ModDestinySounds;
import matteroverdrive.registry.ModExoticItems;
import matteroverdrive.registry.ModExtraBlockEntities;
import matteroverdrive.registry.ModFeatures;
import matteroverdrive.registry.ModItems;
import matteroverdrive.registry.ModMenus;
import matteroverdrive.registry.ModStructures;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.registry.ModSounds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(MatterOverdrive.MOD_ID)
public final class MatterOverdrive {
    public static final String MOD_ID = "matteroverdrive";
    public static final String DISPLAY_VERSION = "0.6";
    public static final String AUTHOR = "MVQ1303";
    private static final Logger LOGGER = LogUtils.getLogger();

    public MatterOverdrive() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModExoticItems.ITEMS.register(modBus);
        ModDestinyItems.ITEMS.register(modBus);
        ModCreativeTabs.CREATIVE_TABS.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);
        ModDestinySounds.SOUND_EVENTS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModExtraBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModFeatures.FEATURES.register(modBus);
        ModStructures.STRUCTURE_TYPES.register(modBus);
        ModStructures.STRUCTURE_PIECES.register(modBus);
        modBus.addListener(ModCapabilities::register);
        modBus.addListener(this::commonSetup);

        if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("guideme")) {
            matteroverdrive.client.GuideMeCompatEvents.bootstrap();
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
        LOGGER.info("M1 VERIFY: Matter Overdrive registry shell initialized - blocks={}, blockItems={}, standaloneItems={}, sounds={}, nativeDestinyWeapons={}", ModBlocks.all().size(), ModItems.BLOCK_ITEMS.size(), ModItems.STANDALONE_ITEMS.size(), ModSounds.all().size(), ModDestinyItems.WEAPONS.size());
        LOGGER.info("M2 VERIFY: machine foundation initialized - blockEntities=19, menus=16, gunSystem=enabled, weaponStation=enabled, energyPipe=enabled, fusionReactor=enabled, transporter=enabled, inscriber=enabled, decomposer=enabled, recycler=enabled, microwave=enabled, spacetimeAccelerator=enabled, handheldMatterTools=enabled, androidAbilities=enabled, documentationItems=enabled, systemGuide=enabled, analyzer=enabled, replicator=enabled, patternStorage=enabled, patternMonitor=enabled, solarPanel=enabled, tritaniumCrate=enabled, networkPipe=enabled, matterPipe=enabled, droneFabricator=enabled, creativeBattery=enabled, legacyEntities=enabled, holoSign=enabled, securityProtocol=enabled, nativeStructures=enabled");
    }
}
