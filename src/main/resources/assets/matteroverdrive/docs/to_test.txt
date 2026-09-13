# Matter Overdrive 1.20.1 - `testing/tech-overhaul` runtime checklist

Release line: `0.6`
Target: Minecraft 1.20.1 / Forge 47.4.10 / Java 17

This is the current development-branch test list. Static validators and a successful build reduce risk but do not replace Forge/Minecraft runtime verification.

> **Retired content:** the Star Map is not part of the active port. Historical inventory/localization references are archaeology only and must not be used as restoration requirements.

## Preflight

Run from the repository root:

```text
VERIFY_M2_BUILD.bat
```

Or run the static gates individually:

```text
python scripts/facility_layout_lab.py --check-only
python scripts/validate_structure_expansion.py
python scripts/validate_port_consistency.py
python scripts/validate_android_consistency.py
python scripts/validate_weapon_consistency.py
python scripts/validate_network_transport_consistency.py
```

Attach `build/reports/m2-port-consistency.md` when reporting static-audit warnings.

## PDA, lore and village specialists

- [ ] Find a Field Scientist, Systems Engineer and Mad Scientist near a vanilla village.
- [ ] Speak to each specialist and confirm the contact is recorded once in the PDA field log.
- [ ] Confirm Field Scientist assignments issue the existing Matter Technology contracts and progress through crafting/placement events.
- [ ] Confirm Systems Engineer assignments issue the existing network/power contracts and progress through placement events.
- [ ] Confirm the Mad Scientist campaign still issues its sequential story/research assignments without duplicate contracts.
- [ ] Relog and reopen the PDA: research status, assignment text, contact lore and scan history remain available.
- [ ] Confirm PDA manual fallback opens without GuideME and GuideME opens the same current manual when installed.

## Wireless FE transfer

- [ ] Charge an Android through an Android Induction Relay and confirm the relay's FE buffer decreases by the amount delivered.
- [ ] Confirm the Induction Relay never charges across dimensions and splits output fairly between nearby Androids.
- [ ] Feed a Quantum Power Relay from an adjacent FE source and confirm an explicitly linked same-dimension relay receives FE.
- [ ] Confirm a nearby unlinked Quantum Power Relay receives zero wireless FE.
- [ ] Configure an adjacent source/output face as DISABLED and confirm a relay does not pull through that face.
- [ ] Configure an adjacent consumer/input face as DISABLED and confirm a relay does not push through that face.
- [ ] Clear a Quantum Linker pair and confirm transfer stops on the next server tick without FE duplication.
- [ ] Save/reload both relays with loaded chunks and confirm energy and reciprocal links persist.

## GuideME blocks and block assets

- [ ] Open GuideME's Block Reference and verify every registered block has an item image and an embedded current crafting recipe.
- [ ] Open representative machine, pipe, decorative, crate, reactor and security-door entries from the reference page.
- [ ] Confirm the block reference contains no active retired-system page or broken local link.
- [ ] Run the block/resource audit and confirm no registered block has a missing blockstate, item model, recipe or loot table.
- [ ] Inspect the main block palette in-world: Heavy Energy Cable, Matter Pipe, Network Pipe and Hybrid Conduit remain visually distinct.
- [ ] Inspect decorative plates, vents, crates and reactor panels in a placed 3D room; confirm faces, collision and rotations are readable.

## Highest priority: facility QA v2

Use a fresh world or unexplored chunks.

- [ ] Layout lab reports all **18** technology-facility layouts reachable.
- [ ] Generate/locate all six facility families without world creation or exploration hangs.
- [ ] Cross chunk boundaries while each site generates; neighbouring chunks complete normally.
- [ ] Save/reload beside every family with no missing-piece/deserialization errors.
- [ ] Android Bunker layout 1: reach the west second Android Bay through the new Z connector.
- [ ] Android Bunker layout 2: reach the east second Android Bay through the mirrored connector.
- [ ] Relay/Fusion recovery terminals remain reachable before secure access is restored.
- [ ] Black Site surface hatch -> security -> vault -> lower lab (layout 2) remains traversable without mining.
- [ ] Refinery excavation and Fusion service-wing lowered transitions work in both directions.
- [ ] Service ladders/catwalks have solid landings and at least two blocks of usable headroom.
- [ ] Security Doors remain restoration-gated and all stacked segments synchronize with redstone.

