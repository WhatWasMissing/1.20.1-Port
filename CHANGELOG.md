# Changelog

All notable changes to the Matter Overdrive 1.20.1 port are documented here.

## 0.7 — Complete feature release

### Added

- Added repeatable **Field Operations** that turn existing technology-site exploration into an ongoing mid/end-game loop instead of one-time scenery rewards.
- Added three operation doctrines: **Recovery**, **Systems**, and **Anomaly**.
- Added stage-gated operations for abandoned matter labs, Android relay outposts, logistics depots, matter observatories, anomaly research sites, and controlled event-horizon exposure.
- Field-operation state is server-authoritative, player-persistent, and event-driven; no global mission tick manager was added.
- Recovery operations reward recovered Android artifact protocols; Systems operations reward machine upgrades; Anomaly operations reward containment-oriented materials/upgrades.
- Added `/matteroverdrive research field status` and `/matteroverdrive research field assign <recovery|systems|anomaly>`.
- Expanded the Data Pad journal with active operation, doctrine, progress, description, and lifetime completion count.
- Added a dedicated Field Operations runtime/abuse/performance checklist to `TECH_OVERHAUL_TEST_PLAN.md`.
- Added the complete merged tech-overhaul feature set: Energy Bank dual FE/Matter storage, Quantum Flux Reactor, Environmental Regulator, Wall Terminal, decorative reactor blocks and hybrid FE+Matter conduit routing.
- Added persistent Energy Bank reactor-IO input, independent FE/Matter output, status/comparator telemetry and network-compatible buffering without resource conversion or feedback loops.
- Added four Frontier Expedition structures: Deep Matter Vault, Autonomous Drone Foundry, Anomaly Quarantine Site and Orbital Recovery Array.
- Added natural hostile-spawn definitions for Rogue Androids, Ranged Rogue Androids, Drones, Mutant Scientists, Assimilators and Phase Stalkers, plus village Field Scientist and Systems Engineer population alongside Mad Scientists.
- Added vanilla structure loot injection that preserves existing vanilla pools and supplies Matter Overdrive materials, with seven deterministic Legendary Relic sources.
- Added themed Legendary Relic pools to the six main technology-facility caches, using the existing recovered artifact item and Android passive-protocol selection.
- Added 35 additional native Destiny GunPack weapon conversions, bringing the registered native Destiny set to 49 energy weapons.
- Imported per-weapon Destiny geometry, textures, source audio, available animations and display transforms; added native animation-name aliases and static-idle fallbacks for models without source clips.
- Connected every Destiny weapon to the existing Energy Weapon, Weapon System, battery/barrel/sights/colour/utility module and Weapon Station contracts.
- Added source-transform first-person rendering, bounded aim/recoil presentation and third-person generic-use pose suppression for energy weapons.
- Added GuideME block-reference descriptions, item image links and live recipe links for all active player-facing blocks, including Drone Fabricator and Matter Network Terminal.
- Added cross-checked static/resource validation for registries, JSON/model/texture references, loot, spawn, PDA, relic, weapon, Android, network and structure contracts.

### Changed

- Compact technology-site recognition now feeds both one-time discovery rewards and repeatable Field Operations while preserving the one-time dossier/XP guard.
- Event-horizon detection now also services the Anomaly doctrine when `Horizon Exposure` is active, while continuing to advance normal anomaly contracts in parallel.

### Design direction

- New systems should connect the existing exploration → research → machines/drones → automation → reactor/anomaly progression spine rather than exist as isolated crafting blocks.
- Structure/world gameplay remains on native chunk-safe structure generation; the retired Star Map is not restored.

### Testing status

The release is source/resource validated. Runtime verification is still required for persistence, multiplayer isolation, Energy Bank throughput, compact-site recognition, facility generation, reward delivery, model transforms, audio playback, spawn rates and anomaly-operation completion. See `docs/testing/TO_TEST.md`.

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

A clean local Java 17 / Forge 1.20.1 build of the current 0.6 `main` head is still required before distribution. GitHub Actions attempts on the release line have previously failed before runner steps began, so those failures are not compile results. The 0.6 runtime checklist covers matter economy, reactor/anomaly, Android, weapons, networking/transporter, structures, GUI and persistence regressions.

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
