# Facility Restoration / Infrastructure Runtime Test Plan

Use `docs/testing/TO_TEST.md` as the release checklist. For this deferred plan, use a fresh world only to confirm that MO-authored placement is absent, then use a copy of an older save containing already-generated facilities for compatibility checks. Existing facilities are not retrofitted.

Before launching Minecraft, run:

```text
python scripts/facility_layout_lab.py --check-only
python scripts/validate_structure_expansion.py
python scripts/validate_lore_events.py
```

Both must pass. The layout lab should report **18 layouts**.

## 1. Placement policy and compatibility safety

- Confirm a fresh world does **not** naturally place the six native facility families, compact sites or Frontier sites.
- Confirm vanilla structures and natural Gravitational Anomalies remain available.
- In a copy of an older save, load each previously generated facility across chunk borders. World loading and exploration must not hang.
- Save/reload while standing in each facility. No missing-piece or structure deserialization error should appear.
- Confirm service spines, gates, ladders, exterior approaches and the Black Site vault stair do not write outside their owning structure pieces into unrelated terrain.
- Repeat at least one facility with its structure start close to a chunk corner.

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
- Power **only the bottom**, **only the middle**, and **only the top** segment of a generated three-high shutter. In every case the entire vertical column must open together.
- Remove that power. The entire column must close together; no segment may oscillate or disagree with the others.
- Manually open an unpowered shutter, then place/break an unrelated adjacent block. The shutter must remain manually open rather than treating the unrelated neighbour update as a redstone-off transition.
- While a shutter is actively powered, manual use must not leave it closed against the active signal.
- Player-place a Security Door outside any generated facility: it should behave as an ordinary use/redstone shutter without restoration gating.
- Confirm all three Android Bunker layout variants gate the armoury approach rather than an unrelated corridor.
- Plant layout 1 must gate the **west Shipping approach**; layout 0/2 must still gate their intended Shipping route.

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

## 5. Mandatory traversal regression matrix

No test below may require mining, block placement, flight or commands after arriving at the entrance.

### Synthetic Manufacturing Plant

- Layout 0: entrance -> core -> both side wings -> Shipping.
- Layout 1: east entrance actually opens into its connecting corridor; core -> fabrication -> assembly -> Shipping remains continuous.
- Layout 2: entrance -> core -> **both offset side wings** -> Shipping. This layout previously had disconnected offset corridors and is a priority regression check.
- Climb onto the service spine using its generated access ladder/landing and walk its full usable length.

### Matter Refinery

- Walk from the core into the excavation wing and back out without jumping a two-block height difference.
- Walk the generated lowered transition in both directions.
- Descend to the excavation shaft bottom and climb back to the top using the generated ladder.
- Confirm the ladder has a solid bottom landing rather than terminating over cleared shaft air.
- Reach and walk the service gantry/spine via its dedicated access route.

### Quantum Relay Station

- Recovery terminal remains reachable before secure access is restored.
- Reach the elevated service spine using its access ladder.
- Layout 2: observation bridge and service spine share a usable deck level; there must not be a one-block vertical collision/duplicate deck.

### Android Command Bunker

- Exterior three-step approach reaches the entrance without placing blocks.
- Descend the full entrance ladder.
- At the bottom, step sideways through the generated landing into the Security Checkpoint. The ladder backing wall must not trap the player.
- Repeat for all three layouts and verify the armoury gate corresponds to that layout's armoury approach.

### Fusion Research Complex

- Exterior three-step approach reaches the entrance.
- Recovery terminal remains available before secure access restoration.
- Service wing can be entered and exited through the two-level transition without a one-way drop.
- Reach the service spine using its dedicated ladder.
- Walk directly between the service spine and observation bridge over the explicit link.
- Repeat layout 2 where the observation bridge is on the opposite Z side.

### Black Site

- Surface hatch is visible/reachable near terrain level; the entrance must no longer terminate approximately four blocks underground with no exterior continuation.
- Descend the full entrance ladder and use the side landing into Black Security. The backing wall must not trap the player.
- Reach the vault through the generated **three-wide** six-block stair without mining or placing blocks.
- Layout 2: walk from the vault level into the additional lower laboratory through the new same-level corridor.

## 6. Industrial blocks

Craft and place all six new blocks. Verify model, collision and drops for:

- Industrial Catwalk
- Industrial Railing
- Cable Tray
- Warning Light
- Damaged Panel
- Security Door

Break each with a pickaxe in Survival and confirm it drops itself. Check Warning Light emits light. Rotate directional blocks through all horizontal facings and confirm models/collision rotate consistently.

## 7. Visual/design review

After functional traversal passes, inspect at least one example of every family from outside and from its main interior route.

- No duplicate service decks occupying adjacent Y levels.
- No ladders ending directly into a wall, roof or unsupported drop.
- No stair/ladder exit opens into an inaccessible one-block pocket.
- Elevated catwalks look intentional and connect to something useful.
- Bunker/Fusion exterior steps and Black Site hatch meet surrounding terrain acceptably; record screenshots where steep terrain still produces awkward blending.
- Damaged variants must not destroy the only required restoration terminal, secure doorway or mandatory traversal route.

## 8. Regression

- Re-run generated facility loot/dossier tests from `STRUCTURE_EXPANSION_TEST_PLAN.md`.
- Verify generated crate loot still resolves once and never refills.
- Verify finite security reserve persistence and Peaceful suppression.
- Verify the six legacy structure families remain loadable in older generated saves; no new-world MO placement is expected.
- Confirm the retired Star Map has not returned to active registration, guides or worldgen.
