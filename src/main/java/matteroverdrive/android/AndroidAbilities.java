package matteroverdrive.android;

import matteroverdrive.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

import java.util.List;

public final class AndroidAbilities {
    public static final int ACTION_CYCLE = 0;
    public static final int ACTION_ACTIVATE = 1;
    public static final String ABILITY_DAMAGE_TAG = "MatterOverdriveAndroidAbilityDamage";

    public static final int CLOAK_ENERGY_PER_TICK = 128;
    public static final int SHIELD_IDLE_ENERGY_PER_TICK = 32;
    public static final int SHIELD_ENERGY_PER_DAMAGE = 64;
    public static final int SHOCKWAVE_ENERGY = 4_096;
    public static final int SHOCKWAVE_COOLDOWN = 100;
    public static final int TELEPORT_ENERGY = 4_096;
    public static final int TELEPORT_COOLDOWN = 60;

    private static final double SHOCKWAVE_RADIUS = 5.0D;
    private static final float SHOCKWAVE_DAMAGE = 6.0F;
    private static final double TELEPORT_RANGE = 8.0D;

    private AndroidAbilities() {
    }

    public static void handleAction(ServerPlayer player, int action) {
        if (!AndroidData.isAndroid(player)) {
            status(player, "Android conversion is not active.", ChatFormatting.RED);
            return;
        }

        if (action == ACTION_CYCLE) {
            AndroidData.Ability selected = AndroidData.cycleAbility(player);
            if (selected == null) {
                status(player, "Install a bionic part to unlock an ability.", ChatFormatting.RED);
            } else {
                status(player, "Selected: " + selected.displayName, ChatFormatting.AQUA);
            }
            ModNetwork.syncAndroidState(player);
            return;
        }

        if (action != ACTION_ACTIVATE) {
            return;
        }

        AndroidData.Ability ability = AndroidData.getSelectedAbility(player);
        if (!AndroidData.isAbilityUnlocked(player, ability)) {
            if (AndroidData.getLevel(player) < ability.requiredLevel) {
                status(player, ability.displayName + " unlocks at Android level "
                        + ability.requiredLevel + ".", ChatFormatting.RED);
            } else {
                status(player, ability.displayName + " requires the "
                        + ability.requiredPart.name().toLowerCase() + " bionic part.", ChatFormatting.RED);
            }
            return;
        }

        switch (ability) {
            case CLOAK -> toggleCloak(player);
            case FORCE_FIELD -> toggleShield(player);
            case SHOCKWAVE -> activateShockwave(player);
            case TELEPORT -> activateTeleport(player);
        }
        ModNetwork.syncAndroidState(player);
    }

