package matteroverdrive.android;

import matteroverdrive.entity.DroneEntity;
import matteroverdrive.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
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

/** Server-authoritative H/N abilities. Every one of the nine Android subclasses has its own pair. */
public final class AndroidClassAbilities {
    public static final int ACTION_CLASS_ABILITY = 3;
    public static final int ACTION_TECH_ABILITY = 4;
    public static final int REQUIRED_LEVEL = 2;

    private static final String CLASS_COOLDOWN = "MatterOverdriveAndroidClassAbilityUntil";
    private static final String TECH_COOLDOWN = "MatterOverdriveAndroidTechAbilityUntil";

    private AndroidClassAbilities() {}

    public static String classAbilityName(AndroidLoadout.Specialization spec) {
        return switch (spec) {
            case UTILITY -> "Reflex Shift";
            case HUNTER_KILLER -> "Pursuit Dash";
            case PRECISION_FRAME -> "Focus Step";
            case ASSAULT -> "Kinetic Ram";
            case CHASSIS -> "Bastion Frame";
            case SIEGE_FRAME -> "Breach Advance";
            case DRONE_COMMANDER -> "Command Relay";
            case NANITE_WEAVER -> "Restoration Well";
            case GRAVITY_CORE -> "Inertial Shift";
        };
    }

    /** Player-facing descriptions deliberately use the same values as the gameplay implementation below. */
    public static String classAbilityDescription(AndroidLoadout.Specialization spec) {
        return switch (spec) {
            case UTILITY -> "Burst forward. Gain 3s cloak, 6s Speed III and 3s Resistance II.";
            case HUNTER_KILLER -> "Fast pursuit dash. Gain 6s Speed III; if a glowing target is within 10 blocks, gain 10s Speed III and 8s Strength II.";
            case PRECISION_FRAME -> "Controlled step with 5s Resistance II, 4s Speed II and 10s Night Vision for a safer firing window.";
            case ASSAULT -> "Powered ram: dash forward and deal 9 damage to hostiles within 4.5 blocks, launching them away.";
            case CHASSIS -> "Brace for 10s with Resistance III and Absorption III, then repel hostiles within 6 blocks.";
            case SIEGE_FRAME -> "Advance under 10s Resistance III, Strength II and Absorption III while surging forward.";
            case DRONE_COMMANDER -> "Within 20 blocks, heal owned drones by 8 HP and grant 15s Resistance II + Speed III. Refund up to 4,000 FE from linked drones.";
            case NANITE_WEAVER -> "Gain 14s Regeneration III + Absorption III. Owned drones within 12 blocks heal 10 HP and gain regeneration + resistance.";
            case GRAVITY_CORE -> "High-mobility dash with 8s Slow Falling + Speed III, followed by a 5-block gravity repulse.";
        };
    }

    public static String techAbilityName(AndroidLoadout.Specialization spec) {
        return switch (spec) {
            case UTILITY -> "Phase Snare";
            case HUNTER_KILLER -> "Predator Sweep";
            case PRECISION_FRAME -> "Target Designator";
            case ASSAULT -> "Seismic Charge";
            case CHASSIS -> "Aegis Pulse";
            case SIEGE_FRAME -> "Ordnance Burst";
            case DRONE_COMMANDER -> "Swarm Surge";
            case NANITE_WEAVER -> "Nanite Surge";
            case GRAVITY_CORE -> "Gravity Pulse";
        };
    }

