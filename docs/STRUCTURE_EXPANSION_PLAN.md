# Matter Overdrive Structure Expansion

This branch now contains a second-generation structure system built around the port's expanded technology rather than only legacy parity.

## Compact field-site family

### Abandoned Matter Laboratory
A compact ruined field laboratory built around Matter processing and storage. It uses the Matter Storage Matrix, Matter Excavator and Holographic Status Panel when those blocks are available.

### Android Relay Outpost
A damaged synthetic communications and charging post using the Android Induction Relay, Grid Capacitor and status-panel technology.

### Anomaly Research Site
A hardened observation/containment site using the Facility Network Controller and Holographic Status Panel with containment-themed hardware.

These remain intentionally compact procedural Features.

## Native multi-chunk facility family

Large facilities use Minecraft's native `Structure` / `StructurePiece` system rather than wide synchronous Feature stamping. Each room, corridor, gantry, tower or shaft is an independent piece and writes only inside the chunk-local `BoundingBox` supplied to `postProcess`.

Implemented facilities:

- Synthetic Manufacturing Plant
- Matter Refinery / Excavation Complex
- Quantum Relay Station
- Android Command Bunker / Drone Facility
- Fusion Research Complex
- Black Site

## Second-generation layout system

The original native-facility pass proved the safe multi-chunk approach but still formed a recognizable core-plus-four-wings cross. That has now been replaced by facility-specific assemblers with three stable seeded layout variants per structure start.

The variant is derived from the structure start chunk rather than mutable worldgen random. This keeps the layout deterministic across save/reload while still preventing every generated site from sharing one footprint.

Reusable modern pieces now include:

- X and Z connector corridors
- elevated service gantries
- rooftop machinery / ventilation plant
- relay masts
- security checkpoints
- glass observation bridges
- excavation shafts
- vertical secure entrances

A deterministic low-frequency damaged-room treatment can remove small roof/wall sections from eligible rooms without affecting the underlying piece graph.

## Facility identity

### Synthetic Manufacturing Plant
Bright production architecture with framed glazing, white fabrication areas, logistics floor striping, roof machinery and an elevated service gantry.

Current functional spaces:
- manufacturing/network core
- fabrication wing
- Android assembly/equipment wing
- shipping/cargo wing
- modern entrance

The three layouts include a traditional production cross, an L-shaped line and a wider split-wing factory arrangement.

### Matter Refinery / Excavation Complex
Heavier industrial construction with green Matter accents, a recessed excavation side, exposed service circulation and a dedicated vertical excavation shaft.

Current spaces:
- refinery control core
- excavation wing
- Matter storage wing
- processing/analyzer wing
- entrance
- excavation shaft
- service gantry

### Quantum Relay Station
A deliberately vertical and luminous facility. The central relay tower now rises above the surrounding rooms and the site is flanked by dedicated relay masts. One variant also carries an elevated observation bridge.

Current spaces:
- quantum core/tower
- relay wing
- Grid Capacitor power wing
- network control wing
- entry spine
- paired relay masts
- optional observation bridge

### Android Command Bunker
A mostly buried defensive complex with a separate surface approach, secure checkpoint, command room and synthetic support bays. Different variants change which side carries the drone, Android and armoury spaces.

Current spaces:
- command centre
- security checkpoint
- drone deployment bay with Android spawner encounter hardware
- Android station/charging bay
- armoury/cache room
- recessed surface entrance and descent

### Fusion Research Complex
The central room is no longer a square shell. It is a large circular containment/research chamber with a framed/glazed outer ring, radial lighting, stabilizer hardware, connected laboratories and an observation bridge.

Current spaces:
- circular fusion/anomaly research core
- stabilizer wing
- reactor control wing
- service/power wing
- observation bridge
- rooftop service machinery
- entrance

### Black Site
The rare Black Site is deliberately compartmentalized, dark and vertical. It uses carbon-fibre/reinforced materials, a small secure surface access, security checkpoint, underground laboratory/containment spaces and a lower vault. One layout adds a deeper secondary laboratory.

Current spaces:
- secure core
- security wing with Android spawner encounter hardware
- research laboratory
- containment room
- lower vault/cache
- checkpoint
- vertical secure entrance

## Modern construction language

New facilities and refreshed legacy structures no longer use strict 1.7 visual replication. Their gameplay identity remains recognizable while the construction language uses:

