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

/** Server-authoritative active class and tech abilities for Strider, Juggernaut and Architect frames. */
public final class AndroidClassAbilities {
    public static final int ACTION_CLASS_ABILITY = 3;
    public static final int ACTION_TECH_ABILITY = 4;
    public static final int REQUIRED_LEVEL = 2;

    private static final String CLASS_COOLDOWN = "MatterOverdriveAndroidClassAbilityUntil";
    private static final String TECH_COOLDOWN = "MatterOverdriveAndroidTechAbilityUntil";

    private AndroidClassAbilities() {}

    public static String classAbilityName(AndroidClasses.AndroidClass androidClass) {
        return switch (androidClass) {
            case STRIDER -> "Reflex Shift";
            case JUGGERNAUT -> "Bastion Frame";
            case ARCHITECT -> "Restoration Well";
        };
    }

    public static String classAbilityDescription(AndroidClasses.AndroidClass androidClass) {
        return switch (androidClass) {
            case STRIDER -> "Rapid evasive burst with short cloak and acceleration.";
            case JUGGERNAUT -> "Brace the chassis, gain heavy protection and repel nearby hostiles.";
            case ARCHITECT -> "Deploy a synthetic recovery field that repairs you and linked drones.";
        };
    }

    public static String techAbilityName(AndroidClasses.AndroidClass androidClass) {
        return switch (androidClass) {
            case STRIDER -> "Predator Sweep";
            case JUGGERNAUT -> "Seismic Charge";
            case ARCHITECT -> "Nanite Surge";
        };
    }

    public static String techAbilityDescription(AndroidClasses.AndroidClass androidClass) {
        return switch (androidClass) {
            case STRIDER -> "Expose and weaken nearby hostiles while entering a brief hunter cloak.";
            case JUGGERNAUT -> "Detonate a close-range kinetic burst that damages and launches enemies.";
            case ARCHITECT -> "Corrupt nearby hostiles with nanites while repairing and reinforcing linked drones.";
        };
    }

    public static void activateClassAbility(ServerPlayer player) {
        if (!ready(player, CLASS_COOLDOWN, classCooldownTicks(AndroidClasses.current(player)), classEnergyCost(AndroidClasses.current(player)), classAbilityName(AndroidClasses.current(player)))) return;
        AndroidClasses.AndroidClass androidClass = AndroidClasses.current(player);
        switch (androidClass) {
            case STRIDER -> reflexShift(player);
            case JUGGERNAUT -> bastionFrame(player);
            case ARCHITECT -> restorationWell(player);
        }
        AndroidData.addExperience(player, 15);
        status(player, "CLASS ABILITY // " + classAbilityName(androidClass), ChatFormatting.AQUA);
        ModNetwork.syncAndroidState(player);
    }

    public static void activateTechAbility(ServerPlayer player) {
        if (!ready(player, TECH_COOLDOWN, techCooldownTicks(AndroidClasses.current(player)), techEnergyCost(AndroidClasses.current(player)), techAbilityName(AndroidClasses.current(player)))) return;
        AndroidClasses.AndroidClass androidClass = AndroidClasses.current(player);
        switch (androidClass) {
            case STRIDER -> predatorSweep(player);
            case JUGGERNAUT -> seismicCharge(player);
            case ARCHITECT -> naniteSurge(player);
        }
        AndroidData.addExperience(player, 20);
        status(player, "TECH ABILITY // " + techAbilityName(androidClass), ChatFormatting.LIGHT_PURPLE);
        ModNetwork.syncAndroidState(player);
    }

    public static int getClassCooldown(ServerPlayer player) { return remaining(player, CLASS_COOLDOWN); }
    public static int getTechCooldown(ServerPlayer player) { return remaining(player, TECH_COOLDOWN); }

    private static boolean ready(ServerPlayer player, String key, int cooldownTicks, int energyCost, String name) {
        if (!AndroidData.isAndroid(player)) {
            status(player, "Android conversion is not active.", ChatFormatting.RED);
            return false;
        }
        if (AndroidData.getLevel(player) < REQUIRED_LEVEL) {
            status(player, "Class abilities unlock at Android level " + REQUIRED_LEVEL + ".", ChatFormatting.RED);
            return false;
        }
        int remaining = remaining(player, key);
        if (remaining > 0) {
            status(player, name + " recharging: " + String.format("%.1fs", remaining / 20.0D), ChatFormatting.RED);
            return false;
        }
        if (!AndroidData.tryConsumeEnergy(player, energyCost)) {
            status(player, name + " needs " + energyCost + " FE.", ChatFormatting.RED);
            return false;
        }
        player.getPersistentData().putLong(key, player.level().getGameTime() + cooldownTicks);
        return true;
    }

