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
 * First-class optional GuideME 20.1.15 bridge.
 *
 * GuideME stays optional at class-loading time: all GuideME types are resolved
 * reflectively, while the exact 20.1.15 builder/registry/page-anchor API is used
 * when the mod is present.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GuideMeCompatEvents {
    public static final ResourceLocation GUIDE_ID = ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, "guide");
    public static final ResourceLocation START_PAGE = ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, "index.md");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean registered;
    private static Object guide;

    private GuideMeCompatEvents() {}

    /**
     * Registers the static guide early enough for GuideME's first resource reload
     * to discover and parse the packaged pages. Calling this again is harmless.
     */
    public static void bootstrap() {
        if (isAvailable()) registerGuide();
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        // Fallback/re-resolution only. Primary registration is performed from the
        // mod constructor before GuideME builds its initial resource page map.
        if (isAvailable()) event.enqueueWork(GuideMeCompatEvents::registerGuide);
    }

    public static boolean isAvailable() {
        return ModList.get().isLoaded("guideme");
    }

    private static synchronized void registerGuide() {
        if (registered || !isAvailable()) return;
        try {
            Class<?> guideClass = Class.forName("guideme.Guide");
            Object builder = guideClass.getMethod("builder", ResourceLocation.class).invoke(null, GUIDE_ID);

            // GuideBuilder 20.1.15 defaults to guides/<namespace>/<guide-path>.
            // Keep the exact packaged resource folder explicit.
            builder.getClass().getMethod("folder", String.class)
                    .invoke(builder, "guides/matteroverdrive/guide");
            builder.getClass().getMethod("defaultNamespace", String.class)
                    .invoke(builder, MatterOverdrive.MOD_ID);
            builder.getClass().getMethod("startPage", ResourceLocation.class)
                    .invoke(builder, START_PAGE);

            guide = builder.getClass().getMethod("build").invoke(builder);
            guide = resolveGuideOrBuilt(guide);
            registered = guide != null;
            if (registered) LOGGER.info("Registered Matter Overdrive GuideME manual {} before resource loading", GUIDE_ID);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            registered = false;
            guide = null;
            LOGGER.error("GuideME is installed but the Matter Overdrive manual could not be registered", ex);
        }
    }

    /** Opens the manual's start page. */
    public static boolean openGuide() {
        return openPage(START_PAGE);
    }

    /** Opens a concrete GuideME markdown page such as matteroverdrive:ae2.md. */
    public static boolean openPage(ResourceLocation page) {
        if (!isAvailable()) return false;
        registerGuide();
        try {
            Object currentGuide = resolveGuideOrBuilt(guide);
            if (currentGuide == null) return false;

            Class<?> guideClass = Class.forName("guideme.Guide");
            Class<?> pageAnchorClass = Class.forName("guideme.PageAnchor");
            Object anchor = pageAnchorClass.getMethod("page", ResourceLocation.class).invoke(null, page);

            Class<?> guideScreenClass = Class.forName("guideme.internal.screen.GuideScreen");
            Method openNew = guideScreenClass.getMethod("openNew", guideClass, pageAnchorClass);
            Object screen = openNew.invoke(null, currentGuide, anchor);
            Minecraft.getInstance().setScreen((net.minecraft.client.gui.screens.Screen) screen);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            LOGGER.error("GuideME is installed but page {} could not be opened", page, ex);
            return false;
        }
    }

    /**
     * Re-resolves the registered guide after a Minecraft resource reload.
     * GuideME 20.1.15 has no public Guides.reload() API; its own reload listener
     * refreshes page resources, so MO must not invoke a nonexistent method here.
     */
    public static synchronized boolean reloadGuide() {
        if (!isAvailable()) return false;
        try {
            guide = resolveGuideOrBuilt(guide);
            if (guide == null) {
                registered = false;
                registerGuide();
            }
            return registered && guide != null;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            LOGGER.error("GuideME manual re-resolution failed", ex);
            return false;
        }
    }

    private static Object resolveGuideOrBuilt(Object builtGuide) throws ReflectiveOperationException {
        Class<?> guidesClass = Class.forName("guideme.Guides");
        Object registeredGuide = guidesClass.getMethod("getById", ResourceLocation.class).invoke(null, GUIDE_ID);
        if (registeredGuide != null) return registeredGuide;
        return builtGuide;
    }
}
