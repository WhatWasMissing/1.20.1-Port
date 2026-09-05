# Matter Overdrive 1.20.1 - Alpha Runtime Testing Checklist

Branch: `testing/main`
Legacy references:
- Matter Overdrive 1.7.10 `0.4.2` jar.
- Matter Overdrive 1.12.2 `0.7.1.0` universal jar.

The 0.8 alpha jar is intentionally excluded from parity decisions.
Build identity: `Alpha Version 3`, made by MVQ1303

This file is bundled in-game as the **M2 Testing Checklist**. GitHub Actions compilation is not a substitute for runtime verification.

## Highest-priority regression
- [ ] Matter production/storage/analysis/replication works end-to-end.
- [ ] Reactor ring, IO, anomaly mass, stabilizers, shared ring power, remote and overlay remain functional.
- [ ] Weapons require valid FE, heat/reload correctly and do not drain unrelated weapons.
- [ ] Android HUD, V cycle, B activate, K tree and perk persistence/refunds work.
- [ ] Security Claim/Access/Remove still gates normal interaction and wrench dismantling.
- [ ] Network Router filters, Network Flash Drive destination filtering, four Speed/Hyper-Speed slots, multi-transfer item budget and 10 FE/item limit still work.
- [ ] Network Switch state changes routing and appearance and persists.
- [ ] Transporter exact-FE operation, max-three living entities, dead-entity rejection, persistent destinations and dismantle returns remain correct.

## Android Spawner / Drone
- [ ] Spawner reaches six owned Androids with the intended 30% melee / 70% ranged mix and does not count unrelated Androids.
- [ ] Ownership survives save/reload and chunk unload; `KILL OWNED` removes only that Spawner's Androids.
- [ ] Six Transport Flash Drive slots provide same-dimension patrol waypoints; combat overrides patrol.
- [ ] COLOR cycles eight persisted squad colors and updates existing plus future spawned units.
- [ ] MODE cycles PATROL -> GUARD -> HOLD. PATROL follows waypoints, GUARD returns idle units toward the Spawner, HOLD suppresses idle movement.
- [ ] Same-Spawner Androids are allies and do not friendly-fire one another.
- [ ] Owned Drone follows its owner, inherits owner team/allies, and does not automatically acquire unrelated players.
- [ ] Unowned Drone remains a hostile ranged mob; Drone owner UUID persists.

## Legacy world structures - new pass
- [ ] Explore **newly generated chunks** in a fresh/new area; existing explored chunks are not expected to gain structures retroactively.
- [ ] Crashed spacecraft generate rarely on dry Overworld terrain. They have a damaged tritanium hull silhouette, striped hull sections, glass bridge detail, a crate and Holo Sign rather than appearing as solid cubes.
- [ ] Cargo ships generate much more rarely than crashed ships and are visibly larger, with a long cargo hull, upper rails/windows, lamps and occasional crates.
- [ ] Underwater bases generate only in ocean biomes at the ocean floor, remain submerged externally, and have a deliberately cleared/dry interior enclosed by tritanium and an Industrial Glass band/dome.
- [ ] Underwater bases include Matter Analyzer / Tritanium Crate set dressing without flooding the interior after initial generation.
- [ ] Mad Scientist houses generate on dry Overworld terrain as enclosed white laboratory structures with windows, beams and real Inscriber/Decomposer/Crate blocks inside.
- [ ] Structure generation does not cascade uncontrollably, repeatedly generate on chunk reload, or cause severe world-generation stalls.
- [ ] The observed rarity hierarchy is sensible: crashed ships are the most common restored ship structure, Mad Scientist houses remain uncommon, underwater bases are rare, cargo ships are exceptionally rare.
- [ ] Structure blocks and machine block entities survive save/reload after generation.

Current limitation to verify rather than misread as a bug: these are modern 1.20.1 structural translations of the authoritative legacy generators. Pixel-exact reconstruction of every old image template, dedicated structure loot population, Android houses/sand pits and richer structure-specific encounters remain later work.

## Star Map navigation / journey
- [ ] Galaxy -> Quadrant -> Star -> Planet navigation retains wheel zoom, drag pan and right-click back.
- [ ] Planet page reports deterministic type/orbit/habitability/temperature/gravity/moons/atmosphere.
- [ ] `TRAVEL` starts a server-authoritative journey; current planet shows `CURRENT LOCATION`; a second destination cannot replace an active journey.
- [ ] Same-star travel uses the AU path and interstellar travel uses the LY path; state and ETA persist through closing/reopening and save/reload.
- [ ] Longer journeys schedule exactly one route-derived event. Asteroid Field adds 100 ticks, Slingshot reduces ETA, Signal Echo adds 40 ticks, and Rogue Android Intercept adds 120 ticks plus two melee and one ranged Rogue Android.
- [ ] Pending encounter countdown is visible in the Planet travel control and nearby event announcements are limited to 32 blocks.

## Star Map fleet combat - new pass
- [ ] The first player to successfully launch from a Star Map becomes that machine fleet's persistent commander; another player cannot commandeer that fleet by starting travel.
- [ ] Star Map header synchronizes fleet **Hull / Shield / Firepower / Victories**. Baseline is 100 hull, 60 shield and 20 firepower.
- [ ] Find a route that rolls the new **Hostile Fleet** encounter. Arrival is paused while combat is active rather than silently completing behind the fight.
- [ ] During hostile-fleet combat the Planet control becomes `FIRE`; after firing it becomes `RECHARGE` for the 20-tick attack cooldown.
- [ ] Each player volley removes the synchronized fleet firepower value from enemy hull; enemy return fire consumes shield before hull.
- [ ] Enemy threat varies deterministically by destination, producing enemy hull/firepower tiers rather than every battle being identical.
- [ ] Winning increments the victory counter, clears combat and resumes the journey while preserving the route time that was paused by combat.
- [ ] Losing cancels the journey back to the last safe Star Map planet and leaves the fleet damaged at 35 hull / 0 shield rather than teleporting the physical player.
- [ ] Beginning a later journey restores shields; successful arrival repairs some hull and restores shields.
- [ ] Save/reload during hostile-fleet combat preserves commander, player hull/shield/firepower/victories, enemy hull and active battle state.
- [ ] Non-commanders cannot send a forged/GUI fleet-fire request that changes battle state.

Current limitation to verify: fleet combat is now real Star Map gameplay, but the old Scout/Colonizer ship item production queue, multi-ship fleet composition, colony/planet ownership, physical planet dimensions and player teleportation are not yet implemented and must not be claimed by the GUI.

## Machine GUI / persistence
- [ ] Decomposer, Replicator, Analyzer, Inscriber, Charging Station, Weapon Station, Fusion Reactor, Android Station/Spawner and Star Map retain visible physical slots and aligned hitboxes across GUI scales.
- [ ] Charging Station wireless Android charging and Range/Power/Power Storage upgrades remain functional.
- [ ] Space-Time Accelerator retains the 1.12 `[-radius, radius)` footprint and live task/upgrade telemetry.
- [ ] Existing machines retain inventories, FE, matter, upgrades, contracts, drives, saved Transporter destinations, Star Map journey/encounter/fleet state, Android squad state and ownership after save/reload.
