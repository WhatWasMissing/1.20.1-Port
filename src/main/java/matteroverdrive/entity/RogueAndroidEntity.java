package matteroverdrive.entity;

import matteroverdrive.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

/**
 * Real Rogue Android entity based on the 1.12.2 EntityRougeAndroidMob hierarchy.
 * Legacy base values: 0.30 movement speed, 24 follow range, 32 + level*10 health,
 * 4 + level melee damage, levels 0-3 and a 3%*level legendary roll.
 */
public class RogueAndroidEntity extends Zombie {
    public static final float NATURAL_SPAWN_CHANCE = 0.10F;
    public static final float LEGENDARY_CHANCE_PER_LEVEL = 0.03F;
    public static final int MAX_PER_CHUNK = 4;

    private int androidLevel;
    private boolean legendary;

    public RogueAndroidEntity(EntityType<? extends RogueAndroidEntity> type, Level level) {
        super(type, level);
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
        this.androidLevel = rolledLevel;
        this.legendary = rolledLevel > 0
                && getRandom().nextDouble() < LEGENDARY_CHANCE_PER_LEVEL * rolledLevel;
        applyLegacyStats(true);
        updateLegacyName();
        setBaby(false);
        return data;
    }

    private void applyLegacyStats(boolean refillHealth) {
        double maxHealth = legendary ? 128.0D : 32.0D + androidLevel * 10.0D;
        double attack = legendary ? 8.0D : 4.0D + androidLevel;
        if (getAttribute(Attributes.MAX_HEALTH) != null) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);
        }
        if (getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(attack);
        }
        if (refillHealth) {
            setHealth((float) maxHealth);
        } else if (getHealth() > maxHealth) {
            setHealth((float) maxHealth);
        }
    }

    private void updateLegacyName() {
        ChatFormatting color = legendary ? ChatFormatting.GOLD : switch (androidLevel) {
            case 1 -> ChatFormatting.DARK_AQUA;
            case 2, 3 -> ChatFormatting.DARK_PURPLE;
            default -> ChatFormatting.GRAY;
        };
        String prefix = legendary ? "Legendary " : "";
        setCustomName(net.minecraft.network.chat.Component.literal(
                prefix + "Rogue Android [Lv " + androidLevel + "]").withStyle(color));
        setCustomNameVisible(false);
    }

    public int getAndroidLevel() {
        return androidLevel;
    }

    public boolean isLegendaryAndroid() {
        return legendary;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return false;
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.get("mobs.rogue_android_say").get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.get("mobs.rogue_android_death").get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AndroidLevel", androidLevel);
        tag.putBoolean("Legendary", legendary);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        androidLevel = Mth.clamp(tag.getInt("AndroidLevel"), 0, 3);
        legendary = tag.getBoolean("Legendary");
        applyLegacyStats(false);
        updateLegacyName();
    }
}
