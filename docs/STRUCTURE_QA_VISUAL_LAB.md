# Facility Structure QA / Visual Layout Lab

Branch target: `testing/tech-overhaul`

This pass adds an offline structure-design layer on top of the native Forge 1.20.1 `Structure` / `StructurePiece` implementation. It does **not** replace Minecraft world generation and does not restore the retired Star Map.

## Why this exists

The modern facilities are assembled from many independently clipped pieces. That is the correct worldgen architecture, but coordinate-only review can hide problems that span multiple pieces: a ladder can be continuous but terminate behind a wall, a corridor can overlap the wrong side of a room, or two elevated decks can occupy different Y levels while looking adjacent in Java.

`scripts/facility_layout_lab.py` mirrors all six facility families and all three deterministic layouts (18 layouts total) as an offline architectural plan. It produces per-layout SVGs, an HTML index and a JSON report under `build/reports/facility_layout_lab/`.

Run:

```text
python scripts/facility_layout_lab.py
python scripts/validate_structure_expansion.py
```

The layout lab uses only the Python standard library and does not require Minecraft, Forge, Pillow or matplotlib.

## Bugs found by the visual/topology audit

The QA pass found and fixes the following issues:

- Android Command Bunker entrance ladder could terminate behind the shaft backing wall instead of providing a lower landing into the checkpoint.
- Black Site entrance ladder had the same lower-landing problem.
- Black Site entrance stopped roughly four blocks below the sampled surface height; a surface hatch now continues the ladder to the exterior.
- Matter Refinery excavation wing was two blocks below its corridor with no reversible transition.
- Fusion service wing had the same two-block transition problem.
- Matter Refinery excavation shaft had no guaranteed solid ladder landing after the shaft interior was cleared.
- Plant service infrastructure used a second deck height over the existing gantry instead of sharing the deck.
- Refinery service infrastructure similarly created a parallel/disconnected deck.
- Relay layout 2 could place service infrastructure one block below its observation bridge; the service deck now shares the observation level.
- Elevated service routes had no guaranteed player access. Each affected family now receives a dedicated ladder/landing connection.
- Fusion's service spine and observation bridge are now explicitly bridged rather than merely being visually close.
- Bunker and Fusion entrances are below their sampled surface height and now receive three-step northern exterior approaches.
- Plant layout 1 gated the wrong shipping approach and relied on incidental corridor overlap to punch through the east-side entrance shell.
- Plant layout 2's offset corridors could leave the core disconnected from the two side wings and from Shipping. Explicit L-shaped links now close those gaps.
- Black Site layout 2's extra lower laboratory had no same-level connection; a lower corridor now connects it to the vault level.
- Black Site vault descent is widened to a three-block stair route instead of a one-block stair line.
- Multi-segment Security Doors could fight themselves when only one segment received redstone. The block now stores a synchronized `POWERED` state for the whole vertical column and only lets redstone transitions override manual state.

## Safety rules

The QA fixes preserve the existing worldgen safety model:

1. Facilities remain native `Structure` / `StructurePiece` generation.
2. Infrastructure writes are checked against both the active chunk `BoundingBox` and the piece's own bounding box.
3. No infrastructure piece force-loads chunks or spawns generation-time entities.
4. Vertical access remains bounded to the facility piece footprint.
5. Existing generated structures are not retrofitted; verify in fresh chunks.
6. The retired Star Map remains excluded from active worldgen and guides.

## What the visual lab can and cannot prove

The lab is intentionally a QA mirror, not an alternative generator. It is useful for facility silhouette, room spacing, Y-level relationships, route intent, secure-threshold placement and detecting missing layout-specific links. The static validator contains regression markers so known fixes cannot silently disappear.

Only Minecraft can prove final block-state rotation, model appearance, collision, terrain blending, lighting, entity spawning and exact `StructurePiece` processing in a real world. The runtime test plan therefore remains the final authority after the offline lab passes.
