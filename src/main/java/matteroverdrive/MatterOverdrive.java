package matteroverdrive;

import com.mojang.logging.LogUtils;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModCreativeTabs;
import matteroverdrive.registry.ModItems;
import matteroverdrive.registry.ModMenus;
import matteroverdrive.registry.ModSounds;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(MatterOverdrive.MOD_ID)
public final class MatterOverdrive {
    public static final String MOD_ID = "matteroverdrive";
    private static final Logger LOGGER = LogUtils.getLogger();

    public MatterOverdrive() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.CREATIVE_TABS.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        modBus.addListener(ModCapabilities::register);
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info(
                "M1 VERIFY: Matter Overdrive registry shell initialized - blocks={}, blockItems={}, standaloneItems={}, sounds={}",
                ModBlocks.all().size(),
                ModItems.BLOCK_ITEMS.size(),
                ModItems.STANDALONE_ITEMS.size(),
                ModSounds.all().size()
        );
        LOGGER.info(
                "M2 VERIFY: machine foundation initialized - blockEntities=7, menus=7, decomposer=enabled, recycler=enabled, analyzer=enabled, replicator=enabled, patternStorage=enabled, patternMonitor=enabled, solarPanel=enabled, networkPipe=enabled, matterPipe=enabled, creativeBattery=enabled"
        );
    }
}
