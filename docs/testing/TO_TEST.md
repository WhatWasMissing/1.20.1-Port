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
- [ ] Network Router/Switch routing and Transporter exact-FE operation remain correct.

## Android Spawner / Drone
- [ ] Spawner caps at six owned Androids with the intended 30% melee / 70% ranged mix and ignores unrelated Androids.
- [ ] Patrol drives, COLOR, PATROL/GUARD/HOLD/ESCORT, commander assignment, formation and coordinated targets persist.
- [ ] Same-Spawner Androids remain allies and protect commander/team allies.
- [ ] Drone linking and FOLLOW/DEFENSIVE/PASSIVE/AGGRESSIVE persist; owned Drones do not attack owner/allies and unowned Drones remain hostile.

## Legacy world structures / population
- [ ] Explore newly generated chunks; old chunks are not retroactively populated.
- [ ] Crashed ships, cargo ships, underwater bases, Mad Scientist houses, Android Houses and Sand Pits generate with their expected shapes/rarity/terrain filters.
- [ ] Generated Tritanium Crates contain structure-specific persisted salvage.
- [ ] Crashed/cargo/Android/Sand-Pit sites receive persistent unowned Android/Drone defenders; Scientist houses get a real Mad Scientist and possible Failed animal; underwater bases get hostile occupants.
- [ ] Structure Androids do not count toward any Android Spawner six-unit cap and structure Drones remain unowned.
- [ ] Occupants do not duplicate on chunk reload and placement does not embed them in walls/machines.
- [ ] Underwater-base interiors remain dry and structure generation does not cause severe stalls/cascades.

## Natural gravitational anomalies
- [ ] New Overworld chunks can generate natural anomalies at the conservative ~1/200 candidate rate.
- [ ] Generated anomaly starting mass is 2,048-10,240 and immediately uses real pull, event-horizon consumption, mass growth and block effects.
- [ ] Block/fluid-obstructed candidates reject cleanly; anomalies persist after save/reload.
- [ ] Reactor/Stabilizer/Equalizer behavior remains unaffected.

## Star Map navigation / journey
- [ ] Galaxy -> Quadrant -> Star -> Planet retains wheel zoom, drag pan and RMB back.
- [ ] Planet properties remain deterministic and TRAVEL remains server-authoritative.
- [ ] Same-star AU and interstellar LY timing, ETA, persistence and encounter scheduling still work.
- [ ] Asteroid, Slingshot, Android Intercept, Signal Echo and Hostile Fleet encounters still resolve correctly.

## Star Map colony / shipyard economy
- [ ] The first commander action binds the console fleet and establishes the starting planet as its persistent homeworld with **Base + Ship Factory** bootstrap state.
- [ ] Planet ownership/buildings are stored in server-global Star Map SavedData, not only in the Star Map block entity; save/reload preserves them.
- [ ] Current-planet GUI exposes real economy controls only while the fleet is stationary.
- [ ] `SCOUT` starts a single server-authoritative **3,600 tick** build and adds one Scout only when complete.
- [ ] `COLONIZER` starts a **5,000 tick** build and adds one Colonizer only when complete.
- [ ] Completed Scout/Colonizer construction spawns a real non-stacking physical ship token carrying owner/type/console/planet metadata.
- [ ] A colony without Ship Factory cannot build ships; `FACTORY` takes **8,000 ticks**.
- [ ] `HANGAR` takes **4,800 ticks** and each completed Hangar adds **2 fleet berths**.
- [ ] Base fleet capacity is 2; available Scout + Colonizer count cannot exceed current colony capacity.
- [ ] `EX` / Matter Extractor takes **14,400 ticks** and completion changes synchronized colony totals by **+10 matter / -6 energy**.
- [ ] `GN` / Power Generator takes **14,400 ticks** and completion changes totals by **+8 energy / -2 matter**.
- [ ] `RS` / Residential takes **6,000 ticks** and completion adds **10,000 population**, **-4 energy**, **-2 matter** and **+4 building capacity**.
- [ ] Current-planet telemetry updates immediately after completion: net **E**, net **M**, population **P**, happiness **H**, and building count/capacity **B**.
- [ ] Multiple Extractors/Generators/Residential buildings stack their legacy stat changes and persist across save/reload.
- [ ] A second build cannot replace an active build and whole-fleet travel is blocked while construction is active.
- [ ] Build action, target planet, finish time, Scout count and Colonizer count survive save/reload/chunk unload.
- [ ] On an unowned current planet, `CLAIM`/colonize requires both an available Colonizer count and its physical token, consumes exactly one, assigns ownership and establishes the Base transactionally.
- [ ] Colonizing an already-owned planet fails without consuming a Colonizer.

## Star Map independent ship travel
- [ ] On a non-current Planet page, `SEND S` and `SEND C` appear while the console fleet is stationary.
- [ ] Dispatch requires the matching physical ship token in the commander's inventory; missing token or zero available ship count rejects cleanly.
- [ ] Successful dispatch consumes exactly one physical token and removes exactly one available Scout/Colonizer count.
- [ ] Multiple independent ship dispatches can coexist and do not start or replace the console fleet's normal TRAVEL journey.
- [ ] Transit telemetry `TRANSIT S# C#` updates after dispatch and survives save/reload.
- [ ] Independent travel time uses the same legacy-derived AU/LY timing calculation as normal Star Map travel.
- [ ] A Scout arrival produces no invented scouting reward, restores one available Scout and rematerializes its physical token.
- [ ] A Colonizer arrival on an unowned planet consumes the ship, assigns the commander as owner and establishes a Base without moving the console fleet there.
- [ ] A Colonizer arriving at a planet that cannot be claimed is not silently lost: its available count and physical token return.
- [ ] Independent dispatch rejects same-planet destinations, invalid positions, non-commanders, forged menu positions and out-of-range packet attempts.
- [ ] Ship events preserve owner, console, ship type, origin, destination, start and duration in world SavedData.

Legacy basis: 1.7 `TravelEvent` persists one ship, from/to positions, start and travel length; `GalaxyServer.createTravelEvent` removes the dispatched ship from the source fleet before adding the event. Scout `onTravel` is empty. Colonizer `onTravel` consumes itself only when it can establish a Base and set planet ownership. The modern console remains the owner anchor while independent ship events restore that per-ship behavior.

Current limitation: independent ship travel is associated with the originating Star Map console rather than a fully restored planet-local fleet inventory. Fighter/Battlecruiser/Mothership enum values exist in the old API, but they are not being treated as implemented ships without corresponding source-backed item behavior.

## Star Map fleet combat
- [ ] Fleet header remains Hull / Shield / Firepower / Victories, baseline 100 / 60 / 20.
- [ ] Hostile Fleet pauses arrival; FIRE/RECHARGE uses the 20-tick cooldown and return fire drains shield before hull.
- [ ] Victory resumes preserved travel time and defeat retreats to the last safe Star Map planet at 35 hull / 0 shield.
- [ ] Combat state survives save/reload and non-commanders cannot forge effective fleet-fire requests.
- [ ] Scout/Colonizer composition, independent ship events and colony production totals do not silently alter combat firepower until explicitly supported by legacy behavior.

## Machine GUI / persistence
- [ ] Major machine GUIs keep visible physical slots and aligned hitboxes across GUI scales.
- [ ] Charging Station, Space-Time Accelerator, Transporter, Reactor, Android systems and Weapon Station retain their current working state/persistence.
- [ ] Existing machines retain inventories, FE, matter, upgrades, contracts, drives, destinations and relevant Star Map/Android state after save/reload.
