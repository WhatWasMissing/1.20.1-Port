# Facility Restoration and Infrastructure Pass

Baseline: `a44e8f75a436496f9713ceeed3ed0ca8a89e4586` on `testing/tech-overhaul`.

This work continues the 9 September structure expansion without changing the native `Structure` / `StructurePiece` generation model. It deliberately does **not** restore the retired Star Map and does not return to large synchronous `Feature` stamping.

## Persistent recovery state

Generated facilities are located through their native structure starts and store a small per-site restoration stage in dimension `SavedData`. Progress survives save/reload and is keyed by facility family plus structure start chunk. Player-placed Facility Network Controllers outside a generated facility remain ordinary telemetry consoles.

Recovery stages are:

1. **Emergency power** - connected stored FE is accepted, otherwise a Battery, High Capacity Battery, Energy Pack or Creative Battery can bootstrap the site.
2. **Control repair** - one Isolinear Circuit Mk1 or better repairs the failed control hardware.
3. **Research verification** - secure access is released before the matching dossier is required; returning the family dossier completes the site without consuming it.
4. **Restored** - unused generated emergency security reserves stand down. Already-deployed defenders are not deleted.

Quantum Relay Stations and Fusion Research Complexes receive accessible recovery terminals in their primary cores because their existing Facility Network Controllers sit in control/reward wings that can be gated.

## Structure-specific security

Generated Android Spawners keep the finite-reserve rules from the previous pass, but apply persisted facility identities to deployed Androids. Manufacturing defenders favour speed, refinery units are lighter/aggressive, relay sentries gain detection range, bunker guards gain armour, fusion sentries gain durability/knockback resistance, and Black Site wardens receive the strongest profile. Minimum Android level increases for later-game families and the fourth Black Site deployment reaches at least level 3.

Player-built spawners retain the existing FE/squad behavior. Facility restoration only zeroes unused generated reserve charges.

## Traversal, secure thresholds and QA fixes

`FacilityInfrastructurePiece` remains a separate registered, chunk-clipped native piece family. The follow-up visual/topology audit expanded it to address routes that looked connected in source but were not reliably traversable as a player.

- Manufacturing, Refinery, Relay and Fusion service infrastructure is aligned to the existing elevated deck/bridge height rather than creating parallel decks.
- Each elevated service route has a dedicated ladder/landing connection.
- Generated secure thresholds use a reusable Security Door locked until emergency power and control repair are complete.
- Security Doors synchronize redstone across their full vertical column and store a `POWERED` transition state so unrelated neighbour updates do not erase manual open state.
- Refinery excavation and Fusion service wings now have reversible two-level transitions instead of one-way drops.
- Refinery excavation shaft receives a guaranteed bottom ladder landing.
- Bunker and Black Site entrance ladders receive explicit side exits at their lower landings so the ladder backing wall cannot trap the player.
- Bunker and Fusion entrances receive three-step exterior approaches to the sampled surface level.
- Black Site receives a four-block continuation from its old shaft top to a visible surface hatch.
- Black Site vault descent is widened to a three-block-wide six-level stair.
- Plant layout 1 explicitly opens the east entrance/corridor connection and gates the actual west Shipping approach.
- Plant layout 2 receives explicit L-links from the core to both offset wings plus a missing core-to-Shipping link.
- Black Site layout 2 receives a lower-level corridor to its additional laboratory.
- Fusion's service spine is explicitly connected to the observation bridge on either layout orientation.

See `docs/STRUCTURE_QA_VISUAL_LAB.md` for the visual audit and `docs/testing/FACILITY_RESTORATION_TEST_PLAN.md` for the runtime matrix.

## Reusable industrial palette

Six player-facing blocks remain part of the facility palette: `industrial_catwalk`, `industrial_railing`, `cable_tray`, `warning_light`, `damaged_panel`, and `security_door`. They have blockstates, models, block drops, pickaxe tags and survival recipes. Facility pieces use the same registered blocks rather than private worldgen-only stand-ins.

## Safety properties

- No generation-time entities are spawned by infrastructure pieces.
- Every infrastructure write checks both the active chunk clip and the owning piece bounding box.
- Infrastructure is serialized as a normal registered `StructurePieceType`.
- Restoration uses server-side persistent state and interaction/tick hooks; it does not force-load facility chunks.
- Security deployment continues to check loaded candidate chunks and collision before insertion.
- Existing generated facilities are not retrofitted; test in fresh chunks.
- The retired Star Map remains absent from active modern worldgen.

## Offline QA

`scripts/facility_layout_lab.py` models all 18 deterministic facility layouts as architectural piece plans and can render SVG/HTML previews without running Forge. The static structure validator now contains regression guards for the topology failures found by that visual pass.

A full Forge compilation and runtime worldgen playtest are still required on a local checkout whenever GitHub Actions is unavailable. The offline lab is a design/static gate, not a substitute for Minecraft runtime validation.
