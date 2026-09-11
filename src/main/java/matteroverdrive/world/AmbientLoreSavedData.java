package matteroverdrive.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** World-persistent per-player collection of optional physical lore fragments. */
public final class AmbientLoreSavedData extends SavedData {
    private static final String ID = "matteroverdrive_ambient_lore";
    private static final int MAX_PLAYERS = 4096;
    private static final int MAX_RECORDS_PER_PLAYER = 128;

    private final Map<UUID, LinkedHashSet<String>> recovered = new HashMap<>();

    public static AmbientLoreSavedData get(ServerLevel level) {
        ServerLevel owner = level.getServer().overworld();
        return owner.getDataStorage().computeIfAbsent(AmbientLoreSavedData::load, AmbientLoreSavedData::new, ID);
    }

    public boolean discover(UUID player, String id) {
        if (player == null || AmbientLoreCatalog.byId(id) == null) return false;
        if (recovered.size() >= MAX_PLAYERS && !recovered.containsKey(player)) return false;
        LinkedHashSet<String> set = recovered.computeIfAbsent(player, ignored -> new LinkedHashSet<>());
        if (set.size() >= MAX_RECORDS_PER_PLAYER || !set.add(id)) return false;
        setDirty();
        return true;
    }

    public boolean has(UUID player, String id) {
        Set<String> set = recovered.get(player);
        return set != null && set.contains(id);
    }

    public int count(UUID player) {
        Set<String> set = recovered.get(player);
        return set == null ? 0 : set.size();
    }

    public List<String> ids(UUID player) {
        Set<String> set = recovered.get(player);
        if (set == null || set.isEmpty()) return List.of();
        return List.copyOf(set);
    }

    public List<AmbientLoreCatalog.Entry> entries(UUID player) {
        List<AmbientLoreCatalog.Entry> result = new ArrayList<>();
        for (String id : ids(player)) {
            AmbientLoreCatalog.Entry entry = AmbientLoreCatalog.byId(id);
            if (entry != null) result.add(entry);
        }
        return List.copyOf(result);
    }

    public static AmbientLoreSavedData load(CompoundTag tag) {
        AmbientLoreSavedData data = new AmbientLoreSavedData();
        ListTag players = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size() && i < MAX_PLAYERS; i++) {
            CompoundTag playerTag = players.getCompound(i);
            if (!playerTag.hasUUID("Player")) continue;
            LinkedHashSet<String> ids = new LinkedHashSet<>();
            ListTag records = playerTag.getList("Records", Tag.TAG_STRING);
            for (int j = 0; j < records.size() && ids.size() < MAX_RECORDS_PER_PLAYER; j++) {
                String id = records.getString(j);
                if (AmbientLoreCatalog.byId(id) != null) ids.add(id);
            }
            if (!ids.isEmpty()) data.recovered.put(playerTag.getUUID("Player"), ids);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag players = new ListTag();
        recovered.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("Player", entry.getKey());
            ListTag records = new ListTag();
            entry.getValue().stream().limit(MAX_RECORDS_PER_PLAYER)
                    .forEach(id -> records.add(StringTag.valueOf(id)));
            playerTag.put("Records", records);
            players.add(playerTag);
        });
        tag.put("Players", players);
        return tag;
    }
}
