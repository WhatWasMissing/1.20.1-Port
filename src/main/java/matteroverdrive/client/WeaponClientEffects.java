package matteroverdrive.client;

import com.mojang.math.Axis;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.weapon.EnergyWeaponItem;
import matteroverdrive.item.weapon.WeaponModuleItem;
import matteroverdrive.item.weapon.WeaponSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only weapon presentation for the first-person Matter Overdrive view model.
 *
 * Renderer 2.0 follows the same broad split used by modern gun mods: server-authoritative
 * weapon gameplay stays in EnergyWeaponItem, while this class owns interpolation and the
 * dedicated first-person camera/view-model path. RenderHandEvent is cancelled only for
 * Matter Overdrive energy weapons, so ordinary items immediately return to vanilla.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class WeaponClientEffects {
    private static long seenShot = Long.MIN_VALUE;

    private static float previousAim;
    private static float aim;
    private static float previousCharge;
    private static float charge;
    private static float previousRenderRecoil;
    private static float renderRecoil;

    private static float recoilPitch;
    private static float recoilYaw;
    private static WeaponItemRenderer firstPersonRenderer;

    private WeaponClientEffects() {}

    private static ItemStack heldStack() {
        var player = Minecraft.getInstance().player;
        return player == null ? ItemStack.EMPTY : player.getMainHandItem();
    }

    private static WeaponItemRenderer firstPersonRenderer() {
        if (firstPersonRenderer == null) firstPersonRenderer = new WeaponItemRenderer();
        return firstPersonRenderer;
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
        previousCharge = charge;
        previousRenderRecoil = renderRecoil;

        ItemStack stack = heldStack();
        boolean holdingWeapon = stack.getItem() instanceof EnergyWeaponItem;
        boolean wantsAim = holdingWeapon && minecraft.player.isUsingItem()
                && minecraft.player.getUsedItemHand() == InteractionHand.MAIN_HAND;
        aim = approach(aim, wantsAim ? 1.0F : 0.0F, wantsAim ? 0.18F : 0.24F);

        float wantedCharge = 0.0F;
        if (stack.getItem() instanceof EnergyWeaponItem weapon && wantsAim) {
            int elapsed = Math.max(0, weapon.getUseDuration(stack) - minecraft.player.getUseItemRemainingTicks());
            wantedCharge = switch (weapon.getWeaponType()) {
                case ION_SNIPER -> Mth.clamp(elapsed / 12.0F, 0.0F, 1.0F);
                case PLASMA_SHOTGUN -> Mth.clamp(elapsed / 20.0F, 0.0F, 1.0F);
                default -> 0.0F;
            };
        }
        charge = approach(charge, wantedCharge, wantedCharge > charge ? 0.16F : 0.28F);

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
            charge = 0.0F;
            previousCharge = 0.0F;
            renderRecoil = 0.0F;
            previousRenderRecoil = 0.0F;
        }

        // A shot is an impulse, not a held-item wobble.
        recoilPitch *= 0.70F;
        recoilYaw *= 0.66F;
        renderRecoil *= 0.58F;
        if (Math.abs(recoilPitch) < 0.01F) recoilPitch = 0.0F;
        if (Math.abs(recoilYaw) < 0.01F) recoilYaw = 0.0F;
        if (renderRecoil < 0.01F) renderRecoil = 0.0F;
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null) return;

        // A dedicated gun view model owns the first-person frame. Keeping the ordinary
        // off hand visible here causes shields/tools to clip directly through long guns.
        if (event.getHand() == InteractionHand.OFF_HAND) {
            if (player.getMainHandItem().getItem() instanceof EnergyWeaponItem) event.setCanceled(true);
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof EnergyWeaponItem weapon)) return;

        event.setCanceled(true);
        float partial = event.getPartialTick();
        float aimed = smoothStep(Mth.lerp(partial, previousAim, aim));
        float charged = smoothStep(Mth.lerp(partial, previousCharge, charge));
        float kick = smoothStep(Mth.lerp(partial, previousRenderRecoil, renderRecoil));
        WeaponRenderProfile profile = WeaponRenderProfile.forWeapon(weapon);

        var pose = event.getPoseStack();
        pose.pushPose();
        applyViewModelTransform(pose, player, profile, aimed, charged, kick,
                event.getEquipProgress(), event.getSwingProgress(), partial);
        firstPersonRenderer().renderFirstPerson(stack, pose, event.getMultiBufferSource(),
                event.getPackedLight(), OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    private static void applyViewModelTransform(com.mojang.blaze3d.vertex.PoseStack pose,
                                                net.minecraft.client.player.LocalPlayer player,
                                                WeaponRenderProfile profile,
                                                float aimed, float charged, float kick,
                                                float equipProgress, float swingProgress,
                                                float partialTick) {
        // Recovered 1.12 first-person pose. The item renderer uses FIXED afterwards,
        // whose 180-degree Y rotation combines with this +5 degrees to reproduce the
        // legacy 185-degree hip orientation. ADS subtracts the same legacy deltas.
        pose.translate(0.13D, -0.18D, -0.55D);
        pose.mulPose(Axis.YP.rotationDegrees(5.0F));
        pose.mulPose(Axis.XP.rotationDegrees(3.0F));

        // Forge/vanilla expose equipProgress as the lowering amount used by the held-item
        // renderer (0 when fully equipped). Keep the motion short so weapon swaps read as
        // a draw instead of the vanilla eating/bow animation.
        float equip = Mth.clamp(equipProgress, 0.0F, 1.0F);
        pose.translate(0.025D * equip, -0.34D * equip, 0.08D * equip);
        pose.mulPose(Axis.ZP.rotationDegrees(7.0F * equip));

        float hip = 1.0F - aimed;
        float swing = Mth.sin(Mth.sqrt(Mth.clamp(swingProgress, 0.0F, 1.0F)) * Mth.PI) * hip;
        pose.translate(-0.035D * swing, 0.012D * swing, 0.018D * swing);
        pose.mulPose(Axis.YP.rotationDegrees(-3.5F * swing));
        pose.mulPose(Axis.ZP.rotationDegrees(1.5F * swing));

        // Modern gun renderers replace large vanilla item bob with smaller view-model sway.
        double horizontalSpeed = player.getDeltaMovement().horizontalDistance();
        float movement = (float) Math.min(1.0D, horizontalSpeed * 5.5D);
        if (!player.onGround()) movement *= 0.20F;
        if (player.isSprinting()) movement = Math.min(1.0F, movement * 1.20F);
        movement *= 1.0F - aimed * 0.82F;
        float phase = (player.tickCount + partialTick) * 0.72F;
        float sideBob = Mth.sin(phase) * movement;
        float verticalBob = Math.abs(Mth.cos(phase)) * movement;
        pose.translate(profile.moveBob() * sideBob,
                -profile.moveBob() * 0.45F * verticalBob,
                0.0025F * verticalBob);
        pose.mulPose(Axis.ZP.rotationDegrees(profile.moveRoll() * sideBob));

        // Tiny idle drift keeps a stationary view model from looking bolted to the screen.
        float idlePhase = (player.tickCount + partialTick) * 0.075F;
        pose.translate(Mth.sin(idlePhase) * 0.0015F * hip,
                Mth.cos(idlePhase * 0.83F) * 0.0010F * hip,
                0.0D);

        // Legacy hip -> aim delta: X 0.13 -> 0, Y rot 185 -> 180, X rot 3 -> 0.
        pose.translate(-0.13D * aimed, 0.04D * aimed, -0.30D * aimed);
        pose.mulPose(Axis.YP.rotationDegrees(-5.0F * aimed));
        pose.mulPose(Axis.XP.rotationDegrees(-3.0F * aimed));

        // Charge motion is presentation-only. Gameplay charge remains server authoritative.
        pose.translate(0.0D, -0.006D * charged, profile.chargeBack() * charged);
        pose.mulPose(Axis.XP.rotationDegrees(profile.chargePitch() * charged));

        // Model kick is synchronized to the same shot timestamp that drives camera recoil.
        pose.translate(0.0D, profile.recoilLift() * kick, profile.recoilBack() * kick);
        pose.mulPose(Axis.XP.rotationDegrees(-profile.recoilPitch() * kick));

        // The recovered first-person OBJ display entry compressed depth to 0.8.
        pose.scale(1.0F, 1.0F, 0.8F);
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
