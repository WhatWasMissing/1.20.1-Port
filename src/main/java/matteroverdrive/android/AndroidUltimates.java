package matteroverdrive.android;

import matteroverdrive.entity.DroneEntity;
import matteroverdrive.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/** Server-authoritative subclass ultimate abilities for the Android class matrix. */
public final class AndroidUltimates {
    public static final int ACTION_ACTIVATE_ULTIMATE = 2;
    public static final int REQUIRED_LEVEL = 4;
    private static final String COOLDOWN_UNTIL = "MatterOverdriveAndroidUltimateUntil";

    private AndroidUltimates() {}

    public static int getRemainingCooldown(ServerPlayer player) {
        long remaining = Math.max(0L, player.getPersistentData().getLong(COOLDOWN_UNTIL) - player.level().getGameTime());
        return remaining > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) remaining;
    }

    public static void activate(ServerPlayer player) {
        if (!AndroidData.isAndroid(player)) {
            status(player, "Android conversion is not active.", ChatFormatting.RED);
            return;
        }
        if (AndroidData.getLevel(player) < REQUIRED_LEVEL) {
            status(player, "Subclass ultimates unlock at Android level " + REQUIRED_LEVEL + ".", ChatFormatting.RED);
            return;
        }

        AndroidLoadout.Specialization specialization = AndroidLoadout.getSpecialization(player);
        AndroidLoadout.Ultimate ultimate = specialization.ultimate;
        int remaining = getRemainingCooldown(player);
        if (remaining > 0) {
            status(player, ultimate.displayName + " recharging: " + formatSeconds(remaining), ChatFormatting.RED);
            return;
        }
        if (AndroidData.getEnergy(player) < ultimate.energyCost) {
            status(player, ultimate.displayName + " needs " + ultimate.energyCost + " FE.", ChatFormatting.RED);
            return;
        }

        AndroidData.tryConsumeEnergy(player, ultimate.energyCost);
        player.getPersistentData().putLong(COOLDOWN_UNTIL, player.level().getGameTime() + ultimate.cooldownTicks);
        switch (specialization) {
            case ASSAULT -> singularityCascade(player);
            case CHASSIS -> citadelProtocol(player);
            case UTILITY -> phaseDominion(player);
            case DRONE_COMMANDER -> overmindAscendant(player);
            case HUNTER_KILLER -> executionLattice(player);
            case PRECISION_FRAME -> railstormProtocol(player);
            case SIEGE_FRAME -> siegeEngine(player);
            case NANITE_WEAVER -> naniteBloom(player);
            case GRAVITY_CORE -> eventHorizon(player);
        }
        AndroidData.addExperience(player, 60);
        status(player, "ULTIMATE // " + ultimate.displayName, ChatFormatting.GOLD);
        ModNetwork.syncAndroidState(player);
    }

    private static List<LivingEntity> hostiles(ServerPlayer player, double radius) {
        return player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius),
                target -> target != player && target.isAlive() && !target.isAlliedTo(player) && !(target instanceof DroneEntity));
    }

    private static void withAbilityDamage(ServerPlayer player, Runnable action) {
        boolean previous = player.getPersistentData().getBoolean(AndroidAbilities.ABILITY_DAMAGE_TAG);
        player.getPersistentData().putBoolean(AndroidAbilities.ABILITY_DAMAGE_TAG, true);
        try {
            action.run();
        } finally {
            if (!previous) player.getPersistentData().remove(AndroidAbilities.ABILITY_DAMAGE_TAG);
        }
    }

    private static void singularityCascade(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<LivingEntity> targets = hostiles(player, 10.0D);
        withAbilityDamage(player, () -> {
            for (LivingEntity target : targets) {
                double distance = Math.max(0.5D, target.distanceTo(player));
                float damage = (float)Math.max(10.0D, 24.0D - distance);
                target.hurt(player.damageSources().playerAttack(player), damage);
                Vec3 push = target.position().subtract(player.position());
                if (push.lengthSqr() > 0.001D) {
                    push = push.normalize().scale(2.1D);
                    target.push(push.x, 0.75D, push.z);
                }
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 1));
            }
        });
        level.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 1.0D, player.getZ(), 18, 4.0D, 1.5D, 4.0D, 0.08D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 120, 5.0D, 2.0D, 5.0D, 0.2D);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.4F, 0.65F);
    }

    private static void citadelProtocol(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 2, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 3, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0, true, true));
        player.serverLevel().sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 80, 1.2D, 1.2D, 1.2D, 0.08D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 0.8F);
    }

    private static void phaseDominion(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 300, 0, true, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 0, true, true));
        for (LivingEntity target : hostiles(player, 18.0D)) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 1));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));
        }
        player.serverLevel().sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 140, 3.0D, 1.5D, 3.0D, 0.35D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.6F);
    }

    private static void overmindAscendant(ServerPlayer player) {
        UUID owner = player.getUUID();
        AABB area = player.getBoundingBox().inflate(32.0D);
        List<DroneEntity> drones = player.level().getEntitiesOfClass(DroneEntity.class, area,
                drone -> owner.equals(drone.getOwnerUuid()) && drone.isAlive());
        for (DroneEntity drone : drones) {
            drone.setHealth(drone.getMaxHealth());
            drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 500, 2, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 500, 2, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 500, 1, true, true));
            player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, drone.getX(), drone.getY() + 0.5D, drone.getZ(), 24, 0.6D, 0.6D, 0.6D, 0.15D);
        }
        for (LivingEntity target : hostiles(player, 32.0D)) target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 240, 0));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 0, true, true));
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 1.25F);
        status(player, "Overmind linked " + drones.size() + " drone(s).", ChatFormatting.AQUA);
    }

    /** Strider / Hunter-Killer: mark the whole hunt radius and brutally reward already exposed targets. */
    private static void executionLattice(ServerPlayer player) {
        List<LivingEntity> targets = hostiles(player, 22.0D);
        withAbilityDamage(player, () -> {
            for (LivingEntity target : targets) {
                boolean marked = target.hasEffect(MobEffects.GLOWING);
                float missingHealth = 1.0F - target.getHealth() / Math.max(1.0F, target.getMaxHealth());
                float damage = 12.0F + (marked ? 10.0F : 0.0F) + missingHealth * 10.0F;
                target.hurt(player.damageSources().playerAttack(player), damage);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 360, 0));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 180, 0));
            }
        });
        AndroidData.receiveEnergy(player, Math.min(6_000, targets.size() * 450));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 180, 2, true, true));
        player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 150, 6.0D, 2.0D, 6.0D, 0.12D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.2F, 0.55F);
    }

    /** Strider / Precision Frame: a forward precision cone rather than another generic radial explosion. */
    private static void railstormProtocol(ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        List<LivingEntity> candidates = hostiles(player, 28.0D);
        withAbilityDamage(player, () -> {
            for (LivingEntity target : candidates) {
                Vec3 toTarget = target.getEyePosition().subtract(player.getEyePosition());
                double distance = toTarget.length();
                if (distance < 0.001D) continue;
                double alignment = look.dot(toTarget.scale(1.0D / distance));
                if (alignment < 0.78D) continue;
                float damage = target.hasEffect(MobEffects.GLOWING) ? 34.0F : 27.0F;
                target.hurt(player.damageSources().playerAttack(player), damage);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 240, 0));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                player.serverLevel().sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 18, 0.25D, 0.4D, 0.25D, 0.12D);
            }
        });
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 140, 1, true, true));
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.9F, 1.6F);
    }

    /** Juggernaut / Siege Frame: temporary heavy-frame state plus an immediate close-range breach. */
    private static void siegeEngine(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 320, 1, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 320, 1, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 320, 2, true, true));
        List<LivingEntity> targets = hostiles(player, 9.0D);
        withAbilityDamage(player, () -> {
            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().playerAttack(player), 18.0F);
                Vec3 push = target.position().subtract(player.position());
                if (push.lengthSqr() > 0.001D) {
                    push = push.normalize().scale(2.4D);
                    target.push(push.x, 0.8D, push.z);
                }
            }
        });
        player.serverLevel().sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.6D, player.getZ(), 16, 3.5D, 0.6D, 3.5D, 0.05D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.2F, 0.55F);
    }

    /** Architect / Nanite Weaver: hostile corrosion and allied repair in the same field. */
    private static void naniteBloom(ServerPlayer player) {
        List<LivingEntity> targets = hostiles(player, 15.0D);
        withAbilityDamage(player, () -> {
            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().playerAttack(player), 8.0F);
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 260, 2));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 260, 1));
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 260, 0));
            }
        });
        player.heal(8.0F);
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1, true, true));
        UUID owner = player.getUUID();
        List<DroneEntity> drones = player.level().getEntitiesOfClass(DroneEntity.class, player.getBoundingBox().inflate(20.0D),
                drone -> owner.equals(drone.getOwnerUuid()) && drone.isAlive());
        for (DroneEntity drone : drones) {
            drone.heal(10.0F);
            drone.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 0, true, true));
        }
        AndroidData.receiveEnergy(player, Math.min(7_000, targets.size() * 500));
        player.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0D, player.getZ(), 140, 5.0D, 1.8D, 5.0D, 0.08D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.45F);
    }

    /** Architect / Gravity Core: pull, suspend and damage rather than simply applying another status burst. */
    private static void eventHorizon(ServerPlayer player) {
        List<LivingEntity> targets = hostiles(player, 18.0D);
        withAbilityDamage(player, () -> {
            for (LivingEntity target : targets) {
                Vec3 pull = player.position().add(0.0D, 1.0D, 0.0D).subtract(target.position());
                if (pull.lengthSqr() > 0.001D) {
                    double scale = Math.min(2.2D, 0.8D + pull.length() * 0.06D);
                    pull = pull.normalize().scale(scale);
                    target.push(pull.x, Math.max(0.15D, pull.y * 0.35D), pull.z);
                }
                target.hurt(player.damageSources().playerAttack(player), 16.0F);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 220, 3));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 220, 1));
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 220, 0));
            }
        });
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 180, 1, true, true));
        player.serverLevel().sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 220, 6.0D, 2.5D, 6.0D, 0.45D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.8F, 0.65F);
    }

    private static String formatSeconds(int ticks) {
        return String.format("%.1fs", ticks / 20.0D);
    }

    private static void status(ServerPlayer player, String message, ChatFormatting color) {
        player.displayClientMessage(Component.literal(message).withStyle(color), true);
    }
}
