# UI and Resource Polish

Branch: `feature/easy-parity-systems`

## Current polish pass

This pass is intentionally cosmetic. It does not change machine recipes, inventories, FE/matter logic, networking, reactor behavior, or weapon behavior.

Implemented scope:

- added a shared dark Matter Overdrive screen style;
- standardized slot frames, energy/matter meters, progress bars, section panels, status panels, and readable labels;
- corrected the shared slot frame to line up with the real 16x16 container slot instead of drawing an oversized/offset border;
- retained always-visible debug/status information while removing the repeated raw `[DEBUG]` prefix from screen presentation;
- cleaned up every currently implemented screen in `matteroverdrive.client.screen` that uses a machine/container UI;
- restyled the Fusion Reactor controller and Reactor Assembly Guide to match the rest of the port;
- restyled the Weapon Station without changing its module installation or persistence logic;
- restored existing legacy machine face textures where the 1.20.1 model was stretching one fallback texture over all six faces;
- no newly generated PNG artwork was added in this pass: the block-model fixes reuse legacy textures already present in the repository.

## Screens covered

- Matter Decomposer
- Matter Recycler
- Matter Analyzer
- Matter Replicator
- Solar Panel
- Molecular Inscriber
- Transporter
- Pattern Monitor
- Pattern Storage
- Energy Pipe
- Tritanium Crate
- Weapon Station
- Fusion Reactor Controller
- Reactor Assembly Guide

## Texture/model corrections

- Decomposer: restored its dedicated top texture while retaining the shared machine base on the other faces.
- Matter Analyzer: uses the analyzer front and top textures instead of repeating the front on every face.
- Matter Recycler: keeps recycler side art on side faces and uses the machine base for top/bottom.
- Replicator: restores the dedicated front face rather than using the body texture everywhere.
- Transporter: restores separate front, side, and top textures.
- Solar Panel: keeps the solar texture on the upper face and uses the machine base on the body faces.
- Weapon Station: now uses its dedicated top and bottom textures instead of repeating `weapon_station_side` on every face.

## Known visual limitations

- Several machine blocks do not yet expose a horizontal-facing blockstate, so a restored front face is fixed to model north. Adding safe placement/orientation and save compatibility is a later polish pass.
- Animated active-state faces remain deferred until the relevant blockstate/state synchronization exists.
- Complex legacy OBJ/custom-rendered models are not being re-enabled blindly; they need a dedicated 1.20.1 renderer pass.
- Some legacy texture sheets, such as custom-rendered/OBJ-era assets, are not safe to map directly onto a modern cube model. Those should be handled individually after runtime screenshots identify which blocks still look wrong.

## Runtime test plan

- Open every currently implemented Matter Overdrive machine/container screen and verify the shared style renders correctly.
- Verify every visible slot frame lines up exactly with the real clickable slot, including Weapon Station modules and all 54 Tritanium Crate slots.
- Test normal GUI scale plus at least one larger/smaller GUI scale and check for clipping or overlapping text.
- Confirm the `INF FE` debug control still works on every screen that exposes it.
- Confirm live energy, matter, progress, recipe, network, reactor, and debug/status values still update.
- Verify Pattern Monitor requests still trigger when clicking a populated ghost slot.
- Verify the Reactor Assembly Guide images, page navigation, overlay legend, and Done button still work.
- Inspect Decomposer, Analyzer, Recycler, Replicator, Transporter, Solar Panel, and Weapon Station from every side in-world.
- Confirm no purple/black missing-texture faces appear.
- Confirm the restored top/front/side artwork appears on the expected model faces.
- Check the corresponding inventory item models for new model-bake or missing-texture errors.
- Save/reload the test world and verify the visual changes do not affect machine state or functionality.
- Run the normal M2 build/runtime gate and inspect the log for model/resource warnings.
