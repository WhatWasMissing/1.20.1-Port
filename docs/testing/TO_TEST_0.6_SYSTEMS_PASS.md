# Matter Overdrive 0.6 - Focused Systems Pass Test Plan

Branch: `main`
Release line: `0.6`

This checklist covers the September 8 systems pass only. **Star Map and all world/natural generation are intentionally out of scope for this pass and should not be changed or used as pass/fail criteria here.** The project-wide `TO_TEST.md` remains the complete release checklist.

## Build / startup gate
- [ ] `BUILD_LOCAL.bat` completes successfully.
- [ ] Build helper prints the exact produced JAR path, size and SHA-256.
- [ ] A production Matter Overdrive 0.6 JAR exists in `build/libs/`.
- [ ] Existing test world loads past 100%, reaches Joining World and enters play normally.
- [ ] No return of the startup matter-audit hang.
- [ ] No return of the tooltip/search-tree recursive matter freeze.

## Android core ability audit
- [ ] Cloak drains FE while active and becomes visibly active on the HUD.
- [ ] Cloak breaks hostile target locks in its intended radius.
- [ ] Entering cloak grants the stated mobility/defensive window.
- [ ] Leaving cloak grants the stated ambush Strength/mobility window.
- [ ] Force Field drains idle FE while enabled.
- [ ] Force Field actually reduces incoming damage by the stated base ratio.
- [ ] Adamant Chassis raises Force Field absorption to the stated upgraded ratio.
- [ ] Force Field activation gives its absorption/repulse feedback.
- [ ] Sonic Shockwave costs FE, respects cooldown, damages enemies, slows/weakens and knocks them back.
- [ ] Shockwave reports actual target count/radius/damage in feedback.
- [ ] Teleport respects collision and does not place the player inside solid blocks.
- [ ] Teleport grants the stated post-blink mobility/resistance and enemy disruption.

## Android loadout truthfulness
- [ ] Vanguard Protocol modifies tagged Android ability damage.
- [ ] Singularity Lattice changes both Shockwave and Teleport as described.
- [ ] Reactive Exoshell visibly triggers its Resistance/Absorption window and respects retrigger timing.
- [ ] Phase Navigator changes Teleport range/cooldown/post-blink effects and cloak exit.
- [ ] Fragment of Translocation changes post-Teleport movement as described.
- [ ] Feedback/Harmonics/Recursive Core return FE from tagged ability hits.
- [ ] Corrosive Cloud applies its poison/weakness effects on ability damage.
- [ ] Mass Driver increases ability damage and knockback.
- [ ] Defensive fragments/passives measurably reduce damage rather than being description-only.
- [ ] Every currently selectable Aspect has an observable runtime effect.
- [ ] Every currently selectable Fragment has an observable runtime effect or an accurately described passive modifier.
- [ ] Every Passive Protocol has a runtime effect matching its description.

## H / N / G subclass abilities
For every specialization, equip it and test H, N and G against multiple hostile mobs.

- [ ] H name in HUD matches the Class Matrix.
- [ ] N name in HUD matches the Class Matrix.
- [ ] G name in HUD matches the Class Matrix.
- [ ] FE cost shown by HUD/Matrix matches actual FE consumed.
- [ ] Cooldown shown by HUD starts immediately and counts down smoothly.
- [ ] Damage/control/healing/range are clearly noticeable at their new 0.6 values.
- [ ] Damaging H/N/G abilities trigger ability-damage Aspects/Fragments.
- [ ] No H/N/G action consumes FE without producing its described primary effect.

Specializations:
- [ ] Singularity Breaker
- [ ] Citadel
- [ ] Phase Stalker
- [ ] Drone Commander
- [ ] Hunter-Killer
- [ ] Precision Frame
- [ ] Siege Frame
- [ ] Nanite Weaver
- [ ] Gravity Core

## Android HUD
- [ ] HUD header shows the active specialization.
- [ ] FE bar and low-energy warning are correct.
- [ ] Level, XP and unspent point count are correct.
- [ ] Loadout line reports correct Aspect/Fragment counts and passive protocol.
- [ ] Core ability state correctly shows READY, cooldown, cloak/field state or offline state.
- [ ] H/N/G each have distinct visible slots.
- [ ] H/N/G show READY, cooldown, level lock or insufficient-FE status correctly.
- [ ] H/N/G show their FE costs.
- [ ] Cooldown progress strips advance in the correct direction.
- [ ] HUD remains readable at GUI scales 2, 3 and Auto.

## Linked Drone fleet
- [ ] Owned drones never target the owner.
- [ ] Same-owner drones never target one another.
- [ ] Same-owner drone projectiles cannot damage fleet allies.
- [ ] FOLLOW uses distinct stable formation positions.
- [ ] Two, four and six drones do not merge into the same position while following.
- [ ] Drones navigate doors/corridors/stairs without persistent wall clipping.
- [ ] Far-behind catch-up only places drones into collision-free positions.
- [ ] PASSIVE/DEFENSIVE/AGGRESSIVE/FOLLOW command modes persist after save/reload.
- [ ] Strength effects on drones increase their projectile damage.
- [ ] Command Authority extends active support to the stated 48-block envelope.
- [ ] Overmind/Swarm/Target Link/Ordnance bonuses produce measurable damage changes.

