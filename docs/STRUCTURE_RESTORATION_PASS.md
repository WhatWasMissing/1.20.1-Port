# Facility Restoration and Infrastructure Pass

Baseline: `a44e8f75a436496f9713ceeed3ed0ca8a89e4586` on `testing/tech-overhaul`.

This pass continues the 9 September structure expansion without changing the native `Structure` / `StructurePiece` generation model. It deliberately does **not** restore the retired Star Map and does not return to large synchronous `Feature` stamping.

## Implemented

### Persistent recovery state

Generated facilities are located through their native structure starts and store a small per-site restoration stage in dimension `SavedData`. Progress survives save/reload and is keyed by facility family plus structure start chunk. Player-placed Facility Network Controllers outside a generated facility remain ordinary telemetry consoles.

Recovery stages are:

1. **Emergency power** - connected stored FE is accepted, otherwise a Battery, High Capacity Battery, Energy Pack or Creative Battery can bootstrap the site.
2. **Control repair** - one Isolinear Circuit Mk1 or better repairs the failed control hardware.
3. **Research verification** - secure access is released before the matching dossier is required; returning the family dossier completes the site without consuming it.
4. **Restored** - unused generated emergency security reserves stand down. Already-deployed defenders are not deleted.

Quantum Relay Stations and Fusion Research Complexes receive accessible recovery terminals in their primary cores because their existing Facility Network Controllers sit in control/reward wings that can be gated.

### Structure-specific security

Generated Android Spawners keep the finite-reserve rules from the previous pass, but now apply persisted facility identities to deployed Androids. Manufacturing defenders favour speed, refinery units are lighter/aggressive, relay sentries gain detection range, bunker guards gain armour, fusion sentries gain durability/knockback resistance, and Black Site wardens receive the strongest profile. Minimum Android level increases for later-game families and the fourth Black Site deployment reaches at least level 3.

Player-built spawners retain the existing FE/squad behavior. Facility restoration only zeroes unused generated reserve charges.

### Traversal and secure thresholds

A separate `FacilityInfrastructurePiece` type overlays infrastructure after the primary room graph. It remains chunk clipped with the same `BoundingBox` discipline as the room pieces.

- Manufacturing, Refinery, Relay and Fusion families receive service catwalk/spine detailing.
- Generated secure thresholds use a reusable Security Door that is locked until emergency power and control repair are complete.
- Refinery excavation shafts gain a ladder route.
- Android Bunker and Black Site vertical entrances gain ladder routes.
- The Black Site corridor now receives a six-block stair descent into the lower vault instead of requiring a drop/build-up workaround.
- Bunker gate placement follows the selected layout so the armoury approach is gated in all three variants.

### Reusable industrial palette

Six player-facing blocks were added: `industrial_catwalk`, `industrial_railing`, `cable_tray`, `warning_light`, `damaged_panel`, and `security_door`. They have blockstates, models, block drops, pickaxe tags and survival recipes. The facility infrastructure pieces use the same registered blocks rather than private worldgen-only stand-ins.

## Safety properties

- No generation-time entities are spawned by infrastructure pieces.
- Every infrastructure write checks both the active chunk clip and the owning piece bounding box.
- Infrastructure is serialized as a normal registered `StructurePieceType`.
- Restoration uses server-side persistent state and interaction/tick hooks; it does not force-load facility chunks.
- Security deployment continues to check loaded candidate chunks and collision before insertion.
- Existing generated facilities are not retrofitted; test in fresh chunks.

## Validation status

The structure validation script is extended to require the new piece registration, clipping guard, restoration state, security hooks and all six block resource/recipe/drop files. Model element coordinates remain inside the repository's `[-16, 32]` model-bounds gate.

A full Forge compilation and runtime worldgen playtest are still required on a local checkout because this environment has no Gradle dependency cache and GitHub Actions quota is intentionally not being used.
