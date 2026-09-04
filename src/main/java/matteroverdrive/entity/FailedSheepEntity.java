package matteroverdrive.entity;

import matteroverdrive.registry.ModEntities;
import matteroverdrive.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class FailedSheepEntity extends Sheep {
    public FailedSheepEntity(EntityType<? extends Sheep> type, Level level) { super(type, level); }
    @Override protected SoundEvent getAmbientSound() { return ModSounds.get("failed_animal_idle_sheep").get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ModSounds.get("failed_animal_idle_sheep").get(); }
    @Override protected SoundEvent getDeathSound() { return ModSounds.get("failed_animal_die").get(); }
    @Nullable @Override public Sheep getBreedOffspring(ServerLevel level, AgeableMob mate) {
        Sheep child = ModEntities.FAILED_SHEEP.get().create(level);
        if (child != null) child.setColor(getColor());
        return child;
    }
}
