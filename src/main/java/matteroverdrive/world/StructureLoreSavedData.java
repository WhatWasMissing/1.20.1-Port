package matteroverdrive.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Persistent per-player record of unique Matter Overdrive structure lore discoveries. */
public final class StructureLoreSavedData extends SavedData {
    private static final String ID = "matteroverdrive_structure_lore";
    private static final int ALL_RECORDS_MASK = 0xFFFF;
    private static final int MAX_PLAYERS = 4096;

    private final Map<UUID, Integer> discovered = new HashMap<>();

    public static StructureLoreSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(StructureLoreSavedData::load, StructureLoreSavedData::new, ID);
    }

    /** Returns true only the first time this player discovers this structure class. */
    public boolean discover(UUID player, int bit) {
        if (bit == 0) return false;
        int oldMask = discovered.getOrDefault(player, 0);
        if ((oldMask & bit) != 0) return false;
        if (discovered.size() >= MAX_PLAYERS && !discovered.containsKey(player)) return false;
        discovered.put(player, (oldMask | bit) & ALL_RECORDS_MASK);
        setDirty();
        return true;
    }

    public int count(UUID player) {
        return Integer.bitCount(discovered.getOrDefault(player, 0) & ALL_RECORDS_MASK);
    }

    public boolean complete(UUID player) {
        return (discovered.getOrDefault(player, 0) & ALL_RECORDS_MASK) == ALL_RECORDS_MASK;
    }

    public static StructureLoreSavedData load(CompoundTag tag) {
        StructureLoreSavedData data = new StructureLoreSavedData();
        ListTag list = tag.getList("Players", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size() && i < MAX_PLAYERS; i++) {
            CompoundTag entry = list.getCompound(i);
            if (entry.hasUUID("Player")) {
                data.discovered.put(entry.getUUID("Player"), entry.getInt("Mask") & ALL_RECORDS_MASK);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        discovered.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            CompoundTag value = new CompoundTag();
            value.putUUID("Player", entry.getKey());
            value.putInt("Mask", entry.getValue() & ALL_RECORDS_MASK);
            list.add(value);
        });
        tag.put("Players", list);
        return tag;
    }
}