## Terrain / exterior integration

- [ ] Surface-facility entrance aprons meet ordinary flat/sloped terrain naturally.
- [ ] Short support piers fill local drop-offs without producing giant pillars into caves/ravines.
- [ ] Salvage yards in layouts 1/2 no longer visibly float over small depressions.
- [ ] Relay mast plinths support the elevated mast bases without blocking circulation.
- [ ] Bunker and Fusion approach support does not bury or obstruct the existing exterior stairs.
- [ ] Black Site hatch crown is visible at terrain level but remains subtler than a surface facility.
- [ ] New terrain pieces never overwrite bedrock or unrelated terrain outside their own footprint.

## Facility restoration / rewards / security

- [ ] Emergency power -> control repair -> secure access -> matching research dossier -> restored loop persists across reload.
- [ ] Generated security doors reject manual/redstone opening before control repair.
- [ ] Generated facility caches resolve themed loot once and never refill after reopening/reload/automation.
- [ ] All six research dossiers grant their first-discovery reward once per player and do not advance scientist quests incorrectly.
- [ ] Facility-specific Android names/stats/reserves match the intended family.
- [ ] Restoring a facility stands down unused generated reserve without deleting already deployed defenders.
- [ ] Player-built Android Spawners retain normal FE/squad behavior.
- [ ] Main facility caches have a rare themed Legendary Relic pool: Manufacturing=Overclocked Relay, Bunker=Swarm Beacon/Aegis Prism, Black Site=Hunter Lens, Refinery=Nanite Crown, Relay=Capacitor Heart and Fusion=Phase Anchor.
- [ ] Relic items show a glint, distinct model/texture and dynamic protocol name; right-clicking as an Android installs the matching existing passive and consumes the relic.
- [ ] A non-Android cannot consume a relic, an already-installed protocol is not consumed twice, and the selected protocol persists after relog/save reload.

## Matter economy and machines

- [ ] Matter Analyzer, Decomposer, Pattern Drive and Replicator agree on effective matter value.
- [ ] Explicit, tag, recipe-derived, dynamic and fallback matter values behave as documented.
- [ ] Recipe cycles terminate and multi-output recipes do not inflate/zero values incorrectly.
- [ ] Decomposer -> Matter Pipe -> storage/Replicator/Fusion IO works through multi-pipe chains.
- [ ] Recycler, Inscriber, Microwave, Pattern Storage and Pattern Monitor persist inventory/progress/upgrades.
- [ ] Machine GUIs retain real slots on every page and report real telemetry rather than decorative values.

## Power / reactor / anomaly

- [ ] Fusion ring validation identifies missing/wrong blocks correctly.
- [ ] Reactor IO exports FE through chained Heavy Energy Cable infrastructure.
- [ ] Shared ring FE and configured stabilizer-from-reactor behavior still work.
- [ ] RUN/SCRAM/redstone/overlay state persists.
- [ ] Living entities consumed by an event horizon add anomaly mass once.
- [ ] Stabilizer facing, obstruction and power checks remain correct.
- [ ] Space-Time Equalizer protects its wearer from pull/event-horizon damage.

## Network / transporter

- [ ] Router/Switch routing survives branching networks and save/reload.
- [ ] Per-side policies/priorities and Hybrid Conduit FE+Matter routing behave as configured.
- [ ] Configure a machine face to INPUT/OUTPUT/DISABLED, break the block, then place a fresh machine at the same position: the replacement starts at default BOTH instead of inheriting stale side policy.
- [ ] Hybrid Conduit visually joins Heavy Energy Cable and Matter/Network pipes without broken arms/seams.
- [ ] Network diagnostic tooling reports the actual connected graph.
- [ ] Router item movement conserves item count when moving between ordinary chests/machines and never duplicates through branching networks.
- [ ] Transporter accepts a target exactly at its displayed effective range (32 blocks with no Range Upgrade) and rejects one block beyond it.
- [ ] A Transporter destination in an unloaded chunk does not force-load that chunk and does not begin/complete transport until the destination is loaded.
- [ ] Obstruct the saved target and nearby arrival cells: a completed charge-up aborts safely, spends no transport FE and starts no cooldown when zero entities can arrive.
- [ ] Leave only one safe arrival for multiple entities: telemetry reports the count actually moved and the successful transport charges its FE cost once.
- [ ] Transported entities arrive on supported, collision-free space rather than inside solid blocks.
- [ ] Transporter uses exact required FE and does not duplicate transports through chained infrastructure.

