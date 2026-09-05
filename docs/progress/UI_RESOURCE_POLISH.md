# UI and Resource Polish

Branch: `testing/main`

## Current polish pass

The GUI parity work now uses the original 1.7/1.12 Matter Overdrive resources and presentation as the visual reference while retaining the 1.20.1 port's current menus, synchronization and expanded systems.

Implemented scope:

- added a shared dark Matter Overdrive screen style backed by original scalable GUI resources;
- standardized slot frames, energy/matter meters, progress bars, section panels, status panels, and readable labels;
- corrected the shared slot frame to line up with the real 16x16 container slot instead of drawing an oversized/offset border;
- retained always-visible debug/status information while removing the repeated raw `[DEBUG]` prefix from screen presentation;
- cleaned up every currently implemented screen in `matteroverdrive.client.screen` that uses a machine/container UI;
- restyled the Fusion Reactor controller and Reactor Assembly Guide to match the rest of the port;
- restyled the Weapon Station without changing its module installation or persistence logic;
- restored existing legacy machine face textures where the 1.20.1 model was stretching one fallback texture over all six faces;
- no newly generated PNG artwork was added: restored visuals reuse legacy resources already present in the repository.

## Reference policy

GUI and gameplay parity work now uses only the Matter Overdrive 1.7 and 1.12 branches/builds as authoritative legacy references. The older alpha jar is not used as a parity target.

## Front-facing parity pass - September 5, 2026

Reference material inspected directly:

- Matter Overdrive 1.12.2 `0.7.1.0` jar;
- Matter Overdrive 1.7 legacy source/resources.

New work:

- Android Station exposes the existing ability-cycling backend through a clickable `CYCLE` control.
- Android Station opens the existing selectable perk tree directly through a `SKILL TREE` control instead of requiring the player to know the external keybind.
- Android Station displays Android online state, stored FE, installed bionic-part state, selected ability, locked/ready/active state, level, XP and available perk points on its front page.
- Ability cycling is handled server-side through `AndroidStationMenu.clickMenuButton`, so the GUI does not maintain a separate client-only selection.
- Fusion Reactor restores the original home-page concept of a dual energy/matter circular readout.
- The dual readout is driven by current reactor FE and matter values rather than being decorative.
- Reactor structure validity, RUN/SCRAM state, redstone permission, efficiency, generated FE/t, demand, ring power, matter drain, IO/stabilizer counts and anomaly/hazard telemetry are reorganized into operator-facing sections.
- Expanded 1.20.1 reactor features remain visible instead of being hidden to achieve visual parity with the older, simpler reactor implementation.
- Matter Analyzer now exposes legacy-inspired `HOME`, `TASKS`, `CONFIG`, and `UPGRADES` pages in a dedicated side panel while keeping all real machine slots visible and clickable at all times.
- Analyzer `TASKS` is backed by the current synchronized scan progress, matter input, pattern-learning progress and FE/t demand.
- Analyzer `CONFIG` exposes the already-implemented server-side redstone mode through a front-facing `CYCLE RS` control.
- Analyzer `UPGRADES` presents the existing four physical upgrade slots without introducing a second inventory or client-only state.
- Replicator now exposes legacy-inspired `HOME`, `TASKS`, `CONFIG`, and `UPGRADES` pages in a dedicated side panel while keeping pattern-drive, battery/output and upgrade slots permanently visible.
- Replicator `TASKS` is backed by current local replication progress, pattern progress, network task queue, cycle time and failure telemetry.
- Replicator `CONFIG` documents the currently implemented automatic network task intake and physical pattern/energy inputs instead of presenting non-functional legacy switches.
- Replicator `UPGRADES` presents the existing four upgrade slots and current backend-driven upgrade effects.
- Added reusable legacy-style dual-ring and status-lamp primitives for later machine-specific parity work.

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
- Android Station

## Texture/model corrections

- Decomposer: restored its dedicated top texture while retaining the shared machine base on the other faces.
- Matter Analyzer: uses the analyzer front and top textures instead of repeating the front on every face.
- Matter Recycler: keeps recycler side art on side faces and uses the machine base for top/bottom.
- Replicator: restores the dedicated front face rather than using the body texture everywhere.
- Transporter: restores separate front, side, and top textures.
- Solar Panel: keeps the solar texture on the upper face and uses the machine base on the body faces.
- Weapon Station: uses its dedicated top and bottom textures instead of repeating `weapon_station_side` on every face.
- Tritanium Lamp: restores the dedicated side, top, and bottom artwork instead of repeating the old all-face lamp texture.
- Pattern Monitor: uses the legacy holographic monitor face, matching rear face, and machine-base body faces instead of projecting the monitor texture onto the entire cube.
- Fusion Reactor Controller: uses one screen face with machine-base body faces and visually rotates that screen with the controller's horizontal-facing blockstate.

## Deliberately deferred

- Star Map still needs a deeper pass against the original galaxy/star/planet page model after the current data model is expanded further.
- Complex legacy OBJ/custom-rendered models are not being re-enabled blindly; they need a dedicated 1.20.1 renderer pass.
- Some legacy texture sheets, such as custom-rendered/OBJ-era assets, are not safe to map directly onto a modern cube model. Those should be handled individually after runtime screenshots identify which blocks still look wrong.
- Generic legacy machine pages will only be added where equivalent 1.20.1 functionality exists; empty decorative controls will not be added for parity alone.

## Runtime test plan

- Open every currently implemented Matter Overdrive machine/container screen and verify the shared style renders correctly.
- Verify every visible slot frame lines up exactly with the real clickable slot, including Weapon Station modules and all 54 Tritanium Crate slots.
- Test normal GUI scale plus at least one larger/smaller GUI scale and check for clipping or overlapping text.
- Confirm the `INF FE` debug control still works on every screen that exposes it.
- Confirm live energy, matter, progress, recipe, network, reactor, and debug/status values still update.
- Android Station: click `CYCLE` repeatedly and confirm only unlocked abilities are selected and the selected label synchronizes immediately.
- Android Station: click `SKILL TREE`, select/refund a perk, reopen the station and verify level/perk-point state remains correct.
- Android Station: verify installed-part indicators and ability LOCKED/READY/ACTIVE state follow current Android state.
- Fusion Reactor: verify both halves of the circular energy/matter gauge update independently.
- Fusion Reactor: verify RUN/SCRAM, redstone mode, structure fault state, efficiency, generated FE/t, demand, ring power and anomaly telemetry update without reopening the GUI.
- Matter Analyzer: switch through `HOME`, `TASKS`, `CONFIG`, and `UPGRADES`; machine slots must remain visible and usable on every page.
- Matter Analyzer: verify `TASKS` follows live scan/pattern values and `CYCLE RS` changes the actual server-side redstone mode.
- Matter Replicator: switch through all four pages; pattern, battery/output and upgrade slots must remain visible and usable on every page.
- Matter Replicator: verify `TASKS` follows local progress, network queue, cycle timing and failure chance without reopening the screen.
- Verify Pattern Monitor requests still trigger when clicking a populated ghost slot.
- Verify the Reactor Assembly Guide images, page navigation, overlay legend, and Done button still work.
- Inspect all corrected machine models from every side in-world.
- Confirm no purple/black missing-texture faces appear.
- Save/reload the test world and verify the visual changes do not affect machine or Android state.
- Run the normal M2 build/runtime gate and inspect the log for model/resource warnings.
