package matteroverdrive.android;

import matteroverdrive.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public final class AndroidAbilities {
    public static final int ACTION_CYCLE = 0;
    public static final int ACTION_ACTIVATE = 1;
    public static final String ABILITY_DAMAGE_TAG = "MatterOverdriveAndroidAbilityDamage";

    public static final int CLOAK_ENERGY_PER_TICK = 128;
    public static final int SHIELD_IDLE_ENERGY_PER_TICK = 32;
    public static final int SHIELD_ENERGY_PER_DAMAGE = 64;
    public static final int SHOCKWAVE_ENERGY = 4_096;
    public static final int SHOCKWAVE_COOLDOWN = 100;
    public static final int TELEPORT_ENERGY = 4_096;
    public static final int TELEPORT_COOLDOWN = 60;

    private static final double SHOCKWAVE_RADIUS = 5.0D;
    private static final float SHOCKWAVE_DAMAGE = 6.0F;
    private static final double TELEPORT_RANGE = 8.0D;

    private AndroidAbilities() {
    }

    public static void handleAction(ServerPlayer player, int action) {
        if (!AndroidData.isAndroid(player)) {
            status(player, "Android conversion is not active.", ChatFormatting.RED);
            return;
        }

        if (action == ACTION_CYCLE) {
            AndroidData.Ability selected = AndroidData.cycleAbility(player);
            if (selected == null) {
                status(player, "Install a bionic part to unlock an ability.", ChatFormatting.RED);
            } else {
                status(player, "Selected: " + selected.displayName, ChatFormatting.AQUA);
            }
            ModNetwork.syncAndroidState(player);
            return;
        }

        if (action != ACTION_ACTIVATE) {
            return;
        }

        AndroidData.Ability ability = AndroidData.getSelectedAbility(player);
        if (!AndroidData.isAbilityUnlocked(player, ability)) {
            status(player, ability.displayName + " requires the "
                    + ability.requiredPart.name().toLowerCase() + " bionic part.", ChatFormatting.RED);
            return;
        }

        switch (ability) {
            case CLOAK -> toggleCloak(player);
            case FORCE_FIELD -> toggleShield(player);
            case SHOCKWAVE -> activateShockwave(player);
            case TELEPORT -> activateTeleport(player);
        }
        ModNetwork.syncAndroidState(player);
    }

    public static void tick(ServerPlayer player) {
        if (!AndroidData.isAndroid(player)) {
            return;
        }

        if (AndroidData.isCloakEnabled(player)) {
            if (!AndroidData.isAbilityUnlocked(player, AndroidData.Ability.CLOAK)
                    || !AndroidData.tryConsumeEnergy(player, CLOAK_ENERGY_PER_TICK)) {
                AndroidData.setCloakEnabled(player, false);
                status(player, "Cloak disabled: insufficient Android FE.", ChatFormatting.RED);
            } else {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 5, 0, true, false, false));
            }
        }

        if (AndroidData.isShieldEnabled(player)
                && (!AndroidData.isAbilityUnlocked(player, AndroidData.Ability.FORCE_FIELD)
                || !AndroidData.tryConsumeEnergy(player, SHIELD_IDLE_ENERGY_PER_TICK))) {
            AndroidData.setShieldEnabled(player, false);
            status(player, "Force Field disabled: insufficient Android FE.", ChatFormatting.RED);
        }
    }

    public static void applyShield(LivingHurtEvent event, ServerPlayer player) {
        if (!AndroidData.isAndroid(player)
                || !AndroidData.isShieldEnabled(player)
                || !AndroidData.isAbilityUnlocked(player, AndroidData.Ability.FORCE_FIELD)
                || event.getAmount() <= 0.0F) {
            return;
        }

        float desiredAbsorption = event.getAmount() * 0.5F;
        float affordableAbsorption = AndroidData.getEnergy(player) / (float) SHIELD_ENERGY_PER_DAMAGE;
        float absorbed = Math.min(desiredAbsorption, affordableAbsorption);
        if (absorbed <= 0.0F) {
            AndroidData.setShieldEnabled(player, false);
            status(player, "Force Field collapsed: Android FE depleted.", ChatFormatting.RED);
            return;
        }

        int energyCost = Math.max(1, (int) Math.ceil(absorbed * SHIELD_ENERGY_PER_DAMAGE));
        if (!AndroidData.tryConsumeEnergy(player, energyCost)) {
            return;
        }
        event.setAmount(Math.max(0.0F, event.getAmount() - absorbed));
        if (AndroidData.getEnergy(player) < SHIELD_ENERGY_PER_DAMAGE) {
            AndroidData.setShieldEnabled(player, false);
            status(player, "Force Field collapsed: Android FE depleted.", ChatFormatting.RED);
        }
    }

    private static void toggleCloak(ServerPlayer player) {
        boolean enabled = !AndroidData.isCloakEnabled(player);
        if (enabled && AndroidData.getEnergy(player) < CLOAK_ENERGY_PER_TICK) {
            status(player, "Cloak needs at least " + CLOAK_ENERGY_PER_TICK + " FE.", ChatFormatting.RED);
            return;
        }
        AndroidData.setCloakEnabled(player, enabled);
        status(player, "Cloak " + (enabled ? "enabled." : "disabled."),
                enabled ? ChatFormatting.GREEN : ChatFormatting.YELLOW);
    }

    private static void toggleShield(ServerPlayer player) {
        boolean enabled = !AndroidData.isShieldEnabled(player);
        if (enabled && AndroidData.getEnergy(player) < SHIELD_IDLE_ENERGY_PER_TICK) {
            status(player, "Force Field needs Android FE.", ChatFormatting.RED);
            return;
        }
        AndroidData.setShieldEnabled(player, enabled);
        status(player, "Force Field " + (enabled ? "enabled." : "disabled."),
                enabled ? ChatFormatting.GREEN : ChatFormatting.YELLOW);
    }

    private static void activateShockwave(ServerPlayer player) {
        long gameTime = player.level().getGameTime();
        int cooldown = AndroidData.getRemainingCooldown(player, AndroidData.Ability.SHOCKWAVE, gameTime);
        if (cooldown > 0) {
            status(player, "Sonic Shockwave cooldown: " + formatSeconds(cooldown), ChatFormatting.RED);
            return;
        }
        if (AndroidData.getEnergy(player) < SHOCKWAVE_ENERGY) {
            status(player, "Sonic Shockwave needs " + SHOCKWAVE_ENERGY + " FE.", ChatFormatting.RED);
            return;
        }

        AABB area = player.getBoundingBox().inflate(SHOCKWAVE_RADIUS, 2.5D, SHOCKWAVE_RADIUS);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                target -> target != player && target.isAlive() && !target.isAlliedTo(player));
        boolean previousAbilityDamage = player.getPersistentData().getBoolean(ABILITY_DAMAGE_TAG);
        player.getPersistentData().putBoolean(ABILITY_DAMAGE_TAG, true);
        try {
            for (LivingEntity target : targets) {
                Vec3 offset = target.position().subtract(player.position());
                Vec3 horizontal = new Vec3(offset.x, 0.0D, offset.z);
                if (horizontal.lengthSqr() < 0.001D) {
                    horizontal = player.getLookAngle();
                }
                horizontal = horizontal.normalize();
                target.hurt(player.damageSources().playerAttack(player), SHOCKWAVE_DAMAGE);
                target.push(horizontal.x * 1.25D, 0.45D, horizontal.z * 1.25D);
            }
        } finally {
            if (previousAbilityDamage) {
                player.getPersistentData().putBoolean(ABILITY_DAMAGE_TAG, true);
            } else {
                player.getPersistentData().remove(ABILITY_DAMAGE_TAG);
            }
        }

        AndroidData.tryConsumeEnergy(player, SHOCKWAVE_ENERGY);
        AndroidData.setCooldownUntil(player, AndroidData.Ability.SHOCKWAVE, gameTime + SHOCKWAVE_COOLDOWN);
        status(player, "Sonic Shockwave hit " + targets.size() + " target(s).", ChatFormatting.AQUA);
    }

    private static void activateTeleport(ServerPlayer player) {
        long gameTime = player.level().getGameTime();
        int cooldown = AndroidData.getRemainingCooldown(player, AndroidData.Ability.TELEPORT, gameTime);
        if (cooldown > 0) {
            status(player, "Ender Teleport cooldown: " + formatSeconds(cooldown), ChatFormatting.RED);
            return;
        }
        if (AndroidData.getEnergy(player) < TELEPORT_ENERGY) {
            status(player, "Ender Teleport needs " + TELEPORT_ENERGY + " FE.", ChatFormatting.RED);
            return;
        }

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 rayEnd = eye.add(look.scale(TELEPORT_RANGE));
        HitResult hit = player.level().clip(new ClipContext(
                eye, rayEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double clearDistance = hit.getType() == HitResult.Type.MISS
                ? TELEPORT_RANGE
                : Math.max(1.0D, hit.getLocation().distanceTo(eye) - 0.75D);

        Vec3 origin = player.position();
        Vec3 destination = null;
        for (double distance = clearDistance; distance >= 1.0D; distance -= 0.5D) {
            Vec3 candidate = origin.add(look.scale(distance));
            BlockPos candidatePos = BlockPos.containing(candidate);
            AABB moved = player.getBoundingBox().move(
                    candidate.x - player.getX(),
                    candidate.y - player.getY(),
                    candidate.z - player.getZ());
            if (candidate.y >= player.level().getMinBuildHeight()
                    && candidate.y + player.getBbHeight() < player.level().getMaxBuildHeight()
                    && player.level().getWorldBorder().isWithinBounds(candidatePos)
                    && player.level().noCollision(player, moved)) {
                destination = candidate;
                break;
            }
        }

        if (destination == null) {
            status(player, "No safe Ender Teleport destination.", ChatFormatting.RED);
            return;
        }

        player.teleportTo(destination.x, destination.y, destination.z);
        player.fallDistance = 0.0F;
        AndroidData.tryConsumeEnergy(player, TELEPORT_ENERGY);
        AndroidData.setCooldownUntil(player, AndroidData.Ability.TELEPORT, gameTime + TELEPORT_COOLDOWN);
        status(player, "Ender Teleport complete.", ChatFormatting.AQUA);
    }

    private static String formatSeconds(int ticks) {
        return String.format("%.1fs", ticks / 20.0D);
    }

    private static void status(ServerPlayer player, String message, ChatFormatting color) {
        player.displayClientMessage(Component.literal(message).withStyle(color), true);
    }
}
