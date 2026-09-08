package matteroverdrive.entity;

import matteroverdrive.MatterOverdrive;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * Source-backed Drone identity corrections that intentionally leave the richer
 * 1.20 ownership/mode/combat backend intact.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DroneParityEvents {
    private static final String LEGACY_STATS_APPLIED = "MatterOverdriveDroneLegacyStats";

    private DroneParityEvents() {}

    /**
     * DamageSource#getEntity resolves an indirect projectile back to its
     * shooter. Cancelling here therefore protects the linked operator and
     * same-owner fleet members even when an arrow has already left the drone.
     * This is deliberately event-side as a second line of defence in addition
     * to DroneEntity#hurt, since the owner is usually a vanilla Player and does
     * not get the drone-specific hurt override.
     */
    @SubscribeEvent
    public static void preventFleetFriendlyFire(LivingAttackEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof DroneEntity drone)) return;

        UUID owner = drone.getOwnerUuid();
        if (owner == null) return;

        Entity victim = event.getEntity();
        if (owner.equals(victim.getUUID())) {
            event.setCanceled(true);
            return;
        }

        if (victim instanceof DroneEntity other && owner.equals(other.getOwnerUuid())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void livingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof DroneEntity drone)) return;

        if (!drone.level().isClientSide && !drone.getPersistentData().getBoolean(LEGACY_STATS_APPLIED)) {
            var health = drone.getAttribute(Attributes.MAX_HEALTH);
            if (health != null) health.setBaseValue(12.0D);
            var armor = drone.getAttribute(Attributes.ARMOR);
            if (armor != null) armor.setBaseValue(0.0D);
            if (drone.getHealth() > 12.0F) drone.setHealth(12.0F);
            drone.getPersistentData().putBoolean(LEGACY_STATS_APPLIED, true);
        }

        if (!drone.level().isClientSide || (drone.tickCount & 1) != 0) return;
        Vec3 motion = drone.getDeltaMovement();
        double speed = motion.length();
        int particles = Math.min(6, (int) (speed * 30.0D));
        if (particles <= 0) return;

        Vec3 look = drone.getLookAngle();
        double x = drone.getX() - look.x * 0.25D;
        double y = drone.getY() + drone.getEyeHeight() + 0.2D - look.y * 0.2D;
        double z = drone.getZ() - look.z * 0.2D;
        for (int i = 0; i < particles; i++) {
            drone.level().addParticle(ParticleTypes.ENCHANTED_HIT, x, y, z,
                    -motion.x * 2.0D, -motion.y * 2.0D, -motion.z * 2.0D);
        }
    }
}
