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
- [ ] Ownership survives save/reload and chunk unload; `KILL` removes only that Spawner's Androids.
- [ ] Six Transport Flash Drive slots provide same-dimension patrol waypoints; combat overrides patrol.
- [ ] COLOR cycles eight persisted squad colors and updates existing plus future spawned units.
- [ ] MODE cycles PATROL -> GUARD -> HOLD -> ESCORT. PATROL follows waypoints, GUARD returns idle units toward the Spawner, HOLD suppresses idle movement and ESCORT follows the assigned commander.
- [ ] Entering ESCORT automatically binds the player pressing MODE as commander; COMMAND can explicitly reassign commander without changing the current mode.
- [ ] ESCORT Androids spread into deterministic positions around the commander instead of stacking directly inside the player, and resume formation after combat.
- [ ] If one Spawner-owned Android acquires a valid target, nearby Androids from the same Spawner with no target coordinate onto it; unrelated Androids are not recruited.
- [ ] Same-Spawner Androids remain allies and do not friendly-fire one another or the assigned commander/team allies.
- [ ] Android squad color, mode, commander UUID, owned UUID set and patrol data survive save/reload.
- [ ] Right-clicking an unowned Drone links it to that player and starts in FOLLOW mode; another player cannot silently steal an already-linked Drone.
- [ ] Owner right-click cycles Drone FOLLOW -> DEFENSIVE -> PASSIVE -> AGGRESSIVE; owner shift-right-click releases the link.
- [ ] FOLLOW stays near the owner without acquiring combat targets.
- [ ] DEFENSIVE follows the owner and attacks a valid mob that recently hurt the owner.
- [ ] PASSIVE continues following but clears combat targets and cannot attack.
- [ ] AGGRESSIVE follows the owner, defends the owner and proactively attacks valid nearby hostile mobs while respecting owner/team/same-owner Drone allies.
- [ ] Owned Drones never attack their owner or unrelated players; unowned Drones remain hostile ranged mobs.
- [ ] Drone owner UUID and command mode persist after save/reload.

## Legacy world structures - population pass
- [ ] Explore **newly generated chunks** in a fresh/new area; existing explored chunks are not expected to gain structures retroactively.
- [ ] Crashed spacecraft generate rarely on dry Overworld terrain with damaged tritanium hull, stripe/glass details, Holo Sign, salvage crate and 1-2 persistent mixed Rogue Android defenders.
- [ ] Cargo ships generate much more rarely and visibly larger, with long cargo hull, upper rails/windows, lamps, multiple possible salvage crates, 2-4 Android defenders and an occasional hostile Drone.
- [ ] Underwater bases generate only in ocean biomes at the ocean floor, remain submerged externally, keep a dry interior, and contain a persistent hostile Drone plus an occasional ranged Rogue Android.
- [ ] Mad Scientist houses generate as enclosed white laboratories with functioning machines, salvage, one persistent Mad Scientist and an occasional Failed animal experiment.
- [ ] Android Houses generate as rare approximately 19 x 19 shelters with functioning machine set dressing, salvage, 3-4 persistent mixed Rogue Android defenders and an occasional hostile Drone.
- [ ] Sand Pits only commit on sand/red-sand/sandstone-family terrain, form a broad stepped excavation and contain a persistent Android guardian plus an occasional Drone.
- [ ] Structure-generated Rogue Androids are **not** registered to an Android Spawner and do not consume any Spawner's six-unit ownership cap.
- [ ] Structure-generated Drones are unowned and retain normal hostile ranged behavior until explicitly linked by a player.
- [ ] Structure occupants survive save/reload and normal distance-based despawn conditions because they are persistence-required.
- [ ] Reloading a generated chunk does not duplicate the original structure occupants; population happens only during one-time feature placement.
- [ ] Mob placement respects collision/world-border guards and does not embed occupants inside structure walls or machinery.
- [ ] Sand Pit terrain rejection does not alter ordinary grass/forest terrain or leave floating water columns.
- [ ] Structure generation does not cascade, regenerate on chunk reload, or cause severe world-generation stalls.
- [ ] The rarity hierarchy remains sensible: terrain-filtered Sand Pit candidates; crashed ships commonest ship tier; Mad Scientist houses uncommon; Android Houses rarer; underwater bases rare; cargo ships exceptionally rare.

