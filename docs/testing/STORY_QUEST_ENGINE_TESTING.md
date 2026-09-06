# Story / Quest Engine Testing

Authoritative references: Matter Overdrive 1.7.10 0.4.2 and 1.12.2 0.7.1.0.

This checklist covers the deeper quest-engine parity work layered on top of the self-contained 1.20.1 ContractItem format. Story definitions are currently dormant unless explicitly handed out by a restored acquisition/dialog path; they are not added to normal Contract Market generation.

## Objective hooks
- [ ] `place` objectives advance once when the matching block is placed by the player.
- [ ] `block_interact` objectives advance once for a matching right-clicked block.
- [ ] `item_interact` objectives advance once for a matching right-clicked item.
- [ ] `item_interact_consume` objectives consume exactly one matching used item in Survival and do not consume in Creative.
- [ ] `conversation` objectives advance from the matching server-authoritative NPC interaction.
- [ ] Existing collect/hunt/craft/mine/scan/transport/anomaly objectives still advance exactly as before.
- [ ] One valid action may advance multiple compatible carried contracts, but never a completed contract.

## Ordered stages
- [ ] A staged contract exposes only its active stage through the existing Type/Target/Goal/Progress compatibility keys.
- [ ] Completing a non-final stage rotates to the next stage instead of marking the whole contract complete.
- [ ] Active stage index, all stage progress and final completion persist through save/reload and inventory transfer.
- [ ] Old non-staged contracts remain unchanged and redeem normally.
- [ ] HUD/tooltips continue to report the currently active objective because active-stage state is mirrored into the existing contract keys.

## Recovered dormant story definitions
- [ ] `crash_landing` maps the old base Security Protocol craft objective to the current `security_protocol_empty` item, gives 60 XP and a `decorative.coils` relay reward.
- [ ] Redeeming Crash Landing creates the chained `we_must_know` contract without inserting either quest into normal market generation.
- [ ] `we_must_know` requires placing `decorative.coils`, gives 120 XP and 8 emeralds.
- [ ] Position-locking for We Must Know remains intentionally inactive until the original crash-site acquisition path can supply/copy a quest position.
- [ ] `gmo` is sequential: scan carrots, then potatoes.
- [ ] Each G.M.O. stage randomizes 12-24 required scans and total XP equals 10 XP per required scan across both stages.
- [ ] G.M.O. reward is a Tritanium Spine; legacy custom attribute/name decoration is not yet recreated.
- [ ] `trade_route` is sequential and worth 180 XP: open the captain Tritanium Crate -> read/use the Mk1 Isolinear Circuit agreement -> talk to a Mad Scientist.
- [ ] Using the Trade Route agreement consumes one circuit in Survival.
- [ ] Talking to the Mad Scientist advances the conversation stage without breaking Puny Humans/Cocktail interactions.
- [ ] Trade Route is not inserted into Contract Market generation.
- [ ] Legacy cargo-ship acquisition is documented: a red Tritanium Crate is the captain chest containing the named `Trade Route Agreement`; a lime Tritanium Crate contains the quest contract and its first-stage position points at the red crate. Worldgen activation remains pending a dedicated structure patch.

## Active quest presentation / management
- [ ] Contract HUD still caps itself to three carried contracts and does not overlap normal hotbar/status UI at common GUI scales.
- [ ] Staged contracts show `Stage n/N`, the current objective and current-stage progress.
- [ ] Completed contracts show `READY TO REDEEM`.
- [ ] Data Pad includes an Active Contracts page before Scan History.
- [ ] Data Pad Active Contracts lists up to four manageable carried contracts, active stage/objective, progress and XP without creating a second quest database.
- [ ] If more than four contracts are carried, the page reports the additional count rather than hiding their existence.
- [ ] First ABANDON click changes only that row to `ABANDON?`; confirmation expires after roughly three seconds.
- [ ] Second confirmation sends the real inventory slot to the server and removes only a Contract item still occupying that slot.
- [ ] Moving/replacing the item before server handling cannot delete an unrelated item.
- [ ] Data Pad page numbering/navigation remains correct across all eight pages.

## Regression
- [ ] Contract Market still generates only its recovered seven weighted market families.
- [ ] Market 18-slot capacity/cadence remains unchanged.
- [ ] Contract redemption still pays all item rewards + XP once and consumes exactly one completed contract.
- [ ] Story chaining does not duplicate follow-up contracts if the player inventory is full; overflow drops once.
- [ ] No quest hook advances from another player's placement/interactions.
