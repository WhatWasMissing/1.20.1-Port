# Facility Restoration / Infrastructure Runtime Test Plan

Use a **fresh test world or unexplored chunks** on `testing/tech-overhaul`. Existing facilities are not retrofitted.

## 1. Worldgen safety

- Generate/locate all six native technology facility families.
- Cross chunk borders repeatedly while the site generates. World creation and exploration must not hang.
- Save/reload while standing in each facility. No missing-piece or structure deserialization error should appear.
- Confirm service spines, gates, ladders and the Black Site vault stair do not write outside their owning structure pieces into unrelated terrain.

## 2. Restoration loop

For each facility, find a Facility Network Controller. Relay and Fusion sites should have an additional accessible recovery terminal in the core.

- Sneak-use before starting: objective should report **restore emergency power**.
- With no stored FE and empty hand, normal use must refuse progression.
- Connect powered network storage and retry: stage should advance without consuming an item.
- In a second facility, bootstrap with Battery / HC Battery / Energy Pack in Survival and confirm exactly one item is consumed.
- Install an Isolinear Circuit Mk1 or better. Exactly one circuit should be consumed in Survival and secure access should release.
- Save/reload before and after every stage; progress must persist.
- Bring the wrong family dossier: no completion.
- Bring the matching dossier: facility becomes restored, dossier remains in hand, and the state survives reload.
- Verify scientist quest/research stages do not change.

## 3. Security Doors

- Before control repair, generated doors must remain closed even if powered by redstone or manually used.
- After control repair, manual use and redstone should open/close the generated door.
- Player-place a Security Door outside any generated facility: it should behave as an ordinary use/redstone shutter without restoration gating.
- Confirm all three Android Bunker layout variants gate the armoury approach rather than an unrelated corridor.

## 4. Structure-specific Android security

Use Survival and non-Peaceful difficulty.

- Manufacturing: spawned names should use **Assembly Defender** and movement should feel slightly faster.
- Refinery: **Malfunctioning Refinery Android**; finite reserve remains 2 before damaged-room reduction.
- Relay: **Relay Sentry** with ranged-heavy composition and increased follow range.
- Bunker: **Command Guard** with reserve 3 and visible durability increase from armour.
- Fusion: **Containment Sentry** with higher durability/knockback resistance.
- Black Site: **Black Site Warden**, reserve 4, ranged-heavy; the final reserve deployment should be at least level 3.
- Save/reload deployed defenders and confirm their facility role/name/stats persist.
- Fully restore a site before its reserve is exhausted. Remaining reserve must drop to zero; already spawned defenders must remain.
- Player-built Android Spawners must continue using FE and normal squad controls.

## 5. Traversal

- Refinery excavation shaft can be climbed from bottom to top using the generated ladder.
- Bunker entrance shaft has a continuous usable ladder route.
- Black Site entrance shaft has a continuous usable ladder route.
- Black Site vault is reachable from the approach corridor via the generated six-block stair without mining or placing blocks.
- Walk every service catwalk; rail collision should prevent accidental side falls where rails are present.

## 6. Industrial blocks

Craft and place all six new blocks. Verify model, collision and drops for:

- Industrial Catwalk
- Industrial Railing
- Cable Tray
- Warning Light
- Damaged Panel
- Security Door

Break each with a pickaxe in Survival and confirm it drops itself. Check Warning Light emits light. Rotate directional blocks through all horizontal facings and confirm models/collision rotate consistently.

## 7. Regression

- Re-run generated facility loot/dossier tests from `STRUCTURE_EXPANSION_TEST_PLAN.md`.
- Verify generated crate loot still resolves once and never refills.
- Verify finite security reserve persistence and Peaceful suppression.
- Verify the six legacy structure families and compact anomaly Features still generate.
- Confirm the retired Star Map has not returned to active registration, guides or worldgen.
