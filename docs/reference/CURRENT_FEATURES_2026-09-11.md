# Matter Overdrive 1.20.1 — Current Feature Inventory

Date: 2026-09-11  
Branch: `main`  
Release line: `0.6`  
Target: Minecraft 1.20.1 / Forge 47.4.10 / Java 17

> **Important:** this document describes features currently present in the source tree. Some systems remain under runtime/balance/visual verification. The retired Star Map is intentionally excluded and is not part of the active port.

# Brief Version

Matter Overdrive 0.6 currently includes a working matter economy and replication chain; FE power generation/storage/distribution; Matter, item and data-network logistics; transporters; a late-game Fusion Reactor and gravitational-anomaly system; Android conversion and a large class/subclass progression system; owned Android squads and specialist drones; modular energy weapons; security/ownership tools; extensive world structures and salvage; persistent facility restoration; a large incident/lore campaign; branching NPC dialogue and relationship state; advancements; field contracts/operations; a multi-tab PDA with facility, lore and technology archives; environmental warnings; first-discovery narration; optional GuideME/JEI integration; and extensive debugging/validation tooling.

The exploration/story layer is now built around sixteen canonical Matter Overdrive facilities, sixteen primary Incident records, five major Incident reconstructions, thirty-two optional physical field logs, facility-specific encounters, collectible lore in chests/caches, contemporary human and synthetic NPCs, and first-discovery PDA narration for facilities, lore records and major technologies.

The prerecorded PDA voice system is optional. It prefers local processed neural-VA WAV files, then bundled WAVs, then local operating-system speech, Minecraft Narrator and captions. A local production pipeline exports the complete voice queue, applies the approved synthetic processing preset and can bundle or inject the finished WAV bank into a built JAR.

Major areas still considered partial/testing include final structure traversal/balance validation, some weapon visual choreography and module mesh placement, drone flying/render/equipment parity, some textures/transparency, and late-game balance tuning.

# Detailed Version

## 1. Platform, build and release baseline

- Minecraft 1.20.1.
- Forge 47.4.10.
- Java 17 toolchain.
- Mod ID `matteroverdrive`.
- Current release line `0.6`.
- `main` is the active development/release baseline.
- Local Gradle build, client-run, packaging and validation batch files are included.
- Build-time bundled-documentation consistency checks prevent several repository docs from drifting from their in-game copies.

## 2. Matter economy and matter-value diagnostics

- Recursive, level-aware matter-value resolver.
- Explicit legacy/material values remain authoritative when defined.
- Dynamic Matter Dust valuation.
- Tag-based material valuation for broad vanilla material groups.
- Recipe-derived values for craftable items.
- Recipe output-count division prevents multi-output recipes from inflating value.
- Cheapest-positive ingredient alternatives are considered where recipes allow multiple ingredients.
- Cycle and recursion-depth protection prevents pathological recipe graphs from hanging valuation.
- Conservative fallback values prevent ordinary obtainable items from silently becoming zero-value matter.
- Matter Analyzer and Matter Decomposer use the same effective value path.
- Pattern Drives preserve analyzed matter values.
- Replicator consumes the matter value stored with the analyzed pattern.
- Item tooltips expose matter value.
- Shift-expanded tooltips identify whether the value source is explicit, tag-based, recipe-derived, dynamic or fallback.
- Startup auditing reports matter-value coverage.
- `/matteroverdrive matter value` reports the held item value/source.
- `/matteroverdrive matter audit` supports economy auditing.
- `/matteroverdrive matter clearcache` clears resolver caches for verification.

## 3. Matter Decomposer and Recycler

- Matter Decomposer consumes FE and matter-valued items into stored matter.
- Failure behavior can produce Matter Dust.
- Decomposer state, inventory and upgrades persist.
- Matter Recycler processes Matter Dust into refined/recovered material while preserving represented matter value.
- Recycler persistence and upgrade handling are implemented.
- Wide operator GUIs expose real HOME/TASKS/UPGRADES state instead of decorative controls.

