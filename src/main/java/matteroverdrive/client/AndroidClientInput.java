package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.android.AndroidAbilities;
import matteroverdrive.network.AndroidAbilityPacket;
import matteroverdrive.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AndroidClientInput {
    private AndroidClientInput() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null || !AndroidClientState.isActive()) {
            return;
        }

        while (AndroidKeyMappings.CYCLE_ABILITY.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new AndroidAbilityPacket(AndroidAbilities.ACTION_CYCLE));
        }
        while (AndroidKeyMappings.ACTIVATE_ABILITY.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new AndroidAbilityPacket(AndroidAbilities.ACTION_ACTIVATE));
        }
    }
}
