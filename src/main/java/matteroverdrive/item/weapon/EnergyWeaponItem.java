package matteroverdrive.item.weapon;

import matteroverdrive.item.CreativeBatteryItem;
import matteroverdrive.registry.ModItems;
import matteroverdrive.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
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
import java.util.function.Consumer;

import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class EnergyWeaponItem extends Item {
    public enum WeaponType {
        PHASER("Phaser", 18, 0.0F, 10, 80, 2, 0.0F),
        PHASER_RIFLE("Phaser Rifle", 32, 8.0F, 11, 80, 1_024, 1.0F),
        ION_SNIPER("Ion Sniper", 96, 21.0F, 30, 100, 3_072, 5.0F),
        PLASMA_SHOTGUN("Plasma Shotgun", 16, 16.0F, 22, 80, 2_560, 5.0F);

        private final String displayName;
        private final int range;
        private final float damage;
        private final int cooldown;
        private final int maxHeat;
        private final int energyPerShot;
        private final float spread;

        WeaponType(String displayName, int range, float damage, int cooldown, int maxHeat, int energyPerShot, float spread) {
            this.displayName = displayName;
            this.range = range;
            this.damage = damage;
            this.cooldown = cooldown;
            this.maxHeat = maxHeat;
            this.energyPerShot = energyPerShot;
            this.spread = spread;
        }
    }

    public static final int BASE_CAPACITY = 32_000;
    private static final int ENERGY_TRANSFER = 128;
    private static final int MAX_USE_TIME = 72_000;
    private static final int SHOTGUN_MAX_CHARGE = 20;
    private static final String ENERGY_TAG = "MatterOverdriveWeaponEnergy";
    private static final String HEAT_TAG = "MatterOverdriveWeaponHeat";
    private static final String OVERHEATED_TAG = "MatterOverdriveWeaponOverheated";
    private static final String LAST_SHOT_TAG = "MatterOverdriveLastShot";
    private static final String PHASER_POWER_TAG = "MatterOverdrivePhaserPower";

    private final WeaponType type;

    public EnergyWeaponItem(Properties properties, WeaponType type) {
        super(properties.stacksTo(1));
        this.type = type;
    }

    public WeaponType getWeaponType() {
        return type;
    }

    public boolean supportsModule(WeaponModuleItem module) {
        if (type == WeaponType.PHASER) {
            return module.getSlotType() == WeaponModuleItem.SlotType.BARREL
                    || module.getSlotType() == WeaponModuleItem.SlotType.COLOR;
        }
        if (type == WeaponType.PLASMA_SHOTGUN
                && module.getEffect() == WeaponModuleItem.Effect.HEAL) {
            return false;
        }
        return module.getEffect() != WeaponModuleItem.Effect.HEAL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack weapon = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResultHolder.pass(weapon);
        }

        if (type == WeaponType.PHASER && player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                int next = (getPhaserPower(weapon) + 1) % 6;
                weapon.getOrCreateTag().putInt(PHASER_POWER_TAG, next);
                player.sendSystemMessage(Component.literal("Phaser mode: " + phaserModeName(next))
                        .withStyle(next >= 3 ? ChatFormatting.RED : ChatFormatting.AQUA));
                playSound(level, player, "weapons.phaser_switch_mode", 0.8F, 1.0F);
            }
            return InteractionResultHolder.sidedSuccess(weapon, level.isClientSide);
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                boolean reloaded = tryReload(weapon, player, getCapacity(weapon));
                player.sendSystemMessage(Component.literal(reloaded
                        ? "Weapon reloaded" : "No charged battery or Energy Pack available")
                        .withStyle(reloaded ? ChatFormatting.AQUA : ChatFormatting.RED));
            }
            return InteractionResultHolder.sidedSuccess(weapon, level.isClientSide);
        }

        if (!canAttemptFire(weapon, player)) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.literal(isOverheated(weapon)
                        ? "Weapon overheated" : "Weapon is recharging").withStyle(ChatFormatting.RED));
            }
            return InteractionResultHolder.fail(weapon);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(weapon);
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack weapon, int remainingUseDuration) {
        if (level.isClientSide || !(living instanceof Player player)) {
            return;
        }
        if (type != WeaponType.PHASER && type != WeaponType.PHASER_RIFLE) {
            return;
        }
        int elapsed = getUseDuration(weapon) - remainingUseDuration;
        int cooldown = getCooldown(weapon);
        if (elapsed % cooldown == 0) {
            if (!fire(level, player, weapon, 1, 0.0F, 1.0F)) {
                player.stopUsingItem();
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack weapon, Level level, LivingEntity living, int timeLeft) {
        if (level.isClientSide || !(living instanceof Player player)) {
            return;
        }
        int elapsed = Mth.clamp(getUseDuration(weapon) - timeLeft, 0, SHOTGUN_MAX_CHARGE);
        if (type == WeaponType.ION_SNIPER) {
            float charge = Mth.clamp(elapsed / 12.0F, 0.25F, 1.0F);
            fire(level, player, weapon, 1, type.spread * (1.25F - charge), charge);
        } else if (type == WeaponType.PLASMA_SHOTGUN) {
            int pellets = Mth.clamp(10 - Math.round(9.0F * elapsed / SHOTGUN_MAX_CHARGE), 1, 10);
            float concentration = 1.0F - (pellets - 1) / 10.0F;
            fire(level, player, weapon, pellets, type.spread * (0.25F + pellets / 10.0F), 1.0F + concentration);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide) {
            float heat = getHeat(stack);
            if (heat > 0.0F) {
                setHeat(stack, Math.max(0.0F, heat - 1.0F));
            }
            if (isOverheated(stack) && getHeat(stack) < 2.0F) {
                stack.getOrCreateTag().putBoolean(OVERHEATED_TAG, false);
            }
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    private boolean fire(Level level, Player shooter, ItemStack weapon, int pellets, float spread, float damageScale) {
        int energyCost = getEnergyCost(weapon);
        if (!hasEnoughEnergy(weapon, shooter, energyCost) && !tryReload(weapon, shooter, energyCost)) {
            shooter.sendSystemMessage(Component.literal("No weapon energy - carry an Energy Pack or charged battery")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        if (isOverheated(weapon) || !shotDelayPassed(level, weapon)) {
            return false;
        }

        drainEnergy(weapon, shooter, energyCost);
        weapon.getOrCreateTag().putLong(LAST_SHOT_TAG, level.getGameTime());
        addShotHeat(level, shooter, weapon, pellets);

        boolean aimed = shooter.isUsingItem();
        float actualSpread = spreadFor(weapon, spread, aimed);
        float damage = weaponDamage(weapon) * damageScale / Math.max(1, pellets);
        int range = getRange(weapon);
        RandomSource random = shooter.getRandom();

        for (int pellet = 0; pellet < pellets; pellet++) {
            Vec3 direction = applySpread(shooter.getLookAngle(), actualSpread, random);
            traceAndApply(level, shooter, weapon, direction, range, damage, pellets, true);
        }

        playFireSound(level, shooter);
        return true;
    }

    private void traceAndApply(Level level, Player shooter, ItemStack weapon, Vec3 direction, int range,
                               float damage, int pellets, boolean allowRicochet) {
        Vec3 start = shooter.getEyePosition().add(direction.scale(0.25D));
        Vec3 end = start.add(direction.scale(range));
        BlockHitResult blockHit = level.clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
        double blockDistance = blockHit.getType() == HitResult.Type.MISS
                ? Double.MAX_VALUE : start.distanceToSqr(blockHit.getLocation());

        AABB searchBox = shooter.getBoundingBox().expandTowards(direction.scale(range)).inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level, shooter, start, end, searchBox,
                entity -> entity != shooter && entity.isAlive() && entity.isPickable() && !entity.isSpectator());
        boolean hitEntity = entityHit != null
                && start.distanceToSqr(entityHit.getLocation()) < blockDistance;
        Vec3 impact = hitEntity ? entityHit.getLocation() : blockHit.getLocation();

        spawnBeam(level, start, impact, weapon);
        if (hitEntity) {
            applyEntityHit(level, shooter, weapon, entityHit.getEntity(), impact, damage, pellets);
            return;
        }
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            applyBlockHit(level, shooter, weapon, blockHit, damage, pellets);
            if (allowRicochet && WeaponSystem.hasEffect(weapon, WeaponModuleItem.Effect.RICOCHET)) {
                Vec3 reflected = reflect(direction, blockHit.getDirection()).normalize();
                traceAndApply(level, shooter, weapon, reflected, Math.max(1, range / 2), damage * 0.75F, pellets, false);
            }
        }
    }

    private void applyEntityHit(Level level, Player shooter, ItemStack weapon, Entity targetEntity,
                                Vec3 impact, float damage, int pellets) {
        if (!(targetEntity instanceof LivingEntity target)) {
            return;
        }
        WeaponModuleItem.Effect barrel = WeaponSystem.getBarrelEffect(weapon);
        if (barrel == WeaponModuleItem.Effect.HEAL) {
            target.heal(Math.max(1.0F, type.damage * 0.2F));
            spawnImpact(level, impact, ParticleTypes.HAPPY_VILLAGER);
            return;
        }
        if (barrel == WeaponModuleItem.Effect.BLOCK) {
            return;
        }

        if (type == WeaponType.PHASER && getPhaserPower(weapon) < 3) {
            int duration = (int) Math.pow(getPhaserPower(weapon) + 1, 5);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 4));
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 4));
        } else if (damage > 0.0F) {
            DamageSource source = level.damageSources().playerAttack(shooter);
            target.hurt(source, damage);
        }

        if (barrel == WeaponModuleItem.Effect.FIRE) {
            target.setSecondsOnFire(Math.max(2, type == WeaponType.PHASER ? getPhaserPower(weapon) : 5));
        }
        if (barrel == WeaponModuleItem.Effect.EXPLOSION || barrel == WeaponModuleItem.Effect.DOOMSDAY) {
            float multiplier = barrel == WeaponModuleItem.Effect.DOOMSDAY ? 3.0F : 1.0F;
            level.explode(shooter, impact.x, impact.y, impact.z,
                    (1.0F + type.damage * 0.08F * multiplier) / (float) Math.sqrt(Math.max(1, pellets)),
                    Level.ExplosionInteraction.NONE);
        }
        spawnImpact(level, impact, barrel == WeaponModuleItem.Effect.FIRE ? ParticleTypes.FLAME : ParticleTypes.ELECTRIC_SPARK);
    }

    private void applyBlockHit(Level level, Player shooter, ItemStack weapon, BlockHitResult hit,
                               float damage, int pellets) {
        WeaponModuleItem.Effect barrel = WeaponSystem.getBarrelEffect(weapon);
        BlockPos pos = hit.getBlockPos();
        if (barrel == WeaponModuleItem.Effect.BLOCK) {
            BlockState state = level.getBlockState(pos);
            float hardness = state.getDestroySpeed(level, pos);
            if (hardness >= 0.0F && hardness <= 5.0F && !state.is(Blocks.BEDROCK)) {
                level.destroyBlock(pos, true, shooter);
            }
        } else if (barrel == WeaponModuleItem.Effect.FIRE) {
            BlockPos firePos = pos.relative(hit.getDirection());
            if (level.isEmptyBlock(firePos)) {
                level.setBlockAndUpdate(firePos, Blocks.FIRE.defaultBlockState());
            }
        } else if (barrel == WeaponModuleItem.Effect.EXPLOSION || barrel == WeaponModuleItem.Effect.DOOMSDAY) {
            float multiplier = barrel == WeaponModuleItem.Effect.DOOMSDAY ? 3.0F : 1.0F;
            Vec3 impact = hit.getLocation();
            level.explode(shooter, impact.x, impact.y, impact.z,
                    (1.0F + type.damage * 0.08F * multiplier) / (float) Math.sqrt(Math.max(1, pellets)),
                    Level.ExplosionInteraction.NONE);
        }
        spawnImpact(level, hit.getLocation(), ParticleTypes.SMOKE);
    }

    private void addShotHeat(Level level, Player shooter, ItemStack weapon, int pellets) {
        float added = switch (type) {
            case PHASER -> 10.0F;
            case PHASER_RIFLE -> 20.0F;
            case ION_SNIPER -> 80.0F;
            case PLASMA_SHOTGUN -> 35.0F + (10 - Math.min(10, pellets)) * 2.0F;
        };
        setHeat(weapon, getHeat(weapon) + added);
        if (getHeat(weapon) >= getMaxHeat(weapon)) {
            weapon.getOrCreateTag().putBoolean(OVERHEATED_TAG, true);
            playSound(level, shooter, "weapons.overheat", 1.0F, 1.0F);
            playSound(level, shooter, "weapons.overheat_alarm", 0.8F, 1.0F);
        }
    }

    private boolean canAttemptFire(ItemStack weapon, Player player) {
        return !player.isSpectator() && !isOverheated(weapon);
    }

    private boolean shotDelayPassed(Level level, ItemStack weapon) {
        return level.getGameTime() - weapon.getOrCreateTag().getLong(LAST_SHOT_TAG) >= getCooldown(weapon);
    }

    private int getCooldown(ItemStack weapon) {
        return Math.max(1, Math.round(type.cooldown * WeaponSystem.cooldownMultiplier(weapon)));
    }

    private int getEnergyCost(ItemStack weapon) {
        if (type == WeaponType.PHASER) {
            int level = getPhaserPower(weapon);
            return Math.max(1, (int) Math.pow(2.1D, level + 1) * getCooldown(weapon));
        }
        return Math.max(1, Math.round(type.energyPerShot * WeaponSystem.energyMultiplier(weapon)));
    }

    private float weaponDamage(ItemStack weapon) {
        if (type == WeaponType.PHASER) {
            int power = getPhaserPower(weapon);
            float base = power >= 3 ? (float) Math.pow(2.0D, power - 2) : 0.0F;
            return base * WeaponSystem.damageMultiplier(weapon);
        }
        return type.damage * WeaponSystem.damageMultiplier(weapon);
    }

    private int getRange(ItemStack weapon) {
        return Math.max(1, Math.round(type.range * WeaponSystem.rangeMultiplier(weapon)));
    }

    private float spreadFor(ItemStack weapon, float suppliedSpread, boolean aimed) {
        float base = suppliedSpread;
        if (type == WeaponType.PHASER) {
            base = 5.0F * getHeat(weapon) / Math.max(1.0F, getMaxHeat(weapon));
        } else if (type == WeaponType.PHASER_RIFLE) {
            base = 1.0F + getHeat(weapon) / (aimed ? 30.0F : 10.0F);
        } else if (type == WeaponType.ION_SNIPER) {
            base += getHeat(weapon) * (aimed ? 0.1F : 0.3F);
        } else if (type == WeaponType.PLASMA_SHOTGUN) {
            base += getHeat(weapon) * 0.03F;
        }
        if (aimed) {
            base *= 0.65F;
        }
        return base * WeaponSystem.accuracyMultiplier(weapon, aimed);
    }

    private Vec3 applySpread(Vec3 look, float spreadDegrees, RandomSource random) {
        if (spreadDegrees <= 0.001F) {
            return look.normalize();
        }
        Vec3 right = look.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 0.001D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }
        right = right.normalize();
        Vec3 up = right.cross(look).normalize();
        double spread = Math.tan(Math.toRadians(spreadDegrees));
        return look.add(right.scale(random.nextGaussian() * spread))
                .add(up.scale(random.nextGaussian() * spread)).normalize();
    }

    private Vec3 reflect(Vec3 direction, Direction face) {
        return switch (face.getAxis()) {
            case X -> new Vec3(-direction.x, direction.y, direction.z);
            case Y -> new Vec3(direction.x, -direction.y, direction.z);
            case Z -> new Vec3(direction.x, direction.y, -direction.z);
        };
    }

    private void spawnBeam(Level level, Vec3 start, Vec3 end, ItemStack weapon) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        int color = WeaponSystem.getColor(weapon);
        Vector3f rgb = new Vector3f(
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F);
        DustParticleOptions particle = new DustParticleOptions(rgb, type == WeaponType.PHASER ? 0.8F : 1.2F);
        Vec3 delta = end.subtract(start);
        int steps = Mth.clamp((int) (delta.length() * 2.0D), 2, 96);
        for (int step = 0; step <= steps; step++) {
            Vec3 point = start.add(delta.scale(step / (double) steps));
            server.sendParticles(particle, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void spawnImpact(Level level, Vec3 impact, ParticleOptions particle) {
        if (level instanceof ServerLevel server) {
            server.sendParticles(particle, impact.x, impact.y, impact.z, 6, 0.12D, 0.12D, 0.12D, 0.02D);
        }
    }

    private void playFireSound(Level level, Player shooter) {
        String id = switch (type) {
            case PHASER -> "weapons.phaser_beam";
            case PHASER_RIFLE -> "weapons.phaser_rifle_shot";
            case ION_SNIPER -> "weapons.sniper_rifle_fire";
            case PLASMA_SHOTGUN -> "weapons.plasma_shotgun_shot";
        };
        playSound(level, shooter, id, type == WeaponType.ION_SNIPER ? 1.5F : 0.9F,
                0.95F + shooter.getRandom().nextFloat() * 0.1F);
    }

    private void playSound(Level level, Player shooter, String id, float volume, float pitch) {
        SoundEvent sound = ModSounds.get(id).get();
        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    private boolean hasEnoughEnergy(ItemStack weapon, Player player, int amount) {
        return hasCreativeBattery(weapon) || getEnergyStored(weapon) >= amount;
    }

    private void drainEnergy(ItemStack weapon, Player player, int amount) {
        if (!hasCreativeBattery(weapon)) {
            setEnergyStored(weapon, getEnergyStored(weapon) - amount);
        }
    }

    private boolean tryReload(ItemStack weapon, Player player, int required) {
        if (hasCreativeBattery(weapon)) {
            return true;
        }
        if (getEnergyStored(weapon) >= required) {
            return true;
        }

        boolean transferred = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize() && getEnergyStored(weapon) < required; slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.is(ModItems.get("energy_pack").get())) {
                if (!player.getAbilities().instabuild) {
                    candidate.shrink(1);
                }
                setEnergyStored(weapon, getEnergyStored(weapon) + EnergyPackItem.ENERGY_AMOUNT);
                transferred = true;
            }
        }

        for (int slot = 0; slot < player.getInventory().getContainerSize() && getEnergyStored(weapon) < required; slot++) {
            transferred |= transferBatteryEnergy(weapon, player, player.getInventory().getItem(slot)) > 0;
        }
        if (getEnergyStored(weapon) < required) {
            transferred |= transferBatteryEnergy(weapon, player, player.getOffhandItem()) > 0;
        }

        if (transferred) {
            playSound(player.level(), player, "weapons.reload", 0.8F, 1.0F);
        }
        return getEnergyStored(weapon) >= required;
    }

    private int transferBatteryEnergy(ItemStack weapon, Player player, ItemStack candidate) {
        if (candidate.isEmpty() || candidate == weapon || !(candidate.getItem() instanceof WeaponBatteryItem)) return 0;
        IEnergyStorage storage = candidate.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (storage == null || storage.getEnergyStored() <= 0) return 0;

        int needed = Math.max(0, getCapacity(weapon) - getEnergyStored(weapon));
        if (needed <= 0) return 0;
        if (player.getAbilities().instabuild) {
            setEnergyStored(weapon, getCapacity(weapon));
            return needed;
        }

        int moved = 0;
        while (needed > 0 && storage.getEnergyStored() > 0) {
            int extracted = storage.extractEnergy(needed, false);
            if (extracted <= 0) break;
            setEnergyStored(weapon, getEnergyStored(weapon) + extracted);
            moved += extracted;
            needed -= extracted;
        }
        return moved;
    }

    public int getEnergyStored(ItemStack weapon) {
        if (hasCreativeBattery(weapon)) {
            return Integer.MAX_VALUE;
        }
        return Mth.clamp(weapon.getOrCreateTag().getInt(ENERGY_TAG), 0, getCapacity(weapon));
    }

    public void setEnergyStored(ItemStack weapon, int amount) {
        if (!hasCreativeBattery(weapon)) {
            weapon.getOrCreateTag().putInt(ENERGY_TAG, Mth.clamp(amount, 0, getCapacity(weapon)));
        }
    }

    public int getCapacity(ItemStack weapon) {
        return WeaponSystem.getCapacity(weapon, BASE_CAPACITY);
    }

    public float getHeat(ItemStack weapon) {
        return Math.max(0.0F, weapon.getOrCreateTag().getFloat(HEAT_TAG));
    }

    public void setHeat(ItemStack weapon, float heat) {
        weapon.getOrCreateTag().putFloat(HEAT_TAG, Mth.clamp(heat, 0.0F, getMaxHeat(weapon) + 1.0F));
    }

    public int getMaxHeat(ItemStack weapon) {
        return type.maxHeat;
    }

    public boolean isOverheated(ItemStack weapon) {
        return weapon.getOrCreateTag().getBoolean(OVERHEATED_TAG);
    }

    private boolean hasCreativeBattery(ItemStack weapon) {
        return WeaponSystem.getModule(weapon, WeaponSystem.BATTERY_SLOT).getItem() instanceof CreativeBatteryItem;
    }

    public int getPhaserPower(ItemStack weapon) {
        return type == WeaponType.PHASER
                ? Mth.clamp(weapon.getOrCreateTag().getInt(PHASER_POWER_TAG), 0, 5) : 0;
    }

    private String phaserModeName(int power) {
        return power < 3 ? "STUN " + (power + 1) : "KILL " + (power - 2);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer renderer =
                    new matteroverdrive.client.WeaponItemRenderer();

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return MAX_USE_TIME;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (hasCreativeBattery(stack)) {
            return 13;
        }
        return Math.round(13.0F * getEnergyStored(stack) / Math.max(1.0F, getCapacity(stack)));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return isOverheated(stack) ? 0xFF3300 : 0x33CCFF;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new WeaponEnergyProvider(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String energy = hasCreativeBattery(stack) ? "Infinite" : getEnergyStored(stack) + " / " + getCapacity(stack);
        tooltip.add(Component.literal("Energy: " + energy + " FE").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Heat: " + Math.round(getHeat(stack)) + " / " + getMaxHeat(stack)
                + (isOverheated(stack) ? " OVERHEATED" : "")).withStyle(isOverheated(stack) ? ChatFormatting.RED : ChatFormatting.GRAY));
        tooltip.add(Component.literal("Damage: " + String.format("%.1f", weaponDamage(stack))
                + " | Range: " + getRange(stack) + " | Cooldown: " + getCooldown(stack) + "t")
                .withStyle(ChatFormatting.AQUA));
        if (type == WeaponType.PHASER) {
            tooltip.add(Component.literal("Shift-right-click: change mode | " + phaserModeName(getPhaserPower(stack)))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        tooltip.add(Component.literal("[DEBUG] Modules: " + WeaponSystem.installedModuleCount(stack)
                + "/6 | Cost: " + getEnergyCost(stack) + " FE/shot").withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private final class WeaponEnergyProvider implements ICapabilityProvider {
        private final LazyOptional<IEnergyStorage> energy;

        private WeaponEnergyProvider(ItemStack stack) {
            energy = LazyOptional.of(() -> new WeaponEnergyStorage(stack));
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
            return cap == ForgeCapabilities.ENERGY ? energy.cast() : LazyOptional.empty();
        }
    }

    private final class WeaponEnergyStorage implements IEnergyStorage {
        private final ItemStack stack;

        private WeaponEnergyStorage(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (hasCreativeBattery(stack)) {
                return 0;
            }
            int accepted = Math.min(Math.max(0, maxReceive), Math.min(ENERGY_TRANSFER, getCapacity(stack) - EnergyWeaponItem.this.getEnergyStored(stack)));
            if (!simulate && accepted > 0) {
                setEnergyStored(stack, EnergyWeaponItem.this.getEnergyStored(stack) + accepted);
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (hasCreativeBattery(stack)) {
                return Math.max(0, maxExtract);
            }
            int extracted = Math.min(Math.max(0, maxExtract), Math.min(ENERGY_TRANSFER, EnergyWeaponItem.this.getEnergyStored(stack)));
            if (!simulate && extracted > 0) {
                setEnergyStored(stack, EnergyWeaponItem.this.getEnergyStored(stack) - extracted);
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return EnergyWeaponItem.this.getEnergyStored(stack);
        }

        @Override
        public int getMaxEnergyStored() {
            return getCapacity(stack);
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return !hasCreativeBattery(stack);
        }
    }
}
