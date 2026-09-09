# Matter Overdrive Structure Expansion

This pass begins a new generation of structures built around the port's expanded technology rather than only legacy parity.

## First compact structure family

### Abandoned Matter Laboratory
A compact ruined field laboratory built around Matter processing and storage. It uses the Matter Storage Matrix, Matter Excavator and Holographic Status Panel when those blocks are available.

### Android Relay Outpost
A damaged synthetic communications and charging post using the Android Induction Relay, Grid Capacitor and status-panel technology.

### Anomaly Research Site
A hardened observation/containment site using the Anomaly Containment Unit, Facility Network Controller and Holographic Status Panel.

These remain intentionally compact procedural Features.

## Native multi-chunk facility family

The large-facility migration is now underway using Minecraft's native `Structure` / `StructurePiece` system rather than wide synchronous Feature stamping.

Implemented facility definitions / modular room layouts:

- Synthetic Manufacturing Plant
- Matter Refinery / Excavation Complex
- Quantum Relay Station
- Android Command Bunker / Drone Facility
- Fusion Research Complex
- Black Site

The facility system breaks a site into independently generated room pieces. Minecraft supplies each piece with the active chunk's clipping box, allowing the facility footprint to cross chunk boundaries without forcing neighbouring chunks to generate synchronously.

### Current room themes

Synthetic Manufacturing Plant:
- manufacturing core
- fabrication wing
- assembly / Android equipment wing
- shipping / cargo wing
- entrance

Matter Refinery:
- refinery core
- excavation wing
- Matter storage wing
- processing wing
- entrance

Quantum Relay Station:
- quantum core / tower
- relay wing
- power wing
- network control wing
- entrance

Android Command Bunker:
- bunker command centre
- drone deployment bay
- Android equipment bay
- armoury
- recessed bunker entrance

Fusion Research Complex:
- fusion / anomaly core
- stabilizer research wing
- reactor control wing
- service/power wing
- entrance

Black Site:
- secure core
- research laboratory
- containment wing
- vault
- security wing
- vertical secure entrance

## Modern visual overhaul

New and legacy structures are no longer being treated as strict 1.7 visual replicas. Their gameplay identity may remain recognizable, but the construction language should match the newer systems in the 1.20.1 port.

See `docs/STRUCTURE_DESIGN_LANGUAGE.md` for the detailed rules.

The current legacy refresh includes:

- Android House rebuilt as a modern synthetic safehouse with framed glazing, carbon-fibre roof framing, an illuminated central spine, modern Android/network equipment and no Star Map.
- Mad Scientist House rebuilt as a compact research pavilion with glass curtain walls, structural ribs, a roof overhang and separated laboratory/fabrication areas.
- Cargo Ship updated with hull ribs, window framing, internal lighting, a raised logistics/service spine and denser cargo staging.
- Crashed Ship given a torn hull/service section, structural ribs and exposed power hardware so it reads as a wreck rather than a parked ship.
- Underwater Base given radial illuminated circulation, hull ribs and more current Matter-network hardware.
- Sand Pit reframed as a buried industrial excavation with exposed tritanium deck, gantries, lighting and excavator hardware.

## World-generation safety

The original large-template Feature approach demonstrated that wide synchronous structure stamping can reach outside the active generation region and hang world generation.

Rules going forward:

1. Large facilities use native `Structure` / `StructurePiece` generation.
2. Structure pieces write only through the current chunk-local `BoundingBox` passed to `postProcess`.
3. Compact field sites may remain Features only while their footprint stays small.
4. Do not restore the old wide PNG-template Feature stamping path for large structures.
5. New decorative complexity should come from modular pieces, detail and vertical composition rather than unsafe direct cross-chunk block writes.

## Current compact-site rarity

- Android Relay Outpost: approximately 1 placement attempt per 260 eligible chunks.
- Abandoned Matter Laboratory: approximately 1 per 320 eligible chunks.
- Anomaly Research Site: approximately 1 per 420 eligible chunks.

All three target Overworld biomes and run in the surface-structures generation step.

## Current large-facility rarity intent

Large facilities are intentionally much rarer than compact field sites. The Black Site is the rarest and is intended to function as an endgame exploration discovery rather than routine world clutter.

Exact spacing/separation should be tuned during the upcoming large playtest rather than aggressively regression-tested during this development pass.

## Next structure work

- add richer exterior silhouettes around the modular rooms (roof equipment, relay masts, exhaust/service stacks, gantries and entry plazas);
- add connector/corridor variants so facilities do not always form the same cross-shaped plan;
- add layout variants per facility and seeded optional wings;
- add structure-specific loot tables / research rewards;
- integrate hostile Android / security encounters where appropriate;
- add damaged/abandoned variants of manufacturing and refinery sites;
- add research/discovery progression hooks once the research spine is brought forward;
- add dedicated structure decoration blocks such as railings, cable trays, light strips, blast doors and hazard markings if needed.