## 4. Matter Analyzer, Scanner and Pattern Drives

- Matter Analyzer creates reusable item patterns.
- Pattern analysis progresses over time rather than instantly.
- Normal Pattern Drives hold multiple patterns.
- Completed patterns record item identity and matter cost.
- Pattern data persists when drives are removed/reinserted.
- Matter Scanner can contribute field analysis through a valid powered Pattern Storage path.
- Scanner connectivity is part of the measurement chain; isolated scans do not fake progress.

## 5. Pattern Storage, Pattern Monitor and Replicator

- Pattern Storage exposes completed patterns to the network.
- Multiple real Pattern Drive slots are retained in the UI.
- Pattern Monitor browses exposed patterns.
- Pattern Monitor can queue replication requests through real ghost/request slots.
- Replicator consumes FE, matter and a valid completed pattern.
- Replication queues and failure behavior are implemented.
- Pattern/data networking remains separate from FE transport and Matter Pipe transport.

## 6. Portable matter equipment

- Portable Decomposer stores FE and matter.
- Configured pickup interception can convert supported drops directly into matter.
- Portable Decomposer matter can be transferred to compatible receivers.
- Matter Containers provide portable matter storage.
- Matter Storage Cells provide tiered portable storage capacities.
- Matter Storage Matrix provides high-capacity fixed storage infrastructure.

## 7. Molecular Inscriber and fabrication

- Powered Molecular Inscriber.
- Mk1 -> Mk2 -> Mk3 -> Mk4 isolinear/circuit progression.
- FE, progress, inventory and upgrades persist.
- The Inscriber remains a deterministic local fabrication path distinct from Matter replication.

## 8. Utility machines

- Microwave uses valid smelting results as a powered food-processing path.
- Microwave waits for output space rather than deleting/overwriting output.
- Space-Time Accelerator consumes FE and matter to apply additional ticks to eligible nearby blocks/block entities.
- Accelerator speed/range/power/storage upgrades affect operating behavior.
- Large accelerator ranges are deliberately treated as performance-sensitive.
- Charging Station recharges supported energy items.
- Charging Station supports current nearby Android wireless-charging behavior.
- Grid Capacitor/facility power-buffer concepts are present in the current technology layer where supported by the runtime implementation.
- Facility/network control infrastructure exposes only real backend state.

## 9. FE generation, storage and transport

- Solar Panel generates FE under valid daylight conditions.
- Solar Panel GUI reports generation, sky/light/daylight state, buffer and output telemetry.
- Rechargeable standard Battery.
- Rechargeable High-Capacity Battery.
- Explicit Energy Pack emergency weapon-energy source.
- Heavy Energy Cable is the primary FE transport path.
- Cable chains transfer FE between compatible machines.
- Cable status/telemetry reflects actual buffer/output state where exposed.
- FE, matter and data routes remain intentionally separate even when visually adjacent.

## 10. Matter Pipe and matter routing

- Matter Pipe moves stored matter between compatible matter endpoints.
- Multi-pipe chains are supported.
- Decomposer -> Matter Pipe -> storage/Replicator/Fusion Reactor IO is an intended working route.
- Matter routing is not treated as FE or item logistics.

## 11. Network Pipe, Switch, Router and Pylons

- Network Pipe carries Matter Overdrive network connectivity.
- Network Switch persists enabled/disabled state and changes active routing topology.
- Network Router moves items across the network.
- Router ordinary filters are supported.
- Network Flash Drive destination filtering is supported.
- Router has Speed/Hyper-Speed upgrade support.
- Router executes using real per-item FE cost.
- Multi-stack item budgets are supported.
- Matching-channel Pylons provide the current local wireless bridge.
- Network diagnostic tooling reports real connectivity rather than inventing a fake controller.
- Per-side policies/priorities are present in the current network hardening layer.
- Replacement machines should return to default side policy rather than inherit stale positional configuration.

## 12. Hybrid Conduit and combined infrastructure

