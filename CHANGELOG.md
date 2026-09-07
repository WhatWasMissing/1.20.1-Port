# Changelog

All notable changes to the Matter Overdrive 1.20.1 port are documented here.

## 0.6 — Matter economy, survival/world integration and release consolidation

### Added

- Recursive matter valuation across crafting/processing recipes, including output-count division, cheapest valid ingredient alternatives, cycle protection and bounded recursion depth.
- Deterministic fallback matter values for items without explicit, tag-based or resolvable recipe values.
- Matter-value source reporting through tooltips and diagnostic auditing.
- `/matteroverdrive matter value`, `/matteroverdrive matter audit` and `/matteroverdrive matter clearcache` diagnostic commands.
- Startup matter-value coverage auditing and dedicated matter-value coverage documentation.
- Restored/expanded survival-facing legacy world content, including structure generation, persisted salvage/inhabitants and natural Gravitational Anomaly generation.
- Expanded GuideME survival progression, current-feature and matter-technology documentation for the 0.6 release line.
- 0.6 release workflow and release identity updates for the current `main` branch.

### Changed

- Matter Analyzer and Decomposer now use the level-aware matter-value resolver so recipes can participate in effective item value calculation.
- Pattern Drives preserve analyzed matter values and Replicator uses the stored pattern value for production cost.
- The legacy no-Level matter API now uses the same fallback-aware value path instead of returning zero for non-explicit items.
- GuideME and repository feature/testing documentation now identify the current build as **Matter Overdrive 0.6** on `main` rather than the obsolete `testing/main` / `Alpha Version 3` identity.
- GuideME is treated as the primary player-facing how-to manual when installed, while the repository reference/testing files remain the detailed implementation and verification sources.
- Normal CI now also covers `release/**` branches and its legacy version-normalization step is idempotent.

### Testing status

A clean local Java 17 / Forge 1.20.1 build of the current 0.6 `main` head is still required before distribution. GitHub Actions attempts on the release line have previously failed before runner steps began, so those failures are not compile results. The 0.6 runtime checklist covers matter economy, reactor/anomaly, Android, weapons, networking/transporter, structures, Star Map, GUI and persistence regressions.

## 0.5 — Research campaign and progression overhaul

### Added

- New paginated NPC dialogue UI with wrapped text, Back, Continue, Close and page indicators.
- Scientist dialogue moved out of chat and into dedicated bottom-screen conversation boxes.
- Restored the six legacy Matter Overdrive scientist quests as one persistent campaign:
  - Crash Landing
  - We Must Know
  - G.M.O.
  - Trade Route
  - Self-Sealing Stem Bolts
  - To the Power Of
- Added distinct research identities for Dr. Voss, Dr. Sato, Dr. Kessler and Dr. Hale.
- Added persistent research-clearance ranks from Probationary Subject through Independent Researcher.
- Added the canonical technology progression:
  - Scientist Research
  - Matter Technology
  - Automation & Drones
  - Advanced Power
  - Fusion Research
  - Anomaly Engineering
- Added 18 post-legacy research assignments which actively teach and exercise existing Matter Overdrive systems.
- Matter Technology campaign:
  - The Value of Everything
  - Break It Down
  - Matter Reserve
  - Pattern Recognition
  - Make Something From Nothing
- Automation & Drones campaign:
  - Network Backbone
  - Matter Logistics
  - Instant Freight
- Advanced Power campaign:
  - A Stable Baseline
  - Stored Potential
  - Heavy Distribution
- Fusion Research campaign:
  - Containment First
  - Build the Impossible
  - Feed and Draw
  - Sustained Experiment
- Anomaly Engineering campaign:
  - Across the Horizon
  - Spacetime Control
  - Anomaly Engineering
- Added staged research objectives for multi-part networking, reactor and anomaly assignments.
- Added stage-to-stage research unlocks and persistent campaign state.
- Added scientist-specific briefings, progress dialogue, completion dialogue and stage transitions for the modern research campaign.
- Added substantial quest rewards tied to the system being taught, including matter storage, pattern, networking, power, reactor and spacetime equipment.
- Added Data Pad research-journal integration showing:
  - current progression stage
  - active assignment
  - complete/active/locked research roadmap
  - existing block scan history
- Added a complete research-progression handoff and testing roadmap.

### Changed

