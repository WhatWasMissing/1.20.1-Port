package matteroverdrive.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/** Persistent one-time discovery ledger for compact field technology sites. */
public final class TechnologySiteDiscoverySavedData extends SavedData {
    private static final int MAX_DISCOVERIES = 4096;
    private static final int MAX_CHAIN_STAGES = 2048;
    private static final int MAX_KEY_LENGTH = 256;
    private static final String ID = "matteroverdrive_technology_site_discoveries";
    private final Set<String> discoveries = new HashSet<>();
    private final Map<UUID, Integer> chainStages = new HashMap<>();
    private static final String[] CHAIN = {"android_relay_outpost", "matter_observatory", "anomaly_research_site"};

    public static TechnologySiteDiscoverySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TechnologySiteDiscoverySavedData::load,
                TechnologySiteDiscoverySavedData::new, ID);
    }

    public boolean discover(ServerLevel level, UUID player, String site, long position) {
        String key = level.dimension().location() + ":" + player + ":" + site + ":" + position;
        if (key.length() > MAX_KEY_LENGTH || discoveries.size() >= MAX_DISCOVERIES) return false;
        if (!discoveries.add(key)) return false;
        setDirty();
        return true;
    }

    /** Advances the optional relay-to-observatory-to-anomaly investigation exactly once per ordered site. */
    public int advanceChain(UUID player, String site) {
        int current = Math.max(0, Math.min(CHAIN.length, chainStages.getOrDefault(player, 0)));
        if (current >= CHAIN.length || !CHAIN[current].equals(site)) return current;
        chainStages.put(player, current + 1);
        setDirty();
        return current + 1;
    }

    public static String nextChainSite(UUID player, int stage) {
        return stage >= CHAIN.length ? "complete" : CHAIN[Math.max(0, stage)];
    }

    /**
     * Returns the player's durable investigation step. Keeping this query on
     * the saved-data owner lets field UI report the same state that awards the
     * ordered-chain reward, rather than reconstructing it from discoveries.
     */
    public int chainStage(UUID player) {
        return Math.max(0, Math.min(CHAIN.length, chainStages.getOrDefault(player, 0)));
    }

    public int chainLength() {
        return CHAIN.length;
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

    public static TechnologySiteDiscoverySavedData load(CompoundTag tag) {
        TechnologySiteDiscoverySavedData data = new TechnologySiteDiscoverySavedData();
        ListTag list = tag.getList("Discoveries", net.minecraft.nbt.Tag.TAG_STRING);
        for (int i = 0; i < list.size() && i < MAX_DISCOVERIES; i++) {
            String discovery = list.getString(i);
            if (discovery.length() <= MAX_KEY_LENGTH) data.discoveries.add(discovery);
        }
        ListTag chains = tag.getList("ChainStages", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < chains.size() && i < MAX_CHAIN_STAGES; i++) {
            CompoundTag entry = chains.getCompound(i);
            if (entry.hasUUID("Player")) data.chainStages.put(entry.getUUID("Player"),
                    Math.max(0, Math.min(CHAIN.length, entry.getInt("Stage"))));
        }
        return data;
    }

    @Override public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        discoveries.stream().sorted().forEach(key -> list.add(StringTag.valueOf(key)));
        tag.put("Discoveries", list);
        ListTag chains = new ListTag();
        chainStages.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> { CompoundTag value = new CompoundTag(); value.putUUID("Player", entry.getKey()); value.putInt("Stage", entry.getValue()); chains.add(value); });
        tag.put("ChainStages", chains);
        return tag;
    }
}
