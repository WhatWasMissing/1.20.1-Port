# Structure Expansion Playtest Plan

Use this during the large playtest for the second-generation Matter Overdrive facilities on `testing/tech-overhaul`.

## Preflight

1. Run `python scripts/validate_structure_expansion.py`.
2. Run the normal local Gradle build used for the port.
3. Test in a fresh world or unexplored chunks so structure placement is not hidden by already-generated terrain.
4. Keep the game log available and search for `M2 FACILITY TRACE` when diagnosing placement/layout issues.

## Native facility locate targets

Use `/locate structure` for each registered structure id:

- `matteroverdrive:synthetic_manufacturing_plant`
- `matteroverdrive:matter_refinery`
- `matteroverdrive:quantum_relay_station`
- `matteroverdrive:android_command_bunker`
- `matteroverdrive:fusion_research_complex`
- `matteroverdrive:black_site`

## Generation safety gate

For every facility:

- teleport to it from several thousand blocks away so Minecraft must generate the destination region;
- confirm terrain generation completes instead of hanging;
- save and quit near the facility, then reload the world;
- travel across several chunks through the facility and confirm neighbouring chunks finish loading;
- verify the facility does not regenerate a different room graph after reload;
- confirm no piece replaces bedrock.

A world-generation stall is a release blocker even if the structure itself looks correct.

## Layout variation

The structure start chunk selects one of three deterministic layouts. Find multiple examples of each common facility rather than judging only one spawn.

Verify:

- repeated facilities do not all form the same cross;
- corridors actually connect intended rooms;
- entrances lead into reachable internal circulation;
- optional pieces do not create sealed rooms;
- rooms do not overlap in a way that deletes machines or creates inaccessible cavities;
- damaged room sections remain small and intentional rather than exposing half the building.

## Synthetic Manufacturing Plant

Check:

- factory silhouette is broad rather than house-like;
- fabrication, assembly and shipping areas are visually distinct;
- framed glass and white fabrication surfaces read clearly;
- roof machinery is visible from outside;
- elevated service gantry looks intentional and is not floating incorrectly;
- shipping crates survive save/reload;
- Inscriber, Replicator, Android Station and Charging Station are placed correctly.

## Matter Refinery

Check:

- green Matter accents are visible without overwhelming the palette;
- excavation wing sits lower than the main floor without being buried beyond access;
- excavation shaft is reachable/legible from the facility area;
- shaft walls do not carve through bedrock;
- Matter Excavator, storage matrices, Decomposer and Matter Analyzer survive reload;
- service gantry does not intersect terrain in an obviously broken way.

## Quantum Relay Station

Check:

- central tower and two relay masts create a recognizable long-distance silhouette;
- tower top is complete across chunk boundaries;
- masts are not truncated at chunk borders;
- optional observation bridge is enclosed and reachable when present;
- Quantum Power Relay, Grid Capacitor, Facility Controller and Status Panel place successfully;
- glass framing does not become opaque due to renderer/resource issues.

## Android Command Bunker

Check:

- surface entrance and descent actually meet the buried level;
- checkpoint does not seal the route;
- room order changes across layout variants but remains navigable;
- Android Spawner encounter hardware behaves acceptably and does not infinitely flood entities;
- Android Station, Charging/Induction equipment and crates persist;
- buried rooms do not open enormous accidental caves to the surface.

## Fusion Research Complex

Check:

- circular fusion chamber remains circular across all intersecting chunks;
- framed/glass outer ring is complete;
- radial lighting/stabilizer positions are symmetrical;
- observation bridge does not overwrite the core roof in a destructive way;
- service wing elevation still connects to circulation;
- no active anomaly is accidentally spawned merely by generating the structure;
- reactor/control hardware does not immediately create invalid ticking-state crashes.

## Black Site

Check:

- surface presence is subtle compared with the underground footprint;
- vertical entrance reaches the security level;
- lower vault is reachable;
- optional deeper lab (layout variant) remains connected or at least intentionally separated;
- dark/carbon-fibre palette remains readable with the installed lighting;
- Android security encounter does not block progression permanently;
- rare-site generation does not create exposed floating underground sections on steep terrain.

## Compact field sites

Also recheck the three compact Features because the large-site changes share the same development branch:

- `matteroverdrive:abandoned_matter_lab`
- `matteroverdrive:android_relay_outpost`
- `matteroverdrive:anomaly_research_site`

These should remain small and must not reproduce the historical wide-Feature world-generation hang.

## Known reward limitation for this pass

Tritanium crates persist inventory but currently do not consume vanilla chest loot tables. Shipping/armoury/vault crates therefore act as reward-room hooks until structure-specific crate seeding is implemented. Do not file empty generated crates as a persistence regression; test instead that they exist, open, save inventory and survive reload.

## Useful feedback to record

For every bad spawn, capture:

- structure id;
- coordinates;
- world seed;
- layout number from the `M2 FACILITY TRACE` log if available;
- screenshot from outside and inside;
- whether the problem appeared on first generation or only after reload;
- whether the affected piece crosses a chunk border.
