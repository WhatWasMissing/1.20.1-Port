package matteroverdrive.item.weapon;

import matteroverdrive.registry.ModDestinySounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public final class NativeDestinyWeaponItem extends EnergyWeaponItem {
    public static final String MAGAZINE_TAG = "MatterOverdriveDestinyMagazine";
    public static final String ANIMATION_TAG = "MatterOverdriveDestinyAnimation";
    public static final String ANIMATION_START_TAG = "MatterOverdriveDestinyAnimationStart";
    public static final String RELOAD_END_TAG = "MatterOverdriveDestinyReloadEnd";
    public static final String LAST_SHOT_TAG = "MatterOverdriveDestinyLastShot";
    private static final String SELECTED_TAG = "MatterOverdriveDestinySelected";

    private final NativeDestinyWeaponProfile profile;

    public NativeDestinyWeaponItem(Item.Properties properties, NativeDestinyWeaponProfile profile) {
        super(properties, WeaponType.PHASER_RIFLE);
        this.profile = profile;
    }

    public NativeDestinyWeaponProfile profile() { return profile; }

    @Override
    public Component getName(ItemStack stack) { return Component.literal(profile.displayName()); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack weapon = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) return InteractionResultHolder.pass(weapon);
        if (player.isShiftKeyDown()) {
            if (getMagazine(weapon) >= profile.magazine()) return InteractionResultHolder.sidedSuccess(weapon, level.isClientSide);
            beginReload(level, player, weapon);
            super.use(level, player, hand);
            return InteractionResultHolder.sidedSuccess(weapon, level.isClientSide);
        }
        if (isReloading(level, weapon)) {
            if (!level.isClientSide) player.displayClientMessage(Component.literal("Capacitor cycle in progress").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(weapon);
        }
        if (profile.automatic()) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(weapon);
        }
        triggerFireAnimation(level, weapon);
        if (!level.isClientSide) fire(level, player, weapon);
        return InteractionResultHolder.sidedSuccess(weapon, level.isClientSide);
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack weapon, int remainingUseDuration) {
        if (!(living instanceof Player player) || !profile.automatic() || isReloading(level, weapon)) return;
        int elapsed = getUseDuration(weapon) - remainingUseDuration;
        if (elapsed % profile.cooldownTicks() != 0) return;
        triggerFireAnimation(level, weapon);
        if (!level.isClientSide && !fire(level, player, weapon)) player.stopUsingItem();
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {}

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        CompoundTag tag = stack.getOrCreateTag();
        boolean wasSelected = tag.getBoolean(SELECTED_TAG);
        if (selected && !wasSelected) {
            tag.putBoolean(SELECTED_TAG, true);
            triggerAnimation(level, stack, "animation.model.draw");
            if (!level.isClientSide && entity instanceof Player player) {
                playProfileSound(level, player, profile.drawSound(), 0.75F);
            }
        } else if (!selected && wasSelected) {
            tag.putBoolean(SELECTED_TAG, false);
        }
        long reloadEnd = tag.getLong(RELOAD_END_TAG);
        if (reloadEnd > 0L && level.getGameTime() >= reloadEnd) {
            tag.putInt(MAGAZINE_TAG, profile.magazine());
            tag.remove(RELOAD_END_TAG);
            if (!level.isClientSide && entity instanceof Player player) {
                player.displayClientMessage(Component.literal(profile.displayName() + " capacitor ready").withStyle(ChatFormatting.AQUA), true);
            }
        }
    }

    private void beginReload(Level level, Player player, ItemStack weapon) {
        CompoundTag tag = weapon.getOrCreateTag();
        if (isReloading(level, weapon)) return;
        triggerAnimation(level, weapon, "animation.model.reload");
        tag.putLong(RELOAD_END_TAG, level.getGameTime() + profile.reloadTicks());
        playProfileSound(level, player, profile.reloadSound(), 0.75F);
    }

    private static void playProfileSound(Level level, Player player, String soundId, float volume) {
        if (level.isClientSide || soundId == null) return;
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModDestinySounds.get(soundId), SoundSource.PLAYERS, volume, 1.0F);
    }

    private boolean fire(Level level, Player shooter, ItemStack weapon) {
        if (isReloading(level, weapon) || !shotDelayPassed(level, weapon)) return false;
        int magazine = getMagazine(weapon);
        if (magazine <= 0) {
            shooter.displayClientMessage(Component.literal("Capacitor empty - shift-right-click to cycle").withStyle(ChatFormatting.RED), true);
            return false;
        }
        if (getEnergyStored(weapon) < profile.energyPerShot() && getEnergyStored(weapon) != Integer.MAX_VALUE) {
            shooter.displayClientMessage(Component.literal("Insufficient FE - recharge the weapon or carry a charged battery").withStyle(ChatFormatting.RED), true);
            return false;
        }
        if (getEnergyStored(weapon) != Integer.MAX_VALUE) setEnergyStored(weapon, getEnergyStored(weapon) - profile.energyPerShot());
        weapon.getOrCreateTag().putInt(MAGAZINE_TAG, magazine - 1);
        weapon.getOrCreateTag().putLong(LAST_SHOT_TAG, level.getGameTime());
        Vec3 direction = applySpread(shooter.getLookAngle(), profile.spreadDegrees(), shooter.getRandom().nextGaussian(), shooter.getRandom().nextGaussian());
        Vec3 start = shooter.getEyePosition().add(direction.scale(0.25D));
        Vec3 end = start.add(direction.scale(profile.range()));
        BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
        double blockDistance = blockHit.getType() == HitResult.Type.MISS ? Double.MAX_VALUE : start.distanceToSqr(blockHit.getLocation());
        AABB searchBox = shooter.getBoundingBox().expandTowards(direction.scale(profile.range())).inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, shooter, start, end, searchBox,
                entity -> entity != shooter && entity.isAlive() && entity.isPickable() && !entity.isSpectator());
        Vec3 impact = blockHit.getLocation();
        if (entityHit != null && start.distanceToSqr(entityHit.getLocation()) < blockDistance) {
            impact = entityHit.getLocation();
            if (entityHit.getEntity() instanceof LivingEntity target) target.hurt(level.damageSources().playerAttack(shooter), profile.damage());
        }
        spawnBeam(level, start, impact);
        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), ModDestinySounds.get(profile.fireSound()), SoundSource.PLAYERS,
                profile == NativeDestinyWeaponProfile.SLEEPER_SIMULANT ? 1.35F : 0.95F, 0.97F + shooter.getRandom().nextFloat() * 0.06F);
        return true;
    }

    private boolean shotDelayPassed(Level level, ItemStack weapon) {
        long last = weapon.getOrCreateTag().getLong(LAST_SHOT_TAG);
        return last <= 0L || level.getGameTime() - last >= profile.cooldownTicks();
    }

    private static Vec3 applySpread(Vec3 direction, float degrees, double gaussianX, double gaussianY) {
        if (degrees <= 0.0F) return direction.normalize();
        double spread = Math.tan(Math.toRadians(degrees));
        Vec3 up = Math.abs(direction.y) > 0.98D ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize();
        Vec3 localUp = right.cross(direction).normalize();
        return direction.add(right.scale(gaussianX * spread)).add(localUp.scale(gaussianY * spread)).normalize();
    }

    private void spawnBeam(Level level, Vec3 start, Vec3 end) {
        if (!(level instanceof ServerLevel server)) return;
        Vector3f rgb = profile == NativeDestinyWeaponProfile.SLEEPER_SIMULANT ? new Vector3f(1.0F, 0.12F, 0.05F) : new Vector3f(0.95F, 0.72F, 0.18F);
        DustParticleOptions particle = new DustParticleOptions(rgb, profile == NativeDestinyWeaponProfile.SLEEPER_SIMULANT ? 1.4F : 0.8F);
        Vec3 delta = end.subtract(start);
        int steps = Mth.clamp((int)(delta.length() * 1.7D), 2, 96);
        for (int i = 0; i <= steps; i++) {
            Vec3 p = start.add(delta.scale(i / (double)steps));
            server.sendParticles(particle, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }

    public int getMagazine(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(MAGAZINE_TAG)) tag.putInt(MAGAZINE_TAG, profile.magazine());
        return Mth.clamp(tag.getInt(MAGAZINE_TAG), 0, profile.magazine());
    }

    public boolean isReloading(Level level, ItemStack stack) {
        long end = stack.getOrCreateTag().getLong(RELOAD_END_TAG);
        return end > level.getGameTime();
    }

    public static String activeAnimation(ItemStack stack) { return stack.getOrCreateTag().getString(ANIMATION_TAG); }
    public static long animationStart(ItemStack stack) { return stack.getOrCreateTag().getLong(ANIMATION_START_TAG); }

    public static void triggerAnimation(Level level, ItemStack stack, String animation) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(ANIMATION_TAG, animation);
        tag.putLong(ANIMATION_START_TAG, level.getGameTime());
    }

    private void triggerFireAnimation(Level level, ItemStack stack) {
        long sequence = level.getGameTime();
        triggerAnimation(level, stack, (sequence & 1L) == 0L ? "animation.model.fire2" : "animation.model.fire");
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer renderer = new matteroverdrive.client.NativeDestinyWeaponRenderer();
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }
        });
    }

    @Override
    public int getUseDuration(ItemStack stack) { return 72_000; }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.NONE; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("NATIVE MATTER OVERDRIVE WEAPON").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Capacitor: " + getMagazine(stack) + " / " + profile.magazine() + " | " + profile.rpm() + " RPM").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Damage: " + String.format("%.1f", profile.damage()) + " | Range: " + profile.range()).withStyle(ChatFormatting.GRAY));
        String energy = getEnergyStored(stack) == Integer.MAX_VALUE ? "Infinite" : getEnergyStored(stack) + " / " + getCapacity(stack);
        tooltip.add(Component.literal("Energy: " + energy + " FE | " + profile.energyPerShot() + " FE/shot").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal(profile.automatic() ? "Hold right-click: automatic fire" : "Right-click: fire").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal("Shift-right-click: capacitor reload + battery transfer").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("Supports Matter Overdrive weapon modules and charging").withStyle(ChatFormatting.DARK_AQUA));
    }
}
