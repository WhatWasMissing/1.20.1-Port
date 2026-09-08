# Matter Overdrive 1.20.1 - 0.6 Runtime Testing Checklist

Branch: `main`
Release line: `0.6`
Legacy references:
- Matter Overdrive 1.7.10 `0.4.2` jar.
- Matter Overdrive 1.12.2 `0.7.1.0` universal jar.

The 0.8 alpha jar is intentionally excluded from parity decisions.
Build identity: `Matter Overdrive 0.6`, maintained by MVQ1303

This file is bundled in-game as the **M2 Testing Checklist**. GitHub Actions compilation is not a substitute for runtime verification. A clean local 0.6 build and the checks below are required before the release is considered runtime-verified.

## Highest-priority regression
- [ ] Matter production/storage/analysis/replication works end-to-end, including recursive/fallback matter values introduced for 0.6.
- [ ] Reactor ring, IO, anomaly mass, stabilizers, shared ring power, remote and overlay remain functional.
- [ ] Weapons require valid FE, heat/reload correctly and do not drain unrelated weapons.
- [ ] Android HUD, V cycle, B activate, K tree and perk persistence/refunds work.
- [ ] Security Claim/Access/Remove still gates normal interaction and wrench dismantling.
- [ ] Network Router/Switch routing and Transporter exact-FE operation remain correct.

## 0.6 matter economy
- [ ] Common explicitly-valued items retain their intended matter values.
- [ ] Recipe-derived values resolve for simple and multi-stage recipes.
- [ ] Multi-output recipes divide input matter across their output count without producing zero-value results.
- [ ] Ingredient alternatives choose the cheapest valid positive matter path.
- [ ] Cyclic/reversible recipes terminate safely rather than recursing forever or inflating value.
- [ ] Items without an explicit or resolvable recipe value receive a deterministic fallback value.
- [ ] Matter Analyzer, Decomposer, Pattern Drive and Replicator agree on the effective matter value.
- [ ] Matter tooltips show the effective value; Shift exposes the value source where supported.
- [ ] `/matteroverdrive matter value`, `/matteroverdrive matter audit` and `/matteroverdrive matter clearcache` execute without errors.
- [ ] Recipe/datapack changes do not leave stale cached matter values after cache invalidation/reload.

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
Recovered 1.7 base capacities are **building / fleet**: Normal **6/6**, Gas Giant **2/8**, Dwarf **4/4**, Homeworld override **8/10**. Base adds +2 building, Residential +4 building and Ship Hangar +2 fleet. Terrestrial/Oceanic map to legacy Normal.

- [ ] Fresh homeworld reports effective B capacity 10 and fleet capacity 10 before Residential/Hangars.
- [ ] Fresh homeworld receives one stationed Scout.
- [ ] Current compatibility bridge still includes starting Ship Factory.
- [ ] Existing pre-capacity homeworld upgrades to homeworld capacities without deleting state.
- [ ] Normal/Oceanic colony with Base reports 8 building / 6 fleet.
- [ ] Gas Giant colony with Base reports 4 / 8.
- [ ] Dwarf colony with Base reports 6 / 4.
- [ ] Residential increases building capacity by 4; Hangar increases fleet capacity by 2.
- [ ] Completed + queued building jobs cannot exceed building capacity.
- [ ] Stationed + queued ship jobs cannot exceed fleet capacity.
- [ ] Queued Residential cannot escape an already-full building cap.
- [ ] Four construction slots remain the queue limit even when capacity is larger.
- [ ] Capacity values survive save/reload and command-fleet movement.

## Star Map colony economy / four-slot construction
- [ ] Planet ownership/buildings survive breaking/replacing Star Map because they live in world SavedData.
- [ ] SCOUT 3,600t; COLONIZER 5,000t; FACTORY 8,000t; HANGAR 4,800t.
- [ ] Matter Extractor 14,400t = +10 matter/-6 energy.
- [ ] Power Generator 14,400t = +8 energy/-2 matter.
- [ ] Residential 6,000t = +10,000 population/-4 energy/-2 matter/+4 building capacity.
- [ ] E/M/P/H/B telemetry updates immediately after completion.
- [ ] Q 0/4 through Q 4/4 reports restored construction slots.
- [ ] Up to four valid jobs run concurrently; fifth rejects.
- [ ] Construction continues while command fleet travels.
- [ ] Build queues survive GUI close, chunk unload, save/reload and Star Map replacement.
- [ ] Elapsed jobs settle later from absolute finish times.
- [ ] Duplicate queued Ship Factory rejects.
- [ ] Previous console-local active build migrates once.

## Planet-local Scout / Colonizer fleets
- [ ] Completed ships belong to build planet.
- [ ] Planet GUI S/C counts are local to current planet.
- [ ] Colony A/B counts remain independent.
- [ ] Old console-local counters migrate once.
- [ ] Physical local-completion tokens carry owner/type/planet metadata.

## Independent ship transfer
- [ ] SEND S / SEND C dispatch while command fleet remains stationary.
- [ ] Dispatch removes exactly one stationed source ship.
- [ ] Remote stationed ships remain commandable without physical token.
- [ ] Multiple dispatches coexist with command-fleet journey.
- [ ] TRANSIT S# C# persists.
- [ ] AU/LY timing matches normal Star Map travel calculation.
- [ ] Another owned loaded Star Map can settle arrivals if source block is gone.
- [ ] Scout friendly arrival stations; failed berth returns to origin without invented reward.
- [ ] Colonizer unowned arrival creates ownership + Base; friendly arrival stations; failed arrival returns.
- [ ] Invalid/foreign/forged/out-of-range dispatch rejects.

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

