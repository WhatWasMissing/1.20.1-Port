package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.blockentity.GravitationalAnomalyBlockEntity;
import matteroverdrive.blockentity.TransporterBlockEntity;
import matteroverdrive.item.ContractItem;
import matteroverdrive.item.MatterScannerItem;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.quest.ContractStageSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ContractEvents {
    private static final Map<UUID, PlayerSnapshot> LAST_PLAYER_STATE = new HashMap<>();
    private static final Map<UUID, String> LAST_SCANNER_SIGNATURE = new HashMap<>();

    private ContractEvents() {}

    @SubscribeEvent
    public static void pickup(PlayerEvent.ItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack picked = event.getStack();
        if (picked.isEmpty() || picked.getCount() <= 0) return;
        advanceMatching(player, picked.getCount(), contract -> ContractItem.advancesWithPickup(contract, picked));
    }

    @SubscribeEvent
    public static void crafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack crafted = event.getCrafting();
        if (crafted.isEmpty()) return;
        advanceMatching(player, Math.max(1, crafted.getCount()), contract -> ContractItem.advancesWithCraft(contract, crafted));
    }

    @SubscribeEvent
    public static void mined(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        ResourceLocation block = ForgeRegistries.BLOCKS.getKey(event.getState().getBlock());
        if (block == null) return;
        advanceMatching(player, 1, contract -> ContractItem.advancesWithMine(contract, block));
    }

    @SubscribeEvent
    public static void kill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        ResourceLocation type = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
        if (type == null) return;
        boolean baby = event.getEntity().isBaby();
        advanceMatching(player, 1, contract -> ContractItem.advancesWithKill(contract, type, baby));
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        UUID id = player.getUUID();
        PlayerSnapshot previous = LAST_PLAYER_STATE.put(id,
                new PlayerSnapshot(player.getX(), player.getY(), player.getZ(), player.level().dimension().location()));

        if (previous != null && hasIncompleteType(player, "transport")
                && previous.dimension().equals(player.level().dimension().location())) {
            double dx = player.getX() - previous.x();
            double dy = player.getY() - previous.y();
            double dz = player.getZ() - previous.z();
            if (dx * dx + dy * dy + dz * dz >= 64.0D && wasOnTransporter(player, previous)) recordTransport(player);
        }

        if (hasIncompleteType(player, "anomaly") && insideAnomalyHorizon(player)) recordAnomalyHorizon(player);
        if (hasIncompleteType(player, "scan")) pollScanner(player);
        if (player.tickCount % 20 == 0) ModNetwork.syncQuestTracker(player);
    }

    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) ModNetwork.syncQuestTracker(player);
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        LAST_PLAYER_STATE.remove(id);
        LAST_SCANNER_SIGNATURE.remove(id);
    }

    public static void recordScan(ServerPlayer player, ResourceLocation block) {
        if (player == null || block == null) return;
        advanceMatching(player, 1, contract -> ContractItem.advancesWithScan(contract, block));
    }

    public static void recordTransport(ServerPlayer player) {
        if (player == null) return;
        advanceMatching(player, 1, ContractItem::advancesWithTransport);
    }

    public static void recordAnomalyHorizon(ServerPlayer player) {
        if (player == null) return;
        advanceMatching(player, 1, ContractItem::advancesWithAnomaly);
    }

    private static void pollScanner(ServerPlayer player) {
        String newestSignature = null;
        String newestName = null;
        for (ItemStack stack : player.getInventory().items) {
            if (!(stack.getItem() instanceof MatterScannerItem) || !stack.hasTag()) continue;
            String name = stack.getTag().getString("ScannerLastScan");
            if (name.isBlank()) continue;
            newestSignature = name + "#" + stack.getTag().getInt("ScannerLastProgress");
            newestName = name;
            break;
        }
        if (newestSignature == null) return;
        String previous = LAST_SCANNER_SIGNATURE.put(player.getUUID(), newestSignature);
        if (previous == null || previous.equals(newestSignature)) return;
        final String scannedName = newestName;
        advanceMatching(player, 1, contract -> ContractItem.advancesWithScanName(contract, scannedName));
    }

    private static boolean wasOnTransporter(ServerPlayer player, PlayerSnapshot previous) {
        BlockPos centre = BlockPos.containing(previous.x(), previous.y(), previous.z());
        for (int dx = -1; dx <= 1; dx++) for (int dy = -2; dy <= 0; dy++) for (int dz = -1; dz <= 1; dz++) {
            if (player.level().getBlockEntity(centre.offset(dx, dy, dz)) instanceof TransporterBlockEntity) return true;
        }
        return false;
    }

    private static boolean insideAnomalyHorizon(ServerPlayer player) {
        BlockPos centre = player.blockPosition();
        for (int dx = -2; dx <= 2; dx++) for (int dy = -2; dy <= 2; dy++) for (int dz = -2; dz <= 2; dz++) {
            if (!(player.level().getBlockEntity(centre.offset(dx, dy, dz)) instanceof GravitationalAnomalyBlockEntity anomaly)) continue;
            double distance = player.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(anomaly.getBlockPos()));
            if (distance <= anomaly.getEventHorizon()) return true;
        }
        return false;
    }

    private static boolean hasIncompleteType(ServerPlayer player, String type) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ContractItem && !ContractItem.complete(stack) && type.equals(ContractItem.type(stack))) return true;
        }
        return false;
    }

    private static void advanceMatching(ServerPlayer player, int amount, ContractPredicate predicate) {
        boolean changed = false;
        boolean completed = false;
        String latestTitle = null;
        for (ItemStack contract : player.getInventory().items) {
            if (!(contract.getItem() instanceof ContractItem) || ContractItem.complete(contract) || !predicate.test(contract)) continue;
            if (ContractStageSupport.advanceAndCheck(contract, amount)) {
                completed = true;
                latestTitle = ContractItem.title(contract);
            }
            changed = true;
        }
        if (!changed) return;
        syncProgress(player);
        if (completed) notifyCompletion(player, latestTitle == null ? "Contract" : latestTitle);
        ModNetwork.syncQuestTracker(player);
    }

    private static void notifyCompletion(ServerPlayer player, String title) {
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Contract complete: " + title), true);
        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(MatterOverdrive.MOD_ID, "gui.quest_complete"));
        if (sound != null) player.playNotifySound(sound, SoundSource.PLAYERS, 0.9F, 1.0F);
    }

    private static void syncProgress(ServerPlayer player) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
    }

    @FunctionalInterface
    private interface ContractPredicate { boolean test(ItemStack contract); }
    private record PlayerSnapshot(double x, double y, double z, ResourceLocation dimension) {}
}
