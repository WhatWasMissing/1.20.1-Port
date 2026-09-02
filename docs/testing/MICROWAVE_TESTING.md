# Microwave Runtime Testing

Use this checklist for the restored 1.20.1 Microwave machine before treating it as integrated.

## Intended behaviour

The Microwave is a food-only FE-powered furnace based on the original Matter Overdrive machine.

Base values:

- 512,000 FE internal storage.
- 1,000 FE nominal energy cost per cooking operation.
- 10-tick base cook time.
- Food-only processing using normal Minecraft smelting recipes.
- Four upgrade slots supporting Speed, Power, Power Storage and Hyper Speed upgrades.

## 1. Craft, place and open

- Craft the existing Microwave recipe in Survival.
- Place the block and confirm its existing block texture/model still renders correctly.
- Right-click it and confirm the Microwave screen opens.
- Confirm the UI shows input, energy, output and four upgrade slots.

## 2. Input rules

- Insert a raw food with a furnace recipe, such as raw beef.
- Confirm it enters the input slot.
- Try a non-food smeltable item such as iron ore/raw iron.
- Confirm the Microwave rejects it from the input slot.
- Try a non-food non-smeltable item and confirm it is rejected.

Expected: the Microwave is deliberately food-only rather than a general electric furnace.

## 3. FE and cooking

- Supply FE through the block capability/cable or place a charged compatible battery in the energy slot.
- Confirm the internal FE bar increases.
- Insert raw food with an available vanilla smelting recipe.
- Confirm cooking starts and the progress bar advances.
- Confirm the correct cooked result appears in the output slot.
- At base settings, confirm a complete cook takes roughly 10 ticks and consumes roughly 1,000 FE in total.

## 4. No-energy behaviour

- Start cooking and remove/deplete the FE source before completion.
- Confirm cooking pauses when the internal buffer cannot pay the current FE/t cost.
- Restore FE.
- Confirm cooking resumes rather than duplicating or deleting the input.

## 5. Output handling

- Cook multiple identical foods and confirm outputs stack normally.
- Fill the output slot to its stack limit.
- Confirm the Microwave stops without consuming additional input while there is no output room.
- Remove some output and confirm processing resumes.
- Put an incompatible item in the output slot through a test/editor if needed and confirm the input is not consumed.

## 6. Energy-item slot

- Insert a normal rechargeable battery with FE.
- Confirm FE transfers from the battery into the Microwave.
- Confirm the battery remains and is drained by the amount transferred.
- Confirm an empty/non-extractable energy item cannot incorrectly provide free FE.

## 7. Upgrade behaviour

### Speed

- Record base cook time and FE/t.
- Install a Speed upgrade.
- Confirm cook time changes according to the current upgrade multiplier.
- Confirm energy usage remains internally consistent with the displayed FE/t/cook duration.

### Hyper Speed

- Install Hyper Speed and confirm cooking becomes substantially faster without bypassing FE checks.

### Power

- Install a Power upgrade and confirm the displayed FE/t/total energy behaviour changes according to the current Power Usage multiplier.

### Power Storage

- Install a Power Storage upgrade.
- Confirm maximum internal FE storage increases from the 512,000 FE base.
- Remove the upgrade and confirm capacity returns to the base value without invalid energy state.

## 8. Automation capability

- Connect a compatible item transport system/hopper-style capability test.
- Confirm food can be inserted into the input.
- Confirm cooked food can be extracted from the output.
- Confirm the machine does not accept arbitrary non-food items into the input through automation.

## 9. Save/reload persistence

With food, FE, upgrades and partial cooking progress present:

- Save and quit.
- Reload the world.
- Confirm input, output, battery, upgrades and FE remain correct.
- Confirm partial cook progress remains valid and processing can continue.

## 10. Break safety

- Put items in the Microwave and install upgrades.
- Break the block.
- Confirm its inventory and installed upgrades are dropped and nothing is silently deleted.
- Confirm no duplicate drops occur.

## 11. Debug/runtime verification

- Use the `INF FE` button and confirm debug infinite energy toggles normally.
- Launch through the normal M2 test client path.
- Run `CHECK_M2_RUNTIME.bat` after closing the client.

Expected runtime marker:

- `blockEntities=17`
- `menus=14`
- `microwave=enabled`

The runtime gate should pass with no Matter Overdrive block-entity, datapack or missing-model failures.

## Pass criteria

The Microwave slice passes when:

- food-only input enforcement works both manually and through capability insertion;
- vanilla food smelting results are produced correctly;
- FE is required and consumed without free-processing exploits;
- output blocking cannot consume or delete input;
- supported upgrades have observable, coherent effects;
- inventory, FE, progress and upgrades survive save/reload;
- breaking the machine safely returns contents;
- the M2 runtime gate passes at 17 block entities / 14 menus with `microwave=enabled`.
