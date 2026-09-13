package matteroverdrive.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

/** Unstable synthetic whose phase cycle makes it briefly faster and visually disappear during pursuit. */
public class PhaseStalkerEntity extends Zombie {
    public PhaseStalkerEntity(EntityType<? extends Zombie> type,Level level){super(type,level);}
    @Override public void aiStep(){super.aiStep();if(!level().isClientSide&&getTarget()!=null&&tickCount%120==0){addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,35,0,true,false));addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,45,1,true,false));}}
}
