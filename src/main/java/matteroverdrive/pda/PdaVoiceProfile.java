package matteroverdrive.pda;

import java.util.Locale;

/**
 * Production/playback profile for an authored PDA callout. The spoken performance
 * remains natural; these profiles only select the restrained post-processing identity.
 */
public enum PdaVoiceProfile {
    STANDARD("standard"),
    HAZARD("hazard"),
    ANOMALY("anomaly"),
    ARCHIVE("archive"),
    ORPHEUS("orpheus"),
    SYNTHETIC("synthetic"),
    CORRUPTED("corrupted");

    private final String id;
    PdaVoiceProfile(String id) { this.id = id; }
    public String id() { return id; }

    public static PdaVoiceProfile forLine(String lineId) {
        String id = lineId == null ? "" : lineId.toLowerCase(Locale.ROOT);
        if (id.equals("closed_loop") || id.equals("signal_echo")
                || id.contains("echo9") || id.contains("lagrange")) return CORRUPTED;
        if (id.contains("orpheus") || id.contains("black_site")) return ORPHEUS;
        if (id.contains("anomaly") || id.contains("resonance") || id.contains("m0")) return ANOMALY;
        if (id.contains("synthetic") || id.contains("morrow") || id.contains("chorus")
                || id.contains("hephaestus")) return SYNTHETIC;
        if (id.contains("warning") || id.contains("pressure") || id.contains("structural")
                || id.contains("icarus")) return HAZARD;
        if (id.startsWith("lore_") || id.startsWith("site_") || id.contains("archive")
                || id.equals("record_recovered") || id.equals("reconstruction_complete")) return ARCHIVE;
        return STANDARD;
    }
}
