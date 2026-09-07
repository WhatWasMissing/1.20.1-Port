package matteroverdrive.android;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.entity.DroneEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;

/**
 * Three top-level Android combat identities inspired by the classic agile / frontline / technomancer RPG split.
 * Existing specialisations remain underneath these classes so older loadouts keep working.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AndroidClasses {
    public enum AndroidClass {
        STRIDER("Strider", "Agile hunter frame", "Mobility, cloak, target acquisition and precision positioning.",
                AndroidLoadout.Specialization.UTILITY),
        JUGGERNAUT("Juggernaut", "Front-line titan frame", "Shockwave pressure, force fields, powered melee and extreme durability.",
                AndroidLoadout.Specialization.CHASSIS),
        ARCHITECT("Architect", "Synthetic warlock frame", "Energy recursion, nanites, battlefield control and drone command.",
                AndroidLoadout.Specialization.DRONE_COMMANDER);

        public final String displayName;
        public final String subtitle;
        public final String description;
        public final AndroidLoadout.Specialization defaultSpecialization;

        AndroidClass(String displayName, String subtitle, String description,
                     AndroidLoadout.Specialization defaultSpecialization) {
            this.displayName = displayName;
            this.subtitle = subtitle;
            this.description = description;
            this.defaultSpecialization = defaultSpecialization;
        }
    }

    private AndroidClasses() {}

    public static AndroidClass fromSpecialization(AndroidLoadout.Specialization specialization) {
        return switch (specialization) {
            case UTILITY -> AndroidClass.STRIDER;
            case ASSAULT, CHASSIS -> AndroidClass.JUGGERNAUT;
            case DRONE_COMMANDER -> AndroidClass.ARCHITECT;
        };
    }

    public static AndroidLoadout.Specialization[] specializations(AndroidClass androidClass) {
        return switch (androidClass) {
            case STRIDER -> new AndroidLoadout.Specialization[]{AndroidLoadout.Specialization.UTILITY};
            case JUGGERNAUT -> new AndroidLoadout.Specialization[]{AndroidLoadout.Specialization.ASSAULT, AndroidLoadout.Specialization.CHASSIS};
            case ARCHITECT -> new AndroidLoadout.Specialization[]{AndroidLoadout.Specialization.DRONE_COMMANDER};
        };
    }

    public static AndroidClass current(net.minecraft.world.entity.player.Player player) {
        return fromSpecialization(AndroidLoadout.getSpecialization(player));
    }

    /** Lightweight intrinsic passives. Build perks and fragments remain the stronger modifiers. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide || !(event.player instanceof ServerPlayer player)) return;
        if (!AndroidData.isAndroid(player) || player.tickCount % 20 != 0) return;

        AndroidClass androidClass = current(player);
        switch (androidClass) {
            case STRIDER -> {
                if (AndroidData.getEnergy(player) >= 500 && AndroidData.tryConsumeEnergy(player, 30)) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 50, 0, true, false, false));
                }
            }
            case JUGGERNAUT -> {
                if (AndroidData.getEnergy(player) >= 800 && AndroidData.tryConsumeEnergy(player, 40)) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 50, 0, true, false, false));
                }
            }
            case ARCHITECT -> {
                AndroidData.receiveEnergy(player, 60);
                UUID owner = player.getUUID();
                List<DroneEntity> drones = player.level().getEntitiesOfClass(DroneEntity.class,
                        player.getBoundingBox().inflate(16.0D), drone -> owner.equals(drone.getOwnerUuid()) && drone.isAlive());
                for (DroneEntity drone : drones) {
                    drone.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50, 0, true, false, false));
                }
            }
        }
    }

    /** Class identity also changes ordinary player combat rather than only active abilities. */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || !AndroidData.isAndroid(player)) return;
        if (!(event.getEntity() instanceof LivingEntity target) || target == player) return;
        if (player.getPersistentData().getBoolean(AndroidAbilities.ABILITY_DAMAGE_TAG)) return;

        switch (current(player)) {
            case STRIDER -> {
                float multiplier = target.hasEffect(MobEffects.GLOWING) ? 1.20F : 1.08F;
                if (player.isSprinting()) multiplier += 0.07F;
                event.setAmount(event.getAmount() * multiplier);
                if (target.hasEffect(MobEffects.GLOWING)) AndroidData.receiveEnergy(player, 120);
            }
            case JUGGERNAUT -> {
                if (AndroidData.tryConsumeEnergy(player, 220)) {
                    event.setAmount(event.getAmount() + 3.0F);
                    Vec3 push = target.position().subtract(player.position());
                    if (push.lengthSqr() > 0.001D) {
                        push = push.normalize().scale(0.65D);
                        target.push(push.x, 0.20D, push.z);
                    }
                }
            }
            case ARCHITECT -> {
                AndroidData.receiveEnergy(player, 180);
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 0, true, false, false));
                if (player.tickCount % 4 == 0) {
                    UUID owner = player.getUUID();
                    List<DroneEntity> drones = player.level().getEntitiesOfClass(DroneEntity.class,
                            player.getBoundingBox().inflate(12.0D), drone -> owner.equals(drone.getOwnerUuid()) && drone.isAlive());
                    for (DroneEntity drone : drones) drone.heal(0.5F);
                }
            }
        }
    }
}