- Hybrid Conduit supports the modern combined-infrastructure direction where implemented.
- FE and Matter routing can coexist without being collapsed into one logical resource.
- Visual connection compatibility with Heavy Energy Cable and Matter/Network pipes is part of the current test contract.

## 13. Transporter

- Same-dimension entity transport.
- Transport Flash Drive stores/binds a destination.
- Transporter validates destination, range, FE and cooldown.
- Distance-scaled FE use.
- Exact-required-FE transport is intended to succeed.
- Multiple entities can be transported in a cycle up to the supported limit.
- Transport destinations must be loaded; the Transporter does not intentionally force-load an unloaded destination.
- Arrival searches for safe collision-free supported space.
- Failed arrival should not consume transport FE or start cooldown when nothing can be moved.
- Partial valid-arrival cases report actual moved count.

## 14. Fusion Reactor

- Horizontal reactor multiblock validation.
- Reactor Controller.
- Fusion Reactor Coil geometry checks.
- Reactor IO shared matter/FE interface.
- Anomaly-mass-scaled output.
- Reactor upgrades.
- Heavy Energy Cable output.
- Connected-demand telemetry.
- Shared reactor-ring power behavior.
- RUN/SCRAM control.
- Redstone behavior.
- Comparator behavior where applicable.
- Reactor Remote linking/access.
- Persistent assembly/debug overlay.
- Reactor/stabilizer integration where configured.
- Late-game reactor state persists across save/reload.

## 15. Gravitational Anomalies

- Natural and reactor-linked Gravitational Anomaly backend.
- Entity/item attraction.
- Event horizon.
- Matter-bearing drops and consumed living entities can contribute mass through the anomaly backend.
- Anomaly mass affects hazard/output behavior.
- Natural anomalies can generate conservatively in fresh chunks.
- Natural anomalies use the real anomaly block entity rather than a fake decorative worldgen object.

## 16. Stabilizers, Equalizer and anomaly protection

- Powered Gravitational Stabilizer.
- Direction/orientation checks.
- Beam obstruction/line-of-sight state.
- Redstone mode.
- Anomaly-lock telemetry.
- Upgrade slots.
- Stabilizers reduce effective danger only when correctly supplied/configured.
- Space-Time Equalizer provides current player protection from anomaly pull/event-horizon hazard behavior according to the active implementation.
- Anomaly Containment Unit exists in the anomaly-engineering progression.

## 17. Android conversion and persistent Android state

- Android conversion path.
- Android Station.
- HEAD/CHEST/ARMS/LEGS body-part installation.
- Persistent Android FE.
- Persistent installed body parts.
- Android level/XP progression.
- Persistent selected ability/loadout state.
- Android HUD.
- Keybind-driven ability cycling/activation/tree access.
- Android state survives normal relog/death/save flows according to the intended persistence contract.

## 18. Android class/subclass progression

- Three top-level classes: Strider, Juggernaut and Architect.
- Three subclasses per class for a 3x3 specialization matrix.
- Unique class/tech/Ultimate ability packages.
- Nine distinct Ultimates across the subclass matrix.
- Selectable Aspects.
- Selectable Fragments.
- Selectable Passive Protocols spanning combat, mobility, defense, energy and drone support.
- Android Mastery progression.
- Permanent mastery perks.
- Refund/reset support without intentionally erasing unrelated progression.
- Nonlinear XP thresholds are reflected by the HUD/tree.
- Ascension Point progression through Android levels.

## 19. Android chassis modules

- Capacitor Core changes effective Android energy capacity.
- Overclock/frame/muscle/optics/shell specialization families.
- Chassis capacity affects HUD/Station/tree thresholds rather than only item text.
- Stored FE clamps when capacity is reduced so removed modules cannot hide banked energy for later restoration.

## 20. Android squads and Android Spawner

