## M1 verification R2 resource-path fix

- Corrected active 1.12 texture references from `matteroverdrive:items/...` and `matteroverdrive:blocks/...` to the 1.20.1 atlas-compatible `matteroverdrive:item/...` and `matteroverdrive:block/...` paths.
- Added active `textures/item` and `textures/block` trees while keeping the untouched legacy assets under `legacy_reference`.
- Updated OBJ material (`.mtl`) references and the industrial-glass texture metadata to the same modern paths.
- Strengthened `verifyM1Resources` so future builds fail if legacy plural model texture paths are reintroduced.
- Added `CHECK_M1_RUNTIME.bat`; `RUN_M1_CLIENT.bat` now runs it automatically after the development client exits.

# Milestone 1 Changelog

## Added

- Forge 1.20.1 / Java 17 project scaffold.
- Modern `@Mod` bootstrap and deferred registries.
- Placeholder registration for the recovered Legacy Edition block namespace.
- Modernized standalone item IDs for legacy metadata variants.
- Block items and a dedicated Matter Overdrive creative tab.
- Sound-event registration matching the legacy sound inventory.
- 1.20.1-safe temporary blockstates, models, loot tables, and mining tag.
- Modern JSON language files generated from the Legacy Edition `.lang` files.
- Complete original Matter Overdrive asset tree under `legacy_reference`.

## Carried forward

- Original textures.
- Original OGG sounds.
- Original `sounds.json`.
- Standard legacy item model JSON files that do not rely on removed Forge model loaders.

## Corrected

- `sounds.json` referenced `gui/button_loud_1`, but the shipped JAR contains
  `button_loud_0.ogg` and `button_loud_3.ogg`. The active port resource now
  points the missing `_1` entry at the shipped `_3` sound. The untouched
  original remains in `legacy_reference`.

## Replaced temporarily

Legacy Forge blockstate files are not active because many use removed 1.12
features such as `defaults`, `custom`, `submodel`, and
`forge:default-block`. They have been replaced by simple valid cube models
for the registry milestone, while the originals are retained for later
visual reconstruction.

## Not yet ported

Machine logic, block entities, menus, networking, Forge Energy, matter
storage/networking, Android mechanics, weapons, entities, worldgen, fusion,
anomalies, transporter logic, quests/dialogue, custom renderers, and fluids.

## Verification tooling update

- Added self-bootstrapping Windows and POSIX Gradle launchers pinned to Gradle 8.1.1.
- Added SHA-256 verification for the Gradle distribution.
- Added `verifyM1Resources` Gradle task.
- Added automated checks for the expected 75 blocks, 72 block items, 98 standalone items, and 57 sound events.
- Added parsing/validation of all active JSON resources during verification.
- Added an `M1 VERIFY` common-setup log marker with registry counts.
- Added `VERIFY_M1_BUILD.bat`, `RUN_M1_CLIENT.bat`, and an exact GO/NO-GO test path.
