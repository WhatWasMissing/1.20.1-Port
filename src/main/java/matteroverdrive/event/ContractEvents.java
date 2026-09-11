package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.blockentity.GravitationalAnomalyBlockEntity;
import matteroverdrive.blockentity.TransporterBlockEntity;
import matteroverdrive.item.ContractItem;
import matteroverdrive.item.MatterScannerItem;
import matteroverdrive.registry.ModItems;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.quest.ContractStageSupport;
import matteroverdrive.world.TechnologySiteDiscoverySavedData;
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
import java.util.Locale;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ContractEvents {
    private static final Map<UUID, PlayerSnapshot> LAST_PLAYER_STATE = new HashMap<>();
    private static final Map<UUID, String> LAST_SCANNER_SIGNATURE = new HashMap<>();
    private static final Map<UUID, Long> LAST_DISCOVERY_CHUNK = new HashMap<>();

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
        if (player.tickCount % 20 == 0) discoverTechnologySite(player);
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
        LAST_DISCOVERY_CHUNK.remove(id);
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

    /** Converts generated compact sites from scenery into persistent, readable field objectives. */
    private static void discoverTechnologySite(ServerPlayer player) {
        long chunkKey = ((long) player.chunkPosition().x << 32) ^ (player.chunkPosition().z & 0xffffffffL);
        Long previousChunk = LAST_DISCOVERY_CHUNK.put(player.getUUID(), chunkKey);
        if (previousChunk != null && chunkKey == previousChunk) return;
        java.util.Map<String, BlockPos> anchors = new java.util.LinkedHashMap<>();
        java.util.Map<String, Integer> matches = new java.util.HashMap<>();
        java.util.List<BlockPos> analyzers = new java.util.ArrayList<>(), controllers = new java.util.ArrayList<>(), containments = new java.util.ArrayList<>();
        java.util.List<BlockPos> chargers = new java.util.ArrayList<>(), switches = new java.util.ArrayList<>(), excavators = new java.util.ArrayList<>(), matrices = new java.util.ArrayList<>(), relays = new java.util.ArrayList<>(), capacitors = new java.util.ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(player.blockPosition().offset(-12, -6, -12), player.blockPosition().offset(12, 6, 12))) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(player.level().getBlockState(pos).getBlock());
            if (id == null || !MatterOverdrive.MOD_ID.equals(id.getNamespace())) continue;
            String path = id.getPath();
            if (path.equals("matter_analyzer")) analyzers.add(pos.immutable());
            if (path.equals("facility_network_controller")) controllers.add(pos.immutable());
            if (path.equals("anomaly_containment_unit")) containments.add(pos.immutable());
            if (path.equals("charging_station")) chargers.add(pos.immutable());
            if (path.equals("network_switch")) switches.add(pos.immutable());
            if (path.equals("matter_excavator")) excavators.add(pos.immutable());
            if (path.equals("matter_storage_matrix")) matrices.add(pos.immutable());
            if (path.equals("android_induction_relay")) relays.add(pos.immutable());
            if (path.equals("grid_capacitor")) capacitors.add(pos.immutable());
        }
        for (BlockPos analyzer : analyzers) for (BlockPos controller : controllers)
            if (analyzer.distSqr(controller) <= 36.0D) {
                matches.merge("matter_observatory", 1, Integer::sum);
                anchors.putIfAbsent("matter_observatory", analyzer);
                break;
            }
        for (BlockPos containment : containments) for (BlockPos controller : controllers)
            if (containment.distSqr(controller) <= 36.0D) {
                matches.merge("anomaly_research_site", 1, Integer::sum);
                anchors.putIfAbsent("anomaly_research_site", containment);
                break;
            }
        pairDiscovery(matches, anchors, "field_logistics_depot", chargers, switches);
        pairDiscovery(matches, anchors, "abandoned_matter_lab", excavators, matrices);
        pairDiscovery(matches, anchors, "android_relay_outpost", relays, capacitors);
        String site = matches.entrySet().stream().filter(entry -> entry.getValue() >= 2)
                .map(java.util.Map.Entry::getKey).findFirst().orElse(null);
        BlockPos anchor = site == null ? null : anchors.get(site);
        if (site == null || anchor == null || !(player.level() instanceof net.minecraft.server.level.ServerLevel level)) return;
        TechnologySiteDiscoverySavedData discoveries = TechnologySiteDiscoverySavedData.get(level);
        if (!discoveries.discover(level, player.getUUID(), site, anchor.asLong())) return;
        int chainStage = discoveries.advanceChain(player.getUUID(), site);
        player.giveExperiencePoints(25);
        ItemStack pad = new ItemStack(ModItems.get("data_pad").get());
        if (!player.getInventory().add(pad)) player.drop(pad, false);
        ItemStack dossier = new ItemStack(ModItems.get("facility_research").get());
        dossier.getOrCreateTag().putString("FacilityArchive", researchArchive(site));
        if (!player.getInventory().add(dossier)) player.drop(dossier, false);
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("FIELD DISCOVERY: " + site.replace('_', ' ').toUpperCase(Locale.ROOT) + " logged. Data Pad updated; research dossier recovered.").withStyle(net.minecraft.ChatFormatting.AQUA));
        if (chainStage > 0) {
            if (chainStage == 3) {
                ItemStack reward = new ItemStack(ModItems.get("upgrade_parallel_processing").get());
                if (!player.getInventory().add(reward)) player.drop(reward, false);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("EXPLORATION CHAIN COMPLETE: anomaly investigation cleared. Parallel Processing Upgrade recovered.").withStyle(net.minecraft.ChatFormatting.GOLD));
            } else {
                String next = TechnologySiteDiscoverySavedData.nextChainSite(player.getUUID(), chainStage);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("INVESTIGATION " + chainStage + "/3: follow the evidence to " + next.replace('_', ' ') + ".").withStyle(net.minecraft.ChatFormatting.YELLOW));
            }
        }
    }

    private static void pairDiscovery(java.util.Map<String, Integer> matches, java.util.Map<String, BlockPos> anchors,
                                      String site, java.util.List<BlockPos> first, java.util.List<BlockPos> second) {
        for (BlockPos a : first) for (BlockPos b : second) if (a.distSqr(b) <= 36.0D) {
            matches.merge(site, 1, Integer::sum);
            anchors.putIfAbsent(site, a);
            return;
        }
    }

    private static String researchArchive(String site) {
        return switch (site) {
            case "abandoned_matter_lab" -> "ABANDONED_MATTER_LAB";
            case "android_relay_outpost" -> "ANDROID_RELAY_OUTPOST";
            case "anomaly_research_site" -> "ANOMALY_RESEARCH_SITE";
            case "matter_observatory" -> "MATTER_OBSERVATORY";
            case "field_logistics_depot" -> "FIELD_LOGISTICS_DEPOT";
            default -> "BLACK_SITE";
        };
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
        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, "gui.quest_complete"));
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
