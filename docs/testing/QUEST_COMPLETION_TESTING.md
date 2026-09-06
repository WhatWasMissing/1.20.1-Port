# Quest Completion Testing

Authoritative references: Matter Overdrive 1.7.10 0.4.2 and 1.12.2 0.7.1.0.

## Contract Market regression
- [ ] Seven recovered weighted market families still generate; story quests never appear in normal market rolls.
- [ ] 18 offer slots and 36,000 + 6,000-per-occupied-slot timing remain unchanged.
- [ ] Market redemption consumes exactly one completed contract and pays all item, custom, world/entity, XP and chained quest rewards once.

## Crash Landing / We Must Know
- [ ] Interacting with the Tritanium Crate in a restored crashed spacecraft seeds Crash Landing once, including already-generated structures.
- [ ] Ordinary Tritanium Crates and cargo-ship crates do not seed Crash Landing.
- [ ] Crafting the current base Security Protocol equivalent completes Crash Landing.
- [ ] Redemption gives 60 XP, a custom-named `Communication Relay`, and We Must Know.
- [ ] We Must Know preserves the source crash position and accepts the decorative coil placement only within radius 4.
- [ ] We Must Know redemption gives 120 XP and 8 Emeralds.

## G.M.O.
- [ ] The restored Mad Scientist House crate seeds G.M.O. and `Mad Scientist's Data Pad` once, including already-generated houses.
- [ ] House detection requires the Mad Scientist + Inscriber + Decomposer signature and does not seed arbitrary crates.
- [ ] G.M.O. stage 1 randomizes 12-24 Carrot scans; stage 2 randomizes 12-24 Potato scans.
- [ ] Mad Scientist Data Pad use on Wheat/Carrots/Potatoes records the block, advances matching scan objectives, and destroys a destroyable target without dropping it.
- [ ] Normal Data Pads remain non-destructive history/guide tools.
- [ ] G.M.O. XP equals 10 times the combined randomized scan goals.
- [ ] G.M.O. redemption gives a custom-named `Hardened Tritanium Spine` once.

## Trade Route / Stem Bolts
- [ ] Existing/new cargo ships seed the red captain crate and lime quest crate exactly once.
- [ ] Red crate contains the specifically named `Trade Route Agreement`; lime crate contains Trade Route locked to the red crate position.
- [ ] Opening a different red crate does not satisfy stage 1.
- [ ] An ordinary Mk1 Isolinear Circuit does not satisfy the named Agreement stage.
- [ ] Using the named Agreement consumes one in Survival, not Creative.
- [ ] Talking to a Mad Scientist completes the conversation stage without breaking Puny Humans/Cocktail interactions.
- [ ] Redeeming Trade Route gives 180 XP, named `Trade Route Agreement Copy`, chained Stem Bolts, and one Failed Pig at stored quest position Y -2.
- [ ] Stem Bolts accepts only the named Agreement Copy and does not consume a generic Mk1 circuit.

## To the Power Of
- [ ] `/matteroverdrive quest give to_the_power_of` produces the source-defined quest for permission-level-2 testers/admins.
- [ ] Crafting one Matter Overdrive Solar Panel completes it.
- [ ] Redemption gives 120 XP, 5 Tritanium Ingots and 4 Tritanium Plates.
- [ ] No BigReactors/ExtraUtilities hard dependency is introduced.
- [ ] The quest is not silently inserted into world loot because no authoritative natural acquisition path was found.

## Puny Humans
- [ ] A non-Android player can start Puny Humans from a Mad Scientist.
- [ ] Objective requires one each of Head, Chest, Arms and Legs Rogue Android part.
- [ ] Returning with all four consumes one of each and activates/begins Android conversion.
- [ ] Completion awards 256 XP plus Battery/Android Pill rewards once.
- [ ] A save with the earlier port's active Puny Humans state and an already-Android player can migrate/finish without becoming stranded.
- [ ] New source-backed Puny Humans starts use the four-part mode rather than the old become-Android approximation.

## Cocktail of Ascension
- [ ] Only a junkie Mad Scientist after Puny Humans can start Cocktail.
- [ ] Creeper progress increases only for Creepers killed while a shovel is in the main hand.
- [ ] Picking up Gunpowder while active consumes up to five into quest progress.
- [ ] Red Mushrooms count only when picked up in the Nether and consume up to five into quest progress.
- [ ] Over-limit pickups leave excess items intact.
- [ ] Returning early reports all three counters.
- [ ] Returning at 5/5/5 spawns the Mutant Scientist outcome, marks completion, awards 512 XP and one each of the three Android Pill types, then removes the original Mad Scientist.

## Quest management / persistence
- [ ] Data Pad Active Contracts page still reports staged objective, progress, XP and redeem-ready state.
- [ ] Two-click ABANDON removes only the server-validated Contract item in that inventory slot.
- [ ] Quest positions, radii, required custom names, stage progress and NextContract survive save/reload and item transfer.
- [ ] `/matteroverdrive quest list` lists every source-defined non-market story contract supported by the port.
- [ ] `/matteroverdrive quest give <id>` rejects unknown ids without deleting/replacing any item.

## Visual checks for later
- [ ] Contract HUD remains readable at common GUI scales and does not overlap important HUD elements.
- [ ] Data Pad Active Contracts page fits four managed contracts without clipping.
- [ ] Quest start/complete messages and sounds are clear.
- [ ] Mad Scientist dialogue remains functionally clear; cinematic legacy conversation-shot presentation is tracked separately for the Forge visual harness.
