# Structures + AE2 + GuideME full-pass handoff

Target branch: `testing/main`
Date: 2026-09-06

## Current state

The pass has moved beyond the original population-only audit. The three image-backed 1.12.2 structures now use the original PNG templates through a dedicated legacy decoder, external item automation has been hardened for AE2/Forge automation, GuideME 20.1.15 uses its actual optional API surface, and the first source-backed MOImageGen structure callbacks are restored.

## Structures

### Android House population
- `SourcePopulationStructureFeature` corrects the modern reconstruction after placement.
- Ordinary defenders are normalized to the source-backed 3/4/5 population.
- Source mix is 60% ranged / 40% melee.
- The reconstruction-only Drone is removed.
- Exactly one persistent level-3 legendary ranged Rogue Android is added at the centred equivalent of legacy template-relative `(12,4,10)`.

### Other populations
The authoritative 1.12.2 generation hooks for Crashed Ship, Cargo Ship, Underwater Base and Sand Pit do not create Rogue Android/Drone combat occupants, so reconstruction-only occupants are removed from those structures. Mad Scientist House remains on its separate recovered village-piece path.

### Exact PNG geometry
`LegacyImageTemplatePlacer` is now the normal generation path for:
- Crashed Ship (`11x35` legacy layer size)
- Cargo Ship (`58x23`)
- Underwater Base (`43x43`)

The PNG is decoded as the old MOImageGen Y-layer atlas. Cargo/Underwater alpha is translated as legacy metadata (`255-alpha`) where Minecraft 1.20.1 has a meaningful equivalent block state. Magenta remains explicit air. Random ore mapping is retained. If a legacy resource is missing/corrupt the old procedural translation remains a safety fallback rather than silently deleting worldgen.

Android House and Sand Pit remain class-driven translations because their authoritative generation path contains additional class logic and is not simply equivalent to these three PNG workers.

### Source-backed callbacks now restored
`LegacyStructureCallbacks` runs only after successful exact PNG placement.

Crashed Ship:
- crashed-ship crate is reset to the authoritative one-roll loot table: Tritanium Nugget weight 80, Battery weight 10, empty weight 10;
- `crash_landing` story contract is inserted and stores the crash position;
- Holo Signs use the two independent legacy 30% text checks (`I hope my insurance covers this.` / `Keep calm and respawn.` / otherwise blank);
- Weapon Stations retain the legacy 10/200 (5%) generated level-3 weapon chance, using the source weapon-type weights and modern registered weapon equivalents.

Cargo Ship:
- the red crate receives the named Mk1 Isolinear Circuit `Trade Route Agreement` and becomes the Trade Route quest position;
- the first lime crate receives the `trade_route` story contract after generation, matching the old worker state;
- Holo Signs receive the recovered Cargo Unit multiline status text. The modern Holo Sign renderer now supports multiple lines.

Underwater Base:
- exact template geometry/metadata is active;
- population hook remains intentionally empty;
- no invented post-generation callback has been added where the 1.12.2 behaviour has not yet been recovered.

### Placement facts retained from source audit
- Crashed Ship: roughly 256-block same-type separation in legacy.
- Cargo Ship: roughly 4096-block separation plus a 10% generation roll.
- Underwater Base: deep-ocean placement, >26 blocks water-depth validation, roughly 2048-block separation.
- Sand Pit: desert-only, Y offset -9.
- Android House: Y offset -2.

The current 1.20.1 placed-feature rarity system is not naively replaced with legacy candidate weights because the old generator combined weighted candidate selection with separate location/distance checks.

## AE2 15.4.10 / Forge automation

The attached AE2 JAR confirms external storage and buses consume Forge `IItemHandler`; Matter Overdrive therefore does not implement a fake ME network adapter and does not merge Matter Network with ME.

A concrete automation bug was fixed: raw `ItemStackHandler` permits extraction from slots even when `isItemValid` correctly rejects insertion. `AutomationItemHandler` now provides a capability-only view while internal machine inventories remain unchanged.

External item rules:
- Tritanium Crate: normal general-purpose storage.
- Decomposer: insert Input/Energy; extract Output.
- Recycler: insert Input/Energy; extract Output.
- Inscriber: insert Primary/Secondary/Energy; extract Output.
- Replicator: insert Pattern Drive/Energy; extract Output/Failure Output.
- Pattern Storage: public Energy/Pattern Drive slots are bidirectional, with existing item validity preserved.
- Machine upgrade inventories remain private and are never included in the exposed capability.

FE and Matter capabilities remain their existing Forge/MO capabilities. No AE power -> FE conversion, Matter -> AE conversion, or ME/Matter Network merge was added.

The automation-hardening head `ddb4972f3a65ceb9df89b5ef52d7e47b4b876f52` passed the Gradle **Build mod** step. The GitHub workflow then failed only at the repository's recurring Upload test JAR step.

## GuideME 20.1.15

The optional bridge was checked against the exact attached GuideME JAR.
- Builder entry point is `guideme.Guides.builder(ResourceLocation)`.
- `GuideBuilder.build()` registers the guide by default.
- registered guide re-resolution uses `Guides.getById`.
- arbitrary page opening uses `PageAnchor.page(...)`.
- GuideME remains reflection-loaded/optional so Matter Overdrive starts without it.
- resource folder is `guides/matteroverdrive/guide`, namespace `matteroverdrive`, start page `matteroverdrive:index.md`.
- the Data Pad uses GuideME as the primary full manual when available and retains its built-in fallback when GuideME is absent.

Important correction to the older handoff: GuideME 20.1.15 does **not** expose `Guides.reload()`. The invalid reflective reload call was removed; resource reload clears the cached guide and the bridge re-resolves the registered guide instead.

Guide pages currently cover Current Features, Survival, Matter, Power/Machines, Android, Fusion Reactor, Weapons, Matter Network, AE2, World Structures, Quests/Contracts, Star Map, Dimensional Pylon, and Transporter/Security.

## Runtime verification still required
1. Fresh Android Houses: sample 3-5 ordinary defenders, verify approximately 60/40 ranged/melee, exactly one level-3 legendary ranged defender, no structure-generated Drone.
2. Fresh Crashed Ship: exact silhouette, one source-weighted loot roll plus Crash Landing contract, sign text distribution, and rare Weapon Station weapon.
3. Fresh Cargo Ship: named Trade Route Agreement in red crate, Trade Route contract in lime crate, multiline Cargo Unit sign text.
4. Fresh Underwater Base: exact template shape, doors/ladders/crops/glass metadata, no structure-created combat population.
5. AE2 Storage/Import/Export Bus automation on every supported machine face through chunk unload/reload; verify no input stealing, duplication, lost Pattern Drive NBT or reset Replicator task state.
6. Startup matrix: MO only; MO+AE2; MO+GuideME; MO+AE2+GuideME.
7. GuideME: open every manual page, resource-reload, reopen; then remove GuideME and verify Data Pad fallback with no missing-class error.

## Remaining source-backed structure work
- Recover and apply exact old Holo Sign orientation/scale callback state where the modern sign backend can represent it.
- Finish any Underwater Base worker callbacks confirmed by bytecode; do not invent them.
- Audit Mad Scientist House and Sand Pit block/loot callback details against their authoritative class paths.
- Runtime-check the exact PNG atlas orientation and Y offsets in fresh chunks.

Before further writes, always re-fetch `testing/main`; do not assume a SHA remains current.
