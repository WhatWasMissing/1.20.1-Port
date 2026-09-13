# Structure, AE2 and GuideME parity pass — 2026-09-06

Base: `testing/main` at `1f2290243c90e791ceb8274f59798a4dc4bd2dbe`

Authoritative references used:
- Matter Overdrive 1.12.2 `0.7.1.0`
- Matter Overdrive 1.7.10 `0.4.2` where the 1.12 branch does not contain the relevant system
- Applied Energistics 2 Forge `15.4.10`
- GuideME `20.1.15`

## Structure audit

The original 1.12.2 JAR contains the five image-generated building templates:

| Structure | PNG | Size | Source-backed generation facts | Port action in this pass |
| --- | --- | ---: | --- | --- |
| Android House | `android_house.png` | 21x21 | Y offset -2; 3-5 ordinary defenders; 60% ranged/40% melee; guaranteed level-3 legendary ranged Rogue Android | Restored population rule; removed non-source Drone |
| Crashed Ship | `crashed_ship_1.png` | 11x35 | ~256 block same-type separation; empty population hook; crashed-ship crate loot table; Holo Sign callback; rare Weapon Station generated gun | Removed non-source defenders; retained current translated footprint; exact callback/loot follow-up recorded |
| Cargo Ship | `cargo_ship.png` | 58x23 | ~4096 block separation plus 10% generation roll; population hook only injects generated contract into designated crate | Removed non-source Android/Drone defenders; contract injection remains a dedicated follow-up |
| Underwater Base | `underwater_base_1.png` | 43x43 | deep_ocean only; >26 blocks of water-depth validation; ~2048 block separation; empty population hook | Removed non-source Drone/Ranged Android population |
| Sand Pit | `sand_pit.png` | 24x24 | desert only; Y offset -9; empty population hook | Removed non-source Android/Drone population |

### Android House exact recovery

`MOAndroidHouseBuilding.onGeneration` loops from zero to `3 + random.nextInt(3)`, producing 3-5 ordinary defenders. `spawnAndroid` chooses `EntityRangedRogueAndroidMob` for 60% of rolls and the melee Rogue Android otherwise. The class then creates a Ranged Rogue Android, sets Android level 3, marks it legendary, and spawns it at template-relative `(12,4,10)`.

The 1.20.1 population correction layer keeps the existing translated 21x21 layout, removes the reconstruction-only Drone, normalizes the ordinary population to a 3/4/5 distribution, and adds the guaranteed legendary ranged defender at the centred equivalent of the legacy coordinate.

### Crashed Ship callbacks recovered

`MOWorldGenCrashedSpaceShip.onBlockPlace` contains source-backed behaviour beyond geometry:
- Holo Sign direction is selected from template colours.
- A Holo Sign has a 30% chance to receive one of the legacy crash texts.
- Tritanium Crates use `matteroverdrive:crashed_ship` and receive a generated `crash_landing` contract containing the crash position.
- A Weapon Station has a 10/200 (5%) chance to receive a random decorated tier-3 energy weapon.

The embedded `crashed_ship` loot table itself rolls once: Tritanium Nugget weight 80, Battery weight 10, empty weight 10.

The current translated structure loot is still richer than this exact legacy table, so exact crate/callback parity remains separate from this inhabitant correction and should be implemented without losing the restored quest chain.

### Cargo Ship special state

The old Cargo Ship worker remembers a designated crate and a generated QuestStack. Its `onGeneration` inserts the generated contract into that crate. It does not create combat occupants. The current 1.20.1 material loot is a modernization and is not a substitute for the missing contract worker state.

### Template status

The active port still uses procedural translations of the five PNG templates plus 1.20.1 terrain suitability/entrance cleanup. This pass does **not** claim block-for-pixel template parity. Exact template restoration requires translating the legacy colour/alpha block mapping tables from each constructor and the MOImageGen worker semantics; the original PNG dimensions/assets have now been positively identified and remain the authority for that future pass.

## AE2 15.4.10 integration

The attached AE2 JAR confirms Storage/Import/Export automation goes through `ForgeExternalStorageStrategy` and its `ExternalStorageFacade` item-handler path, which consumes Forge item-handler capabilities. Matter Overdrive therefore does not need or want an invented ME network implementation.

Supported contract:
- Tritanium Crates expose item storage.
- Decomposer exposes its public item handler, Forge Energy and Matter; upgrades remain private.
- Recycler exposes public items and Forge Energy; upgrades remain private.
- Inscriber exposes its primary/secondary/output/energy-item handler and Forge Energy; upgrades remain private.
- Replicator exposes public items, Forge Energy and Matter while keeping task/progress state machine-owned.
- Pattern Storage exposes Pattern Drive/energy inventory and Forge Energy; Pattern Drive NBT stays item-owned.

A new `/moae2` runtime audit reports the target machine's side-by-side Forge item-handler visibility, slot counts, FE capability and Matter capability. This makes the actual AE2-visible contract verifiable in-game without linking Matter Overdrive against AE2 classes.

Explicit non-goals, because they are not source-backed and would violate the architecture:
- no AE power -> FE conversion;
- no Matter -> AE item/fluid conversion;
- no merging Router/Switch/Pylon networks into ME;
- no exposing machine-upgrade inventories through AE2.

## GuideME 20.1.15 integration

The exact GuideME API was rechecked with `javap`. `Guide.builder(ResourceLocation)` is valid in 20.1.15, and `GuideBuilder.build()` registers by default. The bridge remains reflective so GuideME stays optional.

The integration is now page-aware and reload-aware:
- the resource folder is explicitly `guides/matteroverdrive/guide`;
- namespace is explicitly `matteroverdrive`;
- start page is explicitly `matteroverdrive:index.md`;
- registered guides are re-resolved through `Guides.getById`;
- arbitrary manual pages can be opened through `PageAnchor.page(...)`;
- `Guides.reload()` can be invoked through the optional bridge;
- a new World Structures page is linked from the GuideME index;
- the AE2 page now documents the complete automation contract and `/moae2` diagnostics.

GuideME remains optional. The Data Pad fallback pages still operate when GuideME is absent.

## Runtime verification required

1. Generate multiple fresh Android Houses and confirm 3-5 ordinary defenders plus exactly one legendary level-3 ranged defender, with no structure-generated Drone.
2. Generate Crashed Ship, Cargo Ship, Underwater Base and Sand Pit and confirm no structure-generated Rogue Android/Drone population is introduced by those features.
3. Run `/moae2` while targeting every supported machine face, then attach AE2 Export/Import/Storage buses and verify results match the reported Forge handlers.
4. Chunk-unload/reload automated Replicator, Pattern Storage and Inscriber setups and check for no duplication, lost Pattern Drive NBT or reset processing state.
5. Start with MO alone, MO+AE2, MO+GuideME, and MO+AE2+GuideME.
6. With GuideME 20.1.15 installed, open the Data Pad manual and navigate every page including World Structures and AE2; then reload resources and reopen the manual.
7. Without GuideME installed, verify the Data Pad fallback guide still opens with no missing-class error.
