package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.pda.PdaVoiceLineCatalog;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/** Serializes PDA callouts and owns transient facility/hazard presentation state. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientPdaNotificationManager {
    public record FacilityBanner(String siteId, String facility, String recordTitle,
                                 int archiveIndex, String classification, int age, int ticksLeft) { }
    public record Hazard(String id, String title, String detail, int severity) {
        public boolean active() { return id != null && !id.isBlank() && !"none".equals(id); }
    }
    private record Pending(String lineId, String text, int priority) { }

    private static final Deque<Pending> QUEUE = new ArrayDeque<>();
    private static Pending active;
    private static int activeTicks;
    private static int activeAge;
    private static String lastQueuedId = "";
    private static String bannerSite = "";
    private static String bannerFacility = "";
    private static String bannerRecord = "";
    private static String bannerClassification = "";
    private static int bannerArchiveIndex;
    private static int bannerTicks;
    private static int bannerAge;
    private static Hazard hazard = new Hazard("none", "", "", 0);

    private ClientPdaNotificationManager() {}

    public static synchronized void enqueue(String lineId) {
        String text = PdaVoiceLineCatalog.line(lineId);
        if (text.isBlank()) return;
        if (lineId.equals(lastQueuedId) && (active != null || !QUEUE.isEmpty())) return;
        lastQueuedId = lineId;
        Pending pending = new Pending(lineId, text, priority(lineId));
        if (pending.priority() >= 9) QUEUE.addFirst(pending);
        else QUEUE.addLast(pending);
    }

    public static synchronized void showFacilityBanner(String siteId, String facility, String recordTitle,
                                                        int archiveIndex, String classification) {
        bannerSite = safe(siteId);
        bannerFacility = safe(facility);
        bannerRecord = safe(recordTitle);
        bannerClassification = safe(classification);
        bannerArchiveIndex = Math.max(0, archiveIndex);
        bannerTicks = 120;
        bannerAge = 0;
        PdaEmbeddedAudio.playUi("ui_facility");
    }

    public static synchronized void setHazard(String id, String title, String detail, int severity) {
        String normalized = safe(id).toLowerCase(Locale.ROOT);
        Hazard next = new Hazard(normalized.isBlank() ? "none" : normalized,
                safe(title), safe(detail), Math.max(0, Math.min(3, severity)));
        boolean changed = !hazard.id().equals(next.id()) || hazard.severity() != next.severity();
        hazard = next;
        if (!changed || !next.active()) return;

        if (next.severity() >= 2) PdaEmbeddedAudio.playUi("ui_hazard");
        switch (next.id()) {
            case "gravitational_anomaly", "anomaly_containment" -> enqueue("anomaly_warning");
            case "icarus_containment" -> enqueue("icarus_warning");
            case "legacy_security" -> enqueue("orpheus_security");
            case "m0_resonance" -> enqueue("matter_resonance");
            case "pressure_damage" -> enqueue("pressure_warning");
            case "structural_damage" -> enqueue("structural_warning");
            case "signal_echo" -> enqueue("signal_echo");
            default -> { }
        }
    }

    public static synchronized FacilityBanner banner() {
        return bannerTicks <= 0 ? null : new FacilityBanner(bannerSite, bannerFacility, bannerRecord,
                bannerArchiveIndex, bannerClassification, bannerAge, bannerTicks);
    }
    public static synchronized Hazard hazard() { return hazard; }
    public static synchronized String caption() { return active == null ? "" : active.text(); }
    public static synchronized int captionAge() { return activeAge; }
    public static synchronized int captionTicksLeft() { return activeTicks; }

    public static synchronized void clear() {
        QUEUE.clear();
        active = null;
        activeTicks = 0;
        activeAge = 0;
        bannerTicks = 0;
        hazard = new Hazard("none", "", "", 0);
        PdaEmbeddedAudio.stopVoice();
        PdaNarrationController.stop();
        lastQueuedId = "";
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        synchronized (ClientPdaNotificationManager.class) {
            if (minecraft.player == null) {
                if (active != null || !QUEUE.isEmpty() || bannerTicks > 0) clear();
                return;
            }
            if (bannerTicks > 0) { bannerTicks--; bannerAge++; }
            if (active != null) {
                activeTicks--;
                activeAge++;
                if (activeTicks <= 0) {
                    active = null;
                    activeAge = 0;
                    PdaEmbeddedAudio.stopVoice();
                    PdaNarrationController.stop();
                }
            }
            // Long-form dossier/NPC narration has priority. Closing/changing the
            // page calls PdaNarrationController.stop(), after which queued alerts resume.
            if (active == null && !QUEUE.isEmpty() && !PdaNarrationController.isSpeaking()) startNext(minecraft);
        }
    }

    private static void startNext(Minecraft minecraft) {
        active = QUEUE.removeFirst();
        activeAge = 0;
        if ("database_ready".equals(active.lineId()) || "field_link".equals(active.lineId())) {
            PdaEmbeddedAudio.playUi("ui_startup");
        } else {
            PdaEmbeddedAudio.playUi("ui_record");
        }
        int offlineDuration = PdaEmbeddedAudio.playVoice(active.lineId());
        if (offlineDuration > 0) {
            activeTicks = offlineDuration + 14;
            return;
        }
        if (PdaVoiceSettings.isEnabled() && minecraft.getNarrator().isActive()) {
            PdaNarrationController.read("P D A. " + active.text());
        }
        activeTicks = Math.max(70, Math.min(260, 30 + active.text().length() * 2));
    }

    private static int priority(String lineId) {
        return switch (lineId) {
            case "closed_loop" -> 10;
            case "icarus_warning", "anomaly_warning", "signal_echo" -> 8;
            case "orpheus_security", "matter_resonance" -> 7;
            case "reconstruction_complete", "pressure_warning", "structural_warning",
                    "field_liaison", "synthetic_liaison", "incident_analyst" -> 6;
            case "field_link", "synthetic_contact" -> 5;
            case "record_recovered" -> 3;
            default -> 1;
        };
    }

    private static String safe(String value) { return value == null ? "" : value; }
}
