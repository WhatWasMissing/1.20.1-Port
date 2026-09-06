# Beta 1 survival + compatibility testing

Use a fresh Survival world for the first run. Keep backups and continue the same world across Beta fixes unless a test explicitly requires fresh chunks.

## 1. Ore bootstrap

- Generate fresh Overworld chunks.
- Confirm Tritanium Ore appears between Y -32 and 64.
- Confirm Dilithium Ore appears between Y -64 and 16.
- Confirm stone, wood and gold pickaxes do not produce the ore drop.
- Confirm an iron-tier pickaxe or better produces the ore block drop.
- Furnace-smelt and blast-smelt Tritanium Ore into Tritanium Ingots.
- Furnace-smelt and blast-smelt Dilithium Ore into Dilithium Crystals.

## 2. First power

Without commands/creative items:

1. Craft Tritanium Plates.
2. Craft Machine Casing.
3. Craft Machine Hull.
4. Craft Isolinear Circuit Mk1.
5. Craft a Solar Panel.
6. Confirm daylight FE generation.
7. Craft a Battery from Tritanium, Dilithium and Redstone.
8. Craft a Charging Station.
9. Confirm the Battery charges and retains FE across save/reload.

## 3. First matter and replication

- Craft S-Magnets and Matter Containers.
- Craft a Decomposer.
- Power it from normal survival generation.
- Decompose a supported item and move/store the resulting matter.
- Craft/use the analysis and Pattern Drive path.
- Confirm a valid pattern can reach the Replicator.
- Replicate an item using survival-obtained FE + matter.
- Save/reload with partial machine progress, stored matter and Pattern Drives.

## 4. Rogue Android / Puny Humans progression

- Confirm natural Rogue Android spawning in fresh Overworld biomes.
- Kill both melee and ranged variants.
- Confirm one random HEAD/CHEST/ARMS/LEGS part can drop at the source-backed 15% base chance.
- Confirm Looting improves that chance.
- Obtain all four parts without commands and complete the Puny Humans Android-conversion route.

## 5. AE2 15.4.10

Run once with AE2 installed and once without it.

With AE2 installed:

- Attach a Storage Bus to a Tritanium Crate; insert and extract items through the ME network.
- Automate Decomposer input/output through its public item handler.
- Automate Matter Recycler input/output.
- Automate Inscriber primary/secondary inputs and output.
- Extract Replicator output without altering/duplicating its Matter Overdrive network task queue.
- Insert/remove Pattern Drives from Pattern Storage through capability automation.
- Confirm machine upgrade slots are not exposed as ordinary public automation inventory.
- Chunk unload/reload automated machines and verify no item duplication or loss.
- Confirm AE2 and Matter Overdrive network routing remain separate.
- Confirm no implicit AE-to-FE conversion occurs.

Without AE2:

- Matter Overdrive must load normally and all normal survival systems remain available.

## 6. GuideME 20.1.15

With GuideME installed:

- Open the Data Pad.
- Confirm `GuideME Manual` is visible.
- Open it and confirm `matteroverdrive:guide` starts on the Matter Overdrive index.
- Navigate every section and verify item icons/links render.
- Close/reopen and ensure the Data Pad still handles contracts and scan history normally.

Without GuideME:

- `GuideME Manual` must not be shown.
- The existing compact Data Pad guide pages, contracts and scan history must continue to work.
- Matter Overdrive must load without missing-class errors.

## 7. Severity during Beta

- BLOCKER: progression cannot continue, crash, save corruption, required resource unobtainable.
- MAJOR: a system is unusable, duplicates/loses resources, or has a severe exploit.
- BALANCE: functional but survival cost/output/pacing is unreasonable.
- VISUAL/QOL: rendering, layout, wording or interaction polish.