- Matter Overdrive now treats `Scientist Research → Matter Technology → Automation & Drones → Advanced Power → Fusion Research → Anomaly Engineering` as the canonical non-Android progression spine.
- Android class progression remains a parallel character-build system rather than a mandatory technology gate.
- Star Map content is retained for registry/save compatibility but is no longer treated as a canonical progression pillar.
- Transporter research is framed as local logistics infrastructure rather than space progression.
- Contract tooltips now expose staged-objective information and named quest-item requirements more clearly.
- Completed contracts can be returned to the quest giver as well as the Contract Market where appropriate.
- The restored legacy campaign now hands directly into the modern Matter Technology campaign rather than ending in an isolated epilogue.
- Cocktail of Ascension remains available as a post-campaign special project instead of being swallowed by the main research flow.

### Fixed

- Existing Android characters that predate Puny Humans quest state can now enter the scientist campaign instead of being trapped at generic dialogue.
- Scientist campaign state is reconciled against legacy contracts already present in the inventory to avoid rewinding older saves.
- Completed and stale earlier story contracts are cleaned up during scientist redemption to reduce duplicate-reward paths.
- Research campaign state similarly reconciles against active modern research contracts.
- Long NPC dialogue no longer silently truncates when it exceeds the fixed dialogue box height.
- Staged contract objective text now includes Stage X/Y and custom required-item names where relevant.

### Testing status

The dialogue and early quest-overhaul foundation previously passed repository CI. The later 0.5 campaign commits are currently affected by GitHub Actions jobs terminating before any runner steps begin; these jobs contain no checkout or Gradle steps and therefore do not provide a compile result. Full clean-world and migrated-world runtime verification is still required for the 18-assignment campaign.

## Unreleased — Android system audit

### Added

- Craftable Matter Overdrive System Guide item with a 28-section bundled manual, simplified and detailed instructions, explicit partial-system labels and troubleshooting routes.
- Sneak-held normal/HC Battery charging for converted Androids at up to 1,024 FE/t.
- Legacy-style 50% movement penalty and HUD warning while the Android core has no FE.
- Collision-safe fallback positions for the simplified Rogue Android Spawner.

### Fixed

- Failed Android actions no longer drain an unaffordable partial FE remainder.
- The Arms damage bonus now applies only to direct melee attacks.
- Sonic Shockwave no longer inherits the Arms melee bonus or extra per-target FE drain.
- Rogue Android spawning no longer consumes 20,000 FE when the world rejects the entity.

### Testing status

Compilation, in-game System Guide rendering/navigation and focused client/dedicated-server Android verification are required before this pass is marked runtime-verified.

## 0.8.0.0-alpha.4.1 — M2 functional systems test build

### Added

- Functional Matter Decomposer, Matter Recycler, Matter Analyzer, Matter Replicator, Pattern Storage, Pattern Monitor, and their network/matter transport links.
- Machine upgrades with persistent debug readouts for the effective values used by each upgraded machine.
- Solar Panel power generation and Tritanium Crates with 54-slot portable inventories across all colour variants.
- Molecular Inscriber production chain, including Mk2–Mk4 Isolinear Circuit recipes and battery-slot support.
- Transporter with bound Transport Flash Drives, entity movement, range/cost/cooldown handling, and debug status.
- Fusion Reactor bundle:
  - Controller, four-coil compact structure, Reactor IO, and Gravitational Anomaly validation.
  - Legacy baseline capacity/output/matter-drain values.
  - Persistent debug UI for structure validity, anomaly distance, efficiency, generation, FE, and matter.
  - Speed, Range, Power Storage, and Matter Storage upgrade support.
- Heavy Energy Cable, using the existing Heavy Matter Pipe block resource:
  - 8,192 FE buffer.
  - Relays up to 1,024 FE/t to each receiving side.
  - Debug UI showing stored energy and latest output.
  - Supports chains: `Reactor IO → Heavy Energy Cable(s) → powered machine`.

### Fixed

- Fusion Reactor screen compilation.
- Fusion Controller shift-click inventory crash.
- Fusion Range upgrades can no longer create a server-freezing anomaly search.
- Fusion Speed upgrades now increase output rather than reducing it.
- Reactor IO output now uses the tested simulation/extract/refund transfer flow.

### Testing status

The M2 machines, inscriber recipes, transporter, Fusion Reactor, and Heavy Energy Cable have been tested in the development client. Run `VERIFY_M2_BUILD.bat` before distributing a build beyond testing.
