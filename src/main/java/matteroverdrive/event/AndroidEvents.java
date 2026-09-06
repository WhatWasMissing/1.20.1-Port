package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.android.AndroidAbilities;
import matteroverdrive.android.AndroidData;
import matteroverdrive.item.weapon.WeaponBatteryItem;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AndroidEvents {
    public static final String ROGUE_ANDROID_TAG = "MatterOverdriveRogueAndroid";
    public static final int HANDHELD_CHARGE_PER_TICK = 1_024;
    private static final UUID OUT_OF_POWER_SPEED_ID = UUID.fromString("ec778ddc-9711-498b-b9aa-8e5adc436e00");
    private static final AttributeModifier OUT_OF_POWER_SPEED = new AttributeModifier(
            OUT_OF_POWER_SPEED_ID, "Matter Overdrive Android out of power", -0.5D,
            AttributeModifier.Operation.MULTIPLY_TOTAL);

    private AndroidEvents() {}

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        AndroidData.copyTo(event.getOriginal(), event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        if (event.player instanceof ServerPlayer player) {
            chargeFromHeldBattery(player);
            AndroidAbilities.tick(player);
            updatePowerState(player);
            if (player.tickCount % 2 == 0) ModNetwork.syncAndroidState(player);
        }

        if (event.player.tickCount % 20 != 0
                || !AndroidData.isAndroid(event.player)
                || AndroidData.getEnergy(event.player) <= 0) return;

        applyAndroidBaseline(event.player);
        runUtilityPerks(event.player);

        int activeParts = Integer.bitCount(AndroidData.getParts(event.player));
        int passiveCost = activeParts * 5;
        if (AndroidData.hasPerk(event.player, AndroidData.Perk.SUSTAINED_SYSTEMS)) {
            passiveCost = Math.max(1, (int)Math.ceil(passiveCost * 0.60D));
        }
        if (activeParts == 0 || !AndroidData.tryConsumeEnergy(event.player, passiveCost)) return;

        if (AndroidData.hasPart(event.player, AndroidData.Part.HEAD)) {
            event.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0, true, false, false));
        }
        if (AndroidData.hasPart(event.player, AndroidData.Part.CHEST)) {
            event.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 45, 0, true, false, false));
        }
        if (AndroidData.hasPart(event.player, AndroidData.Part.LEGS)) {
            int amplifier = AndroidData.hasPerk(event.player, AndroidData.Perk.NEURAL_ACCELERATOR) ? 1 : 0;
            event.player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 45, amplifier, true, false, false));
            if (AndroidData.hasPerk(event.player, AndroidData.Perk.NEURAL_ACCELERATOR)) {
                event.player.addEffect(new MobEffectInstance(MobEffects.JUMP, 45, 0, true, false, false));
            }
            if (AndroidData.hasPerk(event.player, AndroidData.Perk.ADAPTIVE_ARMOR)
                    && event.player.getHealth() < event.player.getMaxHealth() * 0.60F) {
                event.player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 45, 2, true, false, false));
            }
        }
    }

    private static void applyAndroidBaseline(net.minecraft.world.entity.player.Player player) {
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(5.0F);
        player.setAirSupply(player.getMaxAirSupply());
        for (MobEffectInstance effect : java.util.List.copyOf(player.getActiveEffects())) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) player.removeEffect(effect.getEffect());
        }
        if (player.isInWater() && !player.getAbilities().flying) {
            player.setDeltaMovement(player.getDeltaMovement().add(0.0D, -0.035D, 0.0D));
        }
    }

    private static void runUtilityPerks(net.minecraft.world.entity.player.Player player) {
        if (AndroidData.hasPerk(player, AndroidData.Perk.SELF_REPAIR)
                && player.getHealth() < player.getMaxHealth()
                && AndroidData.tryConsumeEnergy(player, 250)) {
            player.heal(AndroidData.hasPerk(player, AndroidData.Perk.SYNTHETIC_PERFECTION) ? 2.0F : 1.0F);
        }

        if (AndroidData.hasPerk(player, AndroidData.Perk.SYNTHETIC_PERFECTION)
                && AndroidData.getEnergy(player) >= AndroidData.ENERGY_CAPACITY * 3 / 4
                && player.getHealth() < player.getMaxHealth()
                && AndroidData.tryConsumeEnergy(player, 125)) {
            player.heal(0.5F);
        }

        if (AndroidData.hasPerk(player, AndroidData.Perk.EMERGENCY_PROTOCOL)
                && player.getHealth() <= player.getMaxHealth() * 0.30F
                && AndroidData.tryConsumeEnergy(player, 500)) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 45, 2, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 45, 1, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 45, 1, true, false, false));
        }

        if (player.tickCount % 40 == 0 && AndroidData.hasPerk(player, AndroidData.Perk.TACTICAL_SCAN)) {
            double radius = AndroidData.hasPerk(player, AndroidData.Perk.SYNTHETIC_PERFECTION) ? 28.0D : 22.0D;
            var targets = player.level().getEntitiesOfClass(Monster.class,
                    player.getBoundingBox().inflate(radius), Monster::isAlive);
            if (!targets.isEmpty() && AndroidData.tryConsumeEnergy(player, 250)) {
                for (Monster target : targets) {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 65, 0, true, false, false));
                }
            }
        }
    }

    private static void chargeFromHeldBattery(ServerPlayer player) {
        if (!AndroidData.isAndroid(player) || !player.isCrouching()
                || AndroidData.getEnergy(player) >= AndroidData.ENERGY_CAPACITY) return;

        int chargeRate = AndroidData.hasPerk(player, AndroidData.Perk.QUICK_CHARGE)
                ? HANDHELD_CHARGE_PER_TICK * 3 : HANDHELD_CHARGE_PER_TICK;
        int remaining = Math.min(chargeRate, AndroidData.ENERGY_CAPACITY - AndroidData.getEnergy(player));
        for (InteractionHand hand : InteractionHand.values()) {
            if (remaining <= 0) break;
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof WeaponBatteryItem)) continue;
            IEnergyStorage source = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
            if (source == null || !source.canExtract()) continue;
            int offered = source.extractEnergy(remaining, true);
            int extracted = source.extractEnergy(offered, false);
            int received = AndroidData.receiveEnergy(player, extracted);
            if (received < extracted && source.canReceive()) source.receiveEnergy(extracted - received, false);
            remaining -= received;
        }
    }

    private static void updatePowerState(ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        boolean outOfPower = AndroidData.isAndroid(player) && AndroidData.getEnergy(player) <= 0;
        if (outOfPower && speed.getModifier(OUT_OF_POWER_SPEED_ID) == null) speed.addTransientModifier(OUT_OF_POWER_SPEED);
        else if (!outOfPower && speed.getModifier(OUT_OF_POWER_SPEED_ID) != null) speed.removeModifier(OUT_OF_POWER_SPEED_ID);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer damaged
                && AndroidData.isAndroid(damaged) && AndroidData.getEnergy(damaged) > 0 && event.getAmount() > 0.0F) {
            damaged.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    damaged.getX(), damaged.getY() + damaged.getBbHeight() * 0.55D, damaged.getZ(),
                    10, 0.3D, 0.45D, 0.3D, 0.04D);
            damaged.level().playSound(null, damaged.getX(), damaged.getY(), damaged.getZ(),
                    SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.PLAYERS, 0.25F, 1.7F);
        }

        if (event.getSource().getEntity() instanceof ServerPlayer attacker
                && event.getSource().getDirectEntity() == attacker
                && !attacker.getPersistentData().getBoolean(AndroidAbilities.ABILITY_DAMAGE_TAG)
                && AndroidData.hasPart(attacker, AndroidData.Part.ARMS)
                && AndroidData.tryConsumeEnergy(attacker, 80)) {
            float perkDamage = AndroidData.hasPerk(attacker, AndroidData.Perk.COMBAT_SERVOS) ? 5.0F : 0.0F;
            event.setAmount(event.getAmount() + 3.0F + perkDamage);
            if (perkDamage > 0.0F && event.getEntity() != attacker) {
                event.getEntity().push(attacker.getLookAngle().x * 0.45D, 0.12D, attacker.getLookAngle().z * 0.45D);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer defender) {
            AndroidAbilities.applyShield(event, defender);
            if (AndroidData.isAndroid(defender)) event.setAmount(event.getAmount() * AndroidData.incomingDamageMultiplier(defender));
        }
    }

    @SubscribeEvent
    public static void onRogueAndroidDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Husk husk) || !husk.getPersistentData().getBoolean(ROGUE_ANDROID_TAG)) return;
        AndroidData.Part[] parts = AndroidData.Part.values();
        AndroidData.Part part = parts[husk.getRandom().nextInt(parts.length)];
        event.getDrops().add(new ItemEntity(husk.level(), husk.getX(), husk.getY(), husk.getZ(),
                new ItemStack(ModItems.get(part.itemId).get())));
    }
}