    public static void tick(ServerPlayer player) {
        if (!AndroidData.isAndroid(player)) {
            return;
        }

        if (AndroidData.isCloakEnabled(player)) {
            if (!AndroidData.isAbilityUnlocked(player, AndroidData.Ability.CLOAK)
                    || !AndroidData.tryConsumeEnergy(player, AndroidData.scaleAbilityEnergy(
                    player, CLOAK_ENERGY_PER_TICK, AndroidData.Perk.GHOST_PROTOCOL))) {
                AndroidData.setCloakEnabled(player, false);
                status(player, "Cloak disabled: insufficient Android FE.", ChatFormatting.RED);
            } else {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 5, 0, true, false, false));
            }
        }

        if (AndroidData.isShieldEnabled(player)
                && (!AndroidData.isAbilityUnlocked(player, AndroidData.Ability.FORCE_FIELD)
                || !AndroidData.tryConsumeEnergy(player, AndroidData.scaleAbilityEnergy(
                player, SHIELD_IDLE_ENERGY_PER_TICK, AndroidData.Perk.BARRIER_MATRIX)))) {
            AndroidData.setShieldEnabled(player, false);
            status(player, "Force Field disabled: insufficient Android FE.", ChatFormatting.RED);
        }
    }

    public static void applyShield(LivingDamageEvent event, ServerPlayer player) {
        if (!AndroidData.isAndroid(player)
                || !AndroidData.isShieldEnabled(player)
                || !AndroidData.isAbilityUnlocked(player, AndroidData.Ability.FORCE_FIELD)
                || event.getAmount() <= 0.0F) {
            return;
        }

        int shieldCostPerDamage = AndroidData.scaleAbilityEnergy(
                player, SHIELD_ENERGY_PER_DAMAGE, AndroidData.Perk.BARRIER_MATRIX);
        float desiredAbsorption = event.getAmount() * 0.5F;
        float affordableAbsorption = AndroidData.getEnergy(player) / (float) shieldCostPerDamage;
        float absorbed = Math.min(desiredAbsorption, affordableAbsorption);
        if (absorbed <= 0.0F) {
            AndroidData.setShieldEnabled(player, false);
            status(player, "Force Field collapsed: Android FE depleted.", ChatFormatting.RED);
            return;
        }

        int energyCost = Math.max(1, (int) Math.ceil(absorbed * shieldCostPerDamage));
        if (!AndroidData.tryConsumeEnergy(player, energyCost)) {
            return;
        }
        event.setAmount(Math.max(0.0F, event.getAmount() - absorbed));
        showShieldPulse(player);
        if (AndroidData.getEnergy(player) < shieldCostPerDamage) {
            AndroidData.setShieldEnabled(player, false);
            status(player, "Force Field collapsed: Android FE depleted.", ChatFormatting.RED);
        }
    }

    private static void toggleCloak(ServerPlayer player) {
        boolean enabled = !AndroidData.isCloakEnabled(player);
        int energyCost = AndroidData.scaleAbilityEnergy(player, CLOAK_ENERGY_PER_TICK, AndroidData.Perk.GHOST_PROTOCOL);
        if (enabled && AndroidData.getEnergy(player) < energyCost) {
            status(player, "Cloak needs at least " + energyCost + " FE.", ChatFormatting.RED);
            return;
        }
        AndroidData.setCloakEnabled(player, enabled);
        status(player, "Cloak " + (enabled ? "enabled." : "disabled."),
                enabled ? ChatFormatting.GREEN : ChatFormatting.YELLOW);
    }

    private static void toggleShield(ServerPlayer player) {
        boolean enabled = !AndroidData.isShieldEnabled(player);
        int energyCost = AndroidData.scaleAbilityEnergy(player, SHIELD_IDLE_ENERGY_PER_TICK, AndroidData.Perk.BARRIER_MATRIX);
        if (enabled && AndroidData.getEnergy(player) < energyCost) {
            status(player, "Force Field needs Android FE.", ChatFormatting.RED);
            return;
        }
        AndroidData.setShieldEnabled(player, enabled);
        if (enabled) {
            showShieldPulse(player);
        }
        status(player, "Force Field " + (enabled ? "enabled." : "disabled."),
                enabled ? ChatFormatting.GREEN : ChatFormatting.YELLOW);
    }

    private static void activateShockwave(ServerPlayer player) {
        long gameTime = player.level().getGameTime();
        int energyCost = AndroidData.scaleAbilityEnergy(player, SHOCKWAVE_ENERGY, AndroidData.Perk.SHOCK_RECYCLER);
        double radius = SHOCKWAVE_RADIUS + (AndroidData.hasPerk(player, AndroidData.Perk.WIDEBAND_PULSE) ? 2.0D : 0.0D);
        float damage = SHOCKWAVE_DAMAGE
                + (AndroidData.hasPerk(player, AndroidData.Perk.RESONANT_PULSE) ? 2.0F : 0.0F)
                + (AndroidData.hasPerk(player, AndroidData.Perk.OVERCHARGED_PULSE) ? 3.0F : 0.0F);
        int cooldownTicks = AndroidData.hasPerk(player, AndroidData.Perk.APEX_CORE)
                ? Math.max(1, (int) Math.ceil(SHOCKWAVE_COOLDOWN * 0.80D)) : SHOCKWAVE_COOLDOWN;
        if (AndroidData.hasPerk(player, AndroidData.Perk.COOLDOWN_ROUTER)) {
            cooldownTicks = Math.max(1, (int) Math.ceil(cooldownTicks * 0.90D));
        }
        int cooldown = AndroidData.getRemainingCooldown(player, AndroidData.Ability.SHOCKWAVE, gameTime);
        if (cooldown > 0) {
            status(player, "Sonic Shockwave cooldown: " + formatSeconds(cooldown), ChatFormatting.RED);
            return;
        }
        if (AndroidData.getEnergy(player) < energyCost) {
            status(player, "Sonic Shockwave needs " + energyCost + " FE.", ChatFormatting.RED);
            return;
        }

        AABB area = player.getBoundingBox().inflate(radius, 2.5D, radius);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                target -> target != player && target.isAlive() && !target.isAlliedTo(player));
        boolean previousAbilityDamage = player.getPersistentData().getBoolean(ABILITY_DAMAGE_TAG);
        player.getPersistentData().putBoolean(ABILITY_DAMAGE_TAG, true);
        try {
            for (LivingEntity target : targets) {
                Vec3 offset = target.position().subtract(player.position());
                Vec3 horizontal = new Vec3(offset.x, 0.0D, offset.z);
                if (horizontal.lengthSqr() < 0.001D) {
                    horizontal = player.getLookAngle();
                }
                horizontal = horizontal.normalize();
                target.hurt(player.damageSources().playerAttack(player), damage);
                double push = AndroidData.hasPerk(player, AndroidData.Perk.SHOCK_MOMENTUM) ? 1.75D : 1.25D;
                target.push(horizontal.x * push, 0.45D, horizontal.z * push);
            }
        } finally {
            if (previousAbilityDamage) {
                player.getPersistentData().putBoolean(ABILITY_DAMAGE_TAG, true);
            } else {
                player.getPersistentData().remove(ABILITY_DAMAGE_TAG);
            }
        }

        AndroidData.tryConsumeEnergy(player, energyCost);
        AndroidData.setCooldownUntil(player, AndroidData.Ability.SHOCKWAVE, gameTime + cooldownTicks);
        AndroidData.addExperience(player, 25);
        status(player, "Sonic Shockwave hit " + targets.size() + " target(s).", ChatFormatting.AQUA);
    }

    private static void activateTeleport(ServerPlayer player) {
        long gameTime = player.level().getGameTime();
        int energyCost = AndroidData.scaleAbilityEnergy(player, TELEPORT_ENERGY, AndroidData.Perk.BLINK_RECYCLER);
        double teleportRange = TELEPORT_RANGE
                + (AndroidData.hasPerk(player, AndroidData.Perk.PHASE_CAPACITOR) ? 4.0D : 0.0D)
                + (AndroidData.hasPerk(player, AndroidData.Perk.PHASE_STABILIZER) ? 4.0D : 0.0D)
                + (AndroidData.hasPerk(player, AndroidData.Perk.LONG_RANGE_BLINK) ? 8.0D : 0.0D);
        int cooldownTicks = AndroidData.hasPerk(player, AndroidData.Perk.RAPID_BLINK)
                ? Math.max(1, (int) Math.ceil(TELEPORT_COOLDOWN * 0.85D)) : TELEPORT_COOLDOWN;
        if (AndroidData.hasPerk(player, AndroidData.Perk.APEX_CORE)) cooldownTicks = Math.max(1, (int) Math.ceil(cooldownTicks * 0.80D));
        if (AndroidData.hasPerk(player, AndroidData.Perk.COOLDOWN_ROUTER)) cooldownTicks = Math.max(1, (int) Math.ceil(cooldownTicks * 0.90D));
        int cooldown = AndroidData.getRemainingCooldown(player, AndroidData.Ability.TELEPORT, gameTime);
        if (cooldown > 0) {
            status(player, "Ender Teleport cooldown: " + formatSeconds(cooldown), ChatFormatting.RED);
            return;
        }
        if (AndroidData.getEnergy(player) < energyCost) {
            status(player, "Ender Teleport needs " + energyCost + " FE.", ChatFormatting.RED);
            return;
        }

        ServerLevel level = player.serverLevel();
        Vec3 origin = player.position();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 rayEnd = eye.add(look.scale(teleportRange));
        HitResult hit = level.clip(new ClipContext(
                eye, rayEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double clearDistance = hit.getType() == HitResult.Type.MISS
                ? teleportRange
                : Math.max(0.0D, hit.getLocation().distanceTo(eye) - 0.75D);

        // Trace from the eyes, then convert each point back to a feet position.
        // The previous implementation measured clearance from the eyes but moved
        // the feet along that vector, which rejected otherwise valid aimed blinks.
        double eyeOffset = eye.y - origin.y;
        Vec3 destination = null;
        for (double distance = clearDistance; distance >= 0.75D; distance -= 0.25D) {
            Vec3 sightPoint = eye.add(look.scale(distance));
            Vec3 candidate = new Vec3(sightPoint.x, sightPoint.y - eyeOffset, sightPoint.z);
            if (isSafeTeleportDestination(player, level, candidate)) {
                destination = candidate;
                break;
            }
        }

        if (destination == null) {
            status(player, "No safe Ender Teleport destination on the view ray.", ChatFormatting.RED);
            return;
        }

        level.sendParticles(ParticleTypes.PORTAL,
                origin.x, origin.y + player.getBbHeight() * 0.5D, origin.z,
                32, 0.35D, 0.65D, 0.35D, 0.15D);
        level.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.15F);

        player.teleportTo(destination.x, destination.y, destination.z);
        player.fallDistance = 0.0F;

        level.sendParticles(ParticleTypes.PORTAL,
                destination.x, destination.y + player.getBbHeight() * 0.5D, destination.z,
                32, 0.35D, 0.65D, 0.35D, 0.15D);
        level.playSound(null, destination.x, destination.y, destination.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.15F);

        AndroidData.tryConsumeEnergy(player, energyCost);
        AndroidData.setCooldownUntil(player, AndroidData.Ability.TELEPORT, gameTime + cooldownTicks);
        AndroidData.addExperience(player, 25);
        status(player, String.format("Ender Teleport complete: %.1f blocks.", origin.distanceTo(destination)),
                ChatFormatting.AQUA);
    }

    private static boolean isSafeTeleportDestination(ServerPlayer player, ServerLevel level, Vec3 candidate) {
        BlockPos candidatePos = BlockPos.containing(candidate);
        AABB moved = player.getBoundingBox().move(
                candidate.x - player.getX(),
                candidate.y - player.getY(),
                candidate.z - player.getZ());
        return candidate.y >= level.getMinBuildHeight()
                && candidate.y + player.getBbHeight() < level.getMaxBuildHeight()
                && level.getWorldBorder().isWithinBounds(candidatePos)
                && level.noCollision(player, moved);
    }

    private static void showShieldPulse(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                player.getX(), player.getY() + player.getBbHeight() * 0.5D, player.getZ(),
                18, 0.45D, 0.75D, 0.45D, 0.05D);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.6F, 1.35F);
    }

    private static String formatSeconds(int ticks) {
        return String.format("%.1fs", ticks / 20.0D);
    }

    private static void status(ServerPlayer player, String message, ChatFormatting color) {
        player.displayClientMessage(Component.literal(message).withStyle(color), true);
    }
}
