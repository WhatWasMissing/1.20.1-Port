package matteroverdrive.client.debug;

import com.mojang.blaze3d.platform.InputConstants;
import matteroverdrive.MatterOverdrive;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DeveloperVisualDebugKeyMappings {
    public static final KeyMapping TOGGLE_GUI_OVERLAY = new KeyMapping(
            "key.matteroverdrive.dev_gui_overlay",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            "key.categories.matteroverdrive"
    );

    public static final KeyMapping TOGGLE_MODEL_OVERLAY = new KeyMapping(
            "key.matteroverdrive.dev_model_overlay",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            "key.categories.matteroverdrive"
    );

    private DeveloperVisualDebugKeyMappings() {}

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_GUI_OVERLAY);
        event.register(TOGGLE_MODEL_OVERLAY);
    }
}
