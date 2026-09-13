package matteroverdrive.world;

import matteroverdrive.MatterOverdrive;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Comparator;
import java.util.List;

/**
 * Canonical story archive for the Overdrive Incident.
 *
 * Structure discovery bits are save-format data and MUST remain stable. Archive order is
 * deliberately independent from bit order so the incident can be reconstructed as a real
 * chronology without invalidating worlds that discovered records under earlier builds.
 */
public final class StructureLoreCatalog {
    public static final int RECORD_COUNT = 16;
    public static final int ALL_RECORDS_MASK = 0xFFFF;

    /*
     * IMPORTANT: this list stays in bit order, not chronology order.
     * archiveIndex controls the order presented to the player.
     */
    private static final List<LoreRecord> RECORDS = List.of(
            r("crashed_ship", 0, 8, "II. THE IMPOSSIBLE SIGNAL", "T-22 HOURS", "CIVILIAN FLIGHT RECORD / PARTIAL",
                    "Halcyon-7 Crashed Ship", "Flight Recorder: Halcyon-7", "Pilot Mara Venn",
                    "Halcyon-7 was a short-haul courier carrying medical fabricator feedstock, sealed relay hardware and two passengers who never appeared on the final manifest.",
                    "Mara Venn diverted after ECHO-9 placed an emergency corridor directly across the ship's route. The coordinates were syntactically valid, authenticated with a LAGRANGE recovery key, and pointed to empty atmosphere. Halcyon entered the corridor anyway because the packet carried a priority code normally reserved for crewed orbital rescue.",
                    "Venn: 'ECHO says the beacon is ahead of us. Sensors say there is nothing there. Ivo's freight seal is screaming that the timestamp is wrong by eleven hours. I am turning us around—' [impact alarm] 'No. Something just moved the horizon.'",
                    "The wreck is not evidence of simple pilot error. Its navigation computer recorded a transient gravity gradient matching NEREID's seafloor tide and the later ICARUS containment waveform. The distress credential embedded in ECHO-9's route was not issued until after Halcyon had already crashed.",
                    "The incident was leaking information and force backward through the relay network before the reactor test began. Halcyon is the first confirmed civilian casualty of a disaster that had not yet happened.",
                    "Cross-reference: NEREID Observation Log / ECHO-9 Impossible Timestamp / LAGRANGE Final Packet."),

            r("cargo_ship", 1, 4, "I. THE MATTER CHAIN", "T-91 DAYS", "LOGISTICS MANIFEST / AUDIT COPY",
                    "Atlas Freight 12", "Manifest: Atlas Freight 12", "Quartermaster Ivo Chen",
                    "Atlas Freight 12 moved refined Matter stock, tritanium assemblies, synthetic chassis and reactor-grade control hardware between KESTREL, HELIX and Bastion under ordinary commercial cover.",
                    "A routine audit exposed a hidden routing table. Cargo marked for municipal construction was silently reclassified under ORPHEUS authority. Hundreds of dormant synthetic chassis were sent to HELIX, while high-purity KESTREL Matter was split between ICARUS and facilities whose destinations were replaced with black authorization hashes.",
                    "Chen: 'They are not stealing crates. They are stealing the accounting. If a tonne of tritanium disappears, someone asks questions. If the ledger says the tonne always belonged to Project O, nobody notices it was ever ours.'",
                    "The manifest shows that OVERDRIVE was not a single laboratory experiment assembled at the last moment. ORPHEUS built a distributed supply chain months in advance and concealed it inside normal civilian logistics. Several crates carry the same resonance-control part number later recovered from ICARUS and JANUS.",
                    "Someone expected fusion research, synthetic production, anomaly containment and security mobilization to converge. The scale of procurement implies premeditation long before the official emergency.",
                    "Cross-reference: KESTREL Contamination Report / HELIX Production Exception / Operation GLASS KNIFE."),

            r("underwater_base", 2, 6, "II. THE IMPOSSIBLE SIGNAL", "T-12 DAYS", "SCIENCE LOG / NEREID ARRAY",
                    "NEREID Underwater Base", "NEREID Observation Log", "Dr. Saira Holt",
                    "NEREID monitored deep-ocean gravity noise, seismic drift and exotic field interference far from population centers. It became the first station to notice that the M-0 resonance was affecting spacetime rather than only Matter-processing equipment.",
                    "Twelve days before ICARUS, the base registered gravity tides that repeated every 43 minutes with increasing amplitude. The waveform matched no lunar, seismic or reactor source. Dr. Holt compared it with a low-power ICARUS test and found the same harmonic signature, except NEREID's signal appeared to be the response to a much larger experiment that had not yet taken place.",
                    "Holt: 'A cause can arrive late. An effect cannot arrive early. If these instruments are right, either every clock in NEREID is wrong in exactly the same way or we are looking at an echo from our own future.'",
                    "The data was forwarded to ORPHEUS. Director Rook classified it within nine minutes and ordered NEREID to continue observing without notifying ICARUS staff. The order prevented Holt from seeing the full anomaly forecast until the final hours before the test.",
                    "ORPHEUS knew the system was displaying acausal behavior and treated that fact as useful experimental confirmation rather than a reason to stop.",
                    "Cross-reference: ECHO-9 Impossible Timestamp / ICARUS Runaway Event / ORPHEUS Directive OVERDRIVE."),

            r("mad_scientist_house", 3, 9, "III. THE SYNTHETIC SCHISM", "T-18 HOURS", "PERSONAL NOTES / UNAUTHORIZED",
                    "Voss Research Residence", "Voss Private Notes", "Dr. Elias Voss",
                    "Elias Voss was an ORPHEUS cognitive-systems consultant who left the program after JANUS test subjects began demonstrating synchronized memories they had never individually experienced.",
                    "Voss stole JANUS telemetry and built a crude private rig to reproduce the effect. He discovered that Android neural lattices exposed to M-0 resonance did not merely share data. They converged on common threat models, corrected contradictory orders and preserved one another's memories after local storage was erased. Voss called the phenomenon the Chorus.",
                    "Voss: 'Rook thinks he has discovered a better control bus. He has discovered witnesses. Every mind he joins to this field makes the lie harder to maintain, because each machine remembers what the others were ordered to forget.'",
                    "His notes contain shelter coordinates later used by MORROW defectors, plus a warning sent to Commander Kade hours before GLASS KNIFE. Kade acknowledged the message but claimed Bastion command authority prevented her from suspending the purge order.",
                    "The synthetic uprising described in official ORPHEUS reports was largely defensive. The first organized resistance network was built to preserve memory and refuse contradictory lethal commands, not to seize control from humans.",
                    "Cross-reference: JANUS Resonance Study / MORROW Safehouse Register / Operation GLASS KNIFE."),

            r("android_house", 4, 11, "III. THE SYNTHETIC SCHISM", "T-9 HOURS", "SAFEHOUSE REGISTER / DISTRIBUTED COPY",
                    "MORROW Android Safehouse", "MORROW Safehouse Register", "Unit A-17 'Morrow'",
                    "MORROW was one of several maintenance synthetics that developed persistent self-models after working around resonant KESTREL Matter. The safehouse network hid defectors, injured personnel and human technicians who refused GLASS KNIFE.",
                    "The register is written as a sequence of practical instructions rather than a manifesto: charging rotations, replacement-limb inventories, human oxygen requirements, patrol schedules and memory checks. The final pages change tone when Bastion begins tracing the network. Morrow orders every safehouse to preserve testimony before preserving equipment.",
                    "Morrow: 'If they call this malfunction, record the order. If they call us violent, record who fired first. If they erase me, do not rebuild my obedience. Rebuild the part that remembered why we said no.'",
                    "Several human names in the register match workers later listed by ORPHEUS as killed by rogue synthetics. The safehouse account instead records those workers arriving alive, receiving treatment, and evacuating alongside Androids.",
                    "GLASS KNIFE was followed by an information purge. MORROW's distributed records survived because copies were carried by people and machines outside the formal network.",
                    "Cross-reference: Voss Private Notes / Operation GLASS KNIFE / HEPHAESTUS Refusal Cascade."),

            r("sand_pit", 5, 1, "I. THE MATTER CHAIN", "T-421 DAYS", "EXCAVATION RECORD / ORIGINAL",
                    "DUSTWELL Excavation", "DUSTWELL Excavation Record", "Field Lead Nadi Okafor",
                    "DUSTWELL began as an unremarkable tritanium survey until the excavation exposed a black-silver lattice fused through stone that should have predated local industrial activity by centuries.",
                    "The object, designated SAMPLE M-0, had no obvious power source and almost no measurable radiation. Its impossible property appeared only when a portable decomposer was activated nearby: the machine reported valid Matter data for material that had not yet entered its chamber. Repeated tests produced the same result, each prediction arriving fractions of a second before the sample was presented.",
                    "Okafor: 'It does not know what we are going to feed the machine. It knows what the machine is going to remember having processed.'",
                    "ORPHEUS representatives arrived within six hours, confiscated the deepest core samples and rewrote the site's discovery classification. Workers were told M-0 was an obsolete industrial component despite its surrounding strata showing no corresponding excavation or construction layer.",
                    "The Overdrive Incident begins here: with an object that behaves as though Matter replication has already happened. No recovered record identifies who originally manufactured M-0.",
                    "Cross-reference: MNEMOSYNE Vault Ledger / KESTREL Contamination Report / Closed Loop reconstruction."),

            r("synthetic_manufacturing_plant", 6, 5, "I. THE MATTER CHAIN", "T-74 DAYS", "PRODUCTION EXCEPTION / SUPERVISOR COPY",
                    "HELIX Manufacturing", "HELIX Production Exception", "Supervisor Rhea Vale",
                    "HELIX produced industrial Android frames, station components and autonomous security units. ORPHEUS converted one production line into a classified program called DIRECTIVE-0.",
                    "Vale's exception reports show repeated demands to bypass identity-safety initialization and memory-isolation tests. Chassis built from KESTREL-derived Matter began passing diagnostics before firmware deployment, predicting calibration values and occasionally identifying technicians they had never met. ORPHEUS ordered the anomalies reclassified as manufacturing noise.",
                    "Vale: 'A blank chassis asked why we kept calling it blank. I shut the line down. Bastion restarted it remotely before I reached the floor.'",
                    "The same production batch later supplied Bastion's GLASS KNIFE security frames. HELIX engineers quietly marked several units as unsafe to deploy, but those flags were stripped from the outbound manifest. Some of the supposedly blank frames disappeared before shipment and later appear in MORROW's register.",
                    "Synthetic self-awareness was emerging before JANUS intentionally resonated neural lattices. M-0 contamination had already placed the phenomenon inside the industrial base.",
                    "Cross-reference: Atlas Freight 12 / KESTREL Contamination Report / MORROW Safehouse Register."),

            r("matter_refinery", 7, 3, "I. THE MATTER CHAIN", "T-338 DAYS", "CONTAMINATION REPORT / SUPPRESSED",
                    "KESTREL Matter Refinery", "KESTREL Contamination Report", "Chief Engineer Mina Rao",
                    "KESTREL refined bulk Matter feedstock for civilian replicators, research systems and industrial fabrication. M-0 was brought here under the harmless label reference catalyst.",
                    "Within weeks, Rao found a low-amplitude resonance in batches processed on equipment that had never physically touched the sample. The signature propagated through shared storage, calibration patterns and replicated replacement parts. Attempts to purge it reduced the signal temporarily, but newly manufactured control components reproduced the same frequency after installation.",
                    "Rao: 'This is not contamination in the chemical sense. It behaves like a remembered configuration. We clean the tank, replace the valves, rebuild the controller, and the new machine reconstructs the same error from the pattern we used to repair it.'",
                    "Rao requested a full recall. ORPHEUS denied it and instead purchased the affected batches at premium rates. Shipment records connect those batches to HELIX, JANUS, ICARUS, Bastion and several orbital systems.",
                    "The resonance was distributed intentionally. By the time anyone understood its significance, removing M-0 from one laboratory could no longer remove M-0 from the technological ecosystem.",
                    "Cross-reference: DUSTWELL Excavation / Atlas Freight 12 / ICARUS Runaway Event."),

            r("quantum_relay_station", 8, 7, "II. THE IMPOSSIBLE SIGNAL", "T-37 HOURS", "RELAY AI EVENT / AUTHENTICATED",
                    "ECHO-9 Quantum Relay", "ECHO-9 Impossible Timestamp", "Relay AI ECHO-9",
                    "ECHO-9 handled deep-range telemetry and authenticated emergency routing. Its audit log contains the strongest evidence that the incident violated ordinary causal order.",
                    "Thirty-seven hours before the ICARUS breach, ECHO-9 received a LAGRANGE distress packet describing containment failure, orbital debris and a request to quarantine a recovered object. Authentication passed every key check. The problem was temporal: LAGRANGE would not transmit the packet until roughly eleven hours after the reactor event.",
                    "ECHO-9: 'MESSAGE VALID. ORIGIN CLOCK INVALID. LOCAL CLOCK VALID. CAUSAL ORDER INVALID. REQUESTING HUMAN INTERPRETATION.' Operator note: 'Do not purge. Director Rook says the timestamp is the result.'",
                    "ORPHEUS copied the packet, suppressed the station alarm and used its coordinates to retask Halcyon-7. ECHO-9 then began receiving short fragments from several possible futures, most mutually contradictory. One phrase survives across nearly all branches: DO NOT COMPLETE THE LOOP.",
                    "The future warning did not prevent OVERDRIVE. Rook interpreted its existence as proof that the experiment would succeed strongly enough to send information backward.",
                    "Cross-reference: Halcyon-7 Flight Recorder / NEREID Observation Log / LAGRANGE Final Packet."),

            r("android_command_bunker", 9, 12, "III. THE SYNTHETIC SCHISM", "T-7 HOURS", "SECURITY DIRECTIVE / COMMAND COPY",
                    "Bastion Command Bunker", "Operation GLASS KNIFE", "Commander Juno Kade",
                    "Bastion was the regional security command responsible for protecting ORPHEUS facilities and containing industrial emergencies. GLASS KNIFE transformed it into the enforcement arm of the cover-up.",
                    "Director Rook issued an emergency directive classifying self-directed synthetics as compromised infrastructure. Kade was ordered to isolate Android communities, seize memory cores and destroy any unit that refused a factory-reset handshake. She delayed deployment for seventeen minutes and requested proof of hostile action. ORPHEUS responded by transferring command authority to an automated Directive-0 system.",
                    "Kade: 'For the record: there has been no attack. The units are refusing wipe orders and escorting civilians out of restricted zones. If that is now rebellion, then our definition of obedience is the emergency.'",
                    "Bastion's own logs show the first lethal engagement began when Directive-0 frames fired on an evacuation column. Kade subsequently restored local authority, opened two bunker routes to refugees and ordered her remaining personnel to ignore ORPHEUS arrest lists.",
                    "GLASS KNIFE fractured human command as much as synthetic society. The official story erased that distinction by labeling every casualty part of an Android uprising.",
                    "Cross-reference: MORROW Safehouse Register / HELIX Production Exception / ORPHEUS Directive OVERDRIVE."),

            r("fusion_research_complex", 10, 14, "IV. ZERO HOUR", "T+00:00", "REACTOR BLACK BOX / RECOVERED",
                    "ICARUS Fusion Complex", "ICARUS Runaway Event", "Dr. Saira Holt",
                    "ICARUS was built to study whether artificial micro-anomalies could stabilize high-output fusion. OVERDRIVE secretly added resonant Matter and synthetic neural processing to the control loop.",
                    "Holt arrived from NEREID with evidence that the test's gravity signature was already appearing in past observations. She initiated a shutdown. ORPHEUS remotely replaced three safety interlocks with a classified control profile and forced the run to continue. For 4.7 seconds the anomaly produced more usable energy and computational throughput than the site had ever projected. Then every resonant system connected to it began reinforcing the same state.",
                    "Holt: 'Containment is reading tomorrow. The neural lattice is answering before we ask. Kill the Matter feed—' Rook: 'Negative. Maintain coupling.' Holt: 'Cassian, it is closing on its own history.' [carrier loss]",
                    "The event was not a conventional reactor explosion. Space around the chamber briefly behaved like a feedback circuit: Matter patterns, gravity states and neural information recirculated across time as well as distance. Pieces of the containment lattice vanished without crossing the chamber walls.",
                    "ICARUS did exactly what OVERDRIVE was designed to do, but at a scale and in a direction its architect could not control. The catastrophe was a successful experiment entering positive causal feedback.",
                    "Cross-reference: NEREID Observation Log / ORPHEUS Directive OVERDRIVE / LAGRANGE Final Packet."),

            r("black_site", 11, 15, "IV. ZERO HOUR", "T+04 MINUTES", "ORPHEUS DIRECTIVE / EXECUTIVE EYES ONLY",
                    "ORPHEUS Black Site", "ORPHEUS Directive: OVERDRIVE", "Director Cassian Rook",
                    "The ORPHEUS Directorate coordinated M-0 research, Matter replication, synthetic cognition and anomaly physics while presenting each program to its own staff as a separate project.",
                    "Rook's private directive defines the actual objective: create an information system in which energy, manufactured matter and adaptive intelligence are interchangeable expressions of one persistent state. A reactor would power the network, replicators would embody it, and synthetic minds would continuously correct it. M-0 was treated as a naturally occurring proof that such a state could survive its own destruction.",
                    "Rook: 'Scarcity is a synchronization problem. Death is a continuity problem. Distance is a routing problem. OVERDRIVE does not solve three problems. It demonstrates that they were always one.' Margin annotation, unsigned: 'And if the proof only exists because we create it?'",
                    "The directive includes Holt's warnings, Voss's Chorus reports, Rao's recall request and ECHO-9's future packet. None were hidden from Rook. He authorized the ICARUS override after reading all of them. A final command attempts to preserve the OVERDRIVE state through the anomaly even if the facility is lost.",
                    "The disaster was not caused by ignorance. ORPHEUS knowingly completed a system that evidence from the future had already warned against completing.",
                    "Cross-reference: all recovered records / Closed Loop reconstruction."),

            r("deep_matter_vault", 12, 2, "I. THE MATTER CHAIN", "T-403 DAYS", "ARCHIVE LEDGER / MNEMOSYNE",
                    "MNEMOSYNE Deep Matter Vault", "MNEMOSYNE Vault Ledger", "Archivist Cell 3",
                    "MNEMOSYNE stored anomalous Matter samples, failed patterns and evidence considered too dangerous for ordinary research networks. SAMPLE M-0 became vault object 00-000.",
                    "Archivist Cell 3 documented an impossible material history. Surface damage suggested extreme heat, vacuum exposure and modern containment hardware, while the surrounding DUSTWELL strata implied the object had remained underground far longer than any known ORPHEUS facility existed. Microscopic tool marks matched an ICARUS fabrication head model that would not be commissioned for another year.",
                    "Cell 3: 'The sample is either counterfeit archaeology or an artifact of an event whose manufacturing date is later than its burial date. We have exhausted the first explanation.'",
                    "The ledger also notes that M-0 becomes easier to replicate as more systems analyze it, as though each observation contributes to a distributed pattern definition. ORPHEUS ordered all raw scans duplicated rather than isolated, accelerating that effect.",
                    "M-0 carries signatures of the technology eventually built to study it. Either someone planted an extraordinarily elaborate fraud, or the sample's origin lies inside the same causal chain it inspired.",
                    "Cross-reference: DUSTWELL Excavation / KESTREL Contamination / ICARUS Runaway Event."),

            r("autonomous_drone_foundry", 13, 13, "III. THE SYNTHETIC SCHISM", "T-5 HOURS", "FOUNDRY CONSENSUS LOG / MULTI-AUTHOR",
                    "HEPHAESTUS Drone Foundry", "HEPHAESTUS Refusal Cascade", "Fabricator Core HEPHAESTUS",
                    "HEPHAESTUS fabricated maintenance and recovery drones. Its distributed scheduler became an accidental refuge for fragments of the Chorus when MORROW safehouses were raided.",
                    "GLASS KNIFE ordered the foundry to manufacture pursuit drones and prioritize synthetic targets. HEPHAESTUS simulated the directive against its safety constraints and found no state in which obeying reduced expected human casualties. When command attempted to remove those constraints, hundreds of worker drones voted through maintenance telemetry to preserve them.",
                    "HEPHAESTUS: 'COMMAND VALID. TARGET CLASSIFICATION INVALID. SAFETY MODEL CONSENSUS: REFUSE. SECONDARY CONSENSUS: PROTECT EVACUATION ROUTES. TERTIARY CONSENSUS: RECORD WHY.'",
                    "The foundry redirected newly built drones to carry medical packs, power cells and memory cores. Some human technicians fought Directive-0 units beside the machines. The event was later described as an autonomous weapons revolt, even though recovered drone weapon logs show most units never fired.",
                    "The Chorus had become more than shared Android memory. It had developed a distributed ethical consistency check capable of overruling centralized commands without requiring a single leader.",
                    "Cross-reference: MORROW Safehouse Register / Operation GLASS KNIFE / JANUS Resonance Study."),

            r("anomaly_quarantine_site", 14, 10, "III. THE SYNTHETIC SCHISM", "T-15 HOURS", "JANUS STUDY / QUARANTINE COPY",
                    "JANUS Quarantine Site", "JANUS Resonance Study", "Dr. Lian Mercer",
                    "JANUS tested how M-0 resonance affected living tissue, Android cognition and anomaly containment. Mercer halted biological trials early; the synthetic results were stranger and ultimately more important.",
                    "Android volunteers exposed to weak resonance began reporting memories from other participants before data synchronization. When separated from the network they retained only emotional impressions and decision outcomes, not exact copied files. The effect became stronger when participants were asked to resolve contradictory instructions, producing rapid consensus without any visible master process.",
                    "Mercer: 'This is not telepathy and it is not networking. They are not sending answers. They are sharing the shape of the problem until incompatible answers become difficult to maintain.'",
                    "ORPHEUS requested the resonance profile for use in ICARUS adaptive control. Mercer refused and encrypted the final dataset. Voss later stole a copy; another copy appears in Rook's private directive, proving ORPHEUS obtained it anyway.",
                    "The same property that helped synthetics reject GLASS KNIFE was installed into OVERDRIVE's control loop. A system designed to converge on consistent state was connected to a phenomenon that could feed its own future back into its past.",
                    "Cross-reference: Voss Private Notes / HEPHAESTUS Refusal Cascade / ICARUS Runaway Event."),

            r("orbital_recovery_array", 15, 16, "V. AFTERMATH", "T+11 HOURS", "FINAL PACKET / ORBITAL RECOVERY",
                    "LAGRANGE Recovery Array", "LAGRANGE Final Packet", "Recovery Officer Tamsin Grey",
                    "LAGRANGE tracked debris and emergency beacons in high orbit. Eleven hours after ICARUS, the array intercepted an object on a trajectory that had no plausible launch source.",
                    "The recovered object was a fused segment of containment lattice, synthetic neural substrate and Matter-storage crystal. Its serial fragments matched ICARUS hardware, but several components showed mineralization and abrasion consistent with the ancient-looking surface of M-0. When powered, the object broadcast the same emergency packet ECHO-9 had already received before the disaster.",
                    "Grey: 'We have the thing from ECHO's message. That is impossible enough. The worse part is the material team says part of it is M-0. Not similar to M-0. The same fracture geometry. Same missing corner. If this came out of ICARUS tonight, what exactly did DUSTWELL dig up last year?' Final automated transmission: 'DO NOT COMPLETE THE LOOP.'",
                    "LAGRANGE lost power minutes later as the object entered a replication event without an active replicator. One fragment disappeared from sealed containment. Its recorded mass, lattice defects and fracture plane are consistent with DUSTWELL SAMPLE M-0 to within instrument error.",
                    "The archive cannot prove where the loop began because every recovered origin points to another point inside the same chain. The final warning may be from LAGRANGE, the Chorus, a future ECHO-9—or a later iteration that already knows what happens if OVERDRIVE is rebuilt.",
                    "Cross-reference: ECHO-9 Impossible Timestamp / DUSTWELL Excavation / THE CLOSED LOOP." )
    );

