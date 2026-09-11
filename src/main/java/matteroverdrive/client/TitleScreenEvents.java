package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.client.screen.MatterOverdriveTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Adds a small, compatibility-friendly Matter Overdrive entry point to the vanilla title screen. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class TitleScreenEvents {
    private TitleScreenEvents() {}

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof TitleScreen title)) return;
        int x = Math.max(8, title.width - 132);
        int y = 8;
        event.addListener(Button.builder(Component.literal("MATTER OVERDRIVE"),
                button -> Minecraft.getInstance().setScreen(new MatterOverdriveTitleScreen(title)))
                .bounds(x, y, 124, 20).build());
    }
}
