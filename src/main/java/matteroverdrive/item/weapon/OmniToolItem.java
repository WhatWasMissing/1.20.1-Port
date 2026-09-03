package matteroverdrive.item.weapon;

import matteroverdrive.item.TritaniumToolTier;
import matteroverdrive.registry.ModItems;
import matteroverdrive.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Matter Overdrive's combined energy weapon and mining tool.
 *
 * The legacy Omni Tool fires from the attack input and mines the looked-at block
 * from range while the use input is held. All destructive work is resolved on
 * the server so the client only requests a shot and supplies no trusted target.
 */
public class OmniToolItem extends EnergyWeaponItem {
    public static final int RANGE = 24;
    private static final float BASE_DAMAGE = 7.0F;
    private static final int SHOT_COOLDOWN = 18;
    private static final int MAX_HEAT = 80;
    private static final int ENERGY_PER_SHOT = 512;
    private static final int ENERGY_PER_MINING_TICK = 1;
    private static final int ENERGY_PER_TOOL_ACTION = 8;
    private static final int MAX_USE_TIME = 240;

    private static final String LAST_SHOT_TAG = "MatterOverdriveLastShot";
    private static final String OVERHEATED_TAG = "MatterOverdriveWeaponOverheated";
    private static final String REMOTE_MINING_POS_TAG = "MatterOverdriveOmniMiningPos";
    private static final String REMOTE_MINING_PROGRESS_TAG = "MatterOverdriveOmniMiningProgress";
    private static final String REMOTE_BREAK_TAG = "MatterOverdriveOmniRemoteBreak";

    public OmniToolItem(Properties properties) {
        super(properties, WeaponType.PHASER_RIFLE);
    }

    @Override
    public boolean supportsModule(WeaponModuleItem module) {
        return module.getSlotType() == WeaponModuleItem.SlotType.COLOR
                || (module.getSlotType() == WeaponModuleItem.SlotType.BARREL
                && module.getEffect() == WeaponModuleItem.Effect.BLOCK);
    }

