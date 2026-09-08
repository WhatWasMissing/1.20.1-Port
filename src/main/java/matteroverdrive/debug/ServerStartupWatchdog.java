package matteroverdrive.debug;

import com.mojang.logging.LogUtils;
import matteroverdrive.MatterOverdrive;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Temporary startup diagnostic for the 0.6 single-player 100% loading hang.
 *
 * If Forge never reaches ServerStartedEvent, this emits thread stacks after a
 * short delay so the exact blocking call is captured in debug.log/latest.log.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerStartupWatchdog {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static final AtomicBoolean WATCHDOG_ARMED = new AtomicBoolean(false);

    private ServerStartupWatchdog() {}

    @SubscribeEvent
    public static void onAboutToStart(ServerAboutToStartEvent event) {
        STARTED.set(false);
        LOGGER.warn("M2 STARTUP TRACE: ServerAboutToStartEvent reached");
    }

    @SubscribeEvent
    public static void onStarting(ServerStartingEvent event) {
        LOGGER.warn("M2 STARTUP TRACE: ServerStartingEvent reached; arming 8s watchdog");
        if (!WATCHDOG_ARMED.compareAndSet(false, true)) {
            return;
        }

        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(8000L);
                if (!STARTED.get()) {
                    LOGGER.error("M2 STARTUP WATCHDOG: ServerStartedEvent not reached after 8s. Capturing relevant thread stacks.");
                    dumpRelevantThreads();
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                WATCHDOG_ARMED.set(false);
            }
        }, "M2 Startup Watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
    }

    @SubscribeEvent
    public static void onStarted(ServerStartedEvent event) {
        STARTED.set(true);
        LOGGER.warn("M2 STARTUP TRACE: ServerStartedEvent reached successfully");
    }

    private static void dumpRelevantThreads() {
        Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
        for (Map.Entry<Thread, StackTraceElement[]> entry : traces.entrySet()) {
            Thread thread = entry.getKey();
            String name = thread.getName();
            if (!(name.equals("Server thread")
                    || name.equals("Render thread")
                    || name.startsWith("Worker-Main")
                    || name.startsWith("ForkJoinPool")
                    || name.startsWith("Netty Local")
                    || name.startsWith("Netty Server"))) {
                continue;
            }

            StringBuilder stack = new StringBuilder(256)
                    .append("M2 STARTUP WATCHDOG THREAD: ")
                    .append(name)
                    .append(" state=")
                    .append(thread.getState());
            for (StackTraceElement element : entry.getValue()) {
                stack.append("\n    at ").append(element);
            }
            LOGGER.error(stack.toString());
        }
    }
}
