# Matter Overdrive Structure Total Redesign — 2026-09-11

Branch: `feature/lead-dev-expansion-2026-09-11`

## Goal

The previous structure set generated, but too many locations still read as rectangular shells with machines placed inside them. This pass treats that as a design failure rather than a cosmetic issue. Primary geometry for every current structure family has been rebuilt around traversal, recognizable function, environmental storytelling and distinct silhouettes.

## Non-negotiable structure contract

A structure is not considered correct because it generated without crashing. Every required route must support the following loop without breaking blocks:

`terrain -> obvious entrance -> security/operations -> specialised spaces -> core objective -> return route -> terrain`

Required properties:

- entrances are visible and physically reachable;
- every required room has a continuous player-sized connection;
- no required route relies on an accidental hole in damaged geometry;
- corridors have at least a three-block-high traversable centre lane;
- machines are placed outside the mandatory centre route;
- vertical movement uses broad stepped routes or deliberately usable access pieces;
- damaged variants never delete the only entrance, cardinal connector or centre aisle;
- rooms express their purpose through geometry, not labels alone;
- exterior silhouettes communicate facility type;
- large facilities have landmarks rather than uniformly flat roofs;
- structure writes remain chunk clipped through native `StructurePiece` generation.

## Legacy structures rebuilt

### Crashed Ship

Rebuilt as a tapered, breached two-deck wreck. The player enters through a visibly torn south hull and follows a lit central route through cockpit/service/engineering zones. Midship bulkheads define spaces without blocking the aisle. The impact breach and debris are intentionally off-route.

### Cargo Ship

Rebuilt as a long industrial freighter with a chamfered bow/stern, raised but walkable bridge, central circulation spine, two cargo rack bays, side loading lane and a distinct engineering stern. Cargo storage now reads as a logistics operation rather than random containers in one hull volume.

### Underwater Base

Rebuilt as a radial pressure installation: central hub plus four separate research/service pods connected by enclosed pressure tubes. A south airlock establishes an obvious entrance. Cardinal doors are always retained in every pressure pod.

### Mad Scientist House / Laboratory

Rebuilt as an asymmetric surface residence hiding a much larger underground laboratory. A broad descending route connects the surface annex to a divided wet-lab/fabrication/specimen level. The laboratory has a permanent three-wide central route.

### Android House / Safehouse

Rebuilt with a central public atrium, two offset technology wings and a rear secure service volume. The footprint is H-like rather than a single box. Charging/maintenance, logistics and secure operations each have dedicated areas connected by explicit openings.

### Sand Pit / Excavation Site

Rebuilt as a terraced industrial dig with multiple excavation levels, a broad zig-zag descent, exposed buried technology, operations equipment and a tall surface crane. The main excavation aisle remains clear and the site is visually legible from the surface.

## Modern technology facilities rebuilt

### Synthetic Manufacturing Plant

Now follows a production route: gate -> security -> central manufacturing line -> fabrication/assembly wings -> shipping/loading. The manufacturing hall contains a visible central production spine and raised service infrastructure.

### Matter Refinery

Now communicates process flow: entrance/security -> refinery core -> excavation/raw intake -> processing -> storage/output. The excavation side has an explicit vertical access structure.

### Quantum Relay Station

Reorganized around a visually dominant central relay tower, with front control, side power/diagnostics wings, twin transmission masts and observation infrastructure.

### Android Command Bunker

Reorganized as a fortified hierarchy: surface vestibule -> stepped underground access -> security -> command -> drone/android deployment -> rear armoury. The surface-to-bunker transition is now deliberately walkable.

### Fusion Research Complex

Rebuilt around a large circular reactor/anomaly chamber with cardinal connections and an internal observation ring. Reactor control sits before the main chamber; stabilizer and service wings are side-accessed rather than acting as arbitrary pass-through rooms.

### Black Site

Reorganized as the deepest security progression: concealed descent -> security -> command -> research/containment wings -> guaranteed vertical connector -> lower vault. This fixes the previous topology where the lower vault could exist without a logically guaranteed playable connection.

## Frontier Expedition structures rebuilt

### Deep Matter Vault

Surface entry -> stepped descent -> security -> central vault -> archive/refinery wings. The central vault has a four-door secure cage so its visual containment does not create a trap.

### Autonomous Drone Foundry

Gate -> control -> fabrication line -> hangar/loading, with salvage as an optional side route. The hangar is deliberately larger than normal rooms and has marked service/charging positions.

### Anomaly Quarantine Site

Entry -> descent -> decontamination -> security -> observation -> circular containment chamber. The containment chamber uses four guaranteed cardinal access points.

### Orbital Recovery Array

Entry -> control -> processing/storage wings -> external recovery array. The array now has a tall mast, structural support arms and dish-like glass geometry to create a recognizable long-distance silhouette.

## Shape language changes

Generic industrial modules now use chamfered corners, recessed side work areas, structural ribs, glazed bands and clear central aisles. Large specialist spaces use bespoke geometry instead of the generic room shell:

- circular fusion containment;
- circular anomaly quarantine;
- radial underwater pressure pods;
- tapered ship hulls;
- large marked hangars;
- tiered command dais;
- terraced excavation;
- tall relay/recovery landmarks.

## Damage rules

Damage is deterministic and deliberately applied away from the required route. Breaches target off-route corners/roof sections and may replace optional machinery with inert wreckage, but centre aisles, cardinal doors, entry openings and explicit vertical transitions remain intact.

## Runtime verification still required

This connector environment cannot run the local Forge client. The redesign must therefore be validated in a fresh world using `docs/testing/STRUCTURE_TOTAL_REDESIGN_TEST_PLAN.md`. Any route failure, sealed entrance, impossible vertical transition, intersecting room, blocked machine, inaccessible cache or chunk-border tear is a blocking bug and should be corrected rather than documented as acceptable.
