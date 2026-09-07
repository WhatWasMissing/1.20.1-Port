# Scientist Story Quest Overhaul

## Scope

This pass turns the Mad Scientist into a dialogue-driven quest host and connects the recovered legacy story contracts into a sequential campaign after Puny Humans.

## Implemented

- Dedicated NPC dialogue packet and client dialogue screen.
- Responsive dialogue wrapping with automatic paging, Back, Continue and Close controls.
- Puny Humans start, progress and completion dialogue moved into the dialogue UI.
- Cocktail of Ascension start, progress and completion dialogue moved into the dialogue UI.
- Sequential legacy scientist campaign:
  1. Crash Landing
  2. We Must Know
  3. G.M.O.
  4. Trade Route
  5. Self-Sealing Stem Bolts
  6. To the Power Of
- Existing legacy rewards are redeemed through the scientist campaign, including custom named/NBT rewards and the Trade Route world reward.
- Story progress persists in player persisted NBT.
- Story index is reconciled against legacy contracts already in inventory so older saves or manually recovered quest items do not rewind the chain.
- Completed and stale earlier legacy contract copies are removed on redemption to prevent repeat rewards.
- Staged contracts now expose Stage X/Y in scientist dialogue and item tooltips.
- Named interaction objectives show the required named quest item rather than only its base registry item.
- Contract completion tooltip now acknowledges either the quest giver or Contract Market as a redemption route.

## Runtime test checklist

### Dialogue UI

- Talk to a Mad Scientist and verify the dialogue box opens instead of chat-only quest text.
- Resize the window while a long dialogue is open and verify wrapping and pages remain usable.
- Verify Back and Continue page navigation and that the final page uses Close.
- Verify dialogue does not pause an integrated server.

### Puny Humans

- Start as a human and verify the quest starts through dialogue.
- Return without all four Rogue Android parts and verify each missing/ready part is reported.
- Return with Head, Chest, Arms and Legs and verify conversion, rewards and completion state.
- Test an already-converted Android path and verify it completes without consuming nonexistent parts.

### Legacy campaign

- Complete the six quests in order and verify a new contract is only issued when no current story contract is active.
- Verify staged quests show Stage X/Y and rotate objectives correctly.
- Verify Trade Route requires its named agreement item where applicable.
- Verify completed contracts are removed and cannot be redeemed twice.
- Verify rewards, XP and special rewards are granted once.
- Verify Trade Route spawns its Failed Pig world reward at the stored quest position.
- Relog between several stages and verify story index and contract progress persist.
- Test with an older/later legacy story contract already in inventory and verify the scientist resumes from that point instead of issuing an earlier duplicate.

### Cocktail of Ascension

- Use a junkie scientist after Puny Humans and verify Cocktail starts.
- Verify progress dialogue reports shovel Creeper kills, gunpowder and Nether red mushrooms.
- Complete all requirements and verify mutant transformation, rewards and completion state.

## Not claimed as runtime verified

The branch is compile/CI checked. The gameplay checklist above still requires an in-game test pass before this system should be treated as fully runtime verified.
