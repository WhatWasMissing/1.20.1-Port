---
navigation:
  title: World Structures
  parent: index.md
  position: 9
  icon: matteroverdrive:tritanium_crate
---
# World Structures

Matter Overdrive world content is reconstructed from the 1.12.2 structure classes and their embedded PNG templates. New structures use modern 1.20.1 terrain placement checks, while recovered dimensions, biome intent and source-backed population rules are preserved wherever the old code is authoritative.

## Android House

The legacy template is **21 x 21** with a **-2 Y offset**. A generated house contains its recovered Matter Overdrive machine palette and **3-5 ordinary Rogue Android defenders**. The old code chooses **60% ranged / 40% melee** for those defenders and always adds one **level-3 legendary ranged Rogue Android**. The port restores that guaranteed legendary defender.

## Crashed Space Ship

The embedded template is **11 x 35** and legacy generation keeps crashed ships at least roughly **256 blocks apart**. The 1.12.2 generation hook does not spawn Android or Drone defenders. Crates use the `matteroverdrive:crashed_ship` loot table; the old table rolls once with 80% Tritanium Nugget, 10% Battery and 10% empty. Holo signs have legacy orientation/text handling, and Weapon Stations can rarely receive a generated decorated energy weapon.

## Cargo Ship

The embedded template is **58 x 23**. Legacy generation requires roughly **4096 blocks** between cargo ships and applies an additional **10% generation roll** after the distance check. Its generation hook inserts a generated contract into a designated Tritanium Crate; it does not create combat defenders.

## Underwater Base

The embedded template is **43 x 43**. The old generator requires the **deep_ocean** biome, more than 26 blocks of water depth at its validation points, and roughly **2048 blocks** between bases. Its population hook is empty: Rogue Androids and Drones are not automatically spawned by the authoritative structure class.

## Sand Pit

The embedded template is **24 x 24**, uses a **-9 Y offset**, and generates in the **desert** biome. Its authoritative generation hook is empty, so the structure itself does not create Android/Drone defenders.

## Mad Scientist House

The Mad Scientist house is a separate village-piece lineage rather than one of the five standalone image-generated building classes above. The port retains its recovered scientist/failed-creature population until that separate village generator has an equally complete source audit.

## Modern placement versus legacy templates

The current 1.20.1 port uses modern terrain suitability and entrance cleanup around the recovered structure footprints. Exact old PNG-to-block decoding remains a distinct visual/layout parity layer: the original JAR contains `android_house.png`, `crashed_ship_1.png`, `cargo_ship.png`, `underwater_base_1.png` and `sand_pit.png`, and those assets remain the authority for future exact block-for-pixel reconstruction.
