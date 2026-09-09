package matteroverdrive.client;

import matteroverdrive.item.weapon.EnergyWeaponItem;
import matteroverdrive.item.weapon.NativeDestinyWeaponItem;
import matteroverdrive.item.weapon.NativeDestinyWeaponProfile;
import matteroverdrive.item.weapon.VexMythoclastItem;
import net.minecraft.world.item.ItemStack;

/**
 * Client-only presentation tuning for Matter Overdrive firearms.
 * Gameplay values deliberately do not live here.
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
    public static WeaponRenderProfile forStack(ItemStack stack) {
        if (stack.getItem() instanceof VexMythoclastItem) {
            return new WeaponRenderProfile(
                    0.045F, 0.012F, 2.65F,
                    0.007F, 0.32F,
                    0.035F, 1.65F);
        }
        if (stack.getItem() instanceof NativeDestinyWeaponItem destiny) {
            NativeDestinyWeaponProfile profile = destiny.profile();
            if (profile == NativeDestinyWeaponProfile.SLEEPER_SIMULANT) {
                return new WeaponRenderProfile(
                        0.105F, 0.030F, 5.60F,
                        0.004F, 0.18F,
                        0.020F, 1.10F);
            }
            if (profile.automatic()) {
                return new WeaponRenderProfile(
                        0.038F, 0.010F, 2.05F,
                        0.008F, 0.36F,
                        0.000F, 0.00F);
            }
            if (profile.rpm() <= 160) {
                return new WeaponRenderProfile(
                        0.068F, 0.018F, 3.65F,
                        0.006F, 0.28F,
                        0.000F, 0.00F);
            }
            return new WeaponRenderProfile(
                    0.050F, 0.014F, 2.80F,
                    0.006F, 0.30F,
                    0.000F, 0.00F);
        }
        if (stack.getItem() instanceof EnergyWeaponItem weapon) return forWeapon(weapon);
        return new WeaponRenderProfile(0.035F, 0.010F, 2.0F, 0.008F, 0.35F, 0.0F, 0.0F);
    }

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
