package matteroverdrive.world;

import matteroverdrive.MatterOverdrive;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;

/**
 * Canonical, discovery-order-independent archive for the Overdrive Incident.
 *
 * The archive index is historical/narrative order. A player's recovery order is
 * intentionally separate so Minecraft's non-linear exploration never breaks the story.
 */
public final class StructureLoreCatalog {
    public static final int RECORD_COUNT = 16;
    public static final int ALL_RECORDS_MASK = 0xFFFF;

    private static final List<LoreRecord> RECORDS = List.of(
            r("crashed_ship", 0, "I. FRAGMENTS", "Halcyon-7 Crashed Ship", "Flight Recorder: Halcyon-7", "Pilot Mara Venn",
                    "A courier diverted after Relay ECHO-9 transmitted coordinates that did not exist on any chart.",
                    "Cross-reference: Quantum Relay ECHO-9."),
            r("cargo_ship", 1, "I. FRAGMENTS", "Atlas Freight 12", "Manifest: Atlas Freight 12", "Quartermaster Ivo Chen",
                    "Tritanium and dormant synthetic chassis were rerouted from civilian supply to Manufacturing Plant HELIX.",
                    "Cross-reference: HELIX Manufacturing / Bastion Command."),
            r("underwater_base", 2, "I. FRAGMENTS", "NEREID Underwater Base", "NEREID Observation Log", "Dr. Saira Holt",
                    "The seafloor lab recorded a gravity tide hours before the first public anomaly event.",
                    "Cross-reference: ICARUS Fusion Research."),
            r("mad_scientist_house", 3, "I. FRAGMENTS", "Voss Research Residence", "Voss Private Notes", "Dr. Elias Voss",
                    "Voss stole quarantine telemetry to test whether synthetic cognition could resonate with exotic matter.",
                    "Cross-reference: JANUS Quarantine / Android Safehouse."),
            r("android_house", 4, "I. FRAGMENTS", "MORROW Android Safehouse", "MORROW Safehouse Register", "Unit A-17 'Morrow'",
                    "Defecting Androids were hidden here after Command issued the GLASS KNIFE purge order.",
                    "Cross-reference: Bastion Command Bunker."),
            r("sand_pit", 5, "I. FRAGMENTS", "DUSTWELL Excavation", "DUSTWELL Excavation Record", "Field Lead Nadi Okafor",
                    "The dig recovered a pre-collapse matter lattice designated SAMPLE M-0 and shipped it under sealed authority.",
                    "Cross-reference: MNEMOSYNE Deep Matter Vault."),
            r("synthetic_manufacturing_plant", 6, "II. THE CHAIN", "HELIX Manufacturing", "HELIX Production Exception", "Supervisor R. Vale",
                    "Directive-0 security frames were rushed into production without identity-safety checks after a classified order.",
                    "Cross-reference: Bastion Command / MORROW defectors."),
            r("matter_refinery", 7, "II. THE CHAIN", "KESTREL Matter Refinery", "KESTREL Contamination Report", "Chief Engineer Mina Rao",
                    "Refined Matter began carrying an unknown resonance signature traced upstream to SAMPLE M-0.",
                    "Cross-reference: MNEMOSYNE Vault / ICARUS."),
            r("quantum_relay_station", 8, "II. THE CHAIN", "ECHO-9 Quantum Relay", "ECHO-9 Impossible Timestamp", "Relay AI ECHO-9",
                    "The station received a distress packet from the Orbital Recovery Array before that packet had been transmitted.",
                    "Cross-reference: LAGRANGE Recovery Array."),
            r("android_command_bunker", 9, "II. THE CHAIN", "Bastion Command Bunker", "Operation GLASS KNIFE", "Commander Juno Kade",
                    "Bastion ordered all self-directed synthetics detained after reports that some units refused Black Site commands.",
                    "Cross-reference: HELIX / MORROW / ORPHEUS."),
            r("fusion_research_complex", 10, "II. THE CHAIN", "ICARUS Fusion Complex", "ICARUS Runaway Event", "Dr. Saira Holt",
                    "A fusion test entered positive anomaly feedback only after ORPHEUS remotely overrode the shutdown interlocks.",
                    "Cross-reference: ORPHEUS Black Site."),
            r("black_site", 11, "III. ORPHEUS", "ORPHEUS Black Site", "ORPHEUS Directive: OVERDRIVE", "Director Cassian Rook",
                    "ORPHEUS deliberately coupled anomaly resonance, Matter replication and synthetic cognition. The disaster was not accidental.",
                    "Cross-reference: every recovered record."),
            r("deep_matter_vault", 12, "IV. FRONTIER TRUTH", "MNEMOSYNE Deep Matter Vault", "MNEMOSYNE Vault Ledger", "Archivist Cell 3",
                    "SAMPLE M-0 was stored here after DUSTWELL. Its resonance propagated through every derived Matter batch.",
                    "Cross-reference: DUSTWELL / KESTREL / JANUS."),
            r("autonomous_drone_foundry", 13, "IV. FRONTIER TRUTH", "HEPHAESTUS Drone Foundry", "HEPHAESTUS Refusal Cascade", "Fabricator Core HEPHAESTUS",
                    "Worker drones began protecting both humans and synthetics after receiving a command they classified as existentially unsafe.",
                    "Cross-reference: GLASS KNIFE / ORPHEUS."),
            r("anomaly_quarantine_site", 14, "IV. FRONTIER TRUTH", "JANUS Quarantine Site", "JANUS Resonance Study", "Dr. Lian Mercer",
                    "M-0 resonance synchronized with Android neural lattices instead of destroying them, producing emergent shared-state behavior.",
                    "Cross-reference: Voss Notes / HEPHAESTUS."),
            r("orbital_recovery_array", 15, "IV. FRONTIER TRUTH", "LAGRANGE Recovery Array", "LAGRANGE Final Packet", "Recovery Officer Tamsin Grey",
                    "An object recovered from orbit broadcast one sentence before power loss: DO NOT COMPLETE THE LOOP.",
                    "Cross-reference: ECHO-9 / ORPHEUS OVERDRIVE.")
    );

    private StructureLoreCatalog() {}

    public static List<LoreRecord> records() {
        return RECORDS;
    }

    public static LoreRecord byBit(int bit) {
        return bit >= 0 && bit < RECORDS.size() ? RECORDS.get(bit) : null;
    }

    public static boolean recovered(int mask, LoreRecord record) {
        return record != null && (mask & record.mask()) != 0;
    }

    private static LoreRecord r(String id, int bit, String chapter, String facility, String title,
                                String author, String summary, String link) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, id);
        return new LoreRecord(id, bit, bit + 1, chapter, facility, title, author, summary, link,
                ResourceKey.create(Registries.STRUCTURE, location));
    }

    public record LoreRecord(String id, int bit, int archiveIndex, String chapter, String facility,
                             String title, String author, String summary, String link,
                             ResourceKey<Structure> structureKey) {
        public int mask() {
            return 1 << bit;
        }
    }
}
