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

    public static String classAbilityDescription(AndroidLoadout.Specialization spec) {
        return switch (spec) {
            case UTILITY -> "Rapid evasive burst with short cloak and acceleration.";
            case HUNTER_KILLER -> "Dash toward your aim direction; marked targets nearby extend the hunter overclock.";
            case PRECISION_FRAME -> "Short controlled reposition that grants brief resistance and a stable firing window.";
            case ASSAULT -> "Launch forward as a powered battering ram and stagger enemies in your path.";
            case CHASSIS -> "Brace the chassis, gain heavy protection and repel nearby hostiles.";
            case SIEGE_FRAME -> "Advance under reinforced plating with strength and knockback resistance.";
            case DRONE_COMMANDER -> "Repair and reinforce linked drones while refreshing their combat telemetry.";
            case NANITE_WEAVER -> "Deploy a synthetic recovery field that repairs you and linked drones.";
            case GRAVITY_CORE -> "Reduce your own inertia, gain mobility and repel nearby enemies with a gravity shear.";
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
            case UTILITY -> "Phase-lock nearby hostiles, slowing and exposing them while you cloak briefly.";
            case HUNTER_KILLER -> "Expose and weaken nearby hostiles, feeding pursuit against marked prey.";
            case PRECISION_FRAME -> "Acquire distant targets in your forward arc and prime them for precision follow-up.";
            case ASSAULT -> "Detonate a close-range kinetic burst that damages and launches enemies.";
            case CHASSIS -> "Emit a defensive pulse that weakens nearby enemies and reinforces your frame.";
            case SIEGE_FRAME -> "Fire a radial heavy-ordnance shock that damages, weakens and throws enemies back.";
            case DRONE_COMMANDER -> "Overclock nearby owned drones with speed, strength and regeneration.";
            case NANITE_WEAVER -> "Corrupt nearby hostiles with nanites while repairing and reinforcing linked drones.";
            case GRAVITY_CORE -> "Pull nearby hostiles inward, damage them and leave them heavily slowed.";
        };
    }

    public static int classEnergyCost(AndroidLoadout.Specialization spec) {
        return switch (spec) {
            case UTILITY -> 5_000; case HUNTER_KILLER -> 5_500; case PRECISION_FRAME -> 5_000;
            case ASSAULT -> 6_500; case CHASSIS -> 6_500; case SIEGE_FRAME -> 7_000;
            case DRONE_COMMANDER -> 5_500; case NANITE_WEAVER -> 6_000; case GRAVITY_CORE -> 6_500;
        };
    }

    public static int classCooldownTicks(AndroidLoadout.Specialization spec) {
        return switch (spec) {
            case UTILITY -> 200; case HUNTER_KILLER -> 220; case PRECISION_FRAME -> 180;
            case ASSAULT -> 260; case CHASSIS -> 320; case SIEGE_FRAME -> 300;
            case DRONE_COMMANDER -> 300; case NANITE_WEAVER -> 360; case GRAVITY_CORE -> 260;
        };
    }

    public static int techEnergyCost(AndroidLoadout.Specialization spec) {
        return switch (spec) {
            case UTILITY -> 7_000; case HUNTER_KILLER -> 7_000; case PRECISION_FRAME -> 8_000;
            case ASSAULT -> 9_000; case CHASSIS -> 8_000; case SIEGE_FRAME -> 10_000;
            case DRONE_COMMANDER -> 7_500; case NANITE_WEAVER -> 8_000; case GRAVITY_CORE -> 9_000;
        };
    }

    public static int techCooldownTicks(AndroidLoadout.Specialization spec) {
        return switch (spec) {
            case UTILITY -> 340; case HUNTER_KILLER -> 360; case PRECISION_FRAME -> 380;
            case ASSAULT -> 400; case CHASSIS -> 360; case SIEGE_FRAME -> 420;
            case DRONE_COMMANDER -> 360; case NANITE_WEAVER -> 400; case GRAVITY_CORE -> 400;
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
        dash(player, 1.75D, 0.18D);
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 45, 0, true, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 90, 1, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 35, 0, true, false));
        particles(player, ParticleTypes.PORTAL, 42, 0.7D, 0.18D);
        sound(player, SoundEvents.ENDERMAN_TELEPORT, 0.7F, 1.55F);
    }

    private static void pursuitDash(ServerPlayer player) {
        dash(player, 2.05D, 0.20D);
        boolean prey = !player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10), e -> hostile(player,e) && e.hasEffect(MobEffects.GLOWING)).isEmpty();
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, prey ? 140 : 80, 1, true, true));
        if (prey) player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 0, true, true));
        particles(player, ParticleTypes.ELECTRIC_SPARK, 40, 0.8D, 0.12D);
    }

    private static void focusStep(ServerPlayer player) {
        dash(player, 1.15D, 0.08D);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 70, 0, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 55, 0, true, false));
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 120, 0, true, false));
        particles(player, ParticleTypes.END_ROD, 24, 0.5D, 0.04D);
    }

    private static void kineticRam(ServerPlayer player) {
        dash(player, 1.65D, 0.25D);
        for (LivingEntity target : hostiles(player, 3.5D)) {
            target.hurt(player.damageSources().playerAttack(player), 6.0F);
            Vec3 push = target.position().subtract(player.position());
            if (push.lengthSqr() > 0.001D) { push = push.normalize().scale(1.2D); target.push(push.x, 0.4D, push.z); }
        }
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 50, 0, true, true));
        particles(player, ParticleTypes.EXPLOSION, 5, 1.0D, 0.02D);
    }

    private static void bastionFrame(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 1, true, true));
        repel(player, 5.0D, 1.15D, 0.35D);
        particles(player, ParticleTypes.ELECTRIC_SPARK, 72, 1.3D, 0.12D);
        sound(player, SoundEvents.SHIELD_BLOCK, 1.0F, 0.7F);
    }

    private static void breachAdvance(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 140, 1, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 140, 0, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 140, 1, true, true));
        dash(player, 1.0D, 0.08D);
        particles(player, ParticleTypes.SMOKE, 50, 1.0D, 0.04D);
    }

    private static void commandRelay(ServerPlayer player) {
        List<DroneEntity> drones = ownedDrones(player, 20.0D);
        for (DroneEntity drone : drones) {
            drone.heal(4.0F);
            drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 180, 1, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 180, 1, true, true));
        }
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 0, true, true));
        AndroidData.receiveEnergy(player, Math.min(2500, drones.size() * 500));
        particles(player, ParticleTypes.ELECTRIC_SPARK, 64, 2.0D, 0.08D);
    }

    private static void restorationWell(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 220, 1, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 220, 1, true, true));
        for (DroneEntity drone : ownedDrones(player, 12.0D)) {
            drone.heal(6.0F);
            drone.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 220, 1, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 220, 0, true, true));
        }
        particles(player, ParticleTypes.END_ROD, 84, 2.2D, 0.06D);
        sound(player, SoundEvents.BEACON_ACTIVATE, 0.85F, 1.3F);
    }

    private static void inertialShift(ServerPlayer player) {
        dash(player, 1.45D, 0.28D);
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1, true, true));
        repel(player, 4.0D, 0.9D, 0.25D);
        particles(player, ParticleTypes.PORTAL, 56, 1.4D, 0.12D);
    }

    private static void phaseSnare(ServerPlayer player) {
        for (LivingEntity target : hostiles(player, 12.0D)) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
        }
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 70, 0, true, false));
        particles(player, ParticleTypes.PORTAL, 70, 3.0D, 0.12D);
    }

    private static void predatorSweep(ServerPlayer player) {
        for (LivingEntity target : hostiles(player, 15.0D)) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 220, 0));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 0));
        }
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 1, true, true));
        AndroidData.receiveEnergy(player, 750);
        particles(player, ParticleTypes.ELECTRIC_SPARK, 64, 4.0D, 0.04D);
    }

    private static void targetDesignator(ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        int marked = 0;
        for (LivingEntity target : hostiles(player, 30.0D)) {
            Vec3 to = target.getEyePosition().subtract(player.getEyePosition()).normalize();
            if (look.dot(to) < 0.72D) continue;
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 260, 0));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
            marked++;
        }
        AndroidData.receiveEnergy(player, Math.min(2000, marked * 300));
        particles(player, ParticleTypes.END_ROD, 36, 1.0D, 0.03D);
    }

    private static void seismicCharge(ServerPlayer player) { damageBurst(player, 6.5D, 10.0F, 1.65D, 0.65D); }

    private static void aegisPulse(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 140, 1, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 140, 1, true, true));
        for (LivingEntity target : hostiles(player, 7.0D)) target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 1));
        repel(player, 7.0D, 0.8D, 0.2D);
        particles(player, ParticleTypes.ELECTRIC_SPARK, 80, 2.0D, 0.08D);
    }

    private static void ordnanceBurst(ServerPlayer player) {
        damageBurst(player, 8.0D, 12.0F, 1.8D, 0.55D);
        for (LivingEntity target : hostiles(player, 8.0D)) target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
    }

    private static void swarmSurge(ServerPlayer player) {
        List<DroneEntity> drones = ownedDrones(player, 24.0D);
        for (DroneEntity drone : drones) {
            drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 220, 1, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 220, 1, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 220, 0, true, true));
        }
        AndroidData.receiveEnergy(player, Math.min(3000, drones.size() * 600));
        particles(player, ParticleTypes.ELECTRIC_SPARK, 90, 2.5D, 0.10D);
    }

    private static void naniteSurge(ServerPlayer player) {
        List<LivingEntity> targets = hostiles(player, 10.0D);
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 140, 1));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 0));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 140, 0));
        }
        for (DroneEntity drone : ownedDrones(player, 16.0D)) {
            drone.heal(4.0F);
            drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 0, true, true));
            drone.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 0, true, true));
        }
        AndroidData.receiveEnergy(player, Math.min(4_000, targets.size() * 500));
        particles(player, ParticleTypes.COMPOSTER, 72, 3.0D, 0.08D);
        sound(player, SoundEvents.BEACON_POWER_SELECT, 0.8F, 1.5F);
    }

    private static void gravityPulse(ServerPlayer player) {
        for (LivingEntity target : hostiles(player, 11.0D)) {
            Vec3 pull = player.position().add(0, 1, 0).subtract(target.position());
            if (pull.lengthSqr() > 0.001D) { pull = pull.normalize().scale(1.1D); target.push(pull.x, 0.15D, pull.z); }
            target.hurt(player.damageSources().playerAttack(player), 7.0F);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 2));
        }
        particles(player, ParticleTypes.PORTAL, 100, 3.5D, 0.18D);
    }

    private static void damageBurst(ServerPlayer player, double radius, float damage, double pushScale, double yPush) {
        boolean previous = player.getPersistentData().getBoolean(AndroidAbilities.ABILITY_DAMAGE_TAG);
        player.getPersistentData().putBoolean(AndroidAbilities.ABILITY_DAMAGE_TAG, true);
        try {
            for (LivingEntity target : hostiles(player, radius)) {
                target.hurt(player.damageSources().playerAttack(player), damage);
                Vec3 push = target.position().subtract(player.position());
                if (push.lengthSqr() > 0.001D) { push = push.normalize().scale(pushScale); target.push(push.x, yPush, push.z); }
            }
        } finally { if (!previous) player.getPersistentData().remove(AndroidAbilities.ABILITY_DAMAGE_TAG); }
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
