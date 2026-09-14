package matteroverdrive.client;

import com.mojang.math.Axis;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.weapon.EnergyWeaponItem;
import matteroverdrive.item.weapon.VexMythoclastItem;
import matteroverdrive.item.weapon.WeaponModuleItem;
import matteroverdrive.item.weapon.WeaponSystem;
import matteroverdrive.network.WeaponTriggerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
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
 * Client-only firearm presentation for Renderer 2.0.
 *
 * Trigger and aim are intentionally independent: LMB drives the server-authoritative
 * Item use/fire loop, RMB drives this ADS state, and Shift+RMB remains reload/mode input.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class WeaponClientEffects {
    private static long seenShot = Long.MIN_VALUE;
    private static String seenWeapon = "";

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
        ItemStack stack = heldStack();
        if (!WeaponTriggerPacket.isFirearm(stack) || !WeaponTriggerInput.isAiming()) return;

        double zoom = aimZoom(stack);
        if (zoom < 1.0D) {
            double easedAim = smoothStep(aim);
            event.setFOV(event.getFOV() * Mth.lerp(easedAim, 1.0D, zoom));
        }
    }

    private static double aimZoom(ItemStack stack) {
        if (stack.getItem() instanceof VexMythoclastItem vex) {
            return vex.isLinearMode(stack) ? 0.62D : 0.78D;
        }
        if (stack.getItem() instanceof EnergyWeaponItem weapon) {
            double zoom = weapon.getWeaponType() == EnergyWeaponItem.WeaponType.ION_SNIPER ? 0.40D : 1.0D;
            if (WeaponSystem.hasEffect(stack, WeaponModuleItem.Effect.SNIPER_SCOPE)) zoom = 0.85D;
            return zoom;
        }
        return 1.0D;
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
        boolean holdingWeapon = WeaponTriggerPacket.isFirearm(stack);
        boolean wantsAim = holdingWeapon && WeaponTriggerInput.isAiming();
        aim = approach(aim, wantsAim ? 1.0F : 0.0F, wantsAim ? 0.18F : 0.24F);

        int triggerTicks = WeaponTriggerInput.heldTicks();
        float wantedCharge = 0.0F;
        if (stack.getItem() instanceof VexMythoclastItem vex && vex.isLinearMode(stack)) {
            wantedCharge = Mth.clamp(triggerTicks / 12.0F, 0.0F, 1.0F);
        } else if (stack.getItem() instanceof EnergyWeaponItem weapon
                ) {
            wantedCharge = switch (weapon.getWeaponType()) {
                case ION_SNIPER -> Mth.clamp(triggerTicks / 12.0F, 0.0F, 1.0F);
                case PLASMA_SHOTGUN -> Mth.clamp(triggerTicks / 20.0F, 0.0F, 1.0F);
                default -> 0.0F;
            };
        }
        charge = approach(charge, wantedCharge, wantedCharge > charge ? 0.16F : 0.28F);

        if (holdingWeapon) {
            long shot = shotTimestamp(stack);
            String weaponId = String.valueOf(BuiltInRegistries.ITEM.getKey(stack.getItem()));
            if (shot != 0L && (shot != seenShot || !weaponId.equals(seenWeapon))) {
                seenShot = shot;
                seenWeapon = weaponId;
                WeaponRenderProfile profile = WeaponRenderProfile.forStack(stack);
                float heatMultiplier = 1.0F;
                if (stack.getItem() instanceof EnergyWeaponItem weapon
                        ) {
                    heatMultiplier += weapon.getHeat(stack) / Math.max(1.0F, weapon.getMaxHeat(stack));
                }
                float base = profile.recoilPitch() * heatMultiplier;
                recoilPitch = Math.max(recoilPitch, base);
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

        recoilPitch *= 0.70F;
        recoilYaw *= 0.66F;
        renderRecoil *= 0.58F;
        if (Math.abs(recoilPitch) < 0.01F) recoilPitch = 0.0F;
        if (Math.abs(recoilYaw) < 0.01F) recoilYaw = 0.0F;
        if (renderRecoil < 0.01F) renderRecoil = 0.0F;
    }

    private static long shotTimestamp(ItemStack stack) {
        if (stack.getItem() instanceof VexMythoclastItem) {
            return stack.getOrCreateTag().getLong("VexLastShot");
        }
        if (stack.getItem() instanceof EnergyWeaponItem) {
            return stack.getOrCreateTag().getLong("MatterOverdriveLastShot");
        }
        return 0L;
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null) return;

        if (event.getHand() == InteractionHand.OFF_HAND) {
            if (WeaponTriggerPacket.isFirearm(player.getMainHandItem())) event.setCanceled(true);
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!WeaponTriggerPacket.isFirearm(stack)) return;

        event.setCanceled(true);
        float partial = event.getPartialTick();
        float aimed = smoothStep(Mth.lerp(partial, previousAim, aim));
        float charged = smoothStep(Mth.lerp(partial, previousCharge, charge));
        float kick = smoothStep(Mth.lerp(partial, previousRenderRecoil, renderRecoil));
        WeaponRenderProfile profile = WeaponRenderProfile.forStack(stack);

        var pose = event.getPoseStack();
        pose.pushPose();
        applyViewModelTransform(pose, player, stack, profile, aimed, charged, kick,
                event.getEquipProgress(), event.getSwingProgress(), partial);
        firstPersonRenderer().renderFirstPerson(stack, pose, event.getMultiBufferSource(),
                event.getPackedLight(), OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    private static void applyViewModelTransform(com.mojang.blaze3d.vertex.PoseStack pose,
                                                net.minecraft.client.player.LocalPlayer player,
                                                ItemStack stack,
                                                WeaponRenderProfile profile,
                                                float aimed, float charged, float kick,
                                                float equipProgress, float swingProgress,
                                                float partialTick) {
        boolean authoredBase = stack.getItem() instanceof VexMythoclastItem;

        if (!authoredBase) {
            // Recovered MO first-person base; FIXED contributes the 180-degree Y rotation.
            pose.translate(0.13D, -0.18D, -0.55D);
            pose.mulPose(Axis.YP.rotationDegrees(5.0F));
            pose.mulPose(Axis.XP.rotationDegrees(3.0F));
        }

        float equip = Mth.clamp(equipProgress, 0.0F, 1.0F);
        pose.translate(0.025D * equip, -0.34D * equip, 0.08D * equip);
        pose.mulPose(Axis.ZP.rotationDegrees(7.0F * equip));

        float hip = 1.0F - aimed;
        float swing = Mth.sin(Mth.sqrt(Mth.clamp(swingProgress, 0.0F, 1.0F)) * Mth.PI) * hip;
        pose.translate(-0.035D * swing, 0.012D * swing, 0.018D * swing);
        pose.mulPose(Axis.YP.rotationDegrees(-3.5F * swing));
        pose.mulPose(Axis.ZP.rotationDegrees(1.5F * swing));

        double horizontalSpeed = player.getDeltaMovement().horizontalDistance();
        float movement = (float)Math.min(1.0D, horizontalSpeed * 5.5D);
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

        float idlePhase = (player.tickCount + partialTick) * 0.075F;
        pose.translate(Mth.sin(idlePhase) * 0.0015F * hip,
                Mth.cos(idlePhase * 0.83F) * 0.0010F * hip,
                0.0D);

        if (authoredBase) {
            // Imported model JSONs retain their authored base placement; Renderer 2.0 adds
            // only the camera-space ADS delta on top.
            pose.translate(-0.105D * aimed, 0.035D * aimed, -0.22D * aimed);
            pose.mulPose(Axis.XP.rotationDegrees(-1.5F * aimed));
        } else {
            // Legacy MO hip -> aim delta: X .13 -> 0, Y 185 -> 180, X 3 -> 0.
            pose.translate(-0.13D * aimed, 0.04D * aimed, -0.30D * aimed);
            pose.mulPose(Axis.YP.rotationDegrees(-5.0F * aimed));
            pose.mulPose(Axis.XP.rotationDegrees(-3.0F * aimed));
        }

        pose.translate(0.0D, -0.006D * charged, profile.chargeBack() * charged);
        pose.mulPose(Axis.XP.rotationDegrees(profile.chargePitch() * charged));

        pose.translate(0.0D, profile.recoilLift() * kick, profile.recoilBack() * kick);
        pose.mulPose(Axis.XP.rotationDegrees(-profile.recoilPitch() * kick));

        if (!authoredBase) pose.scale(1.0F, 1.0F, 0.8F);
    }

    @SubscribeEvent
    public static void onThirdPersonWeaponPose(RenderPlayerEvent.Pre event) {
        var player = event.getEntity();
        if (!player.isUsingItem()
                || player.getUsedItemHand() != InteractionHand.MAIN_HAND
                || !(player.getMainHandItem().getItem() instanceof EnergyWeaponItem)) return;

        // Vanilla assigns the generic ITEM pose to custom UseAnim.NONE weapons. Clear it
        // while the weapon renderer owns the firearm presentation, otherwise third-person
        // aiming looks like the player is permanently eating/using the item.
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
