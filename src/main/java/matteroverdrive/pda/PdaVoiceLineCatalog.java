package matteroverdrive.pda;

import java.util.LinkedHashMap;
import java.util.Map;

/** Original short-form PDA callouts for the Overdrive Incident campaign. */
public final class PdaVoiceLineCatalog {
    private static final Map<String, String> LINES = new LinkedHashMap<>();

    static {
        LINES.put("field_link",
                "Field link established. Matter Overdrive infrastructure has been detected in this world. Recover records, learn the machines, and decide how much of the incident should be rebuilt.");
        LINES.put("record_recovered",
                "New archive entry authenticated. Cross-reference added to the Overdrive Incident reconstruction.");
        LINES.put("reconstruction_complete",
                "Evidence threshold reached. A new incident reconstruction is available in your P D A.");
        LINES.put("matter_resonance",
                "Caution. M-zero resonance detected. Do not assume duplicated matter, synthetic memory, or impossible timestamps are causally local.");
        LINES.put("anomaly_warning",
                "Warning. Gravitational distortion is elevated. Keep a clear return route and avoid unshielded event-horizon exposure.");
        LINES.put("pressure_warning",
                "Caution. Pressure-compromised habitat detected. Bulkheads and flooded sections may no longer match the original emergency route.");
        LINES.put("structural_warning",
                "Structural integrity is degraded. Treat open hull sections as impact damage, not entrances, and verify a return path before descending.");
        LINES.put("signal_echo",
                "Acausal telemetry signature detected. Local timestamps may describe events that have not occurred yet. Archive the signal before trusting it.");
        LINES.put("orpheus_security",
                "ORPHEUS security protocol detected. Legacy authorization is still being enforced. The organization that issued it is not required to still exist.");
        LINES.put("icarus_warning",
                "ICARUS containment architecture identified. Historical records indicate the shutdown chain was overridden. Recreating the original configuration is not advised.");
        LINES.put("synthetic_contact",
                "Independent synthetic signatures detected. Not all Android networks are hostile. Confirm affiliation before engaging.");
        LINES.put("closed_loop",
                "Archive reconstruction complete. Causal origin unresolved. Standing instruction: do not complete the loop.");
        LINES.put("database_ready",
                "Personal data assistant online. Technical manual, recovered records, incident reconstructions, contracts, and field operations are available from the Data Pad.");
    }

    private PdaVoiceLineCatalog() {}

    public static String line(String id) {
        return LINES.getOrDefault(id == null ? "" : id, "");
    }

    public static boolean contains(String id) {
        return id != null && LINES.containsKey(id);
    }

    public static Map<String, String> all() {
        return Map.copyOf(LINES);
    }
}
