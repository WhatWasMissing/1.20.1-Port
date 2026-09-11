package matteroverdrive.entity;

/** Small, persisted encounter identity used by facility security and squad logic. */
public enum EncounterFaction {
    ROGUE_ANDROID("rogue_android"),
    SYNTHETIC_SECURITY("synthetic_security"),
    RESEARCH_CONTAINMENT("research_containment"),
    BLACK_SITE("black_site");

    private final String id;
    EncounterFaction(String id) { this.id = id; }
    public String id() { return id; }
    public String displayName() {
        return switch (this) {
            case ROGUE_ANDROID -> "Rogue Android";
            case SYNTHETIC_SECURITY -> "Synthetic Security";
            case RESEARCH_CONTAINMENT -> "Research Containment";
            case BLACK_SITE -> "Black Site";
        };
    }

    public static EncounterFaction fromFacilityProfile(String profile) {
        return switch (profile == null ? "" : profile) {
            case "fusion_research_complex" -> RESEARCH_CONTAINMENT;
            case "black_site" -> BLACK_SITE;
            case "synthetic_manufacturing_plant", "matter_refinery", "quantum_relay_station", "android_command_bunker" -> SYNTHETIC_SECURITY;
            default -> ROGUE_ANDROID;
        };
    }

    public static EncounterFaction fromId(String id) {
        for (EncounterFaction faction : values()) if (faction.id.equals(id)) return faction;
        return ROGUE_ANDROID;
    }
}
