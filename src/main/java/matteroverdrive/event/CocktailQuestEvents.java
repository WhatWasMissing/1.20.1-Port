package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Source-backed progress tracking for Cocktail of Ascension. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID)
public final class CocktailQuestEvents {
    public static final String ACTIVE = "MatterOverdriveCocktailActive";
    public static final String DONE = "MatterOverdriveCocktailDone";
    public static final String CREEPER_KILLS = "MatterOverdriveCocktailCreeperShovelKills";
    public static final String GUNPOWDER = "MatterOverdriveCocktailGunpowder";
    public static final String MUSHROOMS = "MatterOverdriveCocktailNetherRedMushrooms";
    public static final int REQUIRED_CREEPERS = 5;
    public static final int REQUIRED_GUNPOWDER = 5;
    public static final int REQUIRED_MUSHROOMS = 5;

    private CocktailQuestEvents() {}

    public static CompoundTag persisted(Player player) {
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
        return persisted;
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Creeper)) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        CompoundTag data = persisted(player);
        if (!data.getBoolean(ACTIVE) || data.getBoolean(DONE)) return;
        if (!(player.getMainHandItem().getItem() instanceof ShovelItem)) return;
        int kills = Math.min(REQUIRED_CREEPERS, data.getInt(CREEPER_KILLS) + 1);
        data.putInt(CREEPER_KILLS, kills);
        player.displayClientMessage(Component.literal(
                "Cocktail of Ascension: shovel Creepers " + kills + "/" + REQUIRED_CREEPERS)
                .withStyle(kills >= REQUIRED_CREEPERS ? ChatFormatting.GREEN : ChatFormatting.AQUA), false);
    }

    @SubscribeEvent
    public static void onPickup(PlayerEvent.ItemPickupEvent event) {
        Player player = event.getEntity();
        CompoundTag data = persisted(player);
        if (!data.getBoolean(ACTIVE) || data.getBoolean(DONE)) return;
        var picked = event.getStack();
        if (picked.isEmpty()) return;

        if (picked.is(Items.GUNPOWDER)) {
            int before = data.getInt(GUNPOWDER);
            int take = Math.min(picked.getCount(), Math.max(0, REQUIRED_GUNPOWDER - before));
            if (take > 0) {
                picked.shrink(take);
                data.putInt(GUNPOWDER, before + take);
                player.displayClientMessage(Component.literal("Cocktail: gunpowder " + (before + take) + "/5")
                        .withStyle(ChatFormatting.AQUA), false);
            }
            return;
        }

        if (picked.is(Items.RED_MUSHROOM) && player.level().dimension() == Level.NETHER) {
            int before = data.getInt(MUSHROOMS);
            int take = Math.min(picked.getCount(), Math.max(0, REQUIRED_MUSHROOMS - before));
            if (take > 0) {
                picked.shrink(take);
                data.putInt(MUSHROOMS, before + take);
                player.displayClientMessage(Component.literal("Cocktail: Nether red mushrooms " + (before + take) + "/5")
                        .withStyle(ChatFormatting.LIGHT_PURPLE), false);
            }
        }
    }
}