## Android Spawner squad
- [ ] Spawner owns at most six units.
- [ ] Spawned mix remains approximately 30% melee / 70% ranged over repeated populations.
- [ ] Six patrol-drive slots remain usable and persisted.
- [ ] Color and PATROL/GUARD/HOLD/ESCORT persist after save/reload.
- [ ] Same-spawner Androids remain allied and share hostile targets.
- [ ] ESCORT uses stable two-column formation positions rather than stacking.
- [ ] ESCORT catch-up does not teleport units into solid blocks or another squad member.
- [ ] GUARD returns units toward the spawner.
- [ ] HOLD stops navigation when no combat target is active.
- [ ] DISMISS SQUAD removes only units belonging to that spawner.

## Weapons
- [ ] Phaser, Phaser Rifle, Ion Sniper and Plasma Shotgun refuse to fire when internal FE is insufficient.
- [ ] Reload only takes Energy Packs or Weapon/HC Battery items, never another gun.
- [ ] Creative Battery remains the explicit unlimited-energy exception.
- [ ] Heat and overheat are visible on the weapon HUD and match actual weapon state.
- [ ] Weapon HUD clearly reports NO INTERNAL FE, OVERHEATED or READY.
- [ ] Phaser HUD reports the active STUN/KILL power mode.
- [ ] Ion Sniper aiming uses 0.40 base FOV multiplier.
- [ ] Sniper Scope uses the recovered 0.85 aiming override.
- [ ] Camera recoil is a brief shot impulse and does not wobble continuously while merely holding a weapon.
- [ ] Ion Sniper unzoomed recoil remains stronger than aimed recoil.
- [ ] Weapon models remain visible and correctly oriented in first and third person.

## Weapon Station
- [ ] All seven physical slots remain clickable on HOME, MODULES and STATS.
- [ ] HOME reports weapon frame, FE, heat/overheat, module count and sight.
- [ ] HOME combat-ready / reload / overheat state matches the weapon.
- [ ] MODULES correctly maps Battery, Color, Barrel, Sights, Utility A and Utility B.
- [ ] STATS updates immediately when a module changes.
- [ ] Damage/energy/cooldown/range multipliers match the installed modules.
- [ ] Capacity display matches the weapon after battery/capacity modules.
- [ ] Aim zoom correctly reports Ion Sniper 0.40 base or Sniper Scope 0.85 override.

## Fusion Reactor / anomaly runtime
This section tests existing placed/reactor anomalies only. Natural anomaly generation is intentionally out of scope.

- [ ] Ring structure validation still distinguishes hull/coil/IO/anomaly failures.
- [ ] RUN/SCRAM and redstone mode persist.
- [ ] Potential FE/t, generated FE/t and connected demand are all distinguishable in the reactor GUI.
- [ ] Reactor FE output scales with existing anomaly mass rather than being capped at a tiny fixed value.
- [ ] Multiple external consumers connected through Reactor IO + Heavy Energy Pipe receive fair output rather than the first receiver monopolising the buffer.
- [ ] Fair-share ordering rotates over time and survives save/reload.
- [ ] Direct machine-on-IO output remains functional.
- [ ] Matter input/output through Reactor IO remains functional after the FE routing change.
- [ ] Internal-ring power remains fair across supported internal receivers.
- [ ] Reactor-powered stabilizers continue to work.
- [ ] Stabilizer beam page correctly distinguishes no lock / clear / blocked / powered / redstone-paused states.
- [ ] Stabilizer upgrades remain clickable on all pages.
- [ ] Event-horizon living-entity deaths add mass once, not zero times or twice.
- [ ] Consumed item matter increases anomaly mass once.
- [ ] Space-Time Equalizer prevents pull/event-horizon effects on the wearer.
- [ ] Anomaly telemetry reports affected entities, horizon entities, last feed and broken blocks accurately.

## Machine GUI shared polish
- [ ] Shared frame/accent/section styling renders correctly on every modernized machine screen.
- [ ] New header accent does not cover titles or buttons.
- [ ] Inventory divider does not cross real inventory slots.
- [ ] Stronger slot outlines line up exactly with actual slot hitboxes.
- [ ] No screen moves or hides physical slots as a result of the shared-style change.
- [ ] Decomposer, Recycler, Replicator, Pattern Storage, Pattern Monitor, Transporter, Stabilizer, Charging Station, Solar, Microwave, Inscriber, Space-Time Accelerator, Weapon Station and Fusion Reactor remain usable.
- [ ] GUI scale 2, 3 and Auto do not create overlapping controls on the major screens.

## Release-hardening sanity
- [ ] `BUILD_LOCAL.bat` never reports BUILD PASSED without finding a JAR in `build/libs/`.
- [ ] Build helper lists each JAR path and byte size.
- [ ] SHA-256 is printed when Windows `certutil` is available.
- [ ] GuideME Android page reflects the current ability values and fleet behaviour.
- [ ] No Star Map source/data was changed by this systems pass.
- [ ] No structure set, biome modifier, configured/placed worldgen feature or natural-generation source was changed by this systems pass.
