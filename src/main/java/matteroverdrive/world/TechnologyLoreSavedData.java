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

/** World-persistent, per-player set of technology PDA entries discovered in actual play. */
public final class TechnologyLoreSavedData extends SavedData {
    private static final String ID = "matteroverdrive_technology_lore";
    private static final int MAX_PLAYERS = 4096;
    private static final int MAX_RECORDS_PER_PLAYER = 256;

    private final Map<UUID, LinkedHashSet<String>> discovered = new HashMap<>();

    public static TechnologyLoreSavedData get(ServerLevel level) {
        ServerLevel owner = level.getServer().overworld();
        return owner.getDataStorage().computeIfAbsent(TechnologyLoreSavedData::load, TechnologyLoreSavedData::new, ID);
    }

    public boolean discover(UUID player, String itemId) {
        TechnologyLoreCatalog.TechRecord record = TechnologyLoreCatalog.byItemId(itemId);
        if (player == null || record == null) return false;
        if (discovered.size() >= MAX_PLAYERS && !discovered.containsKey(player)) return false;
        LinkedHashSet<String> set = discovered.computeIfAbsent(player, ignored -> new LinkedHashSet<>());
        if (set.size() >= MAX_RECORDS_PER_PLAYER || !set.add(record.id())) return false;
        setDirty();
        return true;
    }

    public boolean has(UUID player, String itemId) {
        TechnologyLoreCatalog.TechRecord record = TechnologyLoreCatalog.byItemId(itemId);
        if (record == null) return false;
        Set<String> set = discovered.get(player);
        return set != null && set.contains(record.id());
    }

    public int count(UUID player) {
        Set<String> set = discovered.get(player);
        return set == null ? 0 : set.size();
    }

    public List<String> ids(UUID player) {
        Set<String> set = discovered.get(player);
        if (set == null || set.isEmpty()) return List.of();
        return List.copyOf(set);
    }

    public List<TechnologyLoreCatalog.TechRecord> entries(UUID player) {
        List<TechnologyLoreCatalog.TechRecord> result = new ArrayList<>();
        for (String id : ids(player)) {
            TechnologyLoreCatalog.TechRecord record = TechnologyLoreCatalog.byItemId(id);
            if (record != null) result.add(record);
        }
        return List.copyOf(result);
    }

    public static TechnologyLoreSavedData load(CompoundTag tag) {
        TechnologyLoreSavedData data = new TechnologyLoreSavedData();
        ListTag players = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size() && i < MAX_PLAYERS; i++) {
            CompoundTag playerTag = players.getCompound(i);
            if (!playerTag.hasUUID("Player")) continue;
            LinkedHashSet<String> ids = new LinkedHashSet<>();
            ListTag records = playerTag.getList("Technology", Tag.TAG_STRING);
            for (int j = 0; j < records.size() && ids.size() < MAX_RECORDS_PER_PLAYER; j++) {
                String id = records.getString(j);
                TechnologyLoreCatalog.TechRecord record = TechnologyLoreCatalog.byItemId(id);
                if (record != null) ids.add(record.id());
            }
            if (!ids.isEmpty()) data.discovered.put(playerTag.getUUID("Player"), ids);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag players = new ListTag();
        discovered.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("Player", entry.getKey());
            ListTag records = new ListTag();
            entry.getValue().stream().limit(MAX_RECORDS_PER_PLAYER)
                    .forEach(id -> records.add(StringTag.valueOf(id)));
            playerTag.put("Technology", records);
            players.add(playerTag);
        });
        tag.put("Players", players);
        return tag;
    }
}
