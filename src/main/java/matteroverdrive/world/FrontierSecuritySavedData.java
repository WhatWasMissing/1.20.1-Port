package matteroverdrive.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Persistent per-player hostile-clearance state for Frontier Expedition structures. */
public final class FrontierSecuritySavedData extends SavedData {
    private static final String ID = "matteroverdrive_frontier_security";
    private static final int MAX_PROGRESS = 8192;
    private static final int MAX_KEY_LENGTH = 256;

    private final Map<String, Integer> progress = new HashMap<>();
    private final Set<String> secured = new HashSet<>();

    public static FrontierSecuritySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FrontierSecuritySavedData::load,
                FrontierSecuritySavedData::new, ID);
    }

    public ClearanceResult recordKill(ServerLevel level, UUID player, String site, long startChunk, int target) {
        String key = level.dimension().location() + ":" + player + ":" + site + ":" + startChunk;
        if (key.length() > MAX_KEY_LENGTH || progress.size() >= MAX_PROGRESS) return new ClearanceResult(0, target, false, true);
        if (secured.contains(key)) return new ClearanceResult(target, target, false, false);
        int value = Math.min(target, progress.getOrDefault(key, 0) + 1);
        progress.put(key, value);
        boolean completed = value >= target;
        if (completed) secured.add(key);
        setDirty();
        return new ClearanceResult(value, target, completed, false);
    }

    public static FrontierSecuritySavedData load(CompoundTag tag) {
        FrontierSecuritySavedData data = new FrontierSecuritySavedData();
        ListTag entries = tag.getList("Progress", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size() && i < MAX_PROGRESS; i++) {
            CompoundTag entry = entries.getCompound(i);
            String key = entry.getString("Key");
            if (!key.isBlank() && key.length() <= MAX_KEY_LENGTH)
                data.progress.put(key, Math.max(0, Math.min(64, entry.getInt("Value"))));
        }
        ListTag secured = tag.getList("Secured", net.minecraft.nbt.Tag.TAG_STRING);
        for (int i = 0; i < secured.size() && i < MAX_PROGRESS; i++) {
            String key = secured.getString(i);
            if (key.length() <= MAX_KEY_LENGTH) data.secured.add(key);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag entries = new ListTag();
        progress.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            CompoundTag value = new CompoundTag();
            value.putString("Key", entry.getKey());
            value.putInt("Value", entry.getValue());
            entries.add(value);
        });
        tag.put("Progress", entries);
        ListTag securedList = new ListTag();
        secured.stream().sorted().forEach(key -> securedList.add(StringTag.valueOf(key)));
        tag.put("Secured", securedList);
        return tag;
    }

    public record ClearanceResult(int progress, int target, boolean completed, boolean rejected) {}
}
