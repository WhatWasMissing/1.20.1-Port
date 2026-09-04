package matteroverdrive;

import com.mojang.logging.LogUtils;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModCreativeTabs;
import matteroverdrive.registry.ModExtraBlockEntities;
import matteroverdrive.registry.ModItems;
import matteroverdrive.registry.ModMenus;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.registry.ModSounds;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(MatterOverdrive.MOD_ID)
public final class MatterOverdrive {
    public static final String MOD_ID = "matteroverdrive";
    public static final String DISPLAY_VERSION = "Alpha Version 3";
    public static final String AUTHOR = "MVQ1303";
    private static final Logger LOGGER = LogUtils.getLogger();

    public MatterOverdrive() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.CREATIVE_TABS.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModExtraBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        modBus.addListener(ModCapabilities::register);
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
        LOGGER.info(
                "M1 VERIFY: Matter Overdrive registry shell initialized - blocks={}, blockItems={}, standaloneItems={}, sounds={}",
                ModBlocks.all().size(),
                ModItems.BLOCK_ITEMS.size(),
                ModItems.STANDALONE_ITEMS.size(),
                ModSounds.all().size()
        );
        LOGGER.info(
                "M2 VERIFY: machine foundation initialized - blockEntities=18, menus=15, gunSystem=enabled, weaponStation=enabled, energyPipe=enabled, fusionReactor=enabled, transporter=enabled, inscriber=enabled, decomposer=enabled, recycler=enabled, microwave=enabled, spacetimeAccelerator=enabled, handheldMatterTools=enabled, androidAbilities=enabled, documentationItems=enabled, systemGuide=enabled, analyzer=enabled, replicator=enabled, patternStorage=enabled, patternMonitor=enabled, solarPanel=enabled, tritaniumCrate=enabled, networkPipe=enabled, matterPipe=enabled, creativeBattery=enabled"
        );
    }
}