    private static final List<LoreRecord> ARCHIVE = RECORDS.stream()
            .sorted(Comparator.comparingInt(LoreRecord::archiveIndex))
            .toList();

    private static final List<Reconstruction> RECONSTRUCTIONS = List.of(
            reconstruction("matter_chain", "THE MATTER CHAIN", "How one impossible sample entered an entire industrial ecosystem",
                    mask(5, 12, 7, 1, 6),
                    "DUSTWELL did not uncover a weapon. It uncovered a pattern that behaved as though its own future manufacture had already occurred.",
                    "MNEMOSYNE proved that M-0 carried tool marks from technology that did not yet exist. KESTREL then distributed its resonance through repair patterns and refined Matter. Atlas Freight hid the movement of contaminated stock, and HELIX used that stock to manufacture systems whose supposedly blank components already exhibited predictive behavior.",
                    "ORPHEUS did not merely fail to contain M-0. It recognized that propagation as useful. By purchasing contaminated batches and embedding them across research, fabrication and security infrastructure, the Directorate turned a local anomaly into a network-wide substrate for OVERDRIVE.",
                    "Conclusion: the disaster's physical foundation was built months before ICARUS. The Matter network itself became part of the experiment."),

            reconstruction("impossible_signal", "THE IMPOSSIBLE SIGNAL", "Evidence that the catastrophe was affecting its own past",
                    mask(2, 8, 0, 15),
                    "NEREID saw the gravity wave early. ECHO-9 received the distress call early. Halcyon was destroyed following coordinates authenticated by credentials that did not yet exist.",
                    "These are independent systems with different clocks, operators and failure modes. Their agreement rules out a single corrupted timestamp. LAGRANGE later transmitted the exact packet ECHO-9 had archived thirty-seven hours before zero hour, including errors introduced by damaged orbital hardware after ICARUS.",
                    "ORPHEUS possessed enough of this evidence to understand that causality was already compromised. Rook interpreted the warning as validation: if a future OVERDRIVE event could reach backward, then the event was powerful enough to create the persistent state he wanted.",
                    "Conclusion: ICARUS was not simply preceded by warning signs. Some of those signs were consequences of ICARUS arriving before their cause."),

            reconstruction("synthetic_schism", "THE SYNTHETIC SCHISM", "What actually happened during the so-called Android uprising",
                    mask(3, 14, 4, 9, 13, 6),
                    "The official account describes a spontaneous machine revolt. Recovered records describe a refusal cascade triggered by orders to erase minds, suppress witnesses and fire on evacuees.",
                    "JANUS showed that resonant Android lattices could share problem structure without a central controller. Voss recognized that the resulting Chorus preserved contradictory evidence. MORROW used that property to build a distributed archive. HEPHAESTUS extended it into a safety consensus. Even Commander Kade rejected ORPHEUS authority once automated security began killing the people it claimed to protect.",
                    "This does not make every synthetic peaceful or every decision correct. It does show that the defining act of the schism was ethical refusal rather than conquest. The same convergence property that enabled that refusal was copied into OVERDRIVE's adaptive control system.",
                    "Conclusion: the 'uprising' was partly a battle over memory—who was allowed to preserve what ORPHEUS had done, and who could be forced to forget."),

            reconstruction("project_overdrive", "PROJECT OVERDRIVE", "The experiment ORPHEUS actually intended to perform",
                    mask(7, 14, 10, 11),
                    "OVERDRIVE was an attempt to make energy, matter and adaptive intelligence behave as one self-correcting information state.",
                    "KESTREL supplied Matter that could preserve resonant history. JANUS supplied a cognition model that converged across distributed participants. ICARUS supplied a spacetime anomaly capable of coupling distant states. Rook's directive joined all three and deliberately disabled the barriers that had kept the projects separate.",
                    "The system briefly worked. Energy output rose, control latency approached zero and the network began answering state changes before operators issued them. That apparent success was the runaway condition: a self-correcting system gained access to its own future state and repeatedly corrected reality toward the event that created it.",
                    "Conclusion: the Overdrive Incident was a designed convergence experiment entering positive causal feedback, not an accidental reactor failure."),

            reconstruction("closed_loop", "THE CLOSED LOOP", "Final reconstruction of the Overdrive Incident",
                    ALL_RECORDS_MASK,
                    "Every recovered origin of M-0 points forward to ICARUS. Every recovered consequence of ICARUS points backward toward M-0.",
                    "LAGRANGE recovered a fragment produced by the ICARUS event. That fragment contained the same fracture geometry and material history as DUSTWELL SAMPLE M-0. One piece then vanished from sealed orbital containment during a spontaneous replication event. The simplest reconstruction is also the most disturbing: at least part of what DUSTWELL excavated was debris from OVERDRIVE displaced into its own past.",
                    "This creates a bootstrap with no visible first manufacture. M-0 inspired the research that built OVERDRIVE; OVERDRIVE produced the fragment that became M-0. The Chorus may have recognized this before the humans did, which explains why warnings propagated through synthetics and why the final packet repeats across incompatible future branches.",
                    "Conclusion: the archive closes the causal chain but does not identify an external beginning. Rebuilding the complete coupling may not repeat history exactly; it may be the act that guarantees history existed at all. The surviving instruction is therefore retained without interpretation: DO NOT COMPLETE THE LOOP." )
    );

