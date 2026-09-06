# Android Mastery + Reactor/Stabilizer Testing

Baseline: `bd979f5da07f5d7090d9a71bd66e3694522131e7` (0.3 line)

## Android branch mastery

Existing selectable perk ordinals and the persistent perk bitmask are unchanged.

Branch investment now adds derived mastery bonuses:

- 2 nodes: CALIBRATED
- 3 nodes: SPECIALIST
- 4 nodes: MASTERWORK

ASSAULT increases powered melee subsystem damage as mastery rises.
CHASSIS adds multiplicative Android damage mitigation as mastery rises.
UTILITY reduces passive installed-part FE drain and increases handheld battery charging rate as mastery rises.

The skill-tree screen displays current branch focus and mastery state while preserving the existing constellation layout, level gates, capstone forks, refunds and reset flow.

### Verify

1. Existing 0.3 Android saves retain all selected perks after updating.
2. Investing 2 / 3 / 4 nodes in a branch changes its displayed mastery tier.
3. Refunding a node immediately lowers the branch mastery tier when crossing a threshold.
4. ASSAULT mastery increases powered arm/melee bonus damage without affecting ordinary non-Android attacks.
5. CHASSIS mastery reduces received damage in addition to selected armour perks.
6. UTILITY mastery reduces passive FE drain and increases crouch-held battery charging.
7. Resetting the build clears derived mastery naturally because no separate mastery state is persisted.

## Gravitational stabilizer / fusion reactor

A gravitational stabilizer no longer advertises a fixed 4,096 FE/t receive ceiling. Its input capability can accept the reactor's offered energy up to the stabilizer buffer's remaining capacity. Upgrade effects still determine stabilizer operating cost, suppression and buffer capacity; they do not become a reactor-output throttle.

The fusion controller already bases generation on `getRealMassUnsuppressed()`. This must remain true: suppression/containment strength should not reduce the anomaly mass used for reactor generation.

### Verify

1. Record reactor `outputPotential` and `generatedLastTick` with a valid anomaly and no stabilizer Power upgrades.
2. Insert one through four Power upgrades into a powered stabilizer and confirm reactor generation potential does not fall because of stabilizer suppression.
3. Confirm the stabilizer remains powered and its own FE buffer/usage behaves normally.
4. Repeat with Power Storage upgrades and confirm they alter stabilizer buffer capacity without limiting reactor generation/output.
5. Test multiple stabilizers on the internal reactor power bus and confirm no 4,096 FE/t stabilizer receive ceiling constrains the reactor.
6. Confirm external FE cables and reactor IO still obey their own receiver/cable limits normally.
