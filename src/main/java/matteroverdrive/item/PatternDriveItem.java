package matteroverdrive.item;

import matteroverdrive.network.PatternData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PatternDriveItem extends Item {
    public static final int LEGACY_CAPACITY = 2;
    public static final int CREATIVE_CAPACITY = 16;

    private static final String PATTERNS_TAG = "MatterOverdrivePatterns";
    private static final String OLD_PATTERN_TAG = "MatterOverdrivePattern";
    private static final String STACK_TAG = "Stack";
    private static final String MATTER_TAG = "Matter";
    private static final String PROGRESS_TAG = "Progress";

    private final boolean creative;

    public PatternDriveItem(Properties properties, boolean creative) {
        super(properties);
        this.creative = creative;
    }

    public boolean isCreative() {
        return creative;
    }

    public static int getCapacity(ItemStack drive) {
        if (drive.getItem() instanceof PatternDriveItem item) {
            return item.creative ? CREATIVE_CAPACITY : LEGACY_CAPACITY;
        }
        return 0;
    }

    public static List<PatternData> getPatterns(ItemStack drive) {
        if (!(drive.getItem() instanceof PatternDriveItem)) {
            return Collections.emptyList();
        }

        List<PatternData> result = new ArrayList<>();
        CompoundTag root = drive.getTag();
        if (root == null) {
            return result;
        }

        if (root.contains(PATTERNS_TAG, Tag.TAG_LIST)) {
            ListTag list = root.getList(PATTERNS_TAG, Tag.TAG_COMPOUND);
            int capacity = getCapacity(drive);
            for (int i = 0; i < list.size() && result.size() < capacity; i++) {
                PatternData pattern = readPattern(list.getCompound(i));
                if (pattern != null) {
                    result.add(pattern);
                }
            }
            return result;
        }

        if (root.contains(OLD_PATTERN_TAG, Tag.TAG_COMPOUND)) {
            PatternData old = readPattern(root.getCompound(OLD_PATTERN_TAG));
            if (old != null) {
                result.add(old);
            }
        }
        return result;
    }

    public static PatternData getPatternAt(ItemStack drive, int index) {
        List<PatternData> patterns = getPatterns(drive);
        if (index < 0 || index >= patterns.size()) {
            return null;
        }
        return patterns.get(index).copy();
    }

    public static boolean hasPattern(ItemStack drive) {
        return !getPatterns(drive).isEmpty();
    }

    public static ItemStack getPatternStack(ItemStack drive) {
        PatternData first = getPatternAt(drive, 0);
        return first == null ? ItemStack.EMPTY : first.stack().copy();
    }

    public static int getMatter(ItemStack drive) {
        PatternData first = getPatternAt(drive, 0);
        return first == null ? 0 : first.matter();
    }

    public static int getProgress(ItemStack drive) {
        PatternData first = getPatternAt(drive, 0);
        return first == null ? 0 : first.progress();
    }

    public static int getProgressFor(ItemStack drive, ItemStack candidate) {
        for (PatternData pattern : getPatterns(drive)) {
            if (pattern.matches(candidate)) {
                return pattern.progress();
            }
        }
        return 0;
    }

    public static boolean matches(ItemStack drive, ItemStack candidate) {
        for (PatternData pattern : getPatterns(drive)) {
            if (pattern.matches(candidate)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canRecordAnalysis(ItemStack drive, ItemStack analyzed) {
        if (!(drive.getItem() instanceof PatternDriveItem) || analyzed.isEmpty()) {
            return false;
        }
        for (PatternData pattern : getPatterns(drive)) {
            if (pattern.matches(analyzed)) {
                return pattern.progress() < 100;
            }
        }
        return getPatterns(drive).size() < getCapacity(drive);
    }

    public static boolean recordAnalysis(ItemStack drive, ItemStack analyzed, int matter, int progressAdded) {
        if (!(drive.getItem() instanceof PatternDriveItem item)
                || analyzed.isEmpty() || matter <= 0 || progressAdded <= 0) {
            return false;
        }

        List<PatternData> patterns = new ArrayList<>(getPatterns(drive));
        for (int i = 0; i < patterns.size(); i++) {
            PatternData current = patterns.get(i);
            if (current.matches(analyzed)) {
                if (current.progress() >= 100) {
                    return false;
                }
                int next = item.creative ? 100 : Math.min(100, current.progress() + progressAdded);
                patterns.set(i, new PatternData(current.stack(), matter, next));
                writePatterns(drive, patterns);
                return true;
            }
        }

        if (patterns.size() >= getCapacity(drive)) {
            return false;
        }

        int progress = item.creative ? 100 : Math.min(100, progressAdded);
        patterns.add(new PatternData(analyzed, matter, progress));
        writePatterns(drive, patterns);
        return true;
    }

    public static boolean setPattern(ItemStack drive, PatternData pattern) {
        if (!(drive.getItem() instanceof PatternDriveItem) || pattern == null || pattern.stack().isEmpty()) {
            return false;
        }
        List<PatternData> patterns = new ArrayList<>(getPatterns(drive));
        for (int i = 0; i < patterns.size(); i++) {
            if (patterns.get(i).matches(pattern.stack())) {
                patterns.set(i, pattern.copy());
                writePatterns(drive, patterns);
                return true;
            }
        }
        if (patterns.size() >= getCapacity(drive)) {
            return false;
        }
        patterns.add(pattern.copy());
        writePatterns(drive, patterns);
        return true;
    }

    private static PatternData readPattern(CompoundTag tag) {
        if (!tag.contains(STACK_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }
        ItemStack stack = ItemStack.of(tag.getCompound(STACK_TAG));
        int matter = Math.max(0, tag.getInt(MATTER_TAG));
        int progress = Math.max(0, Math.min(100, tag.getInt(PROGRESS_TAG)));
        if (stack.isEmpty() || matter <= 0) {
            return null;
        }
        return new PatternData(stack, matter, progress);
    }

    private static void writePatterns(ItemStack drive, List<PatternData> patterns) {
        CompoundTag root = drive.getOrCreateTag();
        ListTag list = new ListTag();
        int capacity = getCapacity(drive);
        for (int i = 0; i < patterns.size() && i < capacity; i++) {
            PatternData data = patterns.get(i);
            CompoundTag pattern = new CompoundTag();
            CompoundTag stackTag = new CompoundTag();
            data.stack().save(stackTag);
            pattern.put(STACK_TAG, stackTag);
            pattern.putInt(MATTER_TAG, data.matter());
            pattern.putInt(PROGRESS_TAG, data.progress());
            list.add(pattern);
        }
        root.put(PATTERNS_TAG, list);
        root.remove(OLD_PATTERN_TAG);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        List<PatternData> patterns = getPatterns(stack);
        if (patterns.isEmpty()) {
            tooltip.add(Component.literal("Empty pattern drive (0/" + getCapacity(stack) + ")"));
        } else {
            tooltip.add(Component.literal("Patterns: " + patterns.size() + "/" + getCapacity(stack)));
            for (PatternData pattern : patterns) {
                tooltip.add(Component.literal(" - ").append(pattern.stack().getHoverName())
                        .append(Component.literal(" [" + pattern.progress() + "%] " + pattern.matter() + " kM")));
            }
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
