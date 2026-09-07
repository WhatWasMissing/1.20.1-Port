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

/** Server-authoritative class ultimate abilities for the Android subclass loadout. */
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
            status(player, "Class ultimates unlock at Android level " + REQUIRED_LEVEL + ".", ChatFormatting.RED);
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
        }
        AndroidData.addExperience(player, 60);
        status(player, "ULTIMATE // " + ultimate.displayName, ChatFormatting.GOLD);
        ModNetwork.syncAndroidState(player);
    }

    private static void singularityCascade(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        double radius = 10.0D;
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(radius, 4.0D, radius),
                target -> target != player && target.isAlive() && !target.isAlliedTo(player));
        boolean previous = player.getPersistentData().getBoolean(AndroidAbilities.ABILITY_DAMAGE_TAG);
        player.getPersistentData().putBoolean(AndroidAbilities.ABILITY_DAMAGE_TAG, true);
        try {
            for (LivingEntity target : targets) {
                double distance = Math.max(0.5D, target.distanceTo(player));
                float damage = (float) Math.max(10.0D, 24.0D - distance);
                target.hurt(player.damageSources().playerAttack(player), damage);
                Vec3 push = target.position().subtract(player.position());
                if (push.lengthSqr() > 0.001D) {
                    push = push.normalize().scale(2.1D);
                    target.push(push.x, 0.75D, push.z);
                }
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 1));
            }
        } finally {
            if (!previous) player.getPersistentData().remove(AndroidAbilities.ABILITY_DAMAGE_TAG);
        }
        level.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 1.0D, player.getZ(), 18,
                4.0D, 1.5D, 4.0D, 0.08D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 120,
                5.0D, 2.0D, 5.0D, 0.2D);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.4F, 0.65F);
    }

    private static void citadelProtocol(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 2, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 3, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0, true, true));
        player.serverLevel().sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 80,
                1.2D, 1.2D, 1.2D, 0.08D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 0.8F);
    }

    private static void phaseDominion(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 300, 0, true, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 0, true, true));
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(18.0D), target -> target != player && target.isAlive() && !target.isAlliedTo(player));
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 1));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));
        }
        player.serverLevel().sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 140,
                3.0D, 1.5D, 3.0D, 0.35D);
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
            player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    drone.getX(), drone.getY() + 0.5D, drone.getZ(), 24, 0.6D, 0.6D, 0.6D, 0.15D);
        }
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                target -> target != player && target.isAlive() && !target.isAlliedTo(player) && !(target instanceof DroneEntity));
        for (LivingEntity target : targets) target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 240, 0));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 0, true, true));
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 1.25F);
        status(player, "Overmind linked " + drones.size() + " drone(s).", ChatFormatting.AQUA);
    }

    private static String formatSeconds(int ticks) {
        return String.format("%.1fs", ticks / 20.0D);
    }

    private static void status(ServerPlayer player, String message, ChatFormatting color) {
        player.displayClientMessage(Component.literal(message).withStyle(color), true);
    }
}
