# Survival Progression Runtime Testing

Use this checklist for the `feature/survival-progression` changes before treating the new survival route as integrated.

## Preconditions

- Use a new world or travel into completely fresh chunks. Existing generated chunks will not gain the new ores retroactively.
- Keep recipe viewers/JEI available if installed, but also verify the recipes in a normal crafting table/furnace.
- Test in Survival unless a step explicitly asks for Creative/Spectator inspection.

## 1. Datapack/worldgen load

- Start the client/server and create or load the test world.
- Confirm there are no Matter Overdrive datapack errors mentioning configured features, placed features, biome modifiers, Tritanium Ore or Dilithium Ore.
- Confirm world creation/loading completes normally.

Expected: both ore features register without datapack decode errors.

## 2. Tritanium Ore generation

Configured target:

- Overworld biomes.
- Y -32 through 64.
- 10 placement attempts per chunk.
- Vein size up to 6.

Test:

- Explore fresh chunks across several Overworld biomes.
- Use normal mining first; Spectator/Creative inspection is acceptable afterward to verify distribution.
- Confirm Tritanium Ore appears naturally inside stone/deepslate terrain and is not restricted to a single biome.
- Confirm it does not appear obviously above Y 64 or below Y -32.

## 3. Dilithium Ore generation

Configured target:

- Overworld biomes.
- Y -64 through 16.
- 6 placement attempts per chunk.
- Vein size up to 5.

Test:

- Explore fresh deep underground chunks.
- Confirm Dilithium Ore appears naturally and is visibly rarer/deeper than Tritanium.
- Confirm it does not appear obviously above Y 16.

## 4. Ore drops and processing

For Tritanium:

- Mine Tritanium Ore and confirm the block/drop can be collected.
- Smelt it in a furnace and confirm the result is `matteroverdrive:tritanium_ingot`.
- Blast it in a blast furnace and confirm the same result is produced faster.

For Dilithium:

- Mine Dilithium Ore and confirm the block/drop can be collected.
- Smelt it in a furnace and confirm the result is `matteroverdrive:dilithium_crystal`.
- Blast it in a blast furnace and confirm the same result is produced faster.

## 5. Blue Android Pill survival route

Crafting pattern:

```text
T D T
G E G
T D T
```

- `T` = Tritanium Ingot
- `D` = Dilithium Crystal
- `G` = Gold Ingot
- `E` = Ender Pearl

Test:

- Craft the Blue Pill in Survival.
- Use it on a non-Android player.
- Confirm Android conversion occurs and the existing Android HUD/FE behaviour starts normally.

## 6. Red Android Pill survival route

Crafting pattern:

```text
R F R
D T D
R F R
```

- `R` = Redstone
- `F` = Fermented Spider Eye
- `D` = Dilithium Crystal
- `T` = Tritanium Ingot

Test:

- Craft the Red Pill in Survival.
- Use it while converted.
- Confirm Android state is removed and installed bionic parts are returned as expected.

## 7. Yellow Android Pill survival route

Crafting pattern:

```text
G R G
D T D
G R G
```

- `G` = Glowstone Dust
- `R` = Redstone
- `D` = Dilithium Crystal
- `T` = Tritanium Ingot

Test:

- Drain Android FE below full.
- Craft and use the Yellow Pill.
- Confirm Android FE increases according to the existing Yellow Pill behaviour without corrupting state or exceeding the intended capacity.

## 8. Save/reload regression

- Save and quit after finding both ores and using at least one Android pill.
- Reload the world.
- Confirm generated ore blocks remain correct.
- Confirm Android state/FE persists according to the existing Android rules.
- Travel into another fresh chunk and confirm ore generation continues.

## Pass criteria

This slice passes when:

- no datapack/worldgen decode errors occur;
- both ores generate in fresh Overworld chunks within their intended vertical bands;
- both ores process into their intended resources;
- all three Android pills are craftable in Survival and retain their existing behaviours;
- save/reload does not introduce regressions.

The Android pill recipes are intentionally temporary progression bridges until the Mad Scientist/quest route is implemented.