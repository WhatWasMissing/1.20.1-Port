package matteroverdrive.client;

import matteroverdrive.item.weapon.EnergyWeaponItem;

/**
 * Client-only presentation tuning for Matter Overdrive energy weapons.
 *
 * Gameplay values deliberately do not live here. The profile only describes view-model
 * recoil, movement sway and charge motion while EnergyWeaponItem remains authoritative
 * for FE, heat, damage, cooldown, spread and actual charge results.
 */
public record WeaponRenderProfile(
        float recoilBack,
        float recoilLift,
        float recoilPitch,
        float moveBob,
        float moveRoll,
        float chargeBack,
        float chargePitch
) {
    public static WeaponRenderProfile forWeapon(EnergyWeaponItem weapon) {
        return switch (weapon.getWeaponType()) {
            case PHASER -> new WeaponRenderProfile(
                    0.020F, 0.006F, 1.25F,
                    0.010F, 0.45F,
                    0.000F, 0.00F);
            case PHASER_RIFLE -> new WeaponRenderProfile(
                    0.035F, 0.010F, 2.00F,
                    0.008F, 0.38F,
                    0.000F, 0.00F);
            case ION_SNIPER -> new WeaponRenderProfile(
                    0.060F, 0.018F, 3.75F,
                    0.005F, 0.22F,
                    0.025F, 1.20F);
            case PLASMA_SHOTGUN -> new WeaponRenderProfile(
                    0.080F, 0.024F, 4.50F,
                    0.010F, 0.45F,
                    0.045F, 2.00F);
        };
    }
}
