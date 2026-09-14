package matteroverdrive.item.weapon;

import matteroverdrive.registry.ModExoticSounds
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

/** Independent Matter Overdrive implementation of the Vex Mythoclast exotic firing loop. */
public class VexMythoclastItem extends Item {
    private static final String ENERGY = "VexMythoclastEnergy";
    private static final String CHARGES = "VexTemporalCharges";
    private static final String LINEAR = "VexLinearMode";
    private static final String LAST_SHOT = "VexLastShot";
    private static final int CAPACITY = 96_000;
    private static final int MAX_CHARGES = 3;
    private static final int PRIMARY_COST = 600;
    private static final int LINEAR_COST = 3_200;

    public VexMythoclastItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) return InteractionResultHolder.pass(stack);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                boolean next = !isLinearMode(stack);
                if (next && getCharges(stack) <= 0) {
                    player.sendSystemMessage(Component.literal("Temporal charge required for linear mode.").withStyle(ChatFormatting.RED));
                } else {
                    setLinearMode(stack, next);
                    player.sendSystemMessage(Component.literal(next ? "Temporal Unlimiter: LINEAR" : "Temporal Unlimiter: PRIMARY")
                            .withStyle(next ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA));
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide || !(entity instanceof Player player) || isLinearMode(stack)) return;
        int elapsed = getUseDuration(stack) - remainingUseDuration;
        if (elapsed % 3 == 0) fire(level, player, stack, false);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide || !(entity instanceof Player player) || !isLinearMode(stack)) return;
        int elapsed = getUseDuration(stack) - timeLeft;
        if (elapsed >= 12) fire(level, player, stack, true);
    }

    private boolean fire(Level level, Player player, ItemStack stack, boolean linear) {
        long cooldown = linear ? 14L : 3L;
        long now = level.getGameTime();
        if (now - stack.getOrCreateTag().getLong(LAST_SHOT) < cooldown) return false;
        int cost = linear ? LINEAR_COST : PRIMARY_COST;
        if (linear && getCharges(stack) <= 0) {
            setLinearMode(stack, false);
            return false;
        }
        if (getEnergy(stack) < cost) {
            player.sendSystemMessage(Component.literal("Vex Mythoclast requires FE.").withStyle(ChatFormatting.RED));
            return false;
        }

        setEnergy(stack, getEnergy(stack) - cost);
        stack.getOrCreateTag().putLong(LAST_SHOT, now);
        if (linear) setCharges(stack, getCharges(stack) - 1);

        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getLookAngle().normalize();
        double range = linear ? 112.0D : 72.0D;
        Vec3 end = start.add(direction.scale(range));
        HitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double blockDistance = blockHit.getType() == HitResult.Type.MISS ? Double.MAX_VALUE : start.distanceToSqr(blockHit.getLocation());
        AABB search = player.getBoundingBox().expandTowards(direction.scale(range)).inflate(linear ? 0.45D : 0.8D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, player, start, end, search,
                target -> target != player && target.isAlive() && target.isPickable() && !target.isSpectator());
        Vec3 impact = blockHit.getLocation();
        if (entityHit != null && start.distanceToSqr(entityHit.getLocation()) < blockDistance) {
            impact = entityHit.getLocation();
            if (entityHit.getEntity() instanceof LivingEntity target) {
                float damage = linear ? 54.0F : 11.0F;
                boolean wasAlive = target.isAlive();
                target.hurt(level.damageSources().playerAttack(player), damage);
                if (!linear && wasAlive && !target.isAlive()) {
                    setCharges(stack, Math.min(MAX_CHARGES, getCharges(stack) + 1));
                    if (getCharges(stack) >= MAX_CHARGES) {
                        player.sendSystemMessage(Component.literal("Temporal Overcharge complete - linear mode ready.")
                                .withStyle(ChatFormatting.LIGHT_PURPLE));
                    }
                }
            }
        }
        spawnBeam(level, start, impact, linear);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModExoticSounds.get(linear ? "vex_mythoclast_linear_fire" : "vex_mythoclast_fire"),
                SoundSource.PLAYERS, linear ? 1.35F : 0.95F, linear ? 0.84F : 1.08F);
        if (linear && getCharges(stack) <= 0) setLinearMode(stack, false);
        return true;
    }

    private void spawnBeam(Level level, Vec3 start, Vec3 end, boolean linear) {
        if (!(level instanceof ServerLevel server)) return;
        Vector3f rgb = linear ? new Vector3f(1.0F, 0.45F, 0.12F) : new Vector3f(1.0F, 0.72F, 0.20F);
        DustParticleOptions particle = new DustParticleOptions(rgb, linear ? 1.5F : 1.0F);
        Vec3 delta = end.subtract(start);
        int steps = Mth.clamp((int)(delta.length() * 1.5D), 2, 128);
        for (int i = 0; i <= steps; i++) {
            Vec3 p = start.add(delta.scale(i / (double)steps));
            server.sendParticles(particle, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }

    public int getEnergy(ItemStack stack) { return Mth.clamp(stack.getOrCreateTag().getInt(ENERGY), 0, CAPACITY); }
    public void setEnergy(ItemStack stack, int value) { stack.getOrCreateTag().putInt(ENERGY, Mth.clamp(value, 0, CAPACITY)); }
    public int getCharges(ItemStack stack) { return Mth.clamp(stack.getOrCreateTag().getInt(CHARGES), 0, MAX_CHARGES); }
    private void setCharges(ItemStack stack, int value) { stack.getOrCreateTag().putInt(CHARGES, Mth.clamp(value, 0, MAX_CHARGES)); }
    public boolean isLinearMode(ItemStack stack) { return stack.getOrCreateTag().getBoolean(LINEAR); }
    private void setLinearMode(ItemStack stack, boolean value) { stack.getOrCreateTag().putBoolean(LINEAR, value); }

    @Override public int getUseDuration(ItemStack stack) { return 72_000; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return isLinearMode(stack) ? UseAnim.BOW : UseAnim.NONE; }
    @Override public boolean isBarVisible(ItemStack stack) { return true; }
    @Override public int getBarWidth(ItemStack stack) { return Math.round(13.0F * getEnergy(stack) / CAPACITY); }
    @Override public int getBarColor(ItemStack stack) { return isLinearMode(stack) ? 0xFF9933 : 0xFFCC44; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("EXOTIC // Fusion Rifle").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Energy: " + getEnergy(stack) + " / " + CAPACITY + " FE").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Temporal Charges: " + getCharges(stack) + " / " + MAX_CHARGES).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal(isLinearMode(stack) ? "Mode: Temporal Linear" : "Mode: Full-Auto Primary").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Primary kills build Temporal Overcharge; Shift-right-click toggles linear mode.").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> new IEnergyStorage() {
                @Override public int receiveEnergy(int maxReceive, boolean simulate) {
                    int accepted = Math.min(Math.max(0, maxReceive), Math.min(4096, CAPACITY - getEnergy(stack)));
                    if (!simulate && accepted > 0) setEnergy(stack, getEnergy(stack) + accepted);
                    return accepted;
                }
                @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
                @Override public int getEnergyStored() { return getEnergy(stack); }
                @Override public int getMaxEnergyStored() { return CAPACITY; }
                @Override public boolean canExtract() { return false; }
                @Override public boolean canReceive() { return true; }
            });
            @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
                return cap == ForgeCapabilities.ENERGY ? energy.cast() : LazyOptional.empty();
            }
        };
    }
}
