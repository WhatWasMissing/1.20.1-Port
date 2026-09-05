package matteroverdrive.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.UUID;

public class DroneEntity extends Monster implements RangedAttackMob {
    private byte droneType;
    @Nullable private UUID ownerUuid;

    public DroneEntity(EntityType<? extends DroneEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.MOVEMENT_SPEED, .6D)
                .add(Attributes.FOLLOW_RANGE, 16D)
                .add(Attributes.ARMOR, 14D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1D, 25, 16F));
        goalSelector.addGoal(6, new RandomStrollGoal(this, .9D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (ownerUuid != null && ownerUuid.equals(target.getUUID())) return false;
        return super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (ownerUuid != null && ownerUuid.equals(entity.getUUID())) return true;
        if (entity instanceof DroneEntity other && ownerUuid != null && ownerUuid.equals(other.ownerUuid)) return true;
        return super.isAlliedTo(entity);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (level().isClientSide || !canAttack(target)) return;
        Arrow arrow = new Arrow(level(), this);
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double dy = target.getEyeY() - arrow.getY();
        double h = Math.sqrt(dx * dx + dz * dz);
        arrow.setBaseDamage(3D);
        arrow.shoot(dx, dy + h * .1D, dz, 1.6F, 3F);
        level().addFreshEntity(arrow);
        playSound(net.minecraft.sounds.SoundEvents.BLAZE_SHOOT, .6F, 1.8F);
    }

    public byte getDroneType() { return droneType; }
    public void setDroneType(byte type) { droneType = type; }
    @Nullable public UUID getOwnerUuid() { return ownerUuid; }
    public void setOwnerUuid(@Nullable UUID ownerUuid) { this.ownerUuid = ownerUuid; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("DroneType", droneType);
        if (ownerUuid != null) tag.putUUID("OwnerUUID", ownerUuid);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        droneType = tag.getByte("DroneType");
        ownerUuid = tag.hasUUID("OwnerUUID") ? tag.getUUID("OwnerUUID") : null;
    }
}
