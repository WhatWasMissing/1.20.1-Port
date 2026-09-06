# Beta 1 Survival + AE2 + GuideME handoff

## Branch

`testing/main`

This handoff covers the first Beta survival-hardening audit plus optional Applied Energistics 2 / GuideME integration work. Re-fetch the exact branch head before continuing.

## Survival blockers fixed

- Added furnace + blast-furnace processing for Tritanium Ore -> Tritanium Ingot.
- Added furnace + blast-furnace processing for Dilithium Ore -> Dilithium Crystal.
- Added missing survival recipes for Battery, S-Magnet and HC Battery.
- Added `minecraft:mineable/pickaxe` tags for Tritanium/Dilithium ores and blocks.
- Added iron-tier requirements for Tritanium and Dilithium ores.
- Verified `ModBlocks` applies `requiresCorrectToolForDrops()` to the ores.
- Verified both ores have block loot tables.
- Verified configured + placed features exist and `overworld_ores.json` injects both into `#minecraft:is_overworld` at `underground_ores`.
- Verified the first Solar Panel dependency chain is reachable from vanilla resources + smelted Tritanium.
- Verified Battery -> Charging Station and S-Magnet -> Matter Container -> Decomposer bootstrap chains are now reachable.
- Found and fixed a hard Android progression blocker: melee and ranged Rogue Androids had no entity loot table. Both now have a source-backed 15% base chance, +10% per Looting level, to drop one random HEAD/CHEST/ARMS/LEGS part.

## AE2 15.4.10

The authoritative MO 1.7.10/1.12.2 JARs contain no direct `appeng` API module. The restored compatibility model is capability-level interoperability rather than merging MO and ME networks.

Audited public Forge capabilities include:
- Tritanium Crate: 54-slot `IItemHandler`.
- Decomposer: item, FE and matter capabilities.
- Matter Recycler: item + FE.
- Replicator: item + FE + matter; MO network task state remains internal.
- Pattern Storage: Pattern Drive/energy-item inventory + FE.
- Inscriber: processing inventory + FE.

Upgrade inventories remain separate/private. AE2 Storage/Import/Export style automation should therefore interact through public handlers without a Matter Overdrive hard dependency on AE2. No AE <-> FE conversion was added. Router/Switch networks remain distinct from ME networks.

`mods.toml` declares AE2 15.4.10+ as an optional AFTER dependency.

## GuideME 20.1.15

The exact attached GuideME JAR was inspected with `javap`.

- `Guide.builder(ResourceLocation).build()` registers `matteroverdrive:guide`.
- default resources resolve to `assets/matteroverdrive/guides/matteroverdrive/guide`.
- opening uses `Guides.getById`, `Guide.getStartPage`, `PageAnchor.page`, and `GuideScreen.openNew`.
- bridge uses reflection so MO still loads with GuideME absent.
- Data Pad now shows a `GuideME Manual` button only when GuideME is installed.
- Data Pad scan history + contract controls remain intact.
- compact built-in pages remain the no-GuideME fallback.

GuideME pages now cover:
- index
- survival
- matter
- power
- Android
- fusion reactor
- weapons
- Matter Network
- AE2
- quests/contracts
- Star Map
- Dimensional Pylon
- transporter/security

`mods.toml` declares GuideME 20.1.15-20.x as an optional AFTER dependency.

## Docs/testing

- `docs/reference/AE2_GUIDEME_INTEGRATION.md`
- `docs/testing/BETA1_SURVIVAL_COMPAT_TESTING.md`
- this handoff

## Still to verify in game

1. Fresh-world ore frequency and actual iron-pickaxe drops.
2. Full fresh Survival chain through Replicator.
3. Rogue Android part drop pacing and all four-part completion.
4. AE2 Storage Bus/import/export behavior on every audited machine, including chunk unload/reload and no duplication.
5. GuideME rendering/navigation with the exact attached 20.1.15 JAR.
6. Matter Overdrive startup with AE2 absent, GuideME absent, each individually present, and both present.

## Next recommended work

- CI-gate the exact head and repair any Java/resource verifier errors.
- Continue recipe reachability from Replicator into higher-tier machines/upgrades/weapons/reactor.
- Verify quest-structure acquisition frequencies during the survival run.
- After the survival/compat gate is clean, cut the first named Beta testing JAR and update display/mod version together.