### Structure salvage population
- [ ] Generated Tritanium Crates are populated through the existing 54-slot persistent crate inventory and survive save/reload.
- [ ] Crashed-ship salvage contains tritanium plate + matter dust, with possible battery / Mk1 circuit.
- [ ] Cargo-ship salvage contains larger tritanium/dilithium stores, with possible machine casing / upgrade base.
- [ ] Underwater-base salvage contains refined matter, dilithium and Mk2 circuitry, with possible integration matrix / pattern drive.
- [ ] Mad Scientist lab salvage contains circuitry, machine casing and refined matter, with possible integration matrix / artifact.
- [ ] Android House salvage contains battery/circuitry plus an Android body part, with possible blue Android pill / Network Flash Drive.
- [ ] Sand Pit salvage contains tritanium nuggets/ingots and matter dust, with possible dilithium / rare artifact.
- [ ] Loot stacks occupy actual random crate slots and persist through breaking/replacing the crate using its existing inventory serialization behavior.
- [ ] Structure blocks, machines, populated crates and persistent occupants survive save/reload after generation.

Current limitation: these are modern 1.20.1 structural translations of the authoritative legacy generators. Structure-specific population and persisted salvage are now implemented, but pixel-exact reconstruction of every old image template and deeper structure-specific objectives/scripts remain later work.

## Natural gravitational anomalies
- [ ] In **new Overworld chunks**, natural Gravitational Anomalies can generate in open air roughly from sea level +4 through sea level +63.
- [ ] Natural anomaly frequency is deliberately conservative at approximately 1/200 candidate chunks, matching the 1.7 default rather than the much more aggressive 1.12 1/20 default.
- [ ] A generated anomaly initializes through the normal block-entity path with the shared legacy 2,048-10,240 starting mass range.
- [ ] Generated anomalies immediately use the existing real pull, event-horizon consumption, mass growth and block-effect systems; they are not decorative markers.
- [ ] Candidate positions containing blocks or fluids reject cleanly rather than deleting terrain to force an anomaly into place.
- [ ] Natural anomalies persist after save/reload and do not regenerate repeatedly in the same already-generated chunk.
- [ ] Existing Stabilizer / Equalizer / reactor-anomaly behavior is unaffected by the addition of natural worldgen anomalies.

## Star Map navigation / journey
- [ ] Galaxy -> Quadrant -> Star -> Planet navigation retains wheel zoom, drag pan and right-click back.
- [ ] Planet page reports deterministic type/orbit/habitability/temperature/gravity/moons/atmosphere.
- [ ] `TRAVEL` starts a server-authoritative journey; current planet shows `CURRENT LOCATION`; a second destination cannot replace an active journey.
- [ ] Same-star travel uses the AU path and interstellar travel uses the LY path; state and ETA persist through closing/reopening and save/reload.
- [ ] Longer journeys schedule exactly one route-derived event. Asteroid Field adds 100 ticks, Slingshot reduces ETA, Signal Echo adds 40 ticks, and Rogue Android Intercept adds 120 ticks plus two melee and one ranged Rogue Android.
- [ ] Pending encounter countdown is visible in the Planet travel control and nearby event announcements are limited to 32 blocks.

## Star Map fleet combat
- [ ] The first player to successfully launch from a Star Map becomes that machine fleet's persistent commander; another player cannot commandeer that fleet by starting travel.
- [ ] Star Map header synchronizes fleet **Hull / Shield / Firepower / Victories**. Baseline is 100 hull, 60 shield and 20 firepower.
- [ ] Find a route that rolls **Hostile Fleet**. Arrival is paused while combat is active rather than silently completing behind the fight.
- [ ] During hostile-fleet combat the Planet control becomes `FIRE`; after firing it becomes `RECHARGE` for the 20-tick attack cooldown.
- [ ] Each player volley removes synchronized fleet firepower from enemy hull; enemy return fire consumes shield before hull.
- [ ] Enemy threat varies deterministically by destination.
- [ ] Winning increments victories, clears combat and resumes journey while preserving paused route time.
- [ ] Losing cancels journey back to the last safe Star Map planet and leaves fleet at 35 hull / 0 shield without physically teleporting the player.
- [ ] Beginning a later journey restores shields; successful arrival repairs some hull and restores shields.
- [ ] Save/reload during hostile-fleet combat preserves commander, fleet stats, enemy hull and active battle state.
- [ ] Non-commanders cannot send a forged/GUI fleet-fire request that changes battle state.

Current limitation: fleet combat is real Star Map gameplay, but Scout/Colonizer production, multi-ship fleet composition, colony/planet ownership, physical planet dimensions and player teleportation are not yet implemented.

## Machine GUI / persistence
- [ ] Decomposer, Replicator, Analyzer, Inscriber, Charging Station, Weapon Station, Fusion Reactor, Android Station/Spawner and Star Map retain visible physical slots and aligned hitboxes across GUI scales.
- [ ] Charging Station wireless Android charging and Range/Power/Power Storage upgrades remain functional.
- [ ] Space-Time Accelerator retains the 1.12 `[-radius, radius)` footprint and live task/upgrade telemetry.
- [ ] Existing machines retain inventories, FE, matter, upgrades, contracts, drives, saved Transporter destinations, Star Map journey/encounter/fleet state, Android squad state and ownership after save/reload.
