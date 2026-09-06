# Contract / Quest System Parity

Authoritative references: Matter Overdrive 1.7.10 0.4.2 and 1.12.2 0.7.1.0.

## Restored in the 1.20.1 contract layer

- Contract Market keeps the recovered 18 remove-only offer slots.
- Generation cadence remains 36,000 ticks + 6,000 ticks per occupied offer slot.
- The shipped seven market contracts and their weights are restored: kill_androids 100, sacrifice 100, department_of_agriculture 100, weapons_of_war 80, one_true_love 100, is_it_really_me 80, beast_belly 60.
- Recovered randomized objective ranges, XP values and reward sets are represented in contract NBT.
- Contract stacks support multiple targets, multiple rewards, quest IDs/titles, XP and flags while remaining compatible with earlier 1.20.1 contracts that only have Type/Target/Goal/Progress/Reward/RewardCount.
- Objective tracking supports collect, hunt, child-only hunt, craft, mine, scan, Transporter use and gravitational-anomaly event-horizon entry.
- Multiple compatible carried contracts may advance from one valid action, matching the legacy model more closely than the prior first-match-only implementation.
- Completed contracts keep the existing 1.20.1 return-to-market redemption flow; redemption now pays all rewards and recovered XP.
- A compact client contract HUD restores the useful active-quest presentation without introducing a second player quest database.

## Intentional modernization / compatibility decisions

- Existing 1.20.1 contracts are not migrated destructively; old keys remain readable and the first target/reward is mirrored into old keys for compatibility.
- Legacy One True Love destroyed the mined diamond drop. The port tracks the objective but preserves normal Minecraft drops.
- The 1.12 generic rogue_android_part reward is mapped to one random current split Android body part because the 1.20.1 port exposes head/chest/arms/legs as distinct items.
- Legacy upgrade metadata 4 is mapped explicitly to upgrade_range.
- Transporter and anomaly contracts observe real current-port machine/anomaly state rather than generic movement or damage when possible.
- Scanner tracking is bridged through the current scanner's persisted successful-scan telemetry, avoiding invasive changes to the scanner/backend.
- Contract data remains on the item, preserving simple multiplayer transfer and avoiding a brittle global/singleton player quest store.

## Remaining deeper quest parity

The authoritative JARs contain a broader scripted quest/dialog framework beyond market contracts: sequential multi-stage quests, placement and interaction objectives, conversation gates, hidden quest rewards/chaining, entity/sound rewards, NPC dialog cameras, active-quest management/abandon actions, and story quests such as Crash Landing, We Must Know, G.M.O., Trade Route and Stem Bolts. Those should be restored on top of the extensible contract/objective layer rather than by recreating the old global quest architecture verbatim.

## Later: local Forge visual test harness

When a local Forge client is available, add a development-only Matter Overdrive visual test harness so rendering work can be inspected systematically instead of relying on manual setup for every screen/model. Target commands should be able to open known GUIs, equip/configure known weapons, position camera/player, spawn representative structures/entities and capture screenshots plus state metadata. Also add lightweight GUI layout validation for slot/button/text bounds and overlapping hitboxes. Keep this test tooling development-only and isolated from normal gameplay/network state.