- Powered Android Spawner squad management.
- Up to six owned Androids in the intended managed population.
- Approximate 30% melee / 70% ranged managed composition.
- Persistent ownership.
- Six patrol-drive slots.
- PATROL mode.
- GUARD mode.
- HOLD mode.
- ESCORT mode.
- Commander formations.
- Squad colours.
- Coordinated targeting.
- Owned-unit kill/cleanup controls.
- Natural/structure Androids remain distinct from player-owned squad capacity.
- Owned squads should not attack their owner/allies or be silently stolen by another player.

## 21. Android and synthetic entities

- Rogue Android.
- Ranged Rogue Android.
- Defector/friendly Android roles.
- ORPHEUS Directive-0 Enforcer heavy ranged remnant.
- M-0 Resonant Android hostile resonance variant.
- Legacy Android textures use atlas-aware custom model layers rather than generic zombie atlas assumptions.
- Neutral synthetic contacts can defend against hostile monsters without defaulting to player hostility.

## 22. Drones

- Drone entity.
- Player linking/ownership flow.
- FOLLOW behavior.
- DEFENSIVE behavior.
- PASSIVE behavior.
- AGGRESSIVE behavior.
- Owned drones protect owners/allies and reject friendly targets.
- Specialist drone progression includes Combat, Repair, Logistics, Survey and Reactor Maintenance roles in the broader current system.
- Drone Core / deployment infrastructure.
- Drone Fabricator.
- Drone Matrix progression through level 10.
- Fleet/commander integration and drone-oriented Android progression hooks.
- Full legacy flying navigation/render/equipment parity remains partial.

## 23. Energy weapons

- Phaser.
- Phaser Rifle.
- Ion Sniper.
- Plasma Shotgun.
- Omni Tool / field-tool weapon path.
- Weapons require valid energy sources.
- Weapon heat.
- Overheat.
- Reload behavior.
- Weapon-local energy storage.
- Explicit Battery / HC Battery / Energy Pack sourcing.
- Unrelated energy weapons are not intended to be treated as reload batteries.
- Survival and Creative both use the same authoritative energy/heat logic.

## 24. Weapon Station and modules

- Weapon Station persistent inventory/state.
- Typed slots for Weapon, Battery, Color, Barrel, Sights and Utility/module categories.
- Module effects update effective weapon stats.
- Battery changes clamp weapon energy to the final packed capacity.
- Opening/closing the station without changing the battery should not destroy valid stored charge.
- Weapon module families include barrel effects, sights/scope, ricochet, utility and cosmetic colours.
- Sniper Scope has the recovered legacy-style zoom override behavior.
- Ion Sniper uses the recovered legacy base-zoom reference.

## 25. Weapon renderer/input work

- Native Matter Overdrive first-person weapon renderer path.
- Guns remain visible immediately after equip rather than relying on vanilla use-item poses.
- Hip-fire sway.
- Draw/lower transition.
- ADS movement-bob reduction.
- Shot-driven model recoil.
- Shot-driven camera recoil.
- Charge presentation for Ion Sniper / Plasma Shotgun.
- Sight/scope attachments stay associated with the weapon transform.
- Off-hand suppression while the Matter Overdrive gun owns first-person presentation.
- Third-person/GUI/dropped/fixed contexts have explicit rendering support.
- Exact final module mesh placement and complete original hand/weapon animation choreography remain partial/testing.

## 26. Security and ownership

- Empty Security Protocol media.
- Claim protocol.
- Access protocol.
- Remove protocol.
- Ownership/access checks.
- Tritanium Wrench respects security rather than bypassing ownership.
- Supported dismantling preserves machine state/contents where designed.
- Generated facility security doors can be restoration-gated.

## 27. Materials, tools and armour

- Tritanium.
- Dilithium.
- Matter-related materials/dust progression.
- Correct tool-gated mining behavior for Matter Overdrive ores.
- Correct ore drops with appropriate tools.
- Tritanium pickaxe.
- Tritanium axe.
- Tritanium shovel.
- Tritanium hoe.
- Tritanium sword.
- Tritanium helmet/chestplate/leggings/boots.
- Tritanium Wrench.

## 28. Legacy structure families

Six restored legacy exploration families remain active in fresh world generation:

