---
navigation:
  title: Quests and Contracts
  parent: index.md
  position: 9
  icon: matteroverdrive:contract
item_ids:
  - matteroverdrive:contract
---
# Quests and Contracts

Matter Overdrive has two related progression systems: **story quests** tied to NPCs/world structures, and **contracts** carried as items and redeemed through the Contract Market. The unified top-right tracker shows active quests and incomplete carried contracts in one place.

## Active tracker

The top-right **ACTIVE QUESTS** panel is the compact runtime view. It can show:

- quest/contract title;
- current stage label;
- current objective;
- progress text.

Contracts are already included in this unified tracker, so there should not be a second independent contract panel drawn over it. If two panels appear in the same corner, that is a UI regression rather than intended behavior.

Long objectives wrap to keep the tracker inside the GUI-scaled viewport. The tracker is hidden while another screen is open so it does not compete with machine/menus.

## Data Pad

The **Data Pad** is the deeper information tool. Use it when you need more than the compact HUD: carried contracts, stage information, scan history and related progression data can be reviewed there.

The tracker is for moment-to-moment reminders; the Data Pad is for inspection and management.

## Contracts

Contracts are carried inventory items with server-authoritative progress. A contract can contain several stages, so seeing `STAGE 3/3` does not necessarily mean it is ready to redeem: finish that stage's objective first.

Completed contracts are redeemed at a **Contract Market**. If you no longer want a carried contract, use the supported abandon/remove control rather than deleting unrelated quest state.

A useful contract flow is:

1. Acquire a contract.
2. Check its title/objective in the Data Pad or top-right tracker.
3. Complete the current stage objective.
4. Continue until the contract reports completion.
5. Return to a Contract Market and redeem it.

## Story routes

Current restored story routes include:

- **Crash Landing**;
- **We Must Know**;
- **G.M.O.**;
- **Trade Route**;
- **Stem Bolts**;
- **To the Power Of**.

These routes are tied to Matter Overdrive's world structures and NPC interactions. Structure-backed quest acquisition has self-healing behavior where possible so an older generated structure can still establish the intended progression state after relevant port updates.

## Puny Humans

The **Puny Humans** route is the Android-conversion path involving the Mad Scientist. It requires one of each Rogue Android body part:

- head;
- arm;
- leg;
- torso.

Rogue Androids drop a random body part using the restored drop model, with Looting improving the chance. The active tracker reports how many required parts are currently represented in your inventory.

Once all required parts are collected, return to the appropriate Mad Scientist interaction rather than expecting the quest to complete just by holding the items.

## Cocktail of Ascension

The **Cocktail of Ascension** route tracks several ingredient/combat requirements at the same time. The unified quest tracker reports the current counts so you can see which requirement is still incomplete without repeatedly reopening dialogue.

## Troubleshooting quest progress

If a quest seems stuck:

1. Check whether the objective text changed to a new stage.
2. Confirm required items are in your actual player inventory when the quest expects possession.
3. Return to the NPC named by the stage when an objective says to talk/return.
4. Check the Data Pad for the more detailed state.
5. If the quest originated from a structure generated before a recent update, revisit the structure/NPC and allow the self-healing acquisition logic to run.

When reporting a stuck quest, include the quest title, stage label, objective text and progress line shown by the tracker. Those four values are much more useful than only saying that the quest did not advance.
