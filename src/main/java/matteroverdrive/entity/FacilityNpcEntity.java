package matteroverdrive.entity;

import matteroverdrive.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

/**
 * Neutral human facility staff used to make technology sites feel occupied and purposeful.
 * They use lightweight ambient pathfinding rather than Villager AI, so Rogue Androids do not
 * immediately erase the population simply because Zombie AI treats vanilla villagers as prey.
 * The client renders every role on the standard wide-arm Steve model.
 */
public class FacilityNpcEntity extends PathfinderMob {
    public enum Role {
        RESEARCHER("Field Researcher", ChatFormatting.AQUA),
        ENGINEER("Systems Engineer", ChatFormatting.GOLD),
        SECURITY("Facility Security", ChatFormatting.BLUE);

        public final String displayName;
        public final ChatFormatting colour;

        Role(String displayName, ChatFormatting colour) {
            this.displayName = displayName;
            this.colour = colour;
        }
    }

    public FacilityNpcEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        refreshIdentity();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FOLLOW_RANGE, 18.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.75D, 80));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public Role role() {
        if (getType() == ModEntities.FACILITY_ENGINEER.get()) return Role.ENGINEER;
        if (getType() == ModEntities.FACILITY_SECURITY.get()) return Role.SECURITY;
        return Role.RESEARCHER;
    }

    private void refreshIdentity() {
        Role role = role();
        setCustomName(Component.literal(role.displayName).withStyle(role.colour));
        setCustomNameVisible(false);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData, dataTag);
        refreshIdentity();
        setPersistenceRequired();
        return data;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!level().isClientSide) {
            Role role = role();
            String status = switch (role) {
                case RESEARCHER -> "Research telemetry is still being reconstructed. Check secured storage and lab terminals for recovered data.";
                case ENGINEER -> "Power and matter routing are unstable. The safest salvage is usually stored near engineering bays.";
                case SECURITY -> "Restricted facility. Security automation may still identify unknown personnel as hostile.";
            };
            player.sendSystemMessage(Component.literal("[" + role.displayName + "] ").withStyle(role.colour)
                    .append(Component.literal(status).withStyle(ChatFormatting.GRAY)));
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }
}
