package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.android.AndroidAbilities;
import matteroverdrive.android.AndroidData;
import matteroverdrive.android.AndroidLoadout;
import matteroverdrive.entity.DroneEntity;
import matteroverdrive.item.weapon.WeaponBatteryItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
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
    private AndroidLoadoutEvents() {}

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        AndroidLoadout.copyTo(event.getOriginal(), event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide
                || !(event.player instanceof ServerPlayer player) || !AndroidData.isAndroid(player)) return;

        bonusBatteryCharge(player);

        if (player.tickCount % 20 != 0 || AndroidData.getEnergy(player) <= 0) return;

        boolean conserve = AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.CONSERVATION);
        double costMultiplier = conserve ? 0.70D : 1.0D;
        if (AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.OVERCLOCKED_RELAY)) costMultiplier *= 1.10D;
        if (AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.NANITE_CROWN)) costMultiplier *= 0.92D;

        if (AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.TEMPORAL_OVERDRIVE)
                && AndroidData.getEnergy(player) >= AndroidData.ENERGY_CAPACITY * 3 / 4
                && AndroidData.tryConsumeEnergy(player, (int)Math.ceil(80 * costMultiplier))) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 1, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30, 0, true, false, false));
        }

        if (AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.NANITE_BASTION)
                && player.getHealth() < player.getMaxHealth() * 0.50F
                && AndroidData.tryConsumeEnergy(player, (int)Math.ceil(220 * costMultiplier))) {
            float healing = AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.RECOVERY) ? 2.25F : 1.5F;
            if (AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.NANITE_CROWN)) healing += 1.25F;
            if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.OVERFLOW)
                    && AndroidData.getEnergy(player) >= AndroidData.ENERGY_CAPACITY * 3 / 4) healing += 0.75F;
            player.heal(healing);
        }

        if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.MOBILITY)) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 0, true, false, false));
        }
        if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.VEIL)
                && AndroidData.isCloakEnabled(player)) {
            int duration = AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.PHASE_ANCHOR) ? 50 : 30;
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 1, true, false, false));
        }
        if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.BULWARK)
                && AndroidData.isShieldEnabled(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 1, true, false, false));
        }

        if (player.tickCount % 40 == 0
                && AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.HUNTER_ARRAY)) {
            double radius = 24.0D + (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.PREDATION) ? 8.0D : 0.0D);
            if (AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.HUNTER_LENS)) radius += 12.0D;
            var targets = player.level().getEntitiesOfClass(Monster.class,
                    player.getBoundingBox().inflate(radius), Monster::isAlive);
            int cost = (int)Math.ceil(180 * costMultiplier);
            if (!targets.isEmpty() && AndroidData.tryConsumeEnergy(player, cost)) {
                for (Monster target : targets) {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 55, 0, true, false, false));
                }
            }
        }

        applyDroneSupport(player);
    }

    private static void applyDroneSupport(ServerPlayer player) {
        List<DroneEntity> drones = ownedDrones(player, 28.0D);
        if (drones.isEmpty()) return;

        boolean sentinel = AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.SENTINEL);
        boolean repair = AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.REPAIR_BEACON)
                || AndroidLoadout.hasDronePerk(player, AndroidLoadout.DronePerk.FIELD_REPAIR)
                || AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.SWARM_BEACON);
        boolean reinforced = AndroidLoadout.hasDronePerk(player, AndroidLoadout.DronePerk.REINFORCED_DRONES)
                || AndroidLoadout.hasDronePerk(player, AndroidLoadout.DronePerk.OVERMIND)
                || AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.GUARDIAN_DIRECTIVE);

        for (DroneEntity drone : drones) {
            if (sentinel || reinforced) {
                int amplifier = AndroidLoadout.hasDronePerk(player, AndroidLoadout.DronePerk.OVERMIND) ? 1 : 0;
                drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 35, amplifier, true, false, false));
            }
            if (repair && drone.getHealth() < drone.getMaxHealth()) {
                float amount = AndroidLoadout.hasDronePerk(player, AndroidLoadout.DronePerk.OVERMIND) ? 1.5F : 0.75F;
                if (AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.SWARM_LOGIC) && drones.size() >= 2) amount += 0.5F;
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
        if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.INDUCTION)) bonus += 256;
        if (AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.RECURSIVE_CORE)) bonus += 256;
        if (bonus <= 0 || !player.isCrouching() || AndroidData.getEnergy(player) >= AndroidData.ENERGY_CAPACITY) return;

        int remaining = Math.min(bonus, AndroidData.ENERGY_CAPACITY - AndroidData.getEnergy(player));
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
            if (AndroidLoadout.hasDronePerk(owner, AndroidLoadout.DronePerk.TARGETING_SUITE)) multiplier *= 1.12F;
            if (AndroidLoadout.hasFragment(owner, AndroidLoadout.Fragment.ORDNANCE)) multiplier *= 1.15F;
            if (AndroidLoadout.hasDronePerk(owner, AndroidLoadout.DronePerk.ORDNANCE_LINK)) multiplier *= 1.18F;
            if (AndroidLoadout.hasAspect(owner, AndroidLoadout.Aspect.COMMAND_UPLINK)) multiplier *= 1.10F;
            if (AndroidLoadout.hasArtifact(owner, AndroidLoadout.Artifact.SWARM_BEACON)) multiplier *= 1.08F;
            if (AndroidLoadout.hasDronePerk(owner, AndroidLoadout.DronePerk.OVERMIND)) multiplier *= 1.20F;
            if (event.getEntity().hasEffect(MobEffects.GLOWING)
                    && (AndroidLoadout.hasDronePerk(owner, AndroidLoadout.DronePerk.HUNTER_NETWORK)
                    || AndroidLoadout.hasFragment(owner, AndroidLoadout.Fragment.TARGET_LINK)
                    || AndroidLoadout.hasArtifact(owner, AndroidLoadout.Artifact.HUNTER_LENS))) multiplier *= 1.15F;
            int nearby = ownedDrones(owner, 28.0D).size();
            if (nearby >= 2 && (AndroidLoadout.hasDronePerk(owner, AndroidLoadout.DronePerk.SWARM_COHESION)
                    || AndroidLoadout.hasFragment(owner, AndroidLoadout.Fragment.PACK_TACTICS)
                    || AndroidLoadout.hasAspect(owner, AndroidLoadout.Aspect.SWARM_LOGIC))) {
                multiplier *= 1.0F + Math.min(0.25F, nearby * 0.04F);
            }
            event.setAmount(event.getAmount() * multiplier);
            return;
        }

        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)
                || !AndroidData.isAndroid(attacker) || event.getAmount() <= 0.0F) return;

        boolean abilityDamage = attacker.getPersistentData().getBoolean(AndroidAbilities.ABILITY_DAMAGE_TAG);
        float multiplier = 1.0F;
        if (abilityDamage && AndroidLoadout.hasAspect(attacker, AndroidLoadout.Aspect.VANGUARD_PROTOCOL)) multiplier *= 1.20F;
        if (abilityDamage && AndroidLoadout.hasFragment(attacker, AndroidLoadout.Fragment.AMPLITUDE)) multiplier *= 1.08F;
        if (abilityDamage && AndroidLoadout.hasFragment(attacker, AndroidLoadout.Fragment.AFTERSHOCK)) multiplier *= 1.10F;
        if (abilityDamage && AndroidLoadout.hasArtifact(attacker, AndroidLoadout.Artifact.OVERCLOCKED_RELAY)) multiplier *= 1.05F;
        if (AndroidLoadout.hasFragment(attacker, AndroidLoadout.Fragment.SURGE)
                && AndroidData.getEnergy(attacker) >= AndroidData.ENERGY_CAPACITY * 3 / 4) multiplier *= 1.05F;
        if (!abilityDamage && AndroidLoadout.hasAspect(attacker, AndroidLoadout.Aspect.VANGUARD_PROTOCOL)) multiplier *= 1.10F;
        event.setAmount(event.getAmount() * multiplier);

        if (abilityDamage && AndroidLoadout.hasFragment(attacker, AndroidLoadout.Fragment.FEEDBACK)) {
            AndroidData.receiveEnergy(attacker, 150);
        }
        if (abilityDamage && AndroidLoadout.hasAspect(attacker, AndroidLoadout.Aspect.RECURSIVE_CORE)) {
            AndroidData.receiveEnergy(attacker, 100);
        }
        if (abilityDamage && AndroidLoadout.hasFragment(attacker, AndroidLoadout.Fragment.HARMONICS)) {
            AndroidData.receiveEnergy(attacker, 75);
        }
        if (abilityDamage && AndroidLoadout.hasArtifact(attacker, AndroidLoadout.Artifact.CAPACITOR_HEART)
                && AndroidData.getEnergy(attacker) >= AndroidData.ENERGY_CAPACITY / 2) {
            AndroidData.receiveEnergy(attacker, 100);
        }
        if (abilityDamage && AndroidLoadout.hasFragment(attacker, AndroidLoadout.Fragment.SYNAPSE)
                && attacker.getRandom().nextFloat() < 0.30F) {
            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 50, 1, true, false, false));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !AndroidData.isAndroid(player) || event.getAmount() <= 0.0F) return;
        float multiplier = 1.0F;
        if (AndroidLoadout.hasAspect(player, AndroidLoadout.Aspect.AEGIS_WEAVE)) multiplier *= 0.88F;
        if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.WARDING)) multiplier *= 0.95F;
        if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.BARRIER)
                && AndroidData.isShieldEnabled(player)) multiplier *= 0.90F;
        if (AndroidLoadout.hasArtifact(player, AndroidLoadout.Artifact.AEGIS_PRISM)
                && AndroidData.isShieldEnabled(player)) multiplier *= 0.95F;
        if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.RESOLVE)
                && player.getHealth() <= player.getMaxHealth() * 0.35F) multiplier *= 0.90F;
        if (AndroidLoadout.hasFragment(player, AndroidLoadout.Fragment.ESCORT)
                || AndroidLoadout.hasDronePerk(player, AndroidLoadout.DronePerk.ESCORT_PROTOCOL)) {
            if (!ownedDrones(player, 18.0D).isEmpty()) multiplier *= 0.94F;
        }
        event.setAmount(event.getAmount() * multiplier);
    }
}
