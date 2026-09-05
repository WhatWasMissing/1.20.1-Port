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

## Front-facing parity pass - September 5, 2026

Reference jars inspected directly:

- Matter Overdrive 1.12.2 `0.7.1.0`;
- Matter Overdrive `0.8.0.0-alpha.4.1` legacy build.

New work:

- Android Station now exposes the existing ability-cycling backend through a clickable `CYCLE` control.
- Android Station now opens the existing selectable perk tree directly through a `SKILL TREE` control instead of requiring the player to know the external keybind.
- Android Station now displays Android online state, stored FE, installed bionic-part state, selected ability, locked/ready/active state, level, XP and available perk points on its front page.
- Ability cycling is handled server-side through `AndroidStationMenu.clickMenuButton`, so the GUI does not maintain a separate client-only selection.
- Fusion Reactor now restores the original home-page concept of a dual energy/matter circular readout.
- The dual readout is driven by current reactor FE and matter values rather than being decorative.
- Reactor structure validity, RUN/SCRAM state, redstone permission, efficiency, generated FE/t, demand, ring power, matter drain, IO/stabilizer counts and anomaly/hazard telemetry are reorganized into operator-facing sections.
- Expanded 1.20.1 reactor features remain visible instead of being hidden to achieve visual parity with the older, simpler reactor implementation.
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
- Generic legacy machine page tabs (`home`, `tasks`, `config`, `upgrades`) need to be wired only where equivalent 1.20.1 functionality exists; empty decorative pages will not be added for parity alone.

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
- Verify Pattern Monitor requests still trigger when clicking a populated ghost slot.
- Verify the Reactor Assembly Guide images, page navigation, overlay legend, and Done button still work.
- Inspect all corrected machine models from every side in-world.
- Confirm no purple/black missing-texture faces appear.
- Save/reload the test world and verify the visual changes do not affect machine or Android state.
- Run the normal M2 build/runtime gate and inspect the log for model/resource warnings.
