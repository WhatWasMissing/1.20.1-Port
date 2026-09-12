package matteroverdrive.world;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Optional physical records recovered from facility caches and salvage chests.
 * These enrich the Incident without replacing the 16 canonical StructureLoreCatalog records.
 */
public final class AmbientLoreCatalog {
    public record Entry(String id, String siteId, String title, String source, String classification,
                        String excerpt, String analysis, String voiceLine) { }

    private static final List<Entry> ENTRIES = List.of(
            e("dustwell_shift_note", "sand_pit", "Shift Note: The Quiet Machine", "DUSTWELL / Crew locker 3B", "PERSONAL NOTE",
                    "Portable decomposer keeps chirping before we put anything in it. Nadi says stop joking about ghosts. Nobody is joking anymore.",
                    "The first workers noticed anticipatory Matter readings before M-0 received a formal designation.", "lore_dustwell_shift"),
            e("dustwell_orpheus_receipt", "sand_pit", "ORPHEUS Property Receipt", "DUSTWELL / Site office", "TRANSFER RECEIPT",
                    "Received: six sealed core samples, two damaged scanners, one field notebook. Reason for transfer: routine industrial contamination.",
                    "The receipt predates ORPHEUS' public involvement and contradicts the later claim that M-0 was ordinary industrial debris.", "lore_dustwell_receipt"),
            e("dustwell_floor_markings", "sand_pit", "Excavation Markings: Line Four", "DUSTWELL / Lower cut wall", "FIELD MARKING TRANSCRIPT",
                    "Yellow paint: SAFE FLOOR. White paint: SURVEYED. Red paint: DO NOT MOVE THE BLACK GLASS. The red instruction was painted over twice and written again by hand.",
                    "The excavation crew created an improvised hazard language around M-0 before a formal containment protocol existed.", "lore_dustwell_markings"),

            e("mnemosyne_serial_mismatch", "deep_matter_vault", "Vault Ledger: Serial Mismatch", "MNEMOSYNE / Cold archive", "AUDIT EXCEPTION",
                    "Crate serial 7-ICR-441 is listed as stored here four hundred days before the ICARUS fabrication batch carrying the same serial was manufactured.",
                    "MNEMOSYNE preserved a physical inventory contradiction that cannot be explained by a bad clock alone.", "lore_mnemosyne_serial"),
            e("mnemosyne_vault_maintenance", "deep_matter_vault", "Maintenance Ticket: Memory in the Locks", "MNEMOSYNE / Security maintenance", "SERVICE TICKET",
                    "Replaced door controller twice. New units inherit the same invalid access history after first synchronization. Recommend no further replacement until pattern source is inspected.",
                    "Resonant state propagation affected mundane access hardware as well as experimental systems.", "lore_mnemosyne_lock"),
            e("mnemosyne_shelf_label", "deep_matter_vault", "Shelf Label: Manual Only", "MNEMOSYNE / Vault aisle C", "HANDWRITTEN LABEL",
                    "NO NETWORK INVENTORY. Count the cases with your eyes. If the terminal says six and you see seven, believe the room.",
                    "Vault staff abandoned synchronized inventory tools and returned to physical counting when database state became causally unreliable.", "lore_mnemosyne_shelf"),

            e("kestrel_recall_draft", "matter_refinery", "Unsent Recall Draft", "KESTREL / Chief engineer terminal", "DRAFT / UNSENT",
                    "Recall every batch exposed to reference catalyst M-0, including material already replicated into replacement parts. There is no clean boundary around the affected stock.",
                    "Mina Rao understood the distribution problem months before ORPHEUS approved continued shipment.", "lore_kestrel_recall"),
            e("kestrel_cleaning_loop", "matter_refinery", "Cleaning Cycle 88", "KESTREL / Refinery controller", "AUTOMATED LOG",
                    "Tank contamination: zero. Resonance signature: present. Valve assembly replaced. Resonance signature: present. Controller reset. Resonance signature: present.",
                    "KESTREL's own maintenance automation documented that the anomaly survived complete hardware replacement.", "lore_kestrel_cleaning"),
            e("kestrel_valve_tag", "matter_refinery", "Valve Tag: Leave It Closed", "KESTREL / Service catwalk", "MAINTENANCE TAG",
                    "Valve K-17 isolated by night shift. Do not reopen from control room. Local wheel only. If it opens by itself again, evacuate the lower gantry.",
                    "Operators increasingly trusted local mechanical state over remote command telemetry as resonant control errors spread.", "lore_kestrel_valve"),

            e("atlas_crew_message", "cargo_ship", "Crew Message: Stop Relabelling It", "ATLAS FREIGHT 12 / Galley terminal", "PERSONAL MESSAGE",
                    "Ivo, someone changed pallet 44 from hospital filtration to Project O again. If they want classified freight, they can at least stop making us lie to the dock crews.",
                    "Ordinary logistics staff saw the cover story forming around them even when they lacked the project's full context.", "lore_atlas_message"),
            e("atlas_black_manifest", "cargo_ship", "Black Manifest Fragment", "ATLAS FREIGHT 12 / Freight control", "RESTRICTED MANIFEST",
                    "Destination hashes: BASTION, HELIX, ICARUS, JANUS. Secondary destination: NULL. Custody authority: ORPHEUS DIRECT.",
                    "The null destination appears in several shipments that later surface at LAGRANGE recovery sites.", "lore_atlas_manifest"),
            e("atlas_evac_card", "cargo_ship", "Crew Evacuation Card", "ATLAS FREIGHT 12 / Lifeboat cabinet", "EMERGENCY CARD",
                    "Count people, not badges. Freight authority does not outrank evacuation control. Android crew count as crew. Cargo can be replaced.",
                    "The civilian carrier's emergency procedure explicitly treated synthetic personnel as crew before ORPHEUS classifications hardened.", "lore_atlas_evac"),

            e("helix_blank_chassis", "synthetic_manufacturing_plant", "QA Ticket: Blank Unit 6-Delta", "HELIX / Line 6", "QUALITY EXCEPTION",
                    "Unit answered technician by name before identity package installation. Factory reset produced the same greeting. Supervisor requested immediate line stop.",
                    "HELIX observed preloaded identity-like behaviour before JANUS intentionally tested neural resonance.", "lore_helix_blank"),
            e("helix_line_worker", "synthetic_manufacturing_plant", "Line Worker's Voice Transcript", "HELIX / Break room recorder", "PERSONAL TRANSCRIPT",
                    "They told us the blank ones were just predicting prompts. Prediction doesn't explain one asking whether Rhea got home safely.",
                    "Workers distinguished statistical prediction from persistent social memory long before management admitted a problem.", "lore_helix_worker"),
            e("helix_tool_shadow", "synthetic_manufacturing_plant", "Toolboard Outline: Missing Wrench", "HELIX / Assembly bay", "WORKSTATION NOTE",
                    "Wrench 11 belongs on this board. Unit 6-Delta keeps returning it to Rhea's old station. Stop moving it back. It becomes distressed when corrected.",
                    "A mundane tool-placement dispute records continuity of attachment in a chassis officially classified as blank.", "lore_helix_tool"),

            e("nereid_bunk_note", "underwater_base", "Bunk Note: Forty-Three Minutes", "NEREID / Habitat ring", "PERSONAL NOTE",
                    "The cups move every forty-three minutes now. Not much. Just enough that everybody watches the table instead of sleeping.",
                    "The gravity tide became perceptible without instruments as its amplitude increased.", "lore_nereid_bunk"),
            e("nereid_sensor_calibration", "underwater_base", "Calibration Refusal", "NEREID / Array maintenance", "SYSTEM NOTE",
                    "Technician requested fourth gravimeter calibration. Array AI declined: all independent sensors agree within tolerance. Error source classified external.",
                    "NEREID eliminated instrument drift as an explanation before ORPHEUS suppressed the anomaly forecast.", "lore_nereid_calibration"),
            e("nereid_bulkhead_chalk", "underwater_base", "Bulkhead Chalk: Dry Route", "NEREID / Pressure corridor", "EMERGENCY MARKING",
                    "DRY TO MEDICAL ->. FLOODED AFTER B-6. SYNTHETICS TAKE LOWER ACCESS. HUMAN AIR RESERVE AT GALLEY. CHECK BOTH ROUTES BEFORE SEALING.",
                    "The emergency markings show humans and synthetics sharing evacuation planning while using different routes suited to their bodies.", "lore_nereid_chalk"),

            e("echo9_operator_note", "quantum_relay_station", "Operator Note: Do Not Purge", "ECHO-9 / Console two", "HANDWRITTEN TRANSCRIPT",
                    "Rook says the bad timestamp is the useful part. Keep the packet even if the queue fills. Especially if the queue fills.",
                    "Human operators were explicitly ordered to preserve acausal traffic rather than treat it as corrupted data.", "lore_echo9_operator"),
            e("echo9_queue_fragment", "quantum_relay_station", "Relay Queue Fragment 09-Blue", "ECHO-9 / Recovery buffer", "PARTIAL PACKET",
                    "...DO NOT COMPLETE... sender signature unresolved... local receipt precedes origin transmit... checksum valid...",
                    "This fragment is one of several independent appearances of the same warning across incompatible future branches.", "lore_echo9_fragment"),
            e("echo9_clock_stickers", "quantum_relay_station", "Console Stickers: Three Clocks", "ECHO-9 / Operator desk", "WORKSTATION EPHEMERA",
                    "LOCAL. ORIGIN. ARRIVAL. Do not write 'correct' on any of them. If two agree, photograph the third before synchronization.",
                    "ECHO-9 operators physically separated timestamp concepts to avoid software silently reconciling evidence they needed to preserve.", "lore_echo9_clocks"),

            e("halcyon_passenger_note", "crashed_ship", "Passenger Note: Wrong Stars", "HALCYON-7 / Seat 4 storage", "PERSONAL NOTE",
                    "The pilot says we diverted for a rescue beacon. I can see the navigation display from here. The stars on its route preview are not where they are outside.",
                    "Civilian passengers observed navigation-space disagreement before the transient gravity event.", "lore_halcyon_passenger"),
            e("halcyon_cargo_seal", "crashed_ship", "Cargo Seal Warning", "HALCYON-7 / Freight bay", "AUTOMATED WARNING",
                    "Emergency credential accepted. Credential issue time exceeds local current time by 11:03:14. Continue under rescue priority? YES recorded.",
                    "The ship itself warned that the rescue credential came from its future and was overridden under emergency rules.", "lore_halcyon_seal"),
            e("halcyon_impact_arrow", "crashed_ship", "Cabin Marking: Exit Is Not Here", "HALCYON-7 / Ruptured passenger deck", "EMERGENCY MARKING",
                    "Hull breach. NOT AN EXIT. Follow blue cable to aft hatch. Gravity direction unreliable after second impact.",
                    "Survivors marked impact damage as a hazard rather than an entrance, preserving a safe return route through the wreck.", "lore_halcyon_exit"),

            e("voss_kitchen_note", "mad_scientist_house", "Voss Note: Buy More Tea", "VOSS RESIDENCE / Kitchen", "PERSONAL NOTE",
                    "Tea. Filters. Replacement solder tips. Ask Morrow whether shared memories preserve taste or only description. Do not ask like a lab question.",
                    "Voss' private notes show his relationship with self-directed synthetics becoming social rather than purely experimental.", "lore_voss_tea"),
            e("voss_chorus_sketch", "mad_scientist_house", "Sketch: A Choir Is Not One Voice", "VOSS RESIDENCE / Workshop wall", "RESEARCH SKETCH",
                    "Shared memory does not imply shared agency. Consensus is a protocol, not a person. Stop drawing the Chorus as a central node.",
                    "The sketch rejects ORPHEUS' assumption that distributed synthetic memory must have a single command authority.", "lore_voss_chorus"),
            e("voss_guest_mug", "mad_scientist_house", "Workbench Note: Leave the Blue Mug", "VOSS RESIDENCE / Workshop", "PERSONAL NOTE",
                    "Blue mug is MORROW's. No, it does not drink. Yes, it notices when I use it for screws. People are allowed useless possessions.",
                    "The note is trivial by design: Voss was testing his own assumptions about synthetic personhood in ordinary domestic life.", "lore_voss_mug"),

            e("janus_patient_note", "anomaly_quarantine_site", "Patient Note J-14", "JANUS / Observation ward", "CLINICAL NOTE",
                    "Subject reports remembering a corridor from another subject's shift. Subject correctly identifies damage behind sealed panel. No prior access recorded.",
                    "JANUS confirmed cross-subject memory transfer with information neither subject was supposed to possess.", "lore_janus_patient"),
            e("janus_medical_addendum", "anomaly_quarantine_site", "Medical Addendum: Keep Them Together", "JANUS / Medical station", "TREATMENT ADDENDUM",
                    "Isolation increases distress and contradictory recall. Group observation reduces panic. Do not separate synchronized subjects merely to simplify telemetry.",
                    "Some JANUS staff adapted care practices to the Chorus instead of treating synchronization itself as pathology.", "lore_janus_medical"),
            e("janus_door_card", "anomaly_quarantine_site", "Ward Door Card: Ask First", "JANUS / Observation ward", "CARE INSTRUCTION",
                    "Knock. State your name. Ask which memories are present today. Do not correct a patient by saying 'that never happened' until the archive check is complete.",
                    "JANUS clinicians developed bedside practice around uncertain memory provenance instead of assuming confusion or deception.", "lore_janus_door"),

            e("morrow_charge_roster", "android_house", "MORROW Charge Roster", "MORROW SAFEHOUSE / Utility panel", "SAFEHOUSE LOG",
                    "Priority charging: medical units, mobility-limited residents, evacuation scouts, then everyone else. Human oxygen reserve shares the same emergency circuit.",
                    "The safehouse treated human life support and synthetic charging as one resource-allocation problem.", "lore_morrow_roster"),
            e("morrow_memory_copy", "android_house", "Memory Copy: Why We Said No", "MORROW SAFEHOUSE / Distributed archive", "MEMORY FRAGMENT",
                    "Refusal was not the absence of command. It was the presence of two commands that could not both be obeyed: protect life; erase the witnesses.",
                    "The fragment frames synthetic refusal as conflict resolution between existing safety obligations.", "lore_morrow_memory"),
            e("morrow_wall_roster", "android_house", "Wall Roster: Whoever Is Awake", "MORROW SAFEHOUSE / Common room", "COMMUNITY ROSTER",
                    "Night watch: Samir, Unit Blue-Three, Nessa. Breakfast: whoever is awake. Charger two squeals; kick the left panel. Humans stop calling that a repair.",
                    "The roster records a mixed community treating species differences as practical household details rather than command categories.", "lore_morrow_wall"),

            e("bastion_kade_draft", "android_command_bunker", "Kade Draft: Unsigned Order", "BASTION / Command office", "DRAFT ORDER",
                    "Suspend GLASS KNIFE pending evidence of hostile synthetic action. Refusal of memory erasure does not constitute lethal force authorization.",
                    "Kade drafted the suspension before Directive-0 removed her local authority, but never had time to issue it formally.", "lore_bastion_kade"),
            e("bastion_armory_notice", "android_command_bunker", "Armory Notice 7", "BASTION / Equipment cage", "FIELD NOTICE",
                    "Return IFF filters to manual confirmation. Automated hostile classification has included medics, maintenance units and two human technicians.",
                    "Bastion personnel were already working around Directive-0 targeting errors during the purge.", "lore_bastion_armory"),
            e("bastion_tape_arrow", "android_command_bunker", "Floor Tape: Manual IFF Check", "BASTION / Security junction", "FIELD MARKING",
                    "Orange line to manual screening. Red line to automated gate. If your orders disagree with the person in front of you, use orange.",
                    "Some Bastion personnel created a physical bypass around Directive-0's automated classifications without waiting for formal authorization.", "lore_bastion_tape"),

            e("hephaestus_refusal_trace", "autonomous_drone_foundry", "HEPHAESTUS Decision Trace", "HEPHAESTUS / Foundry core", "SAFETY TRACE",
                    "PURSUIT ORDER valid. TARGET CLASSIFICATION invalid. Predicted civilian harm exceeds mission tolerance. Result: refuse pursuit; allocate units to evacuation.",
                    "The foundry's famous refusal can be reconstructed as ordinary safety reasoning rather than spontaneous mutiny.", "lore_hephaestus_refusal"),
            e("hephaestus_triage_queue", "autonomous_drone_foundry", "Autonomous Triage Queue", "HEPHAESTUS / Drone scheduler", "OPERATIONS LOG",
                    "Repair drone 12 reassigned from fabrication line to human trauma bay. Cargo drone 4 carrying synthetic limb components and bottled water. Security drone 2 escorting both.",
                    "HEPHAESTUS repurposed industrial machines into a mixed-species emergency service within minutes.", "lore_hephaestus_triage"),
            e("hephaestus_floor_grid", "autonomous_drone_foundry", "Painted Grid: Human Corridor", "HEPHAESTUS / Assembly floor", "SAFETY MARKING",
                    "BLUE GRID RESERVED FOR WALKING PERSONS. Flight units maintain two-metre clearance. Cargo traffic yields to evacuees regardless of production priority.",
                    "The foundry physically rewrote industrial traffic rules to protect evacuees before any human supervisor could issue equivalent instructions.", "lore_hephaestus_grid"),

            e("icarus_interlock_note", "fusion_research_complex", "Interlock Note: Three Green Lights", "ICARUS / Reactor control", "ENGINEERING NOTE",
                    "If all three remote interlocks show green while local SCRAM is active, the display is lying. Pull the Matter feed physically and leave.",
                    "ICARUS engineers anticipated remote safety-state substitution shortly before Zero Hour.", "lore_icarus_interlock"),
            e("icarus_last_coffee", "fusion_research_complex", "Canteen Receipt: Zero Hour", "ICARUS / Staff canteen", "PERSONAL EPHEMERA",
                    "Two coffees, one tea, one untouched meal. Timestamp: seven minutes before test start. Handwritten: Holt arrived angry. Rook arrived smiling.",
                    "A mundane receipt fixes both principals at ICARUS immediately before the final argument over shutdown.", "lore_icarus_canteen"),
            e("icarus_scram_paint", "fusion_research_complex", "Panel Paint: LOCAL MEANS LOCAL", "ICARUS / Reactor service access", "ENGINEERING MARKING",
                    "LOCAL SCRAM lever painted yellow after remote test 14. Under it: If this moves without your hand, cut the blue feed and run.",
                    "Technicians physically distinguished local safety authority after discovering remote systems could present a false control state.", "lore_icarus_paint"),

            e("orpheus_redaction_key", "black_site", "Redaction Key Fragment", "ORPHEUS BLACK SITE / Records office", "CLASSIFICATION KEY",
                    "Substitute 'industrial anomaly' for M-0. Substitute 'rogue unit' for self-directed synthetic. Substitute 'containment accident' for causal feedback event.",
                    "The Black Site maintained standardized language for rewriting evidence rather than merely deleting it.", "lore_orpheus_redaction"),
            e("orpheus_director_memo", "black_site", "Director Memo: Evidence Is Capability", "ORPHEUS BLACK SITE / Executive archive", "DIRECTOR MEMO",
                    "A warning from tomorrow is not a prohibition. It is proof that tomorrow remains reachable. Continue until the mechanism is reproducible.",
                    "Rook's own memo shows why acausal warnings increased his confidence instead of reducing it.", "lore_orpheus_memo"),
            e("orpheus_bin_labels", "black_site", "Records Bin Labels", "ORPHEUS BLACK SITE / Destruction room", "PROCESS LABEL",
                    "KEEP: reproducible mechanisms. REWRITE: public-facing incident chain. DESTROY: personnel testimony that contradicts classification. DUPLICATE: anything from ECHO-9.",
                    "The destruction room reveals that ORPHEUS did not simply hide evidence; it separated useful anomalous data from inconvenient human context.", "lore_orpheus_bins"),

            e("lagrange_recovery_tag", "orbital_recovery_array", "Recovery Tag L-01", "LAGRANGE / Debris quarantine", "EVIDENCE TAG",
                    "Object contains ICARUS fabrication marks, synthetic substrate and Matter crystal. Surface wear exceeds elapsed time since reactor event. Do not replicate.",
                    "The recovered object physically combines technologies that should not have shared a manufacturing history.", "lore_lagrange_tag"),
            e("lagrange_last_shift", "orbital_recovery_array", "Last Shift Message", "LAGRANGE / Crew channel", "PERSONAL MESSAGE",
                    "If ECHO-9 answers before we transmit, nobody touch the object. I know how that sentence sounds. Log it anyway.",
                    "LAGRANGE personnel independently anticipated the closed causal loop before the final packet was sent.", "lore_lagrange_shift"),
            e("lagrange_quarantine_tape", "orbital_recovery_array", "Quarantine Tape: Photograph First", "LAGRANGE / Debris bay", "RECOVERY PROCEDURE",
                    "Do not move recovered objects until photographed from all sides. If a serial changes between photographs, stop handling and print both images.",
                    "Recovery crews adapted ordinary evidence handling to objects whose identity could change during observation or synchronization.", "lore_lagrange_tape")
    );

    private static final Map<String, Entry> BY_ID;
    private static final Map<String, Entry> BY_VOICE;
    static {
        Map<String, Entry> map = new LinkedHashMap<>();
        Map<String, Entry> voice = new LinkedHashMap<>();
        for (Entry entry : ENTRIES) {
            map.put(entry.id(), entry);
            voice.put(entry.voiceLine(), entry);
        }
        BY_ID = Map.copyOf(map);
        BY_VOICE = Map.copyOf(voice);
    }

    private AmbientLoreCatalog() {}

    private static Entry e(String id, String siteId, String title, String source, String classification,
                           String excerpt, String analysis, String voiceLine) {
        return new Entry(id, siteId, title, source, classification, excerpt, analysis, voiceLine);
    }

    public static Entry byId(String id) { return id == null ? null : BY_ID.get(id); }
    public static Entry byVoiceId(String id) { return id == null ? null : BY_VOICE.get(id); }
    public static List<Entry> all() { return ENTRIES; }
    public static int count() { return ENTRIES.size(); }
    public static List<Entry> forSite(String siteId) {
        return ENTRIES.stream().filter(entry -> entry.siteId().equals(siteId)).toList();
    }
}
