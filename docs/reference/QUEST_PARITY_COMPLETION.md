# Quest System Parity Completion

Authoritative references: Matter Overdrive 1.7.10 0.4.2 and Matter Overdrive 1.12.2 0.7.1.0.

## Defined quest set
The 1.12 quest data plus legacy Java registrations give a finite gameplay quest set. The 1.20.1 port now represents all source-backed quest families rather than treating the Contract Market as the whole quest system.

### Contract Market
The seven weighted market contracts remain: `kill_androids`, `sacrifice`, `department_of_agriculture`, `weapons_of_war`, `one_true_love`, `is_it_really_me`, and `beast_belly`.

### Story / structure quests
- `crash_landing`: recovered from crashed-spacecraft Tritanium Crate injection. Stores the crash/crate position, asks for the modern base Security Protocol equivalent, awards 60 XP, a custom-named Communication Relay, and chains to We Must Know.
- `we_must_know`: place the relay within the recovered four-block position radius, 120 XP, 8 Emeralds.
- `gmo`: Mad Scientist House crate quest. Sequential 12-24 Carrot scans then 12-24 Potato scans. XP follows legacy `QuestLogicScanBlock.modifyXP` (10 per required scan). The house also supplies a custom Mad Scientist's Data Pad reproducing the legacy destructive Wheat/Carrot/Potato scan behavior. Reward is a custom-named Hardened Tritanium Spine; the obsolete custom attribute payload is not installed as an invalid 1.20 attribute.
- `trade_route`: cargo-ship red captain crate -> specifically named Trade Route Agreement -> Mad Scientist conversation. 180 XP. The cargo ship's red/lime crate pairing is restored lazily so already-generated ships can recover the missing quest content. Completion creates the named Agreement Copy, chains to Stem Bolts, and spawns one Failed Pig at the stored quest position with the recovered Y -2 offset.
- `stem_bolts`: read/use the specifically named Trade Route Agreement Copy. The legacy source defines no XP/item reward beyond completion.
- `to_the_power_of`: craft a Matter Overdrive Solar Panel, 120 XP, 5 Tritanium Ingots, 4 Tritanium Plates. The old BigReactors/ExtraUtilities alternatives are not hard dependencies in 1.20.1. No natural acquisition route is present in the authoritative JAR, so this remains source-defined and accessible through admin/debug quest commands rather than an invented world handout.

## Mad Scientist quests
- `puny_humans`: corrected to the real legacy objective. One Head, Chest, Arms and Legs Rogue Android part are required. Completion consumes those four parts and begins/activates Android conversion. It awards 256 XP plus the recovered battery/pill reward shape. Saves that started the earlier port's "become an Android" approximation are migrated instead of becoming impossible to finish.
- `cocktail_of_ascension`: corrected to the legacy event logic: five Creepers killed with a shovel, five Gunpowder collected, and five Red Mushrooms collected specifically in the Nether. Gunpowder/mushrooms are consumed into quest progress when picked up. Return to the junkie Mad Scientist completes the transformation event, grants 512 XP and the recovered Android pill reward shape, and replaces the scientist with the mutant outcome.

## Modern execution decisions
- Quest state remains on Contract items for transferable contract/story quests rather than recreating the old singleton-style PlayerQuestData.
- Puny Humans/Cocktail retain player-persistent state because their original logic is player/NPC transformation state rather than a portable contract.
- Ordered stages mirror their active state into existing compatibility keys, keeping old 1.20 contracts/HUD/tooltips readable.
- Quest positions/radii and exact custom item names are persistent and server-authoritative.
- Structure quest loot self-heals on first valid interaction and is marked seeded, preserving older worlds without duplicating rewards.
- Story quests that lack a source-backed natural handout are not injected into the Contract Market or arbitrary loot.
- `/matteroverdrive quest list` and `/matteroverdrive quest give <id>` provide permission-level-2 test/admin access comparable to the legacy quest command role.

## Presentation status
Functional quest/dialogue progression is restored through the Mad Scientist interaction path, HUD, Data Pad Active Contracts page, completion/start sounds, and server-authoritative actions. The old cinematic conversation camera/shot renderer is intentionally treated as presentation parity rather than quest-state parity; it can be recreated later with the planned Forge visual test harness without another quest save-format migration.
