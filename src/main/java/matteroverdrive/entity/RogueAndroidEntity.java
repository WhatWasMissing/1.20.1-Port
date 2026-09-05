package matteroverdrive.entity;

import matteroverdrive.blockentity.AndroidSpawnerBlockEntity;
import matteroverdrive.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class RogueAndroidEntity extends Zombie {
    public static final float NATURAL_SPAWN_CHANCE = 0.10F;
    public static final float LEGENDARY_CHANCE_PER_LEVEL = 0.03F;
    public static final int MAX_PER_CHUNK = 4;

    public static final int MODE_PATROL = 0;
    public static final int MODE_GUARD = 1;
    public static final int MODE_HOLD = 2;

    private int androidLevel;
    private boolean legendary;
    @Nullable private BlockPos spawnerPosition;
    private final List<BlockPos> patrolPoints = new ArrayList<>();
    private int patrolIndex;
    private int squadColor;
    private int squadMode = MODE_PATROL;

    public RogueAndroidEntity(EntityType<? extends RogueAndroidEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(4, new SquadHoldGoal(this));
        goalSelector.addGoal(5, new SquadGuardGoal(this));
        goalSelector.addGoal(6, new PatrolGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData, dataTag);
        int difficultyId = level.getLevel().getDifficulty().getId();
        int rolledLevel = Mth.clamp((int) Math.abs(getRandom().nextGaussian()
                * (1.0D + difficultyId * 0.25D)), 0, 3);
        androidLevel = rolledLevel;
        legendary = rolledLevel > 0
                && getRandom().nextDouble() < LEGENDARY_CHANCE_PER_LEVEL * rolledLevel;
        applyLegacyStats(true);
        updateLegacyName();
        setBaby(false);
        return data;
    }

    private void applyLegacyStats(boolean refillHealth) {
        double maxHealth = legendary ? 128.0D : 32.0D + androidLevel * 10.0D;
        double attack = legendary ? 8.0D : 4.0D + androidLevel;
        if (getAttribute(Attributes.MAX_HEALTH) != null) getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);
        if (getAttribute(Attributes.ATTACK_DAMAGE) != null) getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(attack);
        if (refillHealth) setHealth((float) maxHealth);
        else if (getHealth() > maxHealth) setHealth((float) maxHealth);
    }

    private void updateLegacyName() {
        ChatFormatting color = squadColorFormatting();
        if (spawnerPosition == null) {
            color = legendary ? ChatFormatting.GOLD : switch (androidLevel) {
                case 1 -> ChatFormatting.DARK_AQUA;
                case 2, 3 -> ChatFormatting.DARK_PURPLE;
                default -> ChatFormatting.GRAY;
            };
        }
        String prefix = legendary ? "Legendary " : "";
        setCustomName(net.minecraft.network.chat.Component.literal(
                prefix + "Rogue Android [Lv " + androidLevel + "]").withStyle(color));
        setCustomNameVisible(false);
    }

    private ChatFormatting squadColorFormatting() {
        return switch (Math.floorMod(squadColor, 8)) {
            case 1 -> ChatFormatting.RED;
            case 2 -> ChatFormatting.GOLD;
            case 3 -> ChatFormatting.YELLOW;
            case 4 -> ChatFormatting.GREEN;
            case 5 -> ChatFormatting.AQUA;
            case 6 -> ChatFormatting.BLUE;
            case 7 -> ChatFormatting.LIGHT_PURPLE;
            default -> ChatFormatting.WHITE;
        };
    }

    public int getAndroidLevel() { return androidLevel; }
    public boolean isLegendaryAndroid() { return legendary; }

    public void setSpawnerPosition(@Nullable BlockPos position) {
        spawnerPosition = position == null ? null : position.immutable();
        updateLegacyName();
    }

    @Nullable public BlockPos getSpawnerPosition() { return spawnerPosition; }
    public boolean wasSpawnedFrom(BlockPos position) {
        return spawnerPosition != null && spawnerPosition.equals(position);
    }

    public void setPatrolPoints(List<BlockPos> points) {
        patrolPoints.clear();
        for (BlockPos point : points) {
            if (point != null && !patrolPoints.contains(point)) patrolPoints.add(point.immutable());
        }
        patrolIndex = patrolPoints.isEmpty() ? 0 : Math.floorMod(patrolIndex, patrolPoints.size());
    }

    public List<BlockPos> getPatrolPoints() { return List.copyOf(patrolPoints); }

    public int getSquadColor() { return squadColor; }
    public int getSquadMode() { return squadMode; }

    public void setSquad(int color, int mode) {
        squadColor = Math.floorMod(color, 8);
        squadMode = Mth.clamp(mode, MODE_PATROL, MODE_HOLD);
        if (squadMode == MODE_HOLD) getNavigation().stop();
        updateLegacyName();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof RogueAndroidEntity other
                && spawnerPosition != null && other.spawnerPosition != null
                && spawnerPosition.equals(other.spawnerPosition)) return true;
        return super.isAlliedTo(entity);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (isAlliedTo(target)) return false;
        return super.canAttack(target);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (reason.shouldDestroy() && !level().isClientSide && spawnerPosition != null
                && level().hasChunkAt(spawnerPosition)
                && level().getBlockEntity(spawnerPosition) instanceof AndroidSpawnerBlockEntity spawner) {
            spawner.unregisterOwnedAndroid(getUUID());
        }
        super.remove(reason);
    }

    @Override public boolean canBeAffected(MobEffectInstance effect) { return false; }
    @Override protected boolean isSunSensitive() { return false; }
    @Override protected SoundEvent getAmbientSound() { return ModSounds.get("mobs.rogue_android_say").get(); }
    @Override protected SoundEvent getDeathSound() { return ModSounds.get("mobs.rogue_android_death").get(); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AndroidLevel", androidLevel);
        tag.putBoolean("Legendary", legendary);
        if (spawnerPosition != null) tag.putLong("SpawnerPosition", spawnerPosition.asLong());
        tag.putLongArray("PatrolPoints", patrolPoints.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putInt("PatrolIndex", patrolIndex);
        tag.putInt("SquadColor", squadColor);
        tag.putInt("SquadMode", squadMode);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        androidLevel = Mth.clamp(tag.getInt("AndroidLevel"), 0, 3);
        legendary = tag.getBoolean("Legendary");
        spawnerPosition = tag.contains("SpawnerPosition") ? BlockPos.of(tag.getLong("SpawnerPosition")) : null;
        patrolPoints.clear();
        for (long packed : tag.getLongArray("PatrolPoints")) patrolPoints.add(BlockPos.of(packed));
        patrolIndex = patrolPoints.isEmpty() ? 0 : Math.floorMod(tag.getInt("PatrolIndex"), patrolPoints.size());
        squadColor = Math.floorMod(tag.getInt("SquadColor"), 8);
        squadMode = tag.contains("SquadMode") ? Mth.clamp(tag.getInt("SquadMode"), MODE_PATROL, MODE_HOLD) : MODE_PATROL;
        applyLegacyStats(false);
        updateLegacyName();
    }

    private static final class PatrolGoal extends Goal {
        private final RogueAndroidEntity android;

        private PatrolGoal(RogueAndroidEntity android) {
            this.android = android;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return android.squadMode == MODE_PATROL
                    && android.getTarget() == null
                    && !android.patrolPoints.isEmpty()
                    && android.getNavigation().isDone();
        }

        @Override
        public boolean canContinueToUse() {
            return android.squadMode == MODE_PATROL
                    && android.getTarget() == null && !android.getNavigation().isDone();
        }

        @Override
        public void start() {
            if (android.patrolPoints.isEmpty()) return;
            BlockPos target = android.patrolPoints.get(android.patrolIndex);
            android.patrolIndex = (android.patrolIndex + 1) % android.patrolPoints.size();
            android.getNavigation().moveTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, 1.0D);
        }
    }

    private static final class SquadGuardGoal extends Goal {
        private final RogueAndroidEntity android;

        private SquadGuardGoal(RogueAndroidEntity android) {
            this.android = android;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (android.squadMode != MODE_GUARD || android.getTarget() != null || android.spawnerPosition == null) return false;
            return android.distanceToSqr(android.spawnerPosition.getX() + 0.5D,
                    android.spawnerPosition.getY() + 0.5D, android.spawnerPosition.getZ() + 0.5D) > 64D;
        }

        @Override
        public boolean canContinueToUse() {
            return android.squadMode == MODE_GUARD && android.getTarget() == null
                    && android.spawnerPosition != null && !android.getNavigation().isDone();
        }

        @Override
        public void start() {
            BlockPos home = android.spawnerPosition;
            if (home != null) android.getNavigation().moveTo(home.getX() + 0.5D, home.getY() + 1D, home.getZ() + 0.5D, 1.1D);
        }
    }

    private static final class SquadHoldGoal extends Goal {
        private final RogueAndroidEntity android;

        private SquadHoldGoal(RogueAndroidEntity android) {
            this.android = android;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override public boolean canUse() { return android.squadMode == MODE_HOLD && android.getTarget() == null; }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public void start() { android.getNavigation().stop(); }
        @Override public void tick() { android.getNavigation().stop(); }
    }
}
