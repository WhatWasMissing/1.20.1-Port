# Dimensional Pylon Parity Handoff

## Pass scope
Restore the actual Dimensional Pylon system from authoritative Matter Overdrive 1.7.10 while preserving the existing 1.20 compatibility relay behavior for old worlds.

## Source authority
- Matter Overdrive 1.7.10 contains the Dimensional Pylon machine, multiblock, generation component, GUI, dimensional-rift world data, OBJ/overlay assets and Pylon effects.
- Matter Overdrive 1.12.2 contains no Dimensional Pylon implementation. Therefore 1.7 defines machine identity for this feature.
- The pre-pass 1.20 `pylon` was a port-created 16-channel / 64-block wireless item-network bridge rather than the legacy Dimensional Pylon.

## Restored legacy identity
Recovered and implemented:
- 2x3x2 solid Pylon multiblock (12 blocks).
- Main/controller position: maxX, minY, maxZ.
- 2048 matter capacity.
- 128 matter receive limit and no matter extraction.
- 1,000,000 FE capacity.
- 2048 FE/t maximum outward transfer.
- maximum charge 2048.
- generation constants: 256 base rift FE/t, 128 full-charge bonus FE/t, 5 full-charge matter/s addition.
- natural charge decay every 40 ticks.
- `blocks.pylon` machine sound while generating.
- child Pylons forward FE/matter capability and GUI state to the main Pylon.

## Modernization decisions
- Unformed Pylons retain the existing channel relay so previously built networks do not break.
- Formed Dimensional Pylons are removed from wireless relay links.
- Shift-right-click forms a complete 2x3x2 assembly; when no valid structure is present it continues cycling relay channels.
- Legacy dimensional-rift shaping `(noise - 0.45)^5 * 180`, clamped to 0..1, is retained conceptually but sampled using a deterministic continuous world-seed/position field. This avoids the legacy integer-lattice sampling problem.
- The legacy power component's `matterDrainPerSec` path appears to add matter and run every tick despite the field name. The port interprets it as an actual drain and consumes it once per second.
- The existing network relay is treated as compatibility behavior, not retroactively described as legacy parity.

## Files changed
- `src/main/java/matteroverdrive/block/PylonBlock.java`
- `src/main/java/matteroverdrive/blockentity/PylonBlockEntity.java`
- `src/main/java/matteroverdrive/menu/PylonMenu.java`
- `src/main/java/matteroverdrive/client/screen/PylonScreen.java`
- `src/main/java/matteroverdrive/registry/ModMenus.java`
- `src/main/java/matteroverdrive/client/ClientModEvents.java`
- `docs/testing/DIMENSIONAL_PYLON_PARITY_TESTING.md`
- `docs/handoffs/DIMENSIONAL_PYLON_PARITY_HANDOFF.md`

## Compatibility / migration notes
- Old unformed Pylons keep their `Channel` NBT.
- Existing item networks continue using unformed Pylons as wireless bridges.
- Forming a structure intentionally switches those 12 blocks out of wireless relay mode.
- Breaking a formed structure returns surviving members to relay mode.
- No registry ID was replaced, so existing placed `matteroverdrive:pylon` blocks remain valid.

## Visually unverified / next work
The backend/operator GUI is code-verifiable, but these still need the later visual test harness or in-game inspection:
- exact legacy OBJ multiblock silhouette;
- animated pylon overlay/glow;
- electric arcs and dimensional-field presentation;
- texture orientation and sound balance.

The next pass after CI should be the broader visual/parity cleanup pass: Pylon renderer first, then structure/model/GUI visual gaps, with weapons left until the visual harness/user screenshots make orientation testing reliable.

## Commit / CI
This handoff is contained in the Dimensional Pylon parity commit built on `2b40b475eb48266f924eb51662146da32d447a05`. Use `git log -1` on `testing/main` for the resulting pass SHA. Exact-head CI status should be checked before treating the pass as compile-verified.
