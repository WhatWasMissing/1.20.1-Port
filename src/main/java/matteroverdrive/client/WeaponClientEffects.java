package matteroverdrive.client;

import com.mojang.math.Axis;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.weapon.EnergyWeaponItem;
import matteroverdrive.item.weapon.WeaponModuleItem;
import matteroverdrive.item.weapon.WeaponSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only weapon presentation restored from the 1.7/1.12 weapon handler behavior.
 *
 * The legacy renderer owned a smooth zoom transform and a short recoil transform in a
 * dedicated first-person path.  The 1.20 implementation keeps that separation instead
 * of putting presentation state into the weapon gameplay item.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class WeaponClientEffects {
    private static long seenShot = Long.MIN_VALUE;

    private static float previousAim;
    private static float aim;
    private static float previousRenderRecoil;
    private static float renderRecoil;

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
        if (zoom < 1.0D) {
            double easedAim = smoothStep(aim);
            event.setFOV(event.getFOV() * Mth.lerp(easedAim, 1.0D, zoom));
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.isPaused()) return;

        previousAim = aim;
        previousRenderRecoil = renderRecoil;

        ItemStack stack = heldStack();
        boolean holdingWeapon = stack.getItem() instanceof EnergyWeaponItem;
        boolean wantsAim = holdingWeapon && minecraft.player.isUsingItem()
                && minecraft.player.getUsedItemHand() == InteractionHand.MAIN_HAND;
        aim = approach(aim, wantsAim ? 1.0F : 0.0F, wantsAim ? 0.18F : 0.24F);

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
                renderRecoil = 1.0F;
            }
        } else {
            aim = 0.0F;
            previousAim = 0.0F;
        }

        // A shot is an impulse, not a held-item wobble.  This mirrors the structure of
        // modern gun renderers while retaining the legacy Matter Overdrive recoil values.
        recoilPitch *= 0.70F;
        recoilYaw *= 0.66F;
        renderRecoil *= 0.58F;
        if (Math.abs(recoilPitch) < 0.01F) recoilPitch = 0.0F;
        if (Math.abs(recoilYaw) < 0.01F) recoilYaw = 0.0F;
        if (renderRecoil < 0.01F) renderRecoil = 0.0F;
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof EnergyWeaponItem weapon)) return;

        float partial = event.getPartialTick();
        float aimed = smoothStep(Mth.lerp(partial, previousAim, aim));
        float kick = smoothStep(Mth.lerp(partial, previousRenderRecoil, renderRecoil));
        WeaponRenderProfile profile = WeaponRenderProfile.forWeapon(weapon);

        var pose = event.getPoseStack();

        // The OBJ display transforms already place each weapon in the hand. Keep the
        // runtime aim delta deliberately small so it does not double-apply the old 1.12
        // transform and push the model out of the first-person camera.
        pose.translate(-0.045D * aimed,
                0.018D * aimed + profile.recoilLift() * kick,
                -0.105D * aimed + profile.recoilBack() * kick);
        pose.mulPose(Axis.YP.rotationDegrees(-1.5F * aimed));
        pose.mulPose(Axis.XP.rotationDegrees(-0.75F * aimed - profile.recoilPitch() * kick));
    }

    @SubscribeEvent
    public static void onThirdPersonWeaponPose(RenderPlayerEvent.Pre event) {
        var player = event.getEntity();
        if (!player.isUsingItem()
                || player.getUsedItemHand() != InteractionHand.MAIN_HAND
                || !(player.getMainHandItem().getItem() instanceof EnergyWeaponItem)) return;

        // startUsingItem is the server-authoritative firing/aim state, but vanilla maps
        // every custom UseAnim.NONE item to the generic ITEM arm pose. That pose looks
        // like eating/holding-use forever in third person, so leave the arm in its
        // ordinary held-item position while the weapon handles its own presentation.
        event.getRenderer().getModel().rightArmPose = HumanoidModel.ArmPose.EMPTY;
    }

    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        if (recoilPitch == 0.0F && recoilYaw == 0.0F) return;
        event.setPitch(event.getPitch() - recoilPitch);
        event.setYaw(event.getYaw() + recoilYaw);
    }

    private static float approach(float value, float target, float amount) {
        if (value < target) return Math.min(target, value + amount);
        if (value > target) return Math.max(target, value - amount);
        return value;
    }

    private static float smoothStep(float value) {
        float t = Mth.clamp(value, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }
}
