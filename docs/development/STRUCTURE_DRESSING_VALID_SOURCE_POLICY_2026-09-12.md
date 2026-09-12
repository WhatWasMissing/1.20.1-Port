# Structure Dressing — Valid Source Policy

All structure-facing environmental work in the immersive-world pass must be derived from the currently valid native structure implementations, not from retired synchronous Feature stamping or speculative new geometry.

Authoritative source patterns:

- `LegacyVanillaStructurePiece` / `LegacyNativeStructure` for the six legacy sites;
- `ModernExplorationStructurePiece` / `TechnologyFacilityStructure` for the six modern facilities;
- `FrontierExplorationStructurePiece` / `FrontierSiteStructure` for the four Frontier sites;
- existing traversal-repair/mezzanine pieces where their specialized ordering is required.

`EnvironmentalDressingPiece` is intentionally separate from those geometry owners. It copies their safe `StructurePiece` mechanics (serializer, fixed bounds, chunk clip checks and local placement) and is added last as an optional overlay.

Rules:

1. Never replace a validated structure piece merely to add decoration.
2. Never change entrances, stairs, ladders, doors or central critical-path cells in a dressing pass.
3. Decorations may only occupy an already-air cell with solid, non-fluid support.
4. Side-route visual markers must remain non-solid/low profile; current implementation uses carpets, lanterns and candles.
5. If a candidate is blocked, unsupported or outside the current chunk clip, skip it rather than forcing placement.
6. No machines, spawners, caches, loot, entities, objectives or chunk tickets belong in the dressing piece.
7. Any future structure variant should begin by copying one of the known-good room/piece patterns and editing it, then pass topology and fresh-world runtime QA before replacing/augmenting the baseline.

This policy exists specifically to prevent environmental polish from regressing the hard-won world-loading and traversal stability of the current port.
