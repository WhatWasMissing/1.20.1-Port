package matteroverdrive.entity;

import matteroverdrive.android.AndroidData;
import matteroverdrive.registry.ModEntities;
import matteroverdrive.registry.ModItems;
import matteroverdrive.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

/**
 * Restores the legacy Mad Scientist as a real NPC and brings back the "Puny Humans"
 * become-an-Android quest/reward loop from the 1.12.2 jar.
 */
public class MadScientistEntity extends Villager {
    private static final String JUNKIE = "Junkie";
    private static final String QUEST_ACTIVE = "MatterOverdrivePunyHumansActive";
    private static final String QUEST_DONE = "MatterOverdrivePunyHumansDone";
    private boolean junkie;

    public MadScientistEntity(EntityType<? extends Villager> type, Level level) {
        super(type, level);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData, dataTag);
        junkie = getRandom().nextBoolean();
        updateName();
        return data;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        boolean active = persisted.getBoolean(QUEST_ACTIVE);
        boolean done = persisted.getBoolean(QUEST_DONE);

        if (!done && active && AndroidData.isAndroid(player)) {
            persisted.putBoolean(QUEST_DONE, true);
            persisted.putBoolean(QUEST_ACTIVE, false);
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
            give(player, new ItemStack(ModItems.get("battery").get()));
            give(player, new ItemStack(ModItems.get("android_pill_blue").get()));
            give(player, new ItemStack(ModItems.get("android_pill_yellow").get(), 5));
            level().playSound(null, blockPosition(), ModSounds.get("gui.quest_complete").get(),
                    net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
            player.displayClientMessage(Component.literal("Puny Humans complete: synthetic ascension confirmed.")
                    .withStyle(ChatFormatting.GREEN), false);
            player.displayClientMessage(Component.literal("Reward: Battery, Blue Android Pill, 5 Yellow Android Pills")
                    .withStyle(ChatFormatting.GOLD), false);
            return InteractionResult.CONSUME;
        }

        if (!done && !active && !AndroidData.isAndroid(player)) {
            persisted.putBoolean(QUEST_ACTIVE, true);
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
            level().playSound(null, blockPosition(), ModSounds.get("gui.quest_started").get(),
                    net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
            player.displayClientMessage(Component.literal("Quest started: Puny Humans")
                    .withStyle(ChatFormatting.AQUA), false);
            player.displayClientMessage(Component.literal("Objective: become an Android, then return to a Mad Scientist.")
                    .withStyle(ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }

        if (!done && active) {
            player.displayClientMessage(Component.literal("Puny Humans: become an Android and return to me.")
                    .withStyle(ChatFormatting.AQUA), false);
            return InteractionResult.CONSUME;
        }

        if (AndroidData.isAndroid(player)) {
            player.displayClientMessage(Component.literal(junkie
                    ? "Magnificent! The machine has finally improved the human."
                    : "Your conversion appears stable. Try not to waste it.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        } else {
            player.displayClientMessage(Component.literal(junkie
                    ? "Science demands sacrifice. Preferably yours."
                    : "Come back when you're ready to transcend biology.")
                    .withStyle(ChatFormatting.GRAY), false);
        }
        return InteractionResult.CONSUME;
    }

    private static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private void updateName() {
        setCustomName(Component.literal(junkie ? "Mad Scientist (Junkie)" : "Mad Scientist")
                .withStyle(junkie ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA));
        setCustomNameVisible(false);
    }

    public boolean isJunkie() {
        return junkie;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(JUNKIE, junkie);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        junkie = tag.getBoolean(JUNKIE);
        updateName();
    }

    @Nullable
    @Override
    public Villager getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return ModEntities.MAD_SCIENTIST.get().create(level);
    }
}
