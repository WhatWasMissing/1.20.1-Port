# Matter Overdrive 1.20.1 - Alpha Runtime Testing Checklist

Branch: `testing/main`
Legacy references:
- Original Matter Overdrive 1.7.10 source branch `simeonradivoev/MatterOverdrive@1.7.10` (`0.4.2`).
- Matter Overdrive 1.12.2 `0.7.1.0` jar and recovered source/resources.
Build identity: `Alpha Version 3`, made by MVQ1303

This file is bundled in-game as the **M2 Testing Checklist**. GitHub Actions compilation is not a substitute for runtime verification.

## Already runtime-passed

- [x] Rogue Android combat and sounds.
- [x] Failed Cow, Pig, Sheep and Chicken spawning/behaviour.
- [x] Mad Scientist interaction and current Puny Humans quest.
- [x] Holo Sign thin monitor geometry and renamed-item programming.
- [x] Previous restored machine-model batch: Android Station, Weapon Station, Star Map, Contract Market, Analyzer, Decomposer, Recycler, Microwave, Pattern Monitor, Pattern Storage, Replicator, Charging Station, Space-Time Accelerator and Solar Panel, aside from the visual regressions patched in the current build.

## Priority 1 - latest visual fixes

- [ ] Program a Holo Sign and view the front. Text must be readable normally, on the screen-facing side, not mirrored/backwards.
- [ ] Place Holo Signs north/east/south/west. Text and panel must rotate together.
- [ ] Place a normal block directly against Pattern Storage on every side. No neighbouring face should disappear.
- [ ] Repeat the neighbour test for Pattern Monitor and Space-Time Accelerator.
- [ ] Place a Pylon. It must render with the Matter Overdrive texture rather than purple/black missing-model squares.
- [ ] Confirm Pylon channel changing and network routing still work after the model correction.

## Priority 2 - 1.7-derived restorations

- [ ] Place multiple Industrial Glass blocks edge-to-edge. Shared interior faces should be suppressed while outside faces remain visible.
- [ ] Break one glass block and verify the newly exposed neighbour face appears immediately.
- [ ] Open Star Map. Mouse wheel should zoom between roughly 0.45x and 2.5x.
- [ ] Click-drag inside the Star Map field to pan it.
- [ ] Contract counts must remain visible and functional while zooming/panning.
- [ ] Closing and reopening Star Map must not affect contracts or inventory state.

## Priority 3 - connected pipes

Test Matter Pipe, Heavy Energy Cable and Network Pipe.

- [ ] A pipe by itself is a small central section, not a full cube.
- [ ] Adjacent same-type pipes grow arms toward each other.
- [ ] A pipe beside its supported machine grows an arm toward the machine.
- [ ] North/south/east/west/up/down arms render correctly and update when neighbours change.
- [ ] Collision/selection follows the centre and active arms.
- [ ] Matter Pipe still transfers matter across chains.
- [ ] Heavy Energy Cable still transfers FE across long chains and multiple outputs.
- [ ] Network Pipe still participates in Pattern Storage/Monitor/Router/Switch networks.

## Priority 4 - original block artwork and item indicators

- [ ] Reactor Coil, Controller, IO and Gravitational Stabilizer use the restored source face assignments and correct orientation.
- [ ] Bright/dark vents, Holo Matrix, striped Tritanium Plate and Decorative Clean use the intended source artwork.
- [ ] Pattern Drive icons change empty -> partial -> full.
- [ ] Matter Scanner changes offline -> online after linking.
- [ ] Matter Container and battery/weapon/Portable Decomposer indicators continue to update.

## Priority 5 - GUI and save regression

- [ ] Open the major machine GUIs and confirm original small-slot artwork aligns with clickable slots.
- [ ] No FE/matter bars, text, buttons or debug panels overlap slots.
- [ ] Existing machines retain inventories, FE, matter, upgrades, contracts and drives after save/reload.
- [ ] Security Claim/Access/Remove still works and persists.

## Core gameplay regression

- [ ] Matter production/storage/replication still works end-to-end.
- [ ] Reactor ring validation, Reactor IO, anomaly mass, stabilizers, shared ring power, RUN/SCRAM, remote and overlay still work.
- [ ] Weapons still require valid FE, heat/reload correctly and do not drain unrelated guns.
- [ ] Android HUD, V cycle, B activate, K tree and perk persistence/refunds still work.

## 1.7 parity targets not claimed yet

- Generic per-machine redstone modes (none/high/low) beyond current reactor-specific controls.
- Wrench sneak-dismantle. The 1.7 source had it, but the modern implementation must first respect Security Protocol ownership so it cannot become a protection bypass.
- Full Star Map galaxy/star/planet data model, selection, travel and events. This build restores navigation presentation only.
- Deeper Network Flash Drive configuration.
- Legacy Android attack/ability branches not already represented by the modern skill tree.
- Exact Pylon animated overlay/old CTM presentation.
- Full weapon module meshes, recoil, scope zoom and remaining first-person animations.
- Old optional cross-mod integrations unless specifically reintroduced for modern equivalents.

## Pass criteria

Pass this build when Holo Sign text is readable on the correct face, Pylon no longer shows a missing model, Pattern Storage/Monitor/Accelerator do not cull neighbours, Industrial Glass connects cleanly, Star Map zoom/pan works without breaking contracts, and the existing core systems remain functional.
