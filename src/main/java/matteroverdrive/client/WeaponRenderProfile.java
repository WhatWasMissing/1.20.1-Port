package matteroverdrive.client;

import matteroverdrive.item.weapon.EnergyWeaponItem;

/**
 * Client-only presentation tuning for Matter Overdrive energy weapons.
 *
 * Gameplay values deliberately do not live here.  The profile only describes how much
 * a shot moves the held model while the server-authoritative weapon item continues to
 * own energy, heat, damage, cooldown and spread.
 */
public record WeaponRenderProfile(float recoilBack, float recoilLift, float recoilPitch) {
    public static WeaponRenderProfile forWeapon(EnergyWeaponItem weapon) {
        return switch (weapon.getWeaponType()) {
            case PHASER -> new WeaponRenderProfile(0.020F, 0.006F, 1.25F);
            case PHASER_RIFLE -> new WeaponRenderProfile(0.035F, 0.010F, 2.00F);
            case ION_SNIPER -> new WeaponRenderProfile(0.060F, 0.018F, 3.75F);
            case PLASMA_SHOTGUN -> new WeaponRenderProfile(0.080F, 0.024F, 4.50F);
        };
    }
}
