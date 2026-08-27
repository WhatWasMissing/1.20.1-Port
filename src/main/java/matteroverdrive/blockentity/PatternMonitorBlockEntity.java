package matteroverdrive.blockentity;

import com.mojang.logging.LogUtils;
import matteroverdrive.menu.PatternMonitorMenu;
import matteroverdrive.network.MatterNetworkUtil;
import matteroverdrive.network.PatternData;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class PatternMonitorBlockEntity extends BlockEntity implements MenuProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int DISPLAY_SLOTS = 12;
    public static final int TASK_QUEUE_CAPACITY = 8;
    public static final int REPLICATION_SEARCH_TIME = 40;

    private final ItemStackHandler displayItems = new ItemStackHandler(DISPLAY_SLOTS) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    };

    private final List<PatternData> displayPatterns = new ArrayList<>();
    private final ArrayDeque<ReplicationRequest> queue = new ArrayDeque<>();
    private int refreshTicker;
    private int dispatchTicker;
    private int lastReportedStorageCount = -1;
    private int lastReportedPatternCount = -1;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            if (index == 0) {
                return displayPatterns.size();
            }
            if (index == 1) {
                return queue.size();
            }
            int patternIndex = index - 2;
            if (patternIndex >= 0 && patternIndex < DISPLAY_SLOTS && patternIndex < displayPatterns.size()) {
                return displayPatterns.get(patternIndex).progress();
            }
            return 0;
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 2 + DISPLAY_SLOTS;
        }
    };

    public PatternMonitorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PATTERN_MONITOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PatternMonitorBlockEntity monitor) {
        monitor.refreshTicker++;
        if (monitor.refreshTicker >= 20) {
            monitor.refreshTicker = 0;
            monitor.refreshPatterns();
        }

        if (monitor.hasUndispatchedRequests()) {
            monitor.dispatchTicker++;
            if (monitor.dispatchTicker >= REPLICATION_SEARCH_TIME) {
                monitor.dispatchTicker = 0;
                monitor.dispatchNextRequest(null);
            }
        } else {
            monitor.dispatchTicker = 0;
        }
    }

    public void refreshPatterns() {
        if (level == null || level.isClientSide) {
            return;
        }

        List<PatternData> refreshed = new ArrayList<>();
        int poweredStorageCount = 0;
        List<PatternStorageBlockEntity> connectedStorages =
                MatterNetworkUtil.findConnected(level, worldPosition, PatternStorageBlockEntity.class);
        for (PatternStorageBlockEntity storage : connectedStorages) {
            if (!storage.isNetworkActive()) {
                continue;
            }
            poweredStorageCount++;
            for (PatternData candidate : storage.getPatterns()) {
                mergePattern(refreshed, candidate);
            }
        }

        if (poweredStorageCount != lastReportedStorageCount || refreshed.size() != lastReportedPatternCount) {
            LOGGER.info(
                    "M2 NETWORK: Pattern Monitor {} discovered {} powered Pattern Storage node(s) and {} pattern(s)",
                    worldPosition, poweredStorageCount, refreshed.size()
            );
            lastReportedStorageCount = poweredStorageCount;
            lastReportedPatternCount = refreshed.size();
        }

        displayPatterns.clear();
        for (int i = 0; i < refreshed.size() && i < DISPLAY_SLOTS; i++) {
            displayPatterns.add(refreshed.get(i).copy());
        }

        for (int slot = 0; slot < DISPLAY_SLOTS; slot++) {
            ItemStack stack = slot < displayPatterns.size() ? displayPatterns.get(slot).stack().copy() : ItemStack.EMPTY;
            displayItems.setStackInSlot(slot, stack);
        }
        setChanged();
    }

    private static void mergePattern(List<PatternData> list, PatternData candidate) {
        for (int i = 0; i < list.size(); i++) {
            PatternData existing = list.get(i);
            if (existing.matches(candidate.stack())) {
                if (candidate.progress() > existing.progress()) {
                    list.set(i, candidate.copy());
                }
                return;
            }
        }
        list.add(candidate.copy());
    }

    public boolean requestReplication(int displayIndex) {
        return requestReplication(displayIndex, null);
    }

    public boolean requestReplication(int displayIndex, @Nullable Player requester) {
        if (level == null || level.isClientSide || displayIndex < 0 || displayIndex >= DISPLAY_SLOTS) {
            LOGGER.warn("M2 NETWORK: Pattern Monitor {} rejected click for slot {} before queueing", worldPosition, displayIndex);
            return false;
        }

        refreshPatterns();
        if (displayIndex >= displayPatterns.size()) {
            LOGGER.warn("M2 NETWORK: Pattern Monitor {} click slot {} has no displayed pattern", worldPosition, displayIndex);
            return false;
        }
        if (queue.size() >= TASK_QUEUE_CAPACITY) {
            LOGGER.warn("M2 NETWORK: Pattern Monitor {} queue is full ({}/{})", worldPosition, queue.size(), TASK_QUEUE_CAPACITY);
            sendDebug(requester, "Pattern Monitor queue is full (" + queue.size() + "/" + TASK_QUEUE_CAPACITY + ")");
            return false;
        }

        PatternData pattern = displayPatterns.get(displayIndex);
        if (pattern.stack().isEmpty() || pattern.matter() <= 0 || pattern.progress() <= 0) {
            LOGGER.warn(
                    "M2 NETWORK: Pattern Monitor {} rejected invalid pattern in slot {} (item={}, matter={}, progress={})",
                    worldPosition, displayIndex, pattern.stack(), pattern.matter(), pattern.progress()
            );
            sendDebug(requester, "Pattern Monitor rejected an invalid pattern in slot " + displayIndex);
            return false;
        }

        UUID requesterId = requester == null ? null : requester.getUUID();
        queue.addLast(new ReplicationRequest(pattern.copy(), 1, false, requesterId));
        setChanged();
        LOGGER.info(
                "M2 NETWORK: Pattern Monitor {} queued replication request for {} (slot {}, queue {}/{})",
                worldPosition, pattern.stack().getHoverName().getString(), displayIndex, queue.size(), TASK_QUEUE_CAPACITY
        );
        sendDebug(requester,
                "Pattern Monitor queued " + pattern.stack().getHoverName().getString()
                        + " | Queue: " + queue.size() + "/" + TASK_QUEUE_CAPACITY);

        dispatchTicker = 0;
        dispatchNextRequest(requester);
        return true;
    }

    private boolean hasUndispatchedRequests() {
        for (ReplicationRequest request : queue) {
            if (!request.dispatched) {
                return true;
            }
        }
        return false;
    }

    private boolean dispatchNextRequest(@Nullable Player debugPlayer) {
        if (level == null || queue.isEmpty()) {
            return false;
        }

        ReplicationRequest request = null;
        for (ReplicationRequest candidate : queue) {
            if (!candidate.dispatched) {
                request = candidate;
                break;
            }
        }
        if (request == null) {
            return false;
        }

        List<ReplicatorBlockEntity> connectedReplicators =
                MatterNetworkUtil.findConnected(level, worldPosition, ReplicatorBlockEntity.class);

        if (connectedReplicators.isEmpty()) {
            LOGGER.warn(
                    "M2 NETWORK: Pattern Monitor {} has {} queued request(s) but discovered 0 Replicators",
                    worldPosition, queue.size()
            );
            sendDebug(debugPlayer,
                    "Pattern Monitor queued " + request.pattern.stack().getHoverName().getString()
                            + " but found no connected Replicator");
            return false;
        }

        LOGGER.info(
                "M2 NETWORK: Pattern Monitor {} dispatching {} to {} connected Replicator(s)",
                worldPosition, request.pattern.stack().getHoverName().getString(), connectedReplicators.size()
        );

        for (ReplicatorBlockEntity replicator : connectedReplicators) {
            if (replicator.queueNetworkReplication(request.pattern, request.amount, worldPosition)) {
                request.dispatched = true;
                setChanged();
                LOGGER.info(
                        "M2 NETWORK: Replicator {} accepted {} from Pattern Monitor {}; monitor queue remains {}/{} until completion",
                        replicator.getBlockPos(), request.pattern.stack().getHoverName().getString(), worldPosition,
                        queue.size(), TASK_QUEUE_CAPACITY
                );
                sendDebug(debugPlayer,
                        "Dispatched " + request.pattern.stack().getHoverName().getString()
                                + " to Replicator " + formatPos(replicator.getBlockPos())
                                + " | Queue stays " + queue.size() + "/" + TASK_QUEUE_CAPACITY + " until completion");
                return true;
            }

            LOGGER.warn(
                    "M2 NETWORK: Replicator {} rejected {} from Pattern Monitor {} (alreadyHasNetworkTask={})",
                    replicator.getBlockPos(), request.pattern.stack().getHoverName().getString(), worldPosition,
                    replicator.hasNetworkTask()
            );
        }

        sendDebug(debugPlayer,
                "Pattern Monitor kept " + request.pattern.stack().getHoverName().getString()
                        + " queued because connected Replicator(s) are busy");
        return false;
    }

    public boolean completeNetworkRequest(ItemStack completedPattern, boolean failed) {
        if (completedPattern.isEmpty()) {
            return false;
        }

        Iterator<ReplicationRequest> iterator = queue.iterator();
        while (iterator.hasNext()) {
            ReplicationRequest request = iterator.next();
            if (request.dispatched && request.pattern.matches(completedPattern)) {
                iterator.remove();
                setChanged();
                LOGGER.info(
                        "M2 NETWORK: Pattern Monitor {} completed {} request (failed={}); queue now {}/{}",
                        worldPosition, completedPattern.getHoverName().getString(), failed, queue.size(), TASK_QUEUE_CAPACITY
                );
                notifyRequester(request,
                        "Replication " + (failed ? "failed" : "completed") + " for "
                                + completedPattern.getHoverName().getString()
                                + " | Pattern Monitor Queue: " + queue.size() + "/" + TASK_QUEUE_CAPACITY);
                return true;
            }
        }

        LOGGER.warn(
                "M2 NETWORK: Pattern Monitor {} received completion for {} but found no matching dispatched queue entry",
                worldPosition, completedPattern.getHoverName().getString()
        );
        return false;
    }

    private void notifyRequester(ReplicationRequest request, String message) {
        if (request.requester == null || level == null || level.getServer() == null) {
            return;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(request.requester);
        sendDebug(player, message);
    }

    private static void sendDebug(@Nullable Player player, String message) {
        if (player != null) {
            player.sendSystemMessage(Component.literal("[MO DEBUG] " + message));
        }
    }

    private static String formatPos(BlockPos pos) {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    public ItemStackHandler getDisplayItems() {
        return displayItems;
    }

    public ContainerData getContainerData() {
        return data;
    }

    public int getQueueSize() {
        return queue.size();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag queueTag = new ListTag();
        for (ReplicationRequest request : queue) {
            CompoundTag entry = new CompoundTag();
            CompoundTag stackTag = new CompoundTag();
            request.pattern.stack().save(stackTag);
            entry.put("Stack", stackTag);
            entry.putInt("Matter", request.pattern.matter());
            entry.putInt("Progress", request.pattern.progress());
            entry.putInt("Amount", request.amount);
            entry.putBoolean("Dispatched", request.dispatched);
            if (request.requester != null) {
                entry.putUUID("Requester", request.requester);
            }
            queueTag.add(entry);
        }
        tag.put("ReplicationQueue", queueTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        queue.clear();
        if (tag.contains("ReplicationQueue", Tag.TAG_LIST)) {
            ListTag queueTag = tag.getList("ReplicationQueue", Tag.TAG_COMPOUND);
            for (int i = 0; i < queueTag.size() && queue.size() < TASK_QUEUE_CAPACITY; i++) {
                CompoundTag entry = queueTag.getCompound(i);
                ItemStack stack = ItemStack.of(entry.getCompound("Stack"));
                int matter = entry.getInt("Matter");
                int progress = entry.getInt("Progress");
                int amount = Math.max(1, entry.getInt("Amount"));
                boolean dispatched = entry.getBoolean("Dispatched");
                UUID requester = entry.hasUUID("Requester") ? entry.getUUID("Requester") : null;
                if (!stack.isEmpty() && matter > 0 && progress > 0) {
                    queue.addLast(new ReplicationRequest(
                            new PatternData(stack, matter, progress), amount, dispatched, requester));
                }
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.matteroverdrive.pattern_monitor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        refreshPatterns();
        return new PatternMonitorMenu(containerId, playerInventory, this);
    }

    private static final class ReplicationRequest {
        private final PatternData pattern;
        private final int amount;
        private boolean dispatched;
        @Nullable
        private final UUID requester;

        private ReplicationRequest(PatternData pattern, int amount, boolean dispatched, @Nullable UUID requester) {
            this.pattern = pattern;
            this.amount = amount;
            this.dispatched = dispatched;
            this.requester = requester;
        }
    }
}