1. Crashed Ship / Halcyon-7 wreck family.
2. Cargo Ship / Atlas Freight family.
3. Underwater Base / NEREID family.
4. Mad Scientist House / Voss research residence/lab family.
5. Android House / MORROW safehouse family.
6. Sand Pit / DUSTWELL excavation family.

These use chunk-safe native structure/piece work in the current structure direction, contain salvage/story caches and can have persistent occupants/encounters. Exact legacy PNG-template parity is not treated as complete.

## 29. Modern facility families

The modern exploration set includes:

1. Synthetic Manufacturing Plant / HELIX.
2. Matter Refinery / KESTREL.
3. Quantum Relay Station / ECHO-9.
4. Android Command Bunker / Bastion.
5. Fusion Research Complex / ICARUS.
6. ORPHEUS Black Site.

Current work includes multiple traversable layouts, terrain integration, approach/entrance readability, secure areas, story/salvage caches, themed defenders and facility restoration state.

## 30. Frontier facility families

The frontier exploration set includes:

1. Deep Matter Vault / MNEMOSYNE.
2. Autonomous Drone Foundry / HEPHAESTUS.
3. Anomaly Quarantine Site / JANUS.
4. Orbital Recovery Array / LAGRANGE.

These extend late-game exploration into Matter storage, drone autonomy, anomaly medicine/containment and the causal-loop recovery chain.

## 31. Structure topology and worldgen safety

- Native `Structure` / `StructurePiece` style chunk-safe generation is the active direction.
- Large synchronous Feature stamping is intentionally avoided for the modern structure work.
- Structure traversal is designed around obvious approaches, entrances, critical paths, guarded focal objectives, optional salvage/story spaces and safe return paths.
- Structure topology validators check reachability assumptions.
- Terrain integration uses bounded local support/apron/pier work rather than giant terrain destruction.
- Worldgen/presentation/population code is designed not to force-load chunks.
- Save/reload/deserialization safety is part of the current structure test contract.

## 32. Facility restoration and security loop

- Emergency power restoration.
- Control repair.
- Secure-access restoration.
- Matching facility research dossier requirement where applicable.
- Restored-state persistence.
- Generated security doors can remain locked before control repair.
- Facility caches resolve themed loot once rather than refilling indefinitely.
- Facility-specific defenders/reserves can stand down when restoration completes without deleting already deployed defenders.

## 33. Facility populations and encounters

- Contemporary structure populations are separate from authored security spawners.
- Finite per-structure-start population ledger.
- Loaded-chunk-only candidate checks.
- Bounded candidate-search radius/check count.
- Floor/headroom validation.
- Structure-piece containment checks.
- Persistent home restrictions.
- No chunk-force-loading API in the population path.

Contemporary human roles:

- Field Researcher.
- Salvage Specialist.
- Recovery Specialist.
- Reactor Recovery Engineer.
- Anomaly Field Medic.
- Incident Archivist.

Independent synthetic roles:

- MORROW Scout.
- Chorus Courier.
- HEPHAESTUS Liaison.

## 34. Legacy/restored creatures and scientists

- Mad Scientist.
- Mutant Scientist.
- Failed Cow.
- Failed Pig.
- Failed Sheep.
- Failed Chicken.
- Puny Humans progression.
- Cocktail of Ascension progression.
- Transformation only commits after the replacement Mutant successfully spawns.

## 35. Overdrive Incident story campaign

The active campaign is built around the Overdrive Incident rather than the retired Star Map.

Core themes include Matter as information-like state, Android memory/continuity, anomaly causality and Project OVERDRIVE's attempt to couple Matter, anomaly/fusion physics and synthetic cognition.

The sixteen canonical facilities collectively reconstruct the M-0 -> OVERDRIVE -> ICARUS -> LAGRANGE causal loop and the political/synthetic consequences around ORPHEUS, GLASS KNIFE, MORROW, the Chorus and HEPHAESTUS.

## 36. Sixteen primary facility records

