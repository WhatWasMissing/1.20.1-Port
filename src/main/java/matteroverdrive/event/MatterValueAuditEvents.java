package matteroverdrive.event;

import com.mojang.logging.LogUtils;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.matter.MatterValueRegistry;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Server lifecycle hooks for the matter-value system.
 *
 * <p>The 0.6 recursive recipe valuation pass originally audited every registered
 * item synchronously from {@link ServerStartedEvent}. On modded instances that
 * turns into a very large recursive recipe-graph walk on the server thread and
 * prevents Forge from completing ServerStartedEvent, leaving the client on the
 * 100% world-loading screen.</p>
 *
 * <p>Keep startup deliberately cheap: invalidate the recipe cache here and leave
 * the expensive full-registry audit to the explicit /matteroverdrive matter audit
 * diagnostic command.</p>
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID)
public final class MatterValueAuditEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    private MatterValueAuditEvents() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MatterValueRegistry.clearRecipeCache();
        LOGGER.info("MATTER VALUE AUDIT: startup sweep skipped; use /matteroverdrive matter audit for an explicit full audit");
    }
}
