# Contract System Parity Runtime Testing

Use a fresh Contract Market where practical. Existing old-format contracts should also be retained for migration tests.

- [ ] Market has 18 take-only offer slots and normal player inventory access.
- [ ] Only one offer is generated per generation cycle.
- [ ] Countdown follows 36,000 + 6,000 x occupied slots and survives save/reload.
- [ ] Market eventually produces all seven legacy weighted contract families.
- [ ] Existing old 1.20.1 collect/hunt contracts still show progress, complete and redeem normally.
- [ ] Kill Androids accepts both Rogue Android and Ranged Rogue Android kills and uses a 12-28 goal.
- [ ] Sacrifice only counts baby pig/cow/sheep/chicken kills and uses an 8-15 goal.
- [ ] Department of Agriculture selects wheat/carrot/potato and uses a 31-63 goal.
- [ ] Weapons of War counts crafted anvils and uses a 1-3 goal.
- [ ] One True Love completes from diamond ore or deepslate diamond ore while the diamond drop remains normal.
- [ ] Is It Really Me completes only after a real Matter Overdrive Transporter movement, not ordinary walking.
- [ ] Beast Belly completes after entering the actual anomaly event horizon.
- [ ] Scan-type contracts only advance on a new successful scanner result matching the target.
- [ ] If two compatible contracts are carried, one qualifying action can advance both.
- [ ] Completion produces the completion notification/sound once per contract.
- [ ] Redeeming pays every listed item reward plus XP and consumes exactly one contract.
- [ ] Full inventory drops overflow rewards safely rather than deleting them.
- [ ] Contract title/objective/progress/reward/XP tooltips remain readable.
- [ ] Compact contract HUD shows up to three carried contracts and marks completed ones distinctly.
- [ ] HUD disappears when no contracts are carried and respects Hide GUI.
- [ ] Contract progress/rewards survive logout, save/reload and moving the contract between inventory slots.
