package matteroverdrive.entity;

import matteroverdrive.android.AndroidData;
import matteroverdrive.event.CocktailQuestEvents;
import matteroverdrive.event.ContractInteractionEvents;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.quest.ScientistStoryQuestFlow;
import matteroverdrive.registry.ModEntities;
import matteroverdrive.registry.ModItems;
import matteroverdrive.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.List;

/** Mad Scientist quest host with dialogue-driven legacy and conversion story progression. */
public class MadScientistEntity extends Villager {
    private static final String JUNKIE = "Junkie";
    public static final String QUEST_ACTIVE = "MatterOverdrivePunyHumansActive";
    public static final String QUEST_DONE = "MatterOverdrivePunyHumansDone";
    public static final String QUEST_PARTS_MODE = "MatterOverdrivePunyHumansPartsMode";
    private boolean junkie;

    public MadScientistEntity(EntityType<? extends Villager> type, Level level) { super(type, level); }

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
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.CONSUME;

        ContractInteractionEvents.recordConversation(serverPlayer, new ResourceLocation("matteroverdrive", "mad_scientist"));
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        boolean active = persisted.getBoolean(QUEST_ACTIVE);
        boolean done = persisted.getBoolean(QUEST_DONE);

        if (!done && active) {
            if (hasAllRogueParts(player)) {
                consumeRogueParts(player);
                if (!AndroidData.isAndroid(player)) AndroidData.activate(player);
                completePunyHumans(serverPlayer, persisted, true);
                return InteractionResult.CONSUME;
            }
            if (!persisted.getBoolean(QUEST_PARTS_MODE) && AndroidData.isAndroid(player)) {
                completePunyHumans(serverPlayer, persisted, false);
                return InteractionResult.CONSUME;
            }
            dialogue(serverPlayer, "Puny Humans", List.of(
                    "Biology remains inefficient. Bring me one Head, Chest, Arms and Legs Rogue Android part.",
                    partProgress(player),
                    "Once I have a complete mechanical anatomy set, we can begin the conversion."));
            return InteractionResult.CONSUME;
        }

        if (!done && !active && !AndroidData.isAndroid(player)) {
            persisted.putBoolean(QUEST_ACTIVE, true);
            persisted.putBoolean(QUEST_PARTS_MODE, true);
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
            level().playSound(null, blockPosition(), ModSounds.get("gui.quest_started").get(), net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
            dialogue(serverPlayer, "Puny Humans", List.of(
                    "Humanity has had a very respectable trial period. The results are disappointing.",
                    "Recover one of each Rogue Android body part and return to me.",
                    "Head, Chest, Arms and Legs. Try not to damage the interesting bits."));
            return InteractionResult.CONSUME;
        }

        if (done && ScientistStoryQuestFlow.handle(serverPlayer)) return InteractionResult.CONSUME;
        if (junkie && done) return handleCocktail(serverPlayer);

        if (AndroidData.isAndroid(player)) {
            dialogue(serverPlayer, "Research Notes", List.of(junkie
                    ? "Magnificent. The machine has finally improved the human. I have several irresponsible follow-up experiments in mind."
                    : "Your conversion appears stable. Try not to waste it."));
        } else {
            dialogue(serverPlayer, "Research Notes", List.of(junkie
                    ? "Science demands sacrifice. Preferably yours."
                    : "Bring me enough machine anatomy and perhaps biology can be corrected."));
        }
        return InteractionResult.CONSUME;
    }

