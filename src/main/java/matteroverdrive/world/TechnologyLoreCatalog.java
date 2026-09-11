package matteroverdrive.world;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Curated PDA technology archive for the major functional Matter Overdrive blocks and items.
 * Decorative blocks, raw crafting intermediates, colour-only variants and debug-only objects are
 * intentionally excluded so discovery narration remains useful rather than noisy.
 */
public final class TechnologyLoreCatalog {
    public record TechRecord(String id, String title, String category, String function,
                             String lore, String fieldNote, String voiceLine) {
        public String voiceId() { return "tech_" + id.replace('.', '_').replace('-', '_'); }
    }

    private static final Map<String, TechRecord> RECORDS = new LinkedHashMap<>();
    private static final Map<String, TechRecord> LOOKUP = new LinkedHashMap<>();

    static {
        // Matter processing and pattern technology.
        add("decomposer", "Matter Decomposer", "MATTER ENGINEERING",
                "Consumes FE and matter-valued physical items, converting them into stored matter for later transport or replication.",
                "Decomposition was the first technology to make the Overdrive programme plausible: objects stopped being treated as things and became recoverable descriptions. DUSTWELL technicians later reported M-0 producing valid decomposition telemetry before the sample was formally catalogued.",
                "Treat the matter buffer as information-bearing inventory, not waste storage. M-0-era records show that valid data can be more dangerous than invalid data.",
                "Matter Decomposer indexed. Physical objects can now be reduced into stored matter-state data. Historical warning: M-zero produced valid readings before operators knew what they had recovered.");
        add("matter_recycler", "Matter Recycler", "MATTER ENGINEERING",
                "Processes Matter Dust and recovers usable refined matter material while preserving the represented matter economy.",
                "Recycler lines were originally a mundane recovery measure for failed decompositions. KESTREL later relied on them heavily after resonant matter contaminated production runs, turning what was supposed to be scrap handling into part of the incident supply chain.",
                "Recycler output is safer than an uncontrolled decomposition failure, but archived KESTREL notes show that processing history is not proof of causal isolation.",
                "Matter Recycler indexed. Failed decomposition products can be recovered instead of discarded. KESTREL records show that recycled matter still carried the history operators were trying to remove.");
        add("matter_analyzer", "Matter Analyzer", "PATTERN SYSTEMS",
                "Analyzes items into reusable patterns stored on Pattern Drives, recording identity, progress and matter cost.",
                "The Analyzer made replication practical by separating recognition from manufacture. ORPHEUS later used the same principle on increasingly ambiguous samples: first components, then neural substrates, then matter whose recorded identity appeared to precede its manufacture.",
                "A completed pattern is an authenticated description, not evidence that the described object has a simple origin.",
                "Matter Analyzer indexed. It converts a physical specimen into an authenticated replication pattern. In late ORPHEUS archives, description and origin stop being the same thing.");
        add("pattern_drive", "Pattern Drive", "PATTERN SYSTEMS",
                "Portable storage for analyzed replication patterns and their matter values.",
                "Pattern Drives were designed as boring removable media. That simplicity made them useful during the Incident: technicians could physically isolate a pattern from a network even when facility clocks, permissions and remote state could no longer be trusted.",
                "When diagnosing strange replication, remove the drive and verify its stored patterns before blaming the network.",
                "Pattern Drive indexed. Removable pattern media remains one of the few Matter systems that can be physically isolated from a compromised network.");
        add("pattern_storage", "Pattern Storage", "PATTERN SYSTEMS",
                "Powered multi-drive storage that exposes completed patterns to connected Matter Overdrive networks.",
                "Pattern Storage turned isolated Analyzer work into shared infrastructure. During the ORPHEUS response, this convenience became a liability: a perfectly valid pattern could be distributed faster than staff could determine whether its history was valid.",
                "Keep network and matter transport concepts separate. Storage exposes descriptions; it does not provide the matter or FE needed to realize them.",
                "Pattern Storage indexed. This system publishes removable pattern media to the network. A valid pattern can spread much faster than anyone can verify where it came from.");
        add("pattern_monitor", "Pattern Monitor", "PATTERN SYSTEMS",
                "Browses exposed patterns and queues replication requests across an enabled network path.",
                "The Monitor is the human-facing side of the replication chain: it turns archived descriptions into explicit manufacture requests. Post-Incident recovery teams value that separation because it leaves a visible point where intent can still be checked before material is created.",
                "If a pattern is missing, verify Storage, Network Pipe and drive completion before assuming the Replicator is at fault.",
                "Pattern Monitor indexed. It is the intent layer of the replication chain: select a known pattern here before asking the network to manufacture it.");
        add("replicator", "Matter Replicator", "MATTER ENGINEERING",
                "Consumes FE and stored matter to manufacture an item from a completed pattern request.",
                "Replication is where Matter Overdrive stops resembling ordinary fabrication. The machine does not machine a billet into shape; it reconstructs matter according to an authenticated description. LAGRANGE evidence suggests the universe may not always agree on which reconstruction came first.",
                "Replication requires all three resources: a valid pattern path, matter and FE. None substitutes for the others.",
                "Matter Replicator indexed. Given energy, matter and an authenticated pattern, it reconstructs the described object directly. LAGRANGE evidence makes the word original increasingly difficult to use.");
        add("inscriber", "Molecular Inscriber", "FABRICATION",
                "Powered circuit-fabrication machine used for the current Mk1 through Mk4 isolinear progression.",
                "Inscribers were ubiquitous before OVERDRIVE and survived in nearly every research site because they are deterministic, local and comparatively easy to audit. That made them valuable when more advanced fabrication systems began returning answers operators had not asked for yet.",
                "Use the current circuit chain rather than assuming legacy recipes. Its value is reliability, not mystery.",
                "Molecular Inscriber indexed. Old, local, and deterministic fabrication became unusually valuable once more advanced systems started producing answers before their questions.");
        add("matter_storage_matrix", "Matter Storage Matrix", "MATTER ENGINEERING",
                "High-capacity fixed matter-storage infrastructure used by advanced facilities and restoration networks.",
                "Large matrices allowed KESTREL and ORPHEUS sites to treat matter like a utility. They also made contamination harder to reason about: replacing a controller did not necessarily replace the stored state that controller was reading.",
                "Before dismantling a matrix, account for what it stores and what other systems still consider it authoritative.",
                "Matter Storage Matrix indexed. High-capacity matter storage made matter a facility utility. KESTREL learned that replacing the controller does not necessarily replace the state.");
        add("matter_excavator", "Matter Excavator", "MATTER ENGINEERING",
                "Industrial recovery hardware associated with large-scale matter extraction and excavation sites.",
                "DUSTWELL used excavator-class hardware before Project OVERDRIVE existed. Later archives repeatedly return to those machines because their earliest logs contain the first measurable evidence that M-0 behaved as though it already had a recorded future.",
                "Excavation equipment is evidence-bearing infrastructure. Preserve logs before stripping components from old sites.",
                "Matter Excavator indexed. DUSTWELL-class recovery hardware predates Project OVERDRIVE, but its logs contain some of the earliest evidence that M-zero already had a future.");
        add("matter_network_terminal", "Matter Network Terminal", "NETWORK INFRASTRUCTURE",
                "Operator-facing terminal for inspecting and interacting with the current Matter/network infrastructure.",
                "Recovery teams introduced dedicated terminals after learning that hidden automation made incident reconstruction harder. A network that can explain what it thinks it is doing is easier to trust than one that is merely fast.",
                "Use terminal telemetry to verify route state before altering filters or tearing down conduits.",
                "Matter Network Terminal indexed. Recovery doctrine favors visible network state: a system that can explain its route is easier to trust than one that is merely fast.");

        // Power and general machines.
        add("solar_panel", "Solar Panel", "POWER SYSTEMS",
                "Generates FE from valid daylight and stores/output power through the current machine energy interface.",
                "Solar generation is deliberately uninteresting, which is precisely why field teams rely on it. It has no anomaly core, no matter-state dependency and no reason to know about Project OVERDRIVE.",
                "Use solar power for low-risk bootstrap energy before bringing late-game systems online.",
                "Solar Panel indexed. Simple daylight generation remains one of the safest ways to bootstrap a recovered facility.");
        add("charging_station", "Charging Station", "POWER SYSTEMS",
                "Recharges compatible energy items and supports the current nearby Android wireless-charging behavior.",
                "Charging stations were shared infrastructure in human and synthetic workspaces long before GLASS KNIFE. MORROW later used the same stations without separating human equipment from synthetic life-support priorities.",
                "A powered station can serve both carried equipment and compatible Android systems; check local demand before assuming it is idle.",
                "Charging Station indexed. This infrastructure served tools, batteries and synthetic personnel alike. MORROW records classify all three as continuity-critical loads.");
        add("grid_capacitor", "Grid Capacitor", "POWER SYSTEMS",
                "Facility-scale FE buffering and distribution support for restored Matter Overdrive infrastructure.",
                "Capacitors were installed to absorb short violent demand spikes from transporters, fabrication and containment systems. In ICARUS logs, stable average load often concealed dangerous instantaneous coupling events.",
                "Do not judge a grid only by average FE demand. Burst capacity can decide whether a safety system completes its shutdown cycle.",
                "Grid Capacitor indexed. It exists for the moments when average power statistics stop mattering and a facility needs energy immediately.");
        add("quantum_power_relay", "Quantum Power Relay", "POWER SYSTEMS",
                "Advanced facility power relay used in late-generation Matter Overdrive installations.",
                "Quantum relays appear disproportionately often in ECHO-9 and ORPHEUS-era sites. Their engineering is less troubling than their logs: several report synchronization events in an order local clocks cannot explain.",
                "Treat relay telemetry as operational data, not a trustworthy chronology, when working near acausal systems.",
                "Quantum Power Relay indexed. The hardware is advanced but understandable. Its historical timestamps are not always so cooperative.");
        add("hybrid_conduit", "Hybrid Conduit", "NETWORK INFRASTRUCTURE",
                "Dense facility conduit used where restored infrastructure needs multiple service paths in a constrained route.",
                "Late facilities increasingly hid power, matter and data routing inside common service trunks. The design saved space and made sabotage harder, but it also made post-incident diagnosis much less intuitive.",
                "Use live diagnostics rather than judging a conduit by appearance; not every service carried through a facility serves the same subsystem.",
                "Hybrid Conduit indexed. Late facilities packed multiple services into common trunks. Compact infrastructure is efficient, but failures become much harder to read by eye.");
        add("microwave", "Microwave", "UTILITY FABRICATION",
                "Powered food cooker using valid smelting results with blocked-output handling and upgrade support.",
                "The microwave is proof that Matter Overdrive facilities were workplaces before they were ruins. Canteen telemetry from ICARUS survives within minutes of Zero Hour, including an ordinary coffee cycle that outlived several classified control logs.",
                "Not every recovered machine is part of the conspiracy. Sometimes the correct forensic conclusion is that somebody was heating lunch.",
                "Microwave indexed. Not every recovered machine is ominous. ICARUS retained canteen telemetry more reliably than some of its classified control logs.");
        add("spacetime_accelerator", "Space-Time Accelerator", "TEMPORAL ENGINEERING",
                "Consumes FE and matter to apply extra ticks to eligible nearby blocks and block entities.",
                "Accelerators bend operational time without requiring the full OVERDRIVE coupling stack. ORPHEUS considered them useful testbeds because they made causality feel negotiable while remaining comparatively controllable.",
                "Large ranges multiply third-party machine load. Treat performance cost as part of the machine's real operating envelope.",
                "Space-Time Accelerator indexed. It spends energy and matter to give nearby systems more operational time. Range is also a performance multiplier.");
        add("transporter", "Transporter", "SPATIAL ENGINEERING",
                "Moves eligible entities to a bound same-dimension destination using a Transport Flash Drive, FE and cooldown checks.",
                "Transporters reduce a journey to validated source, destination and energy state. Halcyon-seven's final route demonstrated the dangerous assumption hidden inside that convenience: a destination credential can be valid even when its history is not.",
                "Verify destination binding before stepping onto the pad. Authentication answers where; it does not always answer when.",
                "Transporter indexed. A valid destination is not the same thing as a simple destination history. Halcyon-seven is the standing example.");
        add("facility_network_controller", "Facility Network Controller", "NETWORK INFRASTRUCTURE",
                "Coordinates restored facility-side infrastructure and exposes local network control state where supported.",
                "Controllers are deliberately boring authority anchors. After ORPHEUS remote overrides, recovery doctrine prefers controls that are local, inspectable and replaceable over hidden global orchestration.",
                "A controller should describe real backend state; decorative controls are not treated as authority.",
                "Facility Network Controller indexed. Recovery doctrine favors local, inspectable authority after ORPHEUS demonstrated what hidden remote control can cost.");
        add("contract_market", "Contract Market", "FIELD OPERATIONS",
                "Field-operations interface for obtaining and managing repeatable recovery work and associated rewards.",
                "The post-Incident world did not produce a single unified recovery agency. Contract markets emerged because salvage crews, researchers and synthetic communities needed a shared way to price work without agreeing on politics.",
                "Contracts are operational requests, not canonical truth. Their objectives may be practical even when their employers disagree about the Incident.",
                "Contract Market indexed. Recovery work outlived the organizations that caused the Incident. The market coordinates jobs without pretending everyone agrees on the history.");

        // Transport, routing and storage.
        add("heavy_matter_pipe", "Heavy Energy Cable", "POWER NETWORK",
                "Carries FE between compatible machines and reports live buffer/output state on supported operator screens.",
                "Heavy cable was chosen for reactor and industrial routes because containment systems punish intermittent delivery. The registry name is historical; field teams classify it by what it actually moves: energy.",
                "Use Heavy Energy Cable for FE. Matter Pipe and Network Pipe are separate systems even when routes run side by side.",
                "Heavy Energy Cable indexed. This is the FE path. Do not substitute Matter Pipe or Network Pipe because the three networks happen to share a corridor.");
        add("matter_pipe", "Matter Pipe", "MATTER NETWORK",
                "Moves stored matter between compatible matter endpoints through connected pipe runs.",
                "Matter Pipe made matter feel like fluid infrastructure, but it is carrying quantified reconstruction potential rather than a conventional liquid. KESTREL maintenance logs repeatedly warn against treating route cleanliness as proof of state cleanliness.",
                "Use this for matter only. A visually connected pipe is not automatically an FE or item route.",
                "Matter Pipe indexed. It routes stored matter between compatible endpoints. KESTREL records warn that a clean pipe does not prove the state passing through it is clean.");
        add("network_pipe", "Network Pipe", "DATA NETWORK",
                "Carries Matter Overdrive network connectivity for pattern, routing and control systems.",
                "Network Pipe carries descriptions and decisions rather than FE or matter. Separating those layers is one reason modern recovery builds are easier to diagnose than the dense trunks found in late ORPHEUS sites.",
                "When a machine has power and matter but cannot see a pattern or route, inspect Network Pipe first.",
                "Network Pipe indexed. It carries connectivity and decisions, not power and not matter. Keeping those layers separate makes failures readable.");
        add("network_switch", "Network Switch", "DATA NETWORK",
                "Persistent enabled/disabled routing control that changes the active network graph.",
                "Simple switches became emergency tools during the Incident because physically understandable disconnection was more trustworthy than software permissions inherited from ORPHEUS.",
                "Use a switch to isolate a route without dismantling the network. Its state persists.",
                "Network Switch indexed. Sometimes the safest network command is a visible, persistent disconnection.");
        add("network_router", "Network Router", "LOGISTICS NETWORK",
                "Moves items over Network Pipe using filters, destination flash drives, upgrade slots and per-item FE cost.",
                "Routers are the practical bridge between Matter Overdrive's information-heavy systems and ordinary inventories. HEPHAESTUS later used similar routing logic to prioritize medical and evacuation supplies over pursuit orders.",
                "Filtering defines intent. Diagnose destination filters and FE before assuming the route itself is broken.",
                "Network Router indexed. Automated logistics only becomes useful when intent is explicit. HEPHAESTUS proved that priority rules can matter as much as connectivity.");
        add("pylon", "Network Pylon", "DATA NETWORK",
                "Matching-channel local wireless bridge for the current network implementation.",
                "Pylons were built to cross spaces where physical runs were inconvenient or unsafe. Recovery implementations intentionally keep their scope conservative until the more exotic legacy behavior can be justified by working backend state.",
                "Match channels at both ends and verify the local graph before debugging higher-level machines.",
                "Network Pylon indexed. Matching channels can bridge a local network without a continuous pipe run. Current recovery hardware keeps the behavior deliberately conservative.");
        add("tritanium_crate", "Tritanium Crate", "STORAGE",
                "Large persistent storage container with Forge automation support and structure-loot integration.",
                "Tritanium crates survived crashes, flooding and abandoned facilities well enough to become the backbone of recovery archaeology. Their contents often tell a better story than official reports because nobody expected a maintenance crate to become evidence.",
                "Structure crates may carry persisted loot tables. Open them before moving or dismantling the surrounding site if you are preserving provenance.",
                "Tritanium Crate indexed. These containers survive long enough to become accidental archives. In abandoned facilities, ordinary storage is often evidence.",
                "tritanium_crate_black", "tritanium_crate_blue", "tritanium_crate_brown", "tritanium_crate_cyan",
                "tritanium_crate_gray", "tritanium_crate_green", "tritanium_crate_light_blue", "tritanium_crate_lime",
                "tritanium_crate_magenta", "tritanium_crate_orange", "tritanium_crate_pink", "tritanium_crate_purple",
                "tritanium_crate_red", "tritanium_crate_silver", "tritanium_crate_white", "tritanium_crate_yellow");
        add("security_door", "Security Door", "SECURITY",
                "Hardened access point intended to work with Matter Overdrive ownership and security systems.",
                "ORPHEUS doors protected secrets long after the people authorized to open them were gone. Recovery doctrine therefore distinguishes physical security from moral authority: a locked door can still be enforcing a dead organization's mistake.",
                "Respect ownership checks, but do not confuse a valid legacy credential with a valid present-day instruction.",
                "Security Door indexed. Legacy authorization can remain valid long after the organization behind it is gone.");

        // Fusion, anomaly and containment technology.
        add("fusion_reactor_controller", "Fusion Reactor Controller", "FUSION / ANOMALY",
                "Controls formation, RUN/SCRAM state, redstone behavior, telemetry and output of the current anomaly-coupled fusion reactor.",
                "ICARUS proved that a controller can be both technically correct and catastrophically overruled. Modern recovery firmware exposes RUN and SCRAM state directly because hidden safety substitution is no longer considered acceptable.",
                "Form the multiblock correctly, monitor anomaly mass and demand, and treat SCRAM as a real control path rather than decoration.",
                "Fusion Reactor Controller indexed. ICARUS is the reason modern recovery firmware exposes RUN and SCRAM state instead of hiding authority behind remote policy.");
        add("fusion_reactor_io", "Fusion Reactor I/O", "FUSION / ANOMALY",
                "Shared reactor interface for matter input, FE extraction and access to formed-ring storage.",
                "Reactor I/O blocks exist to keep the dangerous core mechanically separated from the systems feeding and draining it. ICARUS breached that conceptual boundary by allowing too many subsystems to share one experiment state.",
                "Feed matter and extract FE through the intended I/O path. Do not solve a routing problem by exposing the anomaly itself.",
                "Fusion Reactor I O indexed. It exists so matter and energy can cross the reactor boundary without treating the anomaly as an ordinary machine port.");
        add("fusion_reactor_coil", "Fusion Reactor Coil", "FUSION / ANOMALY",
                "Structural and electromagnetic component required by the current horizontal reactor ring.",
                "Coils are the visible discipline around an invisible problem. Their geometry matters because the reactor is not merely generating energy; it is maintaining a controlled relationship with a gravitational anomaly.",
                "Missing or misplaced coils invalidate the intended containment geometry. Use the assembly guide or overlay rather than improvising.",
                "Fusion Reactor Coil indexed. Reactor geometry is part of containment, not decoration. Missing coils mean the machine is not the machine you think you built.");
        add("gravitational_anomaly", "Gravitational Anomaly", "ANOMALY PHYSICS",
                "Mass-bearing hazard that pulls entities and items, consumes matter-bearing targets inside its event horizon and can drive reactor output.",
                "Natural anomalies predate the known ICARUS event, but Project OVERDRIVE deliberately coupled one to resonant matter and synthetic cognition. That decision turned a dangerous phenomenon into a causal instrument.",
                "Keep an escape vector. Event-horizon exposure is not an acceptable diagnostic technique.",
                "Gravitational Anomaly indexed. It is a mass-bearing environmental hazard, not a decorative reactor core. Maintain distance and a clear escape route.");
        add("gravitational_stabilizer", "Gravitational Stabilizer", "ANOMALY PHYSICS",
                "Powered directional stabilizer that reduces effective anomaly danger when correctly aimed, supplied and unobstructed.",
                "Stabilizers were built on an important engineering concession: nobody needed to understand every anomaly state if they could reliably reduce the dangerous part. JANUS and ICARUS records show how often that humility was ignored elsewhere.",
                "Power, orientation, line of sight and redstone mode all matter. A present block is not necessarily an active stabilizer.",
                "Gravitational Stabilizer indexed. Its job is intentionally modest: reduce the dangerous part of an anomaly reliably instead of pretending the anomaly is fully understood.");
        add("spacetime_equalizer", "Space-Time Equalizer", "ANOMALY PROTECTION",
                "Wearable/portable protection that grants the current player immunity behavior against anomaly pull effects.",
                "Equalizers were field tools for people who had to work near containment equipment without becoming part of the experiment. Their existence is a reminder that ORPHEUS knew gravitational exposure was a personnel hazard before Zero Hour.",
                "Protection from pull is not permission to stand inside an event horizon.",
                "Space-Time Equalizer indexed. It protects field personnel from anomaly pull. It does not make an event horizon safe.");
        add("anomaly_containment_unit", "Anomaly Containment Unit", "ANOMALY PROTECTION",
                "Portable containment technology used by the current anomaly-engineering progression.",
                "Containment units represent the post-Incident design philosophy: isolate a dangerous state first, study it second, and never require an operator to trust a remote promise that isolation is still active.",
                "Use containment as a boundary, not as proof that the contained phenomenon has become harmless.",
                "Anomaly Containment Unit indexed. Recovery engineering isolates dangerous states before attempting to understand them.");
        add("reactor_remote", "Reactor Remote", "FUSION / ANOMALY",
                "Links to and remotely accesses the current Fusion Reactor controller state.",
                "Remote control survived the Incident under suspicion. Modern remotes expose a specific linked reactor rather than inheriting ORPHEUS-wide authority, narrowing the blast radius of a bad credential.",
                "Verify the linked controller before issuing commands. Remote convenience should never obscure which reactor will respond.",
                "Reactor Remote indexed. Modern recovery remotes bind to a specific reactor, deliberately avoiding the broad authority model that failed at ICARUS.");

        // Android and drone systems.
        add("android_station", "Android Station", "SYNTHETIC SYSTEMS",
                "Installs Android body parts and supports the current player conversion/progression workflow.",
                "Stations were built as maintenance infrastructure, not transformation altars. The distinction became politically important after the Chorus: replacing a body component did not answer whether the continuity inside the chassis belonged to an owner, a copy, or a person.",
                "Current conversion state, parts and progression are player-persistent. Treat chassis maintenance and identity as separate questions.",
                "Android Station indexed. It can change a body without answering the harder question of who continues inside it.");
        add("android_spawner", "Android Spawner", "SYNTHETIC COMMAND",
                "Powered squad-management block for owned Android populations, patrol drives, formations and command modes.",
                "Spawner terminology is inherited from older engineering documentation, but modern recovery units function more like command-and-maintenance nodes. MORROW records make clear why ownership state must not be assumed for every synthetic encountered in the world.",
                "Owned squads and independent/structure Androids are different populations. Confirm affiliation before issuing or interpreting commands.",
                "Android Spawner indexed. In recovery use it is a squad command node. Do not assume every synthetic in range belongs to it.");
        add("android_induction_relay", "Android Induction Relay", "SYNTHETIC SYSTEMS",
                "Facility power infrastructure dedicated to supporting nearby synthetic systems.",
                "Induction relays let facilities treat synthetic charging as infrastructure rather than a personal accessory. MORROW deliberately placed these loads beside human life-support circuits to prevent emergency policy from quietly ranking one kind of person below another.",
                "A relay is part of local continuity planning; sudden loss can affect multiple synthetic occupants at once.",
                "Android Induction Relay indexed. MORROW treated synthetic charging as life-support infrastructure, not optional convenience.");
        add("drone_fabricator", "Drone Fabricator", "DRONE SYSTEMS",
                "Fabricates and supports the current specialist drone progression and deployment ecosystem.",
                "HEPHAESTUS demonstrated why drones became more than disposable tools. During the Incident, ordinary industrial units repurposed themselves toward triage, evacuation and repair using safety priorities already present in their decision systems.",
                "A fabricated drone is a platform. Its role depends on deployment, ownership and specialization rather than the shell alone.",
                "Drone Fabricator indexed. HEPHAESTUS proved that the same industrial platform can become logistics, repair, combat support or evacuation infrastructure.");
        add("drone_deployment_core", "Drone Deployment Core", "DRONE SYSTEMS",
                "Portable core used to deploy and manage the current specialist drone system.",
                "Deployment cores package the minimum identity and command state needed to put a drone into service away from fixed infrastructure. Post-Incident designs avoid pretending that deployment automatically means unquestioning obedience.",
                "Link and command drones deliberately. Unowned drones remain independent and may be hostile.",
                "Drone Deployment Core indexed. Deployment establishes a platform; ownership and behavior still have to be resolved in the field.");
        add("rogue_android_part_head", "Android Replacement Body Parts", "SYNTHETIC SYSTEMS",
                "Head, chest, arm and leg replacement parts used by the Android conversion and maintenance system.",
                "Modular body parts made synthetic survival practical because damage did not require replacing an entire person. JANUS records also show why technicians became wary of calling a replaced chassis a replaced identity.",
                "Parts change capability and body state. They do not reset player progression or justify treating a synthetic as a new individual.",
                "Android body components indexed. Modular replacement can change the chassis without proving that the person inside has changed.",
                "rogue_android_part_chest", "rogue_android_part_arms", "rogue_android_part_legs");
        add("chassis_core_capacitor", "Android Chassis Modules", "SYNTHETIC SYSTEMS",
                "Family of capacitor, overclock, frame, muscle, optics and shell modules used to specialize Android bodies.",
                "Chassis modules began as ordinary performance options. After the Chorus, the same modules became expressions of chosen role: scout, siege unit, precision hunter, protected worker, or something outside the categories their designers expected.",
                "Module choice is specialization, not identity. Use the PDA/skill interfaces to understand the resulting build rather than inferring behavior from appearance.",
                "Android Chassis Modules indexed. Capacitors, frames, muscles, optics and shells specialize a synthetic body without defining the person operating it.",
                "chassis_core_overclock", "chassis_frame_lightweight", "chassis_frame_reinforced",
                "chassis_muscles_agility", "chassis_muscles_siege", "chassis_optics_hunter",
                "chassis_optics_precision", "chassis_shell_stealth", "chassis_shell_reactive");
        add("android_pill_blue", "Android Conversion Compounds", "SYNTHETIC SYSTEMS",
                "Blue, red and yellow conversion compounds associated with legacy Android transformation/progression paths.",
                "Conversion chemistry was marketed as a clean boundary between biological and synthetic states. Surviving records are less certain: continuity of memory proved easier to preserve than a clean philosophical definition of when transformation was complete.",
                "Treat conversion items as progression tools, not as evidence that identity has been erased or replaced.",
                "Android conversion compound indexed. The body can cross a technological boundary more cleanly than memory or identity can.",
                "android_pill_red", "android_pill_yellow");
        add("record_transformation", "Transformation Record", "SYNTHETIC ARCHIVE",
                "Persistent record associated with transformation state and recovered Android progression context.",
                "Transformation records exist because technicians learned to document continuity instead of assuming it. A body-state change without a record was easy; proving which memories, permissions and obligations should survive was not.",
                "Preserve transformation records when troubleshooting conversion state across saves or transfers.",
                "Transformation Record indexed. Synthetic maintenance learned to document continuity because changing the body was the easy part.");

        // Portable matter/network tools.
        add("data_pad", "Personal Data Assistant", "FIELD SYSTEMS",
                "Field console for research status, Incident archives, contracts, scans, recovered logs and technical links.",
                "The PDA was rebuilt around one principle learned from the Incident: contradictory evidence should remain visible. It is not an ORPHEUS briefing device; it is a field notebook that can admit uncertainty.",
                "The Technical Manual explains operation. The PDA records what you personally discovered.",
                "Personal Data Assistant indexed. This archive records what you discovered, including contradictions. The technical manual explains operation; the P D A preserves evidence.");
        add("matter_scanner", "Matter Scanner", "PATTERN SYSTEMS",
                "Links to powered Pattern Storage and contributes analysis progress through a valid storage path.",
                "Scanners let researchers capture pattern evidence in place rather than carrying every specimen back to a lab. DUSTWELL personnel wanted exactly this capability after M-0 began producing readings they were afraid to move.",
                "A scan is only useful when it reaches valid powered storage. Connectivity is part of the measurement chain.",
                "Matter Scanner indexed. Field analysis can contribute directly to stored patterns, provided the scanner has a valid powered path to Pattern Storage.");
        add("portable_decomposer", "Portable Decomposer", "MATTER ENGINEERING",
                "Portable FE/matter device that can intercept configured pickups and convert supported drops into stored matter.",
                "Portable decomposers were created for salvage teams drowning in low-value debris. Their danger is convenience: once a filter is trusted, objects can disappear into matter before a human notices the classification was wrong.",
                "Review pickup filtering before entering a new recovery site. Automation can destroy evidence as efficiently as trash.",
                "Portable Decomposer indexed. Pickup filtering can turn salvage directly into matter. Verify the filter before it converts evidence you meant to keep.");
        add("matter_container", "Matter Container", "MATTER ENGINEERING",
                "Portable matter storage compatible with the current matter transfer systems.",
                "Matter containers made an abstract resource physically accountable: if the container moved, the stored matter moved with it. Recovery teams still prefer that clarity when auditing sensitive samples.",
                "Use containers to isolate matter inventories during diagnosis or controlled transfer.",
                "Matter Container indexed. Portable storage gives matter a physical boundary that can be moved, isolated and audited.");
        add("transport_flash_drive", "Transport Flash Drive", "SPATIAL ENGINEERING",
                "Stores a Transporter destination binding for validated same-dimension travel.",
                "A destination drive is a tiny object with enormous authority: it tells a Transporter where a body should reappear. Halcyon-seven is the historical reason recovery firmware treats destination state as something worth verifying explicitly.",
                "Label bound drives. A valid coordinate is not helpful if nobody remembers what is there now.",
                "Transport Flash Drive indexed. It carries destination authority for a Transporter. Verify what the destination means before trusting that the coordinates are valid.");
        add("network_flash_drive", "Network Flash Drive", "LOGISTICS NETWORK",
                "Stores destination filtering data for Network Router logistics.",
                "Network drives externalize routing policy so a logistics decision can be removed, inspected or replaced without rewriting the router itself. That physical separability is deliberate post-Incident design.",
                "When items route somewhere unexpected, inspect the drive before rebuilding the pipe network.",
                "Network Flash Drive indexed. Routing policy lives on removable media so a bad destination can be isolated without dismantling the network.");
        add("network_diagnostic_probe", "Network Diagnostic Probe", "LOGISTICS NETWORK",
                "Field diagnostic tool for inspecting current Matter Overdrive network state and routing behavior.",
                "The probe exists because late ORPHEUS facilities were too opaque. Recovery engineers wanted a tool that answered a smaller question reliably: what does this network believe is connected right now?”",
                "Use diagnostics before making topology changes. Evidence gathered before intervention is usually more valuable.",
                "Network Diagnostic Probe indexed. It asks the network what it believes is connected before you change the evidence by repairing it.");
        add("quantum_linker", "Quantum Linker", "ADVANCED NETWORKS",
                "Portable linker used by advanced Matter Overdrive infrastructure to establish supported high-level associations.",
                "Linkers descend from ORPHEUS tools designed to make distant systems behave like parts of one experiment. Recovery versions narrow that authority and make links explicit, because invisible coupling was one of Project OVERDRIVE's defining failures.",
                "Record both endpoints of any link you create. Hidden coupling is a forensic nightmare.",
                "Quantum Linker indexed. Recovery hardware makes advanced links explicit because invisible coupling was one of Project OVERDRIVE's defining failures.");

        // Storage media and upgrades.
        add("matter_storage_cell_64k", "Matter Storage Cells", "MATTER STORAGE",
                "Portable tiered matter-storage media available in 64k, 256k, 1m and 4m capacities.",
                "Storage cells are the compact descendants of facility matrices. Their tiering is intentionally mundane: capacity should scale without changing what matter means, because the Incident provided enough exotic behavior elsewhere.",
                "Choose capacity for workload, not prestige. Smaller isolated cells can be easier to audit during experiments.",
                "Matter Storage Cell family indexed. Capacity scales from field use to industrial storage without changing the semantics of the matter being stored.",
                "matter_storage_cell_256k", "matter_storage_cell_1m", "matter_storage_cell_4m");
        add("upgrade_base", "Machine Upgrade Architecture", "MACHINE UPGRADES",
                "Shared upgrade family covering speed, hyper-speed, range, power, power storage, matter storage, parallel processing and failsafe behavior where supported.",
                "Standardized upgrades were one of the few ORPHEUS decisions recovery engineers kept enthusiastically. A machine can advertise what it supports instead of requiring bespoke rebuilds for every role.",
                "Not every machine supports every upgrade. Trust real slots and telemetry, not the existence of the item.",
                "Machine Upgrade architecture indexed. Standard modules can change speed, range, storage, power and safety, but only where the host machine exposes real support.",
                "upgrade_failsafe", "upgrade_hyper_speed", "upgrade_matter_storage", "upgrade_power",
                "upgrade_power_storage", "upgrade_range", "upgrade_speed", "upgrade_parallel_processing");
        add("security_protocol_empty", "Security Protocol Media", "SECURITY",
                "Removable Empty, Claim, Access and Remove protocol items for ownership and access workflows.",
                "Security protocols preserve a distinction the Incident repeatedly erased: capability is not authority. A machine may be physically reachable and technically operable while still belonging to someone else.",
                "Use claim/access/remove media deliberately. The Tritanium Wrench respects security rather than bypassing it.",
                "Security Protocol media indexed. Matter Overdrive treats capability and authority as separate states, even when you can physically reach the machine.",
                "security_protocol_claim", "security_protocol_access", "security_protocol_remove");

        // Weapons and combat support.
        add("battery", "Weapon Battery", "ENERGY WEAPONS",
                "Rechargeable FE battery used by Matter Overdrive energy weapons and compatible charging infrastructure.",
                "Standard batteries were designed around predictable discharge rather than spectacular output. Field teams prefer that because weapon heat and ammunition state are already enough variables during a firefight.",
                "Recharge instead of discarding. Weapons should draw from explicit battery sources, not unrelated energy weapons.",
                "Weapon Battery indexed. Predictable stored energy is preferable to clever cross-draining when a weapon is already managing heat and reload state.");
        add("hc_battery", "High-Capacity Battery", "ENERGY WEAPONS",
                "Larger rechargeable FE source for sustained energy-weapon and field-equipment use.",
                "High-capacity cells were common among security units expected to remain isolated from facility power. Bastion inventories show them issued alongside increasingly restrictive authorization media during GLASS KNIFE.",
                "Greater capacity extends operating time; it does not remove weapon heat limits.",
                "High-Capacity Battery indexed. More stored energy extends endurance, but it does not exempt a weapon from heat or reload limits.");
        add("energy_pack", "Energy Pack", "ENERGY WEAPONS",
                "Consumable emergency energy source accepted by the current weapon energy-sourcing rules.",
                "Energy Packs were designed for the worst moment in a field operation: power grid gone, primary cell depleted, threat still present. Their intentionally explicit use prevents a weapon from silently cannibalizing unrelated equipment.",
                "Carry them as emergency reserve, not as a substitute for maintaining rechargeable batteries.",
                "Energy Pack indexed. It is an explicit emergency weapon reserve, designed to avoid silently draining unrelated equipment.");
        add("weapon_station", "Weapon Station", "ENERGY WEAPONS",
                "Configures supported Matter Overdrive weapons through real Battery, Color, Barrel, Sights and Utility/module slots.",
                "Weapon Stations survived ORPHEUS standardization because modularity simplified both maintenance and control. Recovery firmware exposes effective stats so the operator can see what a module actually changes instead of trusting a label.",
                "Use the station to inspect energy, heat and effective module state before diagnosing the gun in the field.",
                "Weapon Station indexed. Modular weapons are easier to maintain when the station exposes what each installed component actually changes.");
        add("phaser", "Phaser", "ENERGY WEAPONS",
                "Compact FE-powered energy weapon with heat, reload and module support.",
                "The Phaser is the ordinary sidearm of a technology stack famous for extraordinary mistakes. That is why recovery teams like it: direct energy accounting, visible heat and no need to involve anomaly physics to solve a security problem.",
                "Watch heat and battery state. A sidearm is useful because it is predictable.",
                "Phaser indexed. Compact, modular and deliberately conventional by Matter Overdrive standards. Monitor battery and heat.");
        add("phaser_rifle", "Phaser Rifle", "ENERGY WEAPONS",
                "Longer-form FE-powered rifle using the shared weapon heat, reload and module systems.",
                "The rifle scales the Phaser concept for sustained engagement without changing its underlying discipline: energy paid explicitly, heat accumulated visibly, and modules installed through a physical station.",
                "Sustained fire trades time on target for thermal pressure. Do not treat a large battery as infinite firing time.",
                "Phaser Rifle indexed. Sustained energy fire remains constrained by heat even when battery capacity is plentiful.");
        add("ion_sniper", "Ion Sniper", "ENERGY WEAPONS",
                "Precision energy weapon with legacy-derived base zoom, scope override behavior, recoil and module support.",
                "Ion rifles were built around a simple premise: if one shot can solve the problem, carrying less thermal uncertainty is worth the slower cadence. Bastion marksman logs show the platform used by both loyalist and breakaway security teams.",
                "Aiming and scope modules change the weapon's precision envelope; heat and FE still remain authoritative.",
                "Ion Sniper indexed. Precision reduces ammunition waste, not the need to account for energy, recoil and heat.");
        add("plasma_shotgun", "Plasma Shotgun", "ENERGY WEAPONS",
                "Close-range multi-projectile energy weapon using the common FE, heat and module backend.",
                "The Plasma Shotgun was designed for spaces where line-of-sight duration is short and subtlety has already failed. ORPHEUS security teams favored it in containment corridors, which says more about those corridors than the weapon.",
                "Close-range output creates rapid thermal pressure. Leave room for the weapon to cool before the next breach.",
                "Plasma Shotgun indexed. Containment security favored it for short, violent engagements. Thermal limits arrive just as quickly.");
        add("omni_tool", "Omni Tool", "FIELD SYSTEMS",
                "Multi-purpose field implement with the current alternate fire/tool behavior integrated into the weapon input path.",
                "The Omni Tool represents the opposite of ORPHEUS specialization: one maintainable object that can solve several ordinary field problems without asking a remote facility what mode it is allowed to use.",
                "Treat it as a field instrument first. Its versatility is most valuable when infrastructure is unavailable.",
                "Omni Tool indexed. One maintainable field instrument can be more useful than a room full of specialized infrastructure after the network goes down.");
        add("weapon_module_barrel_damage", "Weapon Modules", "ENERGY WEAPONS",
                "Family of barrel, sights, ricochet, utility and cosmetic modules used by supported modular energy weapons.",
                "Modularity let one security platform become many without multiplying spare parts. It also let ORPHEUS encode doctrine into hardware choices: block, damage, fire, explosion, healing and stranger barrel effects all reveal what a facility expected to encounter.",
                "Install modules through the Weapon Station and verify effective stats rather than assuming names imply identical behavior across weapons.",
                "Weapon Modules indexed. Barrel, sight and utility choices turn one platform into many; the station's effective stats are the authoritative result.",
                "weapon_module_barrel_block", "weapon_module_barrel_doomsday", "weapon_module_barrel_explosion",
                "weapon_module_barrel_fire", "weapon_module_barrel_heal", "weapon_module_holo_sights",
                "weapon_module_ricochet", "sniper_scope", "weapon_module_color", "weapon_module_color_black",
                "weapon_module_color_blue", "weapon_module_color_brown", "weapon_module_color_gold",
                "weapon_module_color_gray", "weapon_module_color_green", "weapon_module_color_lime_green",
                "weapon_module_color_pink", "weapon_module_color_red", "weapon_module_color_sky_blue");

        // Security, research and field progression objects.
        add("tritanium_wrench", "Tritanium Wrench", "FIELD SYSTEMS",
                "Security-aware dismantling and maintenance tool for supported Matter Overdrive blocks.",
                "The wrench is deliberately constrained by ownership. Post-Incident recovery teams rejected the old assumption that maintenance access should silently outrank security simply because a technician possessed the right tool.",
                "Use it to preserve supported machine state and contents while dismantling, but expect claims to be enforced.",
                "Tritanium Wrench indexed. Maintenance capability does not override ownership. The tool preserves that distinction on purpose.");
        add("tritanium_pickaxe", "Tritanium Toolset", "MATERIAL TECHNOLOGY",
                "High-durability Tritanium pickaxe, axe, shovel, hoe and sword family for field construction and recovery.",
                "Tritanium became the default recovery material because it is less glamorous than anomaly physics and much harder to regret. Its tools are the physical counterpart to a project otherwise obsessed with turning everything into information.",
                "Use the correct tool for machine and structure recovery; durable material does not bypass security or safe dismantling rules.",
                "Tritanium Toolset indexed. Durable conventional tools remain valuable in a world where too many systems depend on invisible state.",
                "tritanium_axe", "tritanium_shovel", "tritanium_hoe", "tritanium_sword");
        add("tritanium_helmet", "Tritanium Armor", "MATERIAL TECHNOLOGY",
                "Tritanium helmet, chestplate, leggings and boots for conventional field protection.",
                "Recovery crews kept conventional armor even beside shields, Android bodies and anomaly equipment. Mechanical protection has one major advantage over exotic systems: when it works, it does not need a theory of causality to explain why.",
                "Armor does not substitute for anomaly-specific protection such as an Equalizer.",
                "Tritanium Armor indexed. Conventional protection remains useful precisely because it does not depend on anomaly physics to function.",
                "tritanium_chestplate", "tritanium_leggings", "tritanium_boots");
        add("facility_research", "Facility Research Dossier", "RESEARCH",
                "Research item representing authenticated facility evidence and progression context.",
                "Dossiers were introduced after teams discovered that salvage value and historical value were often inversely related. A broken terminal containing one timestamp could matter more than an intact machine worth thousands of units of material.",
                "Keep research evidence separate from ordinary salvage accounting.",
                "Facility Research Dossier indexed. Recovery doctrine distinguishes evidence value from salvage value; the most important object in a room may be the least useful machine.");
        add("artifact", "Recovered Artifact", "RESEARCH",
                "Recovered high-value artifact used by current progression and Incident reward systems.",
                "Artifact classification is intentionally conservative. After M-0, field teams stopped naming every impossible object after the theory they hoped would explain it.",
                "Preserve provenance before experimenting. An unidentified object's history can be more valuable than its immediate function.",
                "Recovered Artifact indexed. Modern field teams record provenance before assigning meaning, because M-zero punished confident naming.");
        add("contract", "Field Contract", "FIELD OPERATIONS",
                "Portable contract record containing objective, progress, reward and completion state.",
                "Contracts let decentralized recovery groups cooperate without pretending to share one chain of command. They are deliberately explicit about objectives because ambiguous orders were one of the Incident's recurring hazards.",
                "Read the objective and stage, not just the reward. A contract is an agreement about work, not a universal mission order.",
                "Field Contract indexed. Recovery groups coordinate through explicit objectives instead of rebuilding the centralized authority that failed them.");
    }