    public static String techAbilityDescription(AndroidLoadout.Specialization spec) {
        return switch (spec) {
            case UTILITY -> "Mark hostiles within 14 blocks for 10s and apply Slowness IV for 8s; you cloak for 5s.";
            case HUNTER_KILLER -> "Mark hostiles within 18 blocks for 14s, apply Weakness II for 10s and gain Speed III for 8s. Refund 1,500 FE.";
            case PRECISION_FRAME -> "Mark targets in a wide forward cone out to 36 blocks for 16s and apply Weakness II for 8s. Refund 500 FE per target, up to 4,000.";
            case ASSAULT -> "Detonate an 8-block kinetic burst for 14 damage with strong horizontal and vertical knockback.";
            case CHASSIS -> "Gain 10s Resistance III + Absorption III; enemies within 8 blocks suffer Weakness II and are repelled.";
            case SIEGE_FRAME -> "Heavy 10-block radial blast for 16 damage, strong knockback and 6s Weakness II.";
            case DRONE_COMMANDER -> "Overclock owned drones within 24 blocks for 16s with Strength III, Speed III and Regeneration II. Refund up to 4,000 FE.";
            case NANITE_WEAVER -> "Hostiles within 12 blocks take 10s Poison III + Weakness II. Nearby drones heal 6 HP and gain 10s combat regeneration.";
            case GRAVITY_CORE -> "Pull hostiles within 13 blocks inward, deal 10 ability damage and apply Slowness IV for 9s.";
        };
    }

    public static int classEnergyCost(AndroidLoadout.Specialization spec) {
        return switch (spec) {
            case UTILITY -> 3_000; case HUNTER_KILLER -> 3_250; case PRECISION_FRAME -> 2_750;
            case ASSAULT -> 4_000; case CHASSIS -> 4_250; case SIEGE_FRAME -> 4_500;
            case DRONE_COMMANDER -> 3_500; case NANITE_WEAVER -> 4_000; case GRAVITY_CORE -> 3_750;
        };
    }

    public static int classCooldownTicks(AndroidLoadout.Specialization spec) {
        return switch (spec) {
            case UTILITY -> 140; case HUNTER_KILLER -> 150; case PRECISION_FRAME -> 120;
            case ASSAULT -> 180; case CHASSIS -> 220; case SIEGE_FRAME -> 200;
            case DRONE_COMMANDER -> 200; case NANITE_WEAVER -> 240; case GRAVITY_CORE -> 180;
        };
    }

    public static int techEnergyCost(AndroidLoadout.Specialization spec) {
        return switch (spec) {
            case UTILITY -> 4_500; case HUNTER_KILLER -> 4_750; case PRECISION_FRAME -> 5_000;
            case ASSAULT -> 6_000; case CHASSIS -> 5_500; case SIEGE_FRAME -> 6_500;
            case DRONE_COMMANDER -> 5_000; case NANITE_WEAVER -> 5_250; case GRAVITY_CORE -> 5_750;
        };
    }

    public static int techCooldownTicks(AndroidLoadout.Specialization spec) {
        return switch (spec) {
            case UTILITY -> 240; case HUNTER_KILLER -> 260; case PRECISION_FRAME -> 260;
            case ASSAULT -> 300; case CHASSIS -> 280; case SIEGE_FRAME -> 320;
            case DRONE_COMMANDER -> 260; case NANITE_WEAVER -> 300; case GRAVITY_CORE -> 300;
        };
    }

    public static void activateClassAbility(ServerPlayer player) {
        AndroidLoadout.Specialization spec = AndroidLoadout.getSpecialization(player);
        if (!ready(player, CLASS_COOLDOWN, classCooldownTicks(spec), classEnergyCost(spec), classAbilityName(spec))) return;
        switch (spec) {
            case UTILITY -> reflexShift(player);
            case HUNTER_KILLER -> pursuitDash(player);
            case PRECISION_FRAME -> focusStep(player);
            case ASSAULT -> kineticRam(player);
            case CHASSIS -> bastionFrame(player);
            case SIEGE_FRAME -> breachAdvance(player);
            case DRONE_COMMANDER -> commandRelay(player);
            case NANITE_WEAVER -> restorationWell(player);
            case GRAVITY_CORE -> inertialShift(player);
        }
        AndroidData.addExperience(player, 15);
        status(player, "CLASS ABILITY // " + classAbilityName(spec), ChatFormatting.AQUA);
        ModNetwork.syncAndroidState(player);
    }

