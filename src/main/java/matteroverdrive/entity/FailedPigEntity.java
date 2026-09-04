package matteroverdrive.entity;

import matteroverdrive.registry.ModEntities;
import matteroverdrive.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class FailedPigEntity extends Pig {
    public FailedPigEntity(EntityType<? extends Pig> type, Level level) { super(type, level); }
    @Override protected SoundEvent getAmbientSound() { return ModSounds.get("failed_animal_idle_pig").get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ModSounds.get("failed_animal_idle_pig").get(); }
    @Override protected SoundEvent getDeathSound() { return ModSounds.get("failed_animal_die").get(); }
    @Override protected float getSoundVolume() { return 1.0F; }
    @Nullable @Override public Pig getBreedOffspring(ServerLevel level, AgeableMob mate) { return ModEntities.FAILED_PIG.get().create(level); }
}
