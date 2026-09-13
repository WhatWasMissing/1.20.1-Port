package matteroverdrive.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

/** Collective-style synthetic that weakens biological targets while closing distance. */
public class AssimilatorEntity extends Zombie {
    public AssimilatorEntity(EntityType<? extends Zombie> type,Level level){super(type,level);}
    @Override public boolean doHurtTarget(net.minecraft.world.entity.Entity target){boolean hit=super.doHurtTarget(target);if(hit&&target instanceof LivingEntity living)living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,100,0));return hit;}
}
