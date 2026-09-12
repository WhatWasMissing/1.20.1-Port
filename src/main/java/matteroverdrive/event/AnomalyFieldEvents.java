package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.blockentity.GravitationalAnomalyBlockEntity;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Player-proximity anomaly phenomena. This deliberately inspects only chunks already
 * loaded around the player and never creates tickets, stamps structures, or scans a
 * large block cube synchronously.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AnomalyFieldEvents {
    private static final String ROOT_KEY = "MatterOverdriveAnomalyField";
    private static final int SCAN_INTERVAL = 40;
    private static final double MAX_EVENT_DISTANCE = 24.0D;
    private static final int ALL_SIGNATURES = 0b1111;

    private AnomalyFieldEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % SCAN_INTERVAL != 0) return;
        ServerLevel level = player.serverLevel();
        GravitationalAnomalyBlockEntity anomaly = nearestLoadedAnomaly(level, player);
        if (anomaly == null) return;

        double distance = Math.sqrt(anomaly.getBlockPos().distSqr(player.blockPosition()));
        double activeDistance = Math.min(MAX_EVENT_DISTANCE, Math.max(7.0D, anomaly.getMaxRange() + 5.0D));
        if (distance > activeDistance) return;

        CompoundTag root = root(player);
        long now = level.getGameTime();
        if (root.getLong("NextEvent") > now) return;

        // Events become more likely close to the anomaly while remaining sparse enough
        // to feel like field phenomena rather than a repeating potion aura.
        double proximity = 1.0D - Math.min(1.0D, distance / Math.max(1.0D, activeDistance));
        if (level.random.nextDouble() > 0.18D + proximity * 0.42D) return;

        int eventId = level.random.nextInt(4);
        boolean equalized = player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.get("spacetime_equalizer").get());
        trigger(player, eventId, equalized);

        int mask = root.getInt("Signatures") | (1 << eventId);
        root.putInt("Signatures", mask & ALL_SIGNATURES);
        root.putInt("EventsWitnessed", Math.min(10_000, root.getInt("EventsWitnessed") + 1));
        int baseCooldown = equalized ? 20 * 45 : 20 * 30;
        root.putLong("NextEvent", now + baseCooldown + level.random.nextInt(20 * 25));
        saveRoot(player, root);

        if ((mask & ALL_SIGNATURES) == ALL_SIGNATURES) grant(player, "anomaly_field_observer");
    }

    public static int signatures(ServerPlayer player) {
        return Integer.bitCount(root(player).getInt("Signatures") & ALL_SIGNATURES);
    }

    public static int eventsWitnessed(ServerPlayer player) {
        return Math.max(0, root(player).getInt("EventsWitnessed"));
    }

    public static String status(ServerPlayer player) {
        return "Anomaly signatures: " + signatures(player) + "/4 • field events observed: " + eventsWitnessed(player);
    }

    private static void trigger(ServerPlayer player, int eventId, boolean equalized) {
        int scale = equalized ? 1 : 2;
        switch (eventId) {
            case 0 -> {
                player.sendSystemMessage(Component.literal("PDA // GRAVITY SHEAR: local vector field is oscillating.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 6, 0, false, false, true));
                if (!equalized) player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 4, 0, false, false, true));
                ModNetwork.sendPdaVoice(player, "anomaly_warning");
            }
            case 1 -> {
                player.sendSystemMessage(Component.literal("PDA // TEMPORAL DRAG: local action timing has lost phase coherence.")
                        .withStyle(ChatFormatting.DARK_AQUA));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 4 * scale, equalized ? 0 : 1, false, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20 * 5 * scale, 0, false, false, true));
                ModNetwork.sendPdaVoice(player, "signal_echo");
            }
            case 2 -> {
                player.sendSystemMessage(Component.literal("PDA // RESONANCE ECHO: sensory state does not match a single local timestamp.")
                        .withStyle(ChatFormatting.AQUA));
                if (!equalized) player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 5, 0, false, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 4, 0, false, false, true));
                ModNetwork.sendPdaVoice(player, "matter_resonance");
            }
            default -> {
                player.sendSystemMessage(Component.literal("PDA // MATTER STATIC: replicated-state noise is coupling to nearby inventory patterns.")
                        .withStyle(ChatFormatting.YELLOW));
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 20 * 8, equalized ? 0 : 1, false, false, true));
                ModNetwork.sendPdaVoice(player, "matter_resonance");
            }
        }
        if (equalized) {
            player.displayClientMessage(Component.literal("Space-Time Equalizer reduced the field effect.")
                    .withStyle(ChatFormatting.GREEN), true);
        }
    }

    private static GravitationalAnomalyBlockEntity nearestLoadedAnomaly(ServerLevel level, ServerPlayer player) {
        int playerChunkX = player.blockPosition().getX() >> 4;
        int playerChunkZ = player.blockPosition().getZ() >> 4;
        GravitationalAnomalyBlockEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(playerChunkX + dx, playerChunkZ + dz);
                if (chunk == null) continue;
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (!(blockEntity instanceof GravitationalAnomalyBlockEntity anomaly)) continue;
                    double distance = anomaly.getBlockPos().distSqr(player.blockPosition());
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearest = anomaly;
                    }
                }
            }
        }
        return nearest;
    }

    private static void grant(ServerPlayer player, String path) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(
                ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, "campaign/" + path));
        if (advancement == null || player.getAdvancements().getOrStartProgress(advancement).isDone()) return;
        for (String criterion : advancement.getCriteria().keySet()) player.getAdvancements().award(advancement, criterion);
    }

    private static CompoundTag root(ServerPlayer player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        return persisted.getCompound(ROOT_KEY).copy();
    }

    private static void saveRoot(ServerPlayer player, CompoundTag root) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(ROOT_KEY, root);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }
}
