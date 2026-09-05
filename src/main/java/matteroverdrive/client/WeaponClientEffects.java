package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.weapon.EnergyWeaponItem;
import matteroverdrive.item.weapon.WeaponModuleItem;
import matteroverdrive.item.weapon.WeaponSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client-only weapon presentation restored from the 1.7/1.12 weapon handler behavior. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class WeaponClientEffects {
    private static long seenShot = Long.MIN_VALUE;
    private static float recoilPitch;
    private static float recoilYaw;

    private WeaponClientEffects() {}

    private static ItemStack heldStack() {
        var player = Minecraft.getInstance().player;
        return player == null ? ItemStack.EMPTY : player.getMainHandItem();
    }

    @SubscribeEvent
    public static void onFov(ViewportEvent.ComputeFov event) {
        var player = Minecraft.getInstance().player;
        ItemStack stack = heldStack();
        if (player == null || !player.isUsingItem() || !(stack.getItem() instanceof EnergyWeaponItem weapon)) return;

        // Legacy EnergyWeapon#getZoomMultiply asks a scope module first, then the weapon base.
        // 1.12 Ion Sniper base zoom = 0.4 and Sniper Scope getZoomAmount = 0.85.
        double zoom = weapon.getWeaponType() == EnergyWeaponItem.WeaponType.ION_SNIPER ? 0.40D : 1.0D;
        if (WeaponSystem.hasEffect(stack, WeaponModuleItem.Effect.SNIPER_SCOPE)) zoom = 0.85D;
        if (zoom < 1.0D) event.setFOV(event.getFOV() * zoom);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.isPaused()) return;

        ItemStack stack = heldStack();
        if (stack.getItem() instanceof EnergyWeaponItem weapon) {
            long shot = stack.getOrCreateTag().getLong("MatterOverdriveLastShot");
            if (shot != 0L && shot != seenShot) {
                seenShot = shot;
                float heat = weapon.getHeat(stack) / Math.max(1.0F, weapon.getMaxHeat(stack));
                float base = switch (weapon.getWeaponType()) {
                    case PHASER -> 0.55F;
                    case PHASER_RIFLE -> 1.10F;
                    case ION_SNIPER -> minecraft.player.isUsingItem() ? 3.0F : 4.0F;
                    case PLASMA_SHOTGUN -> 2.35F;
                };
                recoilPitch = Math.max(recoilPitch, base + base * heat);
                recoilYaw += (minecraft.player.getRandom().nextFloat() - 0.5F) * base * 0.28F;
            }
        }

        recoilPitch *= 0.70F;
        recoilYaw *= 0.66F;
        if (Math.abs(recoilPitch) < 0.01F) recoilPitch = 0.0F;
        if (Math.abs(recoilYaw) < 0.01F) recoilYaw = 0.0F;
    }

    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        if (recoilPitch == 0.0F && recoilYaw == 0.0F) return;
        event.setPitch(event.getPitch() - recoilPitch);
        event.setYaw(event.getYaw() + recoilYaw);
    }
}
