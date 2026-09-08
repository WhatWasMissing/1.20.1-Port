package matteroverdrive.debug;

import com.mojang.logging.LogUtils;
import matteroverdrive.MatterOverdrive;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Diagnostics for integrated-server startup and runtime stalls.  The continuous
 * heartbeat monitor is intentionally cheap and is particularly useful while the
 * restored native structures are being reintroduced: if a structure generation
 * task deadlocks a worldgen worker or blocks the server tick, the log captures the
 * relevant stacks without requiring a debugger.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerStartupWatchdog {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static final AtomicBoolean STOPPING = new AtomicBoolean(false);
    private static final AtomicBoolean WATCHDOG_ARMED = new AtomicBoolean(false);
    private static final AtomicBoolean POST_LOGIN_WATCHDOG_ARMED = new AtomicBoolean(false);
    private static final AtomicBoolean CONTINUOUS_WATCHDOG_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean STALL_REPORTED = new AtomicBoolean(false);
    private static final AtomicLong LAST_SERVER_TICK_NANOS = new AtomicLong(System.nanoTime());
    private static final long RUNTIME_STALL_NANOS = 5_000_000_000L;

    private ServerStartupWatchdog() {}

    @SubscribeEvent
    public static void onAboutToStart(ServerAboutToStartEvent event) {
        STARTED.set(false);
        STOPPING.set(false);
        STALL_REPORTED.set(false);
        LAST_SERVER_TICK_NANOS.set(System.nanoTime());
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
                    dumpRelevantThreads("M2 STARTUP WATCHDOG THREAD");
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
        LAST_SERVER_TICK_NANOS.set(System.nanoTime());
        LOGGER.warn("M2 STARTUP TRACE: ServerStartedEvent reached successfully; continuous runtime watchdog active");
        startContinuousWatchdog();
    }

    @SubscribeEvent
    public static void onStopping(ServerStoppingEvent event) {
        STOPPING.set(true);
        STARTED.set(false);
        STALL_REPORTED.set(false);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            LAST_SERVER_TICK_NANOS.set(System.nanoTime());
            STALL_REPORTED.set(false);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        LAST_SERVER_TICK_NANOS.set(System.nanoTime());
        LOGGER.warn("M2 RUNTIME TRACE: player login completed; arming post-login stall watchdog");
        if (!POST_LOGIN_WATCHDOG_ARMED.compareAndSet(false, true)) {
            return;
        }

        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(6500L);
                long stalledMillis = (System.nanoTime() - LAST_SERVER_TICK_NANOS.get()) / 1_000_000L;
                if (stalledMillis >= 4000L) {
                    LOGGER.error("M2 RUNTIME WATCHDOG: server tick heartbeat stalled for {} ms after player login. Capturing relevant thread stacks.", stalledMillis);
                    dumpRelevantThreads("M2 RUNTIME WATCHDOG THREAD");
                } else {
                    LOGGER.warn("M2 RUNTIME TRACE: post-login server tick heartbeat is healthy ({} ms since last completed tick)", stalledMillis);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                POST_LOGIN_WATCHDOG_ARMED.set(false);
            }
        }, "M2 Post Login Watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
    }

    private static void startContinuousWatchdog() {
        if (!CONTINUOUS_WATCHDOG_RUNNING.compareAndSet(false, true)) return;
        Thread watchdog = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(1000L);
                    if (!STARTED.get() || STOPPING.get()) continue;
                    long stalledNanos = System.nanoTime() - LAST_SERVER_TICK_NANOS.get();
                    if (stalledNanos < RUNTIME_STALL_NANOS) continue;
                    if (!STALL_REPORTED.compareAndSet(false, true)) continue;
                    long stalledMillis = stalledNanos / 1_000_000L;
                    LOGGER.error("M2 CONTINUOUS WATCHDOG: no completed server tick for {} ms. This may indicate chunk/structure generation deadlock or another blocking task.", stalledMillis);
                    dumpRelevantThreads("M2 CONTINUOUS WATCHDOG THREAD");
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                CONTINUOUS_WATCHDOG_RUNNING.set(false);
            }
        }, "M2 Continuous Runtime Watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
    }

    private static void dumpRelevantThreads(String prefix) {
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
                    .append(prefix)
                    .append(": ")
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
