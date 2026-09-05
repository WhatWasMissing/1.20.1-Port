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
- [ ] Six Transport Flash Drive patrol slots work and persist.
- [ ] COLOR and PATROL/GUARD/HOLD/ESCORT persist through save/reload and level-up.
- [ ] ESCORT formation follows the commander without stacking all Androids in one position.
- [ ] Same-Spawner Androids coordinate targets and remain allied to one another and the commander/team.
- [ ] Drone linking works; another player cannot silently steal a linked Drone.
- [ ] FOLLOW/DEFENSIVE/PASSIVE/AGGRESSIVE modes persist and owned Drones never attack owner/allies.

## Legacy world structures / population
- [ ] Explore newly generated chunks; old chunks are not retroactively populated.
- [ ] Crashed ships, cargo ships, underwater bases, Mad Scientist houses, Android Houses and Sand Pits generate.
- [ ] Generated Tritanium Crates contain structure-specific persisted salvage.
- [ ] Crashed/cargo/Android/Sand-Pit sites receive persistent unowned Android/Drone defenders.
- [ ] Mad Scientist houses get a real Mad Scientist and possible Failed animal; underwater bases get hostile occupants.
- [ ] Structure Androids do not count toward Android Spawner ownership/cap and structure Drones begin unowned.
- [ ] Occupants do not duplicate on chunk reload and are not embedded in structure walls/machines.
- [ ] Underwater-base interiors remain dry.
- [ ] Note geometry is still approximate versus authoritative old templates: Android House should ultimately be 21x21/yOffset -2 and Sand Pit 24x24/yOffset -9.

## Natural gravitational anomalies
- [ ] New Overworld chunks can generate natural anomalies at the conservative ~1/200 candidate rate.
- [ ] Starting mass is within 2,048-10,240.
- [ ] Pull, event horizon, mass growth and block/fluid effects activate normally.
- [ ] Block/fluid-obstructed candidates reject cleanly and generated anomalies persist after save/reload.
- [ ] Reactor/Stabilizer/Equalizer behavior is unaffected by natural-generation support.

## Star Map navigation / command fleet
- [ ] Galaxy -> Quadrant -> Star -> Planet retains wheel zoom, drag pan and RMB back.
- [ ] Planet properties remain deterministic.
- [ ] Whole-fleet `TRAVEL` remains server-authoritative and survives close/reload/chunk unload.
- [ ] Same-star AU and interstellar LY timing remain correct.
- [ ] Asteroid, Slingshot, Android Intercept, Signal Echo and Hostile Fleet encounters still resolve.
- [ ] Hostile Fleet pauses arrival until combat resolves.
- [ ] Fleet baseline remains Hull 100 / Shield 60 / Firepower 20.
- [ ] FIRE/RECHARGE uses the 20-tick cooldown; return fire drains shield before hull.
- [ ] Victory resumes preserved travel time; defeat returns navigation to the previous safe planet at 35 hull / 0 shield.
- [ ] Non-commanders cannot issue effective travel/economy/dispatch/combat packets through someone else's console.

## Star Map colony economy
- [ ] First commander binding creates a persistent homeworld with Base + Ship Factory.
- [ ] Planet ownership/buildings survive breaking/replacing the Star Map because they live in world SavedData.
- [ ] `SCOUT` build = 3,600 ticks.
- [ ] `COLONIZER` build = 5,000 ticks.
- [ ] `FACTORY` build = 8,000 ticks and is required before ship construction.
- [ ] `HANGAR` build = 4,800 ticks and adds exactly 2 fleet berths.
- [ ] Base fleet capacity is 2 before Hangars.
- [ ] Matter Extractor = 14,400 ticks and changes totals by +10 matter / -6 energy.
- [ ] Power Generator = 14,400 ticks and changes totals by +8 energy / -2 matter.
- [ ] Residential = 6,000 ticks and adds +10,000 population / -4 energy / -2 matter / +4 building capacity.
- [ ] E/M/P/H/B telemetry updates immediately after construction.
- [ ] Only one colony construction job runs per console and whole-fleet departure is blocked while it builds.
- [ ] Construction target/action/finish time survives save/reload.

## Planet-local Scout / Colonizer fleets
This is the newest high-priority test set.