## Weapons parity / regression
- [ ] Phaser, Phaser Rifle, Ion Sniper and Plasma Shotgun cannot fire without valid FE.
- [ ] One energy weapon never drains another energy weapon as a reload source.
- [ ] Only explicit Energy Packs and Weapon Battery/HC Battery items are consumed for reload; installed weapon battery/capacity remains coherent.
- [ ] Batteries/HC Batteries remain rechargeable after draining.
- [ ] Heat, overheat and reload work in Survival and Creative.
- [ ] Ion Sniper aiming visibly applies the recovered legacy 0.40 base FOV multiplier.
- [ ] Installing Sniper Scope changes aiming to the recovered 0.85 scope override on supported weapons.
- [ ] Scope range/accuracy effects agree with the Weapon Station STATS page.
- [ ] Firing produces a brief decaying camera recoil instead of constant held-weapon camera wobble.
- [ ] Ion Sniper recoil is stronger unzoomed than zoomed, matching the recovered legacy 4/3 reference behavior.
- [ ] Recoil does not continue after the shot impulse decays or while merely holding the weapon.
- [ ] Weapon Station HOME reports current weapon type, energy, heat/overheat, module count and sight state.
- [ ] Weapon Station MODULES reports the six role slots correctly and all seven real station slots stay usable on every page.
- [ ] Weapon Station STATS updates immediately when modules change.
- [ ] Weapon Station slots/modules persist and dismantling returns contents.
- [ ] No weapon becomes backwards/invisible in first or third person after the client-effects change.

## Machine GUI parity / persistence
- [ ] Decomposer uses HOME/TASKS/UPGRADES side pages while input/output/battery and all four real upgrade slots remain visible and clickable.
- [ ] Decomposer TASKS reports real progress, input matter yield and FE demand; no value is decorative/faked.
- [ ] Matter Recycler uses HOME/TASKS/UPGRADES while input/output/battery and all four upgrade slots remain visible and clickable.
- [ ] Recycler TASKS progress/yield/FE demand matches the actual active recycle cycle.
- [ ] Microwave uses HOME/TASKS/UPGRADES while its food input, energy item, output and four upgrade slots stay visible on every page.
- [ ] Microwave TASKS progress/duration/FE demand follows the real food-only cooking cycle.
- [ ] Solar Panel uses HOME/GEN/UPGRADES while both physical upgrade slots remain visible and clickable.
- [ ] Solar GEN page matches live generation, sky access, light, daylight factor, buffer/output and per-side cap telemetry.
- [ ] Pattern Storage uses HOME/DRIVES/UPGRADES while its energy slot, six Pattern Drive slots and four upgrade slots remain visible/clickable on every page.
- [ ] Pattern Storage HOME/DRIVES telemetry agrees with real pattern count, FE buffer and idle FE/t.
- [ ] Pattern Monitor uses PATTERNS/QUEUE without moving or disabling any of its 12 ghost request slots.
- [ ] Clicking a visible Pattern Monitor pattern still requests exactly one replication task after switching between pages.
- [ ] Pattern Monitor QUEUE reports the actual synchronized queue size and does not fabricate per-task state that the backend does not expose.
- [ ] Gravitational Stabilizer uses HOME/BEAM/UPGRADES while all four upgrade slots remain visible and RS MODE works from every page.
- [ ] Stabilizer BEAM page correctly distinguishes anomaly lock, clear beam, blocked beam and blocked distance from synchronized server state.
- [ ] Stabilizer HOME redstone mode/operation state changes immediately after RS MODE and survives reopen/reload.
- [ ] Heavy Energy Pipe status reports real buffer percentage and last output; no fake routing/configuration controls appear.
- [ ] Replicator HOME/TASKS/CONFIG/UPGRADES pages retain visible physical slots and accurate queue/pattern telemetry.
- [ ] Transporter HOME/TASKS/LOCATIONS/UPGRADES retains real saved-destination controls and visible physical slots.
- [ ] Charging Station HOME/ANDROID/UPGRADES retains item/upgrade slots and real wireless charging telemetry.
- [ ] Space-Time Accelerator HOME/TASKS/UPGRADES retains its four upgrade slots and live pulse/radius/resource state.
- [ ] Inscriber HOME/TASKS/UPGRADES retains its real ingredient/output/energy/upgrade slots and live tier/cycle telemetry.
- [ ] Weapon Station HOME/MODULES/STATS retains all physical slots as above.
- [ ] Fusion Reactor direct controls remain real RUN/SCRAM/redstone/debug controls and its upgrade slots/dual resource telemetry remain visible.
- [ ] Major GUIs keep aligned slot hitboxes at GUI scales 2, 3 and Auto where practical.
- [ ] Switching pages never moves/hides a real inventory slot or causes ghost-clicks.

## GUI / persistence / release sanity
- [ ] GuideME index opens every current system page and all internal links resolve.
- [ ] GuideME Current Features identifies the build as 0.6 and matches the repository feature reference.
- [ ] Current Feature Reference matches this build.
- [ ] M2 Testing Checklist matches this file.
- [ ] Save/reload preserves machine inventories, FE, matter, upgrades, contracts, patrol drives, Android state, colony state, planet construction/capacities and ship travel state.
- [ ] No missing-texture purple/black models appear for weapon optics, ship items or restored world content.
