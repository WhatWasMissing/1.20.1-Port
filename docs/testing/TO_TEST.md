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
- [ ] Spawner caps at six owned Androids with intended 30% melee / 70% ranged mix and ignores unrelated Androids.
- [ ] Six Transport Flash Drive patrol slots work and persist.
- [ ] COLOR and PATROL/GUARD/HOLD/ESCORT persist through save/reload and level-up.
- [ ] ESCORT formation follows commander without stacking all Androids in one position.
- [ ] Same-Spawner Androids coordinate targets and remain allied to one another and commander/team.
- [ ] Drone linking works; another player cannot silently steal a linked Drone.
- [ ] FOLLOW/DEFENSIVE/PASSIVE/AGGRESSIVE modes persist and owned Drones never attack owner/allies.

## Legacy world structures / population
- [ ] Explore newly generated chunks; old chunks are not retroactively populated.
- [ ] Crashed ships, cargo ships, underwater bases, Mad Scientist houses, Android Houses and Sand Pits generate.
- [ ] Generated Tritanium Crates contain structure-specific persisted salvage.
- [ ] Structure occupants persist, do not duplicate on reload and are not embedded in walls/machines.
- [ ] Structure Androids do not count toward Android Spawner ownership/cap and structure Drones begin unowned.
- [ ] Underwater-base interiors remain dry.
- [ ] Note geometry remains approximate: Android House target 21x21/yOffset -2; Sand Pit target 24x24/yOffset -9.

## Natural gravitational anomalies
- [ ] New Overworld chunks can generate anomalies at conservative ~1/200 candidate rate.
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
- [ ] FIRE/RECHARGE uses 20-tick cooldown; return fire drains shield before hull.
- [ ] Victory resumes preserved travel time; defeat returns navigation to prior safe planet at 35 hull / 0 shield.
- [ ] Non-commanders cannot issue effective travel/economy/dispatch/combat packets through someone else's console.

## Star Map legacy capacity parity
This is the newest high-priority test set.

Recovered 1.7 base capacities are **building / fleet**:
- Normal: **6 / 6**.
- Gas Giant: **2 / 8**.
- Dwarf: **4 / 4**.
- Homeworld override: **8 / 10**.

Modifiers:
- Base: **+2 building capacity**.
- Residential: **+4 building capacity** each.
- Ship Hangar: **+2 fleet capacity** each.

Port mapping: Terrestrial/Oceanic -> legacy Normal; Gas Giant -> Gas Giant; Dwarf -> Dwarf.

- [ ] Fresh homeworld reports effective **B capacity 10** before Residential and **fleet capacity 10** before Hangars.
- [ ] Fresh homeworld receives one stationed Scout, matching the 1.7 homeworld setup.
- [ ] Current compatibility bridge still includes the starting Ship Factory and does not break existing progression.
- [ ] Existing pre-capacity homeworld save at bootstrap planet upgrades to homeworld 8/10 base capacities without deleting buildings/ships.
- [ ] Normal/Oceanic colony with Base reports **8 building capacity / 6 fleet capacity** before Residential/Hangars.
- [ ] Gas Giant colony with Base reports **4 / 8**.
- [ ] Dwarf colony with Base reports **6 / 4**.
- [ ] One Residential increases displayed building capacity by exactly 4.
- [ ] One Hangar increases displayed fleet capacity by exactly 2.
- [ ] Building construction is rejected when `completed buildings + queued building jobs` reaches building capacity.
- [ ] Ship construction is rejected when stationed ships + queued Scout/Colonizer jobs reaches fleet capacity.
- [ ] A queued Residential itself occupies one building space until completion; it cannot be used to escape an already-full legacy building cap.
- [ ] Four construction slots remain the independent queue limit even when building/fleet capacity is larger than four.
- [ ] Capacity values survive save/reload and do not reset after moving the command fleet.

Legacy basis: 1.7 `Planet.canBuild(IBuilding...)` checks `buildings.size < getBuildingSpaces`, requires a Base, then delegates to the building. Planet generators supply Normal 6/6, Gas Giant 2/8 and Dwarf 4/4; `GalaxyServer.buildHomeworld` overrides to 8/10. Base adds +2 BUILDINGS_SIZE, Residential +4 BUILDINGS_SIZE, and Ship Hangar +2 FLEET_SIZE.