- [ ] Completed Scout/Colonizer production adds the ship to the **planet where it was built**, not to a global console counter.
- [ ] Planet GUI `S` and `C` counts describe the currently visited planet's stationed fleet only.
- [ ] Moving the command console fleet to a different owned planet shows that planet's own independent ship counts.
- [ ] Each planet enforces its own Base + Hangar fleet capacity.
- [ ] Building ships on Colony A does not increase Colony B's ship count.
- [ ] Existing worlds from the previous build migrate old console-local Scout/Colonizer counts once into the bound console's current planet; reloading again does not duplicate the migration.
- [ ] A physical Scout/Colonizer item token still appears after construction and carries owner/type/planet metadata.

## Independent ship transfer
- [ ] On a non-current Planet page, `SEND S` / `SEND C` can dispatch a ship while the command fleet stays where it is.
- [ ] Dispatch removes exactly one stationed ship from the current source planet.
- [ ] A carried matching physical token is consumed when present, but a remote stationed ship is still dispatchable without requiring an item token in the player's inventory.
- [ ] Multiple independent dispatches coexist without replacing each other or the main command-fleet journey.
- [ ] `TRANSIT S# C#` tracks the commander's active independent travel events and survives save/reload.
- [ ] Travel time uses the same legacy-derived AU/LY calculation as normal Star Map travel.
- [ ] The exact source Star Map block may be broken/replaced while a ship is in transit; arrival can be settled by another loaded Star Map owned by the same commander.
- [ ] Scout arriving at an owned friendly colony with room becomes stationed there.
- [ ] Scout arriving where it cannot berth returns to its origin colony instead of disappearing.
- [ ] Scout travel grants no invented reward; the 1.7 Scout travel hook is empty.
- [ ] Colonizer arriving at an unowned planet is consumed, assigns ownership and creates a Base.
- [ ] Colonizer arriving at an existing friendly colony with room stations there instead of being consumed.
- [ ] Colonizer unable to claim or berth returns to its origin colony.
- [ ] Same-planet, invalid, foreign-source, forged-menu and out-of-range dispatch attempts reject cleanly.
- [ ] Breaking/replacing a Star Map does not delete stationed planet fleets or completed colonies.

Legacy basis: 1.7 `TravelEvent` stores one ship, source, destination, start and duration; dispatch removes the ship from the source fleet. Scout `onTravel` is empty. Colonizer arrival establishes a Base and planet ownership when allowed. This port now stores stationed ship counts on persistent planets while retaining the modern command-fleet travel/combat layer separately.

## Matter / network / transporter regression
- [ ] Decomposer -> Matter Pipe -> Replicator/Fusion IO remains functional through multi-pipe chains.
- [ ] Pattern Analyzer/Storage/Monitor/Replicator networking still discovers and queues patterns correctly.
- [ ] Router ordinary filter and Network Flash Drive destination filter work.
- [ ] Speed/Hyper-Speed router upgrades persist and do not exceed the 10 FE/item execution rule.
- [ ] Switch enabled state persists and actually affects graph routing.
- [ ] Transporter exact-required-FE succeeds and chained infrastructure does not create duplicate transport.

## Reactor / anomaly regression
- [ ] Ring structure validation still detects missing/wrong blocks correctly.
- [ ] IO shares controller FE/matter and exports through Heavy Energy Cable chains.
- [ ] RUN/SCRAM and redstone modes persist.
- [ ] Shared ring power and stabilizer-from-reactor behavior remain functional where configured.
- [ ] Event-horizon living-entity deaths add mass once.
- [ ] Equalizer still protects the wearer from anomaly pull/event-horizon damage.
- [ ] Overlay persists and reports real block positions.

## Weapons regression
- [ ] Phaser, Phaser Rifle, Ion Sniper and Plasma Shotgun cannot fire without valid FE.
- [ ] One weapon does not drain unrelated weapons as an energy source.
- [ ] Batteries/HC Batteries remain rechargeable after draining.
- [ ] Heat, overheat and reload behavior work in Survival and Creative.
- [ ] Weapon Station slots/modules persist and dismantling returns contents.

## GUI / persistence / release sanity
- [ ] Major machine GUIs keep visible physical slots and aligned hitboxes at multiple GUI scales.
- [ ] In-game System Guide index opens the correct sections and remembers the last page when closed/reopened.
- [ ] Current Feature Reference matches this build's implemented systems.
- [ ] M2 Testing Checklist matches this file.
- [ ] Save/reload preserves machine inventories, FE, matter, upgrades, contracts, patrol drives, Android state, colony state and ship travel state.
- [ ] No missing-texture purple/black models appear for newly added ship items or restored world content.