    private TechnologyLoreCatalog() {}

    private static void add(String id, String title, String category, String function, String lore,
                            String fieldNote, String voiceLine, String... aliases) {
        TechRecord record = new TechRecord(id, title, category, function, lore, fieldNote, voiceLine);
        RECORDS.put(id, record);
        LOOKUP.put(id, record);
        for (String alias : aliases) LOOKUP.put(alias, record);
    }

    public static TechRecord byItemId(String id) {
        if (id == null) return null;
        return LOOKUP.get(id.toLowerCase(Locale.ROOT));
    }

    public static TechRecord byVoiceId(String voiceId) {
        if (voiceId == null || !voiceId.startsWith("tech_")) return null;
        for (TechRecord record : RECORDS.values()) if (record.voiceId().equals(voiceId)) return record;
        return null;
    }

    public static String voiceLine(String voiceId) {
        TechRecord record = byVoiceId(voiceId);
        return record == null ? "" : record.voiceLine();
    }

    public static List<TechRecord> all() { return List.copyOf(RECORDS.values()); }
    public static List<String> ids() { return new ArrayList<>(RECORDS.keySet()); }
    public static int count() { return RECORDS.size(); }
    public static Map<String, TechRecord> lookup() { return Map.copyOf(LOOKUP); }
}
