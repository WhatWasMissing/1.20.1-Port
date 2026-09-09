# Matter Overdrive Modern Structure Design Language

The 1.20.1 port should preserve the identity and gameplay role of classic Matter Overdrive structures without preserving the visibly dated construction language of the 1.7 era.

## Visual target

Matter Overdrive facilities should feel like functioning pieces of advanced infrastructure rather than rectangular shells containing machines.

The target language is:

- layered wall depth instead of single-plane facades;
- structural tritanium ribs and darker carbon-fibre framing;
- framed industrial glass rather than uninterrupted glass rectangles;
- recessed ceiling and floor lighting;
- service conduits, vents, coils, maintenance bays and equipment runs;
- purposeful room zoning: control, fabrication, storage, power, security, transit, research;
- recognizable silhouettes visible before the player reaches the entrance;
- raised roof elements, antennae, relay towers, gantries, overhangs and recessed entrances;
- colour/accent differences that communicate facility purpose;
- believable access paths and circulation instead of machines scattered against walls;
- occasional damage, abandoned sections and asymmetric repairs where a site is ruined.

## External inspiration

Reference only; do not copy structures or assets.

Contemporary 1.20-era technology/structure mods tend to get much of their visual improvement from build composition rather than sheer structure size. Useful references reviewed for this pass include tech-structure packs, Create-themed structure packs, BetterBlockZ-style futuristic/industrial palettes and modern industrial decoration mods. Recurring useful ideas were structural framing, machine bays, connected industrial spaces, integrated lighting, catwalk-like circulation and visually differentiated wall/floor systems.

Matter Overdrive should keep its own tritanium / holographic / matter-tech identity while adopting those more modern composition principles.

## Matter Overdrive palette

Primary structure blocks already available in the port:

- `decorative.tritanium_plate`
- `decorative.tritanium_plate_colored`
- `decorative.tritanium_plate_stripe`
- `decorative.white_plate`
- `decorative.carbon_fiber_plate`
- `decorative.beams`
- `decorative.floor_tiles`
- `decorative.floor_tile_white`
- `decorative.floor_tiles_green`
- `decorative.holo_matrix`
- `decorative.coils`
- `decorative.vent.bright`
- `decorative.vent.dark`
- `decorative.tritanium_lamp`
- `industrial_glass`
- `holo_sign`
- `holographic_status_panel`

The port should prefer these over vanilla iron/quartz blocks except where vanilla materials intentionally communicate ruin, excavation, containment or environmental integration.

## Facility identities

### Synthetic Manufacturing Plant
Bright industrial production architecture. Broad assembly halls, white/tritanium wall panels, striped logistics lanes, fabrication islands, charging bays, crates, visible power distribution and a central factory-control node.

### Matter Refinery / Excavation Complex
Heavier industrial architecture. Dark floors, green matter accents, storage-matrix vaults, decomposer banks, excavator controls, exposed service conduits and reinforced processing bays.

### Quantum Relay Station
Vertical and luminous. Strong central relay silhouette, holo-matrix elements, symmetrical relay wings, glass observation/control room and highly visible energy infrastructure.

### Android Command Bunker / Drone Facility
Low-profile, defensive and mostly buried. Carbon-fibre/tritanium structure, recessed entrance, command centre, charging/induction areas, drone or synthetic deployment bays, security checkpoints and armoury/storage areas.

### Fusion Research Complex
Large research campus around a visually dominant circular reactor/containment chamber. Control wing, stabilizer research wing, service/power wing, observation glazing and strong safety/containment visual language.

### Black Site
Rare, intentionally ominous endgame facility. Buried reinforced shell, minimal external footprint, long secure approach, dark material palette, compartmentalised laboratories, containment, weapons/synthetic research and a deep power level.

## Legacy refresh policy

Legacy structures can retain recognizable footprint or purpose while being rebuilt visually.

- Android House becomes a modern synthetic safehouse/operations residence. The retired Star Map must not be placed.
- Mad Scientist House becomes a compact research pavilion with modern glazing, structural framing and real lab zoning.
- Cargo Ship gains hull ribs, lighting, a central service/logistics spine and richer cargo spaces.
- Crashed Ship should visibly read as damaged through torn hull sections and exposed systems.
- Underwater Base should gain radial circulation, hull ribs and lighting to break up the large circular interior.
- Sand Pit should read as an excavation / buried industrial site rather than only a terrain depression.

## Worldgen safety rule

Large facilities must use native Minecraft `Structure` / `StructurePiece` generation. Each piece is written only through the chunk-local `BoundingBox` supplied to `postProcess`. Do not return to large synchronous Feature stamping across neighbouring chunks.

Compact field sites may remain ordinary Features because their footprint is kept safely small.

## Future decoration improvements

Potential additions that would improve structures without changing their systems:

- dedicated wall-light strips;
- thin structural trim blocks;
- ceiling cable trays;
- hazard floor markings;
- railing/catwalk blocks;
- blast/security doors;
- laboratory terminal variants;
- damaged machine/decorative variants;
- non-functional structure-only antenna and sensor blocks;
- loot/terminal markers that can be populated by structure processors later.
