# Matter Overdrive Total Structure Redesign — Runtime Test Plan

Branch: `feature/lead-dev-expansion-2026-09-11`

Status: implementation committed; local compile/runtime verification required.

## Build gate

- [ ] `gradlew.bat compileJava --no-daemon`
- [ ] repository resource/documentation verification tasks pass
- [ ] client starts without registry/codec/structure-piece errors
- [ ] dedicated server can create/load a world without serialization errors

## Universal playthrough gate

For every structure below, test at least three separate generated starts where practical. Do not merely inspect from creative flight: enter from terrain and play through the route on foot.

Every structure must pass all of these:

- [ ] exterior silhouette is recognizably different from the other facilities
- [ ] entrance is visible/readable from a reasonable approach direction
- [ ] entrance is reachable without breaking blocks
- [ ] entrance leads to a real interior route rather than a dead vestibule
- [ ] every required room is connected
- [ ] no corridor ends accidentally inside a wall
- [ ] no room overlaps another in a way that blocks traversal
- [ ] doors/openings provide comfortable player clearance
- [ ] machines do not occupy the required centre route
- [ ] loot/security blocks are reachable
- [ ] hostile encounter spaces have room to move and fight
- [ ] vertical routes are usable without impossible jumps
- [ ] no unmarked fatal drops occur on the mandatory path
- [ ] deepest/core objective can be reached and the player can return to terrain
- [ ] damaged variants retain the complete mandatory route
- [ ] no terrain wall seals the only entrance
- [ ] no floating/incoherent approach is visible on ordinary terrain
- [ ] no chunk-border shearing/unclipped writes are visible
- [ ] save/reload preserves the same layout and route

Treat any failed item as a blocking structure bug.

## Legacy structures

### Crashed Ship
- [ ] `/locate structure matteroverdrive:crashed_ship`
- [ ] wreck reads as damaged/tapered rather than intact rectangular vessel
- [ ] torn hull entrance is obvious
- [ ] centre route connects cockpit, service zones and engineering tail
- [ ] impact breach/debris does not remove centre route

### Cargo Ship
- [ ] `/locate structure matteroverdrive:cargo_ship`
- [ ] bow/bridge, cargo bays, loading lane and engineering stern are visually distinct
- [ ] raised bridge is reachable by the built-in step route
- [ ] cargo racks leave functional aisles
- [ ] loading openings do not create unreachable shelves/rooms

### Underwater Base
- [ ] `/locate structure matteroverdrive:underwater_base`
- [ ] south airlock is reachable from surrounding water/seabed
- [ ] hub connects to all four radial pods
- [ ] pressure tubes remain dry/usable where intended
- [ ] cardinal pod doors line up with connecting tubes

### Mad Scientist House
- [ ] `/locate structure matteroverdrive:mad_scientist_house`
- [ ] surface structure reads as a residence/research cover rather than a pure machine box
- [ ] hidden descending route reaches basement lab
- [ ] basement partitions do not block central aisle
- [ ] player can return upstairs without breaking blocks

### Android House
- [ ] `/locate structure matteroverdrive:android_house`
- [ ] centre atrium and both technology wings connect
- [ ] rear secure/service room connects to atrium
- [ ] charging, logistics and operations areas are distinguishable

### Sand Pit / Excavation
- [ ] `/locate structure matteroverdrive:sand_pit`
- [ ] terraced dig reads clearly from surface
- [ ] zig-zag descent reaches bottom without fall damage requirement
- [ ] buried machinery/relic floor has clear aisle
- [ ] crane/gantry does not intersect the descent route

## Modern technology facilities

### Synthetic Manufacturing Plant
- [ ] `/locate structure matteroverdrive:synthetic_manufacturing_plant`
- [ ] route reads entrance -> security -> manufacturing -> side production -> shipping
- [ ] fabrication/assembly wings connect to central hall
- [ ] production spine is visually distinct and walkable

### Matter Refinery
- [ ] `/locate structure matteroverdrive:matter_refinery`
- [ ] route communicates extraction -> processing -> storage/output
- [ ] excavation access is usable
- [ ] lowered/vertical areas do not strand the player

### Quantum Relay Station
- [ ] `/locate structure matteroverdrive:quantum_relay_station`
- [ ] relay tower is a strong long-distance landmark
- [ ] control precedes relay core on player route
- [ ] power/relay wings and observation infrastructure are reachable

### Android Command Bunker
- [ ] `/locate structure matteroverdrive:android_command_bunker`
- [ ] surface vestibule connects to underground security via walkable descent
- [ ] security precedes command/deployment rooms
- [ ] armoury is reachable and player can return to entrance

### Fusion Research Complex
- [ ] `/locate structure matteroverdrive:fusion_research_complex`
- [ ] reactor/anomaly chamber dominates the structure visually
- [ ] control room precedes main chamber
- [ ] all four cardinal chamber openings remain usable
- [ ] observation ring and side wings do not block exits

### Black Site
- [ ] `/locate structure matteroverdrive:black_site`
- [ ] surface/concealed approach leads through security to command
- [ ] lab and containment wings are reachable
- [ ] guaranteed vertical connector reaches lower level
- [ ] lower corridor reaches vault
- [ ] player can return from vault to surface without breaking blocks

## Frontier Expedition structures

### Deep Matter Vault
- [ ] `/locate structure matteroverdrive:deep_matter_vault`
- [ ] surface entry and stepped descent align
- [ ] security precedes core
- [ ] archive and refinery wings connect
- [ ] secure cage has working entrances/exits and cannot trap player

### Autonomous Drone Foundry
- [ ] `/locate structure matteroverdrive:autonomous_drone_foundry`
- [ ] route reads gate -> control -> fabrication -> hangar
- [ ] salvage is optional side route, not required traversal blocker
- [ ] hangar scale feels meaningfully larger than ordinary rooms

### Anomaly Quarantine Site
- [ ] `/locate structure matteroverdrive:anomaly_quarantine_site`
- [ ] entry/descent/decon/security sequence is logical
- [ ] observation connects before/alongside containment objective
- [ ] all four containment cardinal openings remain usable

### Orbital Recovery Array
- [ ] `/locate structure matteroverdrive:orbital_recovery_array`
- [ ] control, processing and storage connect before array
- [ ] array has distinctive mast/support/dish silhouette
- [ ] catwalk rails and width are safe

## Logical-error sweep

Explicitly look for and report any instance of:

- [ ] door opening into solid wall
- [ ] corridor opening misaligned by one or more blocks
- [ ] room floor height mismatch without transition
- [ ] machine blocking sole door
- [ ] window facing directly into buried solid terrain where that makes no design sense
- [ ] stair/descent terminating above or below intended floor
- [ ] overlapping roofs/floors cutting through playable interior
- [ ] security checkpoint located after the protected objective
- [ ] armoury/storage inaccessible behind geometry
- [ ] facility purpose contradicted by its layout
- [ ] damage state removing only path
- [ ] structure exterior looking like a plain rectangular box despite intended specialist role

## Evidence for failures

Capture world seed, exact coordinates, structure ID, screenshots of entrance/problem/core, whether damage variant appears active, and relevant `latest.log` / `debug.log` excerpts. For connectivity failures, include screenshots from both sides of the broken connection.
