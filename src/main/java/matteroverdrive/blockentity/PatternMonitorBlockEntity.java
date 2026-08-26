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

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

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

        if (!monitor.queue.isEmpty()) {
            monitor.dispatchTicker++;
            if (monitor.dispatchTicker >= REPLICATION_SEARCH_TIME) {
                monitor.dispatchTicker = 0;
                monitor.dispatchNextRequest();
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
        if (level == null || level.isClientSide || displayIndex < 0 || displayIndex >= DISPLAY_SLOTS) {
            return false;
        }
        refreshPatterns();
        if (displayIndex >= displayPatterns.size() || queue.size() >= TASK_QUEUE_CAPACITY) {
            return false;
        }
        PatternData pattern = displayPatterns.get(displayIndex);
        if (pattern.stack().isEmpty() || pattern.matter() <= 0 || pattern.progress() <= 0) {
            return false;
        }
        queue.addLast(new ReplicationRequest(pattern.copy(), 1));
        setChanged();
        return true;
    }

    private void dispatchNextRequest() {
        if (level == null || queue.isEmpty()) {
            return;
        }
        ReplicationRequest request = queue.peekFirst();
        for (ReplicatorBlockEntity replicator : MatterNetworkUtil.findConnected(level, worldPosition, ReplicatorBlockEntity.class)) {
            if (replicator.queueNetworkReplication(request.pattern(), request.amount())) {
                queue.removeFirst();
                setChanged();
                return;
            }
        }
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
            request.pattern().stack().save(stackTag);
            entry.put("Stack", stackTag);
            entry.putInt("Matter", request.pattern().matter());
            entry.putInt("Progress", request.pattern().progress());
            entry.putInt("Amount", request.amount());
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
                if (!stack.isEmpty() && matter > 0 && progress > 0) {
                    queue.addLast(new ReplicationRequest(new PatternData(stack, matter, progress), amount));
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

    private record ReplicationRequest(PatternData pattern, int amount) {
    }
}
