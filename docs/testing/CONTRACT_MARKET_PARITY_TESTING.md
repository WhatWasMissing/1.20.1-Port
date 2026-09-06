# Contract Market Parity Testing

Target branch: `testing/main`
Build identity: Alpha Version 3
Legacy references: Matter Overdrive 1.7.10 0.4.2 and 1.12.2 0.7.1.0 only.

## Restored in this pass

- Market capacity is 18 real contract slots, matching the authoritative 1.12 machine inventory.
- Market slots are remove-only. Players may take contracts normally or shift-click them into their inventory, but cannot insert arbitrary items/contracts back into the market.
- The market generates one contract per cycle rather than replacing a batch of offers.
- Generation delay follows the recovered 1.12 rule: 36,000 ticks plus 6,000 ticks for each occupied market slot after generation.
- The GUI exposes synchronized occupied/free slot counts and the live server countdown until the next generation cycle.
- Existing three-slot 1.20.1 saves migrate their offers into the 18-slot inventory and migrate the old `RefreshAt` timestamp into the new generation timer.
- The recovered legacy 100/80/60 weighted contract pool shape is retained. The current quest payloads deliberately use only objective types already implemented by the 1.20.1 ContractItem/ContractEvents backend: collection, Rogue Android hunting and hostile-mob hunting.

The legacy location/dungeon quest engine is NOT claimed as restored by this pass. It remains a broader quest-framework parity gap.

## Runtime checklist

1. Place a fresh Contract Market and open it. Confirm 18 contract slots are visible as a 6x3 board and all correspond to real slots.
2. Confirm one contract appears when the initial generation cycle runs; the market must not fill all slots at once.
3. Take a contract by normal click. Confirm it moves into player inventory and the market slot becomes empty.
4. Shift-click a market contract. Confirm it enters player inventory.
5. Shift-click any player item while the market is open. Confirm it does NOT enter a market slot.
6. Confirm the status panel's `Offers`, `Free`, and `Next` values update from real server state.
7. After a generated contract, confirm the countdown is approximately 30 minutes plus 5 minutes per occupied slot (36,000 + 6,000 * occupied ticks).
8. Take several offers over multiple generation cycles and confirm new contracts use empty market slots rather than replacing existing offers.
9. Fill all 18 slots over time. Confirm the machine remains stable and schedules another cycle rather than overwriting an existing contract.
10. Save and quit with contracts in the market. Reload and confirm the exact offers and next-generation countdown persist.
11. Migration test: load a world made with the previous three-slot market implementation. Confirm existing offers survive in slots 0-2 and the market now has 18 slots.
12. Take a collection contract, collect its target item, and confirm progress increments only for actual pickups.
13. Take a Rogue Android hunt contract, kill a `matteroverdrive:rogue_android`, and confirm progress increments.
14. Complete a contract and use it on the Contract Market. Confirm the reward is issued and one contract item is consumed.
15. Confirm incomplete contracts are not redeemed.
16. Open the market from two clients if multiplayer testing is available. Confirm offer removal/countdown changes synchronize without duplication.

## Visual checks for later

- 256x220 market frame fits at normal and reduced GUI scale.
- Contract items do not overlap status text.
- Player inventory/hotbar slots line up with their rendered slot backgrounds.
- Long countdown strings remain inside the status panel.
- Tooltips are not clipped by the wider operator panel.
