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

## Rewards, dossiers and security pass

Run `python scripts/validate_structure_expansion.py`, then build locally. The development pass passed static resource checks and Java 17 syntax parsing; compilation and runtime behaviour remain to be verified here.

### Cache lifecycle

- In fresh chunks, find shipping (plant), processing (refinery), control (relay), armoury (bunker), reactor control (fusion), and vault/lab (Black Site) caches. Empty main caches are now a bug.
- Before first access, inspect `/data get block X Y Z`: `StructureLoot`, `StructureLootSeed`, and `StructureLootAssigned` should exist. Save/reload before opening and confirm the metadata persists.
- Open the cache: themed items and a matching dossier should appear. Reopening, emptying and reloading must not refill it. The pending table key should disappear after resolution.
- Extract from an unopened cache through an item pipe/hopper and confirm loot resolves once. Pick up an unopened crate, place it again, and check that the saved inventory is present exactly once.
- Player-crafted crates and existing occupied crates retain ordinary inventory behaviour. No retroactive loot is expected in previously generated sites.
- If customizing loot tables, keep their result within the 54-slot crate capacity; excess customized loot is not retained.

### Research recovery

- Use all six family dossiers. Each gives its documented XP/equipment package once and displays its finding. Read again and use a second copy: no second payout.
- Save/reload and die/respawn, then reread: discovery flags under `ForgeData.PlayerPersisted.MatterOverdriveFacilityResearch` must persist.
- Share a dossier with a second player: their first discovery should pay independently. Test a full inventory: reward drops at the player.
- Scientist contracts and research stage must not jump ahead. Dossiers stay readable and reusable.

### Optional investigation chain

- In a fresh test world, discover an Android Relay Outpost, then a Matter Observatory, then an Anomaly Research Site with the same player.
- Confirm the chat progression reports `INVESTIGATION 1/3`, `2/3`, and `COMPLETE` in that order, and that each stage survives save/reload.
- Open the Data Pad after each discovery. It must show the current investigation step and name the next lead; after the third discovery it must show that the anomaly evidence is archived.
- Discover the three sites out of order: ordinary discovery XP, Data Pad and dossier rewards must still work, but the chain stage and completion reward must not advance.
- Complete the ordered chain once and verify exactly one `upgrade_parallel_processing` is awarded. Repeat discovery at the same coordinates or with duplicate dossiers; no second chain reward is allowed.
- Test two players independently: each player has their own chain progress and may earn the completion reward once.

### Finite encounters

- Use Survival, outside Peaceful; Creative is deliberately not an activation trigger.
- Within 16 blocks, allow at least 10 seconds for each deployment. Expect 2 per ordinary station, 3 per bunker station, 4 per Black Site station; damaged stations have one fewer.
- Clear every defender, wait, reload and feed FE: an exhausted generated station must never refill its reserve. Check `FacilityRemaining` with `/data get block` or reopen its menu for the title.
- Block candidate spawn spaces: unsuccessful attempts must preserve the reserve. Reopen the spaces and verify deployment resumes.
- Peaceful must not spend charges. Switch back to Normal and check remaining deployment. Existing surviving hostiles may despawn when difficulty changes, as usual.
- Approach a station at a loaded-chunk boundary and verify no generation stall or forced distant load. Defenders should guard the station, with more ranged units at relay/Black Site facilities.
- A newly player-placed Android Spawner should still use the existing FE and squad controls.

### Abandoned visuals

- Compare intact and damaged eligible rooms. Look for exposed corner framing, rubble, failed support machinery, cobwebs and reduced security reserves.
- Check surface-facility salvage yards in layouts 1/2: bounded broken service frame, clear recovery aisle, themed debris and low-value loot without a research dossier.
- Ensure doors/caches are reachable, rubble stays within its piece and generation does not replace bedrock. Record floating or buried salvage-yard platforms for terrain adaptation follow-up.

## Useful feedback to record

For every bad spawn, capture:

- structure id;
- coordinates;
- world seed;
- layout number from the `M2 FACILITY TRACE` log if available;
- screenshot from outside and inside;
- whether the problem appeared on first generation or only after reload;
- whether the affected piece crosses a chunk border.

