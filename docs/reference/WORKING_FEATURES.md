# Matter Overdrive 1.20.1 - Current Feature Reference

Branch: `main`
Release line: `0.7`
Build identity: `Matter Overdrive 0.7`, maintained by MVQ1303

Legacy references:
- Matter Overdrive 1.7.10 `0.4.2`.
- Matter Overdrive 1.12.2 `0.7.1.0`.

This is the source-of-truth feature summary and is mirrored by the GuideME **Current Features** page. Implemented does not mean runtime-confirmed; use `docs/testing/TO_TEST.md` for deferred manual checks.

## 0.7 additions compared with 0.6

- Energy Bank blocks provide persistent dual FE/Matter storage for reactor output, later machine demand and compatible FE/Matter networks without silently converting or duplicating either resource.
- The tech overhaul adds Quantum Flux Reactor, Environmental Regulator, Wall Terminal, new reactor/decorative blocks, hybrid FE+Matter conduit support, frontier facilities and additional machine telemetry.
- Rogue Androids, Ranged Rogue Androids, Drones, Mutant Scientists, Assimilators and Phase Stalkers have natural hostile-spawn definitions; Field Scientists, Systems Engineers and Mad Scientists populate vanilla villages.
- Vanilla structure loot keeps its original pools and receives Matter Overdrive supplies plus seven deterministic Legendary Relic sources. Six main facility loot pools also contain their themed relics.
- The PDA, GuideME pages, relic behavior, facility discovery, source/resource validators and deferred test plan are connected to the same active registries and progression systems.
- The native Destiny set now contains 49 energy-weapon profiles: 14 retained profiles and 35 additional GunPack conversions with imported geometry, textures, source audio, available animations and per-weapon display transforms. Every profile uses the existing Energy Weapon and Weapon Station module contracts.
- First-person firearm presentation uses the imported display transforms and bounded ADS delta. Third-person generic held-use posing is suppressed while an energy weapon is active, so aiming does not look like continuous vanilla item use.

## Matter, power and machines

Decomposer, Recycler, Matter Analyzer, Pattern Drives, Pattern Storage, Pattern Monitor, Replicator, Inscriber/circuit progression, Matter Scanner, Portable Decomposer, Matter Containers and Matter Pipe are implemented. Solar Panel, Heavy Energy Cable, Microwave, Space-Time Accelerator, Charging Station, Transporter, Tritanium Crates, Weapon Station and Tritanium Wrench are functional. Decomposer, Matter Recycler and Microwave expose their real IO and four upgrade slots through HOME/TASKS/UPGRADES pages. Solar Panel exposes live generation, sky/light/daylight, buffer and output telemetry while preserving its real upgrade slots. Pattern Storage preserves its energy slot, six Pattern Drive slots and four upgrades; Pattern Monitor preserves its 12 clickable ghost-pattern request slots. Replicator, Transporter, Charging Station, Space-Time Accelerator and Inscriber retain their richer real-state operator pages. Charging Station battery and Matter Storage Matrix cell bays expose guarded public item capabilities for AE2/Forge automation without exposing machine upgrades.

Matter valuation resolves explicit values, Matter Dust values, tag bases, recursively derived recipe values and deterministic fallbacks with cycle/depth protection, output-count division and cheapest-positive alternatives. Analyzer, Decomposer, Pattern Drive and Replicator share the level-aware value path; tooltips and startup diagnostics expose the value source.

Fusion Reactor/gravity includes horizontal structure validation, shared Controller/IO storage, anomaly-mass-scaled output, upgrades, cable output, demand telemetry, RUN/SCRAM, redstone/comparator behavior, Reactor Remote, persistent overlays, Gravitational Anomaly mass/pull/event-horizon behavior, Equalizer protection and powered Stabilizers. Quantum Flux Reactor heat, reactor IO and Environmental Regulator protection are integrated with PDA hazard discovery. Energy Bank stores FE and Matter independently, accepts reactor IO output, exposes controlled output to compatible networks, persists both buffers and never feeds output back into its source reactor.

## Network

Network Pipe, Network Switch, Network Router and matching-channel Pylon routing are present. Switch state persists and changes routing/appearance. Router supports filtering, Network Flash Drive destination filtering, four Speed/Hyper-Speed slots, multi-stack item budgets and the 10 FE/item execution limit. Heavy Energy Pipe exposes live buffer and last-output telemetry. Hybrid Conduit can route both FE and Matter through compatible sides while preserving side policy, range, conservation and reciprocal-link cleanup.

## Androids, drones and entities

Android conversion, FE/HUD, body parts, abilities, V/B/K controls, class/subclass selection, Aspects, Fragments, Passive Protocols, Artifacts and the persistent perk tree are present. Android Spawner restores six-unit mixed melee/ranged populations, persistent ownership, patrol drives, PATROL/GUARD/HOLD/ESCORT, commander formations, squad colors and coordinated targeting. Rogue and Ranged Rogue Androids, Failed animals, Mad Scientists, Mutant Scientists, Drones, Assimilators and Phase Stalkers are implemented. Linked Drones support FOLLOW/DEFENSIVE/PASSIVE/AGGRESSIVE while unowned Drones remain hostile. Command, Guardian, Swarm, Ordnance, Hunter and Escort overlaps are reduced to distinct documented contributions.

