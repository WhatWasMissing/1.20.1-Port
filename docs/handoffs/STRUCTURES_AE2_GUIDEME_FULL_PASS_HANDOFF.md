# Structures + AE2 + GuideME full-pass handoff

Target branch: `testing/main`
Pass head when written: `fcc495c669be1504acb60ab36ec21da8f3ef09f8`
Date: 2026-09-06

## What landed

### Source-backed structure populations
- Added `SourcePopulationStructureFeature` as a correction layer around the existing modern terrain/layout wrapper.
- Android House now removes the reconstruction-only Drone, preserves the legacy 60% ranged / 40% melee ordinary-defender mix, normalizes ordinary population to the source-backed 3/4/5 distribution, and adds one guaranteed level-3 legendary ranged Rogue Android.
- Crashed Ship, Cargo Ship, Underwater Base and Sand Pit now remove Android/Drone combat occupants that were added by the early port but are absent from their authoritative 1.12.2 generation hooks.
- Mad Scientist House is intentionally left on its separate recovered village-piece population path pending a dedicated source audit.

### Deeper structure audit
- Confirmed embedded legacy template dimensions:
  - Android House 21x21
  - Crashed Ship 11x35
  - Cargo Ship 58x23
  - Underwater Base 43x43
  - Sand Pit 24x24
- Confirmed Crashed Ship ~256-block separation, Cargo Ship ~4096 plus 10% roll, Underwater Base deep-ocean + >26-water-depth validation + ~2048 separation, Sand Pit desert-only, Android House Y offset -2 and Sand Pit Y offset -9.
- Recovered Crashed Ship block callbacks: oriented Holo Signs, 30% crash text, crash contract injection into crates, and 5% decorated tier-3 weapon generation in Weapon Stations.
- Exact block-for-pixel PNG decoding is not yet claimed; current layouts remain procedural translations with modern terrain suitability/entrance cleanup.

### AE2 15.4.10
- Confirmed the correct integration boundary from the attached AE2 JAR: `ForgeExternalStorageStrategy` / `ItemHandlerAdapter` consume Forge `IItemHandler` capabilities.
- Kept Matter Network and ME separate; no AE-power-to-FE or Matter conversion was added.
- Added `/moae2` runtime capability audit. Point at a machine to inspect the item handler visible on each face, discovered slot count, FE capability and Matter capability.
- Expanded the GuideME AE2 page with the supported machine matrix, recommended Import/Export/Storage Bus use, persistence expectations and network boundary.

### GuideME 20.1.15
- Rechecked exact GuideME bytecode/API. `Guide.builder(ResourceLocation)` is valid and `GuideBuilder.build()` registers by default.
- Bridge now explicitly owns `guides/matteroverdrive/guide`, `matteroverdrive` namespace and `matteroverdrive:index.md` start page.
- Added page-aware opening via `PageAnchor.page(...)`, registry re-resolution via `Guides.getById`, and reload support via `Guides.reload()`.
- Added a GuideME World Structures page and linked it from the guide index.
- Data Pad fallback remains available when GuideME is absent.

## Runtime verification still required
1. Fresh Android Houses: 3-5 ordinary defenders, 60/40 ranged/melee over a sample, exactly one level-3 legendary ranged defender, no structure-created Drone.
2. Fresh Crashed/Cargo/Underwater/Sand structures: no structure-created Android/Drone defenders.
3. `/moae2` against every face of Crate, Decomposer, Recycler, Inscriber, Replicator and Pattern Storage; compare against actual Storage/Import/Export Bus behaviour.
4. AE2 automation through chunk unload/reload with no duplicate outputs, lost Pattern Drive NBT, or reset active processing.
5. Startup matrix: MO only; MO+AE2; MO+GuideME; MO+AE2+GuideME.
6. GuideME navigation through every page, then resource reload and reopen.
7. Data Pad fallback with GuideME absent.

## Highest-value structure follow-up
Translate the original PNG colour/alpha mapping tables and MOImageGen worker semantics into a 1.20.1 template decoder, then replace the remaining procedural structure approximations block-for-pixel. The old PNGs and constructors are now identified well enough to do this source-backed rather than by eye.

Before further writes, re-fetch `testing/main`; do not assume this SHA remains current.