- One canonical primary archive record per major facility.
- First facility authentication awards that structure family's primary Incident evidence.
- Primary records retain stable structure/discovery identifiers for save compatibility.
- Archive presentation is chronological even though discovery order is non-linear.

## 37. Five Incident reconstructions

Five larger reconstructions unlock from corroborated primary evidence:

- THE MATTER CHAIN.
- THE IMPOSSIBLE SIGNAL.
- THE SYNTHETIC SCHISM.
- PROJECT OVERDRIVE.
- THE CLOSED LOOP.

These provide XP/progression feedback and culminate in the Closed Loop conclusion/warning without requiring a restored Star Map.

## 38. Optional physical lore records

- Thirty-two optional physical field records.
- Two optional records associated with each canonical facility.
- Records include worker notes, maintenance tickets, medical addenda, private messages, security notices, synthetic memory fragments, drone decision traces and recovery tags.
- Optional records do not gate the main Incident reconstruction.
- `recovered_lore_fragment` carries a stable lore ID.
- Right-click displays classification/source/excerpt/PDA analysis.
- First authentication awards XP and queues a dedicated lore callout.
- Duplicate copies remain readable but cannot farm progression.
- Physical fragments remain in the player's possession after reading.

## 39. Lore in structures and chests

- Shared salvage tables can produce optional lore.
- Shared story caches strongly favor documentary/lore outcomes.
- Modern facilities bias local records toward documents authored for that facility.
- Legacy and Frontier structures can surface displaced/cross-facility evidence through shared salvage/story-cache tables.
- Already-generated chests are not expected to retroactively gain new loot; fresh structures/chunks are the correct test path.

## 40. PDA / Personal Data Assistant

The Data Pad/PDA is the player-discovery archive and field console. It is deliberately separate from the Technical Manual.

Current tabs include:

- OVERVIEW.
- DATA BANK.
- INCIDENT.
- FACILITIES.
- FIELD LOGS.
- TECHNOLOGY.
- RESEARCH.
- OPERATIONS.
- CONTRACTS.
- SCAN LOG.

The PDA tracks what the player has actually discovered rather than merely exposing every page from the start.

## 41. PDA primary archive browsing

- DATA BANK browses recovered primary Incident records.
- Multi-section record presentation.
- Chronological archive indexing.
- Locked/unrecovered entries remain hidden rather than leaking story text.
- Recovered excerpts and forensic analysis.
- Incident implication/cross-reference presentation.
- READ ALOUD / STOP support for authenticated pages.

## 42. FIELD LOGS PDA archive

- Dedicated optional-lore tab.
- Collection progress out of 32.
- Title/classification/site/source.
- Full recovered excerpt.
- PDA analysis.
- Previous/next browsing.
- Read-aloud support.

## 43. TECHNOLOGY PDA codex

- Persistent first-discovery technology archive.
- One canonical record per major technology or grouped family.
- Decorative/debug-only/raw crafting intermediates are intentionally excluded from narration noise.
- Records contain stable ID, title, category, function, lore context, field note and first-discovery voice line.
- Variant aliases collapse related families such as crate colours, Android body parts, chassis modules, storage-cell tiers, machine upgrades, security media, weapon modules and Tritanium equipment.
- First discovery can occur through crafting, pickup, placement, inventory acquisition or Data Pad scan.
- Discovery persists per player/world.
- Duplicate copies do not replay narration.
- Technology tab supports browsing and READ ALOUD.

## 44. PDA first-world onboarding and title presentation

- Dedicated Matter Overdrive title/landing-screen entry from the vanilla title screen.
- First-world player briefing.
- Multi-page introductory explanation of field link, field kit and Overdrive Incident.
- Briefing appears once per player/world through persistent SavedData.
- Existing worlds receive the onboarding once after updating rather than every login.
- Startup/UI cue integration.

## 45. Animated facility discovery banner

- Top-centre animated `FACILITY IDENTIFIED` presentation.
- Canonical facility name.
- Archive index/classification.
- Smooth slide/fade presentation.
- First-authentication semantics prevent repeated copies of the same facility family from spamming the banner.

