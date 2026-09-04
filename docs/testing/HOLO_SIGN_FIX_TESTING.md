# Holo Sign focused regression test

Branch: `testing/alpha`

## Runtime report that triggered this fix

The first restored-Holo-Sign alpha test confirmed the other legacy-entity work was functional:

- Rogue Android combat: PASS
- Rogue Android sounds: PASS
- Mad Scientist / Puny Humans quest: PASS
- Failed cow/pig/sheep/chicken spawn and basic behaviour: PASS
- Holo Sign: FAIL

Observed Holo Sign failures:

- the block rendered as a full `base` cube instead of the legacy thin monitor panel;
- sneak-using a renamed BlockItem on the sign placed the held block instead of programming the Holo Sign.

## Fix applied

- Restored the original 1.12.2 thin Holo Sign monitor geometry using `holo_monitor` on the front and `base` on the sides.
- Added horizontal facing and a matching thin selection/collision shape.
- Added a Forge right-click interceptor for crouching Holo Sign interactions so programming occurs before BlockItem placement.
- Security Protocol checks still run first, so a claimed sign cannot be bypassed by the programming interaction.
- Moved/scaled the holographic text toward the restored monitor body for a cleaner first-pass presentation.

## Retest only these items

- [ ] Place Holo Signs facing north/south/east/west. Confirm each is a thin monitor panel rather than a full cube.
- [ ] Rename any item in an anvil to `HELLO TEST`.
- [ ] Sneak-right-click the Holo Sign with the renamed item. Confirm the held item is NOT placed or consumed.
- [ ] Confirm `HELLO TEST` appears as full-bright holographic text.
- [ ] Normal-right-click the sign and confirm its current text is reported.
- [ ] Sneak-right-click with an empty hand and confirm the text clears.
- [ ] Program the sign again, save/quit/re-enter, and confirm the text persists.
- [ ] Leave/re-enter the chunk and confirm the client receives the text again.
- [ ] Claim the Holo Sign with a Security Protocol and confirm unauthorized players cannot program or break it.

Once these pass, the Holo Sign can move from runtime-failing to runtime-verified and the next parity work can continue without retesting the already-passed restored entities.