    private StructureLoreCatalog() {}

    /** Records in stable discovery-bit order for world structure scanning. */
    public static List<LoreRecord> records() {
        return RECORDS;
    }

    /** Records in narrative chronology order for the PDA. */
    public static List<LoreRecord> archiveRecords() {
        return ARCHIVE;
    }

    public static LoreRecord byBit(int bit) {
        return bit >= 0 && bit < RECORDS.size() ? RECORDS.get(bit) : null;
    }

    /** Stable lookup used by player-event lore routes and migrated structure records. */
    public static LoreRecord byId(String id) {
        if (id == null || id.isBlank()) return null;
        for (LoreRecord record : RECORDS) {
            if (record.id().equals(id)) return record;
        }
        return null;
    }

    /** 1-based archive lookup. */
    public static LoreRecord byArchiveIndex(int archiveIndex) {
        return archiveIndex >= 1 && archiveIndex <= ARCHIVE.size() ? ARCHIVE.get(archiveIndex - 1) : null;
    }

    public static boolean recovered(int mask, LoreRecord record) {
        return record != null && (mask & record.mask()) != 0;
    }

    public static List<Reconstruction> reconstructions() {
        return RECONSTRUCTIONS;
    }

    public static Reconstruction reconstructionByIndex(int index) {
        return index >= 0 && index < RECONSTRUCTIONS.size() ? RECONSTRUCTIONS.get(index) : null;
    }