Natural hostile spawning uses Forge biome modifiers for the six configured entities. Village population hooks add Field Scientists, Systems Engineers and Mad Scientists without replacing vanilla villagers. PDA contact records and NPC assignment flow use the existing contract and discovery systems.

## Weapons

Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun, Omni Tool, Vex Mythoclast and 49 native Destiny weapons use FE payment, heat/overheat where applicable, reload, recoil and current weapon module effects. Weapon Station retains its real Weapon/Battery/Color/Barrel/Sights/Utility slots on every page and packs installed modules back into the weapon without losing or exceeding final capacity.

Ion Sniper uses the recovered 0.40 base zoom multiplier while aimed; Sniper Scope uses the recovered 0.85 override through the scope-first/base-second rule. Scope accuracy/range effects remain server-authoritative. Camera recoil reacts to actual shot timestamps and heat. Energy sourcing is limited to the weapon's stored energy, installed creative battery, explicit Energy Packs and real `WeaponBatteryItem` sources; unrelated energy weapons are never used as batteries.

The 35 additional GunPack conversions have native vanilla-rendered geometry, source UV textures, available source animations, source fire/third-person fire recordings and available draw/reload recordings. The loader accepts normalized and native `static_idle`/`shoot`/reload names, compact and per-face Bedrock UV forms, and explicit static-idle fallback for models without source animation clips. Stolen Will uses the source-defined shared Universal Remote fire samples. The five original profiles absent from the GunPack keep their existing recordings. All Destiny profiles accept battery, barrel, sights, colour and utility module effects through the Matter Overdrive Weapon System and Weapon Station.

## Structures, loot and exploration

Sixteen Matter Overdrive-authored structure definitions remain registered for serializers, existing-save compatibility and future reviewed/event-authored encounters: crashed spacecraft, cargo ships, underwater bases, Mad Scientist houses, Android Houses, Sand Pits, six modern facilities and four Frontier site classes. They are intentionally dormant in 0.7: no MO structure set or structure biome modifier places them in new worlds while their unfinished visual/content pass is deferred.

The existing structure pieces, facility caches, restoration/security hooks and environmental dressing remain available to already-generated content and source validation. New player-event routes authenticate all 16 archive records through technology acquisition, block mining, hostile encounters, village assignments, Field Operations and recovered-lore-fragment inspection. Frontier archive progress uses its existing persistent ledger through those same events; it no longer requires generated coordinates.

Vanilla loot injection adds Matter Overdrive supplies without replacing vanilla loot in dungeons, mineshafts, temples, outposts, strongholds, bastions, mansions, shipwrecks, buried treasure, End Cities and Ancient Cities. Legendary Relics use the existing recovered artifact item and existing Android passive-protocol selection; deterministic facility and vanilla relic IDs do not create a second progression system.

Natural Gravitational Anomaly generation remains conservative and uses the shared legacy starting-mass range. Generated anomalies use the real attraction, event-horizon consumption, mass growth and block-effect systems.

## PDA, GuideME and presentation

The Data Pad opens the server-authoritative PDA journal, records scans, technology inspection and player-event lore, displays research/contract/contact/discovery state and falls back cleanly when GuideME is unavailable. PDA voice callouts and long-form read-aloud narration can be toggled from the PDA's `VOICE: ON/OFF` control or the `P` key; disabling voice stops spoken output immediately while captions and PDA text remain available. GuideME registration points to the Matter Overdrive guide; current feature, power, Android, weapon, specialist, structure, infrastructure and block-reference pages use local links plus live item/recipe references. Every active player-facing block has a documented description, item image model, recipe and block resource contract.

The title/briefing screens describe world-based discovery and the retired strategic map without exposing it as active content. No active strategic-map block, registry, worldgen path or progression system is shipped.

## Security, GUI and static quality gates

Empty/Claim/Access/Remove protocols and security-aware wrench dismantling are implemented. Operator screens keep real inventory/player slots visible and only expose controls backed by current server state. Static gates cover active registries, JSON/model/texture references, loot/spawn/PDA/relic wiring, GuideME coverage, weapon contracts, Android capacity/perks, network transport, tech-overhaul resources, model bounds, structure compatibility and lore-event routes. Gradle resource verification is configured alongside the source validators.

## Known limitations and deferred verification

Exact legacy PNG-template geometry, unfinished structure visual/content quality, some deeper structure-specific objectives, drone flying/equipment parity, a few machine configuration modes, full Pylon overlays, remaining module mesh placement and full first-person hand choreography remain incomplete or require runtime confirmation. The imported model loader uses one vanilla cube UV anchor for faces whose source data provides non-uniform per-face UV origins. All manual checks, including sound playback, model orientation, Energy Bank persistence/throughput, spawn rates, old-save structure compatibility and release smoke tests, are deferred to `docs/testing/TO_TEST.md`.
