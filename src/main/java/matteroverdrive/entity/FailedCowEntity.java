package matteroverdrive.entity;

import matteroverdrive.registry.ModEntities;
import matteroverdrive.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class FailedCowEntity extends Cow {
    public FailedCowEntity(EntityType<? extends Cow> type, Level level) { super(type, level); }
    @Override protected SoundEvent getAmbientSound() { return ModSounds.get("failed_animal_idle_cow").get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ModSounds.get("failed_animal_idle_cow").get(); }
    @Override protected SoundEvent getDeathSound() { return ModSounds.get("failed_animal_die").get(); }
    @Nullable @Override public Cow getBreedOffspring(ServerLevel level, AgeableMob mate) { return ModEntities.FAILED_COW.get().create(level); }
}