    public static void activateTechAbility(ServerPlayer player) {
        AndroidLoadout.Specialization spec = AndroidLoadout.getSpecialization(player);
        if (!ready(player, TECH_COOLDOWN, techCooldownTicks(spec), techEnergyCost(spec), techAbilityName(spec))) return;
        switch (spec) {
            case UTILITY -> phaseSnare(player);
            case HUNTER_KILLER -> predatorSweep(player);
            case PRECISION_FRAME -> targetDesignator(player);
            case ASSAULT -> seismicCharge(player);
            case CHASSIS -> aegisPulse(player);
            case SIEGE_FRAME -> ordnanceBurst(player);
            case DRONE_COMMANDER -> swarmSurge(player);
            case NANITE_WEAVER -> naniteSurge(player);
            case GRAVITY_CORE -> gravityPulse(player);
        }
        AndroidData.addExperience(player, 20);
        status(player, "TECH ABILITY // " + techAbilityName(spec), ChatFormatting.LIGHT_PURPLE);
        ModNetwork.syncAndroidState(player);
    }

    public static int getClassCooldown(ServerPlayer player) { return remaining(player, CLASS_COOLDOWN); }
    public static int getTechCooldown(ServerPlayer player) { return remaining(player, TECH_COOLDOWN); }

    private static boolean ready(ServerPlayer player, String key, int cooldownTicks, int energyCost, String name) {
        if (!AndroidData.isAndroid(player)) { status(player, "Android conversion is not active.", ChatFormatting.RED); return false; }
        if (AndroidData.getLevel(player) < REQUIRED_LEVEL) { status(player, "Subclass abilities unlock at Android level " + REQUIRED_LEVEL + ".", ChatFormatting.RED); return false; }
        int remaining = remaining(player, key);
        if (remaining > 0) { status(player, name + " recharging: " + String.format("%.1fs", remaining / 20.0D), ChatFormatting.RED); return false; }
        if (!AndroidData.tryConsumeEnergy(player, energyCost)) { status(player, name + " needs " + energyCost + " FE.", ChatFormatting.RED); return false; }
        player.getPersistentData().putLong(key, player.level().getGameTime() + cooldownTicks);
        return true;
    }

