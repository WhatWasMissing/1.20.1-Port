package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.android.AndroidData;
import matteroverdrive.registry.ModItems;
import matteroverdrive.network.ModNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AndroidEvents {
    public static final String ROGUE_ANDROID_TAG = "MatterOverdriveRogueAndroid";

    private AndroidEvents() {}

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        AndroidData.copyTo(event.getOriginal(), event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (event.player instanceof ServerPlayer player && player.tickCount % 2 == 0) {
            ModNetwork.syncAndroidState(player);
        }
        if (event.player.tickCount % 20 != 0) return;
        if (!AndroidData.isAndroid(event.player) || AndroidData.getEnergy(event.player) <= 0) return;

        int activeParts = Integer.bitCount(AndroidData.getParts(event.player));
        if (activeParts == 0) return;
        if (AndroidData.consumeEnergy(event.player, activeParts * 5) < activeParts * 5) return;

        if (AndroidData.hasPart(event.player, AndroidData.Part.HEAD)) {
            event.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 45, 0, true, false, false));
        }
        if (AndroidData.hasPart(event.player, AndroidData.Part.CHEST)) {
            event.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 45, 0, true, false, false));
        }
        if (AndroidData.hasPart(event.player, AndroidData.Part.LEGS)) {
            event.player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 45, 0, true, false, false));
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer attacker
                && AndroidData.hasPart(attacker, AndroidData.Part.ARMS)
                && AndroidData.consumeEnergy(attacker, 80) == 80) {
            event.setAmount(event.getAmount() + 3.0F);
        }
        if (event.getEntity() instanceof ServerPlayer target
                && AndroidData.hasPart(target, AndroidData.Part.CHEST)
                && AndroidData.consumeEnergy(target, 50) == 50) {
            event.setAmount(event.getAmount() * 0.75F);
        }
    }

    @SubscribeEvent
    public static void onRogueAndroidDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Husk husk)
                || !husk.getPersistentData().getBoolean(ROGUE_ANDROID_TAG)) return;
        AndroidData.Part[] parts = AndroidData.Part.values();
        AndroidData.Part part = parts[husk.getRandom().nextInt(parts.length)];
        event.getDrops().add(new ItemEntity(husk.level(), husk.getX(), husk.getY(), husk.getZ(),
                new ItemStack(ModItems.get(part.itemId).get())));
    }
}