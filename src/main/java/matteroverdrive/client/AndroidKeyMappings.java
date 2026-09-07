package matteroverdrive.client;

import com.mojang.blaze3d.platform.InputConstants;
import matteroverdrive.MatterOverdrive;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AndroidKeyMappings {
    public static final KeyMapping CYCLE_ABILITY = new KeyMapping("key.matteroverdrive.android_cycle_ability", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.matteroverdrive");
    public static final KeyMapping OPEN_SKILL_TREE = new KeyMapping("key.matteroverdrive.android_skill_tree", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.matteroverdrive");
    public static final KeyMapping OPEN_LOADOUT = new KeyMapping("key.matteroverdrive.android_loadout", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_L, "key.categories.matteroverdrive");
    public static final KeyMapping ACTIVATE_ABILITY = new KeyMapping("key.matteroverdrive.android_activate_ability", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.categories.matteroverdrive");

    private AndroidKeyMappings() {}

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(CYCLE_ABILITY);
        event.register(ACTIVATE_ABILITY);
        event.register(OPEN_SKILL_TREE);
        event.register(OPEN_LOADOUT);
    }
}
