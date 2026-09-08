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
import net.minecraft.world.entity.monster.Monster;
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

    public static final int CLOAK_ENERGY_PER_TICK = 80;
    public static final int SHIELD_IDLE_ENERGY_PER_TICK = 20;
    public static final int SHIELD_ENERGY_PER_DAMAGE = 48;
    public static final int SHOCKWAVE_ENERGY = 3_000;
    public static final int SHOCKWAVE_COOLDOWN = 80;
    public static final int TELEPORT_ENERGY = 2_800;
    public static final int TELEPORT_COOLDOWN = 50;

    private static final double SHOCKWAVE_RADIUS = 7.0D;
    private static final float SHOCKWAVE_DAMAGE = 12.0F;
    private static final double TELEPORT_RANGE = 12.0D;

    private AndroidAbilities() {}

    public static void handleAction(ServerPlayer player, int action) {
        if (!AndroidData.isAndroid(player)) { status(player, "Android conversion is not active.", ChatFormatting.RED); return; }
        if (action == ACTION_CYCLE) {
            AndroidData.Ability selected = AndroidData.cycleAbility(player);
            if (selected == null) status(player, "Install a bionic part to unlock an ability.", ChatFormatting.RED);
            else status(player, "Selected: " + selected.displayName, ChatFormatting.AQUA);
            ModNetwork.syncAndroidState(player);
            return;
        }
        if (action != ACTION_ACTIVATE) return;

        AndroidData.Ability ability = AndroidData.getSelectedAbility(player);
        if (!AndroidData.isAbilityUnlocked(player, ability)) {
            if (AndroidData.getLevel(player) < ability.requiredLevel) status(player, ability.displayName + " unlocks at Android level " + ability.requiredLevel + ".", ChatFormatting.RED);
            else status(player, ability.displayName + " requires the " + ability.requiredPart.name().toLowerCase() + " bionic part.", ChatFormatting.RED);
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
        if (!AndroidData.isAndroid(player)) return;
        if (AndroidData.isCloakEnabled(player)) {
            if (!AndroidData.isAbilityUnlocked(player, AndroidData.Ability.CLOAK)
                    || !AndroidData.tryConsumeEnergy(player, AndroidData.scaleAbilityEnergy(player, CLOAK_ENERGY_PER_TICK, AndroidData.Perk.GHOST_PROTOCOL))) {
                AndroidData.setCloakEnabled(player, false);
                status(player, "Cloak disabled: insufficient Android FE.", ChatFormatting.RED);
            } else {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 6, 0, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6, 0, true, false, false));
                if (player.tickCount % 5 == 0) {
                    List<Monster> trackers = player.level().getEntitiesOfClass(Monster.class,
                            player.getBoundingBox().inflate(AndroidData.hasPerk(player, AndroidData.Perk.SILENT_CLOAK) ? 20.0D : 12.0D),
                            mob -> mob.isAlive() && mob.getTarget() == player);
                    for (Monster tracker : trackers) tracker.setTarget(null);
                }
            }
        }
        if (AndroidData.isShieldEnabled(player)
                && (!AndroidData.isAbilityUnlocked(player, AndroidData.Ability.FORCE_FIELD)
                || !AndroidData.tryConsumeEnergy(player, AndroidData.scaleAbilityEnergy(player, SHIELD_IDLE_ENERGY_PER_TICK, AndroidData.Perk.BARRIER_MATRIX)))) {
            AndroidData.setShieldEnabled(player, false);
            status(player, "Force Field disabled: insufficient Android FE.", ChatFormatting.RED);
        }
    }

    public static void applyShield(LivingDamageEvent event, ServerPlayer player) {
        if (!AndroidData.isAndroid(player) || !AndroidData.isShieldEnabled(player)
                || !AndroidData.isAbilityUnlocked(player, AndroidData.Ability.FORCE_FIELD) || event.getAmount() <= 0.0F) return;
        int shieldCostPerDamage = AndroidData.scaleAbilityEnergy(player, SHIELD_ENERGY_PER_DAMAGE, AndroidData.Perk.BARRIER_MATRIX);
        float absorptionRatio = AndroidData.hasPerk(player, AndroidData.Perk.ADAMANT_CHASSIS) ? 0.85F : 0.65F;
        float desiredAbsorption = event.getAmount() * absorptionRatio;
        float affordableAbsorption = AndroidData.getEnergy(player) / (float)shieldCostPerDamage;
        float absorbed = Math.min(desiredAbsorption, affordableAbsorption);
        if (absorbed <= 0.0F) {
            AndroidData.setShieldEnabled(player, false);
            status(player, "Force Field collapsed: Android FE depleted.", ChatFormatting.RED);
            return;
        }
        int energyCost = Math.max(1, (int)Math.ceil(absorbed * shieldCostPerDamage));
        if (!AndroidData.tryConsumeEnergy(player, energyCost)) return;
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
        if (enabled && AndroidData.getEnergy(player) < energyCost) { status(player, "Cloak needs at least " + energyCost + " FE.", ChatFormatting.RED); return; }
        AndroidData.setCloakEnabled(player, enabled);
        if (enabled) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 0, true, false));
            player.serverLevel().sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 36, 0.6D, 0.8D, 0.6D, 0.08D);
        } else {
            int amp = AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.PHASE_NAVIGATOR) ? 2 : 1;
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, amp, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, amp, true, true));
            if (AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.PHASE_NAVIGATOR)) AndroidData.receiveEnergy(player, 750);
            player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 28, 0.5D, 0.6D, 0.5D, 0.05D);
        }
        status(player, "Cloak " + (enabled ? "enabled: target locks broken, Speed II for 3s." : "disabled: ambush Strength/Speed for 4s."), enabled ? ChatFormatting.GREEN : ChatFormatting.YELLOW);
    }

    private static void toggleShield(ServerPlayer player) {
        boolean enabled = !AndroidData.isShieldEnabled(player);
        int energyCost = AndroidData.scaleAbilityEnergy(player, SHIELD_IDLE_ENERGY_PER_TICK, AndroidData.Perk.BARRIER_MATRIX);
        if (enabled && AndroidData.getEnergy(player) < energyCost) { status(player, "Force Field needs Android FE.", ChatFormatting.RED); return; }
        AndroidData.setShieldEnabled(player, enabled);
        if (enabled) {
            showShieldPulse(player);
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1, true, true));
            List<LivingEntity> hostiles = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(4.0D), target -> target != player && target.isAlive() && !target.isAlliedTo(player));
            for (LivingEntity target : hostiles) {
                Vec3 push = target.position().subtract(player.position());
                if (push.lengthSqr() > 0.001D) { push = push.normalize().scale(1.25D); target.push(push.x, 0.3D, push.z); }
            }
        }
        status(player, "Force Field " + (enabled ? "enabled: 65% mitigation + 4 absorption hearts." : "disabled."), enabled ? ChatFormatting.GREEN : ChatFormatting.YELLOW);
    }

    private static void activateShockwave(ServerPlayer player) {
        long gameTime = player.level().getGameTime();
        int energyCost = AndroidData.scaleAbilityEnergy(player, SHOCKWAVE_ENERGY, AndroidData.Perk.SHOCK_RECYCLER);
        boolean lattice = AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.SINGULARITY_LATTICE);
        double radius = SHOCKWAVE_RADIUS + (AndroidData.hasPerk(player, AndroidData.Perk.WIDEBAND_PULSE) ? 4.0D : 0.0D) + (lattice ? 2.0D : 0.0D);
        float damage = SHOCKWAVE_DAMAGE + (AndroidData.hasPerk(player, AndroidData.Perk.RESONANT_PULSE) ? 5.0F : 0.0F)
                + (AndroidData.hasPerk(player, AndroidData.Perk.OVERCHARGED_PULSE) ? 7.0F : 0.0F) + (lattice ? 4.0F : 0.0F);
        int cooldownTicks = AndroidData.hasPerk(player, AndroidData.Perk.APEX_CORE) ? Math.max(1, (int)Math.ceil(SHOCKWAVE_COOLDOWN * 0.50D)) : SHOCKWAVE_COOLDOWN;
        if (AndroidData.hasPerk(player, AndroidData.Perk.COOLDOWN_ROUTER)) cooldownTicks = Math.max(1, (int)Math.ceil(cooldownTicks * 0.80D));
        int cooldown = AndroidData.getRemainingCooldown(player, AndroidData.Ability.SHOCKWAVE, gameTime);
        if (cooldown > 0) { status(player, "Sonic Shockwave cooldown: " + formatSeconds(cooldown), ChatFormatting.RED); return; }
        if (AndroidData.getEnergy(player) < energyCost) { status(player, "Sonic Shockwave needs " + energyCost + " FE.", ChatFormatting.RED); return; }
        AABB area = player.getBoundingBox().inflate(radius, 3.5D, radius);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area, target -> target != player && target.isAlive() && !target.isAlliedTo(player));
        int[] recycledEnergy = {0};
        final float finalDamage = damage;
        withAbilityDamage(player, () -> {
            for (LivingEntity target : targets) {
                Vec3 offset = target.position().subtract(player.position());
                Vec3 horizontal = new Vec3(offset.x, 0.0D, offset.z);
                if (horizontal.lengthSqr() < 0.001D) horizontal = player.getLookAngle();
                horizontal = horizontal.normalize();
                target.hurt(player.damageSources().playerAttack(player), finalDamage);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, lattice ? 120 : 80, lattice ? 2 : 1));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, lattice ? 140 : 100, AndroidData.hasPerk(player, AndroidData.Perk.OVERCHARGED_PULSE) ? 1 : 0));
                if (!target.isAlive() && AndroidData.hasPerk(player, AndroidData.Perk.APEX_CORE)) recycledEnergy[0] += 1_500;
                double push = AndroidData.hasPerk(player, AndroidData.Perk.SHOCK_MOMENTUM) ? 2.6D : 1.75D;
                if (lattice) push += 0.55D;
                target.push(horizontal.x * push, lattice ? 0.85D : 0.65D, horizontal.z * push);
            }
        });
        AndroidData.tryConsumeEnergy(player, energyCost);
        if (recycledEnergy[0] > 0) AndroidData.receiveEnergy(player, recycledEnergy[0]);
        AndroidData.setCooldownUntil(player, AndroidData.Ability.SHOCKWAVE, gameTime + cooldownTicks);
        AndroidData.addExperience(player, 25);
        player.serverLevel().sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.8D, player.getZ(), lattice ? 18 : 12, radius * 0.45D, 0.8D, radius * 0.45D, 0.04D);
        player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 0.8D, player.getZ(), lattice ? 130 : 90, radius * 0.5D, 1.0D, radius * 0.5D, 0.12D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, lattice ? 0.9F : 1.2F);
        status(player, "Sonic Shockwave: " + damage + " damage / " + String.format("%.0f", radius) + " blocks / " + targets.size() + " target(s)" + (recycledEnergy[0] > 0 ? " / +" + recycledEnergy[0] + " FE" : ""), ChatFormatting.AQUA);
    }

    private static void activateTeleport(ServerPlayer player) {
        long gameTime = player.level().getGameTime();
        int energyCost = AndroidData.scaleAbilityEnergy(player, TELEPORT_ENERGY, AndroidData.Perk.BLINK_RECYCLER);
        boolean navigator = AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.PHASE_NAVIGATOR);
        boolean lattice = AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.SINGULARITY_LATTICE);
        boolean translocation = AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.TRANSLOCATION);
        double teleportRange = TELEPORT_RANGE + (AndroidData.hasPerk(player, AndroidData.Perk.PHASE_CAPACITOR) ? 6.0D : 0.0D)
                + (AndroidData.hasPerk(player, AndroidData.Perk.PHASE_STABILIZER) ? 6.0D : 0.0D)
                + (AndroidData.hasPerk(player, AndroidData.Perk.LONG_RANGE_BLINK) ? 16.0D : 0.0D) + (navigator ? 6.0D : 0.0D);
        int cooldownTicks = AndroidData.hasPerk(player, AndroidData.Perk.RAPID_BLINK) ? Math.max(1, (int)Math.ceil(TELEPORT_COOLDOWN * 0.75D)) : TELEPORT_COOLDOWN;
        if (AndroidData.hasPerk(player, AndroidData.Perk.COOLDOWN_ROUTER)) cooldownTicks = Math.max(1, (int)Math.ceil(cooldownTicks * 0.80D));
        if (navigator) cooldownTicks = Math.max(1, (int)Math.ceil(cooldownTicks * 0.80D));
        int cooldown = AndroidData.getRemainingCooldown(player, AndroidData.Ability.TELEPORT, gameTime);
        if (cooldown > 0) { status(player, "Ender Teleport cooldown: " + formatSeconds(cooldown), ChatFormatting.RED); return; }
        if (AndroidData.getEnergy(player) < energyCost) { status(player, "Ender Teleport needs " + energyCost + " FE.", ChatFormatting.RED); return; }

        ServerLevel level = player.serverLevel();
        Vec3 origin = player.position();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        HitResult hit = level.clip(new ClipContext(eye, eye.add(look.scale(teleportRange)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        boolean dimensional = AndroidData.hasPerk(player, AndroidData.Perk.LONG_RANGE_BLINK);
        double clearDistance = dimensional || hit.getType() == HitResult.Type.MISS ? teleportRange : Math.max(0.0D, hit.getLocation().distanceTo(eye) - 0.75D);
        double eyeOffset = eye.y - origin.y;
        Vec3 destination = null;
        for (double distance = clearDistance; distance >= 0.75D; distance -= 0.25D) {
            Vec3 sightPoint = eye.add(look.scale(distance));
            Vec3 candidate = new Vec3(sightPoint.x, sightPoint.y - eyeOffset, sightPoint.z);
            if (isSafeTeleportDestination(player, level, candidate)) { destination = candidate; break; }
        }
        if (destination == null) { status(player, "No safe Ender Teleport destination on the view ray.", ChatFormatting.RED); return; }

        level.sendParticles(ParticleTypes.PORTAL, origin.x, origin.y + player.getBbHeight() * 0.5D, origin.z, dimensional ? 72 : 44, 0.45D, 0.75D, 0.45D, 0.18D);
        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9F, dimensional ? 0.8F : 1.15F);
        player.teleportTo(destination.x, destination.y, destination.z);
        player.fallDistance = 0.0F;
        int mobilityDuration = (navigator ? 100 : 70) + (translocation ? 40 : 0);
        int mobilityAmplifier = (navigator ? 2 : 1) + (translocation ? 1 : 0);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, mobilityDuration, mobilityAmplifier, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, navigator ? 80 : 50, navigator ? 1 : 0, true, true));
        if (navigator) AndroidData.receiveEnergy(player, 1_000);
        level.sendParticles(ParticleTypes.PORTAL, destination.x, destination.y + player.getBbHeight() * 0.5D, destination.z, dimensional ? 72 : 44, 0.45D, 0.75D, 0.45D, 0.18D);
        level.playSound(null, destination.x, destination.y, destination.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9F, dimensional ? 0.8F : 1.15F);

        List<LivingEntity> arrivalTargets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(lattice ? 6.0D : 4.5D), target -> target != player && target.isAlive() && !target.isAlliedTo(player));
        withAbilityDamage(player, () -> {
            for (LivingEntity target : arrivalTargets) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, lattice ? 110 : 70, lattice ? 3 : 2));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, lattice ? 110 : 70, lattice ? 1 : 0));
                if (lattice) {
                    target.hurt(player.damageSources().playerAttack(player), 8.0F);
                    Vec3 push = target.position().subtract(player.position());
                    if (push.lengthSqr() > 0.001D) { push = push.normalize().scale(1.1D); target.push(push.x, 0.35D, push.z); }
                }
            }
        });

        AndroidData.tryConsumeEnergy(player, energyCost);
        AndroidData.setCooldownUntil(player, AndroidData.Ability.TELEPORT, gameTime + cooldownTicks);
        AndroidData.addExperience(player, 25);
        status(player, String.format("Ender Teleport: %.1f blocks / mobility + resistance%s%s%s.", origin.distanceTo(destination),
                navigator ? " / Phase Navigator +1,000 FE" : "", translocation ? " / Translocation boost" : "", lattice ? " / Lattice arrival burst" : ""), ChatFormatting.AQUA);
    }

    public static void withAbilityDamage(ServerPlayer player, Runnable action) {
        boolean previous = player.getPersistentData().getBoolean(ABILITY_DAMAGE_TAG);
        player.getPersistentData().putBoolean(ABILITY_DAMAGE_TAG, true);
        try { action.run(); }
        finally {
            if (previous) player.getPersistentData().putBoolean(ABILITY_DAMAGE_TAG, true);
            else player.getPersistentData().remove(ABILITY_DAMAGE_TAG);
        }
    }

    private static boolean isSafeTeleportDestination(ServerPlayer player, ServerLevel level, Vec3 candidate) {
        BlockPos candidatePos = BlockPos.containing(candidate);
        AABB moved = player.getBoundingBox().move(candidate.x - player.getX(), candidate.y - player.getY(), candidate.z - player.getZ());
        return candidate.y >= level.getMinBuildHeight() && candidate.y + player.getBbHeight() < level.getMaxBuildHeight()
                && level.getWorldBorder().isWithinBounds(candidatePos) && level.noCollision(player, moved);
    }

    private static void showShieldPulse(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + player.getBbHeight() * 0.5D, player.getZ(),
                AndroidData.hasPerk(player, AndroidData.Perk.ADAMANT_CHASSIS) ? 40 : 24, 0.55D, 0.85D, 0.55D, 0.07D);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.7F, 1.25F);
    }

    private static String formatSeconds(int ticks) { return String.format("%.1fs", ticks / 20.0D); }
    private static void status(ServerPlayer player, String message, ChatFormatting color) { player.displayClientMessage(Component.literal(message).withStyle(color), true); }
}
