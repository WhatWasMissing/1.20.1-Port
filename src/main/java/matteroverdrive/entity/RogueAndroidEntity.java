package matteroverdrive.entity;

import matteroverdrive.blockentity.AndroidSpawnerBlockEntity;
import matteroverdrive.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class RogueAndroidEntity extends Zombie {
    public static final float NATURAL_SPAWN_CHANCE = 0.10F;
    public static final float LEGENDARY_CHANCE_PER_LEVEL = 0.03F;
    public static final int MAX_PER_CHUNK = 4;

    public static final int MODE_PATROL = 0;
    public static final int MODE_GUARD = 1;
    public static final int MODE_HOLD = 2;
    public static final int MODE_ESCORT = 3;

    private int androidLevel;
    private boolean legendary;
    private String facilitySecurityProfile = "";
    @Nullable private BlockPos spawnerPosition;
    private final List<BlockPos> patrolPoints = new ArrayList<>();
    private int patrolIndex;
    private int squadColor;
    private int squadMode = MODE_PATROL;
    @Nullable private UUID commanderUuid;
    private int targetShareCooldown;

    public RogueAndroidEntity(EntityType<? extends RogueAndroidEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(4, new SquadHoldGoal(this));
        goalSelector.addGoal(5, new EscortCommanderGoal(this));
        goalSelector.addGoal(6, new SquadGuardGoal(this));
        goalSelector.addGoal(7, new PatrolGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0D);
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

    /** Applies a persisted facility role after normal Android level generation. */
    public void applyFacilitySecurityProfile(String profile, int deploymentIndex) {
        facilitySecurityProfile = profile == null ? "" : profile;
        int minimumLevel = switch (facilitySecurityProfile) {
            case "quantum_relay_station", "android_command_bunker" -> 1;
            case "fusion_research_complex", "black_site" -> 2;
            default -> 0;
        };
        if (facilitySecurityProfile.equals("black_site") && deploymentIndex >= 3) minimumLevel = 3;
        androidLevel = Math.max(androidLevel, minimumLevel);
        applyLegacyStats(false);
        applyFacilityStats(true);
        updateLegacyName();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide || spawnerPosition == null) return;
        if (targetShareCooldown > 0) {
            targetShareCooldown--;
            return;
        }
        targetShareCooldown = 10;
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || !canAttack(target)) return;
        AABB squadArea = getBoundingBox().inflate(24.0D);
        for (RogueAndroidEntity ally : level().getEntitiesOfClass(RogueAndroidEntity.class, squadArea,
                candidate -> candidate != this && candidate.wasSpawnedFrom(spawnerPosition))) {
            if (ally.getTarget() == null && ally.canAttack(target)) ally.setTarget(target);
        }
    }

    private void applyLegacyStats(boolean refillHealth) {
        double maxHealth = legendary ? 128.0D : 32.0D + androidLevel * 10.0D;
        double attack = legendary ? 8.0D : 4.0D + androidLevel;
        if (getAttribute(Attributes.MAX_HEALTH) != null) getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);
        if (getAttribute(Attributes.ATTACK_DAMAGE) != null) getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(attack);
        if (getAttribute(Attributes.MOVEMENT_SPEED) != null) getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.30D);
        if (getAttribute(Attributes.FOLLOW_RANGE) != null) getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(24.0D);
        if (getAttribute(Attributes.ARMOR) != null) getAttribute(Attributes.ARMOR).setBaseValue(0.0D);
        if (getAttribute(Attributes.KNOCKBACK_RESISTANCE) != null) getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.0D);
        if (refillHealth) setHealth((float) maxHealth);
        else if (getHealth() > maxHealth) setHealth((float) maxHealth);
    }

    private void applyFacilityStats(boolean refillHealth) {
        if (facilitySecurityProfile.isEmpty()) return;
        double health = getAttributeValue(Attributes.MAX_HEALTH);
        double attack = getAttributeValue(Attributes.ATTACK_DAMAGE);
        double speed = 0.30D;
        double follow = 24.0D;
        double armor = 0.0D;
        double knockback = 0.0D;
        switch (facilitySecurityProfile) {
            case "synthetic_manufacturing_plant" -> {
                speed = 0.33D;
                follow = 28.0D;
            }
            case "matter_refinery" -> {
                health *= 0.78D;
                attack += 2.0D;
                speed = 0.34D;
            }
            case "quantum_relay_station" -> {
                follow = 36.0D;
                speed = 0.31D;
                armor = 2.0D;
            }
            case "android_command_bunker" -> {
                health *= 1.25D;
                attack += 1.5D;
                armor = 6.0D;
                follow = 32.0D;
            }
            case "fusion_research_complex" -> {
                health *= 1.35D;
                armor = 4.0D;
                knockback = 0.35D;
            }
            case "black_site" -> {
                health *= 1.55D;
                attack += 2.5D;
                armor = 8.0D;
                knockback = 0.45D;
                follow = 36.0D;
            }
            default -> { }
        }
        if (getAttribute(Attributes.MAX_HEALTH) != null) getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        if (getAttribute(Attributes.ATTACK_DAMAGE) != null) getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(attack);
        if (getAttribute(Attributes.MOVEMENT_SPEED) != null) getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        if (getAttribute(Attributes.FOLLOW_RANGE) != null) getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(follow);
        if (getAttribute(Attributes.ARMOR) != null) getAttribute(Attributes.ARMOR).setBaseValue(armor);
        if (getAttribute(Attributes.KNOCKBACK_RESISTANCE) != null) getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(knockback);
        if (refillHealth) setHealth((float) health);
        else if (getHealth() > health) setHealth((float) health);
    }

    private void updateLegacyName() {
        ChatFormatting color = squadColorFormatting();
        if (spawnerPosition == null && facilitySecurityProfile.isEmpty()) {
            color = legendary ? ChatFormatting.GOLD : switch (androidLevel) {
                case 1 -> ChatFormatting.DARK_AQUA;
                case 2, 3 -> ChatFormatting.DARK_PURPLE;
                default -> ChatFormatting.GRAY;
            };
        }
        String prefix = legendary ? "Legendary " : "";
        String role = switch (facilitySecurityProfile) {
            case "synthetic_manufacturing_plant" -> "Assembly Defender";
            case "matter_refinery" -> "Malfunctioning Refinery Android";
            case "quantum_relay_station" -> "Relay Sentry";
            case "android_command_bunker" -> "Command Guard";
            case "fusion_research_complex" -> "Containment Sentry";
            case "black_site" -> "Black Site Warden";
            default -> "Rogue Android";
        };
        setCustomName(net.minecraft.network.chat.Component.literal(
                prefix + role + " [Lv " + androidLevel + "]").withStyle(color));
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
    public String getFacilitySecurityProfile() { return facilitySecurityProfile; }

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
    @Nullable public UUID getCommanderUuid() { return commanderUuid; }

    public void setSquad(int color, int mode) { setSquad(color, mode, commanderUuid); }

    public void setSquad(int color, int mode, @Nullable UUID commander) {
        squadColor = Math.floorMod(color, 8);
        squadMode = Mth.clamp(mode, MODE_PATROL, MODE_ESCORT);
        commanderUuid = commander;
        if (squadMode == MODE_HOLD) getNavigation().stop();
        updateLegacyName();
    }

    @Nullable
    private Player getCommander() {
        if (commanderUuid == null || !(level() instanceof ServerLevel serverLevel)) return null;
        return serverLevel.getPlayerByUUID(commanderUuid);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (commanderUuid != null && commanderUuid.equals(entity.getUUID())) return true;
        if (entity instanceof RogueAndroidEntity other
                && spawnerPosition != null && other.spawnerPosition != null
                && spawnerPosition.equals(other.spawnerPosition)) return true;
        Player commander = getCommander();
        if (commander != null && commander.isAlliedTo(entity)) return true;
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
        tag.putString("FacilitySecurityProfile", facilitySecurityProfile);
        if (spawnerPosition != null) tag.putLong("SpawnerPosition", spawnerPosition.asLong());
        tag.putLongArray("PatrolPoints", patrolPoints.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putInt("PatrolIndex", patrolIndex);
        tag.putInt("SquadColor", squadColor);
        tag.putInt("SquadMode", squadMode);
        if (commanderUuid != null) tag.putUUID("CommanderUUID", commanderUuid);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        androidLevel = Mth.clamp(tag.getInt("AndroidLevel"), 0, 3);
        legendary = tag.getBoolean("Legendary");
        facilitySecurityProfile = tag.getString("FacilitySecurityProfile");
        spawnerPosition = tag.contains("SpawnerPosition") ? BlockPos.of(tag.getLong("SpawnerPosition")) : null;
        patrolPoints.clear();
        for (long packed : tag.getLongArray("PatrolPoints")) patrolPoints.add(BlockPos.of(packed));
        patrolIndex = patrolPoints.isEmpty() ? 0 : Math.floorMod(tag.getInt("PatrolIndex"), patrolPoints.size());
        squadColor = Math.floorMod(tag.getInt("SquadColor"), 8);
        squadMode = tag.contains("SquadMode") ? Mth.clamp(tag.getInt("SquadMode"), MODE_PATROL, MODE_ESCORT) : MODE_PATROL;
        commanderUuid = tag.hasUUID("CommanderUUID") ? tag.getUUID("CommanderUUID") : null;
        applyLegacyStats(false);
        applyFacilityStats(false);
        updateLegacyName();
    }

    private static final class PatrolGoal extends Goal {
        private final RogueAndroidEntity android;
        private PatrolGoal(RogueAndroidEntity android) { this.android = android; setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() { return android.squadMode == MODE_PATROL && android.getTarget() == null && !android.patrolPoints.isEmpty() && android.getNavigation().isDone(); }
        @Override public boolean canContinueToUse() { return android.squadMode == MODE_PATROL && android.getTarget() == null && !android.getNavigation().isDone(); }
        @Override public void start() {
            if (android.patrolPoints.isEmpty()) return;
            BlockPos target = android.patrolPoints.get(android.patrolIndex);
            android.patrolIndex = (android.patrolIndex + 1) % android.patrolPoints.size();
            android.getNavigation().moveTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, 1.0D);
        }
    }

    private static final class SquadGuardGoal extends Goal {
        private final RogueAndroidEntity android;
        private SquadGuardGoal(RogueAndroidEntity android) { this.android = android; setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() {
            if (android.squadMode != MODE_GUARD || android.getTarget() != null || android.spawnerPosition == null) return false;
            return android.distanceToSqr(android.spawnerPosition.getX() + 0.5D, android.spawnerPosition.getY() + 0.5D, android.spawnerPosition.getZ() + 0.5D) > 64D;
        }
        @Override public boolean canContinueToUse() { return android.squadMode == MODE_GUARD && android.getTarget() == null && android.spawnerPosition != null && !android.getNavigation().isDone(); }
        @Override public void start() {
            BlockPos home = android.spawnerPosition;
            if (home != null) android.getNavigation().moveTo(home.getX() + 0.5D, home.getY() + 1D, home.getZ() + 0.5D, 1.1D);
        }
    }

    private static final class SquadHoldGoal extends Goal {
        private final RogueAndroidEntity android;
        private SquadHoldGoal(RogueAndroidEntity android) { this.android = android; setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() { return android.squadMode == MODE_HOLD && android.getTarget() == null; }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public void start() { android.getNavigation().stop(); }
        @Override public void tick() { android.getNavigation().stop(); }
    }

    private static final class EscortCommanderGoal extends Goal {
        private static final double CATCH_UP_DISTANCE_SQR = 48.0D * 48.0D;
        private final RogueAndroidEntity android;
        @Nullable private Player commander;

        private EscortCommanderGoal(RogueAndroidEntity android) {
            this.android = android;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override public boolean canUse() {
            commander = android.getCommander();
            return android.squadMode == MODE_ESCORT && android.getTarget() == null
                    && commander != null && !commander.isSpectator();
        }

        @Override public boolean canContinueToUse() {
            return android.squadMode == MODE_ESCORT && android.getTarget() == null
                    && commander != null && commander.isAlive();
        }

        @Override public void tick() {
            if (commander == null) return;
            android.getLookControl().setLookAt(commander, 10F, android.getMaxHeadXRot());
            Vec3 target = formationTarget();
            if (android.distanceToSqr(commander) > CATCH_UP_DISTANCE_SQR && trySafeCatchUp(target)) return;
            double d2 = android.distanceToSqr(target.x, target.y, target.z);
            if (d2 > 4.0D || android.getNavigation().isDone()) {
                android.getNavigation().moveTo(target.x, target.y, target.z, d2 > 144.0D ? 1.35D : 1.18D);
            }
        }

        private Vec3 formationTarget() {
            if (commander == null || android.spawnerPosition == null) return android.position();
            List<RogueAndroidEntity> squad = commander.level().getEntitiesOfClass(RogueAndroidEntity.class,
                    commander.getBoundingBox().inflate(64.0D),
                    other -> other.spawnerPosition != null && other.spawnerPosition.equals(android.spawnerPosition)
                            && other.squadMode == MODE_ESCORT && other.isAlive());
            squad.sort(Comparator.comparing(other -> other.getUUID().toString()));
            int index = Math.max(0, squad.indexOf(android));
            int row = index / 2;
            int column = index % 2;

            Vec3 look = commander.getLookAngle();
            Vec3 forward = new Vec3(look.x, 0.0D, look.z);
            if (forward.lengthSqr() < 0.001D) forward = new Vec3(0.0D, 0.0D, 1.0D);
            else forward = forward.normalize();
            Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);

            double lateral = column == 0 ? -1.65D : 1.65D;
            double trailing = 2.75D + row * 2.1D;
            return commander.position().add(forward.scale(-trailing)).add(right.scale(lateral));
        }

        private boolean trySafeCatchUp(Vec3 target) {
            if (commander == null) return false;
            Vec3[] offsets = {
                    Vec3.ZERO,
                    new Vec3(1.0D, 0.0D, 0.0D), new Vec3(-1.0D, 0.0D, 0.0D),
                    new Vec3(0.0D, 0.0D, 1.0D), new Vec3(0.0D, 0.0D, -1.0D),
                    new Vec3(0.0D, 1.0D, 0.0D)
            };
            for (Vec3 offset : offsets) {
                Vec3 candidate = target.add(offset);
                AABB moved = android.getBoundingBox().move(candidate.subtract(android.position()));
                if (!android.level().noCollision(android, moved)) continue;
                boolean occupied = !android.level().getEntitiesOfClass(RogueAndroidEntity.class, moved.inflate(0.3D),
                        other -> other != android && other.spawnerPosition != null
                                && other.spawnerPosition.equals(android.spawnerPosition)).isEmpty();
                if (occupied) continue;
                android.teleportTo(candidate.x, candidate.y, candidate.z);
                android.getNavigation().stop();
                return true;
            }
            return false;
        }

        @Override public void stop() {
            commander = null;
            android.getNavigation().stop();
        }
    }
}
