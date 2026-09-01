# Matter Overdrive 1.20.1 — Current Testing Reference

This reference describes the gameplay systems currently present on `testing/main`. A system being listed here means its implementation is wired into the current test build; items marked for runtime verification still need to be exercised in Minecraft before merging to `main`.

## Core machines and storage

| Feature | Current behaviour |
|---|---|
| Matter Decomposer | Converts supported items into matter, consumes FE, supports upgrades, failure chance and debug values. |
| Matter Recycler | Recycles supported items into matter with energy usage, inventory handling, upgrades and debug values. |
| Matter Analyzer | Analyses supported items/patterns, uses FE and participates in the pattern network. |
| Matter Replicator | Replicates queued patterns using matter and FE and reports cycle/failure data. |
| Pattern Storage | Stores Pattern Drives and supplies patterns to the network. |
| Pattern Monitor | Discovers networked Pattern Storage and Replicators and queues replication work. |
| Molecular Inscriber | Produces the Mk2-Mk4 Isolinear Circuit chain, accepts energy items/upgrades and persists its inventory. |
| Solar Panel | Produces FE in suitable daylight, stores/exports FE and supports Power Storage upgrades. |
| Tritanium Crates | All colour variants provide portable 54-slot storage and retain contents in dropped-item NBT. |
| Transporter | Uses a bound Transport Flash Drive and FE to move entities to a same-dimension target; Speed, Range, Power and Power Storage upgrades are active. |
| Charging Station | Buffers FE and charges compatible batteries/energy items. |
| Weapon Station | Installs/removes weapon modules, persists its contents and safely returns its weapon/modules when broken. |

## Matter, FE and network transport

| Feature | Current behaviour |
|---|---|
| Matter Container | Stores and transfers matter to compatible machines. |
| Matter Pipe | Carries matter between compatible matter endpoints. |
| Heavy Energy Cable | Uses the legacy `heavy_matter_pipe` ID but is FE-only in the current port. It buffers and relays Forge Energy and is not a matter-network path. |
| Network Pipe | Carries pattern-network discovery and general item-logistics connectivity. |
| Network Switch | Can enable/disable both general item logistics and pattern-network traversal. State persists. |
| Network Router | Powered item logistics router with filter support, endpoint/node diagnostics and anti-bounce routing state. Connected routers elect one active routing executor rather than double-moving the same network. |
| Dimensional Pylon | Bridges matching-channel item networks wirelessly within its supported range. Channel persists. |
| Pattern network | Analyzer, Pattern Storage, Pattern Monitor and Replicator discovery continues through enabled Network Pipe/Router/Switch paths. |

The general item network intentionally remembers inventories that have received routed items as sinks until those inventories become empty. This prevents undirected two-chest networks from endlessly moving the same items A -> B -> A while consuming FE.

## Fusion Reactor

The current reactor uses the larger ring structure represented by the Reactor Assembly Guide/overlay rather than the obsolete early compact cross layout.

Current controller behaviour includes:

- 100,000,000 FE base storage.
- 2,048 kM base matter storage.
- Matter-driven FE generation scaled by gravitational-anomaly mass and reactor efficiency.
- Reactor IO blocks linked from valid structure positions.
- Internal ring power distribution to compatible machines placed in supported ring positions.
- Connected machine FE-usage diagnostics.
- Persistent structure overlay toggle and Reactor Remote support.
- Power Storage and Matter Storage upgrades changing the corresponding capacities.
- Speed upgrades increasing generation rate and matter consumption proportionally.
- Range upgrades extending the vertical anomaly search/efficiency window from the base three-block distance, with a hard 16-block search cap.

Structure changes and upgrade changes trigger revalidation; the controller reports explicit fault/debug state in its GUI.

## Gravitational Anomaly and Stabilizers

The gravitational system is active in the current testing build:

- Anomalies initialize with persistent mass.
- Nearby items/living entities are pulled according to the current effective mass.
- Dropped items entering the event horizon are consumed once and add their matter value to anomaly mass.
- Living entities take event-horizon damage; their mass contribution is recorded once when the horizon kills them, not on each preceding damage tick.
- The Space-Time Equalizer protects a wearer from anomaly pull/event-horizon effects.
- Gravitational Stabilizers require FE, must face the anomaly, stop on a solid beam obstruction and suppress anomaly strength while powered.
- Stabilizer Power upgrades strengthen suppression and Power Storage upgrades expand its FE capacity.
- Multiple stabilizers can suppress the same anomaly.

## Weapons and charging

The current energy-weapon set includes Phaser, Phaser Rifle, Ion Sniper and Plasma Shotgun plus supported weapon modules.

- Firing is server-checked against the weapon's actual shot cost.
- Heat/overheat state is tracked and shown in the weapon HUD.
- Energy Packs contribute their defined 32,000 FE and are consumed when used.
- Normal and HC weapon batteries contribute only the FE they actually contain; partial batteries cannot fill a weapon for free.
- Batteries are drained rather than destroyed and can be recharged in the Charging Station.
- Weapon Station module state persists and is packed back into the weapon before the station drops its contents.

Held-model alignment and other visual transforms still require in-client verification.

## Android system

The current Android test implementation includes:

- Blue Pill conversion with persistent Android FE/state.
- Red Pill deactivation that returns installed bionic parts.
- Yellow Pill Android-FE recharge.
- Head, Chest, Arms and Legs bionic parts installed through the Android Station.
- Android HUD synchronisation.
- Android Station FE charging within four blocks. Its 2,000 FE/t hand-off is shared between nearby converted players and GUI data is viewer-specific.
- Rogue Android Spawner that consumes FE to create tagged Rogue Android Husks and supplies random bionic-part drops.

The Chest part uses the vanilla Resistance effect while powered; it does not apply a second hidden damage multiplier.

## Contracts and Star Map

- Contract Market supplies collect/hunt Contract items with persistent objective, progress and reward data.
- One pickup/kill advances one matching contract rather than every duplicate copy in the inventory.
- Collect progress is capped to the amount the player's inventory can actually accept from the pickup.
- Completed contracts can be redeemed at the Contract Market.
- After the final offer is taken, the market waits 1,200 ticks before generating the next set; reopening it early does not bypass the delay.
- Star Map reports active/completed contracts for the player viewing it. Multiple players can use the same Star Map without overwriting one another's displayed state.

## Armour, tools and client rendering

- Tritanium tools/armour are registered and functional.
- Full Tritanium armour applies its configured defensive set bonus.
- Equipped Tritanium armour is wired to `tritanium_layer_1.png` / `tritanium_layer_2.png`.
- Industrial Glass, Bounding Box, Matter Plasma and Molten Tritanium are assigned translucent render layers client-side.

Actual transparency appearance, crate/Inscriber UV alignment and weapon held transforms are runtime visual checks and cannot be certified by the Java build alone.

## Persistence / break-safety expectations

Current machine state that should persist includes inventories, upgrades, FE/matter buffers, contracts, router filter/routing state, switch state, pylon channel and Android player state where applicable.

Blocks with internal user items that have dedicated inventories should return those contents when broken. The audit regression checklist specifically covers Charging Station batteries, Stabilizer upgrades, Router filters and Weapon Station weapons/modules.

## Runtime verification

Use the checklists under `docs/testing/`, especially:

- `AUDIT_FIXES_TESTING.md`
- `ANDROID_SYSTEM_TESTING.md`
- `MATTER_NETWORK_LOGISTICS_TESTING.md`
- `CONTRACTS_STAR_MAP_TESTING.md`

A successful GitHub Actions build proves the Forge project compiles/packages; it does not replace the in-world runtime and visual checks above.
