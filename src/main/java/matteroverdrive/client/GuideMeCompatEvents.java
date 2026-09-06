package matteroverdrive.client;

import com.mojang.logging.LogUtils;
import matteroverdrive.MatterOverdrive;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

import java.lang.reflect.Method;

/**
 * Optional GuideME bridge. Reflection deliberately keeps GuideME out of MO's
 * hard dependency graph while still using GuideME 20.1.15 when installed.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GuideMeCompatEvents {
    public static final ResourceLocation GUIDE_ID = new ResourceLocation(MatterOverdrive.MOD_ID, "guide");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean registered;

    private GuideMeCompatEvents() {}

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("guideme")) {
            event.enqueueWork(GuideMeCompatEvents::registerGuide);
        }
    }

    private static void registerGuide() {
        if (registered || !ModList.get().isLoaded("guideme")) return;
        try {
            Class<?> guideClass = Class.forName("guideme.Guide");
            Object builder = guideClass.getMethod("builder", ResourceLocation.class).invoke(null, GUIDE_ID);
            // GuideBuilder defaults are exactly what we need for matteroverdrive:guide:
            // assets/matteroverdrive/guides/matteroverdrive/guide, start page index.md.
            builder.getClass().getMethod("build").invoke(builder);
            registered = true;
        } catch (ReflectiveOperationException ex) {
            LOGGER.error("GuideME is installed but the Matter Overdrive guide could not be registered", ex);
        }
    }

    /** Opens the Matter Overdrive GuideME manual when GuideME is present. */
    public static boolean openGuide() {
        if (!ModList.get().isLoaded("guideme")) return false;
        registerGuide();
        try {
            Class<?> guideClass = Class.forName("guideme.Guide");
            Class<?> guidesClass = Class.forName("guideme.Guides");
            Object guide = guidesClass.getMethod("getById", ResourceLocation.class).invoke(null, GUIDE_ID);
            if (guide == null) return false;

            Object startPage = guideClass.getMethod("getStartPage").invoke(guide);
            Class<?> pageAnchorClass = Class.forName("guideme.PageAnchor");
            Object anchor = pageAnchorClass.getMethod("page", ResourceLocation.class).invoke(null, startPage);

            Class<?> guideScreenClass = Class.forName("guideme.internal.screen.GuideScreen");
            Method openNew = guideScreenClass.getMethod("openNew", guideClass, pageAnchorClass);
            Object screen = openNew.invoke(null, guide, anchor);
            Minecraft.getInstance().setScreen((net.minecraft.client.gui.screens.Screen) screen);
            return true;
        } catch (ReflectiveOperationException ex) {
            LOGGER.error("GuideME is installed but the Matter Overdrive guide could not be opened", ex);
            return false;
        }
    }
}
