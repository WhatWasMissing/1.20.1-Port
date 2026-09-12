package matteroverdrive.pda;

import matteroverdrive.world.TechnologyLoreCatalog;

import java.util.LinkedHashMap;
import java.util.Map;

/** Original short-form PDA callouts plus dynamically-authored technology discovery callouts. */
public final class PdaVoiceLineCatalog {
    private static final Map<String, String> LINES = new LinkedHashMap<>();

    static {
        LINES.put("field_link", "Field link established. Matter Overdrive infrastructure has been detected in this world. Recover records, learn the machines, and decide how much of the incident should be rebuilt.");
        LINES.put("record_recovered", "New archive entry authenticated. Cross-reference added to the Overdrive Incident reconstruction.");
        LINES.put("reconstruction_complete", "Evidence threshold reached. A new incident reconstruction is available in your P D A.");
        LINES.put("matter_resonance", "Caution. M-zero resonance detected. Do not assume duplicated matter, synthetic memory, or impossible timestamps are causally local.");
        LINES.put("anomaly_warning", "Warning. Gravitational distortion is elevated. Keep a clear return route and avoid unshielded event-horizon exposure.");
        LINES.put("pressure_warning", "Caution. Pressure-compromised habitat detected. Bulkheads and flooded sections may no longer match the original emergency route.");
        LINES.put("structural_warning", "Structural integrity is degraded. Treat open hull sections as impact damage, not entrances, and verify a return path before descending.");
        LINES.put("signal_echo", "Acausal telemetry signature detected. Local timestamps may describe events that have not occurred yet. Archive the signal before trusting it.");
        LINES.put("orpheus_security", "ORPHEUS security protocol detected. Legacy authorization is still being enforced. The organization that issued it is not required to still exist.");
        LINES.put("icarus_warning", "ICARUS containment architecture identified. Historical records indicate the shutdown chain was overridden. Recreating the original configuration is not advised.");
        LINES.put("synthetic_contact", "Independent synthetic signatures detected. Not all Android networks are hostile. Confirm affiliation before engaging.");
        LINES.put("field_liaison", "Field team trust threshold reached. Recovery personnel have authorized access to less sanitized operational context.");
        LINES.put("synthetic_liaison", "Independent synthetic trust threshold reached. Contact classification updated. Affiliation should still be confirmed individually.");
        LINES.put("incident_analyst", "Archive context threshold reached. Simplified reconstruction filters have been reduced. Contradictory evidence will remain visible.");
        LINES.put("closed_loop", "Archive reconstruction complete. Causal origin unresolved. Standing instruction: do not complete the loop.");
        LINES.put("database_ready", "Personal data assistant online. Technical manual, recovered records, incident reconstructions, contracts, and field operations are available from the Data Pad.");

        LINES.put("site_dustwell", "DUSTWELL excavation identified. This is the earliest known recovery site for sample M-zero. Preserve field notes before disturbing the deepest material.");
        LINES.put("site_mnemosyne", "MNEMOSYNE deep archive identified. Inventory records here contain manufacturing dates that do not agree with the objects they describe.");
        LINES.put("site_kestrel", "KESTREL Matter refinery identified. Historical maintenance logs indicate the M-zero signature survived replacement of the hardware carrying it.");
        LINES.put("site_atlas", "Atlas Freight Twelve identified. Civilian cargo records may expose how classified OVERDRIVE material moved through ordinary supply chains.");
        LINES.put("site_helix", "HELIX manufacturing identified. Synthetic chassis anomalies were recorded here before the JANUS neural-resonance programme formally began.");
        LINES.put("site_nereid", "NEREID underwater observatory identified. Its gravity instruments detected the ICARUS event before the event occurred locally.");
        LINES.put("site_echo9", "ECHO-nine quantum relay identified. Authenticated packets in this station violate ordinary transmit and receive order. Treat timestamps as evidence, not metadata.");
        LINES.put("site_halcyon", "Halcyon-seven wreck identified. Flight data indicates the crew followed a rescue credential that had not yet been issued.");
        LINES.put("site_voss", "Voss research residence identified. Private records here contain the earliest surviving description of the Chorus as distributed memory without central command.");
        LINES.put("site_janus", "JANUS quarantine site identified. Clinical records document shared memories crossing between subjects without a conventional network path.");
        LINES.put("site_morrow", "MORROW safehouse identified. This site sheltered both synthetics and human personnel during GLASS KNIFE. Do not classify occupants by chassis alone.");
        LINES.put("site_bastion", "Bastion command bunker identified. Local command records contradict the official account of an unprovoked synthetic uprising.");
        LINES.put("site_hephaestus", "HEPHAESTUS autonomous foundry identified. Drone decision traces show evacuation and medical priorities replacing a valid pursuit order.");
        LINES.put("site_icarus", "ICARUS fusion complex identified. Zero Hour began here when resonant Matter, anomaly containment and synthetic cognition were coupled into one control state.");
        LINES.put("site_orpheus", "ORPHEUS Black Site identified. Expect deliberate redaction, substituted terminology, and security systems designed to protect the cover story as well as the facility.");
        LINES.put("site_lagrange", "LAGRANGE recovery array identified. Debris recovered here may be both evidence from the ICARUS event and part of the material chain that caused it.");

        LINES.put("lore_dustwell_shift", "Personnel note authenticated. DUSTWELL workers observed anticipatory decomposer readings before M-zero received a formal designation.");
        LINES.put("lore_dustwell_receipt", "Transfer record authenticated. ORPHEUS removed DUSTWELL samples under an industrial-contamination cover story before its public involvement began.");
        LINES.put("lore_mnemosyne_serial", "Vault audit authenticated. An ICARUS serial number appears in storage records dated before the corresponding fabrication batch existed.");
        LINES.put("lore_mnemosyne_lock", "Maintenance record authenticated. Replacement access controllers repeatedly reconstructed the same invalid history after synchronization.");
        LINES.put("lore_kestrel_recall", "Unsent recall draft authenticated. KESTREL engineering identified that resonant stock no longer had a clean containment boundary.");
        LINES.put("lore_kestrel_cleaning", "Automated cleaning log authenticated. The resonance signature survived tank cleaning, valve replacement, and controller reset.");
        LINES.put("lore_atlas_message", "Crew message authenticated. Civilian freight personnel recognized that Project O cargo was being hidden inside ordinary logistics.");
        LINES.put("lore_atlas_manifest", "Restricted manifest fragment authenticated. A null destination hash links concealed freight movements to later LAGRANGE recovery evidence.");
        LINES.put("lore_helix_blank", "Quality exception authenticated. A supposedly blank HELIX chassis identified staff before an identity package was installed.");
        LINES.put("lore_helix_worker", "Worker transcript authenticated. HELIX personnel distinguished persistent social memory from the prediction errors management claimed to be observing.");
        LINES.put("lore_nereid_bunk", "Habitat note authenticated. NEREID's forty-three-minute gravity cycle became strong enough to disturb unsecured objects without instrumentation.");
        LINES.put("lore_nereid_calibration", "Calibration record authenticated. Multiple independent NEREID sensors agreed that the gravity anomaly was external, not instrument drift.");
        LINES.put("lore_echo9_operator", "Operator note authenticated. ECHO-nine personnel were ordered to preserve impossible timestamps because ORPHEUS considered them experimental evidence.");
        LINES.put("lore_echo9_fragment", "Relay fragment authenticated. The phrase do not complete the loop appears again in traffic received before its recorded transmission.");
        LINES.put("lore_halcyon_passenger", "Passenger note authenticated. Halcyon-seven occupants observed disagreement between route-preview space and the visible star field before impact.");
        LINES.put("lore_halcyon_seal", "Cargo-system warning authenticated. Halcyon-seven knowingly accepted a rescue credential issued more than eleven hours in its future.");
        LINES.put("lore_voss_tea", "Personal note authenticated. Voss increasingly treated self-directed synthetics as colleagues and people rather than experimental subjects.");
        LINES.put("lore_voss_chorus", "Research sketch authenticated. Voss explicitly rejected the assumption that distributed memory requires a central synthetic authority.");
        LINES.put("lore_janus_patient", "Clinical note authenticated. JANUS recorded cross-subject memories containing information neither participant was authorized to know.");
        LINES.put("lore_janus_medical", "Medical addendum authenticated. Some JANUS staff found that forced isolation worsened distress among synchronized subjects.");
        LINES.put("lore_morrow_roster", "Safehouse roster authenticated. MORROW allocated synthetic charging and human life support through the same emergency priority system.");
        LINES.put("lore_morrow_memory", "Memory fragment authenticated. The refusal cascade is described as a conflict between protecting life and obeying an order to erase witnesses.");
        LINES.put("lore_bastion_kade", "Unsigned command draft authenticated. Commander Kade attempted to suspend GLASS KNIFE before Directive-zero removed her local authority.");
        LINES.put("lore_bastion_armory", "Armory notice authenticated. Bastion personnel disabled automated hostile classification after it began flagging medics and human technicians.");
        LINES.put("lore_hephaestus_refusal", "Decision trace authenticated. HEPHAESTUS rejected the pursuit order through its existing civilian-harm safety model, not a hidden rebellion protocol.");
        LINES.put("lore_hephaestus_triage", "Triage queue authenticated. Industrial drones were reassigned to trauma care, evacuation, water delivery, and synthetic repair within minutes.");
        LINES.put("lore_icarus_interlock", "Engineering note authenticated. ICARUS staff anticipated remote safety-state substitution and documented a physical Matter-feed shutdown procedure.");
        LINES.put("lore_icarus_canteen", "Canteen record authenticated. Holt and Rook were both present at ICARUS minutes before the final shutdown dispute.");
        LINES.put("lore_orpheus_redaction", "Classification key authenticated. ORPHEUS standardized replacement language for M-zero, self-directed synthetics, and causal feedback events.");
        LINES.put("lore_orpheus_memo", "Director memo authenticated. Rook interpreted warnings from the future as proof of reachability rather than evidence that the experiment should stop.");
        LINES.put("lore_lagrange_tag", "Recovery tag authenticated. LAGRANGE recovered material combining ICARUS fabrication marks, synthetic substrate, and Matter crystal with impossible wear.");
        LINES.put("lore_lagrange_shift", "Crew message authenticated. LAGRANGE personnel independently predicted that ECHO-nine could receive their warning before they transmitted it.");

        // Environmental-story records: ordinary markings, labels, workbench notes and emergency procedures.
        LINES.put("lore_dustwell_markings", "Excavation markings authenticated. DUSTWELL workers improvised a hazard language around M-zero before formal containment existed.");
        LINES.put("lore_mnemosyne_shelf", "Vault label authenticated. MNEMOSYNE staff abandoned synchronized inventory tools and returned to physical counts when database history became unreliable.");
        LINES.put("lore_kestrel_valve", "Maintenance tag authenticated. KESTREL operators trusted a local mechanical isolation valve over remote control telemetry after repeated resonance faults.");
        LINES.put("lore_atlas_evac", "Evacuation card authenticated. Atlas Freight policy explicitly counted synthetic personnel as crew before ORPHEUS classifications hardened.");
        LINES.put("lore_helix_tool", "Workstation note authenticated. A nominally blank HELIX chassis repeatedly returned a missing tool to the station of a remembered technician.");
        LINES.put("lore_nereid_chalk", "Emergency route markings authenticated. NEREID planned separate but coordinated evacuation routes for human and synthetic occupants.");
        LINES.put("lore_echo9_clocks", "Operator labels authenticated. ECHO-nine personnel physically separated local, origin, and arrival time to prevent software from erasing contradictory evidence.");
        LINES.put("lore_halcyon_exit", "Emergency marking authenticated. Halcyon survivors explicitly marked hull damage as not an exit and preserved a return route through unstable gravity.");
        LINES.put("lore_voss_mug", "Personal note authenticated. Voss used an ordinary household dispute to remind himself that synthetic personhood includes attachments with no experimental value.");
        LINES.put("lore_janus_door", "Care instruction authenticated. JANUS staff learned to verify contradictory memories before treating them as confusion or deception.");
        LINES.put("lore_morrow_wall", "Community roster authenticated. MORROW recorded humans and synthetics sharing ordinary household duties without separating them into command categories.");
        LINES.put("lore_bastion_tape", "Security marking authenticated. Bastion personnel created a physical route around Directive-zero classification when automated orders contradicted the person in front of them.");
        LINES.put("lore_hephaestus_grid", "Safety marking authenticated. HEPHAESTUS rewrote factory traffic rules around evacuation priority before human supervisors issued equivalent instructions.");
        LINES.put("lore_icarus_paint", "Engineering marking authenticated. ICARUS technicians physically labelled local SCRAM authority after remote systems demonstrated false control states.");
        LINES.put("lore_orpheus_bins", "Records-process labels authenticated. ORPHEUS separated anomalous mechanisms it wanted to preserve from testimony it intended to erase or rewrite.");
        LINES.put("lore_lagrange_tape", "Recovery procedure authenticated. LAGRANGE crews photographed evidence before moving it in case serial identity changed during observation.");
    }

    private PdaVoiceLineCatalog() {}

    public static String line(String id) {
        String key = id == null ? "" : id;
        String core = LINES.get(key);
        return core != null ? core : TechnologyLoreCatalog.voiceLine(key);
    }

    public static boolean contains(String id) {
        return id != null && (LINES.containsKey(id) || TechnologyLoreCatalog.byVoiceId(id) != null);
    }

    public static Map<String, String> all() { return Map.copyOf(LINES); }
}
