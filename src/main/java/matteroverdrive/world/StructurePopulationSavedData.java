package matteroverdrive.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

/** Global once-per-structure-start population ledger. */
public final class StructurePopulationSavedData extends SavedData {
    private static final String ID = "matteroverdrive_structure_population";
    private static final int MAX_KEYS = 16384;
    private final Set<String> populated = new HashSet<>();

    public static StructurePopulationSavedData get(ServerLevel level) {
        ServerLevel root = level.getServer().getLevel(Level.OVERWORLD);
        if (root == null) root = level;
        return root.getDataStorage().computeIfAbsent(
                StructurePopulationSavedData::load, StructurePopulationSavedData::new, ID);
    }

    public boolean contains(String key) {
        return populated.contains(key);
    }

    public boolean mark(String key) {
        if (key == null || key.isBlank() || populated.contains(key) || populated.size() >= MAX_KEYS) return false;
        populated.add(key);
        setDirty();
        return true;
    }

    public static StructurePopulationSavedData load(CompoundTag tag) {
        StructurePopulationSavedData data = new StructurePopulationSavedData();
        ListTag list = tag.getList("Populated", net.minecraft.nbt.Tag.TAG_STRING);
        for (int i = 0; i < list.size() && i < MAX_KEYS; i++) data.populated.add(list.getString(i));
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        populated.stream().sorted().limit(MAX_KEYS).forEach(value -> list.add(StringTag.valueOf(value)));
        tag.put("Populated", list);
        return tag;
    }
}