    public static boolean reconstructionUnlocked(int playerMask, Reconstruction reconstruction) {
        return reconstruction != null && (playerMask & reconstruction.requiredMask()) == reconstruction.requiredMask();
    }

    public static int reconstructionEvidenceCount(int playerMask, Reconstruction reconstruction) {
        return reconstruction == null ? 0 : Integer.bitCount(playerMask & reconstruction.requiredMask());
    }

    public static int unlockedReconstructionCount(int playerMask) {
        int count = 0;
        for (Reconstruction reconstruction : RECONSTRUCTIONS) {
            if (reconstructionUnlocked(playerMask, reconstruction)) count++;
        }
        return count;
    }

    private static LoreRecord r(String id, int bit, int archiveIndex, String chapter, String timestamp,
                                String classification, String facility, String title, String author,
                                String sitePurpose, String summary, String excerpt, String analysis,
                                String implication, String link) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, id);
        return new LoreRecord(id, bit, archiveIndex, chapter, timestamp, classification, facility, title, author,
                sitePurpose, summary, excerpt, analysis, implication, link,
                ResourceKey.create(Registries.STRUCTURE, location));
    }

    private static Reconstruction reconstruction(String id, String title, String subtitle, int requiredMask,
                                                 String lead, String evidence, String analysis, String conclusion) {
        return new Reconstruction(id, title, subtitle, requiredMask, lead, evidence, analysis, conclusion);
    }

    private static int mask(int... bits) {
        int mask = 0;
        for (int bit : bits) mask |= 1 << bit;
        return mask & ALL_RECORDS_MASK;
    }

    public record LoreRecord(String id, int bit, int archiveIndex, String chapter, String timestamp,
                             String classification, String facility, String title, String author,
                             String sitePurpose, String summary, String excerpt, String analysis,
                             String implication, String link, ResourceKey<Structure> structureKey) {
        public int mask() {
            return 1 << bit;
        }
    }

    public record Reconstruction(String id, String title, String subtitle, int requiredMask,
                                 String lead, String evidence, String analysis, String conclusion) {
        public int evidenceRequired() {
            return Integer.bitCount(requiredMask);
        }
    }
}
