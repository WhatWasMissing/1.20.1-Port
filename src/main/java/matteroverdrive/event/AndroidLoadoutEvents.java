package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.android.AndroidAbilities;
import matteroverdrive.android.AndroidData;
import matteroverdrive.android.AndroidLoadout;
import matteroverdrive.entity.DroneEntity;
import matteroverdrive.item.weapon.WeaponBatteryItem;
import matteroverdrive.item.ReactorRemoteItem;
import matteroverdrive.blockentity.FusionReactorControllerBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AndroidLoadoutEvents {
    private static final String REACTIVE_EXOSHELL_UNTIL = "MatterOverdriveReactiveExoshellUntil";

    private AndroidLoadoutEvents() {}

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) { AndroidLoadout.copyTo(event.getOriginal(), event.getEntity()); }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide
                || !(event.player instanceof ServerPlayer player) || !AndroidData.isAndroid(player)) return;

        bonusBatteryCharge(player);
        if (player.tickCount % 20 != 0 || AndroidData.getEnergy(player) <= 0) return;

        boolean conserve = AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.CONSERVATION);
        double costMultiplier = conserve ? 0.65D : 1.0D;
        if (AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.OVERCLOCKED_RELAY)) costMultiplier *= 1.08D;
        if (AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.NANITE_CROWN)) costMultiplier *= 0.80D;
        int capacity = AndroidData.getEnergyCapacity(player);

        if (AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.CAPACITOR_HEART)
                && AndroidData.getEnergy(player) >= capacity / 2) AndroidData.receiveEnergy(player, 250);

        // The reactor remains the source of truth: a Symbiote only accepts FE
        // that a carried Remote can withdraw from an already loaded, running
        // linked controller.  No remote chunk loading or client prediction.
        if (AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.REACTOR_SYMBIOTE)
                && AndroidData.getEnergy(player) < capacity) {
            FusionReactorControllerBlockEntity controller = ReactorRemoteItem.findLinkedRunningController(player);
            if (controller != null) {
                int requested = Math.min(1_200, capacity - AndroidData.getEnergy(player));
                int drawn = controller.drawAndroidUplinkEnergy(requested);
                if (drawn > 0) AndroidData.receiveEnergy(player, drawn);
            }
        }

        if (AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.TEMPORAL_OVERDRIVE)
                && AndroidData.getEnergy(player) >= capacity * 3 / 4
                && AndroidData.tryConsumeEnergy(player, (int)Math.ceil(100 * costMultiplier))) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 35, 2, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 35, 1, true, false, false));
        }

        if (AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.NANITE_BASTION)
                && player.getHealth() < player.getMaxHealth() * 0.55F
                && AndroidData.tryConsumeEnergy(player, (int)Math.ceil(240 * costMultiplier))) {
            float healing = AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.RECOVERY) ? 4.5F : 3.0F;
            if (AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.NANITE_CROWN)) healing += 3.0F;
            if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.OVERFLOW)
                    && AndroidData.getEnergy(player) >= capacity * 3 / 4) healing += 1.0F;
            player.heal(healing);
        }

        if (AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.NANITE_CROWN)
                && player.getHealth() < player.getMaxHealth() * 0.70F && player.tickCount % 40 == 0
                && AndroidData.tryConsumeEnergy(player, 100)) player.heal(2.0F);

        if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.MOBILITY))
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 1, true, false, false));
        if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.VEIL) && AndroidData.isCloakEnabled(player)) {
            int duration = AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.PHASE_ANCHOR) ? 80 : 35;
            int amplifier = AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.PHASE_ANCHOR) ? 3 : 2;
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, amplifier, true, false, false));
        }
        if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.BULWARK) && AndroidData.isShieldEnabled(player))
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 2, true, false, false));

        if (player.tickCount % 40 == 0 && AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.HUNTER_ARRAY)) {
            double radius = 26.0D + (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.PREDATION) ? 10.0D : 0.0D);
            if (AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.HUNTER_LENS)) radius += 18.0D;
            var targets = player.level().getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(radius), Monster::isAlive);
            int cost = (int)Math.ceil(180 * costMultiplier);
            if (!targets.isEmpty() && AndroidData.tryConsumeEnergy(player, cost)) {
                for (Monster target : targets) {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 90, 0, true, false, false));
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, false, false));
                }
            }
        }

        if (player.tickCount % 40 == 0 && AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.BALLISTIC_PREDICTION)
                && player.getDeltaMovement().horizontalDistanceSqr() < 0.02D) {
            List<Monster> targets = player.level().getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(36.0D), Monster::isAlive);
            if (!targets.isEmpty() && AndroidData.tryConsumeEnergy(player, (int)Math.ceil(140 * costMultiplier))) {
                for (Monster target : targets) {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 90, 0, true, false, false));
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 70, 1, true, false, false));
                }
            }
        }

        if (AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.MOBILE_FORTRESS)
                && AndroidData.getEnergy(player) >= capacity / 2) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 1, true, false, false));
            if (player.tickCount % 40 == 0) player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 60, 0, true, false, false));
        }

        if (AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.REPAIR_SWARM)) {
            if (player.getHealth() < player.getMaxHealth() && player.tickCount % 40 == 0
                    && AndroidData.tryConsumeEnergy(player, (int)Math.ceil(140 * costMultiplier))) player.heal(2.0F);
            if (player.tickCount % 20 == 0)
                for (DroneEntity drone : ownedDrones(player, 20.0D)) if (drone.getHealth() < drone.getMaxHealth()) drone.heal(1.5F);
        }

        if (player.tickCount % 40 == 0 && AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.GRAVITIC_WELL)) {
            for (Monster target : player.level().getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(11.0D), Monster::isAlive)) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 2, true, false, false));
                Vec3 pull = player.position().subtract(target.position());
                if (pull.lengthSqr() > 0.001D) { pull = pull.normalize().scale(0.55D); target.push(pull.x, 0.12D, pull.z); }
            }
        }

        applyDroneSupport(player);
    }

    private static void applyDroneSupport(ServerPlayer player) {
        boolean commandAuthority = AndroidLoadout.hasDronePerk(player, AndroidLoadout.DronePerk.COMMAND_AUTHORITY);
        boolean guardianDirective = AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.GUARDIAN_DIRECTIVE);
        double supportRadius = commandAuthority ? 48.0D : guardianDirective ? 40.0D : 30.0D;
        List<DroneEntity> drones = ownedDrones(player, supportRadius);
        if (drones.isEmpty()) return;

        boolean swarmPassive = AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.SWARM_BEACON);
        boolean sentinel = AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.SENTINEL);
        boolean repairBeacon = AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.REPAIR_BEACON);
        boolean fieldRepair = AndroidLoadout.hasDronePerk(player, AndroidLoadout.DronePerk.FIELD_REPAIR);
        boolean overmind = AndroidLoadout.hasDronePerk(player, AndroidLoadout.DronePerk.OVERMIND);
        boolean reinforced = AndroidLoadout.hasDronePerk(player, AndroidLoadout.DronePerk.REINFORCED_DRONES);
        boolean swarmLogic = AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.SWARM_LOGIC);

        for (DroneEntity drone : drones) {
            if (commandAuthority) drone.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 35, 1, true, false, false));
            int resistanceAmplifier = -1;
            if (sentinel || guardianDirective) resistanceAmplifier = Math.max(resistanceAmplifier, 1);
            if (reinforced || overmind || swarmPassive) resistanceAmplifier = Math.max(resistanceAmplifier, 2);
            if (resistanceAmplifier >= 0)
                drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 35, resistanceAmplifier, true, false, false));
            if (drone.getHealth() < drone.getMaxHealth()) {
                float amount = 0.0F;
                if (repairBeacon) amount += 0.5F;
                if (fieldRepair) amount += 1.0F;
                if (swarmPassive) amount += 1.0F;
                if (overmind) amount += 1.0F;
                if (swarmLogic && drones.size() >= 2) amount += 0.75F;
                if (amount <= 0.0F) continue;
                drone.heal(amount);
            }
        }
    }

    private static List<DroneEntity> ownedDrones(ServerPlayer player, double radius) {
        return player.level().getEntitiesOfClass(DroneEntity.class, player.getBoundingBox().inflate(radius),
                drone -> player.getUUID().equals(drone.getOwnerUuid()) && drone.isAlive());
    }

    private static void bonusBatteryCharge(ServerPlayer player) {
        int bonus = 0;
        if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.INDUCTION)) bonus += 384;
        if (AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.RECURSIVE_CORE)) bonus += 512;
        int capacity = AndroidData.getEnergyCapacity(player);
        if (bonus <= 0 || !player.isCrouching() || AndroidData.getEnergy(player) >= capacity) return;
        int remaining = Math.min(bonus, capacity - AndroidData.getEnergy(player));
        for (InteractionHand hand : InteractionHand.values()) {
            if (remaining <= 0) break;
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof WeaponBatteryItem)) continue;
            IEnergyStorage source = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
            if (source == null || !source.canExtract()) continue;
            int extracted = source.extractEnergy(remaining, false);
            int accepted = AndroidData.receiveEnergy(player, extracted);
            if (accepted < extracted && source.canReceive()) source.receiveEnergy(extracted - accepted, false);
            remaining -= accepted;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity sourceEntity = event.getSource().getEntity() instanceof LivingEntity living ? living : null;
        if (sourceEntity instanceof DroneEntity drone && drone.getOwner() instanceof ServerPlayer owner
                && AndroidData.isAndroid(owner) && event.getAmount() > 0.0F) {
            float multiplier = 1.0F;
            boolean ranged = event.getSource().getDirectEntity() instanceof Arrow;
            if (AndroidLoadout.hasDronePerk(owner, AndroidLoadout.DronePerk.TARGETING_SUITE)) multiplier *= 1.18F;
            if (ranged && AndroidLoadout.hasFragment(owner, AndroidLoadout.Fragment.ORDNANCE)) multiplier *= 1.20F;
            if (AndroidLoadout.hasDronePerk(owner, AndroidLoadout.DronePerk.OVERMIND)) multiplier *= 1.25F;
            if (event.getEntity().hasEffect(MobEffects.GLOWING)) {
                if (AndroidLoadout.hasDronePerk(owner, AndroidLoadout.DronePerk.HUNTER_NETWORK)) multiplier *= 1.10F;
                if (AndroidLoadout.hasFragment(owner, AndroidLoadout.Fragment.TARGET_LINK)) multiplier *= 1.20F;
                if (AndroidLoadout.hasArtifact(owner, AndroidLoadout.Artifact.HUNTER_LENS)) multiplier *= 1.20F;
            }
            double supportRadius = AndroidLoadout.hasDronePerk(owner, AndroidLoadout.DronePerk.COMMAND_AUTHORITY) ? 48.0D
                    : AndroidLoadout.hasAspect(owner, AndroidLoadout.Aspect.GUARDIAN_DIRECTIVE) ? 40.0D : 30.0D;
            int nearby = ownedDrones(owner, supportRadius).size();
            float fleetScale = 0.0F;
            if (AndroidLoadout.hasDronePerk(owner, AndroidLoadout.DronePerk.SWARM_COHESION)) fleetScale += 0.055F;
            if (AndroidLoadout.hasFragment(owner, AndroidLoadout.Fragment.PACK_TACTICS)) fleetScale += 0.035F;
            if (nearby >= 2 && fleetScale > 0.0F) multiplier *= 1.0F + Math.min(0.35F, nearby * fleetScale);
            event.setAmount(event.getAmount() * multiplier);
            if (ranged && AndroidLoadout.hasDronePerk(owner, AndroidLoadout.DronePerk.ORDNANCE_LINK))
                event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, true, false, false));
            return;
        }

        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker) || !AndroidData.isAndroid(attacker) || event.getAmount() <= 0.0F) return;
        boolean abilityDamage = attacker.getPersistentData().getBoolean(AndroidAbilities.ABILITY_DAMAGE_TAG);
        float multiplier = 1.0F;
        if (abilityDamage && AndroidLoadout.hasAspect(attacker, AndroidLoadout.Aspect.VANGUARD_PROTOCOL)) multiplier *= 1.25F;
        if (abilityDamage && AndroidLoadout.hasFragment(attacker, AndroidLoadout.Fragment.AMPLITUDE)) multiplier *= 1.15F;
        if (abilityDamage && AndroidLoadout.hasFragment(attacker, AndroidLoadout.Fragment.AFTERSHOCK)) multiplier *= 1.15F;
        if (abilityDamage && AndroidLoadout.hasArtifact(attacker, AndroidLoadout.Artifact.OVERCLOCKED_RELAY)) multiplier *= 1.18F;
        if (AndroidLoadout.hasFragment(attacker, AndroidLoadout.Fragment.SURGE)
                && AndroidData.getEnergy(attacker) >= AndroidData.getEnergyCapacity(attacker) * 3 / 4) multiplier *= 1.10F;
        if (!abilityDamage && AndroidLoadout.hasAspect(attacker, AndroidLoadout.Aspect.VANGUARD_PROTOCOL)) multiplier *= 1.15F;
        if (event.getEntity().hasEffect(MobEffects.GLOWING)) {
            if (AndroidLoadout.hasArtifact(attacker, AndroidLoadout.Artifact.HUNTER_LENS)) multiplier *= 1.15F;
            if (AndroidLoadout.hasAspect(attacker, AndroidLoadout.Aspect.PREDATOR_CHAIN)) multiplier *= 1.20F;
            if (AndroidLoadout.hasAspect(attacker, AndroidLoadout.Aspect.STABILIZED_OPTICS)) multiplier *= 1.22F;
        }
        if (AndroidLoadout.hasAspect(attacker, AndroidLoadout.Aspect.EXECUTION_ROUTER)
                && (event.getEntity().hasEffect(MobEffects.GLOWING) || event.getEntity().getHealth() <= event.getEntity().getMaxHealth() * 0.40F)) multiplier *= 1.20F;
        if (!abilityDamage && AndroidLoadout.hasAspect(attacker, AndroidLoadout.Aspect.HEAVY_ORDNANCE)) multiplier *= 1.20F;
        if (abilityDamage && AndroidLoadout.hasAspect(attacker, AndroidLoadout.Aspect.MASS_DRIVER)) multiplier *= 1.22F;
        event.setAmount(event.getAmount() * multiplier);

        if (abilityDamage && AndroidLoadout.hasFragment(attacker, AndroidLoadout.Fragment.FEEDBACK)) AndroidData.receiveEnergy(attacker, 350);
        if (abilityDamage && AndroidLoadout.hasAspect(attacker, AndroidLoadout.Aspect.RECURSIVE_CORE)) AndroidData.receiveEnergy(attacker, 250);
        if (abilityDamage && AndroidLoadout.hasFragment(attacker, AndroidLoadout.Fragment.HARMONICS)) AndroidData.receiveEnergy(attacker, 200);
        if (event.getEntity().hasEffect(MobEffects.GLOWING) && AndroidLoadout.hasAspect(attacker, AndroidLoadout.Aspect.PREDATOR_CHAIN)) AndroidData.receiveEnergy(attacker, 250);
        if (abilityDamage && AndroidLoadout.hasArtifact(attacker, AndroidLoadout.Artifact.OVERCLOCKED_RELAY)) AndroidData.receiveEnergy(attacker, 150);
        if (abilityDamage && AndroidLoadout.hasArtifact(attacker, AndroidLoadout.Artifact.CAPACITOR_HEART)
                && AndroidData.getEnergy(attacker) >= AndroidData.getEnergyCapacity(attacker) / 2) AndroidData.receiveEnergy(attacker, 400);
        if (abilityDamage && AndroidLoadout.hasFragment(attacker, AndroidLoadout.Fragment.SYNAPSE) && attacker.getRandom().nextFloat() < 0.45F)
            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 70, 2, true, false, false));
        if (abilityDamage && AndroidLoadout.hasAspect(attacker, AndroidLoadout.Aspect.CORROSIVE_CLOUD)) {
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1, true, false, false));
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, true, false, false));
        }
        if (abilityDamage && AndroidLoadout.hasAspect(attacker, AndroidLoadout.Aspect.MASS_DRIVER)) {
            Vec3 push = event.getEntity().position().subtract(attacker.position());
            if (push.lengthSqr() > 0.001D) { push = push.normalize().scale(0.90D); event.getEntity().push(push.x, 0.30D, push.z); }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !AndroidData.isAndroid(player) || event.getAmount() <= 0.0F) return;
        float multiplier = 1.0F;
        if (AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.AEGIS_WEAVE)) multiplier *= 0.82F;
        if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.WARDING)) multiplier *= 0.90F;
        if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.BARRIER) && AndroidData.isShieldEnabled(player)) multiplier *= 0.85F;
        if (AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.AEGIS_PRISM)) {
            multiplier *= 0.92F;
            if (AndroidData.isShieldEnabled(player)) multiplier *= 0.82F;
        }
        if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.RESOLVE) && player.getHealth() <= player.getMaxHealth() * 0.35F) multiplier *= 0.82F;
        if (!ownedDrones(player, 20.0D).isEmpty()) {
            if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.ESCORT)) multiplier *= 0.90F;
            if (AndroidLoadout.hasDronePerk(player, AndroidLoadout.DronePerk.ESCORT_PROTOCOL)) multiplier *= 0.95F;
        }

        if (AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.REACTIVE_EXOSHELL)) {
            long now = player.level().getGameTime();
            long readyAt = player.getPersistentData().getLong(REACTIVE_EXOSHELL_UNTIL);
            if (now >= readyAt) {
                player.getPersistentData().putLong(REACTIVE_EXOSHELL_UNTIL, now + 160);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 2, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 1, true, true));
                player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                        player.getX(), player.getY() + 1.0D, player.getZ(), 36, 0.7D, 0.9D, 0.7D, 0.08D);
            }
        }
        event.setAmount(event.getAmount() * multiplier);
    }
}