## Android progression / drones

- [ ] Android HUD appears after conversion and keybinds cycle/activate/open the tree correctly.
- [ ] Class/subclass, Aspects, Fragments, Passive Protocols and Mastery persist through level-up/relog/death.
- [ ] Refund/reset behavior does not erase unrelated progression.
- [ ] **Capacitor Core:** installing it raises effective Android storage from **100,000 FE to 150,000 FE** in the Android Station, HUD and Skill Tree.
- [ ] With a Capacitor Core installed, crouch-held Battery/HC Battery charging continues past 100,000 FE and can reach 150,000 FE.
- [ ] Fragment of Induction / Recursive Core bonus charging also continues to the chassis-adjusted maximum instead of stopping at 100,000 FE.
- [ ] At 150,000 FE capacity, 50% loadout thresholds trigger at 75,000 FE and 75% thresholds at 112,500 FE; removing the core returns thresholds to 50,000/75,000 FE.
- [ ] Charge above 100,000 FE, remove/swap the Capacitor Core, then reinstall it: excess FE is permanently clamped to the reduced capacity and does **not** reappear as hidden banked energy.
- [ ] HUD XP progress matches the nonlinear Skill Tree thresholds (150, 400, 800, 1,400, 2,250, 3,400, 5,000, 7,200, 10,000 total XP).
- [ ] HUD available Ascension Points matches the Skill Tree: one point every two Android levels, maximum five at level 10.
- [ ] Drone Matrix level and selected behavior persist.
- [ ] Drone support overlap audit: Command Authority provides the 48-block envelope and Speed II, Guardian Directive provides the 40-block envelope and Resistance II, and Reinforced Drones upgrades resistance to Resistance III.
- [ ] Drone repair sources stack as distinct contributions, while Swarm Logic improves repair only for fleets of at least two; it does not silently duplicate fleet damage scaling.
- [ ] Ordnance affects ranged drone arrows, Ordnance Link applies Slowness II to ranged hits, and Hunter Network/Target Link/Hunter Lens each contribute their documented glowing-target bonuses.
- [ ] Owned drones/android squads never attack owner/allies and cannot be silently stolen by another player.
- [ ] Patrol/guard/hold/escort state and patrol drives persist after save/reload and level-up.

## Weapons

- [ ] Phaser, Phaser Rifle, Ion Sniper and Plasma Shotgun cannot fire without valid FE.
- [ ] One weapon never drains another weapon as a reload source.
- [ ] Energy Packs / intended battery sources reload correctly and rechargeable batteries remain rechargeable.
- [ ] Heat, overheat and reload work in Survival and Creative.
- [ ] Install a high-capacity weapon battery, charge above the capacity of a smaller/no-battery configuration, then swap/remove it in the Weapon Station: stored weapon FE clamps to the final packed capacity and does not reappear when the larger battery is reinstalled.
- [ ] Opening/closing a Weapon Station without changing the battery does not destroy charge merely because modules are temporarily unpacked for editing.
- [ ] Ion Sniper aim/FOV, Sniper Scope override and recoil behavior match the current recovered parity values.
- [ ] Weapon Station slots/modules persist and its stats update immediately after module changes.
- [ ] First/third-person weapon transforms remain visible and correctly oriented.

## Legacy/world-content regression

- [ ] Legacy native structures still generate in new chunks: crashed ship, cargo ship, underwater base, Mad Scientist house, Android house and Sand Pit.
- [ ] Compact abandoned lab / Android relay / anomaly research features remain small and do not reproduce historical wide-Feature stalls.
- [ ] Natural gravitational anomalies generate conservatively and persist.
- [ ] Structure occupants do not duplicate on reload or count incorrectly toward player spawner ownership caps.
- [ ] Underwater-base interiors remain dry.

## Persistence / release sanity

- [ ] Save/reload preserves machine inventories, FE, matter, upgrades, Android state, patrol data, quests/contracts and facility restoration state.
- [ ] GuideME index opens every active system page and local links resolve.
- [ ] Current Features and in-game testing documentation identify the 0.6 line accurately.
- [ ] No active registered content shows missing-model purple/black textures.
- [ ] No current guide, registry or worldgen path restores the retired Star Map.
