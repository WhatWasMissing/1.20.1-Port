# Matter Overdrive 1.20.1 Feature Backlog

This backlog is derived from the current feature inventory and is intentionally scoped toward integrated gameplay loops. “Implemented” below means source/build evidence exists; runtime playtesting remains a separate state.

## Critical bug

| Item | Value | Dependencies | Validation |
|---|---|---|---|
| Resolve any runtime-reported weapon visibility or transform failures | High | Native Destiny renderer, source assets | Fresh client log plus first/third-person screenshots for every weapon family |
| Validate structure generation and traversal in a fresh world | High | Native `Structure`/`StructurePiece` generation | Structure test plan, screenshots, no generation exceptions |

## Incomplete existing feature

| Item | Value | Dependencies | Validation |
|---|---|---|---|
| Complete remaining weapon first-person animation choreography and module placement | High | Weapon renderer, model/resource mappings | Renderer validator plus Blockbench/source review and runtime screenshots |
| Finish Dimensional Pylon visual effects | Medium | Pylon backend and screen | Model bounds, client render test, multiplayer state check |
| Improve drone flying, equipment, and role presentation | High | Drone entity, commander screen, Android progression | Entity/render audit and role-specific runtime checklist |
| Expand machine-specific GUI detail where real backend state exists | Medium | Existing menus/block entities | Shared layout/hitbox audit and screenshot review |

## Integration improvement

| Item | Value | Dependencies | Validation |
|---|---|---|---|
| Add research rewards for scanner, structure discovery, entity encounters, and artifact analysis | High | Data Pad, dossiers, quest/research flows | Per-player persistence test and reward-duplication test |
| Connect structure-specific loot to machine, Android, drone, and reactor progression | High | Six native families and five compact sites | Loot-table/resource validator and structure-chain checklist |
| Add actionable network, reactor, matter, drone, and weapon diagnostics to player-facing screens | High | Existing telemetry and screens | UI geometry validator plus controlled failure tests |
| Maintain the structure dependency graph and generated SVG/Markdown reports in the full sanity pass | Medium | Graphviz renderer, structure audit JSON | Full sanity and Graphviz parse/layout validation |

## New gameplay system

| Item | Value | Dependencies | Validation |
|---|---|---|---|
| Lightweight faction ownership for security, rogue Android, research-remnant, and scavenger encounters | High | Structure profiles, entity spawning, loot identity | Spawn-location audit, faction behaviour checklist, multiplayer ownership test |
| Structure chains from relay outpost to research facility to restricted laboratory | High | Compact discovery ledger, dossiers, structure loot | Deterministic chain state test and non-RNG dead-end check |
| Artifact behaviours that alter Android abilities, drone roles, weapon heat, or anomaly interaction | High | Research, Android data, weapons, drones, anomaly | Save/load, tooltip, balance, and interaction tests |
| Drone role modules for logistics, scanning, repair, and reconnaissance | High | Drone entity, commander controls, network/matter transport | Role behaviour tests, ownership tests, bounded tick profiling |

## Structure improvement

| Item | Value | Dependencies | Validation |
|---|---|---|---|
| Add explicit room/connector metadata to every native facility profile | High | Native structure pieces and layout lab | Required-room graph, entrance/exit, rotation, bounding-box, and reachability checks |
| Add structure-specific environmental storytelling variants | Medium | Damage variants, facility profiles, industrial detail blocks | Top-down/elevation previews and fresh-world screenshots |
| Tune biome, terrain, depth, and frequency contracts for each structure family | High | Structure-set JSON and biome modifiers | Deterministic placement sampling and worldgen performance check |

## UI/UX improvement

| Item | Value | Dependencies | Validation |
|---|---|---|---|
| Centralize shared screen geometry for complex Android, reactor, network, and research flows | Medium | Existing screens and server-backed state | Render/hitbox parity audit at multiple resolutions |
| Add clear progression state, requirements, and trade-offs to research and upgrade screens | High | Research archives, upgrades, Data Pad | UI bounds/tooltip tests and screenshot review |

## Performance / technical debt

| Item | Value | Dependencies | Validation |
|---|---|---|---|
| Profile bounded discovery scans, network traversal, drone AI, and structure placement | High | Existing caches and watchdog | Tick-time sampling, bounded-search assertions, multiplayer test |
| Replace remaining deprecated resource/API calls in legacy client registrations | Low | Legacy renderers and registration paths | Compile with deprecation warnings and runtime startup check |

## Progression dependency model

```text
Exploration -> Discovery/Scanning -> Research/Dossiers
     -> Machines + Android builds + Drone roles + Weapon choices
     -> Automation/Network mastery -> Advanced facilities
     -> Reactor/Anomaly risk management -> End-game encounters and artifacts
```

The next recommended implementation pass is the integrated research-and-structure-chain slice: it gives existing exploration, Data Pad, loot, quests, and progression systems a shared purpose without making any one structure or reward mandatory.