    @Override
    public int getMaxHeat(ItemStack weapon) {
        return MAX_HEAT;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return MAX_USE_TIME;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    /** Called only from the server-side Omni fire packet. */
    public boolean fireFromInput(ServerPlayer shooter, ItemStack weapon) {
        if (shooter.isSpectator() || weapon != shooter.getMainHandItem() || weapon.getItem() != this) {
            return false;
        }
        return fireOmni(shooter.serverLevel(), shooter, weapon);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack weapon = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResultHolder.pass(weapon);
        }

        if (player.isShiftKeyDown()) {
            clearRemoteMining(level, player, weapon);
            return super.use(level, player, hand);
        }

        if (isOverheated(weapon)) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.literal("Omni Tool overheated").withStyle(ChatFormatting.RED));
            }
            return InteractionResultHolder.fail(weapon);
        }

        if (getEnergyStored(weapon) <= 0 && !reloadForShot(weapon, player)) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.literal("No Omni Tool energy - carry an Energy Pack or charged battery")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResultHolder.fail(weapon);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(weapon);
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack weapon, int remainingUseDuration) {
        if (level.isClientSide || !(living instanceof ServerPlayer player)) {
            return;
        }
        if (!remoteMineTick(player, weapon)) {
            player.stopUsingItem();
        }
    }

    @Override
    public void releaseUsing(ItemStack weapon, Level level, LivingEntity living, int timeLeft) {
        if (!level.isClientSide && living instanceof Player player) {
            clearRemoteMining(level, player, weapon);
        }
    }

    private boolean remoteMineTick(ServerPlayer player, ItemStack weapon) {
        ServerLevel level = player.serverLevel();
        if (isOverheated(weapon) || player.isSpectator()) {
            clearRemoteMining(level, player, weapon);
            return false;
        }

        if (getEnergyStored(weapon) <= 0 && !reloadForShot(weapon, player)) {
            clearRemoteMining(level, player, weapon);
            player.sendSystemMessage(Component.literal("Omni Tool out of mining energy").withStyle(ChatFormatting.RED));
            return false;
        }

        // Legacy digging consumed roughly one FE each active mining tick. It did
        // so even when the ray trace was temporarily over air, so preserve that.
        setEnergyStored(weapon, getEnergyStored(weapon) - ENERGY_PER_MINING_TICK);

        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(RANGE));
        BlockHitResult hit = level.clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            clearRemoteMining(level, player, weapon);
            return true;
        }

        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) {
            clearRemoteMining(level, player, weapon);
            return true;
        }

        var tag = weapon.getOrCreateTag();
        long current = tag.contains(REMOTE_MINING_POS_TAG) ? tag.getLong(REMOTE_MINING_POS_TAG) : Long.MIN_VALUE;
        if (current != pos.asLong()) {
            clearRemoteMining(level, player, weapon);
            tag = weapon.getOrCreateTag();
            tag.putLong(REMOTE_MINING_POS_TAG, pos.asLong());
            tag.putFloat(REMOTE_MINING_PROGRESS_TAG, 0.0F);
        }

        float progress = tag.getFloat(REMOTE_MINING_PROGRESS_TAG)
                + state.getDestroyProgress(player, level, pos) * 2.0F;
        tag.putFloat(REMOTE_MINING_PROGRESS_TAG, progress);
        level.destroyBlockProgress(player.getId(), pos, Mth.clamp((int) (progress * 10.0F), 0, 9));

        if (progress >= 1.0F) {
            tag.putBoolean(REMOTE_BREAK_TAG, true);
            try {
                player.gameMode.destroyBlock(pos);
            } finally {
                tag.remove(REMOTE_BREAK_TAG);
            }
            level.destroyBlockProgress(player.getId(), pos, -1);
            tag.remove(REMOTE_MINING_POS_TAG);
            tag.remove(REMOTE_MINING_PROGRESS_TAG);
        }
        return true;
    }

    private void clearRemoteMining(Level level, Player player, ItemStack weapon) {
        if (level.isClientSide || weapon.isEmpty()) {
            return;
        }
        var tag = weapon.getTag();
        if (tag == null) {
            return;
        }
        if (tag.contains(REMOTE_MINING_POS_TAG)) {
            level.destroyBlockProgress(player.getId(), BlockPos.of(tag.getLong(REMOTE_MINING_POS_TAG)), -1);
        }
        tag.remove(REMOTE_MINING_POS_TAG);
        tag.remove(REMOTE_MINING_PROGRESS_TAG);
        tag.remove(REMOTE_BREAK_TAG);
    }

    private boolean fireOmni(Level level, Player shooter, ItemStack weapon) {
        if (isOverheated(weapon)) {
            return false;
        }
        long lastShot = weapon.getOrCreateTag().getLong(LAST_SHOT_TAG);
        if (level.getGameTime() - lastShot < SHOT_COOLDOWN) {
            return true;
        }
        if (getEnergyStored(weapon) < ENERGY_PER_SHOT && !reloadForShot(weapon, shooter)) {
            shooter.sendSystemMessage(Component.literal("Omni Tool out of energy").withStyle(ChatFormatting.RED));
            return false;
        }

        setEnergyStored(weapon, getEnergyStored(weapon) - ENERGY_PER_SHOT);
        weapon.getOrCreateTag().putLong(LAST_SHOT_TAG, level.getGameTime());

        float newHeat = Math.min(MAX_HEAT + 1.0F, (getHeat(weapon) + 4.0F) * 2.7F);
        setHeat(weapon, newHeat);
        if (newHeat >= MAX_HEAT) {
            weapon.getOrCreateTag().putBoolean(OVERHEATED_TAG, true);
            play(level, shooter, "weapons.overheat", 1.0F, 1.0F);
            play(level, shooter, "weapons.overheat_alarm", 0.8F, 1.0F);
        }

        Vec3 direction = applySpread(shooter.getLookAngle(), 0.3F + getHeat(weapon) / MAX_HEAT * 5.0F, shooter);
        traceShot(level, shooter, weapon, direction);
        play(level, shooter, "weapons.laser_fire", 0.7F, 1.15F + shooter.getRandom().nextFloat() * 0.1F);
        return true;
    }

    private void traceShot(Level level, Player shooter, ItemStack weapon, Vec3 direction) {
        Vec3 start = shooter.getEyePosition().add(direction.scale(0.25D));
        Vec3 end = start.add(direction.scale(RANGE));
        BlockHitResult blockHit = level.clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
        double blockDistance = blockHit.getType() == HitResult.Type.MISS
                ? Double.MAX_VALUE : start.distanceToSqr(blockHit.getLocation());

        AABB searchBox = shooter.getBoundingBox().expandTowards(direction.scale(RANGE)).inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level, shooter, start, end, searchBox,
                entity -> entity != shooter && entity.isAlive() && entity.isPickable() && !entity.isSpectator());
        boolean hitEntity = entityHit != null
                && start.distanceToSqr(entityHit.getLocation()) < blockDistance;
        Vec3 impact = hitEntity ? entityHit.getLocation() : blockHit.getLocation();

        spawnBeam(level, start, impact, weapon);
        if (hitEntity) {
            if (WeaponSystem.getBarrelEffect(weapon) != WeaponModuleItem.Effect.BLOCK
                    && entityHit.getEntity() instanceof LivingEntity target) {
                target.hurt(level.damageSources().playerAttack(shooter), BASE_DAMAGE);
                spawnImpact(level, impact);
            }
            return;
        }

        if (blockHit.getType() == HitResult.Type.BLOCK
                && WeaponSystem.getBarrelEffect(weapon) == WeaponModuleItem.Effect.BLOCK) {
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = level.getBlockState(pos);
            float hardness = state.getDestroySpeed(level, pos);
            if (hardness >= 0.0F && hardness <= 5.0F && !state.is(Blocks.BEDROCK)) {
                level.destroyBlock(pos, true, shooter);
            }
        }
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            spawnImpact(level, blockHit.getLocation());
        }
    }

    private Vec3 applySpread(Vec3 look, float spreadDegrees, Player player) {
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
        return look.add(right.scale(player.getRandom().nextGaussian() * spread))
                .add(up.scale(player.getRandom().nextGaussian() * spread)).normalize();
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
        DustParticleOptions particle = new DustParticleOptions(rgb, 0.9F);
        Vec3 delta = end.subtract(start);
        int steps = Mth.clamp((int) (delta.length() * 2.0D), 2, 64);
        for (int step = 0; step <= steps; step++) {
            Vec3 point = start.add(delta.scale(step / (double) steps));
            server.sendParticles(particle, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void spawnImpact(Level level, Vec3 impact) {
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, impact.x, impact.y, impact.z,
                    6, 0.12D, 0.12D, 0.12D, 0.02D);
        }
    }

    private void play(Level level, Player player, String id, float volume, float pitch) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.get(id).get(), SoundSource.PLAYERS, volume, pitch);
    }

    private boolean reloadForShot(ItemStack weapon, Player player) {
        if (getEnergyStored(weapon) >= ENERGY_PER_SHOT) {
            return true;
        }

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.is(ModItems.get("energy_pack").get())) {
                if (!player.getAbilities().instabuild) {
                    candidate.shrink(1);
                }
                setEnergyStored(weapon, getEnergyStored(weapon) + EnergyPackItem.ENERGY_AMOUNT);
                play(player.level(), player, "weapons.reload", 0.8F, 1.0F);
                return getEnergyStored(weapon) >= ENERGY_PER_SHOT;
            }
        }

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (transferBattery(weapon, player, player.getInventory().getItem(slot)) > 0
                    && getEnergyStored(weapon) >= ENERGY_PER_SHOT) {
                play(player.level(), player, "weapons.reload", 0.8F, 1.0F);
                return true;
            }
        }
        if (transferBattery(weapon, player, player.getOffhandItem()) > 0) {
            play(player.level(), player, "weapons.reload", 0.8F, 1.0F);
        }
        return getEnergyStored(weapon) >= ENERGY_PER_SHOT;
    }

    private int transferBattery(ItemStack weapon, Player player, ItemStack candidate) {
        if (candidate.isEmpty() || candidate == weapon || !(candidate.getItem() instanceof WeaponBatteryItem)) {
            return 0;
        }
        IEnergyStorage storage = candidate.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (storage == null || storage.getEnergyStored() <= 0) {
            return 0;
        }

        int needed = Math.max(0, getCapacity(weapon) - getEnergyStored(weapon));
        if (needed <= 0) {
            return 0;
        }
        if (player.getAbilities().instabuild) {
            setEnergyStored(weapon, getCapacity(weapon));
            return needed;
        }

        int extracted = storage.extractEnergy(needed, false);
        if (extracted > 0) {
            setEnergyStored(weapon, getEnergyStored(weapon) + extracted);
        }
        return Math.max(0, extracted);
    }

    private boolean hasToolPower(ItemStack stack) {
        return getEnergyStored(stack) > 0;
    }

    private boolean isEffective(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                || state.is(BlockTags.MINEABLE_WITH_AXE)
                || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return hasToolPower(stack) && isEffective(state)
                ? TritaniumToolTier.TIER.getSpeed()
                : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (!hasToolPower(stack) || !isEffective(state)) {
            return false;
        }
        return !state.requiresCorrectToolForDrops()
                || TierSortingRegistry.isCorrectTierForDrops(TritaniumToolTier.TIER, state);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (!level.isClientSide && hasToolPower(stack) && isEffective(state)
                && !stack.getOrCreateTag().getBoolean(REMOTE_BREAK_TAG)) {
            setEnergyStored(stack, getEnergyStored(stack) - ENERGY_PER_TOOL_ACTION);
        }
        return true;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return hasToolPower(stack) && (
                ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(toolAction)
                        || ToolActions.DEFAULT_AXE_ACTIONS.contains(toolAction)
                        || ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(toolAction));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if ((player != null && player.isShiftKeyDown()) || !hasToolPower(stack)) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        BlockState result = useAsAxe(state, context);

        if (result == null) {
            if (context.getClickedFace() == Direction.DOWN) {
                return InteractionResult.PASS;
            }
            BlockState flattened = state.getToolModifiedState(context, ToolActions.SHOVEL_FLATTEN, false);
            if (flattened != null && level.isEmptyBlock(pos.above())) {
                level.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                result = flattened;
            } else if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
                if (!level.isClientSide) {
                    level.levelEvent(null, LevelEvent.SOUND_EXTINGUISH_FIRE, pos, 0);
                }
                CampfireBlock.dowse(player, level, pos, state);
                result = state.setValue(CampfireBlock.LIT, false);
            }
        }

        if (result == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
            }
            level.setBlock(pos, result, Block.UPDATE_ALL_IMMEDIATE);
            setEnergyStored(stack, getEnergyStored(stack) - ENERGY_PER_TOOL_ACTION);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    private BlockState useAsAxe(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        BlockState result = state.getToolModifiedState(context, ToolActions.AXE_STRIP, false);
        if (result != null) {
            level.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            return result;
        }
        result = state.getToolModifiedState(context, ToolActions.AXE_SCRAPE, false);
        if (result != null) {
            level.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(player, LevelEvent.PARTICLES_SCRAPE, pos, 0);
            return result;
        }
        result = state.getToolModifiedState(context, ToolActions.AXE_WAX_OFF, false);
        if (result != null) {
            level.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(player, LevelEvent.PARTICLES_WAX_OFF, pos, 0);
            return result;
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String energy = getEnergyStored(stack) == Integer.MAX_VALUE
                ? "Infinite" : getEnergyStored(stack) + " / " + getCapacity(stack);
        tooltip.add(Component.literal("Energy: " + energy + " FE").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Heat: " + Math.round(getHeat(stack)) + " / " + MAX_HEAT
                + (isOverheated(stack) ? " OVERHEATED" : ""))
                .withStyle(isOverheated(stack) ? ChatFormatting.RED : ChatFormatting.GRAY));
        tooltip.add(Component.literal("Weapon: 7 damage | 24 range | 18t cooldown")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Tool: Tritanium-tier Pickaxe + Axe + Shovel | 24-block mining")
                .withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("Left-click: fire | Hold right-click: ranged mine | Shift-right-click: reload")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("Right-click blocks: strip, scrape, wax-off, flatten, extinguish")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Modules: Battery + Color + VENOM Block Barrel")
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("[DEBUG] " + ENERGY_PER_SHOT + " FE/shot | "
                + ENERGY_PER_MINING_TICK + " FE/mining tick | " + ENERGY_PER_TOOL_ACTION
                + " FE/context action | Modules " + WeaponSystem.installedModuleCount(stack) + "/6")
                .withStyle(ChatFormatting.GOLD));
    }
}
