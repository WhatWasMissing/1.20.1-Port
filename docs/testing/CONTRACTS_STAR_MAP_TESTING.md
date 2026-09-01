# Contract Market and Star Map Test Checklist

Branch: `testing/main`

## Contract Market

1. Place and open a Contract Market.

Expected:

- Three contract offers appear.
- Taking an offer adds a single Contract item to the inventory.
- The contract tooltip shows its objective, progress, and reward.

## Collect contracts

1. Take the iron-ingot contract.
2. Keep the Contract item anywhere in the normal player inventory or hotbar.
3. Pick up a single Iron Ingot from the ground.
4. Re-check the tooltip immediately without moving the Contract item.
5. Pick up a stack of Iron Ingots.
6. Repeat once with an almost-full inventory so only part of a ground stack can be collected.

Expected:

- Progress updates immediately on the client after each successful pickup; moving the Contract item or reopening the inventory must not be required to refresh it.
- Progress increases by the exact amount actually picked up.
- A failed/cancelled pickup or items left on the ground because the inventory is full do not count.
- If multiple identical collect contracts exist, a pickup advances only one matching contract rather than multiplying progress/rewards across every copy.
- It gains an enchanted glint when complete.
- Right-click the Contract Market while holding the completed contract.
- The contract is consumed and the listed reward is issued.

## Hunt contracts

1. Take the Zombie contract.
2. Kill one Zombie with the Contract in the player inventory.
3. Re-check the tooltip immediately without moving the Contract item.
4. Continue until the contract completes.

Expected:

- Each player kill advances one matching contract by one.
- Progress updates immediately on the client after the kill.
- Carrying duplicate matching hunt contracts must not make one kill progress every copy.
- Killing a non-target entity does not change progress.
- On completion, return it to the Contract Market for its reward.

## Market refresh timing

1. Leave at least one offer sitting in the market for longer than 1,200 ticks (60 seconds).
2. Take the final remaining offer.

Expected:

- The market stays empty immediately after the final offer is taken.
- A new offer set appears only after another 1,200 ticks from the time the final offer was taken.
- Opening the empty GUI early must not bypass that delay.

## Star Map

1. Take at least one contract.
2. Open the Star Map.

Expected:

- It reports the total active contracts in the player inventory.
- It reports how many are ready to redeem.
- After completing a contract, the ready-to-redeem count updates correctly.
- After redeeming a completed contract, both values update when reopening the Star Map.
- Have two players open the same Star Map simultaneously with different contract counts. Each GUI must continue displaying its own player's values.

## Persistence

- Save/reload with contracts in progress.
- Contract type, target, goal, progress, and reward persist.
- Market offers persist until taken.
- If all offers were taken before saving, the remaining refresh delay persists rather than immediately refilling on load/open.