## Star Map colony economy / four-slot construction
- [ ] Planet ownership/buildings survive breaking/replacing Star Map because they live in world SavedData.
- [ ] `SCOUT` build = 3,600 ticks.
- [ ] `COLONIZER` build = 5,000 ticks.
- [ ] `FACTORY` build = 8,000 ticks and is required before ship construction.
- [ ] `HANGAR` build = 4,800 ticks and adds exactly 2 fleet spaces.
- [ ] Matter Extractor = 14,400 ticks and changes totals by +10 matter / -6 energy.
- [ ] Power Generator = 14,400 ticks and changes totals by +8 energy / -2 matter.
- [ ] Residential = 6,000 ticks and adds +10,000 population / -4 energy / -2 matter / +4 building capacity.
- [ ] E/M/P/H/B telemetry updates immediately after completion.
- [ ] Planet GUI shows `Q 0/4` through `Q 4/4` for restored construction slots.
- [ ] Up to four valid jobs can run concurrently on same planet.
- [ ] Fifth job rejects while all four slots occupied.
- [ ] Starting construction does not block whole-command-fleet departure; active planet builds continue while console travels.
- [ ] Build queues survive GUI close, chunk unload, save/reload and Star Map replacement.
- [ ] Returning after finish time resolves completed project even if original console was not continuously loaded.
- [ ] Duplicate queued Ship Factory construction rejects.
- [ ] Existing worlds with one active console-local build migrate that job once into target planet queue.

## Planet-local Scout / Colonizer fleets
- [ ] Completed Scout/Colonizer production adds ship to planet where it was built.
- [ ] Planet GUI S/C counts describe currently visited planet's stationed fleet only.
- [ ] Moving command fleet to another owned planet shows that planet's independent counts.
- [ ] Building ships on Colony A does not increase Colony B count.
- [ ] Old console-local ship counters migrate once without duplication.
- [ ] Physical Scout/Colonizer token can appear for local completion and carries owner/type/planet metadata.

## Independent ship transfer
- [ ] Non-current Planet page SEND S / SEND C dispatches ship while command fleet stays where it is.
- [ ] Dispatch removes exactly one stationed ship from source planet.
- [ ] Matching carried physical token is consumed when present, but remote stationed ship is still dispatchable without token.
- [ ] Multiple independent dispatches coexist without replacing each other or command-fleet journey.
- [ ] TRANSIT S# C# survives save/reload.
- [ ] Travel time uses same legacy-derived AU/LY calculation as normal Star Map travel.
- [ ] Source Star Map may be broken/replaced while ship is in transit; another commander-owned loaded Star Map can settle arrival.
- [ ] Scout arriving at owned friendly colony with room stations there.
- [ ] Scout unable to berth returns to origin and grants no invented scouting reward.
- [ ] Colonizer arriving at unowned planet is consumed, assigns ownership and creates Base.
- [ ] Colonizer arriving at friendly colony with room stations there instead of being consumed.
- [ ] Colonizer unable to claim/berth returns to origin.
- [ ] Same-planet, invalid, foreign-source, forged-menu and out-of-range dispatch attempts reject cleanly.

## Matter / network / transporter regression
- [ ] Decomposer -> Matter Pipe -> Replicator/Fusion IO works through multi-pipe chains.
- [ ] Pattern Analyzer/Storage/Monitor/Replicator networking discovers and queues patterns correctly.
- [ ] Router ordinary filter and Network Flash Drive destination filter work.
- [ ] Speed/Hyper-Speed router upgrades persist and respect 10 FE/item execution rule.
- [ ] Switch enabled state persists and affects graph routing.
- [ ] Transporter exact-required-FE succeeds and chained infrastructure does not duplicate transport.

## Reactor / anomaly regression
- [ ] Ring validation detects missing/wrong blocks correctly.
- [ ] IO shares controller FE/matter and exports through Heavy Energy Cable chains.
- [ ] RUN/SCRAM and redstone modes persist.
- [ ] Shared ring power and stabilizer-from-reactor behavior remain functional where configured.
- [ ] Event-horizon living-entity deaths add mass once.
- [ ] Equalizer protects wearer from anomaly pull/event-horizon damage.
- [ ] Overlay persists and reports real block positions.

## Weapons regression
- [ ] Phaser, Phaser Rifle, Ion Sniper and Plasma Shotgun cannot fire without valid FE.
- [ ] One weapon does not drain unrelated weapons.
- [ ] Batteries/HC Batteries remain rechargeable after draining.
- [ ] Heat, overheat and reload work in Survival and Creative.
- [ ] Weapon Station slots/modules persist and dismantling returns contents.

## GUI / persistence / release sanity
- [ ] Major GUIs keep visible physical slots and aligned hitboxes at multiple GUI scales.
- [ ] System Guide index opens correct sections and remembers last page.
- [ ] Current Feature Reference matches this build.
- [ ] M2 Testing Checklist matches this file.
- [ ] Save/reload preserves machine inventories, FE, matter, upgrades, contracts, patrol drives, Android state, colony state, planet construction/capacities and ship travel state.
- [ ] No missing-texture purple/black models appear for new ship items or restored world content.
