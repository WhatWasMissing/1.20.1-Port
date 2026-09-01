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
2. Pick up Iron Ingots until its listed goal is met.

Expected:

- Progress increases while the Contract remains in the player inventory.
- It gains an enchanted glint when complete.
- Right-click the Contract Market while holding the completed contract.
- The contract is consumed and the listed reward is issued.

## Hunt contracts

1. Take the Zombie contract.
2. Kill Zombies with the Contract in the player inventory.

Expected:

- Each player kill advances progress.
- On completion, return it to the Contract Market for its reward.

## Star Map

1. Take at least one contract.
2. Open the Star Map.

Expected:

- It reports the total active contracts in the player inventory.
- It reports how many are ready to redeem.
- After redeeming a completed contract, both values update when reopening the Star Map.

## Persistence

- Save/reload with contracts in progress.
- Contract type, target, goal, progress, and reward persist.
- Market offers persist until taken; after all offers are taken, it refreshes a new set after its refresh delay.
