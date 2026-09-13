package matteroverdrive.client;

import com.mojang.blaze3d.platform.InputConstants;
import matteroverdrive.MatterOverdrive;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Key mappings for client-only PDA controls. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PdaKeyMappings {
    public static final KeyMapping TOGGLE_VOICE = new KeyMapping(
            "key.matteroverdrive.pda_voice_toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.matteroverdrive"
    );

    private PdaKeyMappings() { }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_VOICE);
    }
}
