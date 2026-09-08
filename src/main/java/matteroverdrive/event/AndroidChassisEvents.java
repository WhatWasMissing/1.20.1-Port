package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.android.AndroidChassisData;
import matteroverdrive.android.AndroidData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID)
public final class AndroidChassisEvents {
    private AndroidChassisEvents() {}

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && AndroidData.isAndroid(player)) {
            event.setAmount(event.getAmount() * AndroidChassisData.incomingDamageMultiplier(player));
        }
        if (event.getSource().getEntity() instanceof Player attacker && AndroidData.isAndroid(attacker)) {
            float multiplier = AndroidChassisData.attackMultiplier(attacker);
            if (AndroidChassisData.has(attacker, AndroidChassisData.Module.OVERCLOCK_CORE)
                    && AndroidData.getEnergy(attacker) > 0) multiplier *= 1.10F;
            event.setAmount(event.getAmount() * multiplier);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide || !AndroidData.isAndroid(event.player)) return;
        Player player = event.player;
        long time = player.level().getGameTime();

        if (time % 20L == 0L) {
            if (AndroidChassisData.has(player, AndroidChassisData.Module.CAPACITOR_CORE)) {
                AndroidData.receiveEnergy(player, 250);
            }
            if (AndroidChassisData.has(player, AndroidChassisData.Module.OVERCLOCK_CORE)
                    && AndroidData.tryConsumeEnergy(player, 350)) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30, 0, true, false));
            }
            if (AndroidChassisData.has(player, AndroidChassisData.Module.AGILITY_MUSCLES)) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 0, true, false));
            }
            if (AndroidChassisData.has(player, AndroidChassisData.Module.LIGHTWEIGHT_FRAME)) {
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 30, 0, true, false));
            }
            if (AndroidChassisData.has(player, AndroidChassisData.Module.SIEGE_MUSCLES)) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30, 0, true, false));
            }
            if (AndroidChassisData.has(player, AndroidChassisData.Module.STEALTH_SHELL)
                    && player.isCrouching() && AndroidData.tryConsumeEnergy(player, 120)) {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, true, false));
            }
            if (AndroidChassisData.has(player, AndroidChassisData.Module.REACTIVE_SHELL)
                    && player.getHealth() <= player.getMaxHealth() * 0.5F) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 0, true, false));
            }
        }

        if (time % 40L == 0L && AndroidChassisData.has(player, AndroidChassisData.Module.HUNTER_OPTICS)
                && AndroidData.tryConsumeEnergy(player, 80)) {
            AABB range = player.getBoundingBox().inflate(24.0D);
            for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, range,
                    e -> e.isAlive() && e != player && e.getType().getCategory().isFriendly() == false)) {
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0, true, false));
            }
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) event.getOriginal().reviveCaps();
        AndroidChassisData.copyTo(event.getOriginal(), event.getEntity());
        if (event.isWasDeath()) event.getOriginal().invalidateCaps();
    }
}