# Facility Structure QA v2

Branch target: `testing/tech-overhaul`

This pass upgrades the offline facility layout lab from a rectangle/topology viewer into a player-scale reachability gate and adds a separate chunk-clipped terrain-integration piece family. It preserves the native Forge 1.20.1 `Structure` / `StructurePiece` architecture and does not restore the retired Star Map.

## What QA v2 verifies

Run:

```text
python scripts/facility_layout_lab.py --check-only
python scripts/validate_structure_expansion.py
python scripts/validate_port_consistency.py
```

The layout lab mirrors all six facility families and all three deterministic layouts: **18 layouts total**.

For every layout it now builds a connectivity graph using the approximate player-accessible footprint and elevation of each room, corridor, service route, ladder, stair, gate and restoration terminal. It checks two states:

- **pre-restoration**: secure gates are treated as closed; the entrance and any required recovery terminal must remain reachable;
- **post-restoration**: gates are open and every mandatory room must be reachable without mining, block placement, flight or commands.

The generated report also records coverage and inaccessible pieces. Normal rendering produces both a plan SVG and an elevation/access-band SVG for every layout. Unreachable pieces are visually de-emphasized so a broken branch is obvious in the HTML gallery.

## Source-drift guard

The lab now checks critical Java assembler markers before trusting its mirrored layout data. This is intentionally not a Java parser; it is a regression tripwire. If important coordinates or piece families move in Java without the QA mirror being updated, the lab fails instead of silently validating stale geometry.

The active source remains authoritative. The mirror is a test oracle, never an alternative world generator.

## Bug found by the v2 reachability pass

The new graph immediately found a real traversal hole missed by the previous plan-only checks:

- Android Command Bunker layout 1 placed both Drone and Android bays on the west branch, but no north/south corridor linked the second bay.
- Layout 2 had the mirrored problem on the east branch.

`FacilityTerrainPiece.BUNKER_ANDROID_LINK_Z` now adds the missing chunk-clipped connector at the appropriate side for layouts 1 and 2. The layout lab requires both second bays to be reachable, making this a permanent regression gate.

## Terrain and exterior integration

`FacilityTerrainPiece` is a registered native `StructurePiece` family dedicated to bounded site integration. It adds:

- engineered entrance aprons for surface facilities;
- short retaining/support columns under aprons where terrain drops away;
- salvage-yard foundations so recovery sites do not float over small depressions;
- compact relay-mast plinths;
- supported bunker and fusion exterior approaches;
- a restrained Black Site hatch crown at terrain level;
- the bunker side-branch traversal connector discovered by QA v2.

Support fill is deliberately shallow and bounded. The piece checks both the active chunk clip and its own bounding box before reading or writing, never force-loads chunks, never spawns entities, and never replaces bedrock. It does **not** flatten hills or carve large slopes synchronously.

## What still requires Minecraft

Static QA cannot prove final voxel collision, client model rotation, lighting, terrain blending against every biome/noise shape, entity spawning, or the exact order Minecraft processes overlapping structure pieces. Runtime testing remains mandatory.

Highest-value checks in fresh chunks:

1. Bunker layouts 1 and 2: entrance -> command -> side branch -> second Android Bay, with no mining.
2. Surface facilities on slopes: apron supports should meet small drop-offs without creating giant artificial pillars.
3. Relay masts: plinths should read as supports and not overwrite interior circulation.
4. Fusion/Bunker approaches: exterior steps and support pad should meet terrain acceptably.
5. Black Site: hatch crown should be visible but remain subtle.
6. Cross chunk boundaries during generation and save/reload beside every new site-integration piece.

A world-generation stall, forced distant chunk load, piece deserialization failure or out-of-piece write is a release blocker.
