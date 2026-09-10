package matteroverdrive.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/** Persistent one-time discovery ledger for compact field technology sites. */
public final class TechnologySiteDiscoverySavedData extends SavedData {
    private static final String ID = "matteroverdrive_technology_site_discoveries";
    private final Set<String> discoveries = new HashSet<>();
    private final Set<String> explorationChains = new HashSet<>();

    public static TechnologySiteDiscoverySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TechnologySiteDiscoverySavedData::load,
                TechnologySiteDiscoverySavedData::new, ID);
    }

    public boolean discover(ServerLevel level, UUID player, String site, long position) {
        String key = level.dimension().location() + ":" + player + ":" + site + ":" + position;
        if (!discoveries.add(key)) return false;
        setDirty();
        return true;
    }

    public boolean startExplorationChain(ServerLevel level, UUID player, String chain) {
        String key = level.dimension().location() + ":" + player + ":" + chain;
        if (!explorationChains.add(key)) return false;
        setDirty();
        return true;
    }

    public boolean completeExplorationChain(ServerLevel level, UUID player, String chain) {
        String key = level.dimension().location() + ":" + player + ":" + chain;
        if (!explorationChains.remove(key)) return false;
        setDirty();
        return true;
    }

    public int count(UUID player) {
        String prefix = ":" + player + ":";
        return (int) discoveries.stream().filter(key -> key.contains(prefix)).count();
    }

    public Set<String> siteNames(UUID player) {
        Set<String> names = new TreeSet<>();
        String prefix = ":" + player + ":";
        for (String key : discoveries) {
            if (!key.contains(prefix)) continue;
            int end = key.lastIndexOf(':');
            int start = key.lastIndexOf(':', end - 1);
            if (start >= 0 && end > start + 1) names.add(key.substring(start + 1, end));
        }
        return names;
    }

    public Set<String> explorationChainNames(UUID player) {
        Set<String> names = new TreeSet<>();
        String prefix = ":" + player + ":";
        for (String key : explorationChains) {
            if (!key.contains(prefix)) continue;
            int end = key.lastIndexOf(':');
            int start = key.lastIndexOf(':', end - 1);
            if (start >= 0 && end > start + 1) names.add(key.substring(start + 1, end));
        }
        return names;
    }

    public static TechnologySiteDiscoverySavedData load(CompoundTag tag) {
        TechnologySiteDiscoverySavedData data = new TechnologySiteDiscoverySavedData();
        ListTag list = tag.getList("Discoveries", net.minecraft.nbt.Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) data.discoveries.add(list.getString(i));
        ListTag chainList = tag.getList("ExplorationChains", net.minecraft.nbt.Tag.TAG_STRING);
        for (int i = 0; i < chainList.size(); i++) data.explorationChains.add(chainList.getString(i));
        return data;
    }

    @Override public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        discoveries.stream().sorted().forEach(key -> list.add(StringTag.valueOf(key)));
        tag.put("Discoveries", list);
        ListTag chainList = new ListTag();
        explorationChains.stream().sorted().forEach(key -> chainList.add(StringTag.valueOf(key)));
        tag.put("ExplorationChains", chainList);
        return tag;
    }
}
