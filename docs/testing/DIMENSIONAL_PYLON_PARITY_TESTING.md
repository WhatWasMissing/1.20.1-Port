# Dimensional Pylon Parity Testing

Authoritative identity: Matter Overdrive 1.7.10. The 1.12.2 reference removed the Dimensional Pylon entirely, so 1.7 defines the restored machine while the existing 1.20 channel relay is preserved as compatibility behavior for unformed blocks.

## Relay compatibility
- Place a single Pylon: it remains an unformed compatibility relay.
- Shift-right-click without a valid 2x3x2 Pylon structure: channel cycles 0-15 exactly as before.
- Two unformed Pylons on the same channel within 64 blocks must still bridge the item network.
- Existing worlds with saved `Channel` NBT must retain their channel.

## Structure formation
- Build a solid 2x3x2 rectangular prism of 12 Pylon blocks.
- Shift-right-click any member: the structure should form.
- Main block must resolve to maxX/minY/maxZ, matching the 1.7 structure controller position.
- All 12 members should report formed and route their GUI/capabilities to the same main Pylon.
- Right-click any formed member: Dimensional Pylon operator GUI opens.
- Break any member: the whole structure should unform and surviving blocks return to compatibility relay mode.
- Formed Pylons must not create wireless channel links.

## Matter and FE
- Main Pylon matter capacity: 2048.
- Matter can enter through the DOWN/null matter capability, maximum 128 per receive operation.
- Matter cannot be extracted through the external matter capability.
- FE capacity: 1,000,000.
- Pylon cannot receive FE externally.
- Total outward FE transfer must not exceed 2048 FE/t across the whole formed structure.
- Adjacent FE receivers on any outer face of the 12-block structure should be able to receive output.

## Dimensional generation
- Rift strength is deterministic for a fixed world seed and location.
- Moving the structure to another region should usually give a different rift strength.
- Base generation contribution is dimensionalStrength x 256 FE/t.
- Base matter drain is ceil(dimensionalStrength x 20) matter/s.
- Generation stops if matter is below the current per-second drain requirement.
- Generation stops at full FE storage.
- Matter is consumed once per second, not once per tick.
- `blocks.pylon` sound should occasionally play while generating.

## Charge
- Maximum charge is 2048.
- Charge contribution adds up to 128 FE/t at full charge.
- Charge contribution adds up to 5 matter/s drain at full charge.
- Positive charge decays every 40 ticks by `1 + round(charge * 0.005)`.
- Charge, matter, FE, structure state, and main position must persist across save/reload.

## GUI
- GUI must show FE stored/capacity.
- GUI must show matter stored/capacity.
- GUI must show charge/max charge.
- GUI must show live rift strength.
- GUI must show generated FE/t and matter drain/s.
- GUI opened from any child should show the root Pylon state.

## Visual verification later
- Exact 1.7 OBJ-based multiblock silhouette is not yet visually verified.
- Pylon overlay animation/glow needs in-game inspection.
- Electric arc presentation and dimensional-field effects need in-game inspection.
- Confirm pylon texture orientation and sound volume against the legacy reference.