- structural beams and ribs instead of flat box walls
- framed industrial glass
- recessed/integrated lighting
- roof machinery and vents
- hazard/stripe floor accents
- circulation spaces rather than rooms touching directly
- recognizable silhouettes
- facility-specific palettes and verticality
- Holographic Status Panels and Holo Signs where appropriate

See `docs/STRUCTURE_DESIGN_LANGUAGE.md` for the detailed visual rules.

The current legacy refresh includes:

- Android House rebuilt as a modern synthetic safehouse with framed glazing, carbon-fibre roof framing, an illuminated central spine and modern Android/network equipment.
- Mad Scientist House rebuilt as a compact research pavilion with glass curtain walls, structural ribs, a roof overhang and separated laboratory/fabrication areas.
- Cargo Ship updated with hull ribs, window framing, internal lighting, a raised logistics/service spine and denser cargo staging.
- Crashed Ship given a torn hull/service section, structural ribs and exposed power hardware so it reads as a wreck rather than a parked ship.
- Underwater Base given radial illuminated circulation, hull ribs and more current Matter-network hardware.
- Sand Pit reframed as a buried industrial excavation with exposed tritanium deck, gantries, lighting and excavator hardware.

## Encounter and reward hooks

Security-oriented rooms now deliberately place existing Android Spawner hardware in Android bunker and Black Site security spaces. This gives those structures a concrete encounter hook without inventing a second encounter system.

Tritanium crate rooms are present in shipping, armoury and Black Site vault spaces. The current `TritaniumCrateBlockEntity` persists a Forge item handler but does not implement vanilla loot-table semantics, so this pass does not falsely attach chest loot tables that the crate cannot consume. A follow-up reward pass should either add one-time structure-loot support to the crate block entity or introduce a dedicated structure cache marker.

The standalone Anomaly Containment Unit is an item, not a registered placeable block. Where structure code requests a containment-unit visual it intentionally falls back to an explicit vanilla containment material until a dedicated placeable containment block exists.

## World-generation safety

The original large-template Feature approach demonstrated that wide synchronous structure stamping can reach outside the active generation region and hang world generation.

Rules going forward:

1. Large facilities use native `Structure` / `StructurePiece` generation.
2. Structure pieces write only through the current chunk-local `BoundingBox` passed to `postProcess`.
3. Compact field sites may remain Features only while their footprint stays small.
4. Do not restore the old wide PNG-template Feature stamping path for large structures.
5. Decorative complexity comes from additional pieces, local detail and vertical composition rather than unsafe direct cross-chunk writes.
6. Layout variation must be deterministic from the structure start so save/reload cannot rebuild a different facility graph.

## Validation

Run the lightweight source gate before a local Minecraft build:

```text
python scripts/validate_structure_expansion.py
```

It verifies the six native structure/structure-set JSON pairs, modern reusable piece types, stable layout-variant wiring, chunk-local clipping guard, referenced Matter Overdrive block ids and retired-system exclusion.

The full tech-overhaul static gate remains:

```text
python scripts/validate_tech_overhaul.py
```

A normal local Gradle build and in-game generation test are still required before release because static validation cannot prove Mojang/Forge runtime API behaviour.

## Current compact-site rarity

- Android Relay Outpost: approximately 1 placement attempt per 260 eligible chunks.
- Abandoned Matter Laboratory: approximately 1 per 320 eligible chunks.
- Anomaly Research Site: approximately 1 per 420 eligible chunks.

All three target Overworld biomes and run in the surface-structures generation step.

## Current large-facility rarity intent

Large facilities are intentionally much rarer than compact field sites. The Black Site is the rarest and is intended to function as an endgame exploration discovery rather than routine world clutter.

Exact spacing/separation should be tuned during the upcoming large playtest rather than aggressively regression-tested during this development pass.

## Next structure work

- add first-class structure reward seeding to Tritanium crates instead of empty cache hooks;
- add research/discovery progression hooks once the research spine is brought forward;
- add structure-specific damaged exterior pieces and debris fields rather than only small deterministic room damage;
- add dedicated railings, cable trays, wall light strips, blast doors and hazard-marking blocks if visual testing shows the existing decorative palette is insufficient;
- add structure-specific hostile encounter configuration rather than using the generic Android spawner defaults;
- tune terrain adaptation, entrances and spacing after the large playtest exposes real-world generation edge cases.
