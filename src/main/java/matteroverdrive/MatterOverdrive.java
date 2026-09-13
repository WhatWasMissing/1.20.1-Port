package matteroverdrive;

import com.mojang.logging.LogUtils;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.registry.*;
import matteroverdrive.network.ModNetwork;
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
    public static final String MOD_ID="matteroverdrive";public static final String DISPLAY_VERSION="0.6";public static final String AUTHOR="MVQ1303";private static final Logger LOGGER=LogUtils.getLogger();
    public MatterOverdrive(){IEventBus modBus=FMLJavaModLoadingContext.get().getModEventBus();ModBlocks.BLOCKS.register(modBus);ModItems.ITEMS.register(modBus);OverhaulContent.BLOCKS.register(modBus);OverhaulContent.ITEMS.register(modBus);OverhaulContent.ENTITY_TYPES.register(modBus);ModExoticItems.ITEMS.register(modBus);ModDestinyItems.ITEMS.register(modBus);ModCreativeTabs.CREATIVE_TABS.register(modBus);ModSounds.SOUND_EVENTS.register(modBus);ModDestinySounds.SOUND_EVENTS.register(modBus);ModEntities.ENTITY_TYPES.register(modBus);ModBlockEntities.BLOCK_ENTITIES.register(modBus);ModExtraBlockEntities.BLOCK_ENTITIES.register(modBus);OverhaulContent.BLOCK_ENTITIES.register(modBus);ModMenus.MENUS.register(modBus);ModFeatures.FEATURES.register(modBus);ModStructures.STRUCTURE_TYPES.register(modBus);ModStructures.STRUCTURE_PIECES.register(modBus);modBus.addListener(ModCapabilities::register);modBus.addListener(this::commonSetup);if(FMLEnvironment.dist==Dist.CLIENT&&ModList.get().isLoaded("guideme"))matteroverdrive.client.GuideMeCompatEvents.bootstrap();}
    private void commonSetup(final FMLCommonSetupEvent event){event.enqueueWork(ModNetwork::register);LOGGER.info("Matter Overdrive 0.6 overhaul initialized: generatedStructures=retired, playerDrivenPDA=enabled, quantumFluxReactor=enabled, environmentalRegulator=enabled, villageSpecialists=enabled, syntheticThreats=enabled");}
}
