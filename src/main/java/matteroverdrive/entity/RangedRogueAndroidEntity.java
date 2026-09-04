package matteroverdrive.entity;

import matteroverdrive.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import javax.annotation.Nullable;

public class RangedRogueAndroidEntity extends RogueAndroidEntity implements net.minecraft.world.entity.monster.RangedAttackMob {
    public RangedRogueAndroidEntity(EntityType<? extends RangedRogueAndroidEntity> type, Level level){super(type,level);}
    @Override protected void registerGoals(){super.registerGoals();goalSelector.addGoal(1,new RangedAttackGoal(this,1.0D,20,28.0F));}
    @Nullable @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level,DifficultyInstance difficulty,MobSpawnType spawnType,@Nullable SpawnGroupData spawnData,@Nullable CompoundTag dataTag){SpawnGroupData data=super.finalizeSpawn(level,difficulty,spawnType,spawnData,dataTag);setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(ModItems.get(getRandom().nextBoolean()?"phaser_rifle":"ion_sniper").get()));setDropChance(EquipmentSlot.MAINHAND,.025F);return data;}
    @Override public void performRangedAttack(LivingEntity target,float distanceFactor){if(level().isClientSide)return;Arrow arrow=new Arrow(level(),this);double dx=target.getX()-getX(),dz=target.getZ()-getZ(),dy=target.getEyeY()-1.1D-arrow.getY(),horizontal=Math.sqrt(dx*dx+dz*dz);arrow.setBaseDamage(4D+getAndroidLevel());arrow.shoot(dx,dy+horizontal*.15D,dz,1.8F,2F);level().addFreshEntity(arrow);playSound(net.minecraft.sounds.SoundEvents.BLAZE_SHOOT,.8F,1.4F);}
}