## 46. Environmental hazard HUD

Persistent top-right hazard presentation supports advisory/warning/critical states.

Current hazard concepts include:

- structural damage in wrecks;
- pressure-compromised NEREID areas;
- M-0 resonance sites;
- legacy ORPHEUS security;
- acausal ECHO-9/LAGRANGE telemetry;
- ICARUS containment risk;
- anomaly/quarantine risk;
- nearby natural gravitational anomalies.

Natural anomaly warning scans are bounded and loaded-chunk-only.

## 47. PDA notification queue and captions

- Automatic PDA callouts are serialized.
- Only one short automatic line speaks at once.
- Critical warnings have higher queue priority.
- Automatic messages do not interrupt a line already speaking.
- Player-requested long-form narration takes priority over the automatic queue.
- Active automatic callout is captioned on-screen.
- No critical gameplay information exists only in audio.

## 48. PDA UI sounds

Programmatically generated original cue families include:

- startup;
- record acquired;
- facility identified;
- hazard warning;
- communications channel opening.

These are synthesized at runtime rather than relying on hosted binary assets.

## 49. PDA prerecorded/neural voice architecture

The preferred short-callout playback path is:

1. `config/matteroverdrive/pda_voice/<line_id>.wav` local override;
2. processed prerecorded WAV bundled in the JAR;
3. local OS TTS;
4. Minecraft Narrator;
5. captions/text remain available.

Windows fallback uses local System.Speech. macOS uses `say`; Linux can use `espeak`/`spd-say` where present.

No cloud TTS is required at runtime.

## 50. PDA voice-production tooling

- `EXPORT_PDA_VOICE_QUEUE.bat` exports the complete authored production queue.
- Export merges the stable core/facility/field-log manifest with all live `tech_*` technology lines.
- Raw performances are stored/named by stable line ID.
- `PROCESS_PDA_VOICE_BANK.bat` applies the approved ICARUS-style synthetic post-processing.
- Processed output is 48 kHz mono PCM WAV.
- `config/matteroverdrive/pda_voice` supports rapid replacement testing without a rebuild.
- `INJECT_PDA_VOICES_INTO_JAR.bat` can create a voiced JAR after the normal Gradle build.
- Binary voice assets do not need to be committed to GitHub.

## 51. NPC dialogue UI and hologram portraits

- Shared NPC dialogue screen.
- Human/synthetic presentation differences.
- Procedural hologram portrait panel.
- Cyan human contacts.
- Orange-accented synthetic contacts.
- Animated scanline/contact identity presentation.
- Portrait collapses on narrow windows to protect readable dialogue layout.
- READ ALOUD / STOP support for player-requested dialogue narration.

## 52. Branching NPC conversations

Nine contemporary dialogue profiles use server-authored conversation graphs:

Human:

- Field Researcher.
- Salvage Specialist.
- Recovery Specialist.
- Reactor Recovery Engineer.
- Anomaly Field Medic.
- Incident Archivist.

Synthetic:

- MORROW Scout.
- Chorus Courier.
- HEPHAESTUS Liaison.

Each profile has context-specific questions/branches rather than only one-way lore dumps.

## 53. Server-authoritative dialogue security

- Short-lived dialogue sessions are created from real NPC interaction.
- Client choices must match active dialogue ID.
- Client choices must match current server node.
- Choice ID must exist in the server catalogue.
- Client cannot submit arbitrary response text/rewards/trust values.
- Repeating the same authored topic remains readable but cannot repeatedly farm trust/insight progression.

## 54. Persistent NPC relationship/context state

Per-player/world dialogue state tracks:

- visit counts;
- last selected topic;
- durable narrative flags;
- Field Team Trust;
- Synthetic Trust;
- Archive Insight.

Higher context values alter later framing and let recurring contacts acknowledge prior interactions.

## 55. Advancements

The mod contains the main campaign progression plus newer mastery/social/archive branches.

Newer explicit milestones include:

