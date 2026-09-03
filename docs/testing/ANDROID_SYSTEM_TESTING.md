# Android System and Weapon HUD Test Checklist

Branch: `testing/main`

## Android conversion and energy

- [ ] Use a Blue Android Pill in Survival. Confirm conversion activates at 25,000 / 100,000 Android FE.
- [ ] Open an Android Station. Confirm it reports Android status, energy, installed parts, station FE, and transfer rate for the player who opened it.
- [ ] Power the Android Station from Fusion Reactor IO/cables. While standing within four blocks, confirm Android FE increases gradually.
- [ ] Move beyond four blocks. Confirm Android FE stops rising.
- [ ] Disconnect its cable/source while charging. Confirm its 2,000 FE hand-off buffer empties quickly and Android FE stops rising.
- [ ] Put two converted players within four blocks of the same powered station. Confirm its 2,000 FE/t budget is shared between them rather than the most recent GUI viewer becoming the only charge target.
- [ ] Have both players open the station at the same time. Confirm each GUI continues to show that player's own Android energy and installed parts.
- [ ] Confirm the lower-left Android Core HUD appears after conversion and tracks Android FE live.
- [ ] Hold a charged normal or HC Battery in either hand and sneak. Confirm it transfers at most 1,024 FE/t into the Android core, drains only real stored battery FE and leaves the battery item in place.
- [ ] Repeat with an empty battery and while not sneaking. Confirm no Android FE is created.
- [ ] Drain Android FE to zero. Confirm the HUD reports `CORE OFFLINE`, movement speed is reduced by 50%, and normal speed returns immediately after receiving FE or using a Red Pill.
- [ ] Use a Yellow Android Pill while converted. Confirm it adds up to 25,000 FE without exceeding capacity.
- [ ] Use a Red Android Pill. Confirm conversion ends and all installed bionic parts return to the inventory/drop safely.
- [ ] Relog and die/respawn. Confirm active conversion, energy, and parts persist.

## Android progression foundation

- [ ] Convert with a Blue Pill and confirm the HUD starts at Level 1 with 0 XP.
- [ ] Install a new bionic part. Confirm XP increases by 50 and persists after relog/death.
- [ ] Use Sonic Shockwave or Ender Teleport successfully. Confirm XP increases by 25; failed/cooldown/insufficient-FE actions grant no XP.
- [ ] Earn enough XP to cross a 1,000 XP level boundary. Confirm the HUD advances one level and resets the per-level progress display; confirm Level 10 caps XP.
- [ ] Confirm XP is player-specific on a dedicated server and is retained when using a Red Pill (deactivation removes abilities/energy but not progression).

## Bionic parts and abilities

- [ ] While converted, hold each Rogue Android Part and right-click an Android Station to install it.
- [ ] Confirm a second copy of the same part is rejected without consuming it.
- [ ] Head: night vision while Android FE is available.
- [ ] Chest: Resistance lowers incoming damage while Android FE is available. Confirm damage is not reduced a second time by an additional hidden 25% multiplier.
- [ ] Arms: direct melee attacks gain 3 damage for exactly 80 Android FE. Confirm arrows, other projectiles and Sonic Shockwave do not receive the melee bonus or its extra FE cost.
- [ ] Legs: increased movement speed while Android FE is available.
- [ ] Leave less FE than a passive or active action requires. Confirm the failed action does not consume the remaining partial FE.
- [ ] Drain Android FE to zero and confirm abilities stop rather than becoming free.

## Active Android abilities

- [ ] Confirm the controls menu lists `Cycle Android Ability` (default `V`) and `Activate Android Ability` (default `B`) and that both can be rebound.
- [ ] Install only one body part at a time. Confirm cycling skips abilities whose required part is not installed.
- [ ] Confirm the HUD shows the selected ability and remaining cooldown. After enabling Cloak or Force Field, cycle away and confirm the HUD still reports the sustained effect as on.
- [ ] Head / Cloak: toggle it on and confirm invisibility consumes exactly 128 FE/t, then disables when FE is insufficient. Toggle it off and confirm the effect expires without removing unrelated potion state.
- [ ] Chest / Force Field: toggle it on and confirm the actionbar, electric pulse and HUD all report activation, then confirm 32 FE/t idle use. Take controlled damage and verify the field reduces final post-armour/effect damage by up to 50% at 64 FE per absorbed damage point, produces an impact pulse and grants no free absorption.
- [ ] Arms / Sonic Shockwave: confirm it costs exactly 4,096 FE regardless of target count, deals its own six damage, knocks back non-allied living targets in a five-block radius and enforces a five-second cooldown.
- [ ] Legs / Ender Teleport: in open space, test level, upward and downward aim. Confirm it costs 4,096 FE, moves the player's feet along the matching eye-level view ray by up to eight blocks, emits portal feedback and enforces a three-second cooldown.
- [ ] Aim Teleport into a wall, outside the world border and where the player bounding box cannot fit. Confirm it chooses a safe nearer destination on that same view ray or refuses with an actionbar reason without consuming FE/cooldown.
- [ ] Spam activation packets/keys during cooldown and with insufficient FE. Confirm the server refuses them and values never go negative.
- [ ] Relog and die/respawn with Cloak/Force Field active and one-shot abilities cooling down. Confirm state remains coherent and no duplicate effect or free reset occurs.
- [ ] Use a Red Pill while active. Confirm toggles, selection/cooldowns and installed parts are cleared/returned safely.
- [ ] Repeat on a dedicated server with two Android players and confirm one player's ability state never changes the other's.

## Rogue Android Spawner

- [ ] Power an Android Spawner from the reactor/cable network.
- [ ] After it stores 20,000 FE, confirm it creates a named Rogue Android (Husk) when no previous Rogue Android is nearby.
- [ ] Kill the Rogue Android and confirm it drops one random bionic part.
- [ ] Confirm the spawner does not repeatedly spawn while a tagged Rogue Android is within its local range.
- [ ] Obstruct the block above the spawner but leave one of its nearby fallback positions clear. Confirm it chooses a collision-free position.
- [ ] Obstruct all candidate positions. Confirm spawn failure consumes no FE, creates no entity inside blocks and retries later without rapid per-tick spawning.

## Weapon HUD

- [ ] Hold each energy weapon in the main hand. Confirm a compact HUD appears at the lower-right.
- [ ] Confirm the charge bar/value changes while firing/reloading/charging.
- [ ] Confirm heat value and bar rise with firing and cool down normally.
- [ ] Confirm the HUD clearly shows `OVERHEATED` and the weapon cannot fire while overheated.
- [ ] Switch to a non-energy item or hide the GUI. Confirm the HUD disappears.
