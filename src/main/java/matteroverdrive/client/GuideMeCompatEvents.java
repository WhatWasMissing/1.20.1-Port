package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Optional GuideME bridge. Reflection deliberately keeps GuideME out of MO's
 * hard dependency graph while still using the installed GuideME API.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GuideMeCompatEvents {
    public static final ResourceLocation GUIDE_ID = new ResourceLocation(MatterOverdrive.MOD_ID, "guide");
    private static boolean registered;

    private GuideMeCompatEvents() {}

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        if (!ModList.get().isLoaded("guideme")) return;
        event.enqueueWork(GuideMeCompatEvents::registerGuide);
    }

    private static void registerGuide() {
        if (registered) return;
        try {
            Class<?> guideClass = Class.forName("guideme.Guide");
            Method builderMethod = guideClass.getMethod("builder", ResourceLocation.class);
            Object builder = builderMethod.invoke(null, GUIDE_ID);
            Method buildMethod = builder.getClass().getMethod("build");
            buildMethod.invoke(builder);
            registered = true;
        } catch (ReflectiveOperationException ex) {
            MatterOverdrive.LOGGER.error("GuideME is installed but the Matter Overdrive guide could not be registered", ex);
        }
    }

    /** Opens the GuideME manual when GuideME is present. */
    public static boolean openGuide() {
        if (!ModList.get().isLoaded("guideme")) return false;
        registerGuide();
        try {
            Class<?> guidesClass = Class.forName("guideme.Guides");
            Object guide = guidesClass.getMethod("getById", ResourceLocation.class).invoke(null, GUIDE_ID);
            if (guide == null) return false;

            Class<?> guideClass = Class.forName("guideme.Guide");
            Class<?> pageAnchorClass = Class.forName("guideme.PageAnchor");
            Class<?> screenClass = Class.forName("net.minecraft.client.gui.screens.Screen");
            Class<?> guideScreenClass = Class.forName("guideme.internal.screen.GuideScreen");
            Constructor<?> constructor = guideScreenClass.getConstructor(guideClass, pageAnchorClass, screenClass);
            Object screen = constructor.newInstance(guide, null, Minecraft.getInstance().screen);
            Minecraft.getInstance().setScreen((net.minecraft.client.gui.screens.Screen) screen);
            return true;
        } catch (ReflectiveOperationException ex) {
            MatterOverdrive.LOGGER.error("GuideME is installed but the Matter Overdrive guide could not be opened", ex);
            return false;
        }
    }
}
