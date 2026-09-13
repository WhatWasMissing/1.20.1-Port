# JEI Integration

Matter Overdrive supports **Just Enough Items (JEI)** as an optional client integration on Forge 1.20.1.

## Supported JEI line

- Minecraft: 1.20.1
- Forge: 47.4.10+
- JEI: 15.49.0.200 (15.x)

## What works

JEI automatically discovers all standard Matter Overdrive JSON crafting, shapeless, smelting and blasting recipes. No duplicate recipe registration is required for those recipes.

Matter Overdrive also registers a JEI plugin that adds information pages for major custom machines and systems whose processing logic is implemented in Java rather than Minecraft recipe JSON. These pages explain the role of the Decomposer, Recycler, Matter Analyzer, Inscriber, Pattern Storage, Pattern Monitor, Replicator, Charging Station, Solar Panel, Fusion Reactor, Stabilizer, Transporter, Weapon Station and Matter Network blocks.

## Optional dependency behavior

JEI is **not required** to run Matter Overdrive. The production JAR does not bundle JEI. The build compiles against the public JEI API and includes JEI only in the development runtime.

## Testing

1. Launch Matter Overdrive without JEI and verify normal startup.
2. Launch with JEI 15.49.0.200.
3. Search `@matteroverdrive` and verify Matter Overdrive items appear.
4. Open recipe uses/recipes for several craftable Matter Overdrive items and confirm JSON recipes appear.
5. Open uses/recipes for Decomposer, Replicator, Fusion Reactor Controller, Transporter and Network Router and confirm their JEI information pages appear.
6. Confirm no duplicate vanilla crafting or cooking recipes are registered.
