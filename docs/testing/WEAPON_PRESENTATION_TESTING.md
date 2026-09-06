# Weapon Presentation Testing

Branch: `testing/main`
Reference behavior: Matter Overdrive 1.7.10 `0.4.2` and 1.12.2 `0.7.1.0` only.

This pass modernizes client presentation without changing weapon FE, damage, heat, spread or cooldown gameplay.

## First-person placement
- [ ] Phaser is visible and points away from the player in first person.
- [ ] Phaser Rifle is visible and points away from the player in first person.
- [ ] Ion Sniper is visible and points away from the player in first person.
- [ ] Plasma Shotgun is visible and points away from the player in first person.
- [ ] No weapon clips completely out of view during normal use.
- [ ] Switching rapidly between a weapon and a normal item does not leave a stale transform.

## Aim / ADS transition
- [ ] Holding use transitions smoothly into the aimed pose instead of snapping.
- [ ] Releasing use returns smoothly to the hip pose.
- [ ] Ion Sniper reaches its recovered 0.40 base FOV multiplier smoothly.
- [ ] Sniper Scope reaches the recovered 0.85 override smoothly on supported weapons.
- [ ] Weapon position and FOV transition together without a visible one-frame jump.

## Recoil
- [ ] A shot gives the held weapon a brief backwards/upwards kick and then recovers.
- [ ] Recoil does not continue merely because the weapon remains equipped.
- [ ] Phaser recoil is light, Phaser Rifle moderate, Ion Sniper strong and Plasma Shotgun strongest visually.
- [ ] Ion Sniper retains stronger unzoomed than zoomed camera recoil.
- [ ] Rapid Phaser/Phaser Rifle fire produces repeated impulses rather than permanent drift.

## Other render contexts
- [ ] Third-person right-hand orientation remains correct.
- [ ] Third-person left-hand orientation remains correct where applicable.
- [ ] GUI, ground and fixed/item-frame rendering remain correctly positioned.
- [ ] Installed optics do not become detached floating inventory icons.

## Regression
- [ ] Weapon FE rules are unchanged.
- [ ] Heat/overheat behavior is unchanged.
- [ ] Damage/range/spread/cooldown are unchanged.
- [ ] Weapon Station module persistence is unchanged.
- [ ] No render crash occurs with shaders enabled or disabled.
