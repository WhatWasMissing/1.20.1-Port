package matteroverdrive.world;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Map;

/**
 * Lightweight environmental interpretation for already-generated facility spaces.
 * These observations add meaning to scenery without placing blocks or changing traversal.
 */
public final class EnvironmentalStorytellingCatalog {
    private static final Map<String, List<String>> OBSERVATIONS = Map.ofEntries(
            Map.entry("sand_pit", List.of(
                    "Layered hazard paint has been reapplied around the deepest cut. The oldest warning is underneath the newer ORPHEUS markings.",
                    "Several survey stakes point away from the ore seam and toward the same patch of black-silver material.",
                    "Tool storage is intact. Sample handling equipment is not. The evacuation appears to have prioritized evidence over machinery.")),
            Map.entry("deep_matter_vault", List.of(
                    "Shelf numbers are handwritten over disabled inventory terminals. Staff stopped trusting synchronized stock counts.",
                    "Several sealed cases have duplicate serial labels with different dates.",
                    "Manual tally marks continue past the final database audit entry.")),
            Map.entry("matter_refinery", List.of(
                    "A service valve is tagged for local operation only. Remote-control identifiers have been physically scratched out.",
                    "Replacement hardware carries the same resonance-warning marks as the components it replaced.",
                    "Maintenance paths are cleaner than the main processing floor, suggesting repeated emergency access after production stopped.")),
            Map.entry("cargo_ship", List.of(
                    "Emergency routing treats synthetic crew as personnel, not cargo. Later ORPHEUS manifests use different language.",
                    "Freight seals show repeated destination relabeling without matching physical handling records.",
                    "Evacuation markings lead around the classified cargo bay rather than through it.")),
            Map.entry("synthetic_manufacturing_plant", List.of(
                    "Tool shadows remain labelled by technician rather than production line. One workstation has been kept deliberately untouched.",
                    "Several chassis bays are marked BLANK, then crossed out by hand.",
                    "Assembly-line safety markings redirect workers around a station that official records describe as operational.")),
            Map.entry("underwater_base", List.of(
                    "Chalk routes distinguish dry human evacuation paths from lower synthetic access routes, then reconnect at medical.",
                    "Loose objects were strapped down in living areas that were not designed for rough seas.",
                    "Pressure-door annotations are newer than the facility's final maintenance stamp.")),
            Map.entry("quantum_relay_station", List.of(
                    "Three clocks are labelled LOCAL, ORIGIN and ARRIVAL. None is labelled correct.",
                    "Operators used physical note cards beside terminals to preserve timestamps before synchronization could rewrite them.",
                    "A relay buffer has been isolated from automatic cleanup despite being nearly full.")),
            Map.entry("crashed_ship", List.of(
                    "A hull rupture is explicitly marked NOT AN EXIT. Blue emergency cable identifies the intended return path.",
                    "Cabin restraints show the ship experienced a gravity change before impact, not only deceleration.",
                    "The navigation display housing is more damaged from manual access than from the crash itself.")),
            Map.entry("mad_scientist_house", List.of(
                    "Workshop notes mix research questions with groceries and household reminders. The distinction between subject and colleague had already eroded.",
                    "A labelled mug has been kept beside a synthetic maintenance station despite serving no technical purpose.",
                    "Network sketches repeatedly remove a central Chorus node and replace it with peer-to-peer links.")),
            Map.entry("anomaly_quarantine_site", List.of(
                    "Ward instructions tell staff to verify contradictory memories before correcting the patient.",
                    "Observation rooms were rearranged for groups rather than isolation shortly before the site's closure.",
                    "Medical tags distinguish resonance symptoms from ordinary confusion instead of treating them as the same condition.")),
            Map.entry("android_house", List.of(
                    "Human oxygen reserves and synthetic charging schedules share the same emergency board.",
                    "Household duty rosters mix human names and unit identifiers without separate command categories.",
                    "Repairs are improvised, domestic, and repeatedly annotated by different hands and chassis.")),
            Map.entry("android_command_bunker", List.of(
                    "Orange floor tape bypasses automated screening and leads to a manual identification station.",
                    "Several weapon racks carry orders to confirm targets visually before accepting Directive-zero classification.",
                    "Command signage changes from ORPHEUS terminology to local emergency language deeper inside the bunker.")),
            Map.entry("autonomous_drone_foundry", List.of(
                    "Factory traffic markings reserve a human walking corridor through what was originally an autonomous work zone.",
                    "Cargo routes have been repainted around evacuation and medical staging areas.",
                    "Maintenance drones were configured for field triage using ordinary industrial attachment points.")),
            Map.entry("fusion_research_complex", List.of(
                    "LOCAL SCRAM controls are painted more brightly than the surrounding console, as if added after construction.",
                    "Broken coil pedestals preserve a deliberate physical gap where remote-control hardware once connected.",
                    "Service routes point toward manual Matter-feed isolation rather than the central control room.")),
            Map.entry("black_site", List.of(
                    "Records bins separate KEEP, REWRITE and DESTROY rather than classified and unclassified material.",
                    "Some archive shelves have been emptied carefully while nearby testimony storage was physically damaged.",
                    "Security signage protects records-processing rooms as aggressively as experimental spaces.")),
            Map.entry("orbital_recovery_array", List.of(
                    "Evidence bays are marked PHOTOGRAPH BEFORE MOVING in multiple handwriting styles.",
                    "Recovered-object cradles include manual serial-number boards beside the electronic readers.",
                    "Quarantine procedure assumes an object's identity may change between observations, not merely its condition."))
    );

    private EnvironmentalStorytellingCatalog() {}

    public static String observation(String siteId, BlockPos position) {
        List<String> lines = OBSERVATIONS.get(siteId);
        if (lines == null || lines.isEmpty()) return "";
        int index = Math.floorMod(position.getX() * 31 + position.getY() * 17 + position.getZ() * 13, lines.size());
        return lines.get(index);
    }

    public static boolean hasSite(String siteId) {
        return siteId != null && OBSERVATIONS.containsKey(siteId);
    }
}
