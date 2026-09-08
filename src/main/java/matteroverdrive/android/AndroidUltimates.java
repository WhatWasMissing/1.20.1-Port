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
        AndroidData.addExperience(player, 75);
        status(player, "ULTIMATE // " + ultimate.displayName, ChatFormatting.GOLD);
        ModNetwork.syncAndroidState(player);
    }

    private static List<LivingEntity> hostiles(ServerPlayer player, double radius) {
        return player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius),
                target -> target != player && target.isAlive() && !target.isAlliedTo(player) && !(target instanceof DroneEntity));
    }

    private static void withAbilityDamage(ServerPlayer player, Runnable action) {
        AndroidAbilities.withAbilityDamage(player, action);
    }

    private static void singularityCascade(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<LivingEntity> targets = hostiles(player, 12.0D);
        withAbilityDamage(player, () -> {
            for (LivingEntity target : targets) {
                double distance = Math.max(0.5D, target.distanceTo(player));
                float damage = (float)Math.max(16.0D, 34.0D - distance * 1.25D);
                target.hurt(player.damageSources().playerAttack(player), damage);
                Vec3 push = target.position().subtract(player.position());
                if (push.lengthSqr() > 0.001D) {
                    push = push.normalize().scale(2.8D);
                    target.push(push.x, 1.0D, push.z);
                }
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 220, 2));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1));
            }
        });
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 1, true, true));
        level.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 1.0D, player.getZ(), 28, 5.0D, 1.8D, 5.0D, 0.10D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 180, 6.0D, 2.5D, 6.0D, 0.24D);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.6F, 0.55F);
    }

    private static void citadelProtocol(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 500, 3, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 500, 4, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 500, 2, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 500, 0, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1, true, true));
        for (LivingEntity target : hostiles(player, 8.0D)) {
            Vec3 push = target.position().subtract(player.position());
            if (push.lengthSqr() > 0.001D) {
                push = push.normalize().scale(2.0D);
                target.push(push.x, 0.6D, push.z);
            }
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 180, 2));
        }
        player.serverLevel().sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 120, 1.8D, 1.5D, 1.8D, 0.10D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 0.7F);
    }

    private static void phaseDominion(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 3, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 400, 0, true, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1, true, true));
        for (LivingEntity target : hostiles(player, 24.0D)) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 400, 0));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 240, 2));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 1));
        }
        AndroidData.receiveEnergy(player, 4_000);
        player.serverLevel().sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 200, 4.0D, 2.0D, 4.0D, 0.40D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.2F, 0.55F);
    }

    private static void overmindAscendant(ServerPlayer player) {
        UUID owner = player.getUUID();
        AABB area = player.getBoundingBox().inflate(40.0D);
        List<DroneEntity> drones = player.level().getEntitiesOfClass(DroneEntity.class, area,
                drone -> owner.equals(drone.getOwnerUuid()) && drone.isAlive());
        for (DroneEntity drone : drones) {
            drone.setHealth(drone.getMaxHealth());
            drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 3, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 2, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 2, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 3, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 2, true, true));
            player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, drone.getX(), drone.getY() + 0.5D, drone.getZ(), 36, 0.8D, 0.8D, 0.8D, 0.18D);
        }
        for (LivingEntity target : hostiles(player, 40.0D)) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 360, 0));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 1));
        }
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 2, true, true));
        AndroidData.receiveEnergy(player, Math.min(8_000, drones.size() * 1_500));
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.2F, 1.15F);
        status(player, "Overmind fully overclocked " + drones.size() + " drone(s).", ChatFormatting.AQUA);
    }

    private static void executionLattice(ServerPlayer player) {
        List<LivingEntity> targets = hostiles(player, 28.0D);
        withAbilityDamage(player, () -> {
            for (LivingEntity target : targets) {
                boolean marked = target.hasEffect(MobEffects.GLOWING);
                float missingHealth = 1.0F - target.getHealth() / Math.max(1.0F, target.getMaxHealth());
                float damage = 18.0F + (marked ? 14.0F : 0.0F) + missingHealth * 14.0F;
                target.hurt(player.damageSources().playerAttack(player), damage);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 420, 0));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 1));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1));
            }
        });
        AndroidData.receiveEnergy(player, Math.min(10_000, targets.size() * 700));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 3, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 180, 2, true, true));
        player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 200, 7.0D, 2.5D, 7.0D, 0.15D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.3F, 0.5F);
    }

    private static void railstormProtocol(ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        List<LivingEntity> candidates = hostiles(player, 40.0D);
        withAbilityDamage(player, () -> {
            for (LivingEntity target : candidates) {
                Vec3 toTarget = target.getEyePosition().subtract(player.getEyePosition());
                double distance = toTarget.length();
                if (distance < 0.001D) continue;
                double alignment = look.dot(toTarget.scale(1.0D / distance));
                if (alignment < 0.68D) continue;
                float damage = target.hasEffect(MobEffects.GLOWING) ? 52.0F : 40.0F;
                target.hurt(player.damageSources().playerAttack(player), damage);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 320, 0));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 2));
                player.serverLevel().sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 28, 0.3D, 0.5D, 0.3D, 0.16D);
            }
        });
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 2, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 0, true, true));
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.1F, 1.5F);
    }

    private static void siegeEngine(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 2, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 2, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 3, true, true));
        List<LivingEntity> targets = hostiles(player, 12.0D);
        withAbilityDamage(player, () -> {
            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().playerAttack(player), 24.0F);
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 180, 1));
                Vec3 push = target.position().subtract(player.position());
                if (push.lengthSqr() > 0.001D) {
                    push = push.normalize().scale(3.0D);
                    target.push(push.x, 1.0D, push.z);
                }
            }
        });
        player.serverLevel().sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.6D, player.getZ(), 24, 4.5D, 0.9D, 4.5D, 0.07D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.4F, 0.5F);
    }

    private static void naniteBloom(ServerPlayer player) {
        List<LivingEntity> targets = hostiles(player, 20.0D);
        withAbilityDamage(player, () -> {
            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().playerAttack(player), 12.0F);
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 340, 3));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 340, 2));
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 340, 0));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 180, 1));
            }
        });
        player.heal(12.0F);
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 2, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 2, true, true));
        UUID owner = player.getUUID();
        List<DroneEntity> drones = player.level().getEntitiesOfClass(DroneEntity.class, player.getBoundingBox().inflate(24.0D),
                drone -> owner.equals(drone.getOwnerUuid()) && drone.isAlive());
        for (DroneEntity drone : drones) {
            drone.heal(14.0F);
            drone.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 2, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1, true, true));
        }
        AndroidData.receiveEnergy(player, Math.min(10_000, targets.size() * 700));
        player.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0D, player.getZ(), 200, 6.0D, 2.2D, 6.0D, 0.10D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.35F);
    }

    private static void eventHorizon(ServerPlayer player) {
        List<LivingEntity> targets = hostiles(player, 22.0D);
        withAbilityDamage(player, () -> {
            for (LivingEntity target : targets) {
                Vec3 pull = player.position().add(0.0D, 1.0D, 0.0D).subtract(target.position());
                if (pull.lengthSqr() > 0.001D) {
                    double scale = Math.min(3.0D, 1.2D + pull.length() * 0.075D);
                    pull = pull.normalize().scale(scale);
                    target.push(pull.x, Math.max(0.20D, pull.y * 0.45D), pull.z);
                }
                target.hurt(player.damageSources().playerAttack(player), 22.0F);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 4));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 2));
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0));
            }
        });
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 2, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 240, 0, true, true));
        player.serverLevel().sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 300, 7.0D, 3.0D, 7.0D, 0.55D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0F, 0.55F);
    }

    private static String formatSeconds(int ticks) {
        return String.format("%.1fs", ticks / 20.0D);
    }

    private static void status(ServerPlayer player, String message, ChatFormatting color) {
        player.displayClientMessage(Component.literal(message).withStyle(color), true);
    }
}