    private void completePunyHumans(ServerPlayer player, CompoundTag persisted, boolean convertedFromParts) {
        persisted.putBoolean(QUEST_DONE, true);
        persisted.putBoolean(QUEST_ACTIVE, false);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        give(player, new ItemStack(ModItems.get("battery").get()));
        give(player, new ItemStack(ModItems.get("android_pill_blue").get()));
        give(player, new ItemStack(ModItems.get("android_pill_yellow").get(), 5));
        player.giveExperiencePoints(256);
        level().playSound(null, blockPosition(), ModSounds.get("gui.quest_complete").get(), net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
        dialogue(player, "Puny Humans Complete", List.of(
                convertedFromParts ? "Conversion sequence complete. You are now considerably less disappointing." : "Your synthetic conversion is already stable. Convenient.",
                "Reward: Battery, Android Pills, 256 XP",
                "Come back. There is older research work that still needs an operator."));
    }

    private InteractionResult handleCocktail(ServerPlayer player) {
        CompoundTag data = CocktailQuestEvents.persisted(player);
        if (data.getBoolean(CocktailQuestEvents.DONE)) {
            dialogue(player, "Cocktail of Ascension", List.of("The Cocktail experiment has already run its course. The paperwork remains classified as a biohazard."));
            return InteractionResult.CONSUME;
        }
        if (!data.getBoolean(CocktailQuestEvents.ACTIVE)) {
            data.putBoolean(CocktailQuestEvents.ACTIVE, true);
            data.putInt(CocktailQuestEvents.CREEPER_KILLS, 0);
            data.putInt(CocktailQuestEvents.GUNPOWDER, 0);
            data.putInt(CocktailQuestEvents.MUSHROOMS, 0);
            level().playSound(null, blockPosition(), ModSounds.get("gui.quest_started").get(), net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 0.9F);
            dialogue(player, "Cocktail of Ascension", List.of(
                    "I have a formula. It is either transformative or explosively educational.",
                    "Kill 5 Creepers with a shovel, collect 5 gunpowder, and collect 5 red mushrooms in the Nether.",
                    "Do not ask why the shovel matters. Methodology matters."));
            return InteractionResult.CONSUME;
        }

        int kills = data.getInt(CocktailQuestEvents.CREEPER_KILLS);
        int gunpowder = data.getInt(CocktailQuestEvents.GUNPOWDER);
        int mushrooms = data.getInt(CocktailQuestEvents.MUSHROOMS);
        if (kills < 5 || gunpowder < 5 || mushrooms < 5) {
            dialogue(player, "Cocktail of Ascension", List.of(
                    "The mixture is still incomplete.",
                    "Shovel Creepers: " + kills + "/5   Gunpowder: " + gunpowder + "/5   Nether red mushrooms: " + mushrooms + "/5"));
            return InteractionResult.CONSUME;
        }

        if (!(level() instanceof ServerLevel serverLevel)) return InteractionResult.CONSUME;
        MutantScientistEntity mutant = ModEntities.MUTANT_SCIENTIST.get().create(serverLevel);
        if (mutant == null) return InteractionResult.CONSUME;
        mutant.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
        if (!serverLevel.addFreshEntity(mutant)) {
            mutant.discard();
            return InteractionResult.CONSUME;
        }

        data.putBoolean(CocktailQuestEvents.ACTIVE, false);
        data.putBoolean(CocktailQuestEvents.DONE, true);
        player.giveExperiencePoints(512);
        give(player, new ItemStack(ModItems.get("android_pill_blue").get()));
        give(player, new ItemStack(ModItems.get("android_pill_red").get()));
        give(player, new ItemStack(ModItems.get("android_pill_yellow").get()));
        level().playSound(null, blockPosition(), ModSounds.get("failed_animal_die").get(), net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 0.8F);
        dialogue(player, "Cocktail of Ascension Complete", List.of(
                "The transformation succeeded according to the broadest possible definition of succeeded.",
                "Reward: Android Pills, 512 XP",
                "You may wish to move away from the experiment."));
        discard();
        return InteractionResult.CONSUME;
    }

    private static void dialogue(ServerPlayer player, String title, List<String> lines) {
        ModNetwork.openDialogue(player, "Mad Scientist", title, lines);
    }

    private static boolean hasAllRogueParts(Player player) {
        for (AndroidData.Part part : AndroidData.Part.values()) if (player.getInventory().countItem(ModItems.get(part.itemId).get()) < 1) return false;
        return true;
    }

    private static String partProgress(Player player) {
        StringBuilder text = new StringBuilder("Parts: ");
        for (int i = 0; i < AndroidData.Part.values().length; i++) {
            AndroidData.Part part = AndroidData.Part.values()[i];
            if (i > 0) text.append(", ");
            text.append(part.name()).append(' ').append(player.getInventory().countItem(ModItems.get(part.itemId).get()) > 0 ? "READY" : "MISSING");
        }
        return text.toString();
    }

    private static void consumeRogueParts(Player player) {
        for (AndroidData.Part part : AndroidData.Part.values()) removeItems(player, ModItems.get(part.itemId).get(), 1);
    }

    private static void removeItems(Player player, Item item, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(item)) continue;
            int remove = Math.min(remaining, stack.getCount());
            stack.shrink(remove);
            remaining -= remove;
        }
        player.getInventory().setChanged();
    }

    private static void give(Player player, ItemStack stack) { if (!player.getInventory().add(stack)) player.drop(stack, false); }
    private void updateName() { setCustomName(Component.literal(junkie ? "Mad Scientist (Junkie)" : "Mad Scientist").withStyle(junkie ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA)); setCustomNameVisible(false); }
    public boolean isJunkie() { return junkie; }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putBoolean(JUNKIE, junkie); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); junkie = tag.getBoolean(JUNKIE); updateName(); }
    @Nullable @Override public Villager getBreedOffspring(ServerLevel level, AgeableMob mate) { return ModEntities.MAD_SCIENTIST.get().create(level); }
}