    private static int remaining(ServerPlayer player, String key) {
        long value = Math.max(0L, player.getPersistentData().getLong(key) - player.level().getGameTime());
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)value;
    }

    private static Vec3 horizontalLook(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        return horizontal.lengthSqr() < 0.001D ? new Vec3(0, 0, 1) : horizontal.normalize();
    }

    private static void dash(ServerPlayer player, double speed, double y) {
        Vec3 v = horizontalLook(player);
        player.setDeltaMovement(v.x * speed, Math.max(y, player.getDeltaMovement().y), v.z * speed);
        player.fallDistance = 0;
    }

    private static void reflexShift(ServerPlayer player) {
        dash(player, 1.95D, 0.20D);
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, true, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 2, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 1, true, false));
        particles(player, ParticleTypes.PORTAL, 42, 0.7D, 0.18D);
        sound(player, SoundEvents.ENDERMAN_TELEPORT, 0.7F, 1.55F);
    }

    private static void pursuitDash(ServerPlayer player) {
        dash(player, 2.35D, 0.22D);
        boolean prey = !player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10), e -> hostile(player,e) && e.hasEffect(MobEffects.GLOWING)).isEmpty();
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, prey ? 200 : 120, 2, true, true));
        if (prey) player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 1, true, true));
        particles(player, ParticleTypes.ELECTRIC_SPARK, 40, 0.8D, 0.12D);
    }

    private static void focusStep(ServerPlayer player) {
        dash(player, 1.35D, 0.10D);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 1, true, false));
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200, 0, true, false));
        particles(player, ParticleTypes.END_ROD, 24, 0.5D, 0.04D);
    }

    private static void kineticRam(ServerPlayer player) {
        dash(player, 1.85D, 0.28D);
        AndroidAbilities.withAbilityDamage(player, () -> {
            for (LivingEntity target : hostiles(player, 4.5D)) {
                target.hurt(player.damageSources().playerAttack(player), 9.0F);
                Vec3 push = target.position().subtract(player.position());
                if (push.lengthSqr() > 0.001D) { push = push.normalize().scale(1.45D); target.push(push.x, 0.5D, push.z); }
            }
        });
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 70, 1, true, true));
        particles(player, ParticleTypes.EXPLOSION, 7, 1.2D, 0.02D);
    }

    private static void bastionFrame(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2, true, true));
        repel(player, 6.0D, 1.35D, 0.4D);
        particles(player, ParticleTypes.ELECTRIC_SPARK, 72, 1.3D, 0.12D);
        sound(player, SoundEvents.SHIELD_BLOCK, 1.0F, 0.7F);
    }

    private static void breachAdvance(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2, true, true));
        dash(player, 1.25D, 0.10D);
        particles(player, ParticleTypes.SMOKE, 50, 1.0D, 0.04D);
    }

    private static void commandRelay(ServerPlayer player) {
        List<DroneEntity> drones = ownedDrones(player, 20.0D);
        for (DroneEntity drone : drones) {
            drone.heal(8.0F);
            drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2, true, true));
        }
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 140, 1, true, true));
        AndroidData.receiveEnergy(player, Math.min(4_000, drones.size() * 750));
        particles(player, ParticleTypes.ELECTRIC_SPARK, 64, 2.0D, 0.08D);
    }

    private static void restorationWell(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 280, 2, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 280, 2, true, true));
        for (DroneEntity drone : ownedDrones(player, 12.0D)) {
            drone.heal(10.0F);
            drone.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 280, 1, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 280, 1, true, true));
        }
        particles(player, ParticleTypes.END_ROD, 84, 2.2D, 0.06D);
        sound(player, SoundEvents.BEACON_ACTIVATE, 0.85F, 1.3F);
    }

    private static void inertialShift(ServerPlayer player) {
        dash(player, 1.80D, 0.32D);
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 160, 0, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 2, true, true));
        repel(player, 5.0D, 1.05D, 0.3D);
        particles(player, ParticleTypes.PORTAL, 56, 1.4D, 0.12D);
    }

    private static void phaseSnare(ServerPlayer player) {
        for (LivingEntity target : hostiles(player, 14.0D)) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 3));
        }
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, true, false));
        particles(player, ParticleTypes.PORTAL, 70, 3.0D, 0.12D);
    }

    private static void predatorSweep(ServerPlayer player) {
        for (LivingEntity target : hostiles(player, 18.0D)) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 280, 0));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
        }
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 2, true, true));
        AndroidData.receiveEnergy(player, 1_500);
        particles(player, ParticleTypes.ELECTRIC_SPARK, 72, 4.5D, 0.04D);
    }

    private static void targetDesignator(ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        int marked = 0;
        for (LivingEntity target : hostiles(player, 36.0D)) {
            Vec3 to = target.getEyePosition().subtract(player.getEyePosition()).normalize();
            if (look.dot(to) < 0.62D) continue;
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 320, 0));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
            marked++;
        }
        AndroidData.receiveEnergy(player, Math.min(4_000, marked * 500));
        particles(player, ParticleTypes.END_ROD, 42, 1.0D, 0.03D);
    }

    private static void seismicCharge(ServerPlayer player) { damageBurst(player, 8.0D, 14.0F, 1.85D, 0.75D); }

    private static void aegisPulse(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2, true, true));
        for (LivingEntity target : hostiles(player, 8.0D)) target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
        repel(player, 8.0D, 1.0D, 0.25D);
        particles(player, ParticleTypes.ELECTRIC_SPARK, 80, 2.0D, 0.08D);
    }

    private static void ordnanceBurst(ServerPlayer player) {
        damageBurst(player, 10.0D, 16.0F, 2.0D, 0.65D);
        for (LivingEntity target : hostiles(player, 10.0D)) target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
    }

    private static void swarmSurge(ServerPlayer player) {
        List<DroneEntity> drones = ownedDrones(player, 24.0D);
        for (DroneEntity drone : drones) {
            drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 320, 2, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 320, 2, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 320, 1, true, true));
        }
        AndroidData.receiveEnergy(player, Math.min(4_000, drones.size() * 750));
        particles(player, ParticleTypes.ELECTRIC_SPARK, 90, 2.5D, 0.10D);
    }

    private static void naniteSurge(ServerPlayer player) {
        List<LivingEntity> targets = hostiles(player, 12.0D);
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
        }
        for (DroneEntity drone : ownedDrones(player, 16.0D)) {
            drone.heal(6.0F);
            drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1, true, true));
        }
        AndroidData.receiveEnergy(player, Math.min(5_000, targets.size() * 600));
        particles(player, ParticleTypes.COMPOSTER, 72, 3.0D, 0.08D);
        sound(player, SoundEvents.BEACON_POWER_SELECT, 0.8F, 1.5F);
    }

    private static void gravityPulse(ServerPlayer player) {
        AndroidAbilities.withAbilityDamage(player, () -> {
            for (LivingEntity target : hostiles(player, 13.0D)) {
                Vec3 pull = player.position().add(0, 1, 0).subtract(target.position());
                if (pull.lengthSqr() > 0.001D) { pull = pull.normalize().scale(1.25D); target.push(pull.x, 0.18D, pull.z); }
                target.hurt(player.damageSources().playerAttack(player), 10.0F);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 180, 3));
            }
        });
        particles(player, ParticleTypes.PORTAL, 100, 3.5D, 0.18D);
    }

    private static void damageBurst(ServerPlayer player, double radius, float damage, double pushScale, double yPush) {
        AndroidAbilities.withAbilityDamage(player, () -> {
            for (LivingEntity target : hostiles(player, radius)) {
                target.hurt(player.damageSources().playerAttack(player), damage);
                Vec3 push = target.position().subtract(player.position());
                if (push.lengthSqr() > 0.001D) { push = push.normalize().scale(pushScale); target.push(push.x, yPush, push.z); }
            }
        });
        particles(player, ParticleTypes.EXPLOSION, 10, Math.max(1.5D, radius / 2.5D), 0.04D);
        sound(player, SoundEvents.GENERIC_EXPLODE, 1.0F, 0.85F);
    }

    private static void repel(ServerPlayer player, double radius, double scale, double y) {
        for (LivingEntity target : hostiles(player, radius)) {
            Vec3 push = target.position().subtract(player.position());
            if (push.lengthSqr() > 0.001D) { push = push.normalize().scale(scale); target.push(push.x, y, push.z); }
        }
    }

    private static boolean hostile(ServerPlayer player, LivingEntity target) {
        return target != player && target.isAlive() && !target.isAlliedTo(player) && !(target instanceof DroneEntity);
    }

    private static List<LivingEntity> hostiles(ServerPlayer player, double radius) {
        AABB area = player.getBoundingBox().inflate(radius, Math.max(3.0D, radius * 0.4D), radius);
        return player.level().getEntitiesOfClass(LivingEntity.class, area, target -> hostile(player, target));
    }

    private static List<DroneEntity> ownedDrones(ServerPlayer player, double radius) {
        UUID owner = player.getUUID();
        return player.level().getEntitiesOfClass(DroneEntity.class, player.getBoundingBox().inflate(radius), d -> owner.equals(d.getOwnerUuid()) && d.isAlive());
    }

    private static void particles(ServerPlayer player, net.minecraft.core.particles.ParticleOptions type, int count, double spread, double speed) {
        player.serverLevel().sendParticles(type, player.getX(), player.getY() + 1.0D, player.getZ(), count, spread, spread * 0.45D, spread, speed);
    }

    private static void sound(ServerPlayer player, net.minecraft.sounds.SoundEvent event, float volume, float pitch) {
        player.serverLevel().playSound(null, player.blockPosition(), event, SoundSource.PLAYERS, volume, pitch);
    }

    private static void status(ServerPlayer player, String message, ChatFormatting color) {
        player.displayClientMessage(Component.literal(message).withStyle(color), true);
    }
}