- Pattern Architect.
- Network Specialist.
- Applied Energy Weapons.
- Containment Engineer.
- Industrialist.
- Field Liaison.
- Synthetic Liaison.
- Incident Analyst.
- Full-Spectrum Engineer.
- Field Archivist.
- Every Scrap Matters.

Social/archive milestones that depend on hidden server state use server-awarded impossible criteria rather than trusting inventory/client spoofing.

## 56. Research progression and Field Operations

- Research progression/roadmap exists outside GuideME.
- Mad Scientist and research-campaign quest flows.
- Field Operations provide repeatable event-driven assignments.
- Doctrines include Recovery, Systems and Anomaly.
- Operations include salvage/recovery, logistics recommission, observatory survey, containment audit and horizon-exposure style tasks.
- Completion awards XP and doctrine-appropriate rewards.
- Operations reassign within the chosen doctrine after completion where eligible.
- Contract Market / Field Contract concepts provide decentralized repeatable work.

## 57. Contracts and quest HUD

- Portable Field Contract item/state.
- Objective/progress/reward/completion data.
- PDA CONTRACTS tab.
- Contract abandonment flow with confirmation behavior.
- Active quest/contract tracker HUD for current progression states.
- Legacy contract compatibility where retained.

## 58. GuideME and built-in technical documentation

- PDA is the discovery/story/progression surface.
- GuideME is the optional technical manual.
- GuideME does not replace advancements, datapacks or server progression state.
- Technical Manual button opens GuideME when available.
- Built-in documentation remains the fallback.
- Paged guides/index navigation.
- Last-page persistence for built-in guide behavior.
- System guide includes simplified and detailed sections.

## 59. JEI integration

- JEI is optional at runtime.
- Development/runtime integration is configured through ForgeGradle dependencies.
- Core gameplay does not require JEI to exist.

## 60. Debugging, validators and tester tooling

The repository includes many purpose-specific static validators and test plans for:

- port/registry consistency;
- Android progression;
- weapons;
- network/transport;
- weapon rendering;
- structure expansion/topology;
- structure lore;
- PDA/GuideME integration;
- PDA presentation;
- PDA population/Android visuals;
- branching dialogue/advancements/QoL;
- PDA technology lore;
- bundled documentation consistency.

Root batch files provide common verification/build/client/package workflows.

## 61. Persistence model

Persistent state exists for major gameplay loops including:

- machine inventory/FE/matter/upgrades;
- reactor state;
- Android progression/loadout;
- patrol/squad state;
- contracts/quests;
- facility restoration;
- structure population ledgers;
- primary Incident discovery;
- optional lore recovery;
- technology codex discovery;
- first-world onboarding;
- NPC dialogue relationships/context.

## 62. Current known partial/testing areas

The following areas are implemented but remain important runtime/parity targets rather than being treated as final:

- complete in-world traversal audit of every structure/layout on varied terrain;
- final structure balance and encounter tuning;
- remaining structure-specific objectives/events;
- exact legacy structure-template parity where useful;
- final Android HUD/ability-cycle regression testing;
- Android progression reset/refund edge cases;
- drone flying navigation/render/equipment parity;
- some transparent block/crate/Inscriber texture/model issues historically reported and requiring regression verification;
- final anomaly/event-horizon mass and balance verification;
- weapon third-person hold/aim pose and use/place-animation regression verification;
- exact weapon module mesh placement;
- complete original first-person hand/weapon animation choreography;
- final runtime verification of all advancement JSON and loot tables;
- complete neural prerecorded voice bank production/bundling.

## 63. Explicitly retired content

The Star Map is retired and is **not** part of the active Matter Overdrive 1.20.1 port. Historical Star Map code/docs may still exist as archaeology in older files or branches, but current guides, registries, worldgen and new development should not restore it.

## 64. Practical status interpretation

A feature appearing in this document means there is an implementation in the current `main` source tree. It does not automatically mean every balance number, texture, renderer path, terrain case or multiplayer edge case is final. Use the current runtime test plans and local Forge build as the final verification gates.