    private static int remaining(ServerPlayer player, String key) {
        long remaining = Math.max(0L, player.getPersistentData().getLong(key) - player.level().getGameTime());
        return remaining > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) remaining;
    }

    private static int classEnergyCost(AndroidClasses.AndroidClass androidClass) {
        return switch (androidClass) {
            case STRIDER -> 5_000;
            case JUGGERNAUT -> 6_500;
            case ARCHITECT -> 6_000;
        };
    }

    private static int classCooldownTicks(AndroidClasses.AndroidClass androidClass) {
        return switch (androidClass) {
            case STRIDER -> 200;
            case JUGGERNAUT -> 320;
            case ARCHITECT -> 360;
        };
    }

    private static int techEnergyCost(AndroidClasses.AndroidClass androidClass) {
        return switch (androidClass) {
            case STRIDER -> 7_000;
            case JUGGERNAUT -> 9_000;
            case ARCHITECT -> 8_000;
        };
    }

    private static int techCooldownTicks(AndroidClasses.AndroidClass androidClass) {
        return switch (androidClass) {
            case STRIDER -> 360;
            case JUGGERNAUT -> 400;
            case ARCHITECT -> 400;
        };
    }

    private static void reflexShift(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 0.001D) horizontal = new Vec3(0.0D, 0.0D, 1.0D);
        horizontal = horizontal.normalize();
        player.setDeltaMovement(horizontal.x * 1.75D, Math.max(0.18D, player.getDeltaMovement().y), horizontal.z * 1.75D);
        player.fallDistance = 0.0F;
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 45, 0, true, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 90, 1, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 35, 0, true, false));
        player.serverLevel().sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 0.9D, player.getZ(), 42,
                0.5D, 0.6D, 0.5D, 0.18D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7F, 1.55F);
    }

    private static void bastionFrame(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 1, true, true));
        List<LivingEntity> targets = hostiles(player, 5.0D);
        for (LivingEntity target : targets) {
            Vec3 push = target.position().subtract(player.position());
            if (push.lengthSqr() > 0.001D) {
                push = push.normalize().scale(1.15D);
                target.push(push.x, 0.35D, push.z);
            }
        }
        player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 72,
                1.3D, 1.0D, 1.3D, 0.12D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 0.7F);
    }

    private static void restorationWell(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 220, 1, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 220, 1, true, true));
        UUID owner = player.getUUID();
        List<DroneEntity> drones = player.level().getEntitiesOfClass(DroneEntity.class, player.getBoundingBox().inflate(12.0D),
                drone -> owner.equals(drone.getOwnerUuid()) && drone.isAlive());
        for (DroneEntity drone : drones) {
            drone.heal(6.0F);
            drone.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 220, 1, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 220, 0, true, true));
        }
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 0.4D, player.getZ(), 84,
                2.2D, 0.25D, 2.2D, 0.06D);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.85F, 1.3F);
    }

    private static void predatorSweep(ServerPlayer player) {
        List<LivingEntity> targets = hostiles(player, 15.0D);
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));
        }
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, true, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1, true, true));
        player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 64,
                4.0D, 1.0D, 4.0D, 0.04D);
    }

    private static void seismicCharge(ServerPlayer player) {
        List<LivingEntity> targets = hostiles(player, 6.5D);
        boolean previous = player.getPersistentData().getBoolean(AndroidAbilities.ABILITY_DAMAGE_TAG);
        player.getPersistentData().putBoolean(AndroidAbilities.ABILITY_DAMAGE_TAG, true);
        try {
            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().playerAttack(player), 10.0F);
                Vec3 push = target.position().subtract(player.position());
                if (push.lengthSqr() > 0.001D) {
                    push = push.normalize().scale(1.65D);
                    target.push(push.x, 0.65D, push.z);
                }
            }
        } finally {
            if (!previous) player.getPersistentData().remove(AndroidAbilities.ABILITY_DAMAGE_TAG);
        }
        player.serverLevel().sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.5D, player.getZ(), 10,
                2.5D, 0.5D, 2.5D, 0.04D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 0.85F);
    }

    private static void naniteSurge(ServerPlayer player) {
        List<LivingEntity> targets = hostiles(player, 10.0D);
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 140, 1));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 0));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 140, 0));
        }
        UUID owner = player.getUUID();
        List<DroneEntity> drones = player.level().getEntitiesOfClass(DroneEntity.class, player.getBoundingBox().inflate(16.0D),
                drone -> owner.equals(drone.getOwnerUuid()) && drone.isAlive());
        for (DroneEntity drone : drones) {
            drone.heal(4.0F);
            drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 0, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 0, true, true));
        }
        AndroidData.receiveEnergy(player, Math.min(4_000, targets.size() * 500));
        player.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0D, player.getZ(), 72,
                3.0D, 1.2D, 3.0D, 0.08D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8F, 1.5F);
    }

    private static List<LivingEntity> hostiles(ServerPlayer player, double radius) {
        AABB area = player.getBoundingBox().inflate(radius, Math.max(3.0D, radius * 0.4D), radius);
        return player.level().getEntitiesOfClass(LivingEntity.class, area,
                target -> target != player && target.isAlive() && !target.isAlliedTo(player) && !(target instanceof DroneEntity));
    }

    private static void status(ServerPlayer player, String message, ChatFormatting color) {
        player.displayClientMessage(Component.literal(message).withStyle(color), true);
    }
}
