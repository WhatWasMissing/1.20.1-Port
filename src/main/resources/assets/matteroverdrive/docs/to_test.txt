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
- [ ] A colony without Ship Factory cannot build ships; `FACTORY` takes **8,000 ticks**.
- [ ] `HANGAR` takes **4,800 ticks** and each completed Hangar adds **2 fleet berths**.
- [ ] Base fleet capacity is 2; Scout + Colonizer count cannot exceed current colony capacity.
- [ ] `EX` / Matter Extractor takes **14,400 ticks** and completion changes synchronized colony totals by **+10 matter / -6 energy**.
- [ ] `GN` / Power Generator takes **14,400 ticks** and completion changes totals by **+8 energy / -2 matter**.
- [ ] `RS` / Residential takes **6,000 ticks** and completion adds **10,000 population**, **-4 energy**, **-2 matter** and **+4 building capacity**.
- [ ] Current-planet telemetry updates immediately after completion: net **E**, net **M**, population **P**, happiness **H**, and building count/capacity **B**.
- [ ] Residential happiness follows the legacy sign rule: with non-negative energy and matter one Residential contributes +1.00 total happiness; negative power contributes -0.4 instead of +0.5 and negative matter contributes -0.6 instead of +0.5.
- [ ] Multiple Extractors/Generators/Residential buildings stack their exact legacy stat changes and their counts persist across save/reload.
- [ ] A second build cannot replace an active build and travel is blocked while construction is active, including production-building construction.
- [ ] Build action, target planet, finish time, Scout count and Colonizer count survive save/reload/chunk unload.
- [ ] On an unowned current planet, `CLAIM`/colonize requires a completed Colonizer, consumes exactly one, assigns the commander as owner and establishes the Base transactionally.
- [ ] Colonizing an already-owned planet fails without consuming a Colonizer.
- [ ] After colonization, the new colony has a Base but no Ship Factory; build the factory before constructing more ships there.
- [ ] Another player cannot issue valid build/colonize/production requests through a console bound to someone else's fleet.
- [ ] Planet ownership and production-building counts remain after breaking/replacing a Star Map because they are world SavedData; console-local ship counts/build queue remain block-entity state.

Legacy basis: 1.7 Scout = 3600 ticks, Colonizer = 5000, Ship Factory = 8000, Ship Hangar = 4800, Matter Extractor = 14400, Power Generator = 14400, Residential = 6000. Matter Extractor changes MATTER_PRODUCTION +10 / ENERGY_PRODUCTION -6; Generator changes ENERGY_PRODUCTION +8 / MATTER_PRODUCTION -2; Residential changes POPULATION +10000 / ENERGY -4 / MATTER -2 / BUILDINGS_SIZE +4 and calculates happiness from current production signs. Buildings require a Base and ships require a Ship Factory. Colonizer arrival creates a Base and planet ownership. The starting-homeworld Base/Factory bootstrap is the modern bridge needed to enter that loop without the removed galaxy-homeworld generator.

Current limitation: Scout/Colonizer classes are represented as persistent fleet composition rather than restored physical legacy item stacks. Base/Factory/Hangar/Extractor/Generator/Residential state and derived colony stats are real. Multiple independently travelling ship groups and physical planet dimensions/player teleportation remain incomplete. Current per-building count guards are modern safety limits, not legacy constants.

## Star Map fleet combat
- [ ] Fleet header remains Hull / Shield / Firepower / Victories, baseline 100 / 60 / 20.
- [ ] Hostile Fleet pauses arrival; FIRE/RECHARGE uses the 20-tick cooldown and return fire drains shield before hull.
- [ ] Victory resumes preserved travel time and defeat retreats to the last safe Star Map planet at 35 hull / 0 shield.
- [ ] Combat state survives save/reload and non-commanders cannot forge effective fleet-fire requests.
- [ ] Scout/Colonizer composition and colony production totals do not silently alter combat firepower until class/economy combat effects are explicitly supported.

## Machine GUI / persistence
- [ ] Major machine GUIs keep visible physical slots and aligned hitboxes across GUI scales.
- [ ] Charging Station, Space-Time Accelerator, Transporter, Reactor, Android systems and Weapon Station retain their current working state/persistence.
- [ ] Existing machines retain inventories, FE, matter, upgrades, contracts, drives, destinations and relevant Star Map/Android state after save/reload.
