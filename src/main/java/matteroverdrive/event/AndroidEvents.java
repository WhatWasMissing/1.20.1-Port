package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.android.AndroidAbilities;
import matteroverdrive.android.AndroidData;
import matteroverdrive.item.weapon.WeaponBatteryItem;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.TickEvent;
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
    private static final UUID OUT_OF_POWER_SPEED_ID =
            UUID.fromString("ec778ddc-9711-498b-b9aa-8e5adc436e00");
    private static final AttributeModifier OUT_OF_POWER_SPEED = new AttributeModifier(
            OUT_OF_POWER_SPEED_ID,
            "Matter Overdrive Android out of power",
            -0.5D,
            AttributeModifier.Operation.MULTIPLY_TOTAL
    );

    private AndroidEvents() {
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        AndroidData.copyTo(event.getOriginal(), event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        if (event.player instanceof ServerPlayer player) {
            chargeFromHeldBattery(player);
            AndroidAbilities.tick(player);
            updatePowerState(player);
            if (player.tickCount % 2 == 0) {
                ModNetwork.syncAndroidState(player);
            }
        }

        if (event.player.tickCount % 20 != 0
                || !AndroidData.isAndroid(event.player)
                || AndroidData.getEnergy(event.player) <= 0) {
            return;
        }

        int activeParts = Integer.bitCount(AndroidData.getParts(event.player));
        if (activeParts == 0 || !AndroidData.tryConsumeEnergy(event.player, activeParts * 5)) {
            return;
        }

        if (AndroidData.hasPart(event.player, AndroidData.Part.HEAD)) {
            event.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 45, 0, true, false, false));
        }
        if (AndroidData.hasPart(event.player, AndroidData.Part.CHEST)) {
            event.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 45, 0, true, false, false));
        }
        if (AndroidData.hasPart(event.player, AndroidData.Part.LEGS)) {
            event.player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 45, 0, true, false, false));
        }
    }

    private static void chargeFromHeldBattery(ServerPlayer player) {
        if (!AndroidData.isAndroid(player)
                || !player.isCrouching()
                || AndroidData.getEnergy(player) >= AndroidData.ENERGY_CAPACITY) {
            return;
        }

        int remaining = Math.min(HANDHELD_CHARGE_PER_TICK,
                AndroidData.ENERGY_CAPACITY - AndroidData.getEnergy(player));
        for (InteractionHand hand : InteractionHand.values()) {
            if (remaining <= 0) {
                break;
            }
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof WeaponBatteryItem)) {
                continue;
            }
            IEnergyStorage source = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
            if (source == null || !source.canExtract()) {
                continue;
            }

            int offered = source.extractEnergy(remaining, true);
            int extracted = source.extractEnergy(offered, false);
            int received = AndroidData.receiveEnergy(player, extracted);
            if (received < extracted && source.canReceive()) {
                source.receiveEnergy(extracted - received, false);
            }
            remaining -= received;
        }
    }

    private static void updatePowerState(ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        boolean outOfPower = AndroidData.isAndroid(player) && AndroidData.getEnergy(player) <= 0;
        if (outOfPower && speed.getModifier(OUT_OF_POWER_SPEED_ID) == null) {
            speed.addTransientModifier(OUT_OF_POWER_SPEED);
        } else if (!outOfPower && speed.getModifier(OUT_OF_POWER_SPEED_ID) != null) {
            speed.removeModifier(OUT_OF_POWER_SPEED_ID);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer defender) {
            AndroidAbilities.applyShield(event, defender);
        }

        if (event.getSource().getEntity() instanceof ServerPlayer attacker
                && event.getSource().getDirectEntity() == attacker
                && !attacker.getPersistentData().getBoolean(AndroidAbilities.ABILITY_DAMAGE_TAG)
                && AndroidData.hasPart(attacker, AndroidData.Part.ARMS)
                && AndroidData.tryConsumeEnergy(attacker, 80)) {
            event.setAmount(event.getAmount() + 3.0F);
        }
    }

    @SubscribeEvent
    public static void onRogueAndroidDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Husk husk)
                || !husk.getPersistentData().getBoolean(ROGUE_ANDROID_TAG)) {
            return;
        }
        AndroidData.Part[] parts = AndroidData.Part.values();
        AndroidData.Part part = parts[husk.getRandom().nextInt(parts.length)];
        event.getDrops().add(new ItemEntity(husk.level(), husk.getX(), husk.getY(), husk.getZ(),
                new ItemStack(ModItems.get(part.itemId).get())));
    }
}
