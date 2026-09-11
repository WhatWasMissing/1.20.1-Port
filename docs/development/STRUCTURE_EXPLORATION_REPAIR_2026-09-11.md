# Structure Exploration Repair Pass — 2026-09-11

Branch: `feature/lead-dev-expansion-2026-09-11`

## Goal

Generated Matter Overdrive structures are exploration content, not free machine shops.
The vanilla-style contract is:

`terrain -> readable approach -> entrance -> critical path -> guarded reward/story spaces -> focal objective -> safe exit`

Players should not need to break or place blocks to finish a site.

## Repairs in this pass

- Cargo Ship generation moved from `surfaceY + 24` / Y86 minimum to local terrain level so normal survival movement can board it.
- Deep Matter Vault origin raised so its authored entry reaches the terrain surface instead of remaining buried.
- Android Command Bunker and Black Site origins were adjusted so their authored entrances meet surface terrain.
- Fusion Research Complex now starts at terrain level.
- Pre-redesign `FacilityInfrastructurePiece` and `FacilityTerrainPiece` overlays are no longer assembled into newly generated modern facilities. Their old offsets were capable of reintroducing contradictory ladders, corridors, gates and pads after the geometry redesign.

## Exploration-first generated rewards

`StructureExplorationSanitizer` now examines only Matter Overdrive functional block entities in loaded server chunks. A block is transformed only when Minecraft's `StructureManager` confirms that its position belongs to one of the 16 Matter Overdrive structures.

Functional generated machines are replaced with either:

- inert wreckage/industrial props; or
- a seeded Tritanium story cache using `chests/facilities/story_cache`.

Story caches can contain research dossiers, refined Matter, Tritanium components, circuits, energy supplies, upgrades and a rare artifact roll. They also include recovered-log/manual items for lightweight environmental storytelling.

When a safe side position exists, a cache receives a finite facility Android security marker. The guard marker is placed only if the candidate position has a solid floor and clear headroom; the system will not block a doorway or corridor just to force combat.

The sanitizer is self-limiting: once a generated machine has been converted, that block entity is no longer a candidate on later chunk loads. Player-placed machines outside Matter Overdrive structures are unaffected, and no neighboring chunk is force-loaded.

## Static topology validator

Run:

```bat
VALIDATE_STRUCTURE_TOPOLOGY.bat
```

or:

```text
python validate_structure_topology.py
```

The validator checks all 16 structure families for authored entry/objective waypoints, known audited regression signatures, stale helper-overlay assembly, chunk-clipped generation, forbidden force-loading, and the exploration-first sanitizer/loot policy.

It is intentionally allowed to fail while a known topology problem remains. Do not weaken a rule to make the validator green; repair the structure instead.

## Known remaining topology blockers

The initial audit still identified geometry issues that require direct repair in `LegacyNativeStructurePiece`:

- Android Safehouse side-wing connector coordinates;
- Mad Scientist laboratory descent continuity;
- Sand Pit/Excavation second ramp leg continuity;
- Crashed Ship terrain-to-deck return route.

These remain blocking until the validator and runtime survival traversal both pass.

## Runtime gate

Static validation cannot prove terrain interaction or collision. For every structure family, test at least three generated starts and applicable damaged/occupied variants in a fresh world. Walk from natural terrain to the objective and back without breaking or placing blocks. Any failure remains a generation bug.
