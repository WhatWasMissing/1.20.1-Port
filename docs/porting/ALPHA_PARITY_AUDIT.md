# Matter Overdrive 1.12.2 to 1.20.1 parity audit

Branch: testing/alpha
Reference: supplied MatterOverdrive-1.12.2-0.7.1.0-universal jar
Audit date: 2026-09-04

## Reading this audit

- Implemented means there is a dedicated 1.20.1 implementation with a player-facing path.
- Partial means the main loop works but important legacy behaviours, content, rendering or depth remain absent.
- Registered only means the ID/resource exists but the original gameplay is not reproduced.
- Unverified means source inspection supports the claim, but it still needs in-game testing.

This is a gameplay audit, not just a registry count. The legacy jar contains roughly 73 blockstate definitions, 117 recipes, 494 texture assets and dedicated client renderers for weapons, machines, HUDs, entities and the Star Map.

## Parity matrix

| Legacy system | Port status | Current 1.20.1 scope | Major remaining gap |
|---|---|---|---|
| Core registry, recipes and decorative blocks | Partial | Legacy IDs, most block/items, recipe resources, sound registry and decorative content are present. | Some items are generic shells; visual runtime checks remain. |
| Matter decomposition and storage | Implemented | Decomposer, matter containers, recycler/refinement, matter pipes and persistent storage are implemented. | Legacy visual effects and all edge recipes need runtime checks. |
| Pattern analysis and replication | Implemented | Analyzer, Pattern Drives, Pattern Storage, Monitor queue, Replicator and task routing exist. | Legacy task-page depth and renderer polish are incomplete. |
| Matter network | Implemented | Network pipes, routers and switches are traversable task transports. | Advanced legacy configuration/flash-drive behaviour is absent. |
| Fusion reactor and anomaly | Partial | Reactor ring validation, IO, anomaly mass, stabilisers, overlay, shared ring FE and upgrades are implemented. | Exact legacy balance, custom visuals and every multiblock edge case need live comparison. |
| Power machines | Implemented | Solar Panel, Charging Station, FE pipe, Microwave, Inscriber and Space-Time Accelerator have dedicated behaviour. | Complex legacy animation/OBJ rendering is incomplete. |
| Transporter and pylons | Partial | Transporter and flash-drive workflow are implemented. | Original network/pylon presentation and richer targeting options are incomplete. |
| Android conversion and station | Partial | Pills, persistent FE, parts, station charging, HUD, active abilities, skill tree, respec and Rogue Android drops exist. | Mad Scientist transformation route, deeper legacy stat ranks, minimap and dedicated Android entities are absent. |
| Android baseline traits | Partial | Powered Androids maintain hunger/air, clear harmful effects, sink in water and show damage feedback. | Exact legacy beneficial-effect restrictions and visual glitch shader are not recreated. |
| Weapons and Weapon Station | Partial | Energy, heat, reloads, modules, Omni Tool and station persistence are implemented. | Dedicated first-person renderer, recoil/zoom/hand animation, attached OBJ optics, beam presentation and legacy weapon ecosystem remain unfinished. |
| Tritanium tools and armour | Implemented | Full tools, armour and set bonus exist. | Visual validation for all worn/item contexts remains required. |
| Contracts, Data Pad and Star Map | Partial | Contract market, collect/hunt progression, Data Pad pages and scan history are implemented. | Full quest types, dialog/NPC chains and full galaxy/star/planet simulation are missing. |
| Holo Sign and security protocols | Registered only | IDs and basic blocks/items exist. | Ownership, security protocol actions and programmable holographic sign behaviour are missing. |
| Mobs and NPCs | Partial | Android Spawner creates a safe tagged Husk with bionic-part drops. | Dedicated Rogue Android variants, Mad Scientist, failed animals, drones and related AI/rendering are missing. |
| World generation and structures | Partial | Tritanium/Dilithium ore generation is implemented. | Crashed ships, bases, anomaly events and themed structure generation are missing. |
| Client presentation | Partial | Machine screens, documentation, Android/weapon HUDs and many restored resources exist. | Legacy custom renderers for weapons, animated machines, holograms, entities and Star Map visuals are not at parity. |
| Legacy integrations | Not ported | None intentionally recreated. | Old ComputerCraft/Tinkers-era support must be redesigned for modern equivalents. |

## Confirmed high-risk areas

1. Weapon rendering is the largest visible parity gap. The 1.12.2 jar does not use JSON display transforms for first-person guns. It owns the complete weapon, recoil, zoom, hand and module rendering path in dedicated client classes.
2. Star Map parity is intentionally far from complete. Even legacy documentation called it under development; the port currently uses its screen for contract status rather than a galaxy simulation.
3. Security and Holo Sign IDs are not evidence of gameplay parity. Their original ownership/protocol system is still missing.
4. Registered decorative/model resources need in-world checks before being described as visually restored.
5. Existing build success does not validate save data, recipes, fresh-chunk generation, network routing, machine behaviour or rendering.

## Recommended implementation order

1. Complete the dedicated weapon renderer after the current test cycle.
2. Port the Holo Sign/security protocol foundation; it is self-contained and unlocks multiple legacy items.
3. Replace the tagged-Husk shortcut with a real Rogue Android entity and add the Mad Scientist route.
4. Add world structures/events, then expand contracts into a real quest framework.
5. Only then begin full Star Map simulation; it is the largest missing system.

## Focused test list

- Test all existing systems in docs/testing before treating the port status as verified.
- Specifically re-test reactor output against different anomaly mass values.
- Test every Android passive/active ability with zero, low and full FE.
- Test weapons in first and third person, firing and with each module type.
- Check translucent blocks, armour, crates, Inscriber and every machine face for model issues.
- Test fresh world generation, not only an existing test world.
- Confirm Holo Sign/security items are clearly treated as placeholders in survival documentation.

## Branch marker

The standard login message displays Alpha Version 3 on this branch. If it does not appear, the local checkout/build is not using the current testing/alpha revision.
